package com.oreo.lib;

/** Readable comparisons for a non-null number. */
public final class NumberCheck<T extends Number & Comparable<T>> {
    private final T value;

    NumberCheck(T value) {
        this.value = Checks.notNull(value, "value");
    }

    public boolean isBetween(T minimum, T maximum) {
        Checks.notNull(minimum, "minimum");
        Checks.notNull(maximum, "maximum");
        Checks.require(minimum.compareTo(maximum) <= 0, "minimum must be <= maximum");
        return isAtLeast(minimum) && isAtMost(maximum);
    }

    public boolean isGreaterThan(T other) {
        return value.compareTo(Checks.notNull(other, "other")) > 0;
    }

    public boolean isLessThan(T other) {
        return value.compareTo(Checks.notNull(other, "other")) < 0;
    }

    public boolean isAtLeast(T minimum) {
        return value.compareTo(Checks.notNull(minimum, "minimum")) >= 0;
    }

    public boolean isAtMost(T maximum) {
        return value.compareTo(Checks.notNull(maximum, "maximum")) <= 0;
    }

    public boolean isPositive() {
        return value.doubleValue() > 0;
    }

    public boolean isNegative() {
        return value.doubleValue() < 0;
    }

    public boolean isZero() {
        return value.doubleValue() == 0;
    }
}
