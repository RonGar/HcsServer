/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  hcsplatfom.PlatformBridge
 */
package hcsmod.server;

import hcsplatfom.PlatformBridge;
import java.util.LinkedList;
import java.util.Queue;

public abstract class DelayedTask
implements Runnable {
    private static final Queue<DelayedTask> QUEUE = new LinkedList<DelayedTask>();
    private boolean queued = false;
    private long queuedAt = 0L;

    public boolean isQueued() {
        return this.queued;
    }

    public void queue() {
        if (!this.queued) {
            this.queued = true;
            this.queuedAt = System.currentTimeMillis();
            QUEUE.add(this);
        }
    }

    public static boolean runDelayedTask() {
        DelayedTask delayedTask = QUEUE.poll();
        if (delayedTask == null || !delayedTask.queued) {
            return false;
        }
        delayedTask.queued = false;
        delayedTask.run();
        PlatformBridge.timings.report("DelayedTasksLatency", System.currentTimeMillis() - delayedTask.queuedAt);
        return true;
    }
}

