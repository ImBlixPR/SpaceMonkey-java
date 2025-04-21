package com.spacemonkey.game.View;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.badlogic.gdx.utils.viewport.Viewport;



public class GameRenderer {
    private SpriteBatch batch;


    public GameRenderer( SpriteBatch batch) {

        this.batch = batch;
    }

    public void render(Texture texture, float x, float y, float width, float height) {

        batch.draw(texture, x, y, width, height);

    }

    public void renderAdvance(Texture texture, float x, float y,float size, float rotation) {

        batch.draw(
            texture,
            x,
            y,
            size/2, size/2,
            size, size,
            1, 1,
            rotation,
            0, 0,
            texture.getWidth(),
            texture.getHeight(),
            false, false
        );

    }


    public void dispose() {
        batch.dispose();
    }
}

