package com.oreo.lib.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

/**
 * Readable wrapper over a libGDX {@link FileHandle} for save files.
 *
 * <pre>{@code
 * Save.local("world.json").writeText(json);
 *
 * String json = Save.local("world.json").readText("{}");
 * }</pre>
 */
public final class Save {
    private final FileHandle file;

    private Save(FileHandle file) {
        this.file = file;
    }

    /** A file under the local storage path (recommended for saves). */
    public static Save local(String path) {
        return new Save(Gdx.files.local(path));
    }

    /** A file under the user's external/home storage path. */
    public static Save external(String path) {
        return new Save(Gdx.files.external(path));
    }

    public static Save of(FileHandle file) {
        return new Save(file);
    }

    public boolean exists() {
        return file.exists();
    }

    public Save writeText(String text) {
        file.writeString(text, false);
        return this;
    }

    public Save appendText(String text) {
        file.writeString(text, true);
        return this;
    }

    public String readText(String fallback) {
        return file.exists() ? file.readString() : fallback;
    }

    public Save delete() {
        file.delete();
        return this;
    }

    public FileHandle handle() {
        return file;
    }
}
