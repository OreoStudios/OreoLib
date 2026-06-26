package com.oreo.lib;

import java.time.Duration;

/** A configured interval ready to run a recurring task. */
public final class Interval {
    private final Duration duration;

    Interval(Duration duration) {
        this.duration = duration;
    }

    public ScheduledTask run(Runnable action) {
        return ScheduledTask.every(duration, action);
    }
}
