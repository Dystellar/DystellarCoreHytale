package net.zylesh.dystellarcore.listeners;

import fr.mrmicky.fastboard.FastBoard;
import net.luckperms.api.LuckPermsProvider;
import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Scoreboards implements Listener {

    // TODO handle skywars disabling or enabling scoreboard.

    private static final ScheduledExecutorService threadPool = Executors.newSingleThreadScheduledExecutor();
    private static final Map<UUID, Map.Entry<FastBoard, Future<?>>> scoreboards = new HashMap<>();

    public Scoreboards() {
        Bukkit.getPluginManager().registerEvents(this, DystellarCore.getInstance());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        User user =  User.get(event.getPlayer());
        if (user.isScoreboardEnabled()) {
            FastBoard board = new FastBoard(event.getPlayer());
            board.updateTitle(DystellarCore.SCOREBOARD_TITLE);
            EntityPlayer player = ((CraftPlayer) event.getPlayer()).getHandle();
            Future<?> task = threadPool.scheduleAtFixedRate(() -> {
                List<String> updated = new ArrayList<>();
                for (String s : DystellarCore.SCOREBOARD_LINES) {
                    updated.add(ChatColor.translateAlternateColorCodes('&', s.replaceFirst("<player_name>", event.getPlayer().getName()).replaceFirst("<ping>", String.valueOf(player.ping)).replaceFirst("<rank>", getRank(event.getPlayer())).replaceFirst("<online>", String.valueOf(Bukkit.getOnlinePlayers().size()))));
                }
                board.updateLines(updated);
            }, DystellarCore.REFRESH_RATE_SCORE, DystellarCore.REFRESH_RATE_SCORE, TimeUnit.MILLISECONDS);
            scoreboards.put(event.getPlayer().getUniqueId(), new AbstractMap.SimpleEntry<>(board, task));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        User user = User.get(event.getPlayer());
        if (user.isScoreboardEnabled() && scoreboards.containsKey(event.getPlayer().getUniqueId())) {
            scoreboards.get(event.getPlayer().getUniqueId()).getKey().delete();
            scoreboards.get(event.getPlayer().getUniqueId()).getValue().cancel(true);
            scoreboards.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        User user = User.get(event.getPlayer());
        if (user.isScoreboardEnabled() && scoreboards.containsKey(event.getPlayer().getUniqueId())) {
            scoreboards.get(event.getPlayer().getUniqueId()).getKey().delete();
            scoreboards.get(event.getPlayer().getUniqueId()).getValue().cancel(true);
            scoreboards.remove(event.getPlayer().getUniqueId());
        }
    }

    private static String getRank(Player p) {
        return LuckPermsProvider.get().getUserManager().getUser(p.getUniqueId()).getPrimaryGroup();
    }
}
