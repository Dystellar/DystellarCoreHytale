package gg.dystellar.core.serialization;

import gg.dystellar.core.DystellarCore;
import gg.dystellar.core.common.UserComponent;
import gg.dystellar.core.common.punishments.Punishment;
import gg.dystellar.core.serialization.Protocol.RawGroupsData;
import gg.dystellar.core.serialization.Protocol.RawUser;

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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
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

		final var result = new Response(res.body(), res.statusCode());

		if (result.status != 200)
			DystellarCore.getLog().atSevere().log("Failed request GET " + path + ": " + result.json);

		return result;
	}

	private Response requestJson(String path, String method, String json) throws IOException, InterruptedException {
		final var request = HttpRequest.newBuilder(URI.create(this.url + path))
			.method(method, BodyPublishers.ofString(json))
			.header("Authorization", this.token)
			.header("Content-Type", "application/json")
			.build();
		final HttpResponse<String> res = client.send(request, BodyHandlers.ofString());

		final var result = new Response(res.body(), res.statusCode());

		if (result.status != 200)
			DystellarCore.getLog().atSevere().log("Failed request " + method + ' ' + path + ": " + result.json);

		return result;
	}

	private void testConnection() throws IOException, InterruptedException {
		if (getJson("/api/signal/status").status != 200)
			throw new IOException("Backend service didn't respond as expected");
	}

    public Optional<UserComponent> getPlayer(UUID uuid) throws IOException, InterruptedException {
		final var res = this.getJson("/api/privileged/player_data?uuid=" + uuid.toString());
		
		if (res.status != 200)
			return Optional.empty();

		return Optional.of(gson.fromJson(res.json, UserComponent.class));
    }

	public UserComponent playerConnected(String uuid, String name, String address) throws IOException, InterruptedException {
		final var res = this.getJson("/api/privileged/user_connected?uuid=" + uuid + "&name=" + name + "&address=" + address);

		if (res.status != 200)
			throw new IOException("Failed fetch user on connection request");

		return gson.fromJson(res.json, RawUser.class).toUserComponent(address);
	}

	public void saveUser(UserComponent user) throws IOException, InterruptedException {
		final var res = requestJson("/api/privileged/user_save", "PUT", gson.toJson(RawUser.fromUserComponent(user)));

		if (res.status != 200)
			throw new IOException("Failed to save player. Json: " + res.json);
	}

	public Optional<Punishment> punish(UUID uuid, String title, String type, LocalDateTime creation_date, Optional<LocalDateTime> expiration_date, String reason, boolean alsoip, boolean allow_chat, boolean allow_ranked, boolean allow_unranked, boolean allow_join_minigames) throws IOException, InterruptedException {
		Protocol.PunishParams params = new Protocol.PunishParams();
		params.user_uuid = uuid.toString();
		params.title = title;
		params.type = type;
		params.creation_date = creation_date.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
		params.expiration_date = expiration_date.map(t -> t.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()).orElse(null);
		params.reason = reason;
		params.alsoip = alsoip;
		params.allow_chat = allow_chat;
		params.allow_ranked = allow_ranked;
		params.allow_unranked = allow_unranked;
		params.allow_join_minigames = allow_join_minigames;

		final var res = this.requestJson("/api/privileged/punish", "POST", this.gson.toJson(params));

		if (res.status != 200)
			return Optional.empty();

		return Optional.of(gson.fromJson(res.json, Protocol.RawPunishment.class).toPunishment());
	}

	public Optional<RawGroupsData> getGroupsData() throws IOException, InterruptedException {
		final var res = this.getJson("/api/privileged/get_groups");

		if (res.status != 200)
			return Optional.empty();

		return Optional.of(gson.fromJson(res.json, RawGroupsData.class));
	}

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

	private static final class Response {
		public final String json;
		public final int status;

		public Response(String json, int status) {
			this.json = json;
			this.status = status;
		}
	}
}
