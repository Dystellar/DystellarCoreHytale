package gg.dystellar.core.listeners;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GeneralListeners implements Listener {

    private final Set<UUID> awaitingPlayers = new HashSet<>();

    public GeneralListeners() {
        Bukkit.getPluginManager().registerEvents(this, DystellarCore.getInstance());
    }

    @EventHandler
    public void command(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().startsWith("/op")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void command(ServerCommandEvent event) {
        if (event.getCommand().startsWith("op")) {
            event.setCommand("whatever");
        }
    }

    @EventHandler
    public void remoteCommand(RemoteServerCommandEvent event) {
        if (event.getCommand().startsWith("op")) {
            event.setCommand("whatever");
        }
    }

    @EventHandler
    public void weatherChange(WeatherChangeEvent event) {
        if (ConfValues.PREVENT_WEATHER && event.toWeatherState()) event.setCancelled(true);
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		
		// Send plugin messages in case there's any scheduled.
        PluginMessageScheduler.playerJoined(p);

		// Register player on the proxy, so the player is allowed to join other sub-servers freely, if proxy does not respond, the player gets kicked for security as that would mean the player joined from unofficial sources.
        awaitingPlayers.add(p.getUniqueId());
        Bukkit.getScheduler().runTaskLater(DystellarCore.getInstance(), () -> Utils.sendPluginMessage(p, Types.REGISTER), 15L);
        Bukkit.getScheduler().runTaskLater(DystellarCore.getInstance(), () -> {
            if (awaitingPlayers.contains(event.getPlayer().getUniqueId())) {
                p.kickPlayer(ChatColor.RED + "You are not allowed to join this server. Please make sure you are following official procedures.");
                awaitingPlayers.remove(event.getPlayer().getUniqueId());
            }
        }, 35L);
    }
}
