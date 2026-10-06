package com.oreo.lib.ecs;

/**
 * A piece of game behaviour that runs every frame over the engine's entities.
 * Named EntitySystem to avoid clashing with {@link java.lang.System}.
 */
@FunctionalInterface
public interface EntitySystem {
    void update(Engine engine, float delta);
}
