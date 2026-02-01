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
			user.suffix = Suffix.valueOf(suffix);;;;;;;;;;;;;;;;;;;;
			user.language = lang;
			user.scoreboardEnabled = scoreboard;
			user.coins = coins;
			user.friendRequests = friend_reqs;
			Collections.addAll(user.friends, Arrays.stream(friends).map(f -> UUID.fromString(f)).toArray(l -> new UUID[l]));
			Collections.addAll(user.ignoreList, Arrays.stream(ignores).map(i -> UUID.fromString(i)).toArray(l -> new UUID[l]));
			Collections.addAll(user.punishments, Arrays.stream(punishments).map(p -> p.toPunishment()).toArray(l -> new Punishment[l]));
			for (Permission perm : perms) user.perms.put(perm.getPerm(), perm);
			// TODO: user.inbox = inbox;
			if (group != null) user.group = Group.getGroup(group);
			return user;
		}
	}
}
