package com.oreo.lib.db;

import com.oreo.lib.OreoException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Tiny data-mapper "ORM" over plain JDBC. Dependency-free: it works with any
 * JDBC driver on the classpath. {@code sqlite(...)} is a convenience for the
 * common game-save case and needs the {@code org.xerial:sqlite-jdbc} driver.
 *
 * <pre>{@code
 * try (Db db = Db.sqlite("game.db")) {
 *     db.run("CREATE TABLE IF NOT EXISTS score(name TEXT, points INT)");
 *     db.sql("INSERT INTO score(name, points) VALUES(?, ?)").params("Alex", 100).run();
 *
 *     List<Score> top = db.sql("SELECT name, points FROM score ORDER BY points DESC")
 *                         .mapTo(Score.class);
 * }
 * record Score(String name, int points) {}
 * }</pre>
 */
public final class Db implements AutoCloseable {
    private final Connection connection;

    private Db(Connection connection) {
        this.connection = connection;
    }

    /** Opens a SQLite database file (created if missing). Requires the sqlite-jdbc driver. */
    public static Db sqlite(String path) {
        return connect("jdbc:sqlite:" + path);
    }

    /** Opens a private in-memory SQLite database (lost on close). Useful for tests. */
    public static Db sqliteMemory() {
        return connect("jdbc:sqlite::memory:");
    }

    public static Db connect(String url) {
        try {
            return new Db(DriverManager.getConnection(url));
        } catch (SQLException e) {
            throw new OreoException("Could not connect to " + url + ": " + e.getMessage(), e);
        }
    }

    public static Db connect(String url, String user, String password) {
        try {
            return new Db(DriverManager.getConnection(url, user, password));
        } catch (SQLException e) {
            throw new OreoException("Could not connect to " + url + ": " + e.getMessage(), e);
        }
    }

    /** Wraps an existing JDBC connection (OreoLib will not close it unless you call close()). */
    public static Db using(Connection connection) {
        return new Db(connection);
    }

    /** Starts a fluent statement. */
    public Sql sql(String sql) {
        return new Sql(connection, sql);
    }

    /** A Spring-Data-style auto-CRUD repository for an annotated entity class. */
    public <T> Repository<T> repository(Class<T> entityType) {
        return new Repository<>(this, entityType);
    }

    public int run(String sql, Object... params) {
        return sql(sql).params(params).run();
    }

    public List<Row> query(String sql, Object... params) {
        return sql(sql).params(params).query();
    }

    public Optional<Row> queryFirst(String sql, Object... params) {
        return sql(sql).params(params).queryFirst();
    }

    public Connection connection() {
        return connection;
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new OreoException("Could not close database: " + e.getMessage(), e);
        }
    }
}
