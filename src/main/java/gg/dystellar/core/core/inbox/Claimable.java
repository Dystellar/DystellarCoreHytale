package net.zylesh.dystellarcore.core.inbox;

public interface Claimable extends Sendable {

    boolean isClaimed();

    boolean claim();
}
