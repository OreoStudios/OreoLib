package com.oreo.lib;

import java.util.Objects;
import java.util.function.IntConsumer;

/** Fluent repetition builder returned by {@link Oreo#repeat(int)}. */
public final class Repetition {
    private final int count;

    Repetition(int count) {
        Checks.require(count >= 0, "count must be >= 0");
        this.count = count;
    }

    public void times(Runnable action) {
        Objects.requireNonNull(action, "action");
        for (int index = 0; index < count; index++) action.run();
    }

    public void times(IntConsumer action) {
        Objects.requireNonNull(action, "action");
        for (int index = 0; index < count; index++) action.accept(index);
    }
}
