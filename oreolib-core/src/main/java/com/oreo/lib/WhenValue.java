package com.oreo.lib;

import java.util.function.Predicate;

/** Fluent if / else-if chain around one value. */
public final class WhenValue<T> {
    private final T value;
    private boolean matched;

    WhenValue(T value) {
        this.value = value;
    }

    public WhenValue<T> is(Predicate<? super T> condition, Runnable action) {
        if (!matched && condition.test(value)) {
            action.run();
            matched = true;
        }
        return this;
    }

    public WhenValue<T> isValue(T expected, Runnable action) {
        return is(value -> java.util.Objects.equals(value, expected), action);
    }

    public void otherwise(Runnable action) {
        if (!matched) action.run();
    }
}
