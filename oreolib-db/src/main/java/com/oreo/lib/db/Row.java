package com.oreo.lib.db;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A single row of a query result with readable, type-safe accessors.
 * Column lookup is case-insensitive.
 */
public final class Row {
    private final Map<String, Object> values;

    Row(Map<String, Object> values) {
        this.values = values;
    }

    public Object get(String column) {
        return values.get(column.toLowerCase());
    }

    public boolean isNull(String column) {
        return get(column) == null;
    }

    public String getString(String column) {
        Object value = get(column);
        return value == null ? null : value.toString();
    }

    public int getInt(String column) {
        return (int) getLong(column);
    }

    public long getLong(String column) {
        Object value = get(column);
        if (value == null) return 0L;
        if (value instanceof Number number) return number.longValue();
        return Long.parseLong(value.toString().trim());
    }

    public double getDouble(String column) {
        Object value = get(column);
        if (value == null) return 0d;
        if (value instanceof Number number) return number.doubleValue();
        return Double.parseDouble(value.toString().trim());
    }

    public boolean getBoolean(String column) {
        Object value = get(column);
        if (value == null) return false;
        if (value instanceof Boolean bool) return bool;
        if (value instanceof Number number) return number.longValue() != 0;
        String text = value.toString().trim();
        return text.equalsIgnoreCase("true") || text.equals("1");
    }

    /** A copy of this row as a plain map keyed by lower-case column name. */
    public Map<String, Object> asMap() {
        return new LinkedHashMap<>(values);
    }

    @Override
    public String toString() {
        return values.toString();
    }
}
