package com.oreo.lib.db;

import com.oreo.lib.OreoException;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Maps a {@link Row} onto a Java record by matching component names to columns. */
final class Rows {
    private Rows() {}

    /** Reads an entire result set into rows keyed by lower-case column label. */
    static List<Row> read(ResultSet resultSet) throws SQLException {
        ResultSetMetaData meta = resultSet.getMetaData();
        int columnCount = meta.getColumnCount();
        String[] labels = new String[columnCount];
        for (int i = 0; i < columnCount; i++) {
            labels[i] = meta.getColumnLabel(i + 1).toLowerCase();
        }
        List<Row> rows = new ArrayList<>();
        while (resultSet.next()) {
            Map<String, Object> values = new LinkedHashMap<>();
            for (int i = 0; i < columnCount; i++) {
                values.put(labels[i], resultSet.getObject(i + 1));
            }
            rows.add(new Row(values));
        }
        return rows;
    }

    static <T> T toRecord(Row row, Class<T> type) {
        if (!type.isRecord()) {
            throw new OreoException("mapTo requires a record type, got: " + type.getName());
        }
        RecordComponent[] components = type.getRecordComponents();
        Class<?>[] parameterTypes = new Class<?>[components.length];
        Object[] arguments = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            parameterTypes[i] = components[i].getType();
            arguments[i] = convert(row, components[i].getName(), components[i].getType());
        }
        try {
            Constructor<T> constructor = type.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return constructor.newInstance(arguments);
        } catch (ReflectiveOperationException e) {
            throw new OreoException("Could not map row to " + type.getName() + ": " + e.getMessage(), e);
        }
    }

    private static Object convert(Row row, String column, Class<?> target) {
        if (target == String.class) return row.getString(column);
        if (target == int.class || target == Integer.class) return row.getInt(column);
        if (target == long.class || target == Long.class) return row.getLong(column);
        if (target == double.class || target == Double.class) return row.getDouble(column);
        if (target == float.class || target == Float.class) return (float) row.getDouble(column);
        if (target == boolean.class || target == Boolean.class) return row.getBoolean(column);
        // Fall back to whatever the driver returned (e.g. byte[], Timestamp).
        return row.get(column);
    }
}
