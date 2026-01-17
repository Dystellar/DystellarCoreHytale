package gg.dystellar.core;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import gg.dystellar.core.services.Services;
import gg.dystellar.core.utils.Hooks;
import gg.dystellar.core.common.PacketListener;
import gg.dystellar.core.common.User;
import gg.dystellar.core.common.inbox.Inbox;
import gg.dystellar.core.config.Config;
import gg.dystellar.core.config.Messages;
import gg.dystellar.core.config.Setup;
import gg.dystellar.core.listeners.*;
import gg.dystellar.core.arenasapi.AbstractArena;
import gg.dystellar.core.commands.*;

public final class DystellarCore extends JavaPlugin {

    private static DystellarCore INSTANCE;

    public static DystellarCore getInstance() { return INSTANCE; }
	public static HytaleLogger getLog() { return getInstance().LOGGER; }

	private final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    /**
     * Initialize plugin
     */
	public DystellarCore(JavaPluginInit init) {
		super(init);
		INSTANCE = this;
        Hooks.registerHooks();
        loadConfig();
        initialize();

        if (ConfValues.AUTOMATED_MESSAGES_ENABLED)
            Services.startAutomatedMessagesService();

        Bukkit.getMessenger().registerOutgoingPluginChannel(this, CHANNEL);
        Bukkit.getMessenger().registerIncomingPluginChannel(this, CHANNEL, this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        Bukkit.getPluginManager().registerEvents(this, this);

        // Listeners start
        if (ConfValues.SCOREBOARD_ENABLED) new Scoreboards();
        if (ConfValues.HANDLE_SPAWN_MECHANICS) new SpawnMechanics();
        if (ConfValues.PACK_ENABLED) new ResourceListener();
        new User.UserListener(); new Inbox.SenderListener(); new Punish();
        new PacketListener(); new GeneralListeners();
        // Listeners end

        // Commands start
        if (ConfValues.HANDLE_SPAWN_PROTECTION) new EditmodeCommand();
        new SetSpawnCommand(); new GameModeCommand(); new HealCommand();
        new FlyCommand(); new FreezeCommand(); new BroadcastCommand();
        new JoinCommand(); new MSGCommand(); new ReplyCommand();
        new BlacklistCommand(); new MuteCommand();
        new NoteCommand(); new PunishmentsCommand(); new NotesCommand();
        new GiveItemCommand(); new ItemMetaCommand(); new PingCommand();
        new ToggleChatCommand(); new TogglePrivateMessagesCommand();
        new ToggleGlobalTabComplete(); new IgnoreCommand(); new IgnoreListCommand();
        new InboxCommand(); new FriendCommand(); new SuffixCommand();
        new WandCommand(); new UnpunishCommand();
        // Commands end

        AbstractArena.init();
	}

    /**
     * Setup commands
     */
	@Override
	protected void setup() {
		this.getCommandRegistry().registerCommand(new BanCommand("ban", "Dystellar's custom ban command"));
	}

    public static final String CHANNEL = "dyst:ellar";

    public final File conf = new File(getDataFolder(), "config.yml");
    public final File si = new File(getDataFolder(), "spawnitems.yml");
    public final File am = new File(getDataFolder(), "automated-messages.txt");

	private final Config<Setup> config = new Config<>("setup.json", Setup.class);
	private final Config<Messages> lang_en = new Config<>("lang_en.json", Messages.class);
    private final YamlConfiguration spawnitems = YamlConfiguration.loadConfiguration(si);

    @Override
    public void onDisable() {
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(this, "BungeeCord");
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this, "BungeeCord");
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(this, CHANNEL);
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this, CHANNEL);
    }

    private void loadConfig() {

        try {
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[Dystellar] Loading configuration...");
            saveResource("config.yml", false);
            saveResource("spawnitems.yml", false);
            saveResource("lang-en.yml", false);
            config.load(conf);
            spawnitems.load(si);
            lang.load(m);
            String currentVersion = lang.getString("config-version");
            InputStreamReader reader0 = new InputStreamReader(getResource("lang-en.yml"));
            YamlConfiguration rawLang = YamlConfiguration.loadConfiguration(reader0);
            reader0.close();
            String newVersion = rawLang.getString("config-version");
            if (currentVersion.equals("1.0") && newVersion.equals("1.1")) {
                saveResource("lang-en.yml", true);
                lang.load(m);
            }
            Msgs.init();
            if (am.createNewFile()) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClassLoader().getResourceAsStream("automated-messages.txt")))); PrintWriter writer = new PrintWriter(am)) {
                    reader.lines().forEach(writer::println);
                }
            }
            Bukkit.getConsoleSender().sendMessage("[Dystellar] Configuration loaded successfully");
            try {
                MariaDB.loadFromConfig();
                Bukkit.getLogger().info("Testing database configuration provided in config.yml");
                MariaDB.dataSourceTestInit();
                initDb();
                Bukkit.getLogger().info("Your configuration looks great!");
            } catch (Exception e) {
                e.printStackTrace();
                Bukkit.getLogger().severe("Failed to initialize database, check your configuration. Server will now shutdown.");
                Bukkit.getServer().shutdown();
            }
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public YamlConfiguration getLang() {
        return lang;
    }

    private void initDb() throws IOException, SQLException {
        String setup;
        try (InputStream in = getClassLoader().getResourceAsStream("database.sql");
				BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            setup = reader.lines().collect(Collectors.joining());
            String[] queries = setup.split(";");
            for (String query : queries) {
                if (query.isEmpty()) continue;
                try (Connection connection = MariaDB.getConnection()) {
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.execute();
                }
            }
        }
        getLogger().info("§2Database setup complete.");
    }

    private void initialize() {
        ConfValues.init(getConfig());
        Suffix.initialize();
    }

    @Override
    public YamlConfiguration getConfig() {
        return config;
    }

    public YamlConfiguration getSpawnitems() {
        return spawnitems;
    }

    @Override
    public void saveConfig() {
        try {
            config.save(conf);
            spawnitems.save(si);
            lang.save(m);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public final Map<UUID, UUID> requests = new HashMap<>();
}
