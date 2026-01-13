package net.zylesh.dystellarcore.commands;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.Msgs;
import net.zylesh.dystellarcore.core.User;
import net.zylesh.dystellarcore.serialization.Mapping;
import net.zylesh.dystellarcore.serialization.MariaDB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public class IgnoreListCommand implements CommandExecutor {

    private static final String[] help = {
            ChatColor.DARK_AQUA + "/blockslist list",
            ChatColor.DARK_AQUA + "/blockslist remove <player>"
    };

    public IgnoreListCommand() {
        Bukkit.getPluginCommand("ignorelist").setExecutor(this);
        Bukkit.getPluginCommand("blockslist").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return true;
        Player p = (Player) commandSender;
        if (strings.length < 1) {
            p.sendMessage(help);
            return true;
        }
        User user = User.get(p);
        switch (strings[0]) {
            case "list": {
                DystellarCore.getAsyncManager().submit(() -> {
                    Set<Mapping> mappings = MariaDB.getUUIDMappings(user.getIgnoreList().toArray(new UUID[0]));
                    if (mappings.isEmpty()) {
                        p.sendMessage(Msgs.BLACKLIST_EMPTY);
                        return;
                    }
                    String[] message = new String[mappings.size() + 3];
                    message[0] = ChatColor.DARK_AQUA + "Black list:";
                    message[1] = ChatColor.WHITE.toString() + ChatColor.STRIKETHROUGH + "----------------------------------";
                    message[message.length - 1] = ChatColor.WHITE.toString() + ChatColor.STRIKETHROUGH + "----------------------------------";
                    int pos = 2;
                    for (Mapping m : mappings) {
                        if (message[pos] != null || pos >= message.length-1) {
                            Bukkit.getLogger().warning("Algorithm error.");
                            break;
                        }
                        message[pos] = ChatColor.DARK_AQUA + m.getName();
                        pos++;
                    }
                    p.sendMessage(message);
                });
                break;
            }
            case "remove": {
                if (strings.length < 2) {
                    p.sendMessage(ChatColor.RED + "Usage: /blockslist remove <player>");
                    return true;
                }
                Player pInt = Bukkit.getPlayer(strings[1]);
                if (pInt == null || !pInt.isOnline()) {
                    DystellarCore.getAsyncManager().submit(() -> {
                        UUID uuid = MariaDB.loadUUID(strings[1]);
                        if (uuid == null) {
                            p.sendMessage(Msgs.ERROR_PLAYER_NOT_FOUND);
                            return;
                        }
                        if (user.getIgnoreList().remove(uuid)) {
                            p.sendMessage(Msgs.BLACKLIST_PLAYER_REMOVED.replace("<player>", strings[1]));
                        } else {
                            p.sendMessage(Msgs.ERROR_NOT_ON_BLACKLIST);
                        }
                    });
                } else {
                    if (user.getIgnoreList().remove(pInt.getUniqueId())) {
                        p.sendMessage(Msgs.BLACKLIST_PLAYER_REMOVED.replace("<player>", strings[1]));
                    } else {
                        p.sendMessage(Msgs.ERROR_NOT_ON_BLACKLIST);
                    }
                }
            }
        }
        return true;
    }
}
