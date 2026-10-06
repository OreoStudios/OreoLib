package com.oreo.lib;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Fluent UTF-8 file operations. */
public final class OreoFile {
    private final Path path;

    OreoFile(Path path) {
        this.path = Checks.notNull(path, "path");
    }

    public Result<String> readText() {
        return Result.of(() -> OreoFiles.read(path));
    }

    public OreoFile writeText(String content) {
        OreoFiles.write(path, content);
        return this;
    }

    public OreoFile appendText(String content) {
        OreoFiles.append(path, content);
        return this;
    }

    public boolean exists() {
        return Files.exists(path);
    }

    public boolean delete() {
        try {
            return Files.deleteIfExists(path);
        } catch (IOException error) {
            throw new OreoException("Failed to delete file: " + path, error);
        }
    }

    public Path path() {
        return path;
    }
}
