package gg.dystellar.core.utils;

import net.zylesh.dystellarcore.DystellarCore;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TimeCounter {

    private volatile int value = 0;
    private int freezedInt = 0;
    private boolean isRunning;
    private ScheduledFuture<?> task;

    public TimeCounter() {}

    private synchronized void add() {
        value++;
    }

    public void startOver() {
        task = DystellarCore.getAsyncManager().scheduleAtFixedRate(this::add, 1000L, 1000L, TimeUnit.MILLISECONDS);
        this.isRunning = true;
    }

    public String formatTime() {
        double minutes = (double) value / 60;
        if (minutes < 1) {
            if (value < 10) {
                return "0:0" + value;
            } else {
                return "0:" + value;
            }
        }
        int minsLess = 60 * (int) minutes;
        if (value - minsLess < 10) {
            return ((int) minutes) + ":0" + (value - minsLess);
        } else {
            return ((int) minutes) + ":" + (value - minsLess);
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void endTask() {
        if (task != null && !task.isCancelled()) {
            task.cancel(true);
        }
        this.isRunning = false;
        this.value = 0;
    }

    public void getAndEndTask() {
        if (!task.isCancelled()) {
            task.cancel(true);
        }
        this.isRunning = false;
        this.freezedInt += this.value;
        this.value = 0;
    }

    public int getFreezedInt() {
        return freezedInt;
    }

    public void setFreezedInt(int freezedInt) {
        this.freezedInt = freezedInt;
    }
}
