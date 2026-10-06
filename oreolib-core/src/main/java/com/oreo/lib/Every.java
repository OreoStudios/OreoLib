package com.oreo.lib;

import java.time.Duration;

/** Starts a readable recurring interval such as {@code every(5).seconds()}. */
public final class Every {
    private final long amount;

    Every(long amount) {
        Checks.require(amount > 0, "amount must be > 0");
        this.amount = amount;
    }

    public Interval milliseconds() {
        return new Interval(Duration.ofMillis(amount));
    }

    public Interval seconds() {
        return new Interval(Duration.ofSeconds(amount));
    }

    public Interval minutes() {
        return new Interval(Duration.ofMinutes(amount));
    }

    public Interval hours() {
        return new Interval(Duration.ofHours(amount));
    }
}
