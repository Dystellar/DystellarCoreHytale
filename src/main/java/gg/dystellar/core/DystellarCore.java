package gg.dystellar.core;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.ShutdownReason;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;

import gg.dystellar.core.utils.Hooks;
import gg.dystellar.core.common.PacketListener;
import gg.dystellar.core.common.Suffix;
import gg.dystellar.core.common.UserComponent;
import gg.dystellar.core.common.inbox.Inbox;
import gg.dystellar.core.config.Messages;
import gg.dystellar.core.config.Setup;
import gg.dystellar.core.listeners.*;
import gg.dystellar.core.perms.CustomPermProvider;
import gg.dystellar.core.perms.Group;
import gg.dystellar.core.arenasapi.AbstractArena;
import gg.dystellar.core.commands.*;
import gg.dystellar.core.api.API;
import gg.dystellar.core.api.Config;

public final class DystellarCore extends JavaPlugin {

	private static DystellarCore INSTANCE;
	private static API API;

	public static DystellarCore getInstance() { return INSTANCE; }
	public static HytaleLogger getLog() { return getInstance().LOGGER; }
	public static API getApi() { return API; }

	private final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

	/**
	 * Initialize plugin
	 */
	public DystellarCore(JavaPluginInit init) {
		super(init);
		INSTANCE = this;
		Hooks.registerHooks();
		loadConfig();

		if (config.get().automated_messages)
			startAutomatedMessages();

		// Listeners start
		new Inbox.SenderListener(); new Punish();
		new PacketListener(); new GeneralListeners();
		// Listeners end

		// Commands start
		new SetSpawnCommand(); new GameModeCommand();
		new JoinCommand(); new MSGCommand(); new ReplyCommand();
		new MuteCommand();
		new NoteCommand(); new PunishmentsCommand(); new NotesCommand();
		new GiveItemCommand(); new ItemMetaCommand(); new PingCommand();
		new ToggleChatCommand(); new TogglePrivateMessagesCommand();
		new ToggleGlobalTabComplete(); new IgnoreCommand(); new IgnoreListCommand();
		new InboxCommand(); new FriendCommand(); new SuffixCommand();
		new WandCommand(); new UnpunishCommand();
	}

	/**
	 * Setup commands
	 */
	@Override
	protected void setup() {
		initialize();
		AbstractArena.init();

		// Listeners
		JoinsListener.register(this);
		FreezeCommand.register(this);

		// Commands
		this.getCommandRegistry().registerCommand(new BanCommand());
		this.getCommandRegistry().registerCommand(new BlacklistCommand());
		this.getCommandRegistry().registerCommand(new BroadcastCommand());
		this.getCommandRegistry().registerCommand(new FlyCommand());
		this.getCommandRegistry().registerCommand(new FreezeCommand());

		// Register provider
		PermissionsModule.get().addProvider(new CustomPermProvider());
	}

	public final Config<Setup> config = new Config<>(this, "setup.json", Setup.class);
	public final Config<Messages> lang_en = new Config<>(this, "lang_en.json", Messages.class);
	public final Config<Messages> lang_es = new Config<>(this, "lang_es.json", Messages.class);

	public void loadConfig() {
		try {
			LOGGER.atInfo().log("[Dystellar] Loading configuration...");
			config.load();

			API = new API(config.get().api, config.get().api_key);

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
		Suffix.initialize();
	}

	private void startAutomatedMessages() {
		final byte[] b = {0, 0};

		HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> {
			b[0]++;
			b[1]++;
			if (b[0] >= lang_en.get().automatedMessages.length)
				b[0] = 0;
			if (b[1] >= lang_en.get().automatedMessages.length)
				b[1] = 0;

			try {
				Universe.get().getPlayers().forEach(p -> {
					if (p.isValid()) {
						final var user = p.getHolder().getComponent(UserComponent.getComponentType());

						switch (user.language) {
							case "es" -> p.sendMessage(lang_es.get().automatedMessages[b[1]]);
							default -> p.sendMessage(lang_en.get().automatedMessages[b[0]]);
						}
					}
				});
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}, config.get().automated_messages_rate, config.get().automated_messages_rate, TimeUnit.SECONDS);
	}

	public void addInboxMessage(UUID target, Sendable sender, Player issuer) {
		if (User.getUsers().containsKey(target)) {
			User.get(target).getInbox().addSender(sender);
		} else sendInbox(sender, issuer, target);
	}

	private void sendInbox(Sendable sender, Player player, UUID target) {
		Utils.sendPluginMessage(player, Types.INBOX_UPDATE, sender.encode(target));
		/*if (sender instanceof PKillEffectReward) {
			PKillEffectReward reward = (PKillEffectReward) sender;
			String effect = reward.getKillEffect().name();
			String title = reward.getTitle();
			String msg = reward.getSerializedMessage();
			String from = reward.getFrom();
			Boolean claimed = reward.isClaimed();
			Boolean deleted = reward.isDeleted();
			Collections.addAll(obj, target.toString(), PKILL_EFFECT, id, submission, effect, title, msg, from, claimed, deleted);
		} else if (sender instanceof EloGainNotifier) {
			EloGainNotifier reward = (EloGainNotifier) sender;
			Integer elo = reward.getElo();
			byte compatibility = reward.getCompatibilityType();
			String ladder = reward.getLadder();
			String msg = reward.getSerializedMessage();
			String from = reward.getFrom();
			Boolean claimed = reward.isClaimed();
			Boolean deleted = reward.isDeleted();
			if (compatibility == EloGainNotifier.PRACTICE) Collections.addAll(obj, target.toString(), ELO_GAIN_NOTIFIER, id, submission, elo, compatibility, ladder, msg, from, claimed, deleted);
			else if (compatibility == EloGainNotifier.SKYWARS) Collections.addAll(obj, target.toString(), ELO_GAIN_NOTIFIER, id, submission, elo, compatibility, msg, from, claimed, deleted);
		}*/
	}

	@Override
	public void onPluginMessageReceived(String s, @NotNull Player p, byte[] bytes) {
		if (!s.equalsIgnoreCase(CHANNEL))
			return;
		Types.handle(p, bytes);
	}
}
