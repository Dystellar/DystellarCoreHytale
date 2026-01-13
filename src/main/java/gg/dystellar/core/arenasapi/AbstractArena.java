package net.zylesh.dystellarcore.arenasapi;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public abstract class AbstractArena {

    public static final ItemStack WAND = new ItemStack(Material.BLAZE_ROD);

    public static void init() {
        ItemMeta meta = WAND.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Utility Wand");
        List<String> lore = List.of(ChatColor.WHITE + "Use it like if it was World Edit.");
        meta.setLore(lore);
        WAND.setItemMeta(meta);
    }

    private final String name;

    public AbstractArena(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
