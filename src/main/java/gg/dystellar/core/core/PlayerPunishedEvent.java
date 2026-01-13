package gg.dystellar.core.core;

import net.zylesh.dystellarcore.core.punishments.Punishment;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

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
