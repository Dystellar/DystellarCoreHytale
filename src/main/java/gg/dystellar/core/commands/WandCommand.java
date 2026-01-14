package gg.dystellar.core.commands;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.arenasapi.BlockGeometrySchemaUtilRepresentation;
import net.zylesh.dystellarcore.arenasapi.BlocksSchemes;
import net.zylesh.dystellarcore.arenasapi.OfflineRegion;
import net.zylesh.dystellarcore.core.Msgs;
import net.zylesh.dystellarcore.utils.Operation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Command to manage the region API, they are bugged.
 */
public class WandCommand implements CommandExecutor, Listener {

    private final ItemStack WAND = new ItemStack(Material.BLAZE_ROD);

    public WandCommand() {
        Bukkit.getPluginCommand("wand").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, DystellarCore.getInstance());
        Bukkit.getPluginCommand("paste").setExecutor(new PasteCommand());
        Bukkit.getPluginCommand("save").setExecutor(new SaveCommand());
        Bukkit.getPluginCommand("load").setExecutor(new LoadCommand());
        ItemMeta meta = WAND.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_AQUA + "Wand");
        meta.setLore(List.of(ChatColor.GRAY + "Left Click to select position 1.", ChatColor.GRAY + "Right Click to select position 2"));
        WAND.setItemMeta(meta);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Msgs.ERROR_NOT_A_PLAYER);
            return true;
        }
        Player p = (Player) commandSender;
        if (p.getItemInHand() != null && p.getItemInHand().getType() != Material.AIR) {
            p.sendMessage(ChatColor.RED + "Your hand must be empty.");
            return true;
        }
        p.setItemInHand(WAND);
        p.updateInventory();
        return true;
    }

    public final Map<UUID, Location[]> selections = new HashMap<>();

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if (!Objects.equals(WAND, event.getItem())) return;
        if (!selections.containsKey(event.getPlayer().getUniqueId())) selections.put(event.getPlayer().getUniqueId(), new Location[]{null, null});
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            Location loc = event.getClickedBlock().getLocation();
            selections.get(event.getPlayer().getUniqueId())[0] = loc;
            event.getPlayer().sendMessage(ChatColor.DARK_AQUA + "Position 1 set to: " + ChatColor.GRAY + "(" + loc.getWorld().getName() + "; " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")");
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            Location loc = event.getClickedBlock().getLocation();
            selections.get(event.getPlayer().getUniqueId())[1] = loc;
            event.getPlayer().sendMessage(ChatColor.DARK_AQUA + "Position 2 set to: " + ChatColor.GRAY + "(" + loc.getWorld().getName() + "; " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")");
        }
    }

    public final Map<UUID, OfflineRegion> loaded = new ConcurrentHashMap<>();
    private final Map<UUID, Map.Entry<Operation, ScheduledFuture<?>>> ongoingOperations = new ConcurrentHashMap<>();

    public class PasteCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
            if (!(commandSender instanceof Player)) return true;
            Player p = (Player) commandSender;
            if (!loaded.containsKey(p.getUniqueId())) {
                p.sendMessage(ChatColor.RED + "Load a region first with /load <filename>.");
                return true;
            }
            p.sendMessage(ChatColor.DARK_GREEN + "Initializing operation's required schedulers...");
            AtomicBoolean finishSoon = new AtomicBoolean();
            ScheduledFuture<?> future = DystellarCore.getAsyncManager().scheduleAtFixedRate(() -> {
                if (!ongoingOperations.containsKey(p.getUniqueId())) {
                    OfflineRegion region = loaded.get(p.getUniqueId());
                    Operation operation = region.paste(p.getWorld(), new Vector(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ()));
                    ongoingOperations.put(p.getUniqueId(), new AbstractMap.SimpleEntry<>(operation, null));
                }
                if (ongoingOperations.get(p.getUniqueId()).getValue() != null && finishSoon.get()) {
                    p.sendMessage(ChatColor.GREEN + "Operation completed successfully!");
                    ongoingOperations.get(p.getUniqueId()).getValue().cancel(true);
                    return;
                }
                Operation currentOperation = ongoingOperations.get(p.getUniqueId()).getKey();
                p.sendMessage(ChatColor.RED + "Progress: " + ChatColor.DARK_AQUA + currentOperation.getProcessPercent() + "%");
                if (currentOperation.isFinished()) {
                    finishSoon.set(true);
                }
            }, 10L, 700L, TimeUnit.MILLISECONDS);
            DystellarCore.getAsyncManager().schedule(() -> {
                ongoingOperations.get(p.getUniqueId()).setValue(future);
            }, 400L, TimeUnit.MILLISECONDS);
            return true;
        }
    }

    public class SaveCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
            if (!(commandSender instanceof Player)) return true;
            Player p = (Player) commandSender;
            if (!selections.containsKey(p.getUniqueId()) || selections.get(p.getUniqueId())[0] == null || selections.get(p.getUniqueId())[1] == null) {
                p.sendMessage(ChatColor.RED + "Make a selection first.");
                return true;
            }
            if (strings.length < 1) {
                p.sendMessage(ChatColor.RED + "Usage: /save <filename>");
                return true;
            }
            Location pos1 = selections.get(p.getUniqueId())[0];
            Location pos2 = selections.get(p.getUniqueId())[1];
            DystellarCore.getAsyncManager().submit(() -> {
                String filename = strings[0].endsWith(".rscheme") ? strings[0] : strings[0] + ".rscheme";
                BlockGeometrySchemaUtilRepresentation representation = new BlockGeometrySchemaUtilRepresentation(p.getWorld(), new Vector(pos1.getBlockX(), pos1.getBlockY(), pos1.getBlockZ()), new Vector(pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ()));
                File path = new File(DystellarCore.getInstance().getDataFolder() + File.separator + "region_schemes");
                if (path.exists()) {
                    p.sendMessage(ChatColor.RED + "This region scheme already exists!");
                    return;
                }
                path.mkdirs();
                File path2 = new File(path, filename);
                p.sendMessage(ChatColor.DARK_GREEN + "Saving...");
                try {
                    BlocksSchemes.save(representation, path2);
                    p.sendMessage(ChatColor.GREEN + "Region Scheme successfully saved!");
                } catch (IOException e) {
                    p.sendMessage(ChatColor.RED + "There was an error trying to save this region scheme. Check logs.");
                    e.printStackTrace();
                }
            });
            return true;
        }
    }

    public class LoadCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
            if (!(commandSender instanceof Player)) return true;
            Player p = (Player) commandSender;
            if (strings.length < 1) {
                p.sendMessage(ChatColor.RED + "Usage: /load <filename>");
                return true;
            }
            DystellarCore.getAsyncManager().submit(() -> {
                String filename = strings[0].endsWith(".rscheme") ? strings[0] : strings[0] + ".rscheme";
                File path = new File(DystellarCore.getInstance().getDataFolder() + File.separator + "region_schemes", filename);
                p.sendMessage(ChatColor.DARK_GREEN + "Loading...");
                if (!path.exists()) {
                    p.sendMessage(ChatColor.RED + "This region scheme does not exist!");
                    return;
                }
                try {
                    DataInputStream input = new DataInputStream(new FileInputStream(path));
                    OfflineRegion region = new BlockGeometrySchemaUtilRepresentation().loadFromFile(input, true);
                    loaded.put(p.getUniqueId(), region);
                    p.sendMessage(ChatColor.GREEN + "Load successful! Use /paste to paste.");
                } catch (IOException e) {
                    p.sendMessage(ChatColor.RED + "Error trying to load this region scheme. Check logs.");
                    e.printStackTrace();
                }
            });
            return true;
        }
    }
}
