package net.zylesh.dystellarcore.commands;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.Msgs;
import net.zylesh.dystellarcore.core.Suffix;
import net.zylesh.dystellarcore.core.User;

import static gg.zylesh.dystellarcore.DystellarCore.NULL_GLASS;

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
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

public class SuffixCommand implements CommandExecutor, Listener {

    public SuffixCommand() {
        Bukkit.getPluginCommand("suffix").setExecutor(this);
        Bukkit.getPluginCommand("suffixs").setExecutor(this);
        Bukkit.getPluginCommand("suffixes").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, DystellarCore.getInstance());
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return true;
        Player p = (Player) commandSender;
        p.openInventory(Suffix.GUI);
        return true;
    }

    @EventHandler
    public void drag(InventoryDragEvent e) {
        if (e.getInventory().equals(Suffix.GUI)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void click(InventoryClickEvent e) {
        if (e.getClickedInventory() == null || !e.getClickedInventory().equals(Suffix.GUI) || e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;
        e.setCancelled(true);
        ItemStack i = e.getCurrentItem();
        if (i.equals(NULL_GLASS)) {
            e.setCancelled(true);
            return;
        }
        Player p = (Player) e.getWhoClicked();
        Suffix suffixSelected = Suffix.SUFFIXES_BY_SLOT.get(e.getSlot());
        if (suffixSelected == null) return;
        if (e.getWhoClicked().hasPermission(suffixSelected.getPermission())) {
            User user = User.get(e.getWhoClicked().getUniqueId());
            user.setSuffix(suffixSelected);
            p.sendMessage(ChatColor.BLUE + "Suffix selected" + ChatColor.WHITE + ": " + ChatColor.AQUA + user.getSuffix().toString());
            p.closeInventory();
        } else {
            p.sendMessage(Msgs.ERROR_PREFIX_NOT_OWNED);
            p.closeInventory();
        }
    }
}
