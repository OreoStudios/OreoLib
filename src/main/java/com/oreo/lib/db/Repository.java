package com.oreo.lib.db;

import com.oreo.lib.OreoException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Spring-Data-style auto CRUD for an annotated entity. Reads {@link Entity}/
 * {@link Table}/{@link Id}/{@link Column} and builds the SQL for you.
 *
 * <pre>{@code
 * @Table(name = "players")
 * class PlayerRow {
 *     @Id Long id;                 // empty -> INSERT (id written back); set -> UPDATE
 *     @Column(name = "name") String name;
 *     int score;
 *     PlayerRow() {}
 * }
 *
 * Repository<PlayerRow> players = db.repository(PlayerRow.class).createTable();
 * PlayerRow saved = players.save(new PlayerRow(...));   // saved.id is now populated
 * List<PlayerRow> all = players.findAll();
 * Optional<PlayerRow> one = players.findById(saved.id);
 * }</pre>
 */
public final class Repository<T> {
    private final Db db;
    private final EntityInfo<T> info;

    Repository(Db db, Class<T> type) {
        this.db = db;
        this.info = EntityInfo.of(type);
    }

    /** Creates the backing table if it does not exist (types inferred from the fields). */
    public Repository<T> createTable() {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
            .append(info.table).append(" (");
        for (int i = 0; i < info.fields.size(); i++) {
            Field field = info.fields.get(i);
            if (i > 0) sql.append(", ");
            sql.append(info.columns.get(i)).append(' ').append(info.sqlType(field.getType()));
            if (field == info.idField) sql.append(" PRIMARY KEY");
        }
        sql.append(')');
        db.run(sql.toString());
        return this;
    }

    public List<T> findAll() {
        List<T> out = new ArrayList<>();
        for (Row row : db.query("SELECT * FROM " + info.table)) out.add(toEntity(row));
        return out;
    }

    public Optional<T> findById(Object id) {
        requireId();
        return db.sql("SELECT * FROM " + info.table + " WHERE " + info.idColumn + " = ?")
            .param(id).queryFirst().map(this::toEntity);
    }

    public long count() {
        return db.queryFirst("SELECT COUNT(*) AS c FROM " + info.table)
            .map(row -> row.getLong("c")).orElse(0L);
    }

    /** INSERT when the @Id is empty (writing the generated key back), otherwise UPDATE. */
    public T save(T entity) {
        return info.idIsEmpty(entity) ? insert(entity) : update(entity);
    }

    public void delete(T entity) {
        requireId();
        deleteById(info.get(info.idField, entity));
    }

    public void deleteById(Object id) {
        requireId();
        db.run("DELETE FROM " + info.table + " WHERE " + info.idColumn + " = ?", id);
    }

    private T insert(T entity) {
        List<String> cols = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        for (int i = 0; i < info.fields.size(); i++) {
            Field field = info.fields.get(i);
            if (field == info.idField && info.idIsEmpty(entity)) continue; // let the DB generate it
            cols.add(info.columns.get(i));
            values.add(info.get(field, entity));
        }
        String placeholders = String.join(", ", Collections.nCopies(cols.size(), "?"));
        String sql = "INSERT INTO " + info.table + " (" + String.join(", ", cols)
            + ") VALUES (" + placeholders + ")";
        long key = db.sql(sql).params(values.toArray()).insert();
        if (info.idField != null && info.idIsEmpty(entity) && key >= 0) {
            info.set(info.idField, entity, key);
        }
        return entity;
    }

    private T update(T entity) {
        requireId();
        List<String> assignments = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        for (int i = 0; i < info.fields.size(); i++) {
            Field field = info.fields.get(i);
            if (field == info.idField) continue;
            assignments.add(info.columns.get(i) + " = ?");
            values.add(info.get(field, entity));
        }
        values.add(info.get(info.idField, entity));
        String sql = "UPDATE " + info.table + " SET " + String.join(", ", assignments)
            + " WHERE " + info.idColumn + " = ?";
        db.sql(sql).params(values.toArray()).run();
        return entity;
    }

    private T toEntity(Row row) {
        return info.map(row);
    }

    private void requireId() {
        if (info.idField == null) {
            throw new OreoException("Entity " + info.type.getName()
                + " needs an @Id field for this operation");
        }
    }
}
