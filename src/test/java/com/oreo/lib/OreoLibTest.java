package com.oreo.lib;

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
