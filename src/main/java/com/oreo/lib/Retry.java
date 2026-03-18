package com.oreo.lib;

import java.time.Duration;

/** Fluent retry builder. */
public final class Retry {
    private final int attempts;
    private Duration delay = Duration.ZERO;

    Retry(int attempts) {
        Checks.require(attempts > 0, "attempts must be > 0");
        this.attempts = attempts;
    }

    public Retry delay(long millis) {
        this.delay = Duration.ofMillis(millis);
        return this;
    }

    public Retry delay(Duration duration) {
        this.delay = duration;
        return this;
    }

    public <T> T get(ThrowingSupplier<T> supplier) {
        return Attempts.retry(attempts, delay, supplier);
    }

    public void run(ThrowingRunnable runnable) {
        Attempts.retry(attempts, delay, () -> {
            runnable.run();
            return null;
        });
    }
}
