package gg.dystellar.core.common.punishments;

import java.time.LocalDateTime;

public final class Punishment {

    private LocalDateTime creationDate;
    private LocalDateTime expirationDate;
	private String title;
    private String reason;
    private int id;
	private boolean allowChat;
	private boolean allowRanked;
	private boolean allowUnranked;
	private boolean allowJoinMinigames;

    public Punishment(int id, String title, LocalDateTime creationDate, LocalDateTime expirationDate, String reason, boolean allowChat, boolean allowRanked, boolean allowUnranked, boolean allowJoinMinigames) {
        this.id = id;
		this.title = title;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
        this.reason = reason;

		this.allowChat = allowChat;
		this.allowRanked = allowRanked;
		this.allowUnranked = allowUnranked;
		this.allowJoinMinigames = allowJoinMinigames;
    }

	public int getId() { return this.id; }

    public boolean allowChat() { return this.allowChat; }

    public boolean allowRanked() { return this.allowRanked; }

    public boolean allowUnranked() { return this.allowUnranked; }

    public boolean allowJoinMinigames() { return this.allowJoinMinigames; }

    public String getTitle() { return this.title; }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }
}
