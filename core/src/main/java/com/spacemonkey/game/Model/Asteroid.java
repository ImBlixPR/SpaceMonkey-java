package com.spacemonkey.game.Model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.spacemonkey.game.View.GameRenderer;


public class Asteroid {
    public Vector2 position;
    private Vector2 velocity;
    private float rotationSpeed;
    private float rotation;
    public float size;
    private Texture asteroidTexture;
    private float accelaration;

    public Asteroid(String Type, float accelaration) {
        this.position = new Vector2();
        this.velocity = new Vector2();
        this.rotationSpeed = 0.0f;
        this.rotation = 0.0f;
        this.size = 0.0f;
        this.accelaration = accelaration;
        asteroidTexture = new Texture(Gdx.files.internal(Type));
    }

    public Vector2 getPosition() {
        return position;
    }
    public void setVelocity(Vector2 vel) {
        velocity.set(vel).scl(accelaration);
    }

    public  void setRotationSpeed(float rotSpeed)
    {
        rotationSpeed = rotSpeed;
    }

    public void setPosition(Vector2 position) {
        this.position.set(position);
    }

    public void update(float dt) {
        position.add(velocity.x * dt, velocity.y * dt);
        rotation += rotationSpeed * dt;
    }

    public void draw(GameRenderer renderer) {
        //
        renderer.renderAdvance(
            asteroidTexture,
            position.x - size/2,
            position.y - size/2,
            size,
            rotation * MathUtils.radiansToDegrees
        );


    }
    public void dispose(){
        asteroidTexture.dispose();
    }
}
