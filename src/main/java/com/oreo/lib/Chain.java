package com.oreo.lib;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/** Fluent value pipeline inspired by Kotlin/JavaScript-style chaining. */
public final class Chain<T> {
    private final T value;

    private Chain(T value) {
        this.value = value;
    }

    public static <T> Chain<T> of(T value) {
        return new Chain<>(value);
    }

    public <R> Chain<R> map(Function<T, R> mapper) {
        return new Chain<>(mapper.apply(value));
    }

    public Chain<T> tap(Consumer<T> consumer) {
        consumer.accept(value);
        return this;
    }

    public Chain<T> when(Predicate<T> condition, Consumer<T> consumer) {
        if (condition.test(value)) consumer.accept(value);
        return this;
    }

    public T get() {
        return value;
    }
}
