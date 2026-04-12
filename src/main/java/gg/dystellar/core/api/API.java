package gg.dystellar.core.api;

import gg.dystellar.core.DystellarCore;
import gg.dystellar.core.api.comms.WsClient;
import gg.dystellar.core.common.UserComponent;
import gg.dystellar.core.common.punishments.Punishment;
import gg.dystellar.core.perms.Group;
import gg.dystellar.core.perms.Permission;
import gg.dystellar.core.serialization.Protocol.*;
import gg.dystellar.core.utils.Result;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Database storage, should use the backend instead.
 */
public final class API {

	private final Gson gson = new Gson();

    public API(String url, String wsUrl, String name, String token) throws IOException, InterruptedException, ExecutionException {
		this.url = url;
		this.token = token;
		this.client = HttpClient.newBuilder()
			.version(Version.HTTP_2)
			.followRedirects(Redirect.NEVER)
			.connectTimeout(Duration.ofSeconds(20))
			.build();
		this.testConnection();
		this.wsClient = new WsClient(wsUrl, token, name, this.client);
	}

	private final HttpClient client;
	private final String url;
	private final String token;
	public final WsClient wsClient;

	public Response getJson(String path) throws IOException, InterruptedException {
		final var request = HttpRequest.newBuilder(URI.create(this.url + path))
			.method("GET", BodyPublishers.noBody())
			.header("authorization", this.token)
			.header("Content-Type", "application/json")
			.header("X-Target-Host", DystellarCore.getInstance().config.get().host)
			.build();
		final HttpResponse<String> res = client.send(request, BodyHandlers.ofString());

		final var result = new Response(res.body(), res.statusCode());

		if (result.status != 200)
			DystellarCore.getLog().atSevere().log("Failed request GET " + path + ": " + result.json);

		return result;
	}

	public Response requestJson(String path, String method, String json) throws IOException, InterruptedException {
		final var request = HttpRequest.newBuilder(URI.create(this.url + path))
			.method(method, BodyPublishers.ofString(json))
			.header("authorization", this.token)
			.header("Content-Type", "application/json")
			.header("X-Target-Host", DystellarCore.getInstance().config.get().host)
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
		final var res = this.getJson("/api/core/player_data?uuid=" + uuid.toString());
		
		if (res.status != 200)
			return Optional.empty();

		return Optional.of(gson.fromJson(res.json, UserComponent.class));
    }

	public UserComponent playerConnected(String uuid, String name, String address) throws IOException, InterruptedException {
		final var res = this.getJson("/api/core/user_connected?uuid=" + uuid + "&name=" + name + "&address=" + address);

		if (res.status != 200)
			throw new IOException("Failed fetch user on connection request");

		return gson.fromJson(res.json, RawUser.class).toUserComponent(address);
	}

	public Result<Void, String> playerFriendRemove(UUID sender, UUID receiver) throws IOException, InterruptedException {
		final var res = this.requestJson("/api/core/user_friend_remove", "PUT", gson.toJson(new UuidPair(sender, receiver)));

		if (res.status != 200) {
			final var err = gson.fromJson(res.json, BackendError.class);
			return Result.err(err.error());
		}

		return Result.ok(null);
	}

	public void saveUser(UserComponent user) throws IOException, InterruptedException {
		final var res = requestJson("/api/core/user_save", "PUT", gson.toJson(RawUser.fromUserComponent(user)));

		if (res.status != 200)
			throw new IOException("Failed to save player. Json: " + res.json);
	}

	public Optional<Punishment> punish(UUID uuid, String title, String type, LocalDateTime creation_date, Optional<LocalDateTime> expiration_date, String reason, boolean alsoip, boolean allow_chat, boolean allow_ranked, boolean allow_unranked, boolean allow_join_minigames) throws IOException, InterruptedException {
		PunishParams params = new PunishParams();
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

		final var res = this.requestJson("/api/core/punish", "POST", this.gson.toJson(params));

		if (res.status != 200)
			return Optional.empty();

		return Optional.of(gson.fromJson(res.json, RawPunishment.class).toPunishment());
	}

