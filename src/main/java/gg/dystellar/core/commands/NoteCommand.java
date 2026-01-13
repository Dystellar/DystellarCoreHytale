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

public class NoteCommand implements CommandExecutor {

    public NoteCommand() {
        Bukkit.getPluginCommand("note").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!commandSender.hasPermission("dystellar.staff")) {
            commandSender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (strings.length < 2) {
            commandSender.sendMessage(ChatColor.RED + "Usage: /note <player> <message-note>");
            return true;
        }
        Player playerInt = Bukkit.getPlayer(strings[0]);
        if (playerInt != null && playerInt.isOnline()) {
            User userInt = User.get(playerInt);
            StringBuilder reason = new StringBuilder();
            for (int i = 1; i < strings.length; i++) {
                if (i == 1) reason.append(strings[i]);
                else reason.append(" ").append(strings[i]);
            }
            userInt.addNote(reason.toString());
            commandSender.sendMessage(ChatColor.DARK_AQUA + "Note added!");
        } else {
            DystellarCore.getAsyncManager().submit(() -> {
                Mapping mapping = MariaDB.loadMapping(strings[1]);
                if (mapping == null) {
                    commandSender.sendMessage(Msgs.ERROR_PLAYER_NOT_FOUND);
                } else {
                    User user = MariaDB.loadPlayerFromDatabase(mapping.getUUID(), mapping.getIP(), mapping.getName());
                    StringBuilder reason = new StringBuilder();
                    for (int i = 2; i < strings.length; i++) {
                        if (i == 2) reason.append(strings[i]);
                        else reason.append(" ").append(strings[i]);
                    }
                    user.addNote(reason.toString());
                    MariaDB.savePlayerToDatabase(user);
                    commandSender.sendMessage(ChatColor.DARK_AQUA + "Note added!");
                }
            });
        }
        return true;
    }
}
