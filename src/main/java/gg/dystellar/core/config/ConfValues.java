package gg.dystellar.core.config;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.serialization.LocationSerialization;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ConfValues {

    public static String BROADCAST_FORMAT;
    public static String SCOREBOARD_TITLE;
    public static List<String> SCOREBOARD_LINES;
    public static List<String> FREEZE_MESSAGE;
    public static String UNFREEZE_MESSAGE;
    public static boolean SCOREBOARD_ENABLED = false;
    public static boolean VOID_TELEPORT = false;
    public static boolean JOIN_TELEPORT = false;
    public static Location SPAWN_LOCATION;
    public static boolean ALLOW_BANNED_PLAYERS = false;
    public static String BAN_MESSAGE;
    public static String BLACKLIST_MESSAGE;
    public static String RANKED_BAN_MESSAGE;
    public static String MUTE_MESSAGE;
    public static boolean HANDLE_SPAWN_PROTECTION = false;
    public static boolean HANDLE_SPAWN_MECHANICS = false;
    public static String PLAYER_MSG_DISABLED;
    public static String MSG_SEND_FORMAT;
    public static String MSG_RECEIVE_FORMAT;
    public static List<String> WARN_MESSAGE;
    public static String KICK_MESSAGE;
    public static int REFRESH_RATE_SCORE;
    public static boolean ALLOW_SIGNS;
    public static boolean AUTOMATED_MESSAGES_ENABLED = true;
    public static int AUTOMATED_MESSAGES_RATE = 360;
    public static final List<String> AUTOMATED_MESSAGES = new ArrayList<>();
    private static final AtomicInteger i = new AtomicInteger();
    public static boolean PREVENT_WEATHER = true;
    public static boolean PACK_ENABLED = false;
    public static String PACK_LINK;
    public static boolean DEBUG_MODE = false;

    public static void init(YamlConfiguration conf) {
        BROADCAST_FORMAT = ChatColor.translateAlternateColorCodes('&', conf.getString("broadcast-format"));
        SCOREBOARD_TITLE = ChatColor.translateAlternateColorCodes('&', conf.getString("scoreboard.title"));
        SCOREBOARD_LINES = new ArrayList<>();
        for (String line : conf.getStringList("scoreboard.lines")) {
            SCOREBOARD_LINES.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        REFRESH_RATE_SCORE = conf.getInt("scoreboard.refresh-rate");
        FREEZE_MESSAGE = new ArrayList<>();
        for (String line : conf.getStringList("freeze-message")) {
            FREEZE_MESSAGE.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        UNFREEZE_MESSAGE = ChatColor.translateAlternateColorCodes('&', conf.getString("unfreeze-message"));
        SCOREBOARD_ENABLED = conf.getBoolean("scoreboard-enabled");
        VOID_TELEPORT = conf.getBoolean("teleport-on-void");
        JOIN_TELEPORT = conf.getBoolean("teleport-on-join");
        if (conf.contains("spawn-location")) SPAWN_LOCATION = LocationSerialization.stringToLocation(conf.getString("spawn-location"));
        ALLOW_BANNED_PLAYERS = conf.getBoolean("allow-banned-players");
        List<String> ban_msg = conf.getStringList("ban-message");
        StringBuilder builder = new StringBuilder();
        for (String ban : ban_msg) {
            builder.append(ChatColor.translateAlternateColorCodes('&', ban)).append("\n");
        }
        builder.delete(builder.length() - 3, builder.length());
        BAN_MESSAGE = builder.toString();
        List<String> blacklist_msg = conf.getStringList("blacklist-message");
        builder = new StringBuilder();
        for (String blacklist : blacklist_msg) {
            builder.append(ChatColor.translateAlternateColorCodes('&', blacklist)).append("\n");
        }
        BLACKLIST_MESSAGE = builder.toString();
        RANKED_BAN_MESSAGE = ChatColor.translateAlternateColorCodes('&', conf.getString("ranked-ban-message"));
        MUTE_MESSAGE = ChatColor.translateAlternateColorCodes('&', conf.getString("mute-message"));
        HANDLE_SPAWN_PROTECTION = conf.getBoolean("handle-spawn-protection");
        HANDLE_SPAWN_MECHANICS = conf.getBoolean("handle-spawn-mechanics");
        PLAYER_MSG_DISABLED = ChatColor.translateAlternateColorCodes('&', conf.getString("player-msg-disabled"));
        MSG_SEND_FORMAT = ChatColor.translateAlternateColorCodes('&', conf.getString("msg-send-format"));
        MSG_RECEIVE_FORMAT = ChatColor.translateAlternateColorCodes('&', conf.getString("msg-receive-format"));
        WARN_MESSAGE = new ArrayList<>();
        for (String line : conf.getStringList("warn-message")) {
            WARN_MESSAGE.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        KICK_MESSAGE = ChatColor.translateAlternateColorCodes('&', conf.getString("kick-message"));
        ALLOW_SIGNS = conf.getBoolean("block-signs-crafting");
        AUTOMATED_MESSAGES_ENABLED = conf.getBoolean("automated-messages-enabled");
        AUTOMATED_MESSAGES_RATE = conf.getInt("automated-messages-rate");
        PREVENT_WEATHER = conf.getBoolean("prevent-weather-changing");
        DEBUG_MODE = conf.getBoolean("debug-mode");
        try (BufferedReader reader = new BufferedReader(new FileReader(DystellarCore.getInstance().am))) {
            reader.lines().forEach(s -> {
                if (s.startsWith("-=")) {
                    AUTOMATED_MESSAGES.add(s.substring(2));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        PACK_ENABLED = conf.getBoolean("send-texturepack");
        PACK_LINK = conf.getString("texturepack-link");
    }
}
