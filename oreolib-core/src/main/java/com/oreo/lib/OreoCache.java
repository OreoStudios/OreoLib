package com.oreo.lib;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/** Tiny thread-safe cache with optional expiry. */
public final class OreoCache<K, V> {
    private record Entry<V>(V value, long expiresAt) {}

    private final Map<K, Entry<V>> values = new ConcurrentHashMap<>();
    private volatile long ttlMillis = -1;

    public OreoCache<K, V> expireAfter(Duration duration) {
        Checks.notNull(duration, "duration");
        this.ttlMillis = duration.toMillis();
        return this;
    }

    public OreoCache<K, V> expireAfter(long amount, java.util.concurrent.TimeUnit unit) {
        return expireAfter(Duration.ofMillis(unit.toMillis(amount)));
    }

    public V get(K key, Supplier<? extends V> loader) {
        Entry<V> current = values.get(key);
        long now = System.currentTimeMillis();
        if (current != null && (current.expiresAt < 0 || current.expiresAt > now)) return current.value;

        V loaded = loader.get();
        put(key, loaded);
        return loaded;
    }

    public V getIfPresent(K key) {
        Entry<V> current = values.get(key);
        if (current == null) return null;
        if (current.expiresAt >= 0 && current.expiresAt <= System.currentTimeMillis()) {
            values.remove(key);
            return null;
        }
        return current.value;
    }

    public OreoCache<K, V> put(K key, V value) {
        long expires = ttlMillis < 0 ? -1 : System.currentTimeMillis() + ttlMillis;
        values.put(key, new Entry<>(value, expires));
        return this;
    }

    public void remove(K key) {
        values.remove(key);
    }

    public void clear() {
        values.clear();
    }

    public int size() {
        cleanup();
        return values.size();
    }

    public void cleanup() {
        long now = System.currentTimeMillis();
        values.entrySet().removeIf(e -> e.getValue().expiresAt >= 0 && e.getValue().expiresAt <= now);
    }
}
