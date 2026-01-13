package gg.dystellar.core.services.messaging;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.zylesh.dystellarcore.commands.FriendCommand;
import net.zylesh.dystellarcore.commands.InboxCommand;
import net.zylesh.dystellarcore.core.User;
import net.zylesh.dystellarcore.core.inbox.Sendable;
import net.zylesh.dystellarcore.core.inbox.senders.CoinsReward;
import net.zylesh.dystellarcore.core.inbox.senders.Message;
import net.zylesh.dystellarcore.core.punishments.Punishment;
import net.zylesh.dystellarcore.serialization.Consts;
import net.zylesh.dystellarcore.serialization.InboxSerialization;
import net.zylesh.dystellarcore.serialization.MariaDB;
import net.zylesh.dystellarcore.serialization.Punishments;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static net.zylesh.dystellarcore.commands.UnpunishCommand.invs;
import static org.bukkit.Bukkit.getLogger;

public class Types {
    public static void handle(Player p, byte[] data) {
        ByteArrayDataInput in = ByteStreams.newDataInput(data);
        byte id = in.readByte();
        switch (id) {
            case DEMAND_PUNISHMENTS_DATA:
				Handler.handlePunData(in); break;
            case PUNISHMENTS_DATA_RESPONSE:
                Handler.handlePunDataRes(p, in); break;
            case FRIEND_ADD_REQUEST_APPROVE: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                if (FriendCommand.requestsCache.contains(player.getUniqueId())) {
                    player.sendMessage(ChatColor.GREEN + "Friend request sent!");
                } else {
                    getLogger().warning("Friend request approve operation for " + player.getName() + " received, but this player didn't send any friend request. Ignoring packet...");
                }
                break;
            }
            case FRIEND_ADD_REQUEST_DENY: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                if (FriendCommand.requestsCache.remove(player.getUniqueId())) {
                    player.sendMessage(ChatColor.GREEN + "This player is not online.");
                } else {
                    getLogger().warning("Friend request deny operation for " + player.getName() + " received, but this player didn't send any friend request. Ignoring packet...");
                }
                break;
            }
            case FRIEND_ADD_REQUEST_DISABLED: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                if (FriendCommand.requestsCache.remove(player.getUniqueId())) {
                    player.sendMessage(ChatColor.GREEN + "This player is not accepting friend requests.");
                } else {
                    getLogger().warning("Friend request deny operation for " + player.getName() + " received, but this player didn't send any friend request. Ignoring packet...");
                }
                break;
            }
            case FRIEND_ADD_REQUEST: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                UUID uuid = UUID.fromString(in.readUTF());
                String name = in.readUTF();
                requests.put(player.getUniqueId(), uuid);
                PacketPlayOutChat chat = new PacketPlayOutChat(ChatSerializer.a("[\"\",{\"text\":\"You've received a friend request from \",\"color\":\"dark_aqua\"},{\"text\":\"" + name + "\",\"color\":\"light_purple\"},{\"text\":\"!\",\"color\":\"dark_aqua\"},{\"text\":\" \",\"bold\":true,\"color\":\"green\"},{\"text\":\"[Accept]\",\"bold\":true,\"color\":\"green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/f accept\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"§aClick to accept!\"}},{\"text\":\" \",\"bold\":true,\"color\":\"red\"},{\"text\":\"[Reject]\",\"bold\":true,\"color\":\"red\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/f reject\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"§cClick to reject!\"}}]"));
                player.sendPacket(chat);
                Bukkit.getScheduler().runTaskLater(this, () -> requests.remove(player.getUniqueId()), 800L);
                break;
            }
            case DEMAND_IS_PLAYER_ACCEPTING_FRIEND_REQUESTS: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                User u = User.get(player);
                if (u == null) return;
                boolean response;
                response = u.extraOptions[Consts.EXTRA_OPTION_FRIEND_REQUESTS_ENABLED_POS] == Consts.BYTE_TRUE;
                sendPluginMessage(player, DEMAND_IS_PLAYER_ACCEPTING_FRIEND_REQUESTS_RESPONSE, response);
            }
            case REMOVE_FRIEND: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                UUID uuid = UUID.fromString(in.readUTF());
                User u = User.get(player);
                if (u == null) {
                    getLogger().warning(player.getName() + " is supposed to delete a player from its friends list as stated by the packet received, but he is not online...");
                    return;
                }
                u.friends.remove(uuid);
                asyncManager.submit(() -> {
                    String name = MariaDB.loadName(uuid.toString());
                    if (name != null) {
                        player.sendMessage(ChatColor.RED + name + " has removed been removed from your friends list. (He removed you)");
                    }
                });
                break;
            }
            case FRIEND_ADD_REQUEST_ACCEPT: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                UUID uuid = UUID.fromString(in.readUTF());
                FriendCommand.requestAccepted(player, uuid, unsafe);
                break;
            }
            case FRIEND_ADD_REQUEST_REJECT: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                FriendCommand.requestRejected(player, unsafe);
                break;
            }
            case SHOULD_SEND_PACK_RESPONSE: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                User user = User.get(player);
                if (user.extraOptions[Consts.EXTRA_OPTION_RESOURCEPACK_PROMPT_POS] == Consts.BYTE_TRUE) {
                    player.openInventory(packConfirmation);
                    prompts.add(player);
                } else {
                    sendResourcePack(player);
                }
                break;
            }
            case PUNISHMENT_ADD_CLIENTBOUND: {
                
            }
        }
	}
}
