package com.oreo.lib;

import java.time.Duration;

/** A readable delay such as {@code waitFor(2).seconds()}. */
public final class Delay {
    private final long amount;

    Delay(long amount) {
        Checks.require(amount >= 0, "amount must be >= 0");
        this.amount = amount;
    }

    public void milliseconds() {
        sleep(Duration.ofMillis(amount));
    }

    public void seconds() {
        sleep(Duration.ofSeconds(amount));
    }

    public void minutes() {
        sleep(Duration.ofMinutes(amount));
    }

    public void hours() {
        sleep(Duration.ofHours(amount));
    }

    private void sleep(Duration duration) {
        Tasks.sleep(duration.toMillis());
    }
}
