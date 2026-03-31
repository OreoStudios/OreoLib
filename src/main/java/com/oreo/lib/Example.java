package com.oreo.lib;

import java.time.Duration;
import java.util.List;

import static com.oreo.lib.Oreo.*;

public final class Example {
    public static void main(String[] args) {
        String clean = text("  hello Oreo Lib  ")
                .trim()
                .camelCase()
                .get();

        List<String> names = list("Elias", "Alex", "Emma", "Elias");
        List<String> shortNames = where(Lists.distinct(names), name -> name.length() <= 5);

        write("build/example.txt", "Hello from OreoLib!\n");
        append("build/example.txt", "camelCase = " + clean + "\n");

        String content = tryGet(() -> read("build/example.txt"))
                .orElse("Could not read file");

        each(shortNames, Console::println);
        Console.println(content);
