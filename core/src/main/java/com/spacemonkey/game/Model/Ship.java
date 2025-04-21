package com.spacemonkey.game.Model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class Ship {

    private static final float MAX_SPEED = 600.0f;
    private static final float ACCELERATION = 800.0f;
    private static final float DECELERATION = 400.0f;
    public static final float WORLD_WIDTH = 1280;
    public static final float WORLD_HEIGHT = 720;

    // Ship properties
    private Vector2 position;
    private Vector2 velocity;
    private float width = 100.0f;
    private float height = 100.0f;
    private Texture shipTexture;

    // Health system
    private static final int MAX_HEALTH = 6;
    private int health = MAX_HEALTH;
    private HealthBar healthBar;

    public Ship() {
        this.position = new Vector2(100.0f, 100.0f);
        this.velocity = new Vector2(0.0f, 0.0f);
        shipTexture = new Texture(Gdx.files.internal("textures/monkeySpaceShip.png"));
        healthBar = new HealthBar(MAX_HEALTH, WORLD_HEIGHT);
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position.set(position);
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2 velocity) {
        this.velocity.set(velocity);
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getMaxSpeed() {
        return MAX_SPEED;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
        // Update health bar display
        for (int i = 0; i < MAX_HEALTH; i++) {
            healthBar.lifeCountActive[i] = (i < health);
        }
    }

    public float getRadius() {
        return width / 2.5f;
    }

    public void takeDamage() {
        health--;
        // Update health bar
        if (health >= 0) {
            healthBar.lifeCountActive[health] = false;
        }
    }

    public void reset() {
        position.set(100.0f, 100.0f);
        velocity.set(0.0f, 0.0f);
        health = MAX_HEALTH;
        // Reset health bar
        for (int i = 0; i < MAX_HEALTH; i++) {
            healthBar.lifeCountActive[i] = true;
        }
    }

    public void drawShip(SpriteBatch batch) {
        batch.draw(
            shipTexture,
            position.x - width/2,
            position.y - height/2,
            width, height
        );
    }

    public void drawHealthBar(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        healthBar.draw(batch, shapeRenderer, health);
    }

    public void updateMovement(Vector2 inputDirection, float delta) {
        if (inputDirection.len() > 0.0f) {
            inputDirection.nor();
            velocity.add(
                inputDirection.x * ACCELERATION * delta,
                inputDirection.y * ACCELERATION * delta
            );

            // Clamp velocity to maximum speed
            if (velocity.len() > MAX_SPEED) {
                velocity.nor().scl(MAX_SPEED);
            }
        } else {
            // Decelerate when no input
            if (velocity.len() > 0.0f) {
                Vector2 deceleration = new Vector2(velocity).nor().scl(DECELERATION * delta);

                // Make sure we don't overshoot zero
                if (deceleration.len() > velocity.len()) {
                    velocity.set(0, 0);
                } else {
                    velocity.sub(deceleration);
                }
            }
        }

        // Update position based on velocity
        position.add(
            velocity.x * delta,
            velocity.y * delta
        );

        // Keep the ship within screen bounds
        clampPositionToScreen();
    }

    private void clampPositionToScreen() {
        // Wrap around screen edges
        if (position.x < -width)
            position.x = WORLD_WIDTH;
        if (position.y < -height)
            position.y = WORLD_HEIGHT;
        if (position.x > WORLD_WIDTH + width)
            position.x = -width;
        if (position.y > WORLD_HEIGHT + height)
            position.y = -height;
    }

    public void dispose() {
        shipTexture.dispose();
    }

    // Health bar class definition
    public class HealthBar {
        private Vector2 position;
        private float width = 200.0f;
        private float height = 100.0f;
        public boolean[] lifeCountActive;
        private int max_health;
        private Texture healthbarTexture;

        public HealthBar(int max_health, float world_height) {
            this.position = new Vector2(5, world_height - height - 5);
            this.lifeCountActive = new boolean[max_health];
            this.max_health = max_health;
            for (int i = 0; i < max_health; i++) {
                this.lifeCountActive[i] = true;
            }
            healthbarTexture = new Texture(Gdx.files.internal("textures/healthbar.png"));
        }

        public void draw(SpriteBatch batch, ShapeRenderer shapeRenderer, int playerHealth) {
            // Draw health units
            float healthWidth = (width - 40.0f) / max_health;
            float healthHeight = height - 10.0f;

            batch.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

            for (int i = 0; i < playerHealth; i++) {
                if (lifeCountActive[i]) {
                    shapeRenderer.setColor(Color.RED);
                    shapeRenderer.rect(
                        position.x + 10.0f + (i * healthWidth) + (i * 5.0f),
                        position.y + 20.0f,
                        healthWidth,
                        healthHeight - height / 2
                    );
                }
            }

            shapeRenderer.end();
            batch.begin();

            // Draw health bar background
            batch.draw(
                healthbarTexture,
                position.x,
                position.y,
                width,
                height
            );
        }

        public void dispose() {
            healthbarTexture.dispose();
        }
    }
}