	public Result<Void, String> unpunish(String username, long punishmentId) throws IOException, InterruptedException {
		final var res = this.requestJson("/api/core/unpunish", "PUT", gson.toJson(new UnpunishData(username, punishmentId)));

		if (res.status != 200) {
			final var err = gson.fromJson(res.json, BackendError.class);
			return Result.err(err.error());
		}

		return Result.ok(null);
	}

	public Optional<RawGroupsData> getGroupsData() throws IOException, InterruptedException {
		final var res = this.getJson("/api/core/get_groups");

		if (res.status != 200)
			return Optional.empty();

		return Optional.of(gson.fromJson(res.json, RawGroupsData.class));
	}

	public Optional<RawGroup> getGroup(String name) throws IOException, InterruptedException {
		final var res = this.getJson("/api/core/get_groups?name=" +  name);

		if (res.status != 200)
			return Optional.empty();

		return Optional.of(gson.fromJson(res.json, RawGroup.class));
	}

	public Result<Void, String> setDefaultGroup(String name) throws IOException, InterruptedException {
		final var res = this.requestJson("/api/core/set_group_default", "PUT", this.gson.toJson(new SimpleName(name)));

		if (res.status != 200) {
			final var err = gson.fromJson(res.json, BackendError.class);
			return Result.err(err.error());
		}
		
		return Result.ok(null);
	}

	public Result<Void, String> setGroupToUser(String group, UUID uuid) throws IOException, InterruptedException {
		final var data = new UserGroup(uuid, group);
		final var res = this.requestJson("/api/core/set_user_group", "PUT", this.gson.toJson(data));

		if (res.status != 200) {
			final var err = gson.fromJson(res.json, BackendError.class);
			return Result.err(err.error());
		}

		return Result.ok(null);
	}

	public Result<Void, String> setGroupToUserByName(String group, String username) throws IOException, InterruptedException {
		final var data = new UserGroupByName(username, group);
		final var res = this.requestJson("/api/core/set_user_group", "PUT", this.gson.toJson(data));

		if (res.status != 200) {
			final var err = gson.fromJson(res.json, BackendError.class);
			return Result.err(err.error());
		}

		return Result.ok(null);
	}

	public Result<Void, String> updateGroup(Group group) throws IOException, InterruptedException {
		final var res = this.requestJson("/api/core/update_group", "POST", gson.toJson(RawGroup.fromGroup(group)));

		if (res.status != 200) {
			final var err = gson.fromJson(res.json, BackendError.class);
			return Result.err(err.error());
		}

		return Result.ok(null);
	}

	public Result<Void, String> removePermsAndUpdateGroup(Group group) throws IOException, InterruptedException {
		final var res = this.requestJson("/api/core/delete_perms_and_update_group", "PUT", gson.toJson(RawGroup.fromGroup(group)));

		if (res.status != 200) {
			final var err = gson.fromJson(res.json, BackendError.class);
			return Result.err(err.error());
		}

		return Result.ok(null);
	}

	public Result<Void, String> addPermToGroup(String name, Permission perm) throws IOException, InterruptedException {
		final var res = this.requestJson("/api/core/add_perm_to_group", "PUT", gson.toJson(new GroupPermission(name, perm)));

		if (res.status != 200) {
			final var err = gson.fromJson(res.json, BackendError.class);
			return Result.err(err.error());
		}

		return Result.ok(null);
	}

	public Result<Void, String> removePermFromGroup(String name, String perm) throws IOException, InterruptedException {
		final var res = this.requestJson("/api/core/add_perm_to_group", "DELETE", gson.toJson(new GroupPermissionName(name, perm)));

		if (res.status != 200) {
			final var err = gson.fromJson(res.json, BackendError.class);
			return Result.err(err.error());
		}

		return Result.ok(null);
	}

	public Result<Void, String> deleteGroup(String name) throws IOException, InterruptedException {
		final var res = this.requestJson("/api/core/delete_group", "DELETE", gson.toJson(new SimpleName(name)));

		if (res.status != 200) {
			final var err = gson.fromJson(res.json, BackendError.class);
			return Result.err(err.error());
		}

		return Result.ok(null);
	}

	public static final class Response {
		public final String json;
		public final int status;

		Response(String json, int status) {
			this.json = json;
			this.status = status;
		}
	}
}
