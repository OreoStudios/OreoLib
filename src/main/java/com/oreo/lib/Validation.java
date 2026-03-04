package com.oreo.lib;

import java.util.ArrayList;
import java.util.List;

/** Collect several validation errors before throwing once. */
public final class Validation {
    private final List<String> errors = new ArrayList<>();

    public Validation notEmpty(String value, String name) {
        if (value == null || value.isEmpty()) errors.add(name + " must not be empty");
        return this;
    }

    public Validation notBlank(String value, String name) {
        if (value == null || value.isBlank()) errors.add(name + " must not be blank");
        return this;
    }

    public Validation min(Number value, double minimum, String name) {
        if (value == null || value.doubleValue() < minimum) errors.add(name + " must be >= " + minimum);
        return this;
    }

    public Validation max(Number value, double maximum, String name) {
        if (value == null || value.doubleValue() > maximum) errors.add(name + " must be <= " + maximum);
        return this;
    }

    public Validation check(boolean condition, String message) {
        if (!condition) errors.add(message);
        return this;
    }

    public boolean valid() {
        return errors.isEmpty();
    }

    public List<String> errors() {
        return List.copyOf(errors);
    }

    public void throwIfInvalid() {
        if (!errors.isEmpty()) throw new IllegalArgumentException(String.join("; ", errors));
    }
}
