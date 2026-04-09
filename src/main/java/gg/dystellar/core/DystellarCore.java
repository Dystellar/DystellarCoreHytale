package gg.dystellar.core;

import java.io.*;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.hypixel.hytale.builtin.weather.resources.WeatherResource;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.ShutdownReason;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;

import gg.dystellar.core.common.UserComponent;
import gg.dystellar.core.config.Messages;
import gg.dystellar.core.config.Setup;
import gg.dystellar.core.listeners.*;
import gg.dystellar.core.perms.CustomPermProvider;
import gg.dystellar.core.perms.Group;
import gg.dystellar.core.messaging.Handler;
import gg.dystellar.core.commands.*;
import gg.dystellar.core.api.API;
import gg.dystellar.core.api.Config;
import gg.dystellar.core.api.comms.Channel;

public final class DystellarCore extends JavaPlugin {

	private static DystellarCore INSTANCE;
	private static API API;
	private static Channel CHANNEL;

	public static DystellarCore getInstance() { return INSTANCE; }
	public static HytaleLogger getLog() { return getInstance().LOGGER; }
	public static API getApi() { return API; }
	public static Channel getChannel() { return CHANNEL; }

	private final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

	private static final Gson GSON_SKIP_FALSE_BOOLS = new GsonBuilder()
		.setPrettyPrinting()
		.registerTypeAdapter(Boolean.class, new TypeAdapter<Boolean>() {
			@Override
			public Boolean read(JsonReader arg0) throws IOException { return arg0.nextBoolean(); }

			@Override
			public void write(JsonWriter arg0, Boolean arg1) throws IOException {
				if (Boolean.TRUE.equals(arg1))
					arg0.value(true);
				else arg0.nullValue();
			}
		})
		.create();

	/**
	 * Initialize plugin
	 */
	public DystellarCore(JavaPluginInit init) {
		super(init);
		INSTANCE = this;
		loadConfig();

		if (config.get().automated_messages)
			startAutomatedMessages();
	}

	/**
	 * Setup commands
	 */
	@Override
	protected void setup() {
		initialize();

		// Listeners
		JoinsListener.register(this);
		FreezeCommand.register(this);

		// Commands
		this.getCommandRegistry().registerCommand(new BanCommand());
		this.getCommandRegistry().registerCommand(new BlacklistCommand());
		this.getCommandRegistry().registerCommand(new BroadcastCommand());
		this.getCommandRegistry().registerCommand(new FlyCommand());
		this.getCommandRegistry().registerCommand(new FreezeCommand());
		this.getCommandRegistry().registerCommand(new FriendCommand());
		this.getCommandRegistry().registerCommand(new IgnoreCommand());
		this.getCommandRegistry().registerCommand(new IgnoreListCommand());
		this.getCommandRegistry().registerCommand(new JoinCommand());
		this.getCommandRegistry().registerCommand(new MSGCommand());
		this.getCommandRegistry().registerCommand(new MuteCommand());
		this.getCommandRegistry().registerCommand(new NoteCommand());
		this.getCommandRegistry().registerCommand(new NotesCommand());
		this.getCommandRegistry().registerCommand(new PermsCommand());
		this.getCommandRegistry().registerCommand(new PunishmentsCommand());
		this.getCommandRegistry().registerCommand(new UnpunishCommand());
		this.getCommandRegistry().registerCommand(new FriendCommand());
		this.getCommandRegistry().registerCommand(new SuffixCommand());
		this.getCommandRegistry().registerCommand(new ToggleChatCommand());
		this.getCommandRegistry().registerCommand(new TogglePrivateMessagesCommand());
		this.getCommandRegistry().registerCommand(new ReplyCommand());

		// Register provider
		PermissionsModule.get().addProvider(new CustomPermProvider());

		if (config.get().prevent_weather) {
			for (final var w : Universe.get().getWorlds().values()) {
				final var store = w.getEntityStore().getStore();
				final var resource = store.getResource(WeatherResource.getResourceType());

				resource.setForcedWeather(config.get().forced_weather);
			}
		}
	}

	public final Config<Setup> config = new Config<>(this, "setup.json", Setup.class);
	public final Config<Messages> lang_en = new Config<>(this, "lang_en.json", Messages.class, GSON_SKIP_FALSE_BOOLS);
	public final Config<Messages> lang_es = new Config<>(this, "lang_es.json", Messages.class, GSON_SKIP_FALSE_BOOLS);

	public void loadConfig() {
		try {
			LOGGER.atInfo().log("[Dystellar] Loading configuration...");
			config.load();

			API = new API(config.get().api, config.get().websocket_endpoint, config.get().server_name, config.get().api_key);
			CHANNEL = API.wsClient.registerChannel("core", Handler::handle, (_,_,_) -> {});

			lang_en.load();
			lang_es.load();

			lang_en.get().compile();
			lang_es.get().compile();

			Group.initGroups();

			LOGGER.atInfo().log("[Dystellar] Configuration loaded successfully");
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.atSevere().log("[Dystellar] Failed to load core plugin, server will shutdown");
			HytaleServer.get().shutdownServer(ShutdownReason.CRASH);
		}
	}

	public Messages getLang(String lang) {
		switch (lang) {
			case "es": return lang_es.get();
			default: return lang_en.get();
		}
	}

	public Setup getSetup() {
		return this.config.get();
	}

	private void initialize() {
		UserComponent.init(this);
	}

	private void startAutomatedMessages() {
		final byte[] b = {0, 0};

		HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> {
			b[0]++;
			b[1]++;
			if (b[0] >= lang_en.get().automatedMessages.length)
				b[0] = 0;
			if (b[1] >= lang_es.get().automatedMessages.length)
				b[1] = 0;

			try {
				Universe.get().getPlayers().forEach(p -> {
					if (p.isValid()) {
						final var user = p.getHolder().getComponent(UserComponent.getComponentType());

						switch (user.language) {
							case "es" -> p.sendMessage(lang_es.get().automatedMessages[b[1]].buildMessage());
							default -> p.sendMessage(lang_en.get().automatedMessages[b[0]].buildMessage());
						}
					}
				});
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}, config.get().automated_messages_rate, config.get().automated_messages_rate, TimeUnit.SECONDS);
	}
}
