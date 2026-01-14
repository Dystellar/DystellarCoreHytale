package gg.dystellar.core.listeners;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.config.ConfValues;
import net.zylesh.dystellarcore.core.User;
import net.zylesh.dystellarcore.serialization.Consts;
import net.zylesh.dystellarcore.utils.factory.InventoryBuilder;
import net.zylesh.dystellarcore.utils.factory.ItemBuilder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

/**
 * This is a resourcepack prompt that players receive, should probably be reimplemented because it's shit and doesn't work
 */
public class ResourceListener implements Listener {

    private final ItemStack CONFIRM = ItemBuilder.packConfirm();
    private final ItemStack INFO = ItemBuilder.packInfo();
    private final ItemStack DENY = ItemBuilder.packDeny();

    private final Inventory packConfirmation = InventoryBuilder.packPrompt(CONFIRM, DENY, INFO);

    private static ResourceListener instance;


    public ResourceListener() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, DystellarCore.getInstance());
	}

    private void sendResourcePack(Player p) {
        p.setResourcePack(ConfValues.PACK_LINK);
    }

    private final Set<Player> prompts = new HashSet<>();

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null)
			return;

        if (event.getClickedInventory().equals(packConfirmation)) {
            event.setCancelled(true);
            ItemStack i = event.getCurrentItem();
            if (i == null || i.getType().equals(Material.AIR)) return;
            Player p = (Player) event.getWhoClicked();
            if (i.equals(CONFIRM)) {
                prompts.remove(p);
                p.closeInventory();
                User user = User.get(p);
                user.extraOptions[Consts.EXTRA_OPTION_RESOURCEPACK_PROMPT_POS] = Consts.BYTE_FALSE;
                Bukkit.getScheduler().runTaskLater(DystellarCore.getInstance(), () -> sendResourcePack(p), 5L);
            } else if (i.equals(DENY)) {
                prompts.remove(p);
                p.kickPlayer(ChatColor.RED + "Resource Pack denied.");
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().equals(packConfirmation)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(packConfirmation) && prompts.contains((Player) event.getPlayer())) {
            event.getPlayer().openInventory(packConfirmation);
        }
    }
}
