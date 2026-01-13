package net.zylesh.dystellarcore.commands;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.User;
import net.zylesh.dystellarcore.core.punishments.Blacklist;
import net.zylesh.dystellarcore.serialization.Punishments;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BlacklistCommand implements CommandExecutor {

    public BlacklistCommand() {
        Bukkit.getPluginCommand("blacklist").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!commandSender.hasPermission("dystellar.admin")) {
            commandSender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (strings.length < 2) {
            commandSender.sendMessage(ChatColor.RED + "Usage: /blacklist <player> <reason>");
            return true;
        }
        Player playerInt = Bukkit.getPlayer(strings[0]);
        if (playerInt != null && playerInt.isOnline()) {
            User userInt = User.get(playerInt);
            StringBuilder reason = new StringBuilder();
            for (int i = 2; i < strings.length; i++) {
                if (i == 2) reason.append(strings[i]);
                else reason.append(" ").append(strings[i]);
            }
            Blacklist blacklist = new Blacklist(reason.toString());
            userInt.punish(blacklist);
        } else {
            if (!(commandSender instanceof Player)) {
                commandSender.sendMessage(ChatColor.RED + "You must be a player in order to punish offline players.");
                return true;
            }
            Player p = (Player) commandSender;
            StringBuilder reason = new StringBuilder();
            for (int i = 2; i < strings.length; i++) {
                if (i == 2) reason.append(strings[i]);
                else reason.append(" ").append(strings[i]);
            }
            Blacklist ban = new Blacklist(reason.toString());
            DystellarCore.getInstance().sendPluginMessage(p, DystellarCore.PUNISHMENT_ADD_PROXYBOUND, strings[0], Punishments.serialize(ban));
        }
        return true;
    }
}

