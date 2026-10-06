package com.oreo.lib;

import java.util.Collection;
import java.util.Map;

/** Fluent argument validation. */
public final class Requirement<T> {
    private final T value;
    private final String name;

    Requirement(T value, String name) {
        this.value = value;
        this.name = name == null || name.isBlank() ? "value" : name;
    }

    public Requirement<T> notNull() {
        if (value == null) throw new IllegalArgumentException(name + " must not be null");
        return this;
    }

    public Requirement<T> notEmpty() {
        notNull();
        boolean empty = false;
        if (value instanceof CharSequence s) empty = s.length() == 0;
        else if (value instanceof Collection<?> c) empty = c.isEmpty();
        else if (value instanceof Map<?, ?> m) empty = m.isEmpty();
        if (empty) throw new IllegalArgumentException(name + " must not be empty");
        return this;
    }

    public Requirement<T> notBlank() {
        notNull();
        if (value instanceof CharSequence s && s.toString().isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return this;
    }

    public Requirement<T> min(double minimum) {
        notNull();
        if (!(value instanceof Number number)) {
            throw new IllegalStateException(name + " is not a number");
        }
        if (number.doubleValue() < minimum) {
            throw new IllegalArgumentException(name + " must be >= " + minimum);
        }
        return this;
    }

    public Requirement<T> max(double maximum) {
        notNull();
        if (!(value instanceof Number number)) {
            throw new IllegalStateException(name + " is not a number");
        }
        if (number.doubleValue() > maximum) {
            throw new IllegalArgumentException(name + " must be <= " + maximum);
        }
        return this;
    }

    public T get() {
        return value;
    }
}
