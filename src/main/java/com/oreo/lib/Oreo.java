package com.oreo.lib;

import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Main entry point for OreoLib.
 *
 * Recommended: import static com.oreo.lib.Oreo.*;
 */
public final class Oreo {
    private Oreo() {}

    public static void out(Object value) { Console.out(value); }
    public static void out(Object... values) { Console.out(values); }
    public static void print(Object value) { Console.print(value); }
    public static void printf(String format, Object... args) { Console.printf(format, args); }
    public static void outf(String format, Object... args) { Console.outf(format, args); }
    public static void info(Object value) { Console.info(value); }
    public static void success(Object value) { Console.success(value); }
    public static void warn(Object value) { Console.warn(value); }
    public static void error(Object value) { Console.error(value); }

    public static void when(boolean condition, Runnable action) {
        if (condition) action.run();
    }

    public static void when(boolean condition, Runnable yes, Runnable no) {
        if (condition) yes.run(); else no.run();
    }

    public static <T> T choose(boolean condition, T yes, T no) {
        return condition ? yes : no;
    }

    public static <T> T choose(boolean condition, Supplier<? extends T> yes, Supplier<? extends T> no) {
        return condition ? yes.get() : no.get();
    }

    public static <T> WhenValue<T> when(T value) {
        return new WhenValue<>(value);
    }

    public static <T> Match<T> match(T value) {
        return new Match<>(value);
    }

    public static <T> Safe<T> safe(T value) {
        return Safe.of(value);
    }

    public static <T> T or(T value, T fallback) {
        return value != null ? value : fallback;
    }

    @SafeVarargs
    public static <T> T firstNonNull(T... values) {
