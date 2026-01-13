package net.zylesh.dystellarcore.serialization;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.inbox.Inbox;
import net.zylesh.dystellarcore.core.Suffix;
import net.zylesh.dystellarcore.core.User;
import net.zylesh.dystellarcore.core.inbox.Sendable;
import net.zylesh.dystellarcore.core.punishments.Ban;
import net.zylesh.dystellarcore.core.punishments.Punishment;
import net.zylesh.dystellarcore.core.punishments.SenderContainer;
import net.zylesh.dystellarcore.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.mariadb.jdbc.MariaDbDataSource;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class MariaDB {

    private MariaDB() {}

    private static final int SERIALIZAION_VERSION = 1;

    private static String HOST;
    private static int PORT;
    private static String DATABASE;
    private static String USER;
    private static String PASSWORD;
    private static MariaDbDataSource DS;

    public static void loadFromConfig() {
        HOST = DystellarCore.getInstance().getConfig().getString("mariadb.host");
        PORT = DystellarCore.getInstance().getConfig().getInt("mariadb.port");
        DATABASE = DystellarCore.getInstance().getConfig().getString("mariadb.database");
        USER = DystellarCore.getInstance().getConfig().getString("mariadb.user");
        PASSWORD = DystellarCore.getInstance().getConfig().getString("mariadb.password");
    }

    public static void dataSourceTestInit() throws SQLException {
        MariaDbDataSource ds = new MariaDbDataSource("jdbc:mariadb://" + HOST + ":" + PORT + "/" + DATABASE);
        ds.setLoginTimeout(5);
        ds.setUser(USER);
        ds.setPassword(PASSWORD);
        DS = ds;
        try (Connection connection = getConnection()) {
            if (!connection.isValid(5)) {
                throw new SQLException("Could not create a connection.");
            }
        }
    }

    @Nullable
    public static User loadPlayerFromDatabase(UUID uuid, String IP, String name) {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(
                "SELECT chat, messages, suffix, punishments, notes, lang, inbox, version, tabcompletion, scoreboard, ignoreList, friends, otherConfigs, tips FROM players_core WHERE uuid = ?;"
        )) {
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();
            User user;
            if (resultSet.next()) {
                user = new User(uuid, IP, name);
                user.setGlobalChatEnabled(resultSet.getBoolean("chat"));
                user.setPrivateMessagesMode((byte) resultSet.getInt("messages"));
                user.setSuffix(Suffix.valueOf(resultSet.getString("suffix")));
                Punishments.deserializePunishments(resultSet.getString("punishments"), user.getPunishments());
                if (resultSet.getString("notes") != null) user.getNotes().addAll(Punishments.deserializeNotes(resultSet.getString("notes")));
                user.setLanguage(resultSet.getString("lang"));
                String inbox = resultSet.getString("inbox");
                if (inbox == null) {
                    user.assignInbox(new Inbox(user));
                } else {
                    user.assignInbox(InboxSerialization.stringToInbox(inbox, user));
                }
                int version = resultSet.getInt("version");
                user.setVersion(version);
                user.setGlobalTabComplete(resultSet.getBoolean("tabcompletion"));
                user.setScoreboardEnabled(resultSet.getBoolean("scoreboard"));
                for (String uuids : resultSet.getString("ignoreList").split(";"))
                    user.getIgnoreList().add(UUID.fromString(uuids));
                if (version == 0) {
                    user.assignTips(Utils.stringToBytes(resultSet.getString("tips"), true));
                    user.assignExtraOptions(Utils.stringToBytes(resultSet.getString("otherConfigs"), true));
                } else {
                    user.assignTips(Utils.stringToBytes(resultSet.getString("tips"), false));
                    user.assignExtraOptions(Utils.stringToBytes(resultSet.getString("otherConfigs"), false));
                }
                return user;
            } else {
                user = new User(uuid, IP, name);

                byte[] tips = new byte[50];
                user.assignTips(tips);

                byte[] otherConfigs = new byte[50];
                otherConfigs[Consts.EXTRA_OPTION_FRIEND_REQUESTS_ENABLED_POS] = Consts.BYTE_TRUE;
                otherConfigs[Consts.EXTRA_OPTION_RESOURCEPACK_PROMPT_POS] = Consts.BYTE_TRUE;
                user.assignExtraOptions(otherConfigs);

                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Could not load data for " + uuid.toString());
        }
        return null;
    }

    @Nullable
    public static Mapping loadMapping(String IP) {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT something1, something2, punishments FROM mappings WHERE something0 = ?;")) {
            statement.setString(1, IP);
            ResultSet rs = statement.executeQuery();
            if (!rs.next()) return null;
            Set<Punishment> punishmentSet = new HashSet<>();
            for (String s : rs.getString("punishments").split(":")) punishmentSet.add(Punishments.deserialize(s));
            return new Mapping(UUID.fromString(rs.getString("something2")), IP, rs.getString("something1"), punishmentSet);
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Could not load mapping for " + IP);
        }
        return null;
    }

    /**
     * Highly recommended to do this async
     * @param user player to save
     */
    public static void savePlayerToDatabase(User user) {
        Inbox.SenderListener.unregisterInbox(user.getUUID());
        StringBuilder ipP = null;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("REPLACE players_core(uuid, chat, messages, suffix, punishments, notes, lang, inbox, version, tabcompletion, scoreboard, ignoreList, friends, otherConfigs, tips) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);")
        ) {
            statement.setString(1, user.getUUID().toString());
            statement.setBoolean(2, user.isGlobalChatEnabled());
            statement.setInt(3, user.getPrivateMessagesMode());
            statement.setString(4, user.getSuffix().name());
            if (user.getPunishments().isEmpty()) statement.setString(5, null);
            else {
                StringBuilder sb = new StringBuilder();
                for (Punishment p : user.getPunishments()) {
                    String pun = Punishments.serialize(p);
                    sb.append(pun).append(":\\|");
                    if (p instanceof Ban && ((Ban) p).isAlsoIP()) {
                        if (ipP == null) ipP = new StringBuilder();
                        ipP.append(pun).append(":\\|");
                    }
                }
                statement.setString(5, sb.toString());
            }
            if (user.getNotes().isEmpty()) statement.setString(6, null);
            else statement.setString(6, Punishments.serializeNotes(user.getNotes()));
            statement.setString(7, user.getLanguage());
            statement.setString(8, InboxSerialization.inboxToString(user.getInbox()));
            statement.setInt(9, SERIALIZAION_VERSION);
            statement.setBoolean(10, user.isGlobalTabComplete());
            statement.setBoolean(11, user.isScoreboardEnabled());
            StringBuilder builder = new StringBuilder();
            for (UUID uuid : user.getIgnoreList()) builder.append(uuid).append(";");
            statement.setString(12, builder.toString());
            builder = new StringBuilder();
            for (UUID uuid : user.friends) builder.append(uuid).append(";");
            statement.setString(13, builder.toString());

            statement.setString(14, Utils.bytesToString(user.extraOptions));

            statement.setString(15, Utils.bytesToString(user.tipsSent));
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Could not save data for " + user.getUUID());
        }
        try (Connection connection = getConnection(); PreparedStatement statement1 = connection.prepareStatement("REPLACE mappings(something0, something1, something2, punishments) VALUES(?, ?, ?, ?);")) // UUID, IP, Name.)
        {
            statement1.setString(1, user.getUUID().toString());
            statement1.setString(2, user.getIp());
            statement1.setString(3, user.getName());
            statement1.setString(4, null);
            statement1.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Could not save UUID mappings for " + user.getUUID());
        }
        try (Connection connection = getConnection(); PreparedStatement statement2 = connection.prepareStatement("REPLACE mappings(something0, something1, something2, punishments) VALUES(?, ?, ?, ?);")) // IP, Name, UUID. // UUID, IP, Name.)
        {
            statement2.setString(1, user.getIp());
            statement2.setString(2, user.getName());
            statement2.setString(3, user.getUUID().toString());
            if (ipP == null) statement2.setString(4, "");
            else statement2.setString(4, ipP.toString());
            statement2.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Could not save IP mappings for " + user.getIp());
        }
        try (Connection connection = getConnection(); PreparedStatement statement3 = connection.prepareStatement("REPLACE mappings(something0, something1, something2, punishments) VALUES(?, ?, ?, ?);")) // IP, Name, UUID. // UUID, IP, Name.)
        {
            statement3.setString(1, user.getName());
            statement3.setString(2, user.getUUID().toString());
            statement3.setString(3, user.getIp());
            statement3.setString(4, null);
            statement3.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Could not save name mappings for " + user.getName());
        }
    }

    public static void deletePlayerData(User user) {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM players_core WHERE uuid = ?;"
        )) {
            statement.setString(1, user.getUUID().toString());
            statement.execute();
            if (User.getUsers().containsKey(user.getUUID())) {
                User realUser = User.get(user.getUUID());
                User user1 = new User(realUser.getUUID(), realUser.getIp(), realUser.getName());
                User.getUsers().replace(realUser.getUUID(), user1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Could not delete data for " + user.toString());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DS.getConnection();
    }

    public static void deleteAllData(CommandSender sender) {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(
                "SELECT uuid FROM players_core;"
        )) {
            ResultSet resultSet = statement.executeQuery();
            List<User> players = new ArrayList<>();
            while (resultSet.next()) {
                User user = new User(UUID.fromString(resultSet.getString("uuid")), "", "");
                players.add(user);
            }
            players.forEach(MariaDB::deletePlayerData);
            sender.sendMessage(ChatColor.GREEN + "You deleted all data.");
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Could not delete data.");
        }
    }

    public static Set<Mapping> getUUIDMappings(UUID... uuids) {
        Set<Mapping> uuidMappings = new HashSet<>();
        Set<UUID> uuids1 = new HashSet<>(Arrays.asList(uuids));
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT something0, something1, something2, punishments FROM mappings;")) {
            ResultSet rs = statement.executeQuery();
            if (uuids1.isEmpty()) {
                while (rs.next()) {
                    String uuid = rs.getString("something0");
                    if (!stringIsUUID(uuid)) continue;
                    String ip = rs.getString("something1");
                    String name = rs.getString("something2");
                    Set<Punishment> punishmentSet = null;
                    String punishments = rs.getString("punishments");
                    if (punishments != null) {
                        punishmentSet = new HashSet<>();
                        for (String s : punishments.split(":"))
                            punishmentSet.add(Punishments.deserialize(s));
                    }
                    uuidMappings.add(new Mapping(UUID.fromString(uuid), ip, name, punishmentSet));
                }
            } else {
                while (rs.next()) {
                    String uuid = rs.getString("something0");
                    UUID realUUID = UUID.fromString(uuid);
                    if (!stringIsUUID(uuid) || !uuids1.contains(realUUID)) continue;
                    String ip = rs.getString("something1");
                    String name = rs.getString("something2");
                    Set<Punishment> punishmentSet = null;
                    String punishments = rs.getString("punishments");
                    if (punishments != null) {
                        punishmentSet = new HashSet<>();
                        for (String s : punishments.split(":"))
                            punishmentSet.add(Punishments.deserialize(s));
                    }
                    uuidMappings.add(new Mapping(UUID.fromString(uuid), ip, name, punishmentSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return uuidMappings;
    }

    /*
    UUID, IP, Name
    IP, Name, UUID
    Name, UUID, IP
     */

    @Nullable
    public static UUID loadUUID(String aString) {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT something1, something2 FROM mappings WHERE something0 = ?;")) {
            statement.setString(1, aString);
            ResultSet rs = statement.executeQuery();
            if (!rs.next()) return null;
            if (stringIsIP(aString)) {
                return UUID.fromString(rs.getString("something2"));
            } else {
                return UUID.fromString(rs.getString("something1"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Could not load UUID from " + aString);
        }
        return null;
    }

    @Nullable
    public static String loadIP(String aString) {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT something1, something2 FROM mappings WHERE something0 = ?;")) {
            statement.setString(1, aString);
            ResultSet rs = statement.executeQuery();
            if (!rs.next()) return null;
            if (stringIsUUID(aString)) {
                return rs.getString("something1");
            } else {
                return rs.getString("something2");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Could not load UUID from " + aString);
        }
        return null;
    }

    @Nullable
    public static String loadName(String aString) {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT something1, something2 FROM mappings WHERE something0 = ?;")) {
            statement.setString(1, aString);
            ResultSet rs = statement.executeQuery();
            if (!rs.next()) return null;
            if (stringIsUUID(aString)) {
                return rs.getString("something2");
            } else if (stringIsIP(aString)) {
                return rs.getString("something1");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Could not load UUID from " + aString);
        }
        return null;
    }

    public static SenderContainer[] loadSenderContainers() {
        Set<SenderContainer> containers = new HashSet<>();
        try (
                Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT id, serialized FROM senders;")
        ) {
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Sendable sender = InboxSerialization.stringToSender(rs.getString("serialized"), null);
                containers.add(new SenderContainer(sender));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Could not load sender containers.");
        }
        return containers.toArray(new SenderContainer[0]);
    }

    public static void saveSenderContainer(SenderContainer container) {
        try (
                Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement("REPLACE senders(id, serialized) VALUES(?, ?);")
        ) {
            statement.setInt(1, container.getSender().getId());
            statement.setString(2, InboxSerialization.senderToString(container.getSender(), container.getSender().getSerialID()));
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Could not save sender container.");
        }
    }

    public static void deleteSenderContainer(int id) {
        try (
                Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement("DELETE FROM senders WHERE id = ?;;")
        ) {
            statement.setInt(1, id);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Could not delete sender container.");
        }
    }

    public static boolean stringIsIP(String s) {
        return s.split("\\.").length == 4 && s.matches("[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+");
    }

    public static boolean stringIsUUID(String s) {
        return s.length() == 36;
    }
}
