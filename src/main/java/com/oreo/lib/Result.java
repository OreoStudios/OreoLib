package com.oreo.lib;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Result<T> {
    private final T value;
    private final Exception error;

    private Result(T value, Exception error) {
        this.value = value;
        this.error = error;
    }

    public static <T> Result<T> of(ThrowingSupplier<T> supplier) {
        try {
            return new Result<>(supplier.get(), null);
        } catch (Exception e) {
            return new Result<>(null, e);
        }
    }

    public static Result<Void> run(ThrowingRunnable runnable) {
        try {
            runnable.run();
            return new Result<>(null, null);
        } catch (Exception e) {
            return new Result<>(null, e);
        }
    }

    public boolean ok() {
        return error == null;
    }

    public boolean failed() {
        return error != null;
    }

    public T get() {
        if (error != null) throw new OreoException("Result contains an error", error);
        return value;
    }

    public T orElse(T fallback) {
        return ok() ? value : fallback;
    }

    public T orElseGet(ThrowingSupplier<T> fallback) {
        if (ok()) return value;
        try {
            return fallback.get();
        } catch (Exception e) {
            throw new OreoException("Fallback failed", e);
        }
    }

    public Result<T> success(Consumer<T> consumer) {
        return onSuccess(consumer);
    }

    public Result<T> failure(Consumer<Exception> consumer) {
        return onError(consumer);
    }

    public Result<T> onSuccess(Consumer<T> consumer) {
        if (ok()) consumer.accept(value);
        return this;
    }

    public Result<T> onError(Consumer<Exception> consumer) {
        if (failed()) consumer.accept(error);
        return this;
    }

    public <R> Result<R> map(Function<T, R> mapper) {
        if (failed()) return new Result<>(null, error);
        try {
            return new Result<>(mapper.apply(value), null);
        } catch (Exception e) {
            return new Result<>(null, e);
        }
    }

    public Optional<T> optional() {
        return ok() ? Optional.ofNullable(value) : Optional.empty();
    }

    public Exception error() {
        return error;
    }
}
