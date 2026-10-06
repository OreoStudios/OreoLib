package com.oreo.lib.db;

/**
 * SQL flavour differences that matter for schema generation (column types and
 * auto-increment primary keys). Queries themselves are plain JDBC and portable.
 */
public enum Dialect {
    SQLITE,
    POSTGRESQL,
    MYSQL,
    MARIADB,
    GENERIC;

    public static Dialect fromUrl(String url) {
        if (url == null) return GENERIC;
        String u = url.toLowerCase();
        if (u.startsWith("jdbc:sqlite")) return SQLITE;
        if (u.startsWith("jdbc:postgresql")) return POSTGRESQL;
        if (u.startsWith("jdbc:mysql")) return MYSQL;
        if (u.startsWith("jdbc:mariadb")) return MARIADB;
        return GENERIC;
    }

    /** Column type for a plain (non-id) Java type. */
    public String type(Class<?> javaType) {
        if (javaType == int.class || javaType == Integer.class) {
            return this == MYSQL || this == MARIADB ? "INT" : "INTEGER";
        }
        if (javaType == long.class || javaType == Long.class) {
            return this == SQLITE || this == GENERIC ? "INTEGER" : "BIGINT";
        }
        if (javaType == double.class || javaType == Double.class
            || javaType == float.class || javaType == Float.class) {
            return switch (this) {
                case POSTGRESQL -> "DOUBLE PRECISION";
                case MYSQL, MARIADB -> "DOUBLE";
                default -> "REAL";
            };
        }
        if (javaType == boolean.class || javaType == Boolean.class) {
            return switch (this) {
                case POSTGRESQL -> "BOOLEAN";
                case MYSQL, MARIADB -> "TINYINT(1)";
                default -> "INTEGER";
            };
        }
        // String, UUID, Instant, LocalDate/LocalDateTime, enum(STRING)
        return this == MYSQL || this == MARIADB ? "VARCHAR(255)" : "TEXT";
    }

    /** Full primary-key column definition for a database-generated id. */
    public String generatedIdColumn(String column, Class<?> javaType) {
        boolean large = javaType == long.class || javaType == Long.class;
        return switch (this) {
            case POSTGRESQL -> column + (large ? " BIGSERIAL" : " SERIAL") + " PRIMARY KEY";
            case MYSQL, MARIADB -> column + (large ? " BIGINT" : " INT") + " AUTO_INCREMENT PRIMARY KEY";
            default -> column + " INTEGER PRIMARY KEY"; // SQLite / generic
        };
    }
}
