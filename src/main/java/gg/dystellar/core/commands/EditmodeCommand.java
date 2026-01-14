package gg.dystellar.core.commands;

import net.zylesh.dystellarcore.DystellarCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * This command enables edit mode, allowing admins to bypass spawn protections and other stuff
 */
public class EditmodeCommand implements CommandExecutor, Listener {

    private static final Set<UUID> playersOnEditmode = new HashSet<>();

    public EditmodeCommand() {
        Bukkit.getPluginCommand("editmode").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, DystellarCore.getInstance());
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return true;
        Player p = (Player)commandSender;
        if (!playersOnEditmode.contains(p.getUniqueId())) playersOnEditmode.add(p.getUniqueId());
        else playersOnEditmode.remove(p.getUniqueId());
        p.sendMessage(ChatColor.DARK_AQUA + "Editmode enabled: " + ChatColor.WHITE + playersOnEditmode.contains(p.getUniqueId()));
        return true;
    }

    @EventHandler
    public void drag(InventoryDragEvent event) {
        if (!playersOnEditmode.contains(event.getWhoClicked().getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    public void click(InventoryClickEvent event) {
        if (!playersOnEditmode.contains(event.getWhoClicked().getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    public void drop(PlayerDropItemEvent event) {
        if (!playersOnEditmode.contains(event.getPlayer().getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        if (!playersOnEditmode.contains(event.getPlayer().getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    public void blockPlace(BlockPlaceEvent event) {
        if (!playersOnEditmode.contains(event.getPlayer().getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    public void damage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && !playersOnEditmode.contains(event.getEntity().getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    public void itemPickup(PlayerPickupItemEvent event) {
        if (!playersOnEditmode.contains(event.getPlayer().getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    public void food(FoodLevelChangeEvent event) {
        if (!playersOnEditmode.contains(event.getEntity().getUniqueId())) event.setCancelled(true);
    }
}
