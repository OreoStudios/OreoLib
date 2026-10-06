package com.oreo.lib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

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

    public Flow<T> reversed() {
        List<T> out = new ArrayList<>(values);
        Collections.reverse(out);
        return new Flow<>(out, false);
    }

    public T firstOr(T fallback) {
        return values.isEmpty() ? fallback : values.get(0);
    }

    public T lastOr(T fallback) {
        return values.isEmpty() ? fallback : values.get(values.size() - 1);
    }

    public long count() {
        return values.size();
    }

    public long countWhere(Predicate<? super T> predicate) {
        long total = 0;
        for (T item : values) if (predicate.test(item)) total++;
        return total;
    }

    /** Sums an int property across every element. */
    public int sumInt(ToIntFunction<? super T> property) {
        int total = 0;
        for (T item : values) total += property.applyAsInt(item);
        return total;
    }

    /** Sums a double property across every element. */
    public double sumDouble(ToDoubleFunction<? super T> property) {
        double total = 0;
        for (T item : values) total += property.applyAsDouble(item);
        return total;
    }

    public T maxBy(Comparator<? super T> comparator, T fallback) {
        T best = null;
        for (T item : values) if (best == null || comparator.compare(item, best) > 0) best = item;
        return best == null ? fallback : best;
    }

    public T minBy(Comparator<? super T> comparator, T fallback) {
        T best = null;
        for (T item : values) if (best == null || comparator.compare(item, best) < 0) best = item;
        return best == null ? fallback : best;
    }

    /** Joins the elements' text with a separator. */
    public String join(String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(separator);
            sb.append(values.get(i));
        }
        return sb.toString();
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

    public List<T> toList() {
        return list();
    }

    public Set<T> toSet() {
        return new LinkedHashSet<>(values);
    }
}
