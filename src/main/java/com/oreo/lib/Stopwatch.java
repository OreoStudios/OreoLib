package com.oreo.lib;

import java.time.Duration;

public final class Stopwatch {
    private final long startedNanos;

    private Stopwatch() {
        this.startedNanos = System.nanoTime();
    }

    public static Stopwatch start() {
        return new Stopwatch();
    }

    public Duration elapsed() {
        return Duration.ofNanos(System.nanoTime() - startedNanos);
    }

    public long millis() {
        return elapsed().toMillis();
    }

    public static long measure(Runnable action) {
        Stopwatch sw = start();
        action.run();
        return sw.millis();
    }
}
