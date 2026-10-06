package com.oreo.lib.gdx;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

/**
 * Readable wrapper over libGDX {@link AssetManager}. Queue assets, load them,
 * then fetch them by path.
 *
 * <pre>{@code
 * Assets assets = new Assets()
 *     .queueTexture("player.png")
 *     .queueSound("jump.ogg")
 *     .finishLoading();
 *
 * Texture player = assets.texture("player.png");
 * }</pre>
 */
public final class Assets implements AutoCloseable {
    private final AssetManager manager;

    public Assets() {
        this(new AssetManager());
    }

    public Assets(AssetManager manager) {
        this.manager = manager;
    }

    public Assets queue(String path, Class<?> type) { manager.load(path, type); return this; }
    public Assets queueTexture(String path) { manager.load(path, Texture.class); return this; }
    public Assets queueSound(String path) { manager.load(path, Sound.class); return this; }
    public Assets queueMusic(String path) { manager.load(path, Music.class); return this; }
    public Assets queueFont(String path) { manager.load(path, BitmapFont.class); return this; }

    /** Blocks until every queued asset is loaded. */
    public Assets finishLoading() { manager.finishLoading(); return this; }

    /** Advances async loading; returns true when everything is loaded. */
    public boolean update() { return manager.update(); }

    public float progress() { return manager.getProgress(); }
    public boolean isLoaded(String path) { return manager.isLoaded(path); }

    public Texture texture(String path) { return manager.get(path, Texture.class); }
    public Sound sound(String path) { return manager.get(path, Sound.class); }
    public Music music(String path) { return manager.get(path, Music.class); }
    public BitmapFont font(String path) { return manager.get(path, BitmapFont.class); }
    public <T> T get(String path, Class<T> type) { return manager.get(path, type); }

    public Assets unload(String path) { manager.unload(path); return this; }

    public AssetManager manager() { return manager; }

    @Override
    public void close() { manager.dispose(); }
}
