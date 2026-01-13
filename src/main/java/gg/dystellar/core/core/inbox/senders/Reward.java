package gg.dystellar.core.core.inbox.senders;

import net.zylesh.dystellarcore.core.inbox.Claimable;
import net.zylesh.dystellarcore.core.inbox.Inbox;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class Reward extends Message implements Claimable {

    protected ItemStack readIcon;
    protected final String title;
    protected boolean isClaimed;

    protected Reward(Inbox inbox, String title, String from, String... messageLines) {
        super(inbox, from, messageLines);
        this.title = title;
    }

    protected Reward(Inbox inbox, int id, String from, String[] messageLines, LocalDateTime submissionDate, boolean isDeleted, String title, boolean isClaimed) {
        super(inbox, id, from, messageLines, submissionDate, isDeleted);
        this.title = title;
        this.isClaimed = isClaimed;
    }

    @Override
    public abstract void initializeIcons();

    public String getTitle() {
        return title;
    }

    @Override
    public ItemStack getReadIcon() {
        return readIcon;
    }

    @Override
    public byte getSerialID() {
        return -1;
    }

    @Override
    public abstract void onLeftClick();

    @Override
    public abstract void onRightClick();

    @Override
    public abstract boolean claim();

    @Override
    public boolean isClaimed() {
        return isClaimed;
    }

	@Override
	public abstract Object[] encode(UUID target);
}
