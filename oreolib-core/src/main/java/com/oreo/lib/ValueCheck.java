package com.oreo.lib;

import java.util.Objects;
import java.util.function.Predicate;

/** Readable checks such as {@code is(value).equalTo(expected)}. */
public final class ValueCheck<T> {
    private final T value;

    ValueCheck(T value) {
        this.value = value;
    }

    public boolean equalTo(T expected) {
        return Objects.equals(value, expected);
    }

    public boolean notEqualTo(T expected) {
        return !equalTo(expected);
    }

    public boolean notNull() {
        return value != null;
    }

    public boolean nullValue() {
        return value == null;
    }

    @SafeVarargs
    public final boolean oneOf(T... candidates) {
        if (candidates == null) return false;
        for (T candidate : candidates) {
            if (Objects.equals(value, candidate)) return true;
        }
        return false;
    }

    public boolean in(Iterable<? extends T> candidates) {
        Checks.notNull(candidates, "candidates");
        for (T candidate : candidates) {
            if (Objects.equals(value, candidate)) return true;
        }
        return false;
    }

    public boolean matches(Predicate<? super T> condition) {
        Checks.notNull(condition, "condition");
        return condition.test(value);
    }
}
