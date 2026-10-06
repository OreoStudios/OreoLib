package com.oreo.lib;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** A fluent view of a {@link CompletableFuture}. */
public final class AsyncTask<T> {
    private final CompletableFuture<T> root;
    private CompletableFuture<T> completion;

    private AsyncTask(CompletableFuture<T> future) {
        this.root = future;
        this.completion = future;
    }

    public static AsyncTask<Void> run(Runnable action) {
        Checks.notNull(action, "action");
        return new AsyncTask<>(Tasks.async(action));
    }

    public static <T> AsyncTask<T> supply(Supplier<T> action) {
        Checks.notNull(action, "action");
        return new AsyncTask<>(Tasks.async(action));
    }

    public AsyncTask<T> whenDone(Runnable action) {
        Checks.notNull(action, "action");
        completion = completion.whenComplete((value, error) -> {
            if (error == null) action.run();
        });
        return this;
    }

    public AsyncTask<T> whenDone(Consumer<? super T> action) {
        Checks.notNull(action, "action");
        completion = completion.whenComplete((value, error) -> {
            if (error == null) action.accept(value);
        });
        return this;
    }

    public AsyncTask<T> whenFailed(Consumer<? super Throwable> action) {
        Checks.notNull(action, "action");
        completion = completion.whenComplete((value, error) -> {
            if (error != null) action.accept(unwrap(error));
        });
        return this;
    }

    public T await() {
        return completion.join();
    }

    public CompletableFuture<T> future() {
        return completion;
    }

    public boolean isDone() {
        return root.isDone();
    }

    public boolean cancel() {
        return root.cancel(true);
    }

    private static Throwable unwrap(Throwable error) {
        if (error instanceof CompletionException && error.getCause() != null) {
            return error.getCause();
        }
        return error;
    }
}
