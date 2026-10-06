package com.oreo.lib.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

/**
 * Readable entry point for MongoDB. Unlike {@code oreolib-db}, this is a
 * document store — there is no SQL, no {@code @Query}, no {@code @Entity}.
 * You work with {@code Map}-shaped documents.
 *
 * <pre>{@code
 * try (Mongo mongo = Mongo.connect("mongodb://localhost:27017", "kaiju")) {
 *     Documents officers = mongo.collection("officers");
 *     officers.insert(Map.of("_id", id, "email", "a@x.io", "role", "ADMIN"));
 *     Optional<Map<String, Object>> one = officers.findById(id);
 *     List<Map<String, Object>> admins = officers.where("role", "ADMIN");
 * }
 * }</pre>
 *
 * Requires the {@code org.mongodb:mongodb-driver-sync} dependency on the classpath.
 */
public final class Mongo implements AutoCloseable {
    private final MongoClient client;
    private final MongoDatabase database;

    private Mongo(MongoClient client, MongoDatabase database) {
        this.client = client;
        this.database = database;
    }

    public static Mongo connect(String uri, String database) {
        MongoClient client = MongoClients.create(uri);
        return new Mongo(client, client.getDatabase(database));
    }

    public Documents collection(String name) {
        return new Documents(database.getCollection(name));
    }

    public MongoDatabase database() {
        return database;
    }

    public MongoClient client() {
        return client;
    }

    @Override
    public void close() {
        client.close();
    }
}
