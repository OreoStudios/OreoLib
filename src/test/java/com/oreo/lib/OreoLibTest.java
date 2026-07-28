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

