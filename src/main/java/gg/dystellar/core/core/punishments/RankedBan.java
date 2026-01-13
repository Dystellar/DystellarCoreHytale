package gg.dystellar.core.core.punishments;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.User;

import java.time.LocalDateTime;

public class RankedBan extends Punishment {

    public static final byte SERIALIZATION_ID = 3;

    public RankedBan(LocalDateTime expirationDate, String reason) {
        super(expirationDate, reason);
    }

    public RankedBan(int id, LocalDateTime creationDate, LocalDateTime expirationDate, String reason) {
        super(id, creationDate, expirationDate, reason);
    }

    @Override
    public void onPunishment(User user) {
        super.onPunishment(user);
    }

    @Override
    public boolean allowChat() {
        return true;
    }

    @Override
    public boolean allowRanked() {
        return false;
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
        return DystellarCore.RANKED_BAN_MESSAGE;
    }

    @Override
    public int getPriorityScale() {
        return 4;
    }

    @Override
    public byte getSerializedId() {
        return SERIALIZATION_ID;
    }
}
