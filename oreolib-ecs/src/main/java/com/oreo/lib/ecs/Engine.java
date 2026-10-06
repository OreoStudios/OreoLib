package com.oreo.lib.ecs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Holds entities and systems and drives them each frame.
 *
 * <pre>{@code
 * Engine engine = new Engine();
 * engine.create().set(new Position(0, 0)).set(new Velocity(1, 0));
 *
 * engine.add((e, dt) -> {
 *     for (Entity entity : e.entitiesWith(Position.class, Velocity.class)) {
 *         Position p = entity.get(Position.class);
 *         Velocity v = entity.get(Velocity.class);
 *         p.x += v.dx * dt;
 *     }
 * });
 *
 * engine.update(1f / 60f);
 * }</pre>
 */
public final class Engine {
    private final List<Entity> entities = new ArrayList<>();
    private final List<EntitySystem> systems = new ArrayList<>();

    public Entity create() {
        Entity entity = new Entity();
        entities.add(entity);
        return entity;
    }

    public Engine add(Entity entity) {
        entities.add(entity);
        return this;
    }

    public Engine remove(Entity entity) {
        entities.remove(entity);
        return this;
    }

    public Engine add(EntitySystem system) {
        systems.add(system);
        return this;
    }

    public List<Entity> entities() {
        return List.copyOf(entities);
    }

    /** Every entity that has all of the given component types (a safe snapshot copy). */
    public List<Entity> entitiesWith(Class<?>... types) {
        List<Entity> matches = new ArrayList<>();
        for (Entity entity : entities) {
            if (entity.has(types)) matches.add(entity);
        }
        return matches;
    }

    public void each(Consumer<Entity> action) {
        for (Entity entity : new ArrayList<>(entities)) action.accept(entity);
    }

    /** Runs every system once, in the order they were added. */
    public void update(float delta) {
        for (EntitySystem system : systems) {
            system.update(this, delta);
        }
    }

    public int size() {
        return entities.size();
    }
}
