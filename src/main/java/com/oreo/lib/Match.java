package com.oreo.lib;

import java.util.Objects;

/**
 * Small fluent replacement for switch/case when each branch performs an action.
 */
public final class Match<T> {
    private final T value;
    private boolean matched;

    Match(T value) {
        this.value = value;
    }

    public Match<T> caseOf(T expected, Runnable action) {
        if (!matched && Objects.equals(value, expected)) {
            action.run();
            matched = true;
        }
        return this;
    }

    public Match<T> caseWhen(java.util.function.Predicate<T> predicate, Runnable action) {
        if (!matched && predicate.test(value)) {
            action.run();
            matched = true;
        }
        return this;
    }

    public void otherwise(Runnable action) {
        if (!matched) action.run();
    }

    public boolean matched() {
        return matched;
    }
}
