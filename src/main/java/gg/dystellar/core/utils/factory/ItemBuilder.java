package gg.dystellar.core.utils.factory;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemBuilder {

	public static ItemStack packConfirm() {
		ItemStack confirm = new ItemStack(Material.LIME_WOOL);
	
		ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.setDisplayName(ChatColor.GREEN + "Confirm");
        confirm.setItemMeta(confirmMeta);
		
		return confirm;
	}

	public static ItemStack packDeny() {
		ItemStack deny = new ItemStack(Material.RED_WOOL);

		ItemMeta meta = deny.getItemMeta();
		meta.setDisplayName(ChatColor.RED + "Deny");
		deny.setItemMeta(meta);

		return deny;
	}

	public static ItemStack packInfo() {
		ItemStack info = new ItemStack(Material.WHITE_WOOL);
		
		ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(ChatColor.DARK_AQUA + "Info:");
        List<String> loreInfo = List.of(
                ChatColor.WHITE + "This server uses a custom resource pack",
                ChatColor.WHITE + "to enhance your game experience. Click",
                ChatColor.WHITE + "\"Confirm\" to download and apply."
        );
        infoMeta.setLore(loreInfo);
		info.setItemMeta(infoMeta);

		return info;
	}
}
