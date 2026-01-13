package net.zylesh.dystellarcore.core.inbox.senders;

import net.zylesh.dystellarcore.core.inbox.Inbox;
import net.zylesh.dystellarcore.core.inbox.Sendable;
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
import java.util.Random;
import java.util.UUID;

public class Message implements Sendable {

    public static final byte ID = 0;

    private static final Random r = new Random();

    protected final String[] message;
    protected final LocalDateTime submissionDate;
    protected final Inbox inbox;
    protected final String from;
    protected ItemStack icon;
    protected boolean isDeleted;
    protected final int id;

    public Message(Inbox inbox, String from, String... messageLines) {
        this.message = messageLines;
        this.inbox = inbox;
        this.from = from;
        this.submissionDate = LocalDateTime.now();
        this.id = r.nextInt();
        this.isDeleted = false;
        initializeIcons();
    }

    public Message(Inbox inbox, int id, String from, String[] messageLines, LocalDateTime submissionDate, boolean isDeleted) {
        this.inbox = inbox;
        this.id = id;
        this.from = from;
        this.message = messageLines;
        this.submissionDate = submissionDate;
        this.isDeleted = isDeleted;
    }

    @Override
    public void initializeIcons() {
        this.icon = new ItemStack(Material.PAPER);
        ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_AQUA + "Message");
        List<String> lore = new ArrayList<>();
        lore.add(" ");
        Arrays.asList(this.message).forEach(s -> lore.add(ChatColor.WHITE + s));
        lore.add(" ");
        lore.add(ChatColor.DARK_AQUA + "From" + ChatColor.WHITE + ": " + this.from);
        lore.add(" ");
        lore.add(ChatColor.DARK_AQUA + "Submission Date" + ChatColor.WHITE + ": " + submissionDate.format(DateTimeFormatter.ISO_DATE_TIME));
        lore.add(" ");
        lore.add(ChatColor.YELLOW + "Right Click to delete.");
        meta.setLore(lore);
        icon.setItemMeta(meta);
    }

    public String[] getMessage() {
        return message;
    }

    public String getSerializedMessage() {
        StringBuilder builder = new StringBuilder();
        for (String s : message) builder.append(s).append(":;");
        return builder.toString();
    }

    @Override
    public ItemStack getUnreadIcon() {
        return icon;
    }

    @Override
    public byte getSerialID() {
        return ID;
    }

    @Override
    public String getFrom() {
        return from;
    }

    @Override
    public ItemStack getReadIcon() {
        return icon;
    }

    @Override
    public void onLeftClick() {}

    @Override
    public void onRightClick() {
        inbox.deleteSender(this);
        Player p = Bukkit.getPlayer(inbox.getUser().getUUID());
        if (p != null) p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.4f, 1.4f);
        delete();
    }

    @Override
    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void delete() {
        isDeleted = true;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int compareTo(Sendable o) {
        return submissionDate.compareTo(o.getSubmissionDate());
    }

    /**
     * Must override
     */
    public Message clone(Inbox inbox) {
        return new Message(inbox, id, from, message, LocalDateTime.now(), isDeleted);
    }

	@Override
	public Object[] encode(UUID target) {
		String msg = getSerializedMessage();
		String from = getFrom();
		Boolean deleted = isDeleted();
		return new Object[] {target.toString(), SenderTypes.MESSAGE, id, submissionDate.format(DateTimeFormatter.ISO_DATE_TIME), msg, from, deleted};
	}
}
