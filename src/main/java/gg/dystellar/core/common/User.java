package gg.dystellar.core.common;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import gg.dystellar.core.common.inbox.Inbox;
import gg.dystellar.core.common.punishments.Punishment;

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
    private byte privateMessagesMode = PMS_ENABLED;
    private Suffix suffix = Suffix.NONE;
    private final List<Punishment> punishments = new ArrayList<>();
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

	// TODO: implement properly
    public void punish(Punishment punishment) {
        this.punishments.add(punishment);
    }

    public void toggleScoreboard() {
        setScoreboardEnabled(!scoreboardEnabled);
        // TODO: updateScoreboardItem();
    }

    public void toggleGlobalTabComplete() {
        setGlobalTabComplete(!globalTabComplete);
        // TODO: updateGlobalTabCompleteItem();
    }

    public void toggleGlobalChat() {
        setGlobalChatEnabled(!globalChatEnabled);
        // TODO: updateGlobalChatItem();
    }

    public void togglePms() {
        switch (privateMessagesMode) {
            case PMS_ENABLED:
            case PMS_ENABLED_FRIENDS_ONLY:
                privateMessagesMode++;
                break;
            case PMS_DISABLED:
                privateMessagesMode = PMS_ENABLED;
                break;
        }
        // TODO: updatePmsItem();
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

    public List<Punishment> getPunishments() {
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
