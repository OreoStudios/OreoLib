package com.oreo.lib;

import java.util.Objects;

/** Fluent true/false branch that reads like an English sentence. */
public final class WhenCondition {
    private final boolean condition;

    WhenCondition(boolean condition) {
        this.condition = condition;
    }

    public WhenCondition then(Runnable action) {
        Objects.requireNonNull(action, "action");
        if (condition) action.run();
        return this;
    }

    public WhenCondition otherwise(Runnable action) {
        Objects.requireNonNull(action, "action");
        if (!condition) action.run();
        return this;
    }
}
