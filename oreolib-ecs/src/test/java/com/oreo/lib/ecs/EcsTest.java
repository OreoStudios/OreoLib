package com.oreo.lib.ecs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EcsTest {
    @Test
    void ecsRunsSystemsOverMatchingEntities() {
        class Pos { float x, y; Pos(float x, float y) { this.x = x; this.y = y; } }
        class Vel { float dx, dy; Vel(float dx, float dy) { this.dx = dx; this.dy = dy; } }

        Engine engine = new Engine();
        engine.create().set(new Pos(0, 0)).set(new Vel(2, 1));
        engine.create().set(new Pos(5, 5)); // no velocity -> must be skipped

        engine.add((e, dt) -> {
            for (Entity entity : e.entitiesWith(Pos.class, Vel.class)) {
                Pos p = entity.get(Pos.class);
                Vel v = entity.get(Vel.class);
                p.x += v.dx * dt;
                p.y += v.dy * dt;
            }
        });

        engine.update(1f);

        assertEquals(2, engine.size());
        assertEquals(1, engine.entitiesWith(Vel.class).size());
        Pos moved = engine.entitiesWith(Vel.class).get(0).get(Pos.class);
        assertEquals(2f, moved.x);
        assertEquals(1f, moved.y);
    }
}
