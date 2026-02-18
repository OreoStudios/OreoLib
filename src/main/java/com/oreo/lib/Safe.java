package com.oreo.lib;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/** Null-safe fluent value chain. */
public final class Safe<T> {
    private final T value;

    private Safe(T value) {
        this.value = value;
    }

    public static <T> Safe<T> of(T value) {
        return new Safe<>(value);
    }

    public <R> Safe<R> map(Function<? super T, ? extends R> mapper) {
        if (value == null) return new Safe<>(null);
        return new Safe<>(mapper.apply(value));
    }

    public Safe<T> ifPresent(Consumer<? super T> consumer) {
        if (value != null) consumer.accept(value);
        return this;
    }

    public Safe<T> ifNull(Runnable action) {
        if (value == null) action.run();
        return this;
    }

    public T orElse(T fallback) {
        return value != null ? value : fallback;
    }

    public T orElseGet(Supplier<? extends T> supplier) {
        return value != null ? value : supplier.get();
    }

    public Optional<T> optional() {
        return Optional.ofNullable(value);
    }

    public T get() {
        return value;
    }
}
