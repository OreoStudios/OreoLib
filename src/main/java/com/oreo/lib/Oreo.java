package com.oreo.lib;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Main entry point for OreoLib.
 *
 * Import statically when you want very short calls:
 * import static com.oreo.lib.Oreo.*;
 */
public final class Oreo {
    private Oreo() {}


    public static void out(Object value) {
        Console.out(value);
    }

    public static void out(Object... values) {
        Console.out(values);
    }

    public static void print(Object value) {
        Console.print(value);
    }

    public static void printf(String format, Object... args) {
        Console.printf(format, args);
