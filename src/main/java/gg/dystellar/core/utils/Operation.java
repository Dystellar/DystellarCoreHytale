package gg.dystellar.core.utils;

import java.util.concurrent.atomic.AtomicInteger;

import gg.dystellar.core.arenasapi.OfflineRegion;

/**
 * A helper utility that computes stuff related to ArenasAPI? Don't really remember.
 */
public class Operation {

    private final AtomicInteger xPos;
    private final AtomicInteger yPos;
    private final AtomicInteger zPos;
    private final int totalOperations;

    public Operation(OfflineRegion region, AtomicInteger xPos, AtomicInteger yPos, AtomicInteger zPos) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.zPos = zPos;
        this.totalOperations = region.getBlockData().length * region.getBlockData()[0].length * region.getBlockData()[0][0].length;
    }

    public int getProcessPercent() {
        return ((xPos.get() * yPos.get() * zPos.get()) / totalOperations) * 100;
    }

    public boolean isFinished() {
        return !((xPos.get() * yPos.get() * zPos.get()) < totalOperations);
    }
}
