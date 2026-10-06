package com.oreo.lib.mongo;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;

/**
 * Readable wrapper over a MongoDB collection. Documents are plain
 * {@code Map<String, Object>} (org.bson.Document is itself such a map).
 */
public final class Documents {
    private final MongoCollection<Document> collection;

    Documents(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    public Documents insert(Map<String, Object> document) {
        collection.insertOne(new Document(document));
        return this;
    }

    public long count() {
        return collection.countDocuments();
    }

    public List<Map<String, Object>> all() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Document document : collection.find()) out.add(document);
        return out;
    }

    /** Every document whose field equals the given value. */
    public List<Map<String, Object>> where(String field, Object value) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Document document : collection.find(eq(field, value))) out.add(document);
        return out;
    }

    public Optional<Map<String, Object>> findById(Object id) {
        return Optional.<Map<String, Object>>ofNullable(collection.find(eq("_id", id)).first());
    }

    public Documents deleteById(Object id) {
        collection.deleteOne(eq("_id", id));
        return this;
    }

    public MongoCollection<Document> raw() {
        return collection;
    }
}
