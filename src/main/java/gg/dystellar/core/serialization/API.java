package gg.dystellar.core.serialization;

import gg.dystellar.core.DystellarCore;
import gg.dystellar.core.common.User;

import javax.annotation.Nullable;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Database storage, should use the backend instead.
 */
public final class API {

	private final Gson gson = new Gson();

    public API(String url, String token) throws IOException, InterruptedException {
		this.url = url;
		this.token = token;
		this.client = HttpClient.newBuilder()
			.version(Version.HTTP_2)
			.followRedirects(Redirect.NEVER)
			.connectTimeout(Duration.ofSeconds(20))
			.build();
		this.testConnection();
	}

	private final HttpClient client;
	private final String url;
	private final String token;

	private Response getJson(String path) throws IOException, InterruptedException {
		final var request = HttpRequest.newBuilder(URI.create(this.url + path))
			.method("GET", BodyPublishers.noBody())
			.header("Authorization", this.token)
			.header("Content-Type", "application/json")
			.build();
		final HttpResponse<String> res = client.send(request, BodyHandlers.ofString());

		return new Response(res.body(), res.statusCode());
	}

	private Response requestJson(String path, String method, String json) throws IOException, InterruptedException {
		final var request = HttpRequest.newBuilder(URI.create(this.url + path))
			.method(method, BodyPublishers.ofString(json))
			.header("Authorization", this.token)
			.header("Content-Type", "application/json")
			.build();
		final HttpResponse<String> res = client.send(request, BodyHandlers.ofString());

		return new Response(res.body(), res.statusCode());
	}

	private void testConnection() throws IOException, InterruptedException {
		if (getJson("/api/signal/status").status != 200)
			throw new IOException("Backend service didn't respond as expected");
	}

    public static Optional<User> getPlayer(UUID uuid, String IP, String name) {
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

	private static final class Response {
		public final String json;
		public final int status;

		public Response(String json, int status) {
			this.json = json;
			this.status = status;
		}
	}
}
