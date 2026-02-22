package gg.dystellar.core.listeners;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GeneralListeners implements Listener {

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
}
