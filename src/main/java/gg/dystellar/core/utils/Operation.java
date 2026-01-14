package gg.dystellar.core.utils;

import net.zylesh.dystellarcore.arenasapi.OfflineRegion;

import java.util.concurrent.atomic.AtomicInteger;

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
        this.totalOperations = this.calculateTotalValue(region.getBlockData().length, region.getBlockData()[0].length, region.getBlockData()[0][0].length);
    }

    private int calculateTotalValue(int x, int y, int z) {
        int temp = y * x;
        return temp * z;
    }

    public int getProcessPercent() {
        return (this.calculateTotalValue(xPos.get(), yPos.get(), zPos.get()) / totalOperations) * 100;
    }

    public boolean isFinished() {
        return !(calculateTotalValue(xPos.get(), yPos.get(), zPos.get()) < totalOperations);
    }
}
