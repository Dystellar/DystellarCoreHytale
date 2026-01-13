package net.zylesh.dystellarcore.commands;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.User;
import net.zylesh.dystellarcore.core.punishments.*;
import net.zylesh.dystellarcore.utils.factory.InventoryBuilder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UnpunishCommand implements Listener, CommandExecutor {

    public static final ConcurrentMap<UUID, Map.Entry<UUID, Punishment[]>> invs = new ConcurrentHashMap<>();

    public UnpunishCommand() {
        Bukkit.getPluginCommand("unpunish").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, DystellarCore.getInstance());
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player))
			return true;

        Player p = (Player) commandSender;

        if (strings.length < 1) {
            p.sendMessage(ChatColor.RED + "Error: You must specify a player!");
            p.sendMessage(ChatColor.RED + "Usage: /unpunish <player>");
            return true;
        }

        Player pInt = Bukkit.getPlayer(strings[0]);

        if (pInt != null && pInt.isOnline()) {
            invs.put(p.getUniqueId(), new AbstractMap.SimpleImmutableEntry<>(pInt.getUniqueId(), new Punishment[27]));
            Inventory inv = InventoryBuilder.punishmentsInv(p, User.get(pInt).getPunishments());
            p.openInventory(inv);
        } else {
            p.sendMessage(ChatColor.DARK_AQUA + "This player is not online in your server. Asking data to the proxy...");
            p.sendMessage(ChatColor.DARK_AQUA + "Please wait...");
            DystellarCore.getInstance().sendPluginMessage(p, DEMAND_PUNISHMENTS_DATA, strings[0]);
        }

        return true;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().getTitle().equals(TITLE) || event.getCurrentItem() == null)
			return;
		event.setCancelled(true);

        Punishment pun = invs.get(event.getWhoClicked().getUniqueId()).getValue()[event.getSlot()];

        if (pun == null)
			return;

        Player pInt = Bukkit.getPlayer(invs.get(event.getWhoClicked().getUniqueId()).getKey());
        Player p = (Player) event.getWhoClicked();

        if (pInt != null && pInt.isOnline()) {
            User user = User.get(pInt);

            user.getPunishments().remove(pun);
            event.getWhoClicked().closeInventory();
            p.sendMessage(ChatColor.GREEN + "Punishment removed!");
            pInt.sendMessage(ChatColor.GREEN + "The punishment with ID " + pun.hashCode() + " was removed from your punishments list!");

            String[] details = new String[] {
                    ChatColor.DARK_GREEN + "Punishment details:",
                    "===============================",
                    ChatColor.DARK_AQUA + "Type" + ChatColor.WHITE + ": " + ChatColor.GRAY + p.getClass().getSimpleName(),
                    ChatColor.DARK_AQUA + "Creation Date" + ChatColor.WHITE + ": " + ChatColor.GRAY + pun.getCreationDate().format(DateTimeFormatter.ISO_DATE_TIME),
                    ChatColor.DARK_AQUA + "Expiration Date" + ChatColor.WHITE + ": " + ChatColor.GRAY + (pun.getExpirationDate() == null ? "Never" : pun.getExpirationDate().format(DateTimeFormatter.ISO_DATE_TIME)),
                    ChatColor.DARK_AQUA + "Reason" + ChatColor.WHITE + ": " + ChatColor.GRAY + pun.getReason(),
                    "==============================="
            };

            pInt.sendMessage(details);
        } else {
            p.sendMessage(ChatColor.GREEN + "Sending request...");
            DystellarCore.getInstance().sendPluginMessage(p, DystellarCore.REMOVE_PUNISHMENT_BY_ID, invs.get(p.getUniqueId()).getKey().toString(), invs.get(p.getUniqueId()).getValue()[event.getSlot()].hashCode());
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getTitle().equals(TITLE)) invs.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getTitle().equals(TITLE)) event.setCancelled(true);
    }
}
