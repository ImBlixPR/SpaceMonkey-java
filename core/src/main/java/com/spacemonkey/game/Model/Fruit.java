package com.spacemonkey.game.Model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.spacemonkey.game.View.GameRenderer;

public class Fruit {
    private Vector2 position;
    public float size = 50.0f;
    private Texture fruitTextures;
    private int valuse;

    public Fruit(String Type, int valuse) {
        this.position = new Vector2();
        this.valuse = valuse;
        fruitTextures = new Texture(Gdx.files.internal(Type));

    }

    public Vector2 getPosition() {
        return position;
    }
    public int getValuse()
    {
        return valuse;
    }

    public void setPosition(Vector2 position) {
        this.position.set(position);
    }

    public void draw(GameRenderer renderer) {
        renderer.render(fruitTextures, position.x - size/2,
            position.y - size/2,
            size, size);

    }

    public void dispose(){

        fruitTextures.dispose();

    }
}
