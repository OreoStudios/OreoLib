package com.oreo.lib;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class OreoFiles {
    private OreoFiles() {}

    public static String read(String path) {
        return read(Path.of(path));
    }

    public static String read(Path path) {
        try {
            return java.nio.file.Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new OreoException("Failed to read file: " + path, e);
        }
    }

    public static void write(String path, String content) {
        write(Path.of(path), content);
    }

    public static void write(Path path, String content) {
        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) java.nio.file.Files.createDirectories(parent);
            java.nio.file.Files.writeString(path, content, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new OreoException("Failed to write file: " + path, e);
        }
    }

    public static void append(String path, String content) {
        append(Path.of(path), content);
    }

    public static void append(Path path, String content) {
        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) java.nio.file.Files.createDirectories(parent);
            java.nio.file.Files.writeString(path, content, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new OreoException("Failed to append file: " + path, e);
        }
    }

    public static boolean exists(String path) {
        return java.nio.file.Files.exists(Path.of(path));
    }

    public static void deleteIfExists(String path) {
        try {
            java.nio.file.Files.deleteIfExists(Path.of(path));
        } catch (IOException e) {
            throw new OreoException("Failed to delete file: " + path, e);
        }
    }
}
