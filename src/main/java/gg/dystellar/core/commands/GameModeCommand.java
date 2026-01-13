package gg.dystellar.core.commands;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.Msgs;
import net.zylesh.practice.PUser;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import gg.zylesh.skywars.SkywarsAPI;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameModeCommand implements CommandExecutor, Listener {

    public GameModeCommand() {
        Bukkit.getPluginCommand("gamemode").setExecutor(this);
        Bukkit.getPluginCommand("gm").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, DystellarCore.getInstance());
    }

    private static final Map<UUID, Map.Entry<ItemStack[], ItemStack[]>> playersInSpecMode = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length < 2) {
            if (!(commandSender instanceof Player)) return true;
            Player player = (Player) commandSender;
            if ((DystellarCore.PRACTICE_HOOK && PUser.get(player).isInGame()) || (DystellarCore.SKYWARS_HOOK && SkywarsAPI.getPlayerUser(player).isInGame())) {
                player.sendMessage(Msgs.COMMAND_DENY_INGAME);
                return true;
            }
            if (!strings[0].matches("[0-3]")) {
                player.sendMessage(ChatColor.RED + "The only gamemodes available are 0, 1, 2, 3");
                return true;
            }
            switch (Integer.parseInt(strings[0])) {
                case 0:
                    if (playersInSpecMode.containsKey(player.getUniqueId())) setSpectator(player);
                    player.setGameMode(GameMode.SURVIVAL);
                    break;
                case 1:
                    if ((DystellarCore.PRACTICE_HOOK && PUser.get(player).isInGame()) || (DystellarCore.SKYWARS_HOOK && SkywarsAPI.getPlayerUser(player).isInGame())) player.sendMessage(ChatColor.RED + "This command is blocked ingame.");
                    else {
                        if (playersInSpecMode.containsKey(player.getUniqueId())) setSpectator(player);
                        player.setGameMode(GameMode.CREATIVE);
                    }
                    break;
                case 2:
                    if (playersInSpecMode.containsKey(player.getUniqueId())) setSpectator(player);
                    player.setGameMode(GameMode.ADVENTURE);
                    break;
                case 3:
                    if (!playersInSpecMode.containsKey(player.getUniqueId())) setSpectator(player);
                    break;
                default:
                    break;
            }
        } else {
            Player player = Bukkit.getPlayer(strings[1]);
            if (player == null) {
                commandSender.sendMessage(Msgs.ERROR_PLAYER_NOT_ONLINE);
                return true;
            }
            if (!strings[0].matches("[0-3]")) {
                commandSender.sendMessage(ChatColor.RED + "The only gamemodes available are 0, 1, 2, 3");
                return true;
            }
            switch (Integer.parseInt(strings[0])) {
                case 0:
                    if (playersInSpecMode.containsKey(player.getUniqueId())) setSpectator(player);
                    player.setGameMode(GameMode.SURVIVAL);
                    break;
                case 1:
                    if ((DystellarCore.PRACTICE_HOOK && PUser.get(player).isInGame()) || (DystellarCore.SKYWARS_HOOK && SkywarsAPI.getPlayerUser(player).isInGame())) player.sendMessage(ChatColor.RED + "This command is blocked ingame.");
                    else {
                        if (playersInSpecMode.containsKey(player.getUniqueId())) setSpectator(player);
                        player.setGameMode(GameMode.CREATIVE);
                    }
                    break;
                case 2:
                    if (playersInSpecMode.containsKey(player.getUniqueId())) setSpectator(player);
                    player.setGameMode(GameMode.ADVENTURE);
                    break;
                case 3:
                    if (!playersInSpecMode.containsKey(player.getUniqueId())) setSpectator(player);
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    private static void setSpectator(Player player) {
        if (playersInSpecMode.containsKey(player.getUniqueId())) {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
            player.setAllowFlight(false);
            player.setFlying(false);
            player.getInventory().setArmorContents(playersInSpecMode.get(player.getUniqueId()).getKey());
            player.getInventory().setContents(playersInSpecMode.get(player.getUniqueId()).getValue());
            Bukkit.getScheduler().runTaskLater(DystellarCore.getInstance(), () -> playersInSpecMode.remove(player.getUniqueId()), 40L);
        } else {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, true));
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(true);
            player.teleport(new Location(player.getWorld(), player.getLocation().getX(), player.getLocation().getY() + 2.0, player.getLocation().getZ()));
            player.setFlying(true);
            ItemStack[] armorSet = player.getInventory().getArmorContents();
            ItemStack[] contents = player.getInventory().getContents();
            player.sendMessage(ChatColor.YELLOW + "Game Mode set to spectator (emulated).");
            playersInSpecMode.put(player.getUniqueId(), new AbstractMap.SimpleEntry<>(armorSet, contents));
        }
    }

    @EventHandler
    public void onHit(EntityDamageEvent event) {
        if (playersInSpecMode.containsKey(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        if (event instanceof EntityDamageByEntityEvent) {
            if (playersInSpecMode.containsKey(((EntityDamageByEntityEvent) event).getDamager().getUniqueId())) event.setCancelled(true);
        }
    }
}
