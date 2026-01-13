package net.zylesh.dystellarcore.core.punishments;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.User;
import net.zylesh.dystellarcore.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;

public class Blacklist extends Punishment {

    public static final byte SERIALIZATION_ID = 1;

    public Blacklist(String reason) {
        super(null, reason);
    }

    public Blacklist(int id, LocalDateTime creationDate, String reason) {
        super(id, creationDate, null, reason);
    }

    @Override
    public void onPunishment(User user) {
        super.onPunishment(user);
        if (user == null) return;
        Player p = Bukkit.getPlayer(user.getUUID());
        if (p != null) {
            p.kickPlayer(ChatColor.translateAlternateColorCodes('&', getMessage().replaceAll("<reason>", getReason()).replaceAll("<time>", Utils.getTimeFormat(getExpirationDate()))));
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

    @Override
    public boolean allowUnranked() {
        return false;
    }

    @Override
    public boolean allowJoinMinigames() {
        return false;
    }

    @Override
    public String getMessage() {
        return DystellarCore.BLACKLIST_MESSAGE;
    }

    @Override
    public int getPriorityScale() {
        return 0;
    }

    @Override
    public byte getSerializedId() {
        return SERIALIZATION_ID;
    }
}
