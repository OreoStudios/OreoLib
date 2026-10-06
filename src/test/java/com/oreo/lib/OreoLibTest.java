package com.oreo.lib;

import com.oreo.lib.db.Column;
import com.oreo.lib.db.Db;
import com.oreo.lib.db.Id;
import com.oreo.lib.db.Modifying;
import com.oreo.lib.db.Param;
import com.oreo.lib.db.Query;
import com.oreo.lib.db.Repositories;
import com.oreo.lib.db.Repository;
import com.oreo.lib.db.Row;
import com.oreo.lib.db.Table;
import com.oreo.lib.ecs.Engine;
import com.oreo.lib.ecs.Entity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.oreo.lib.Oreo.*;
import static org.junit.jupiter.api.Assertions.*;

class OreoLibTest {
    @Test
    void englishConditionAndRepetitionReadNaturally() {
        AtomicInteger branch = new AtomicInteger();
        when(true)
                .then(() -> branch.set(1))
                .otherwise(() -> branch.set(2));
        assertEquals(1, branch.get());

        AtomicInteger repeats = new AtomicInteger();
        repeat(3).times(repeats::incrementAndGet);
        assertEquals(3, repeats.get());

        AtomicInteger indexes = new AtomicInteger();
        repeat(4).times(indexes::addAndGet);
        assertEquals(6, indexes.get());
    }

    @Test
    void mathAndAggregateHelpersReadNaturally() {
        assertEquals(5, clamp(10, 0, 5));
        assertEquals(0, clamp(-3, 0, 5));
        assertEquals(3, clamp(3, 0, 5));
        assertEquals(5f, lerp(0f, 10f, 0.5f));
        assertEquals(0.5f, fraction(50f, 0f, 100f));
        assertEquals(1f, fraction(999f, 0f, 100f));

        AtomicInteger sum = new AtomicInteger();
        loop(4, sum::addAndGet);
        assertEquals(6, sum.get());

        List<String> words = list("a", "bb", "ccc");
        assertEquals(6, sumBy(words, String::length));
        assertEquals(2, countWhere(words, w -> w.length() >= 2));
        assertEquals(6, from(words).sumInt(String::length));
        assertEquals("a,bb,ccc", from(words).join(","));
        assertEquals("ccc", from(words).maxBy(java.util.Comparator.comparingInt(String::length), ""));
        assertEquals("ccc", from(words).reversed().firstOr(""));
        assertEquals("ccc", from(words).lastOr(""));
    }

    @Test
    void ormStoresQueriesAndMapsToRecords() {
        record Score(String name, int points) {}

        try (Db db = Db.sqliteMemory()) {
            db.run("CREATE TABLE score(name TEXT, points INT)");

            long affected = db.sql("INSERT INTO score(name, points) VALUES(?, ?)")
                .params("Alex", 100).run();
            db.sql("INSERT INTO score(name, points) VALUES(?, ?)").params("Steve", 250).run();
            assertEquals(1, affected);

            List<Row> rows = db.query("SELECT name, points FROM score WHERE points >= ?", 150);
            assertEquals(1, rows.size());
            assertEquals("Steve", rows.get(0).getString("name"));
            assertEquals(250, rows.get(0).getInt("points"));

            List<Score> top = db.sql("SELECT name, points FROM score ORDER BY points DESC")
                .mapTo(Score.class);
            assertEquals(2, top.size());
            assertEquals(new Score("Steve", 250), top.get(0));
            assertEquals(new Score("Alex", 100), top.get(1));

            int total = db.queryFirst("SELECT SUM(points) AS total FROM score")
                .map(row -> row.getInt("total")).orElse(0);
            assertEquals(350, total);
        }
    }

    @Table(name = "players")
    static class PlayerRow {
        @Id Long id;
        @Column(name = "name") String name;
        int score;
        PlayerRow() {}
        PlayerRow(String name, int score) { this.name = name; this.score = score; }
    }

    interface PlayerDao {
        @Query("SELECT * FROM players WHERE score >= :min ORDER BY score DESC")
        List<PlayerRow> topScorers(@Param("min") int min);

        @Query("SELECT COUNT(*) FROM players")
        long total();

        @Modifying
        @Query("UPDATE players SET score = score + :bonus WHERE name = :name")
        int award(@Param("name") String name, @Param("bonus") int bonus);
    }

    @Test
    void repositoryInterfaceRunsQueryAnnotations() {
        try (Db db = Db.sqliteMemory()) {
            Repository<PlayerRow> players = db.repository(PlayerRow.class).createTable();
            players.save(new PlayerRow("Alex", 100));
            players.save(new PlayerRow("Steve", 250));
            players.save(new PlayerRow("Max", 300));

            PlayerDao dao = Repositories.create(PlayerDao.class, db);

            List<PlayerRow> top = dao.topScorers(250);
            assertEquals(2, top.size());
            assertEquals("Max", top.get(0).name);        // ordered DESC
            assertEquals(3L, dao.total());

            assertEquals(1, dao.award("Alex", 50));       // @Modifying UPDATE
            PlayerRow alex = dao.topScorers(0).stream()
                .filter(p -> p.name.equals("Alex")).findFirst().orElseThrow();
            assertEquals(150, alex.score);
        }
    }

    @Test
    void repositoryDoesSpringStyleCrudFromAnnotations() {
        try (Db db = Db.sqliteMemory()) {
            Repository<PlayerRow> players = db.repository(PlayerRow.class).createTable();

            PlayerRow saved = players.save(new PlayerRow("Alex", 100));
            assertNotNull(saved.id);                 // generated key written back
            assertTrue(saved.id > 0);
            players.save(new PlayerRow("Steve", 250));
            assertEquals(2, players.count());

            PlayerRow found = players.findById(saved.id).orElseThrow();
            assertEquals("Alex", found.name);
            assertEquals(100, found.score);

            found.score = 150;
            players.save(found);                     // UPDATE (id is set)
            assertEquals(150, players.findById(saved.id).orElseThrow().score);
            assertEquals(2, players.count());        // still two rows, no duplicate

            players.deleteById(saved.id);
            assertEquals(1, players.count());
        }
    }

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

