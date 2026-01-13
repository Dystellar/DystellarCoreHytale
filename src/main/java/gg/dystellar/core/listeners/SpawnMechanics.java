package net.zylesh.dystellarcore.listeners;

import net.zylesh.dystellarcore.DystellarCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpawnMechanics implements Listener {

    private static boolean GAMES_ENABLED;
    private static int GAMES_SLOT;
    private static final List<String> GAMES_LORE = new ArrayList<>();
    private static final Map<ItemStack, String> GAMES_INV_STACKS = new HashMap<>();
    private static ItemStack GAMES_ITEM;

    private static boolean PEARL_ENABLED;
    private static final List<String> PEARL_LORE = new ArrayList<>();
    private static int PEARL_SLOT;
    private static ItemStack PEARL_ITEM;

    private static boolean LOBBIES_ENABLED;
    private static int LOBBIES_SLOT;
    private static final List<String> LOBBIES_LORE = new ArrayList<>();
    private static ItemStack LOBBIES_ITEM;
    private static final Map<ItemStack, String> LOBBIES_INV_STACKS = new HashMap<>();

    private static boolean PROFILE_ENABLED;
    private static int PROFILE_SLOT;
    private static final List<String> PROFILE_LORE = new ArrayList<>();
    private static String PROFILE_COMMAND;
    private static ItemStack PROFILE_ITEM;

    private static boolean CUSTOM_ENABLED;
    private static int CUSTOM_SLOT;
    private static final List<String> CUSTOM_LORE = new ArrayList<>();
    private static String CUSTOM_COMMAND;
    private static ItemStack CUSTOM_ITEM;

    private static Inventory gamesInv;
    private static Inventory lobbiesInv;

    public SpawnMechanics() {
        Bukkit.getPluginManager().registerEvents(this, DystellarCore.getInstance());
        initialize();
    }

    private void initialize() {
        DystellarCore p = DystellarCore.getInstance();
        CUSTOM_ENABLED = p.getSpawnitems().getBoolean("custom1.enabled");
        if (CUSTOM_ENABLED) {
            Material CUSTOM_MATERIAL = Material.getMaterial(p.getSpawnitems().getString("custom1.material"));
            CUSTOM_SLOT = p.getSpawnitems().getInt("custom1.slot");
            String CUSTOM_TITLE = ChatColor.translateAlternateColorCodes('&', p.getSpawnitems().getString("custom1.title"));
            CUSTOM_LORE.clear();
            for (String s : p.getSpawnitems().getStringList("custom1.lore")) CUSTOM_LORE.add(ChatColor.translateAlternateColorCodes('&', s));
            CUSTOM_COMMAND = p.getSpawnitems().getString("custom1.command");
            CUSTOM_ITEM = new ItemStack(CUSTOM_MATERIAL);
            ItemMeta meta = CUSTOM_ITEM.getItemMeta();
            meta.setDisplayName(CUSTOM_TITLE);
            meta.setLore(CUSTOM_LORE);
            CUSTOM_ITEM.setItemMeta(meta);
        }
        PROFILE_ENABLED = p.getSpawnitems().getBoolean("perfil.enabled");
        if (PROFILE_ENABLED) {
            Material PROFILE_MATERIAL = Material.getMaterial(p.getSpawnitems().getString("perfil.material"));
            PROFILE_SLOT = p.getSpawnitems().getInt("perfil.slot");
            String PROFILE_TITLE = ChatColor.translateAlternateColorCodes('&', p.getSpawnitems().getString("perfil.title"));
            PROFILE_LORE.clear();
            for (String s : p.getSpawnitems().getStringList("perfil.lore")) PROFILE_LORE.add(ChatColor.translateAlternateColorCodes('&', s));
            PROFILE_COMMAND = p.getSpawnitems().getString("perfil.command");
            PROFILE_ITEM = new ItemStack(PROFILE_MATERIAL);
            ItemMeta meta = PROFILE_ITEM.getItemMeta();
            meta.setDisplayName(PROFILE_TITLE);
            meta.setLore(PROFILE_LORE);
            PROFILE_ITEM.setItemMeta(meta);
        }
        PEARL_ENABLED = p.getSpawnitems().getBoolean("enderpearl.enabled");
        if (PEARL_ENABLED) {
            String PEARL_TITLE = ChatColor.translateAlternateColorCodes('&', p.getSpawnitems().getString("enderpearl.title"));
            Material PEARL_MATERIAL = Material.getMaterial(p.getSpawnitems().getString("enderpearl.material"));
            PEARL_LORE.clear();
            for (String s : p.getSpawnitems().getStringList("enderpearl.lore")) PEARL_LORE.add(ChatColor.translateAlternateColorCodes('&', s));
            PEARL_SLOT = p.getSpawnitems().getInt("enderpearl.slot");
            PEARL_ITEM = new ItemStack(PEARL_MATERIAL);
            ItemMeta meta = PEARL_ITEM.getItemMeta();
            meta.setDisplayName(PEARL_TITLE);
            meta.setLore(PEARL_LORE);
            PEARL_ITEM.setItemMeta(meta);
        }
        GAMES_ENABLED = p.getSpawnitems().getBoolean("game-selector.enabled");
        if (GAMES_ENABLED) {
            String GAMES_MATERIAL = p.getSpawnitems().getString("game-selector.material");
            GAMES_SLOT = p.getSpawnitems().getInt("game-selector.slot");
            String GAMES_TITLE = ChatColor.translateAlternateColorCodes('&', p.getSpawnitems().getString("game-selector.title"));
            GAMES_LORE.clear();
            for (String s : p.getSpawnitems().getStringList("game-selector.lore"))
                GAMES_LORE.add(ChatColor.translateAlternateColorCodes('&', s));
            int GAMES_INV_ROWS = p.getSpawnitems().getInt("game-selector.inventory-rows");
            GAMES_ITEM = new ItemStack(Material.getMaterial(GAMES_MATERIAL));
            ItemMeta metaGames = GAMES_ITEM.getItemMeta();
            metaGames.setDisplayName(GAMES_TITLE);
            metaGames.setLore(GAMES_LORE);
            GAMES_ITEM.setItemMeta(metaGames);
            gamesInv = Bukkit.createInventory(null, 9 * GAMES_INV_ROWS, GAMES_TITLE);
            GAMES_INV_STACKS.clear();
            for (String item : p.getSpawnitems().getStringList("game-selector.inventory")) {
                String[] splitted = item.split(";");
                Material material = Material.getMaterial(splitted[0]);
                int slot = Integer.parseInt(splitted[1]);
                String displayName = ChatColor.translateAlternateColorCodes('&', splitted[2]);
                boolean loreEnabled = !splitted[3].equalsIgnoreCase("false") && !splitted[3].equalsIgnoreCase("none");
                List<String> lore = new ArrayList<>();
                for (String s : splitted[3].split("%")) lore.add(ChatColor.translateAlternateColorCodes('&', s));
                short durability = Short.parseShort(splitted[4]);
                String command = splitted[5];
                ItemStack finnalyDesiredItem = new ItemStack(material);
                ItemMeta meta = finnalyDesiredItem.getItemMeta();
                meta.setDisplayName(displayName);
                if (loreEnabled) meta.setLore(lore);
                finnalyDesiredItem.setItemMeta(meta);
                finnalyDesiredItem.setDurability(durability);
                gamesInv.setItem(slot, finnalyDesiredItem);
                boolean commandEnabled = !splitted[5].equalsIgnoreCase("false") && !splitted[5].equalsIgnoreCase("none");
                if (commandEnabled) GAMES_INV_STACKS.put(finnalyDesiredItem, command);
            }
        }
        LOBBIES_ENABLED = p.getSpawnitems().getBoolean("lobbies.enabled");
        if (LOBBIES_ENABLED) {
            String LOBBIES_MATERIAL = p.getSpawnitems().getString("lobbies.material");
            LOBBIES_SLOT = p.getSpawnitems().getInt("lobbies.slot");
            String LOBBIES_TITLE = ChatColor.translateAlternateColorCodes('&', p.getSpawnitems().getString("lobbies.title"));
            LOBBIES_LORE.clear();
            for (String s : p.getSpawnitems().getStringList("lobbies.lore"))
                LOBBIES_LORE.add(ChatColor.translateAlternateColorCodes('&', s));
            int LOBBIES_INV_ROWS = p.getSpawnitems().getInt("lobbies.inventory-rows");
            LOBBIES_ITEM = new ItemStack(Material.getMaterial(LOBBIES_MATERIAL));
            ItemMeta metaGames = LOBBIES_ITEM.getItemMeta();
            metaGames.setDisplayName(LOBBIES_TITLE);
            metaGames.setLore(LOBBIES_LORE);
            LOBBIES_ITEM.setItemMeta(metaGames);
            lobbiesInv = Bukkit.createInventory(null, 9 * LOBBIES_INV_ROWS, LOBBIES_TITLE);
            LOBBIES_INV_STACKS.clear();
            for (String item : p.getSpawnitems().getStringList("lobbies.inventory")) {
                String[] splitted = item.split(";");
                Material material = Material.getMaterial(splitted[0]);
                int slot = Integer.parseInt(splitted[1]);
                String displayName = ChatColor.translateAlternateColorCodes('&', splitted[2]);
                boolean loreEnabled = !splitted[3].equalsIgnoreCase("false") && !splitted[3].equalsIgnoreCase("none");
                List<String> lore = new ArrayList<>();
                for (String s : splitted[3].split("%")) lore.add(ChatColor.translateAlternateColorCodes('&', s));
                short durability = Short.parseShort(splitted[4]);
                String command = splitted[5];
                ItemStack finnalyDesiredItem = new ItemStack(material);
                ItemMeta meta = finnalyDesiredItem.getItemMeta();
                meta.setDisplayName(displayName);
                if (loreEnabled) meta.setLore(lore);
                finnalyDesiredItem.setItemMeta(meta);
                finnalyDesiredItem.setDurability(durability);
                lobbiesInv.setItem(slot, finnalyDesiredItem);
                boolean commandEnabled = !splitted[5].equalsIgnoreCase("false") && !splitted[5].equalsIgnoreCase("none");
                if (commandEnabled) LOBBIES_INV_STACKS.put(finnalyDesiredItem, command);
            }
        }
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        event.getPlayer().getInventory().clear();
        event.getPlayer().getInventory().setArmorContents(new ItemStack[] {null, null, null, null});
        if (GAMES_ENABLED) event.getPlayer().getInventory().setItem(GAMES_SLOT, GAMES_ITEM);
        if (PEARL_ENABLED) event.getPlayer().getInventory().setItem(PEARL_SLOT, PEARL_ITEM);
        if (LOBBIES_ENABLED) event.getPlayer().getInventory().setItem(LOBBIES_SLOT, LOBBIES_ITEM);
        if (PROFILE_ENABLED) event.getPlayer().getInventory().setItem(PROFILE_SLOT, PROFILE_ITEM);
        if (CUSTOM_ENABLED) event.getPlayer().getInventory().setItem(CUSTOM_SLOT, CUSTOM_ITEM);
        event.getPlayer().updateInventory();
        if (DystellarCore.JOIN_TELEPORT && DystellarCore.SPAWN_LOCATION != null) event.getPlayer().teleport(DystellarCore.SPAWN_LOCATION);
        event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.LEVEL_UP, 1.2f, 1.2f);
    }

    @EventHandler
    public void playerMove(PlayerMoveEvent event) {
        if (DystellarCore.VOID_TELEPORT && DystellarCore.SPAWN_LOCATION != null && event.getTo().getY() <= 0.0) event.getPlayer().teleport(DystellarCore.SPAWN_LOCATION);
    }

    @EventHandler
    public void use(PlayerInteractEvent event) {
        if (!event.hasItem()) return;
        if (event.getItem().equals(GAMES_ITEM) && GAMES_ENABLED) {
            event.setCancelled(true);
            event.getPlayer().openInventory(gamesInv);
        } else if (event.getItem().equals(PEARL_ITEM) && PEARL_ENABLED) {
            Bukkit.getScheduler().runTaskLater(DystellarCore.getInstance(), () -> {
                event.getPlayer().getInventory().setItem(PEARL_SLOT, PEARL_ITEM);
                event.getPlayer().updateInventory();
            }, 5L);
        } else if (event.getItem().equals(LOBBIES_ITEM) && LOBBIES_ENABLED) {
            event.setCancelled(true);
            event.getPlayer().openInventory(lobbiesInv);
        } else if (event.getItem().equals(PROFILE_ITEM) && PROFILE_ENABLED) {
            event.setCancelled(true);
            Bukkit.dispatchCommand(event.getPlayer(), PROFILE_COMMAND);
        } else if (event.getItem().equals(CUSTOM_ITEM) && CUSTOM_ENABLED) {
            event.setCancelled(true);
            Bukkit.dispatchCommand(event.getPlayer(), CUSTOM_COMMAND);
        }
    }

    @EventHandler
    public void click(InventoryClickEvent event) {
        if ((gamesInv != null && gamesInv.equals(event.getClickedInventory())) || (lobbiesInv != null && lobbiesInv.equals(event.getClickedInventory()))) event.setCancelled(true);
        if (event.getCurrentItem() != null && GAMES_INV_STACKS.containsKey(event.getCurrentItem())) Bukkit.dispatchCommand((CommandSender) event.getWhoClicked(), GAMES_INV_STACKS.get(event.getCurrentItem()));
        if (event.getCurrentItem() != null && LOBBIES_INV_STACKS.containsKey(event.getCurrentItem())) Bukkit.dispatchCommand((CommandSender) event.getWhoClicked(), LOBBIES_INV_STACKS.get(event.getCurrentItem()));
    }

    @EventHandler
    public void drag(InventoryDragEvent event) {
        if ((gamesInv != null && gamesInv.equals(event.getInventory())) || (lobbiesInv != null && lobbiesInv.equals(event.getInventory()))) event.setCancelled(true);
    }

    @EventHandler
    public void weather(WeatherChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        String reason = event.getReason();
        event.setReason(DystellarCore.KICK_MESSAGE.replaceAll("<reason>", reason));
    }
}
