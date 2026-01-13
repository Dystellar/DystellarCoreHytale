package gg.dystellar.core.utils.factory;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.zylesh.dystellarcore.core.punishments.Punishment;

public class InventoryBuilder {

	public static Inventory packPrompt(ItemStack confirm, ItemStack deny, ItemStack info) {
		Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_AQUA + "Resource Pack Prompt");

		inv.setItem(2, confirm);
        inv.setItem(4, info);
        inv.setItem(6, deny);

		return inv;
	}

	public static Inventory punishmentsInv(Player p, Set<Punishment> punishments) {
		Inventory inv = Bukkit.createInventory(null, 27, ChatColor.RED + "Punishments");
		int i = 0;
		for (Punishment p : punishments) {
			if (i > 26) break;
			invs.get(issuer.getUniqueId()).getValue()[i] = p;
			ItemStack icon = null;
			switch (p.getSerializedId()) {
				case Ban.SERIALIZATION_ID:
				case RankedBan.SERIALIZATION_ID: icon = new ItemStack(Material.WOOL, 1, (short) 14); break;
				case Blacklist.SERIALIZATION_ID: icon = new ItemStack(Material.WOOL, 1, (short) 15); break;
				case Mute.SERIALIZATION_ID:icon = new ItemStack(Material.WOOL, 1, (short) 4); break;
				case Warn.SERIALIZATION_ID: icon = new ItemStack(Material.WOOL, 1, (short) 6); break;
			}
			ItemMeta meta = icon.getItemMeta();
			meta.setDisplayName(ChatColor.RED + "Punishment ID: " + ChatColor.WHITE + p.hashCode());
			List<String> lore = List.of(
				ChatColor.DARK_AQUA + "Type" + ChatColor.WHITE + ": " + ChatColor.GRAY + p.getClass().getSimpleName(),
				ChatColor.DARK_AQUA + "Creation Date" + ChatColor.WHITE + ": " + ChatColor.GRAY + p.getCreationDate().format(DateTimeFormatter.ISO_DATE_TIME),
				ChatColor.DARK_AQUA + "Expiration Date" + ChatColor.WHITE + ": " + ChatColor.GRAY + (p.getExpirationDate() == null ? "Never" : p.getExpirationDate().format(DateTimeFormatter.ISO_DATE_TIME)),
				ChatColor.DARK_AQUA + "Reason" + ChatColor.WHITE + ": " + ChatColor.GRAY + p.getReason(),
				" ",
				ChatColor.YELLOW + "Right Click to remove this punishment from the player."
			);
			meta.setLore(lore);
			inv.setItem(i, icon);
			i++;
		}
		return inv;
	}
}
