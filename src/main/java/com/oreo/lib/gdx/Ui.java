package com.oreo.lib.gdx;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/**
 * Small Scene2D builders that trim the usual boilerplate.
 *
 * <pre>{@code
 * Table menu = Ui.table().top();
 * menu.add(Ui.label("Pause", skin)).row();
 * menu.add(Ui.button("Resume", skin, this::resume)).row();
 * }</pre>
 */
public final class Ui {
    private Ui() {}

    /** A new table that fills its parent (common for full-screen menus). */
    public static Table table() {
        Table table = new Table();
        table.setFillParent(true);
        return table;
    }

    public static Label label(String text, Skin skin) {
        return new Label(text, skin);
    }

    public static TextButton button(String text, Skin skin) {
        return new TextButton(text, skin);
    }

    /** A button that runs the given action when clicked. */
    public static TextButton button(String text, Skin skin, Runnable onClick) {
        TextButton button = new TextButton(text, skin);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onClick.run();
            }
        });
        return button;
    }

    /** Attaches a click action to any actor and returns it. */
    public static <T extends Actor> T onClick(T actor, Runnable onClick) {
        actor.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor source) {
                onClick.run();
            }
        });
        return actor;
    }
}
