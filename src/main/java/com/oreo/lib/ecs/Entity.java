package com.oreo.lib.ecs;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A game object: just a bag of components keyed by their class.
 *
 * <pre>{@code
 * Entity player = new Entity()
 *     .set(new Position(0, 0))
 *     .set(new Velocity(1, 0));
 *
 * if (player.has(Position.class)) {
 *     Position p = player.get(Position.class);
 * }
 * }</pre>
 *
 * Components are stored under their concrete runtime class.
 */
public final class Entity {
    private final Map<Class<?>, Object> components = new HashMap<>();

    public <T> Entity set(T component) {
        components.put(component.getClass(), component);
        return this;
    }

    /** Stores a component under an explicit key (e.g. an interface type). */
    public <T> Entity set(Class<? super T> key, T component) {
        components.put(key, component);
        return this;
    }

    public <T> T get(Class<T> type) {
        return type.cast(components.get(type));
    }

    public boolean has(Class<?> type) {
        return components.containsKey(type);
    }

    public boolean has(Class<?>... types) {
        for (Class<?> type : types) {
            if (!components.containsKey(type)) return false;
        }
        return true;
    }

    public Entity remove(Class<?> type) {
        components.remove(type);
        return this;
    }

    public Collection<Object> components() {
        return components.values();
    }
}
