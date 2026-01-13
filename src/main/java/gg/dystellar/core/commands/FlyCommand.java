package net.zylesh.dystellarcore.commands;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.Msgs;
import net.zylesh.practice.PUser;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import gg.zylesh.skywars.SkywarsAPI;

public class FlyCommand implements CommandExecutor {

    public FlyCommand() {
        Bukkit.getPluginCommand("fly").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return true;
        Player player = (Player) commandSender;
        if (strings.length < 1) {
            if (!player.hasPermission("dystellar.plus")) {
                player.sendMessage(Msgs.FLY_NEED_PLUS_RANK);
                return true;
            }
            if ((DystellarCore.PRACTICE_HOOK && PUser.get(player).isInGame()) || (DystellarCore.SKYWARS_HOOK && SkywarsAPI.getPlayerUser(player).isInGame())) {
                player.sendMessage(Msgs.COMMAND_DENY_INGAME);
                return true;
            }
            player.setAllowFlight(!player.getAllowFlight());
            if (player.getAllowFlight()) {
                player.sendMessage(Msgs.FLY_MODE_ENABLED);
            } else {
                player.setFlying(false);
                player.sendMessage(Msgs.FLY_MODE_DISABLED);
            }
        } else {
            if (!player.getName().equalsIgnoreCase(strings[0]) && !player.hasPermission("dystellar.mod")) {
                player.sendMessage(Msgs.NO_PERMISSION);
                return true;
            }
            Player p = Bukkit.getPlayer(strings[0]);
            if (p == null || !p.isOnline()) {
                player.sendMessage(Msgs.ERROR_PLAYER_NOT_ONLINE);
                return true;
            }
            p.setAllowFlight(!p.getAllowFlight());
            if (p.getAllowFlight()) {
                player.sendMessage(Msgs.ADMIN_FLY_MODE_ENABLED_OTHER.replace("<player>", player.getName()));
                p.sendMessage(Msgs.FLY_MODE_ENABLED_BY_ADMIN);
            } else {
                p.setFlying(false);
                player.sendMessage(Msgs.ADMIN_FLY_MODE_DISABLED_OTHER.replace("<player>", player.getName()));
                p.sendMessage(Msgs.FLY_MODE_DISABLED_BY_ADMIN);
            }
        }
        return true;
    }
}
