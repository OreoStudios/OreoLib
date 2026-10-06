package com.oreo.lib;

import com.oreo.lib.db.Db;

import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

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

    public static WhenCondition when(boolean condition) {
        return new WhenCondition(condition);
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
        if (values == null) return null;
        for (T value : values) if (value != null) return value;
        return null;
    }

    public static Text text(String value) { return Text.of(value); }

    @SafeVarargs
    public static <T> List<T> list(T... values) { return Lists.of(values); }

    public static List<Integer> range(int startInclusive, int endExclusive) {
        return Lists.range(startInclusive, endExclusive);
    }

    public static <T> Flow<T> from(Iterable<T> source) {
        return Flow.from(source);
    }

    public static <T> T chooseOneFrom(Iterable<? extends T> source) {
        return Lists.chooseOne(source);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> mapOf(K firstKey, V firstValue, Object... rest) {
        if (rest.length % 2 != 0) throw new IllegalArgumentException("mapOf requires key/value pairs");
        Map<K, V> map = new LinkedHashMap<>();
        map.put(firstKey, firstValue);
        for (int i = 0; i < rest.length; i += 2) map.put((K) rest[i], (V) rest[i + 1]);
        return map;
    }

    public static <T> List<T> where(Iterable<T> source, Predicate<T> predicate) {
        return Lists.filter(source, predicate);
    }

    public static <T, R> List<R> map(Iterable<T> source, Function<T, R> mapper) {
        return Lists.map(source, mapper);
    }

    public static <T> void each(Iterable<T> source, Consumer<T> consumer) {
        Lists.each(source, consumer);
    }

    public static void repeat(int times, Runnable action) {
        Checks.require(times >= 0, "times must be >= 0");
        for (int i = 0; i < times; i++) action.run();
    }

    public static Repetition repeat(int times) {
        return new Repetition(times);
    }

    public static void times(int times, Consumer<Integer> action) {
        Checks.require(times >= 0, "times must be >= 0");
        for (int i = 0; i < times; i++) action.accept(i);
    }

    /** Runs the action for each index 0..count-1 without boxing. */
    public static void loop(int count, IntConsumer action) {
        Checks.require(count >= 0, "count must be >= 0");
        for (int i = 0; i < count; i++) action.accept(i);
    }

    public static int clamp(int value, int min, int max) { return Maths.clamp(value, min, max); }
    public static long clamp(long value, long min, long max) { return Maths.clamp(value, min, max); }
    public static float clamp(float value, float min, float max) { return Maths.clamp(value, min, max); }
    public static double clamp(double value, double min, double max) { return Maths.clamp(value, min, max); }
    public static float lerp(float from, float to, float t) { return Maths.lerp(from, to, t); }
    public static double lerp(double from, double to, double t) { return Maths.lerp(from, to, t); }
    public static float fraction(float value, float min, float max) { return Maths.fraction(value, min, max); }

    public static <T> int sumBy(Iterable<T> source, ToIntFunction<? super T> property) {
        return Lists.sumBy(source, property);
    }

    public static <T> int countWhere(Iterable<T> source, Predicate<? super T> predicate) {
        return Lists.countWhere(source, predicate);
    }

    public static <T> Chain<T> chain(T value) { return Chain.of(value); }

    public static String read(String path) { return OreoFiles.read(path); }
    public static String read(Path path) { return OreoFiles.read(path); }
    public static void write(String path, String content) { OreoFiles.write(path, content); }
    public static void append(String path, String content) { OreoFiles.append(path, content); }

    public static OreoFile file(String path) { return new OreoFile(Path.of(path)); }
    public static OreoFile file(Path path) { return new OreoFile(path); }

    public static Result<Void> tryRun(ThrowingRunnable action) { return Result.run(action); }
    public static <T> Result<T> tryGet(ThrowingSupplier<T> action) { return Result.of(action); }
    public static Attempt<Void> attempt(ThrowingRunnable action) { return Attempt.run(action); }
    public static <T> Attempt<T> attempt(ThrowingSupplier<T> action) { return Attempt.of(action); }

    public static <T> T retry(int attempts, ThrowingSupplier<T> action) {
        return Attempts.retry(attempts, Duration.ZERO, action);
    }

    public static <T> T retry(int attempts, Duration delay, ThrowingSupplier<T> action) {
        return Attempts.retry(attempts, delay, action);
    }

    public static Retry retry(int attempts) { return new Retry(attempts); }

    public static <T> Requirement<T> require(T value) { return new Requirement<>(value, "value"); }
    public static <T> Requirement<T> require(String name, T value) { return new Requirement<>(value, name); }
    public static Validation validate() { return new Validation(); }
    public static <T> ValueCheck<T> is(T value) { return new ValueCheck<>(value); }
    public static <T extends Number & Comparable<T>> NumberCheck<T> number(T value) {
        return new NumberCheck<>(value);
    }

    public static long timed(String label, Runnable action) {
        long millis = Stopwatch.measure(action);
        out(label, "took", millis + "ms");
        return millis;
    }

    public static <T> T timed(String label, Supplier<T> action) {
        Stopwatch sw = Stopwatch.start();
        T result = action.get();
        out(label, "took", sw.millis() + "ms");
        return result;
    }

    public static void sleep(long millis) { Tasks.sleep(millis); }
    public static CompletableFuture<Void> async(Runnable action) { return Tasks.async(action); }
    public static <T> CompletableFuture<T> async(Supplier<T> action) { return Tasks.async(action); }
    public static Delay waitFor(long amount) { return new Delay(amount); }
    public static AsyncTask<Void> runAsync(Runnable action) { return AsyncTask.run(action); }
    public static <T> AsyncTask<T> supplyAsync(Supplier<T> action) { return AsyncTask.supply(action); }
    public static Every every(long amount) { return new Every(amount); }

    public static Setting setting(String key) { return new Setting(key); }

    /** Opens a SQLite database file (needs the sqlite-jdbc driver on the classpath). */
    public static Db sqlite(String path) { return Db.sqlite(path); }
    /** Connects to any JDBC database (you supply the driver). */
    public static Db db(String jdbcUrl) { return Db.connect(jdbcUrl); }
    public static Db db(String jdbcUrl, String user, String password) {
        return Db.connect(jdbcUrl, user, password);
    }

    public static <K, V> OreoCache<K, V> cache() { return new OreoCache<>(); }

    public static <K> Cooldown<K> cooldown(Duration duration) { return new Cooldown<>(duration); }

    public static <K> Cooldown<K> cooldown(long amount, TimeUnit unit) {
        return new Cooldown<>(Duration.ofMillis(unit.toMillis(amount)));
    }
}
