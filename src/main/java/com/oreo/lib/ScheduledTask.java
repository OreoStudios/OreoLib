package com.oreo.lib;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/** Handle for a recurring daemon task. Close it when the task is no longer needed. */
public final class ScheduledTask implements AutoCloseable {
    private static final ScheduledExecutorService SCHEDULER =
            Executors.newSingleThreadScheduledExecutor(action -> {
                Thread thread = new Thread(action, "oreolib-scheduler");
                thread.setDaemon(true);
                return thread;
            });

    private final ScheduledFuture<?> future;

    private ScheduledTask(ScheduledFuture<?> future) {
        this.future = future;
    }

    static ScheduledTask every(Duration interval, Runnable action) {
        Objects.requireNonNull(interval, "interval");
        Objects.requireNonNull(action, "action");
        long delayNanos = interval.toNanos();
        Checks.require(delayNanos > 0, "interval must be > 0");
        return new ScheduledTask(SCHEDULER.scheduleAtFixedRate(
                action,
                delayNanos,
                delayNanos,
                TimeUnit.NANOSECONDS
        ));
    }

    public boolean cancel() {
        return future.cancel(false);
    }

    public boolean isCancelled() {
        return future.isCancelled();
    }

    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public void close() {
        cancel();
    }
}
