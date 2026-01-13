package net.zylesh.dystellarcore.commands;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.Msgs;
import net.zylesh.dystellarcore.core.User;
import net.zylesh.dystellarcore.core.punishments.Mute;
import net.zylesh.dystellarcore.serialization.Mapping;
import net.zylesh.dystellarcore.serialization.MariaDB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;

public class MuteCommand implements CommandExecutor {

    public MuteCommand() {
        Bukkit.getPluginCommand("mute").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!commandSender.hasPermission("dystellar.admin")) {
            commandSender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (strings.length < 3) {
            commandSender.sendMessage(ChatColor.RED + "Usage: /mute <player> <time> <reason>");
            return true;
        }
        Player playerInt = Bukkit.getPlayer(strings[0]);
        if (playerInt != null && playerInt.isOnline()) {
            User userInt = User.get(playerInt);
            LocalDateTime time;
            if (strings[1].equalsIgnoreCase("null")) {
                time = null;
            } else {
                time = LocalDateTime.now();
                for (String e : strings[1].split(",")) {
                    if (!e.matches("[0-9]+[dhm]")) {
                        commandSender.sendMessage(ChatColor.RED + "The format is incorrect.");
                        return true;
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
            for (int i = 2; i < strings.length; i++) {
                if (i == 2) reason.append(strings[i]);
                else reason.append(" ").append(strings[i]);
            }
            Mute ban = new Mute(time, reason.toString());
            userInt.punish(ban);
        } else {
            DystellarCore.getAsyncManager().execute(() -> {
                Mapping mapping = MariaDB.loadMapping(strings[1]);
                if (mapping == null) {
                    commandSender.sendMessage(Msgs.ERROR_PLAYER_NOT_FOUND);
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
                    for (int i = 2; i < strings.length; i++) {
                        if (i == 2) reason.append(strings[i]);
                        else reason.append(" ").append(strings[i]);
                    }
                    Mute ban = new Mute(time, reason.toString());
                    user.addPunishment(ban);
                    MariaDB.savePlayerToDatabase(user);
                }
            });
        }

        return true;
    }
}

