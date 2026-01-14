package gg.dystellar.core.utils;

import net.zylesh.dystellarcore.DystellarCore;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Scheduler utils for computing expensive tasks, allowing to perform them safely without destroying the performance
 */
public class Scheduler {
    /**
     * Perform a heavy task to an iterable (or collection).
     * This code will split the tasks and will slowly execute them in the main thread.
     * It's useful for operations that are required to run on the main thread, like world manipulation and stuff.
     * What it does, instead of running all the tasks in one go, as that would lag the server until the tasks are finished,
     * it splits the tasks into various ticks, basically not blocking the thread and synchronously run them,
     * while leaving enough computer power to process the remaining internal stuff that needs to run in a tick.
     *
     * Could be better designed, but I didn't know better at the time.
     */
    public static <T> void splitIteration(Iterable<T> collection, GenericRunnable<T> task, int maxIterationsPerTick) {
        Iterator<T> iterator = collection.iterator();
        AtomicReference<BukkitTask> task1 = new AtomicReference<>();
        task1.set(Bukkit.getScheduler().runTaskTimer(DystellarCore.getInstance(), () -> {
            if (iterator.hasNext())
                next(iterator, task, maxIterationsPerTick);
            else
                cancelTask(task1.get());
        }, 0L, 1L));
    }

    /**
     * Painful
     */
    private static void cancelTask(BukkitTask task) {
        if (task == null)
            return;
        task.cancel();
    }

    /**
     * Helper function
     */
    private static <T> void next(Iterator<T> iterator, GenericRunnable<T> task, int maxIterationsPerTick) {
        int index = 0;
        while (iterator.hasNext() && index < maxIterationsPerTick) {
            index++;
            T next = iterator.next();
            task.run(next);
        }
    }

    public static <T> void splitTridimensionalArrayIteration(T[][][] array, GenericRunnableWithAutoCancel<T> task, int maxOperationsPerTick) {
        AtomicInteger i = new AtomicInteger();
        AtomicInteger j = new AtomicInteger();
        AtomicInteger k = new AtomicInteger();
        AtomicBoolean isFinished = new AtomicBoolean();
        AtomicReference<BukkitTask> task1 = new AtomicReference<>();
        Bukkit.getScheduler().runTaskTimer(DystellarCore.getInstance(), () -> {
            if (isFinished.get()) {
                cancelTask(task1.get());
            } else {
                next(i, j, k, isFinished, array, task, maxOperationsPerTick);
            }
        }, 1L, 1L);
    }

    private static <T> void next(AtomicInteger i, AtomicInteger j, AtomicInteger k, AtomicBoolean isFinished, T[][][] array, GenericRunnableWithAutoCancel<T> task, int maxOperationsPerTick) {
        int index = 0;
        while (index < maxOperationsPerTick) {
            T obj = array[i.get()][j.get()][k.get()];
            task.run(obj, isFinished);
            k.getAndIncrement();
            if (!(array[i.get()][j.get()].length < k.get())) {
                k.set(0);
                j.getAndIncrement();
                if (!(array[i.get()].length < j.get())) {
                    j.set(0);
                    i.getAndIncrement();
                    if (!(array.length < i.get())) {
                        isFinished.set(true);
                        break;
                    }
                }
            }
            index++;
        }
    }
}
