package gg.dystellar.core.messaging;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.Options;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

import gg.dystellar.core.DystellarCore;
import gg.dystellar.core.api.comms.Receiver;
import gg.dystellar.core.api.comms.Channel.ByteBufferInputStream;
import gg.dystellar.core.common.UserComponent;
import gg.dystellar.core.perms.Group;
import gg.dystellar.core.perms.Permission;
import gg.dystellar.core.utils.Pair;
import gg.dystellar.core.utils.Utils;

/**
 * Incoming plugin messages handler.
 */
public class Handler {
	public static void handle(String source, ByteBufferInputStream in) {
		Subchannel.values()[in.readByte()].callback.ifPresent(f -> f.receive(source, in));
	}

	private static final Random RAND = new Random();
	public static final Map<Integer, Pair<ScheduledFuture<?>, Receiver>> SESSIONS = new ConcurrentHashMap<>();

	public static int createMessageSession(Receiver callback, Runnable failed, long expirationMillis) {
		final int id = RAND.nextInt();

		final var task = HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> {
			SESSIONS.remove(id);
			failed.run();
		}, expirationMillis, TimeUnit.MILLISECONDS);
		SESSIONS.put(id, new Pair<>(task, callback));

		return id;
	}

	public static int createMessageSession(Receiver callback, Runnable failed) {
		return createMessageSession(callback, failed, 2000L);
	}

	public static void handleSession(String source, ByteBufferInputStream in) {
		final var session = SESSIONS.remove(in.readInt());

		if (session != null) {
			session.first.cancel(false);
			session.second.receive(source, in);
		}
	}

	public static void handleAddressRequest(String s, ByteBufferInputStream in) {
		final int id = in.readInt();
		final var host = DystellarCore.getInstance().config.get().public_ip;
		final var port = Options.getOptionSet().valueOf(Options.BIND).getPort();

		Utils.sendTargetedOutputStream(s, Subchannel.SESSION, 36, out -> {
			out.writeInt(id);
			out.writePrefixedUTF8(host);
			out.writeInt(port);
		});
	}

	public static void handleFriendRemove(String s, ByteBufferInputStream in) {
		final var senderUuid = in.readPrefixedUTF8();
		final var receiverUuid = in.readPrefixedUTF8();
		final var receiver = Universe.get().getPlayer(UUID.fromString(receiverUuid));

		if (receiver != null && receiver.isValid()) {
			final var user = receiver.getHolder().getComponent(UserComponent.getComponentType());
			final var found = Utils.find(user.friends, map -> map.uuid().toString().equals(senderUuid));
			if (found.isPresent()) {
				final var lang = DystellarCore.getInstance().getLang(user.language);
				user.friends.removeIf(map -> map.uuid().equals(found.get().uuid()));

				receiver.sendMessage(lang.friendRemovedReceiver.buildMessage().param("player", found.get().name()));
			}
		}
	}

	public static void handleDemFindPlayer(String source, ByteBufferInputStream in) {
		final var playerName = in.readPrefixedUTF8();
		final var playerRef = Universe.get().getPlayerByUsername(playerName, NameMatching.EXACT_IGNORE_CASE);

		if (playerRef != null) {
			final var id = in.readInt();
			Utils.sendTargetedOutputStream(source, Subchannel.SESSION, 50, out -> {
				out.writeInt(id);
				out.writePrefixedUTF8(playerRef.getUsername());
			});
		}
	}

	public static void handleDefaultGroupUpdate(String source, ByteBufferInputStream in) {
		final var name = in.readPrefixedUTF8();
		final var group = Group.getGroup(name);

		if (group.isEmpty()) {
			DystellarCore.getLog().atWarning().log("Received a default group update for a group " + name + " that doesn't exist");
			return;
		}

		Group.setDefaultGroup(group.get());
	}

	public static void handleUserGroupUpdate(String source, ByteBufferInputStream in) {
		final var username = in.readPrefixedUTF8();
		final var groupName = in.readPrefixedUTF8();

		final var target = Universe.get().getPlayerByUsername(username, NameMatching.EXACT_IGNORE_CASE);
		if (target == null) return;
		final var group = Group.getGroup(groupName);

		if (group.isEmpty()) {
			DystellarCore.getLog().atWarning().log("Received a user group update with group " + groupName + " that doesn't exist");
			return;
		}
		final var user = target.getHolder().getComponent(UserComponent.getComponentType());
		user.group = group;
	}

	public static void handleGroupUpdate(String source, ByteBufferInputStream in) {
		final var name = in.readPrefixedUTF8();
		final var group = Group.getGroup(name);

		if (group.isEmpty()) {
			DystellarCore.getLog().atWarning().log("Received a group update for a group " + name + " that doesn't exist");
			return;
		}

		try {
			final var remoteGroup = DystellarCore.getApi().getGroup(name);
			if (remoteGroup.isEmpty()) {
				DystellarCore.getLog().atSevere().log("Group update for " + name + " exists internally but not remotely... wtf? Something nasty happened, it would be wise to check it out.");
				return;
			}

			final var g = group.get();
			final var gr = remoteGroup.get();
			g.setPrefix(gr.prefix);
			g.setSuffix(gr.suffix);
			g.getPermissions().clear();

			for (final Permission p : gr.perms)
				g.getPermissions().put(p.getPerm(), p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void handleGroupCreate(String source, ByteBufferInputStream in) {
		final var name = in.readPrefixedUTF8();

		if (Group.getGroup(name).isPresent()) {
			DystellarCore.getLog().atWarning().log("Received a group create for a group " + name + " that already exists");
			return;
		}

		Group.registerGroup(new Group(name, "", "", List.of()));
	}

	public static void handleGroupDelete(String source, ByteBufferInputStream in) {
		final var groupName = in.readPrefixedUTF8();
		final var group = Group.getGroup(groupName);
		if (group.isEmpty()) {
			DystellarCore.getLog().atWarning().log("Received a group update for a group " + groupName + " that doesn't exist");
			return;
		}

		Group.groups.remove(groupName);
		if (Group.getDefaultGroup().isPresent() && Group.getDefaultGroup().get().getName().equals(groupName))
			Group.setDefaultGroup(null);
		else {
			for (PlayerRef p : Universe.get().getPlayers()) {
				final var user = p.getHolder().getComponent(UserComponent.getComponentType());
				if (!p.isValid() || user == null) continue;

				if (user.group.isPresent() && user.group.get().getName().equals("groupName"))
					user.group = Group.getDefaultGroup();
			}
		}
	}

	public static void handleUnpunish(String source, ByteBufferInputStream in) {
		final var name = in.readPrefixedUTF8();
		final var id = in.readLong();

		final var p = Universe.get().getPlayer(name, NameMatching.EXACT_IGNORE_CASE);

		if (p != null && p.isValid()) {
			final var user = p.getHolder().getComponent(UserComponent.getComponentType());
			final var punishment = Utils.find(user.punishments, pun -> pun.getId() == id);
			if (punishment.isPresent())
				punishment.get().setExpirationDate(LocalDateTime.now(ZoneId.of("UTC")));
		}
	}

	public static void handleInboxManagerUpdate() {}

	/* TODO: Implement inboxes stuff
	public static void handleInboxSend(String source, ByteBufferInputStream in) {
		String unsafe = in.readUTF();
		Player player = Bukkit.getPlayer(unsafe);
		if (player == null || !player.isOnline()) {
			Bukkit.getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
			return;
		}
		User user = User.get(player);
		Sendable sender = InboxSerialization.stringToSender(in.readUTF(), user.getInbox());
		user.getInbox().addSender(sender);
	}*/

	/* TODO:
	public static void handleInboxUpdate(String source, ByteBufferInputStream in) {
		UUID uuid = UUID.fromString(in.readUTF());
		if (!User.getUsers().containsKey(uuid)) return;
		User user = User.get(uuid);
		byte type = in.readByte();
		switch (type) {
			case COINS_REWARD: {
				int iid = in.readInt();
				LocalDateTime submission = LocalDateTime.parse(in.readUTF(), DateTimeFormatter.ISO_DATE_TIME);
				int coins = in.readInt();
				String title = in.readUTF();
				String[] message = in.readUTF().split(":;");
				String from = in.readUTF();
				boolean claimed = in.readBoolean();
				boolean deleted = in.readBoolean();
				CoinsReward reward = new CoinsReward(user.getInbox(), iid, from, message, submission, deleted, title, claimed, coins);
				reward.initializeIcons();
				user.getInbox().addSender(reward);
				break;
			}
			case MESSAGE: {
				int iid = in.readInt();
				LocalDateTime submission = LocalDateTime.parse(in.readUTF(), DateTimeFormatter.ISO_DATE_TIME);
				String[] message = in.readUTF().split(":;");
				String from = in.readUTF();
				boolean deleted = in.readBoolean();
				Message reward = new Message(user.getInbox(), iid, from, message, submission, deleted);
				reward.initializeIcons();
				user.getInbox().addSender(reward);
				break;
			}
		}
	}*/
}
