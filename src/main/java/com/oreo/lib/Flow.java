package com.oreo.lib;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/** Lightweight eager collection pipeline for concise everyday transformations. */
public final class Flow<T> {
    private final List<T> values;

    private Flow(Iterable<T> source) {
        this.values = new ArrayList<>();
        for (T item : source) values.add(item);
    }

    private Flow(List<T> values, boolean copy) {
        this.values = copy ? new ArrayList<>(values) : values;
    }

    public static <T> Flow<T> from(Iterable<T> source) {
        return new Flow<>(source);
    }

    public Flow<T> where(Predicate<? super T> predicate) {
        List<T> out = new ArrayList<>();
        for (T item : values) if (predicate.test(item)) out.add(item);
        return new Flow<>(out, false);
    }

    public <R> Flow<R> map(Function<? super T, ? extends R> mapper) {
        List<R> out = new ArrayList<>();
        for (T item : values) out.add(mapper.apply(item));
        return new Flow<>(out, false);
    }

    public Flow<T> distinct() {
        Set<T> set = new LinkedHashSet<>(values);
        return new Flow<>(new ArrayList<>(set), false);
    }

    public Flow<T> sorted(Comparator<? super T> comparator) {
        List<T> out = new ArrayList<>(values);
        out.sort(comparator);
        return new Flow<>(out, false);
    }

    @SuppressWarnings("unchecked")
    public Flow<T> sorted() {
        List<T> out = new ArrayList<>(values);
        out.sort((a, b) -> ((Comparable<Object>) a).compareTo(b));
        return new Flow<>(out, false);
    }

    public Flow<T> take(int amount) {
        if (amount <= 0) return new Flow<>(new ArrayList<>(), false);
        return new Flow<>(new ArrayList<>(values.subList(0, Math.min(amount, values.size()))), false);
    }

    public Flow<T> skip(int amount) {
        int start = Math.min(Math.max(0, amount), values.size());
        return new Flow<>(new ArrayList<>(values.subList(start, values.size())), false);
    }

    public Flow<T> each(Consumer<? super T> consumer) {
        values.forEach(consumer);
        return this;
    }

    public T firstOr(T fallback) {
        return values.isEmpty() ? fallback : values.get(0);
    }

    public long count() {
        return values.size();
    }

    public boolean any(Predicate<? super T> predicate) {
        for (T item : values) if (predicate.test(item)) return true;
        return false;
    }

    public boolean all(Predicate<? super T> predicate) {
        for (T item : values) if (!predicate.test(item)) return false;
        return true;
    }

    public List<T> list() {
        return new ArrayList<>(values);
    }
}
