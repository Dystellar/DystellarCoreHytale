package net.zylesh.dystellarcore.commands;

import net.zylesh.dystellarcore.DystellarCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BroadcastCommand implements CommandExecutor {

    public BroadcastCommand() {
        Bukkit.getPluginCommand("broadcast").setExecutor(this);
        Bukkit.getPluginCommand("bc").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length < 1) commandSender.sendMessage(ChatColor.RED + "Usage: /bc <mensaje>");
        StringBuilder sb = new StringBuilder();
        for (String words : strings) {
            sb.append(ChatColor.translateAlternateColorCodes('&', words)).append(" ");
        }
        Bukkit.broadcastMessage(DystellarCore.BROADCAST_FORMAT.replaceFirst("<message>", sb.toString()));
        return true;
    }
}
