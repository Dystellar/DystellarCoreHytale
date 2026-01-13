package net.zylesh.dystellarcore.core.punishments;

import net.zylesh.dystellarcore.core.PlayerPunishedEvent;
import net.zylesh.dystellarcore.core.User;
import org.bukkit.Bukkit;

import java.time.Duration;
import java.time.LocalDateTime;

public abstract class Punishment implements Comparable<Punishment> {

    private final LocalDateTime creationDate;
    private final LocalDateTime expirationDate;
    private final String reason;
    protected final int id;

    protected Punishment(LocalDateTime expirationDate, String reason) {
        this.creationDate = LocalDateTime.now();
        this.expirationDate = expirationDate;
        this.reason = reason;
        this.id = (int) (Math.random() * (double) 31 * (double) 1000000 / (double) 3);
    }

    protected Punishment(int id, LocalDateTime creationDate, LocalDateTime expirationDate, String reason) {
        this.id = id;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
        this.reason = reason;
    }

    public abstract byte getSerializedId();

    public void onPunishment(User user) {
        Bukkit.getPluginManager().callEvent(new PlayerPunishedEvent(user, this));
    }

    public abstract boolean allowChat();

    public abstract boolean allowRanked();

    public abstract boolean allowUnranked();

    public abstract boolean allowJoinMinigames();

    public abstract String getMessage();

    public String getReason() {
        return reason;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public abstract int getPriorityScale();

    @Override
    public int compareTo(Punishment o) {
        if (getPriorityScale() != o.getPriorityScale()) {
            return Integer.compare(getPriorityScale(), o.getPriorityScale());
        }
        if (expirationDate == null && o.expirationDate != null) {
            return -1;
        } else if (o.expirationDate == null && expirationDate != null) {
            return 1;
        } else if (expirationDate == null) {
            return 0;
        }
        long time = Duration.between(LocalDateTime.now(), expirationDate).getSeconds();
        long otime = Duration.between(LocalDateTime.now(), o.expirationDate).getSeconds();
        if (time > otime) {
            return -1;
        } else if (otime > time) {
            return 1;
        }
        return 0;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
