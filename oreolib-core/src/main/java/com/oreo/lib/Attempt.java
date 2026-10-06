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
        Checks.notNull(mapper, "mapper");
        return Attempt.of(() -> mapper.apply(get()));
    }

    public Optional<T> optional() {
        resolve();
        return error == null ? Optional.ofNullable(value) : Optional.empty();
    }

    public Exception error() {
        resolve();
        return error;
    }

    private synchronized void resolve() {
        if (resolved) return;

        for (int currentAttempt = 1; currentAttempt <= maximumAttempts; currentAttempt++) {
            try {
                value = operation.get();
                error = null;
                resolved = true;
                return;
            } catch (Exception failure) {
                error = failure;
                if (currentAttempt < maximumAttempts && !delay.isZero()) {
                    Tasks.sleep(delay.toMillis());
                }
            }
        }

        resolved = true;
    }

    private void ensureConfigurable() {
        if (resolved) throw new IllegalStateException("attempt has already run");
    }

    public final class Count {
        private final int count;

        private Count(int count) {
            this.count = count;
        }

        public Attempt<T> times() {
            ensureConfigurable();
            maximumAttempts = count;
            return Attempt.this;
        }
    }

    public final class Waiting {
        private final long amount;

        private Waiting(long amount) {
            this.amount = amount;
        }

        public Attempt<T> milliseconds() {
            return using(Duration.ofMillis(amount));
        }

        public Attempt<T> seconds() {
            return using(Duration.ofSeconds(amount));
        }

        public Attempt<T> minutes() {
            return using(Duration.ofMinutes(amount));
        }

        private Attempt<T> using(Duration duration) {
            ensureConfigurable();
            delay = duration;
            return Attempt.this;
        }
    }
}
