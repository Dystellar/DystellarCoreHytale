package gg.dystellar.core.common.inbox;

import org.bukkit.inventory.ItemStack;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Interface for basic email functionality
 */
public interface Sendable extends Comparable<Sendable> {

	/**
	 * Internal method
	 */
    void initializeIcons();

    ItemStack getUnreadIcon();

    ItemStack getReadIcon();

    void onLeftClick();

    void onRightClick();

    LocalDateTime getSubmissionDate();

    boolean isDeleted();

    byte getSerialID();

    String getFrom();

    int getId();

    Sendable clone(Inbox inbox);

	/**
	 * Encode the sender to make it ready to be sent to a player through plugin messages.
	 * @param target The player who will receive this sendable.
	 */
	Object[] encode(UUID target);
}
