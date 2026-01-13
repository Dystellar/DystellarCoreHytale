package gg.dystellar.core.core.inbox;

public interface Claimable extends Sendable {

    boolean isClaimed();

    boolean claim();
}
