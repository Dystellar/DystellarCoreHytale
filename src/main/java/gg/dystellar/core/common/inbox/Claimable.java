package gg.dystellar.core.common.inbox;

public interface Claimable extends Sendable {
    boolean isClaimed();

    boolean claim();
}
