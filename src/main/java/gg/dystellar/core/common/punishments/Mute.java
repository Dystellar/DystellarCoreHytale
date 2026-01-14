package gg.dystellar.core.common.punishments;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.User;
import net.zylesh.dystellarcore.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;

public class Mute extends Punishment {

    public static final byte SERIALIZATION_ID = 2;

    public Mute(LocalDateTime expirationDate, String reason) {
        super(expirationDate, reason);
    }

    public Mute(int id, LocalDateTime creationDate, LocalDateTime expirationDate, String reason) {
        super(id, creationDate, expirationDate, reason);
    }

    @Override
    public void onPunishment(User user) {
        super.onPunishment(user);
        if (user == null) return;
        Player p = Bukkit.getPlayer(user.getUUID());
        if (p != null) {
            p.sendMessage(ChatColor.RED + "You have been muted:");
            p.sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------------------");
            p.sendMessage(" ");
            p.sendMessage(ChatColor.RED + "Reason: " + ChatColor.WHITE + getReason());
            p.sendMessage(ChatColor.RED + "Time: " + ChatColor.WHITE + Utils.getTimeFormat(getExpirationDate()));
            p.sendMessage(" ");
            p.sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------------------");
        }
    }

    @Override
    public boolean allowChat() {
        return false;
    }

    @Override
    public boolean allowRanked() {
        return true;
    }

    @Override
    public boolean allowUnranked() {
        return true;
    }

    @Override
    public boolean allowJoinMinigames() {
        return true;
    }

    @Override
    public String getMessage() {
        return DystellarCore.MUTE_MESSAGE;
    }

    @Override
    public int getPriorityScale() {
        return 3;
    }

    @Override
    public byte getSerializedId() {
        return SERIALIZATION_ID;
    }
}
