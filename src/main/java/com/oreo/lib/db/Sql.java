package com.oreo.lib.db;

import com.oreo.lib.OreoException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * A fluent SQL statement. Bind values with {@link #param}/{@link #params},
 * then finish with a terminal call such as {@link #run}, {@link #query},
 * {@link #map} or {@link #mapTo}. (Named {@code Sql} so the {@code @Query}
 * annotation can own the {@code Query} name.)
 */
public final class Sql {
    private final Connection connection;
    private final String sql;
    private final List<Object> parameters = new ArrayList<>();

    Sql(Connection connection, String sql) {
        this.connection = connection;
        this.sql = sql;
    }

    public Sql param(Object value) {
        parameters.add(value);
        return this;
    }

    public Sql params(Object... values) {
        Collections.addAll(parameters, values);
        return this;
    }

    /** Executes an INSERT/UPDATE/DELETE/DDL statement and returns the affected row count. */
    public int run() {
        try (PreparedStatement statement = prepare(false)) {
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw fail(e);
        }
    }

    /** Executes an INSERT and returns the generated key (-1 if none). */
    public long insert() {
        try (PreparedStatement statement = prepare(true)) {
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : -1L;
            }
        } catch (SQLException e) {
            throw fail(e);
        }
    }

    /** Executes a SELECT and returns every row. */
    public List<Row> query() {
        try (PreparedStatement statement = prepare(false);
             ResultSet resultSet = statement.executeQuery()) {
            return Rows.read(resultSet);
        } catch (SQLException e) {
            throw fail(e);
        }
    }

    public Optional<Row> queryFirst() {
        List<Row> rows = query();
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    /** Executes a SELECT and converts each row with the given mapper. */
    public <T> List<T> map(Function<Row, T> mapper) {
        List<Row> rows = query();
        List<T> out = new ArrayList<>(rows.size());
        for (Row row : rows) out.add(mapper.apply(row));
        return out;
    }

    public <T> Optional<T> mapFirst(Function<Row, T> mapper) {
        return queryFirst().map(mapper);
    }

    /** Executes a SELECT and maps each row onto a record whose components match the columns. */
    public <T> List<T> mapTo(Class<T> recordType) {
        return map(row -> Rows.toRecord(row, recordType));
    }

    public <T> Optional<T> firstAs(Class<T> recordType) {
        return mapFirst(row -> Rows.toRecord(row, recordType));
    }

    private PreparedStatement prepare(boolean returnKeys) throws SQLException {
        PreparedStatement statement = returnKeys
            ? connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            : connection.prepareStatement(sql);
        for (int i = 0; i < parameters.size(); i++) {
            statement.setObject(i + 1, parameters.get(i));
        }
        return statement;
    }

    private OreoException fail(SQLException e) {
        return new OreoException("SQL failed: " + sql + " -> " + e.getMessage(), e);
    }
}
