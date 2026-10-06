package com.oreo.lib;

import java.util.List;

import static com.oreo.lib.Oreo.*;

public final class Example {
    public static void main(String[] args) {
        out("OreoLib 1.2.0");

        boolean serverOnline = true;
        when(serverOnline)
                .then(() -> success("Server is online"))
                .otherwise(() -> warn("Server is offline"));

        repeat(3).times(() -> out("Welcome!"));

        List<String> names = from(list("Steve", "Alex", "Elias", "Bob"))
                .where(name -> name.length() >= 5)
                .map(String::toUpperCase)
                .sorted()
                .toList();
        out(names);

        int port = setting("port").asInteger().orElse(25565);
        out("Port:", port);

        String data = attempt(Example::loadData)
                .upTo(3).times()
                .waiting(250).milliseconds()
                .orElse("Unavailable");
        out(data);

        out("Valid level:", number(10).isBetween(1, 100));
        out("Random name:", chooseOneFrom(names));
    }

    private static String loadData() {
        return "Ready";
    }
}
