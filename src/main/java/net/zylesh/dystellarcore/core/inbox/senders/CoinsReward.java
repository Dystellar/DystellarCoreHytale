package net.zylesh.dystellarcore.core.inbox.senders;

import net.zylesh.dystellarcore.core.inbox.Inbox;
import net.zylesh.dystellarcore.core.inbox.SenderTypes;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CoinsReward extends Reward {

    public static final byte ID = 1;

    private final int coins;

    public CoinsReward(Inbox inbox, int coins, String title, String from, String... messageLines) {
        super(inbox, title, from, messageLines);
        this.coins = coins;
    }

    public CoinsReward(Inbox inbox, int id, String from, String[] messageLines, LocalDateTime submissionDate, boolean isDeleted, String title, boolean isClaimed, int coins) {
        super(inbox, id, from, messageLines, submissionDate, isDeleted, title, isClaimed);
        this.coins = coins;
    }

    @Override
    public void initializeIcons() {
        this.icon = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_AQUA + title);
        List<String> lore = new ArrayList<>();
        lore.add(" ");
        Arrays.asList(this.message).forEach(s -> lore.add(ChatColor.WHITE + s));
        lore.add(" ");
        lore.add(ChatColor.DARK_AQUA + "Reward" + ChatColor.WHITE + ": " + ChatColor.GOLD + coins);
        lore.add(" ");
        lore.add(ChatColor.DARK_AQUA + "From" + ChatColor.WHITE + ": " + this.from);
        lore.add(" ");
        lore.add(ChatColor.DARK_AQUA + "Submission Date" + ChatColor.WHITE + ": " + submissionDate.format(DateTimeFormatter.ISO_DATE_TIME));
        lore.add(" ");
        lore.add(ChatColor.GREEN + "Click to claim.");
        meta.setLore(lore);
        icon.setItemMeta(meta);
        this.readIcon = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta meta1 = icon.getItemMeta();
        meta1.setDisplayName(ChatColor.RED.toString() + ChatColor.STRIKETHROUGH + title + ChatColor.GRAY + " - " + ChatColor.YELLOW + "Claimed");
        List<String> lore1 = new ArrayList<>();
        lore1.add(" ");
        Arrays.asList(this.message).forEach(s -> lore1.add(ChatColor.WHITE + s));
        lore.add(" ");
        lore.add(ChatColor.DARK_AQUA + "Reward" + ChatColor.WHITE + ": " + ChatColor.GOLD + coins);
        lore1.add(" ");
        lore1.add(ChatColor.DARK_AQUA + "From" + ChatColor.WHITE + ": " + this.from);
        lore1.add(" ");
        lore1.add(ChatColor.DARK_AQUA + "Submission Date" + ChatColor.WHITE + ": " + submissionDate.format(DateTimeFormatter.ISO_DATE_TIME));
        lore1.add(" ");
        lore1.add(ChatColor.YELLOW + "Right Click to delete.");
        meta1.setLore(lore1);
        icon.setItemMeta(meta1);
    }

    @Override
    public void onLeftClick() {
        isClaimed = claim();
        if (isClaimed) inbox.update();
    }


    @Override
    public void onRightClick() {
        if (isClaimed) {
            inbox.deleteSender(this);
            Player p = Bukkit.getPlayer(inbox.getUser().getUUID());
            if (p != null) p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.4f, 1.4f);
            delete();
        }
    }

    public int getCoins() {
        return coins;
    }

    @Override
    public byte getSerialID() {
        return ID;
    }

    @Override
    public boolean claim() {
        if (isClaimed) return false;
        inbox.getUser().coins += coins;
        Bukkit.getPlayer(inbox.getUser().getUUID()).playSound(Bukkit.getPlayer(inbox.getUser().getUUID()).getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.12f, 1.12f);
        return true;
    }

    @Override
    public CoinsReward clone(Inbox inbox) {
        return new CoinsReward(inbox, id, from, message, submissionDate, isDeleted, title, isClaimed, coins);
    }

	@Override
	public Object[] encode(UUID target) {
        return new Object[] {target.toString(), SenderTypes.COINS_REWARD, id, submissionDate.format(DateTimeFormatter.ISO_DATE_TIME), coins, title, getSerializedMessage(), getFrom(), isClaimed(), isDeleted};
	}
}
