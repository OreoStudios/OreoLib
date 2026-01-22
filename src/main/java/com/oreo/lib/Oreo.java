package com.oreo.lib;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Main entry point for OreoLib.
 *
 * Import statically when you want very short calls:
 * import static com.oreo.lib.Oreo.*;
 */
public final class Oreo {
    private Oreo() {}


    public static void out(Object value) {
        Console.out(value);
    }

    public static void out(Object... values) {
        Console.out(values);
    }

    public static void print(Object value) {
        Console.print(value);
    }

    public static void printf(String format, Object... args) {
        Console.printf(format, args);
    }

    public static void outf(String format, Object... args) {
        Console.outf(format, args);
    }

    public static void info(Object value) {
        Console.info(value);
    }

    public static void success(Object value) {
        Console.success(value);
    }

    public static void warn(Object value) {
        Console.warn(value);
    }

    public static void error(Object value) {
        Console.error(value);
    }

    public static Text text(String value) {
        return Text.of(value);
    }

    @SafeVarargs
    public static <T> List<T> list(T... values) {
        return Lists.of(values);
    }

    public static List<Integer> range(int startInclusive, int endExclusive) {
        return Lists.range(startInclusive, endExclusive);
    }

    public static <T> Chain<T> chain(T value) {
        return Chain.of(value);
    }

    public static String read(String path) {
        return OreoFiles.read(path);
    }

    public static String read(Path path) {
        return OreoFiles.read(path);
    }

    public static void write(String path, String content) {
        OreoFiles.write(path, content);
    }

    public static void append(String path, String content) {
        OreoFiles.append(path, content);
    }

    public static Result<Void> tryRun(ThrowingRunnable action) {
        return Result.run(action);
    }

    public static <T> Result<T> tryGet(ThrowingSupplier<T> action) {
        return Result.of(action);
    }

    public static <T> T retry(int attempts, ThrowingSupplier<T> action) {
        return Attempts.retry(attempts, Duration.ZERO, action);
