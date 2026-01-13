package gg.dystellar.core.core.punishments;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.User;
import net.zylesh.dystellarcore.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;

public class Ban extends Punishment {

    public static final byte SERIALIZATION_ID = 0;

    private boolean isAlsoIP = false;

    public Ban(LocalDateTime expirationDate, String reason) {
        super(expirationDate, reason);
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

    @Override
    public boolean allowChat() {
        return false;
    }

    @Override
    public boolean allowRanked() {
        return false;
    }

    public boolean isAlsoIP() {
        return isAlsoIP;
    }

    public void setAlsoIP(boolean alsoIP) {
        isAlsoIP = alsoIP;
    }

    @Override
    public boolean allowUnranked() {
        return false;
    }

    @Override
    public byte getSerializedId() {
        return SERIALIZATION_ID;
    }

    @Override
    public boolean allowJoinMinigames() {
        return false;
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
