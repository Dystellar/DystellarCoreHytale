package gg.dystellar.core.commands;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.Msgs;
import net.zylesh.dystellarcore.core.User;
import net.zylesh.dystellarcore.core.punishments.Punishment;
import net.zylesh.dystellarcore.serialization.Mapping;
import net.zylesh.dystellarcore.serialization.MariaDB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PunishmentsCommand implements CommandExecutor {

    public PunishmentsCommand() {
        Bukkit.getPluginCommand("punishments").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if ((!commandSender.hasPermission("dystellar.staff") || strings.length < 1) && commandSender instanceof Player) {
            Player p = (Player)commandSender;
            User user = User.get(p);
            p.sendMessage(ChatColor.YELLOW + "Your punishments:");
            Bukkit.getScheduler().runTaskLater(DystellarCore.getInstance(), () -> {
                if (user.getPunishments().isEmpty()) {
                    p.sendMessage(ChatColor.GREEN + "You don't have any punishment yet!");
                    return;
                }
                for (Punishment punishment : user.getPunishments()) {
                    String[] info = new String[] {
                            ChatColor.RED + punishment.getClass().getSimpleName() + ":",
                            ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------------------",
                            ChatColor.DARK_AQUA + "Creation Date" + ChatColor.WHITE + ": " + punishment.getCreationDate().format(DateTimeFormatter.BASIC_ISO_DATE),
                            ChatColor.DARK_AQUA + "Expiration Date" + ChatColor.WHITE + ": " + (punishment.getExpirationDate() != null ? punishment.getExpirationDate().format(DateTimeFormatter.BASIC_ISO_DATE) + ChatColor.GRAY + " Is Expired: " + (punishment.getExpirationDate().isBefore(LocalDateTime.now()) ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No") : ChatColor.RED + "Never"),
                            ChatColor.DARK_AQUA + "Reason" + ChatColor.WHITE + ": " + punishment.getReason(),
                            ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------------------",
                    };
                    p.sendMessage(info);
                }
            }, 20L);
        } else {
            Player p = Bukkit.getPlayer(strings[0]);
            if (p != null && p.isOnline()) {
                User user = User.get(p);
                commandSender.sendMessage(ChatColor.YELLOW + p.getName() + "'s punishments:");
                Bukkit.getScheduler().runTaskLater(DystellarCore.getInstance(), () -> {
                    for (Punishment punishment : user.getPunishments()) {
                        if (user.getPunishments().isEmpty()) {
                            commandSender.sendMessage(ChatColor.YELLOW + "This player does not have any punishment yet!");
                            return;
                        }
                        String[] info = new String[] {
                                ChatColor.RED + punishment.getClass().getSimpleName() + ":",
                                ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------------------",
                                ChatColor.DARK_AQUA + "Creation Date" + ChatColor.WHITE + ": " + punishment.getCreationDate().format(DateTimeFormatter.BASIC_ISO_DATE),
                                ChatColor.DARK_AQUA + "Expiration Date" + ChatColor.WHITE + ": " + (punishment.getExpirationDate() != null ? punishment.getExpirationDate().format(DateTimeFormatter.BASIC_ISO_DATE) + ChatColor.GRAY + " Is Expired: " + (punishment.getExpirationDate().isBefore(LocalDateTime.now()) ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No") : ChatColor.RED + "Never"),
                                ChatColor.DARK_AQUA + "Reason" + ChatColor.WHITE + ": " + punishment.getReason(),
                                ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------------------",
                        };
                        commandSender.sendMessage(info);
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
                    commandSender.sendMessage(ChatColor.YELLOW + mapping.getName() + "'s punishments:");
                    Bukkit.getScheduler().runTaskLater(DystellarCore.getInstance(), () -> {
                        for (Punishment punishment : user.getPunishments()) {
                            if (user.getPunishments().isEmpty()) {
                                commandSender.sendMessage(ChatColor.YELLOW + "This player does not have any punishment yet!");
                                return;
                            }
                            String[] info = new String[] {
                                    ChatColor.RED + punishment.getClass().getSimpleName() + ":",
                                    ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------------------",
                                    ChatColor.DARK_AQUA + "Creation Date" + ChatColor.WHITE + ": " + punishment.getCreationDate().format(DateTimeFormatter.BASIC_ISO_DATE),
                                    ChatColor.DARK_AQUA + "Expiration Date" + ChatColor.WHITE + ": " + (punishment.getExpirationDate() != null ? punishment.getExpirationDate().format(DateTimeFormatter.BASIC_ISO_DATE) + ChatColor.GRAY + " Is Expired: " + (punishment.getExpirationDate().isBefore(LocalDateTime.now()) ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No") : ChatColor.RED + "Never"),
                                    ChatColor.DARK_AQUA + "Reason" + ChatColor.WHITE + ": " + punishment.getReason(),
                                    ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------------------",
                            };
                            commandSender.sendMessage(info);
                        }
                    }, 20L);
                });
            }
        }
        return true;
    }
}
