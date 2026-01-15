package gg.dystellar.core.common.punishments;

import java.time.Duration;
import java.time.LocalDateTime;

public abstract class Punishment implements Comparable<Punishment> {

    private final LocalDateTime creationDate;
    private final LocalDateTime expirationDate;
    private final String reason;
    protected int id = -1;
	private final boolean allowChat;
	private final boolean allowRanked;
	private final boolean allowUnranked;
	private final boolean allowJoinMinigames;

    protected Punishment(final LocalDateTime expirationDate, final String reason, boolean allowChat, boolean allowRanked, boolean allowUnranked, boolean allowJoinMinigames) {
        this.creationDate = LocalDateTime.now();
        this.expirationDate = expirationDate;
        this.reason = reason;

		this.allowChat = allowChat;
		this.allowRanked = allowRanked;
		this.allowUnranked = allowUnranked;
		this.allowJoinMinigames = allowJoinMinigames;
    }

    protected Punishment(int id, LocalDateTime creationDate, LocalDateTime expirationDate, String reason, boolean allowChat, boolean allowRanked, boolean allowUnranked, boolean allowJoinMinigames) {
        this.id = id;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
        this.reason = reason;

		this.allowChat = allowChat;
		this.allowRanked = allowRanked;
		this.allowUnranked = allowUnranked;
		this.allowJoinMinigames = allowJoinMinigames;
    }

    public abstract byte getSerializedId();

    public final boolean allowChat() { return this.allowChat; }

    public final boolean allowRanked() { return this.allowRanked; }

    public final boolean allowUnranked() { return this.allowUnranked; }

    public final boolean allowJoinMinigames() { return this.allowJoinMinigames; }

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
}
