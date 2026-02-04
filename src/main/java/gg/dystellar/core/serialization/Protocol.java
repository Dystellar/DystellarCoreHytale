package gg.dystellar.core.serialization;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import gg.dystellar.core.common.Suffix;
import gg.dystellar.core.common.UserComponent;
import gg.dystellar.core.common.punishments.Punishment;
import gg.dystellar.core.perms.Group;
import gg.dystellar.core.perms.Permission;
import gg.dystellar.core.utils.Utils;

public final class Protocol {
	private Protocol() {}

	public static final class PunishParams {
		public String user_uuid;
		public String title;
		public String type;
		public long creation_date;
		public Long expiration_date;
		public String reason;
		public boolean alsoip;
		public boolean allow_chat;
		public boolean allow_ranked;
		public boolean allow_unranked;
		public boolean allow_join_minigames;
	}

	public static final class RawPunishment {
		public long id;
		public String title;
		public String type;
		public long creation_date;
		public Long expiration_date;
		public String reason;
		public boolean alsoip;
		public boolean allow_chat;
		public boolean allow_ranked;
		public boolean allow_unranked;
		public boolean allow_join_minigames;

		public Punishment toPunishment() {
			final var creationDate = LocalDateTime.from(Instant.ofEpochMilli(creation_date).atZone(ZoneId.of("UTC")));
			LocalDateTime expirationDate = null;
			if (expiration_date != null)
				expirationDate = LocalDateTime.from(Instant.ofEpochMilli(expiration_date).atZone(ZoneId.of("UTC")));

			return new Punishment(
				id, title, type, creationDate, expirationDate, reason,
				allow_chat, allow_ranked, allow_unranked, allow_join_minigames
			);
		}

		public static RawPunishment fromPunishment(Punishment pun) {
			final var raw = new RawPunishment();
			raw.id = pun.getId();
			raw.title = pun.getTitle();
			raw.type = pun.getType();
			raw.creation_date = pun.getCreationDate().atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
			raw.expiration_date = pun.getExpirationDate().map(t -> t.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()).orElse(null);
			raw.reason = pun.getReason();
			raw.alsoip = false;
			raw.allow_chat = pun.allowChat();
			raw.allow_ranked = pun.allowRanked();
			raw.allow_unranked = pun.allowUnranked();
			raw.allow_join_minigames = pun.allowJoinMinigames();

			return raw;
		}
	}

	public static final class RawGroup {
		public String name;
		public String prefix;
		public String suffix;
		public Permission[] perms;
	}

	public static final class RawUser {
		public String uuid;
		public String name;
		public String email;
		public boolean chat;
		public byte pms;
		public String suffix;
		public String lang;
		public boolean scoreboard;
		public long coins;
		public boolean friend_reqs;
		public long created_at;
		public String[] friends;
		public String[] ignores;
		public RawPunishment[] punishments;
		public Permission[] perms;
		public String group;

		public UserComponent toUserComponent(String address) {
			var user = new UserComponent(UUID.fromString(uuid), address, name);
			user.email = Optional.ofNullable(email);
			user.globalChatEnabled = chat;
			user.privateMessagesMode = pms;
			user.suffix = Utils.findArr(Suffix.values(), s -> s.name().equals(suffix)).orElse(Suffix.NONE);
			user.language = lang;
			user.scoreboardEnabled = scoreboard;
			user.coins = coins;
			user.friendRequests = friend_reqs;
			Collections.addAll(user.friends, Arrays.stream(friends).map(f -> UUID.fromString(f)).toArray(l -> new UUID[l]));
			Collections.addAll(user.ignoreList, Arrays.stream(ignores).map(i -> UUID.fromString(i)).toArray(l -> new UUID[l]));
			Collections.addAll(user.punishments, Arrays.stream(punishments).map(p -> p.toPunishment()).toArray(l -> new Punishment[l]));
			for (Permission perm : perms) user.perms.put(perm.getPerm(), perm);
			user.creationDate = LocalDateTime.from(Instant.ofEpochMilli(created_at).atZone(ZoneId.of("UTC")));
			// TODO: user.inbox = inbox;
			if (group != null) user.group = Group.getGroup(group);
			return user;
		}

		public static RawUser fromUserComponent(UserComponent user) {
			final var raw = new RawUser();
			raw.uuid = user.uuid.toString();
			raw.name = user.name;
			raw.email = user.email.orElse(null);
			raw.chat = user.globalChatEnabled;
			raw.pms = user.privateMessagesMode;
			raw.suffix = user.suffix.name();
			raw.lang = user.language;
			raw.scoreboard = user.scoreboardEnabled;
			raw.coins = user.coins;
			raw.friend_reqs = user.friendRequests;
			raw.created_at = user.creationDate.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
			raw.friends = user.friends.stream().map(f -> f.toString()).toArray(len -> new String[len]);
			raw.ignores = user.ignoreList.stream().map(i -> i.toString()).toArray(len -> new String[len]);
			raw.punishments = user.punishments.stream().map(pun -> RawPunishment.fromPunishment(pun)).toArray(len -> new RawPunishment[len]);
			raw.perms = user.perms.values().toArray(len -> new Permission[len]);
			raw.group = user.group.map(g -> g.getName()).orElse(null);

			return raw;
		}
	}
}
