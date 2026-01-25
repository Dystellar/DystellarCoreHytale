package gg.dystellar.core.common;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.inbox.Inbox;
import net.zylesh.dystellarcore.core.punishments.Punishment;
import net.zylesh.dystellarcore.serialization.Consts;
import net.zylesh.dystellarcore.serialization.Mapping;
import net.zylesh.dystellarcore.serialization.MariaDB;
import net.zylesh.dystellarcore.utils.Utils;
import net.zylesh.dystellarcore.utils.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class User {

    protected static final Map<UUID, User> users = new ConcurrentHashMap<>(Bukkit.getMaxPlayers());

    public static User get(Player p) {
        return users.get(p.getUniqueId());
    }

    public synchronized static User get(UUID uuid) {
        return users.get(uuid);
    }

    public static Map<UUID, User> getUsers() {
        return users;
    }

    public static final byte PMS_ENABLED = 0;
    public static final byte PMS_ENABLED_FRIENDS_ONLY = 1;
    public static final byte PMS_DISABLED = 2;

    private final UUID id;
    private boolean globalChatEnabled = true;
    private byte privateMessagesMode = PMS_ENABLED_WITH_IGNORELIST;
    private Suffix suffix = Suffix.NONE;
    private final TreeSet<Punishment> punishments = new TreeSet<>();
    private String language = "en";
    private User lastMessagedPlayer;
    private final String ip;
    private final String name;
    private final Set<String> notes = new HashSet<>();
    private Inbox inbox;
    private boolean globalTabComplete = false;
    private boolean scoreboardEnabled = true;
    public int coins;

	public final List<UUID> friends = new ArrayList<>();
    public final List<UUID> ignoreList = new ArrayList<>();
    private int version;
    public byte[] tipsSent;
    public byte[] extraOptions;

    public User(UUID id, String ip, String name) {
        this.id = id;
        this.ip = ip;
        this.name = name;
    }

    public void punish(Punishment punishment) {
        this.punishments.add(punishment);
        punishment.onPunishment(this);
    }

    private Inventory configManager;

    private ItemStack globalChatItem;
    private ItemStack pmsItem;
    private ItemStack globalTabCompleteItem;
    private ItemStack scoreboardEnabledItem;

    public void initializeSettingsPanel(Player p) {
        configManager = Bukkit.createInventory(p, 18, ChatColor.DARK_AQUA + "Settings");

        this.globalChatItem = new ItemStack(Material.PAPER);
        this.pmsItem = new ItemStack(Material.BOOK);
        this.globalTabCompleteItem = new ItemStack(Material.COMMAND_BLOCK);
        this.scoreboardEnabledItem = new ItemStack(Material.CLAY);

        updateGlobalChatItem();
        updatePmsItem();
        updateGlobalTabCompleteItem();
        updateScoreboardItem();
    }

    private void updateScoreboardItem() {
        ItemMeta meta = scoreboardEnabledItem.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_AQUA + "Scoreboard");
        List<String> metaList = List.of(
                " ",
                scoreboardEnabled ? ChatColor.GREEN + "➢ On" : ChatColor.GRAY + " On",
                !scoreboardEnabled ? ChatColor.RED + "➢ Off" : ChatColor.GRAY + " Off",
                " ",
                ChatColor.YELLOW + "Click to toggle."
        );
        meta.setLore(metaList);
        scoreboardEnabledItem.setItemMeta(meta);
        configManager.setItem(0, scoreboardEnabledItem);
    }

    public void toggleScoreboard() {
        setScoreboardEnabled(!scoreboardEnabled);
        updateScoreboardItem();
    }

    private void updateGlobalTabCompleteItem() {
        ItemMeta gtci = globalTabCompleteItem.getItemMeta();
        gtci.setDisplayName(ChatColor.DARK_AQUA + "Global Tab Completion" + ChatColor.WHITE + ":");
        List<String> gciList = List.of(
                " ",
                globalTabComplete ? ChatColor.GREEN + "➢ On" : ChatColor.GRAY + " On",
                !globalTabComplete ? ChatColor.RED + "➢ Off" : ChatColor.GRAY + " Off",
                " ",
                ChatColor.YELLOW + "Click to toggle."
        );
        gtci.setLore(gciList);
        globalTabCompleteItem.setItemMeta(gtci);
        configManager.setItem(1, globalTabCompleteItem);
    }

    public void toggleGlobalTabComplete() {
        setGlobalTabComplete(!globalTabComplete);
        updateGlobalTabCompleteItem();
    }

    private void updateGlobalChatItem() {
        ItemMeta gci = globalChatItem.getItemMeta();
        gci.setDisplayName(ChatColor.DARK_AQUA + "Global Chat" + ChatColor.WHITE + ":");
        List<String> gciList = List.of(
                " ",
                globalChatEnabled ? ChatColor.GREEN + "➢ On" : ChatColor.GRAY + " On",
                !globalChatEnabled ? ChatColor.RED + "➢ Off" : ChatColor.GRAY + " Off",
                " ",
                ChatColor.YELLOW + "Click to toggle."
        );
        gci.setLore(gciList);
        globalChatItem.setItemMeta(gci);
        configManager.setItem(2, globalChatItem);
    }

    public void toggleGlobalChat() {
        setGlobalChatEnabled(!globalChatEnabled);
        updateGlobalChatItem();
    }

    /**
     * Tip: the class Consts contains all bytes entries for all the tips.
     */
    public boolean checkIsTipSent(byte entry) {
        if ((entry + 1) > tipsSent.length) return false;
        return tipsSent[entry] == Consts.BYTE_TRUE;
    }

    private void updatePmsItem() {
        ItemMeta pmsi = pmsItem.getItemMeta();
        pmsi.setDisplayName(ChatColor.DARK_AQUA + "Private Messages" + ChatColor.WHITE + ":");
        List<String> pmsiList = null;
        switch (privateMessagesMode) {
            case PMS_ENABLED:
                pmsiList = List.of(
                        " ",
                        ChatColor.GREEN + "➢ On",
                        ChatColor.GRAY + " On (with Ignore List)",
                        ChatColor.GRAY + " On (Friends Only)",
                        ChatColor.GRAY + " Off",
                        " ",
                        ChatColor.YELLOW + "Click to change."
                );
                break;
            case PMS_ENABLED_WITH_IGNORELIST:
                pmsiList = List.of(
                        " ",
                        ChatColor.GRAY + " On",
                        ChatColor.YELLOW + "➢ On (with Ignore List)",
                        ChatColor.GRAY + " On (Friends Only)",
                        ChatColor.GRAY + " Off",
                        " ",
                        ChatColor.YELLOW + "Click to change."
                );
                break;
            case PMS_ENABLED_FRIENDS_ONLY:
                pmsiList = List.of(
                        " ",
                        ChatColor.GRAY + " On",
                        ChatColor.GRAY + " On (with Ignore List)",
                        ChatColor.GOLD + "➢ On (Friends Only)",
                        ChatColor.GRAY + " Off",
                        " ",
                        ChatColor.YELLOW + "Click to change."
                );
                break;
            case PMS_DISABLED:
                pmsiList = List.of(
                        " ",
                        ChatColor.GRAY + " On",
                        ChatColor.GRAY + " On (with Ignore List)",
                        ChatColor.GRAY + " On (Friends Only)",
                        ChatColor.RED + "➢ Off",
                        " ",
                        ChatColor.YELLOW + "Click to change."
                );
                break;
        }
        pmsi.setLore(pmsiList);
        pmsItem.setItemMeta(pmsi);
        configManager.setItem(3, pmsItem);
    }

    public void togglePms() {
        switch (privateMessagesMode) {
            case PMS_ENABLED:
            case PMS_ENABLED_WITH_IGNORELIST:
            case PMS_ENABLED_FRIENDS_ONLY:
                privateMessagesMode++;
                break;
            case PMS_DISABLED:
                privateMessagesMode = PMS_ENABLED;
                break;
        }
        updatePmsItem();
    }

    public Set<String> getNotes() {
        return notes;
    }

    public void addNote(String note) {
        notes.add(note);
    }

    public Inbox getInbox() {
        return inbox;
    }

    public String getIp() {
        return ip;
    }

    public String getName() {
        return name;
    }

    public boolean isGlobalChatEnabled() {
        return globalChatEnabled;
    }

    public Suffix getSuffix() {
        return suffix;
    }

    public boolean isScoreboardEnabled() {
        return scoreboardEnabled;
    }

    public void setScoreboardEnabled(boolean scoreboardEnabled) {
        this.scoreboardEnabled = scoreboardEnabled;
    }

    public void setSuffix(Suffix suffix) {
        this.suffix = suffix;
    }

    public boolean isGlobalTabComplete() {
        return globalTabComplete;
    }

    public void setGlobalTabComplete(boolean globalTabComplete) {
        this.globalTabComplete = globalTabComplete;
    }

    public void setGlobalChatEnabled(boolean globalChatEnabled) {
        this.globalChatEnabled = globalChatEnabled;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Set<Punishment> getPunishments() {
        return punishments;
    }

    public UUID getUUID() {
        return id;
    }

    /**
     * Do not use if you don't know what you are doing, if you want to punish a player use user.punish(punishment) instead.
     * this method is only for internal purposes and its ONLY called when punishing offline players. Using this method on
     * online players will not work as expected.
     */
    public void addPunishment(Punishment punishment) {
        this.punishments.add(punishment);
    }

    public User getLastMessagedPlayer() {
        return lastMessagedPlayer;
    }

    public void setLastMessagedPlayer(User lastMessagedPlayer) {
        this.lastMessagedPlayer = lastMessagedPlayer;
    }

    public byte getPrivateMessagesMode() {
        return privateMessagesMode;
    }

    public void setPrivateMessagesMode(byte privateMessagesActive) {
        this.privateMessagesMode = privateMessagesActive;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public static class UserListener implements Listener {

        public UserListener() {
            Bukkit.getPluginManager().registerEvents(this, DystellarCore.getInstance());
            DystellarCore.getAsyncManager().scheduleAtFixedRate(() -> {
                synchronized (users) {
                    for (User user : users.values()) {
                       MariaDB.savePlayerToDatabase(user);
                    }
                }
            }, 10L, 10L, TimeUnit.MINUTES);
        }

        @EventHandler
        public void onCraft(CraftItemEvent event) {
            if (DystellarCore.ALLOW_SIGNS && event.getCurrentItem() != null && (event.getCurrentItem().getType().equals(Material.SIGN) || event.getCurrentItem().getType().equals(Material.SIGN_POST) || event.getCurrentItem().getType().equals(Material.WALL_SIGN))) event.setCancelled(true);
        }

        @EventHandler
        public void onJoin(AsyncPlayerPreLoginEvent event) {
            if (!Validate.validateName(event.getName())) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Your nickname is invalid. (Contact us if you think this is an error)");
                Bukkit.getLogger().warning(event.getUniqueId() + " tried to join with an invalid nickname.");
                return;
            }
            User user = MariaDB.loadPlayerFromDatabase(event.getUniqueId(), event.getAddress().getHostAddress(), event.getName());
            Mapping map = MariaDB.loadMapping(event.getAddress().getHostAddress());
            if (user == null) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Could not fetch your data.");
                return;
            }
            if (!user.getPunishments().isEmpty() && !DystellarCore.ALLOW_BANNED_PLAYERS) {
                LocalDateTime now = LocalDateTime.now();
                for (Punishment punishment : user.punishments) {
                    if (punishment.getExpirationDate().isBefore(now) && !punishment.allowJoinMinigames()) {
                        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, punishment.getMessage().replaceAll("<reason>", punishment.getReason()).replaceAll("<time>", Utils.getTimeFormat(punishment.getExpirationDate())));
                        return;
                    }
                }
            }
            if (map != null && map.getPunishments() != null && !map.getPunishments().isEmpty()) {
                LocalDateTime now = LocalDateTime.now();
                for (Punishment punishment : map.getPunishments()) {
                    if (punishment.getExpirationDate().isBefore(now)) {
                        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, punishment.getMessage().replaceAll("<reason>", punishment.getReason()).replaceAll("<time>", Utils.getTimeFormat(punishment.getExpirationDate())));
                    }
                }
            }
            users.put(event.getUniqueId(), user);
        }

        @EventHandler
        public void onJoin(PlayerJoinEvent event) {
            User user = User.get(event.getPlayer());
            user.initializeSettingsPanel(event.getPlayer());
            Bukkit.getScheduler().runTaskLater(DystellarCore.getInstance(), () -> {
                if (user.globalTabComplete) DystellarCore.getInstance().sendPluginMessage(event.getPlayer(), DystellarCore.GLOBAL_TAB_REGISTER);
                if (DystellarCore.PACK_ENABLED) {
                    DystellarCore.getInstance().sendPluginMessage(event.getPlayer(), DystellarCore.SHOULD_SEND_PACK);
                    if (DystellarCore.DEBUG_MODE) Bukkit.getLogger().info("[Debug] Resource pack request sent to proxy.");
                }
            }, 30L);
        }

        @EventHandler
        public void clicked(InventoryClickEvent event) {
            User u = User.get(event.getWhoClicked().getUniqueId());
            if (u == null) {
                event.setCancelled(true);
                return;
            }
            if (event.getClickedInventory().equals(u.configManager)) {
                event.setCancelled(true);
                ItemStack i = event.getCurrentItem();
                if (i == null || i.getType() == Material.AIR) return;
                if (i.equals(u.globalChatItem)) u.toggleGlobalChat();
                else if (i.equals(u.pmsItem)) u.togglePms();
                else if (i.equals(u.globalTabCompleteItem)) u.toggleGlobalTabComplete();
                else if (i.equals(u.scoreboardEnabledItem)) u.toggleScoreboard();
                Player p = (Player) event.getWhoClicked();
                p.playSound(p.getLocation(), Sound.CLICK, 1.8f, 1.8f);
            }
        }

        @EventHandler
        public void drag(InventoryDragEvent event) {
            User u = User.get(event.getWhoClicked().getUniqueId());
            if (event.getInventory().equals(u.configManager)) event.setCancelled(true);
        }

        @EventHandler
        public void onLeave(PlayerQuitEvent event) {
            DystellarCore.getAsyncManager().submit(() -> MariaDB.savePlayerToDatabase(users.get(event.getPlayer().getUniqueId())));
        }

        @EventHandler
        public void onKick(PlayerKickEvent event) {
            DystellarCore.getAsyncManager().submit(() -> MariaDB.savePlayerToDatabase(users.get(event.getPlayer().getUniqueId())));
        }
    }
}
