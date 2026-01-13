package net.zylesh.dystellarcore.utils;

import net.zylesh.dystellarcore.DystellarCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
        if (!tasks.isEmpty()) {
            tasks.forEach(task -> task.run(p));
        }
    }

    public interface Task {
        void run(Player player);
    }
}
