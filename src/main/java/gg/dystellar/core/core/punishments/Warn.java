package net.zylesh.dystellarcore.core.punishments;

import net.zylesh.dystellarcore.commands.Punish;
import net.zylesh.dystellarcore.core.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;

public class Warn extends Punishment {

    public static final byte SERIALIZATION_ID = 4;

    public Warn(LocalDateTime expirationDate, String reason) {
        super(expirationDate, reason);
    }

    public Warn(int id, LocalDateTime creationDate, LocalDateTime expirationDate, String reason) {
        super(id, creationDate, expirationDate, reason);
    }

    @Override
    public void onPunishment(User user) {
        super.onPunishment(user);
        if (user == null) return;
        Player p = Bukkit.getPlayer(user.getUUID());
        if (p != null) {
            p.sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------------------");
            p.sendMessage(" ");
            p.sendMessage(ChatColor.RED + "You have been warned!");
            p.sendMessage(ChatColor.RED + "Reason: " + ChatColor.WHITE + getReason());
            p.sendMessage(" ");
            p.sendMessage(ChatColor.RED + "Accumulation of several warns will get you banned!");
            p.sendMessage(" ");
            p.sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------------------");
            int warns = (int) user.getPunishments().stream()
                    .filter(punishment -> punishment instanceof Warn)
                    .count();
            try {
                switch (warns) {
                    case 3: user.punish(Punish.BAN_3_WARNS.call()); break;
                    case 5: user.punish(Punish.BAN_5_WARNS.call()); break;
                    case 7: user.punish(Punish.BAN_7_WARNS.call()); break;
                    case 10: user.punish(Punish.BLACKLIST_10_WARNS); break;
                }
            } catch (Exception ignored) {}

        }
    }

    @Override
    public boolean allowChat() {
        return true;
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
        return null;
    }

    @Override
    public int getPriorityScale() {
        return 5;
    }

    @Override
    public byte getSerializedId() {
        return SERIALIZATION_ID;
    }
}
