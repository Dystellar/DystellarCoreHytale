package gg.dystellar.core.utils;

import net.zylesh.dystellarcore.DystellarCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This was important for minecraft.
 * The server would send a plugin message to the proxy through a player's connection.
 * But the problem is, when there is no player online, the message just gets lost.
 * So what this API does is cache the plugin messages if no player is online,
 * and send them all when a player joins.
 *
 * Not sure why I don't use a GenericRunner here instead of this Task interface.
 */
public class PluginMessageScheduler {

    private static final Set<PluginMessageScheduler.Task> tasks = Collections.synchronizedSet(new HashSet<>());

    public static void scheduleTask(PluginMessageScheduler.Task task) {
        if (Bukkit.getOnlinePlayers().isEmpty())
            tasks.add(task);
        else {
            Iterator<? extends Player> iterator = Bukkit.getOnlinePlayers().iterator();
            Player player = iterator.next();
            task.run(player);
        }
    }

    public static void playerJoined(Player p) {
        tasks.forEach(task -> task.run(p));
    }

    public interface Task {
        void run(Player player);
    }
}
