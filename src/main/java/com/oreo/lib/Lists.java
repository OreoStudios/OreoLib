package com.oreo.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public final class Lists {
    private Lists() {}

    @SafeVarargs
    public static <T> List<T> of(T... values) {
        return new ArrayList<>(Arrays.asList(values));
    }

    public static List<Integer> range(int startInclusive, int endExclusive) {
        List<Integer> out = new ArrayList<>();
        for (int i = startInclusive; i < endExclusive; i++) out.add(i);
        return out;
    }

    public static <T> List<T> filter(Iterable<T> source, Predicate<T> predicate) {
        List<T> out = new ArrayList<>();
        for (T item : source) if (predicate.test(item)) out.add(item);
        return out;
    }

    public static <T, R> List<R> map(Iterable<T> source, Function<T, R> mapper) {
        List<R> out = new ArrayList<>();
        for (T item : source) out.add(mapper.apply(item));
        return out;
    }

    public static <T> void each(Iterable<T> source, Consumer<T> consumer) {
        for (T item : source) consumer.accept(item);
    }

    public static <T> List<T> distinct(Iterable<T> source) {
        Set<T> set = new LinkedHashSet<>();
        for (T item : source) set.add(item);
        return new ArrayList<>(set);
    }

    public static <T> T firstOr(Iterable<T> source, T fallback) {
        for (T item : source) return item;
        return fallback;
    }

    public static <T> T firstWhere(Iterable<T> source, Predicate<T> predicate, T fallback) {
        for (T item : source) if (predicate.test(item)) return item;
        return fallback;
    }
}
