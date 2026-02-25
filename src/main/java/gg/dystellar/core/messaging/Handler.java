package gg.dystellar.core.messaging;

import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

import gg.dystellar.core.api.comms.Receiver;
import gg.dystellar.core.api.comms.Channel.ByteBufferInputStream;
import gg.dystellar.core.utils.Pair;
import gg.dystellar.core.utils.Utils;

/**
 * Incoming plugin messages handler.
 */
public class Handler {
	public static void handle(String source, ByteBufferInputStream in) {
		try {
			Subchannel.values()[in.read()].callback.ifPresent(f -> f.receive(source, in));
		} catch (Exception ignored) {}
	}

	private static final Random RAND = new Random();
	private static final Map<Integer, Pair<ScheduledFuture<?>, Receiver>> SESSIONS = new ConcurrentHashMap<>();

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

	public static void handlePunData(String source, ByteBufferInputStream in) {
		String string = in.readUTF();
		Player player = Bukkit.getPlayer(string);

		if (player == null) return;
		User user = User.get(player);
		Utils.sendPluginMessage(player, Types.PUNISHMENTS_DATA_RESPONSE, Punishments.serializePunishments(user.getPunishments()));
	}

	public static void handlePunDataRes(String source, ByteBufferInputStream in) {
		String string = in.readUTF();
		Player player = Bukkit.getPlayer(string);

		if (player == null)
			return;
		UUID target = UUID.fromString(in.readUTF());
		Set<Punishment> punishments = Punishments.deserializePunishments(in.readUTF(), new HashSet<>());

		invs.put(p.getUniqueId(), new AbstractMap.SimpleImmutableEntry<>(target, new Punishment[27]));
		Inventory inv = InventoryBuilder.punishmentsInv(p, punishments);
		p.openInventory(inv);
	}

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

	public static void handleInboxManagerUpdate() {}

	public static void handleFriendReqApprove() {}

	public static void handleFriendReqDeny() {}

	public static void handleFriendReqDisabled() {}

	public static void handleFriendAddReq() {}

	public static void handleDemIsPlayerAcceptingReqs() {}

	public static void handleDemIsPlayerWithinNetwork(String source, ByteBufferInputStream in) {

	}

	public static void handleFriendRemove() {}

	public static void handleDemFindPlayer(String source, ByteBufferInputStream in) {
		final var playerName = in.readPrefixedUTF8();
		final var playerRef = Universe.get().getPlayerByUsername(playerName, NameMatching.EXACT);

		if (playerRef != null) {
			final var id = in.readInt();
			Utils.sendTargetedOutputStream(source, Subchannel.DEMAND_FIND_PLAYER_RES, 50, out -> out.writeInt(id));
		}
	}

	public static void handleDemFindPlayerRes(String source, ByteBufferInputStream in) {
		final var id = in.readInt();
		
		final var pair = SESSIONS.remove(id);
		if (pair != null) {
			pair.first.cancel(true);
			pair.second.receive(source, in);
		}
	}

	public static void handleFriendReqAccept() {}

	public static void handleFriendReqReject() {}

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

	public static void handleShouldSendPackRes() {}

	public static void handlePunishmentAddServer(String source, ByteBufferInputStream in) {
		String unsafe = in.readUTF();
		Player player = Bukkit.getPlayer(unsafe);
		if (player == null || !player.isOnline()) {
			Bukkit.getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
			return;
		}
		String serialized = in.readUTF();
		Punishment punishment = Punishments.deserialize(serialized);
		User user = User.get(player);
		user.punish(punishment);
	}

	public static void handleRemovePunishmentById(String source, ByteBufferInputStream in) {
		String unsafe = in.readUTF();
		int pId = in.readInt();
		Player player = Bukkit.getPlayer(unsafe);
		if (player == null || !player.isOnline()) return;
		User user = User.get(player);
		Punishment punishmentToRemove = null;
		for (Punishment pun : user.getPunishments()) {
			if (pun.hashCode() == pId) {
				punishmentToRemove = pun;
				break;
			}
		}

		if (punishmentToRemove == null || !user.getPunishments().remove(punishmentToRemove)) return;

		player.sendMessage(ChatColor.GREEN + "The punishment with ID " + pId + " was removed from your punishments list!");
		String[] details = new String[] {
			ChatColor.DARK_GREEN + "Punishment details:",
			"===============================",
			ChatColor.DARK_AQUA + "Type" + ChatColor.WHITE + ": " + ChatColor.GRAY + p.getClass().getSimpleName(),
			ChatColor.DARK_AQUA + "Creation Date" + ChatColor.WHITE + ": " + ChatColor.GRAY + punishmentToRemove.getCreationDate().format(DateTimeFormatter.ISO_DATE_TIME),
			ChatColor.DARK_AQUA + "Expiration Date" + ChatColor.WHITE + ": " + ChatColor.GRAY + (punishmentToRemove.getExpirationDate() == null ? "Never" : punishmentToRemove.getExpirationDate().format(DateTimeFormatter.ISO_DATE_TIME)),
			ChatColor.DARK_AQUA + "Reason" + ChatColor.WHITE + ": " + ChatColor.GRAY + punishmentToRemove.getReason(),
			"==============================="
		};
		player.sendMessage(details);
	}
}
