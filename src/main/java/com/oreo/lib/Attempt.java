package com.oreo.lib;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A lazy operation with readable fallback and retry configuration.
 * The operation runs once when a terminal method such as {@link #get()} is called.
 */
public final class Attempt<T> {
    private final ThrowingSupplier<T> operation;
    private int maximumAttempts = 1;
    private Duration delay = Duration.ZERO;
    private boolean resolved;
    private T value;
    private Exception error;

    private Attempt(ThrowingSupplier<T> operation) {
        this.operation = Checks.notNull(operation, "operation");
    }

    public static <T> Attempt<T> of(ThrowingSupplier<T> operation) {
        return new Attempt<>(operation);
    }

    public static Attempt<Void> run(ThrowingRunnable operation) {
        Checks.notNull(operation, "operation");
        return new Attempt<>(() -> {
            operation.run();
            return null;
        });
    }

    public Count upTo(int maximumAttempts) {
        ensureConfigurable();
        Checks.require(maximumAttempts > 0, "maximumAttempts must be > 0");
        return new Count(maximumAttempts);
    }

    public Waiting waiting(long amount) {
        ensureConfigurable();
        Checks.require(amount >= 0, "amount must be >= 0");
        return new Waiting(amount);
    }

    public boolean ok() {
        resolve();
        return error == null;
    }

    public boolean failed() {
        return !ok();
    }

    public T get() {
        resolve();
        if (error != null) throw new OreoException("Attempt failed", error);
        return value;
    }

    public T orElse(T fallback) {
        resolve();
        return error == null ? value : fallback;
    }

    public T orElseGet(ThrowingSupplier<? extends T> fallback) {
        resolve();
        if (error == null) return value;
        try {
            return fallback.get();
        } catch (Exception fallbackError) {
            throw new OreoException("Fallback failed", fallbackError);
        }
    }

    public Attempt<T> success(Consumer<? super T> consumer) {
        return onSuccess(consumer);
    }

    public Attempt<T> failure(Consumer<? super Exception> consumer) {
        return onError(consumer);
    }

    public Attempt<T> onSuccess(Consumer<? super T> consumer) {
        Checks.notNull(consumer, "consumer");
        resolve();
        if (error == null) consumer.accept(value);
        return this;
    }

    public Attempt<T> onError(Consumer<? super Exception> consumer) {
        Checks.notNull(consumer, "consumer");
        resolve();
        if (error != null) consumer.accept(error);
        return this;
    }

    public <R> Attempt<R> map(Function<? super T, ? extends R> mapper) {
