package gg.dystellar.core.common.punishments;

import java.time.LocalDateTime;

public class Ban extends Punishment {

    public static final byte SERIALIZATION_ID = 0;

    private boolean isAlsoIP = false;

    public Ban(LocalDateTime expirationDate, String reason) {
        super(expirationDate, reason, false, false, false, false);
    }

    public Ban(int id, LocalDateTime creationDate, LocalDateTime expirationDate, String reason, boolean isAlsoIP) {
        super(id, creationDate, expirationDate, reason);
        this.isAlsoIP = isAlsoIP;
    }

    @Override
    public void onPunishment(User user) {
        super.onPunishment(user);
        if (user == null) return;
        Player p = Bukkit.getPlayer(user.getUUID());
        if (p != null) {
            if (!DystellarCore.ALLOW_BANNED_PLAYERS) {
                p.kickPlayer(ChatColor.translateAlternateColorCodes('&', getMessage().replace("<reason>", getReason()).replace("<time>", Utils.getTimeFormat(getExpirationDate()))));
            } else {
                p.sendMessage(" ");
                p.sendMessage(ChatColor.RED + "You have been banned.");
                p.sendMessage(" ");
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage().replaceAll("<reason>", getReason()).replaceAll("<time>", Utils.getTimeFormat(getExpirationDate()))));
            }
        }
    }

    public boolean isAlsoIP() {
        return isAlsoIP;
    }

    public void setAlsoIP(boolean alsoIP) {
        isAlsoIP = alsoIP;
    }

    @Override
    public byte getSerializedId() {
        return SERIALIZATION_ID;
    }

    @Override
    public String getMessage() {
        return DystellarCore.BAN_MESSAGE;
    }

    @Override
    public int getPriorityScale() {
        return 1;
    }
}
