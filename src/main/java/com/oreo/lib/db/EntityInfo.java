package com.oreo.lib.db;

import com.oreo.lib.OreoException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/** Reflective mapping between an annotated entity class and a table. Package-private. */
final class EntityInfo<T> {
    final Class<T> type;
    final String table;
    final List<Field> fields = new ArrayList<>();
    final List<String> columns = new ArrayList<>();
    Field idField;
    String idColumn;

    private EntityInfo(Class<T> type) {
        this.type = type;
        this.table = resolveTable(type);
        for (Field field : type.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) continue;
            if (field.isAnnotationPresent(Transient.class)) continue;
            field.setAccessible(true);
            String column = columnName(field);
            fields.add(field);
            columns.add(column);
            if (field.isAnnotationPresent(Id.class)) {
                idField = field;
                idColumn = column;
            }
        }
        if (fields.isEmpty()) {
            throw new OreoException("Entity " + type.getName() + " has no mapped fields");
        }
    }

    static <T> EntityInfo<T> of(Class<T> type) {
        return new EntityInfo<>(type);
    }

    private static String resolveTable(Class<?> type) {
        Table table = type.getAnnotation(Table.class);
        if (table != null && !table.name().isBlank()) return table.name();
        Entity entity = type.getAnnotation(Entity.class);
        if (entity != null && !entity.name().isBlank()) return entity.name();
        return type.getSimpleName().toLowerCase();
    }

    private static String columnName(Field field) {
        Column column = field.getAnnotation(Column.class);
        return column != null && !column.name().isBlank() ? column.name() : field.getName();
    }

    T instantiate() {
        try {
            Constructor<T> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new OreoException("Entity " + type.getName()
                + " needs a no-arg constructor for repository use: " + e.getMessage(), e);
        }
    }

    Object get(Field field, T entity) {
        try {
            return field.get(entity);
        } catch (IllegalAccessException e) {
            throw new OreoException("Cannot read field " + field.getName(), e);
        }
    }

    void set(Field field, T entity, Object value) {
        try {
            field.set(entity, coerce(value, field.getType()));
        } catch (IllegalAccessException e) {
            throw new OreoException("Cannot set field " + field.getName(), e);
        }
    }

    boolean idIsEmpty(T entity) {
        if (idField == null) return false;
        Object value = get(idField, entity);
        if (value == null) return true;
        return value instanceof Number number && number.longValue() == 0L;
    }

    String sqlType(Class<?> target) {
        if (target == int.class || target == Integer.class
            || target == long.class || target == Long.class
            || target == boolean.class || target == Boolean.class) return "INTEGER";
        if (target == double.class || target == Double.class
            || target == float.class || target == Float.class) return "REAL";
        return "TEXT";
    }

    static Object coerce(Object value, Class<?> target) {
        if (value == null) return null;
        if (target.isInstance(value)) return value;
        if (value instanceof Number number) {
            if (target == int.class || target == Integer.class) return number.intValue();
            if (target == long.class || target == Long.class) return number.longValue();
            if (target == double.class || target == Double.class) return number.doubleValue();
            if (target == float.class || target == Float.class) return number.floatValue();
            if (target == boolean.class || target == Boolean.class) return number.longValue() != 0L;
        }
        String text = value.toString().trim();
        if (target == String.class) return text;
        if (target == int.class || target == Integer.class) return Integer.parseInt(text);
        if (target == long.class || target == Long.class) return Long.parseLong(text);
        if (target == double.class || target == Double.class) return Double.parseDouble(text);
        if (target == float.class || target == Float.class) return Float.parseFloat(text);
        if (target == boolean.class || target == Boolean.class) {
            return text.equalsIgnoreCase("true") || text.equals("1");
        }
        return value;
    }
}
