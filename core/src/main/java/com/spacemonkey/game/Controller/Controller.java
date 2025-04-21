package com.spacemonkey.game.Controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.spacemonkey.game.Model.Ship;

public class Controller {
    // Dash ability parameters
    private static final float DASH_DURATION = 0.3f;
    private static final float DASH_COOLDOWN = 1.5f;

    private boolean isDashing = false;
    private float dashTimer = 0.0f;
    private float dashCooldownTimer = 0.0f;
    private boolean dashReady = true;
    private Vector2 dashDirection;
    private Vector2 dashStartPosition;
    private Vector2 dashTargetPosition;
    private float prevCooldownPercentage = 1.0f;

    // Ship reference
    private Ship ship;

    // Invulnerability tracking
    private boolean invulnerable = false;
    private float invulnerabilityTimer = 0.0f;

    public Controller(Ship ship) {
        this.ship = ship;
        this.dashDirection = new Vector2();
        this.dashStartPosition = new Vector2();
        this.dashTargetPosition = new Vector2();
    }

    public boolean isDashing() {
        return isDashing;
    }

    public boolean isInvulnerable() {
        return invulnerable;
    }

    public float getInvulnerabilityTimer() {
        return invulnerabilityTimer;
    }

    public boolean isDashReady() {
        return dashReady;
    }

    public float getDashCooldownTimer() {
        return dashCooldownTimer;
    }

    public float getDashCooldownPercentage() {
        return dashReady ? 1.0f : (1.0f - (dashCooldownTimer / DASH_COOLDOWN));
    }


    public void setPrevCooldownPercentage(float value) {
        this.prevCooldownPercentage = value;
    }

    public Vector2 getDashStartPosition() {
        return dashStartPosition;
    }

    public Vector2 getDashTargetPosition() {
        return dashTargetPosition;
    }

    public Vector2 getDashDirection() {
        return dashDirection;
    }


    public void setInvulnerable(boolean invulnerable, float duration) {
        this.invulnerable = invulnerable;
        this.invulnerabilityTimer = duration;
    }


    public void updateDash(float delta) {
        if (isDashing) {

            dashTimer += delta;

            if (dashTimer >= DASH_DURATION) {

                isDashing = false;

                ship.setPosition(dashTargetPosition);
            } else {

                float t = dashTimer / DASH_DURATION;
                ship.setPosition(new Vector2(
                    smootherLerp(dashStartPosition.x, dashTargetPosition.x, t),
                    smootherLerp(dashStartPosition.y, dashTargetPosition.y, t)
                ));


                if (t > 0.9f) {
                    ship.setVelocity(new Vector2(dashDirection).scl(ship.getMaxSpeed() * 0.7f));
                }
            }
        } else {

            if (dashReady && Gdx.input.isKeyJustPressed(Keys.SPACE)) {

                Vector2 dashDir = new Vector2(0, 0);


                if (Gdx.input.isKeyPressed(Keys.W)) dashDir.y += 1.0f;
                if (Gdx.input.isKeyPressed(Keys.S)) dashDir.y -= 1.0f;
                if (Gdx.input.isKeyPressed(Keys.A)) dashDir.x -= 1.0f;
                if (Gdx.input.isKeyPressed(Keys.D)) dashDir.x += 1.0f;


                if (dashDir.len() < 0.1f && ship.getVelocity().len() > 0.1f) {
                    dashDir.set(ship.getVelocity()).nor();
                }


                if (dashDir.len() > 0.1f) {
                    dashDirection.set(dashDir).nor();

                    // Calculate dash start and end positions
                    dashStartPosition.set(ship.getPosition());


                    float dashDistance = 300.0f;
                    dashTargetPosition.set(dashStartPosition).add(
                        dashDirection.x * dashDistance,
                        dashDirection.y * dashDistance
                    );


                    isDashing = true;
                    dashReady = false;
                    dashTimer = 0.0f;
                    dashCooldownTimer = DASH_COOLDOWN;


                    invulnerable = true;
                    invulnerabilityTimer = DASH_DURATION + 0.2f;
                }
            }


            if (!isDashing) {
                Vector2 inputDirection = new Vector2(0, 0);

                if (Gdx.input.isKeyPressed(Keys.W)) inputDirection.y += 1.0f;
                if (Gdx.input.isKeyPressed(Keys.S)) inputDirection.y -= 1.0f;
                if (Gdx.input.isKeyPressed(Keys.A)) inputDirection.x -= 1.0f;
                if (Gdx.input.isKeyPressed(Keys.D)) inputDirection.x += 1.0f;


                ship.updateMovement(inputDirection, delta);
            }
        }


        if (invulnerable) {
            invulnerabilityTimer -= delta;
            if (invulnerabilityTimer <= 0.0f) {
                invulnerable = false;
            }
        }


        if (!dashReady) {
            dashCooldownTimer -= delta;
            if (dashCooldownTimer <= 0.0f) {
                dashReady = true;
            }
        }
    }


    private float smootherLerp(float start, float end, float t) {

        t = t * t * t * (t * (t * 6 - 15) + 10);
        return start + (end - start) * t;
    }
}
