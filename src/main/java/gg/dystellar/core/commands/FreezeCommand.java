package gg.dystellar.core.commands;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.Msgs;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Moderator command that lets you freeze a player to prevent them from moving.
 * This is useful for moderators that want to redirect the player somewhere and ask them a few questions.
 * It interrups whatever game the player was in, as it's usually used for cheaters.
 */
public class FreezeCommand implements CommandExecutor, Listener {

    public FreezeCommand() {
        Bukkit.getPluginCommand("freeze").setExecutor(this);
        Bukkit.getPluginCommand("ss").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, DystellarCore.getInstance());
    }

    private static final Set<UUID> frozenPlayers = new HashSet<>();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length < 1) {
            commandSender.sendMessage(ChatColor.RED + "Usage: /ss <player>");
            return true;
        }
        Player player = Bukkit.getPlayer(strings[0]);
        if (player == null) {
            commandSender.sendMessage(Msgs.ERROR_PLAYER_NOT_ONLINE);
            return true;
        }
        if (frozenPlayers.remove(player.getUniqueId())) {
            commandSender.sendMessage(Msgs.STAFF_UNFREEZE.replace("<player>", player.getName()));
            player.sendMessage(DystellarCore.UNFREEZE_MESSAGE);
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            player.removePotionEffect(PotionEffectType.SLOW);
            return true;
        }
        frozenPlayers.add(player.getUniqueId());
        for (String s1 : DystellarCore.FREEZE_MESSAGE) player.sendMessage(s1);
        commandSender.sendMessage(Msgs.STAFF_FREEZE.replace("<player>", player.getName()));
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 100));
        return true;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (frozenPlayers.contains(event.getPlayer().getUniqueId())) {
            if (event.getTo().getX() != event.getFrom().getX() || event.getTo().getY() != event.getFrom().getY() || event.getTo().getZ() != event.getFrom().getZ()) event.getPlayer().teleport(event.getFrom());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        frozenPlayers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        frozenPlayers.remove(event.getPlayer().getUniqueId());
    }
}
