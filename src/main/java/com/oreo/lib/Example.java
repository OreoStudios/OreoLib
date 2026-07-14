package com.oreo.lib;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.oreo.lib.Oreo.*;

public final class Example {
    public static void main(String[] args) {
        out("OreoLib 1.1.0");

        int score = 75;
        when(score)
                .is(v -> v >= 100, () -> out("Legend"))
                .is(v -> v >= 50, () -> out("Pro"))
                .otherwise(() -> out("Beginner"));

        String status = "ONLINE";
        match(status)
                .caseOf("ONLINE", () -> success("Online"))
                .caseOf("OFFLINE", () -> warn("Offline"))
                .otherwise(() -> error("Unknown"));

        List<String> names = from(list("Steve", "Alex", "Elias", "Bob"))
                .where(name -> name.length() >= 5)
                .map(String::toUpperCase)
                .sorted()
                .list();
        out(names);

        String nested = safe(new User(new Profile("Oreo")))
                .map(User::profile)
                .map(Profile::name)
                .orElse("Unknown");
        out(nested);
