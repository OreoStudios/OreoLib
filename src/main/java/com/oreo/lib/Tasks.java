package com.oreo.lib;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public final class Tasks {
    private Tasks() {}

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OreoException("Sleep interrupted", e);
        }
    }

    public static CompletableFuture<Void> async(Runnable action) {
        return CompletableFuture.runAsync(action);
    }

    public static <T> CompletableFuture<T> async(Supplier<T> action) {
        return CompletableFuture.supplyAsync(action);
    }
}
