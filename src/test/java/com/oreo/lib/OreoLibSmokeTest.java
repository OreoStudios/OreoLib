package com.oreo.lib;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.oreo.lib.Oreo.*;

public final class OreoLibSmokeTest {
    public static void main(String[] args) {
        check(choose(true, "yes", "no").equals("yes"), "choose");

        AtomicInteger number = new AtomicInteger();
        when(true, number::incrementAndGet);
        check(number.get() == 1, "when");

        AtomicInteger branch = new AtomicInteger();
        match("ONLINE")
                .caseOf("OFFLINE", () -> branch.set(1))
                .caseOf("ONLINE", () -> branch.set(2))
                .otherwise(() -> branch.set(3));
        check(branch.get() == 2, "match");

        AtomicInteger ranged = new AtomicInteger();
        when(75)
                .is(v -> v >= 100, () -> ranged.set(3))
                .is(v -> v >= 50, () -> ranged.set(2))
                .otherwise(() -> ranged.set(1));
        check(ranged.get() == 2, "when(value)");

        String nested = safe(new User(new Profile("Oreo")))
                .map(User::profile)
                .map(Profile::name)
                .orElse("Unknown");
        check(nested.equals("Oreo"), "safe");

        String missing = safe(new User(null))
                .map(User::profile)
                .map(Profile::name)
                .orElse("Unknown");
        check(missing.equals("Unknown"), "safe null");

        List<String> names = from(List.of("Steve", "Alex", "Elias", "Bob"))
                .where(name -> name.length() >= 5)
                .map(String::toUpperCase)
                .sorted()
                .list();
        check(names.equals(List.of("ELIAS", "STEVE")), "flow");

        Map<String, Integer> fruit = mapOf("apple", 5, "banana", 10, "orange", 4);
        check(fruit.get("banana") == 10, "mapOf");

        require("username", "Oreo").notNull().notBlank().notEmpty();
        require("age", 22).min(18).max(100);

        validate()
                .notBlank("Oreo", "username")
                .min(22, 18, "age")
                .throwIfInvalid();

        int parsed = attempt(() -> Integer.parseInt("42")).orElse(0);
        check(parsed == 42, "attempt");
        int fallback = attempt(() -> Integer.parseInt("bad")).orElse(7);
        check(fallback == 7, "attempt fallback");

        AtomicInteger tries = new AtomicInteger();
        String retried = retry(3).get(() -> {
            if (tries.incrementAndGet() < 2) throw new Exception("first try fails");
            return "ok";
        });
        check(retried.equals("ok") && tries.get() == 2, "retry builder");

        OreoCache<String, Integer> cache = Oreo.<String, Integer>cache().expireAfter(Duration.ofMinutes(1));
        AtomicInteger loads = new AtomicInteger();
        check(cache.get("x", loads::incrementAndGet) == 1, "cache first load");
        check(cache.get("x", loads::incrementAndGet) == 1 && loads.get() == 1, "cache hit");

        Cooldown<String> cooldown = Oreo.<String>cooldown(1, TimeUnit.SECONDS);
        check(cooldown.use("player"), "cooldown first use");
        check(!cooldown.use("player"), "cooldown blocked");
        cooldown.reset("player");
        check(cooldown.use("player"), "cooldown reset");

        AtomicInteger timesCount = new AtomicInteger();
        times(5, i -> timesCount.addAndGet(i));
        check(timesCount.get() == 10, "times");

        check(or(null, "fallback").equals("fallback"), "or");
        check(firstNonNull(null, null, "x", "y").equals("x"), "firstNonNull");

        out("OreoLib smoke tests passed");
    }

    private static void check(boolean condition, String name) {
        if (!condition) throw new AssertionError("Failed: " + name);
    }

    private record Profile(String name) {}
