package gg.dystellar.core.common.punishments;

import net.zylesh.dystellarcore.core.inbox.Sendable;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SenderContainer {

    private final Sendable sender;
    private ItemStack icon;

    public SenderContainer(Sendable sender) {
        this.sender = sender;
        init();
    }

    private void init() {
        icon = new ItemStack(sender.getUnreadIcon().getType());
        String title = sender.getUnreadIcon().getItemMeta().getDisplayName();
        List<String> strings = new ArrayList<>(sender.getUnreadIcon().getItemMeta().getLore());
        strings.add(" ");
        strings.add(ChatColor.DARK_AQUA + "ID: " + ChatColor.YELLOW + sender.getId());
        strings.add(ChatColor.GREEN + "Click to copy");
        ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName(title);
        meta.setLore(strings);
        icon.setItemMeta(meta);
    }

    public Sendable getSender() {
        return sender;
    }

    public ItemStack getIcon() {
        return icon;
    }
}
