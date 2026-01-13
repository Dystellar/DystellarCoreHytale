package gg.dystellar.core.commands;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.User;
import net.zylesh.dystellarcore.core.punishments.Ban;
import net.zylesh.dystellarcore.serialization.Mapping;
import net.zylesh.dystellarcore.serialization.MariaDB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;

public class BanCommand implements CommandExecutor {

    public BanCommand() {
        Bukkit.getPluginCommand("ban").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!commandSender.hasPermission("dystellar.admin")) {
            commandSender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (strings.length < 3) {
            commandSender.sendMessage(ChatColor.RED + "Usage: /ban <player> <time> <reason> [Optional: BanAlsoIP: <true> (only write if true, if you don't write anything at the end it defaults to false)]");
            return true;
        }
        Player playerInt = Bukkit.getPlayer(strings[0]);
        if (playerInt != null && playerInt.isOnline()) {
            User userInt = User.get(playerInt);
            LocalDateTime time = LocalDateTime.now();
            for (String e : strings[1].split(",")) {
                if (!e.matches("[0-9]+[dhm]")) {
                    commandSender.sendMessage(ChatColor.RED + "The format is incorrect.");
                    return true;
                }
                int integer = Integer.parseInt(e.substring(0, 1));
                switch (e.charAt(e.length() - 1)) {
                    case 'd': time = time.plusDays(integer); break;
                    case 'h': time = time.plusHours(integer); break;
                    case 'm': time = time.plusMinutes(integer); break;
                }
            }
            StringBuilder reason = new StringBuilder();
            boolean isIpBan = false;
            for (int i = 2; i < strings.length; i++) {
                if (i == strings.length - 1 && strings[i].equalsIgnoreCase("true")) {
                    isIpBan = true;
                    break;
                }
                if (i == 2) reason.append(strings[i]);
                else reason.append(" ").append(strings[i]);
            }
            Ban ban = new Ban(time, reason.toString());
            ban.setAlsoIP(isIpBan);
            userInt.punish(ban);
        } else {
            DystellarCore.getAsyncManager().execute(() -> {
                Mapping mapping = MariaDB.loadMapping(strings[1]);
                if (mapping == null) {
                    commandSender.sendMessage(ChatColor.RED + "This player does not exist in the database.");
                } else {
                    User user = MariaDB.loadPlayerFromDatabase(mapping.getUUID(), mapping.getIP(), mapping.getName());
                    LocalDateTime time;
                    if (strings[1].equalsIgnoreCase("null")) {
                        time = null;
                    } else {
                        time = LocalDateTime.now();
                        for (String e : strings[1].split(",")) {
                            if (!e.matches("[0-9]+[dhm]")) {
                                commandSender.sendMessage(ChatColor.RED + "The format is incorrect.");
                                return;
                            }
                            int integer = Integer.parseInt(e.substring(0, e.length() - 1));
                            switch (e.charAt(e.length() - 1)) {
                                case 'd': time = time.plusDays(integer); break;
                                case 'h': time = time.plusHours(integer); break;
                                case 'm': time = time.plusMinutes(integer); break;
                            }
                        }
                    }
                    StringBuilder reason = new StringBuilder();
                    boolean isIpBan = false;
                    for (int i = 2; i < strings.length; i++) {
                        if (i == strings.length - 1 && strings[i].equalsIgnoreCase("true")) {
                            isIpBan = true;
                            break;
                        }
                        if (i == 2) reason.append(strings[i]);
                        else reason.append(" ").append(strings[i]);
                    }
                    Ban ban = new Ban(time, reason.toString());
                    ban.setAlsoIP(isIpBan);
                    user.addPunishment(ban);
                    MariaDB.savePlayerToDatabase(user);
                }
            });
        }

        return true;
    }
}