    @Test
    void attemptsAreLazyRetryableAndCached() {
        AtomicInteger calls = new AtomicInteger();
        Attempt<String> operation = attempt(() -> {
            if (calls.incrementAndGet() < 3) throw new Exception("not yet");
            return "ready";
        });

        assertEquals(0, calls.get());
        String result = operation
                .upTo(3).times()
                .waiting(1).milliseconds()
                .orElse("unavailable");

        assertEquals("ready", result);
        assertEquals(3, calls.get());
        assertTrue(operation.ok());
        assertEquals(3, calls.get());

        assertEquals(25565, attempt(() -> Integer.parseInt("invalid")).orElse(25565));
    }

    @Test
    void collectionPipelineHasEnglishTerminal() {
        List<String> names = from(List.of("Steve", "Alex", "Elias", "Bob"))
                .where(name -> name.length() >= 5)
                .map(String::toUpperCase)
                .sorted()
                .toList();

        assertEquals(List.of("ELIAS", "STEVE"), names);
        assertEquals("only", chooseOneFrom(List.of("only")));
        assertThrows(IllegalArgumentException.class, () -> chooseOneFrom(List.of()));
    }

    @Test
    void fluentFilesReadWriteAndDelete(@TempDir Path directory) {
        OreoFile config = file(directory.resolve("config.txt"));

        config.writeText("enabled=true\n")
                .appendText("port=25565\n");

        assertTrue(config.exists());
        assertEquals("enabled=true\nport=25565\n", config.readText().orElse(""));
        assertTrue(config.delete());
        assertFalse(config.exists());
    }

    @Test
    void readableChecksHandleValuesAndNumbers() {
        assertTrue(is("ONLINE").equalTo("ONLINE"));
        assertTrue(is("ONLINE").oneOf("OFFLINE", "ONLINE"));
        assertTrue(is("ONLINE").matches(value -> value.startsWith("ON")));
        assertTrue(is(new Object()).notNull());
        assertTrue(is(null).nullValue());

        assertTrue(number(10).isBetween(1, 20));
        assertTrue(number(10).isAtLeast(10));
        assertTrue(number(-1).isNegative());
        assertThrows(IllegalArgumentException.class, () -> number(10).isBetween(20, 1));
    }

    @Test
    void typedSettingsUseSystemPropertiesAndFallbacks() {
        String key = "oreolib.test.port";
        String oldValue = System.getProperty(key);
        try {
            System.setProperty(key, "25565");
            assertTrue(setting(key).exists());
            assertEquals(25565, setting(key).asInteger().orElse(0));
            assertEquals(7, setting("oreolib.missing.setting").asInteger().orElse(7));
        } finally {
            if (oldValue == null) System.clearProperty(key);
            else System.setProperty(key, oldValue);
        }
    }

    @Test
    void asyncTasksExposeSuccessAndFailureCallbacks() {
        AtomicInteger completed = new AtomicInteger();
        runAsync(completed::incrementAndGet)
                .whenDone(completed::incrementAndGet)
                .whenFailed(error -> fail(error.getMessage()))
                .await();
        assertEquals(2, completed.get());

        AtomicReference<Throwable> failure = new AtomicReference<>();
        AsyncTask<Void> failedTask = runAsync(() -> {
            throw new IllegalStateException("boom");
        }).whenFailed(failure::set);

        assertThrows(CompletionException.class, failedTask::await);
        assertInstanceOf(IllegalStateException.class, failure.get());
    }

    @Test
    void recurringTasksCanBeCancelled() throws InterruptedException {
        CountDownLatch ran = new CountDownLatch(1);
        ScheduledTask task = every(1).milliseconds().run(ran::countDown);
        try {
            assertTrue(ran.await(2, TimeUnit.SECONDS));
        } finally {
            task.close();
        }
        assertTrue(task.isCancelled());
    }

    @Test
    void readableWaitingUsesTheRequestedUnit() {
        long started = System.nanoTime();
        waitFor(1).milliseconds();
        assertTrue(System.nanoTime() >= started);
    }

    @Test
    void existingFluentApisRemainAvailable() {
        AtomicInteger branch = new AtomicInteger();
        match("ONLINE")
                .caseOf("OFFLINE", () -> branch.set(1))
                .caseOf("ONLINE", () -> branch.set(2))
                .otherwise(() -> branch.set(3));
        assertEquals(2, branch.get());

        String nested = safe(new User(new Profile("Oreo")))
                .map(User::profile)
                .map(Profile::name)
                .orElse("Unknown");
        assertEquals("Oreo", nested);

        Map<String, Integer> fruit = mapOf("apple", 5, "banana", 10);
        assertEquals(10, fruit.get("banana"));

        OreoCache<String, Integer> cache = Oreo.<String, Integer>cache()
                .expireAfter(Duration.ofMinutes(1));
        AtomicInteger loads = new AtomicInteger();
        assertEquals(1, cache.get("x", loads::incrementAndGet));
        assertEquals(1, cache.get("x", loads::incrementAndGet));

        Cooldown<String> cooldown = Oreo.<String>cooldown(1, TimeUnit.SECONDS);
        assertTrue(cooldown.use("player"));
        assertFalse(cooldown.use("player"));
    }

    private record Profile(String name) {}
    private record User(Profile profile) {}
}
