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

public class NotesCommand implements CommandExecutor {

    public NotesCommand() {
        Bukkit.getPluginCommand("notes").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length < 1) {
            if (!(commandSender instanceof Player)) return true;
            Player p = (Player)commandSender;
            User user = User.get(p);
            p.sendMessage(ChatColor.YELLOW + "Your notes:");
            Bukkit.getScheduler().runTaskLater(DystellarCore.getInstance(), () -> {
                if (user.getNotes().isEmpty()) {
                    p.sendMessage(ChatColor.GREEN + "You don't have any note yet.");
                    return;
                }
                for (String note : user.getNotes()) {
                    p.sendMessage(ChatColor.GRAY + " - " + ChatColor.YELLOW + note);
                }
            }, 20L);
        } else {
            if (!commandSender.hasPermission("dystellar.staff")) {
                commandSender.sendMessage(ChatColor.RED + "No permission.");
                return true;
            }
            Player p = Bukkit.getPlayer(strings[0]);
            if (p != null && p.isOnline()) {
                User user = User.get(p);
                commandSender.sendMessage(ChatColor.YELLOW + p.getName() + "'s notes:");
                Bukkit.getScheduler().runTaskLater(DystellarCore.getInstance(), () -> {
                    for (String note : user.getNotes()) {
                        if (user.getPunishments().isEmpty()) {
                            commandSender.sendMessage(ChatColor.YELLOW + "This player does not have any note yet!");
                            return;
                        }
                        commandSender.sendMessage(ChatColor.GRAY + " - " + ChatColor.YELLOW + note);
                    }
                }, 20L);
            } else {
                DystellarCore.getAsyncManager().execute(() -> {
                    String ip = MariaDB.loadIP(strings[0]);
                    if (ip == null) {
                        commandSender.sendMessage(Msgs.ERROR_PLAYER_NOT_FOUND);
                        return;
                    }
                    Mapping mapping = MariaDB.loadMapping(ip);
                    if (mapping == null) {
                        commandSender.sendMessage(Msgs.ERROR_PLAYER_NOT_FOUND);
                        return;
                    }
                    User user = MariaDB.loadPlayerFromDatabase(mapping.getUUID(), mapping.getIP(), mapping.getName());
                    commandSender.sendMessage(ChatColor.YELLOW + mapping.getName() + "'s notes:");
                    Bukkit.getScheduler().runTaskLater(DystellarCore.getInstance(), () -> {
                        for (String note : user.getNotes()) {
                            if (user.getPunishments().isEmpty()) {
                                commandSender.sendMessage(ChatColor.YELLOW + "This player does not have any note yet!");
                                return;
                            }
                            commandSender.sendMessage(ChatColor.GRAY + " - " + ChatColor.YELLOW + note);
                        }
                    }, 20L);
                });
            }
        }
        return true;
    }
}
