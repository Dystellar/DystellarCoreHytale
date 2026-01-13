package gg.dystellar.core.arenasapi;

import gg.dystellar.core.utils.Operation;
import gg.dystellar.core.utils.Scheduler;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.concurrent.atomic.AtomicInteger;

public class OfflineRegion {

    private final OfflineBlock[][][] blockData;
    private final double[] center;

    public OfflineRegion(OfflineBlock[][][] data, int xLenght, int yLenght, int zLenght) {
        this.blockData = data;
        this.center = new double[] {(double) xLenght / 2.0, (double) yLenght / 2.0, (double) zLenght / 2.0};
    }

    /**
     * Recommended to do this asynchronously.
     * @param world The world
     * @param vector Location vector (this location will be assumed as center)
     */
    public Operation paste(World world, Vector vector) {
        AtomicInteger realPositionX = new AtomicInteger((int) (vector.getBlockX() - center[0]));
        AtomicInteger realPositionY = new AtomicInteger((int) (vector.getBlockY() - center[1]));
        AtomicInteger realPotitionZ = new AtomicInteger((int) (vector.getBlockZ() - center[2]));
        int xMax = (int) (vector.getBlockX() + center[0]);
        int yMax = (int) (vector.getBlockY() + center[1]);
        int zMax = (int) (vector.getBlockZ() + center[2]);
        Operation operation = new Operation(this, realPositionX, realPositionY, realPotitionZ);

        Scheduler.splitTridimensionalArrayIteration(blockData, (object, isFinished) -> {
            if (isFinished.get()) return;

            object.paste(world, realPositionX.get(), realPositionY.get(), realPotitionZ.get());
            realPotitionZ.getAndIncrement();
            if (!(realPotitionZ.get() < zMax)) {
                realPotitionZ.set(vector.getBlockZ());
                realPositionY.getAndIncrement();
                if (!(realPositionY.get() < yMax)) {
                    realPositionY.set(vector.getBlockY());
                    realPositionX.getAndIncrement();
                    if (!(realPositionX.get() < xMax)) {
                        isFinished.set(true);
                    }
                }
            }
        }, 50);
        return operation;
    }

    public OfflineBlock[][][] getBlockData() {
        return blockData;
    }

    public double[] getCenter() {
        return center;
    }
}
