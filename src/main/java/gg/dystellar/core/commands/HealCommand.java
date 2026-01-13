package gg.dystellar.core.commands;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.Msgs;
import net.zylesh.practice.PUser;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import gg.zylesh.skywars.SkywarsAPI;

public class HealCommand implements CommandExecutor {

    public HealCommand() {
        Bukkit.getPluginCommand("heal").setExecutor(this);
        Bukkit.getPluginCommand("hl").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length < 1) {
            if (!(commandSender instanceof Player)) return true;
            Player player = (Player) commandSender;
            if ((DystellarCore.PRACTICE_HOOK && PUser.get(player).isInGame()) || (DystellarCore.SKYWARS_HOOK && SkywarsAPI.getPlayerUser(player).isInGame())) {
                player.sendMessage(Msgs.COMMAND_DENY_INGAME);
                return true;
            }
            player.setHealth(20.0);
            player.setFoodLevel(20);
            player.setSaturation(12.0f);
        } else {
            Player player = Bukkit.getPlayer(strings[0]);
            if (player == null) {
                commandSender.sendMessage(Msgs.ERROR_PLAYER_NOT_ONLINE);
                return true;
            }
            player.setHealth(20.0);
            player.setFoodLevel(20);
            player.setSaturation(12.0f);
        }
        return true;
    }
}
