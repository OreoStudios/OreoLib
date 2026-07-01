package com.oreo.lib;

import java.util.Locale;

/** Reads a system property or environment variable using a typed fluent API. */
public final class Setting {
    private final String key;

    Setting(String key) {
        this.key = Checks.notBlank(key, "key");
    }

    public Result<String> asText() {
        return Result.of(this::requiredValue);
    }

    public Result<Integer> asInteger() {
        return asText().map(value -> Integer.parseInt(value.trim()));
    }

    public Result<Long> asLong() {
        return asText().map(value -> Long.parseLong(value.trim()));
    }

    public Result<Double> asDouble() {
        return asText().map(value -> Double.parseDouble(value.trim()));
    }

    public Result<Boolean> asBoolean() {
        return asText().map(this::parseBoolean);
    }

    public boolean exists() {
        return findValue() != null;
    }

    private String requiredValue() {
        String value = findValue();
        if (value == null) throw new IllegalStateException("Setting not found: " + key);
        return value;
    }

    private String findValue() {
        String value = System.getProperty(key);
        if (value != null) return value;

        value = System.getenv(key);
        if (value != null) return value;

        String environmentKey = key.toUpperCase(Locale.ROOT)
                .replace('.', '_')
                .replace('-', '_');
        return System.getenv(environmentKey);
    }

    private boolean parseBoolean(String rawValue) {
        return switch (rawValue.trim().toLowerCase(Locale.ROOT)) {
            case "true", "yes", "on", "1" -> true;
            case "false", "no", "off", "0" -> false;
            default -> throw new IllegalArgumentException(
                    "Setting " + key + " must be true/false, yes/no, on/off, or 1/0"
            );
        };
    }
}
