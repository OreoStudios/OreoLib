package com.oreo.lib;

public final class Checks {
    private Checks() {}

    public static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }

    public static <T> T notNull(T value, String name) {
        if (value == null) throw new IllegalArgumentException(name + " must not be null");
        return value;
    }

    public static String notBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    public static int positive(int value, String name) {
        if (value <= 0) throw new IllegalArgumentException(name + " must be > 0");
        return value;
    }
}
