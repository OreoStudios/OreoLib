package com.oreo.lib.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Readable wrapper over libGDX {@link Preferences} for settings, high scores
 * and small save data. Remember to {@link #save()} after writing.
 *
 * <pre>{@code
 * Prefs settings = Prefs.named("settings");
 * settings.put("volume", 0.8f).put("name", "Alex").save();
 *
 * float volume = settings.getFloat("volume", 1f);
 * }</pre>
 */
public final class Prefs {
    private final Preferences preferences;

    private Prefs(Preferences preferences) {
        this.preferences = preferences;
    }

    public static Prefs named(String name) {
        return new Prefs(Gdx.app.getPreferences(name));
    }

    public static Prefs of(Preferences preferences) {
        return new Prefs(preferences);
    }

    public Prefs put(String key, String value) { preferences.putString(key, value); return this; }
    public Prefs put(String key, int value) { preferences.putInteger(key, value); return this; }
    public Prefs put(String key, long value) { preferences.putLong(key, value); return this; }
    public Prefs put(String key, float value) { preferences.putFloat(key, value); return this; }
    public Prefs put(String key, boolean value) { preferences.putBoolean(key, value); return this; }

    public String getString(String key, String fallback) { return preferences.getString(key, fallback); }
    public int getInt(String key, int fallback) { return preferences.getInteger(key, fallback); }
    public long getLong(String key, long fallback) { return preferences.getLong(key, fallback); }
    public float getFloat(String key, float fallback) { return preferences.getFloat(key, fallback); }
    public boolean getBoolean(String key, boolean fallback) { return preferences.getBoolean(key, fallback); }

    public boolean has(String key) { return preferences.contains(key); }
    public Prefs remove(String key) { preferences.remove(key); return this; }
    public Prefs clear() { preferences.clear(); return this; }

    /** Persists the changes to disk. */
    public void save() { preferences.flush(); }

    public Preferences raw() { return preferences; }
}
