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

        int port = attempt(() -> Integer.parseInt("25565")).orElse(25565);
        out("Port:", port);

        OreoCache<String, String> cache = Oreo.<String, String>cache()
                .expireAfter(Duration.ofMinutes(10));
        out(cache.get("motd", () -> "Welcome!"));

        Cooldown<String> cooldown = Oreo.<String>cooldown(30, TimeUnit.SECONDS);
        out("Can use:", cooldown.use("player"));
    }

    private record Profile(String name) {}
    private record User(Profile profile) {}
}
