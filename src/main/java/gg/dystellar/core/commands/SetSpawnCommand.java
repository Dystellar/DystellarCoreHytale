package gg.dystellar.core.commands;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.serialization.LocationSerialization;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Sets the spawn location. This is part of lobby functionality and should probably be separated into another plugin.
 */
public class SetSpawnCommand implements CommandExecutor {

    public SetSpawnCommand() {
        Bukkit.getPluginCommand("setspawn").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return true;
        DystellarCore.getInstance().getConfig().set("spawn-location", LocationSerialization.locationToString(((Player) commandSender).getLocation()));
        DystellarCore.getInstance().saveConfig();
        DystellarCore.SPAWN_LOCATION = ((Player) commandSender).getLocation();
        commandSender.sendMessage(ChatColor.AQUA + "Spawn set.");
        return true;
    }
}
