package com.spacemonkey.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;

public class SpaceMonkeyGame extends ApplicationAdapter {
    private Game game;

    @Override
    public void create() {
        game = new Game();
        game.create();
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        game.render(deltaTime);
    }

    @Override
    public void resize(int width, int height) {
        game.resize(width, height);
    }

    @Override
    public void dispose() {
        game.dispose();
    }
}
