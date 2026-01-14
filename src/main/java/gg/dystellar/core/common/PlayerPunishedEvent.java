package gg.dystellar.core.common;

import net.zylesh.dystellarcore.core.punishments.Punishment;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Bukkit event that gets triggered whenever a player receives a punishment of any sort.
 */
public class PlayerPunishedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final User user;
    private final Punishment punishment;

    public PlayerPunishedEvent(User user, Punishment punishment) {
        this.user = user;
        this.punishment = punishment;
    }

    public Punishment getPunishment() {
        return punishment;
    }

    public User getUser() {
        return user;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
