package com.oreo.lib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class Text {
    private final String value;

    private Text(String value) {
        this.value = value == null ? "" : value;
    }

    public static Text of(String value) {
        return new Text(value);
    }

    public Text trim() {
        return new Text(value.trim());
    }

    public Text lower() {
        return new Text(value.toLowerCase(Locale.ROOT));
    }

    public Text upper() {
        return new Text(value.toUpperCase(Locale.ROOT));
    }

    public Text capitalize() {
        if (value.isEmpty()) return this;
        return new Text(Character.toUpperCase(value.charAt(0)) + value.substring(1));
    }

    public Text repeat(int times) {
        Checks.require(times >= 0, "times must be >= 0");
        return new Text(value.repeat(times));
    }

    public Text replace(String target, String replacement) {
        return new Text(value.replace(target, replacement));
    }

    public Text remove(String target) {
        return replace(target, "");
    }

    public Text before(String token) {
        int index = value.indexOf(token);
        return index < 0 ? this : new Text(value.substring(0, index));
    }

    public Text after(String token) {
        int index = value.indexOf(token);
        return index < 0 ? new Text("") : new Text(value.substring(index + token.length()));
    }

    public Text defaultIfBlank(String fallback) {
        return value.isBlank() ? new Text(fallback) : this;
    }

    public Text snakeCase() {
        return new Text(toSeparatedCase('_'));
    }

    public Text kebabCase() {
        return new Text(toSeparatedCase('-'));
    }

    public Text camelCase() {
        List<String> words = words();
        if (words.isEmpty()) return new Text("");
        StringBuilder out = new StringBuilder(words.get(0).toLowerCase(Locale.ROOT));
        for (int i = 1; i < words.size(); i++) {
            String word = words.get(i).toLowerCase(Locale.ROOT);
            if (!word.isEmpty()) out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return new Text(out.toString());
    }

    public boolean isBlank() {
        return value.isBlank();
    }

    public boolean containsIgnoreCase(String other) {
        return value.toLowerCase(Locale.ROOT).contains(other.toLowerCase(Locale.ROOT));
    }

    public List<String> split(String regex) {
        if (value.isEmpty()) return Collections.emptyList();
        return List.of(value.split(regex));
    }

    public String get() {
        return value;
    }

    private String toSeparatedCase(char separator) {
        List<String> words = words();
