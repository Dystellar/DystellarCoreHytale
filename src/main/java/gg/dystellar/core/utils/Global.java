package gg.dystellar.core.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Global {

    public static final ItemStack NULL_GLASS = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);

    static {
        ItemMeta meta = NULL_GLASS.getItemMeta();
        meta.setDisplayName(ChatColor.GRAY + " ");
        NULL_GLASS.setItemMeta(meta);
    }
}
