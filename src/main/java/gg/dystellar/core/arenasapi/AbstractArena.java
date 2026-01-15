package gg.dystellar.core.arenasapi;

import com.hypixel.hytale.server.core.inventory.ItemStack;

import java.util.List;

/**
 * Part of ArenasAPI, an API that resembles the behavior of WorldEdit
 * Functionality like region copying, pasting, and storing region to disk for portability.
 * It's designed for performance, to be able to create arenas efficiently at runtime for minigames and stuff
 *
 * May be unneeded as hytale already provides amazing tooling for world manipulation
 *
 * Also it's completely experimental, tested it for a few days and didn't fully got it working.
 *
 * The workflow should be: setting position1 with left click and position2 with right click, and from one position to another is the region selected
 */
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
