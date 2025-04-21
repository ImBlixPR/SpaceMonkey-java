package com.spacemonkey.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.spacemonkey.game.Model.*;
import com.spacemonkey.game.Controller.Controller;
import com.spacemonkey.game.View.GameRenderer;

public class Game {
    public static final float WORLD_WIDTH = 1280;
    public static final float WORLD_HEIGHT = 720;

    private Texture bgTexture;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private Viewport viewport;

    private Texture defeatedTexture;
    private Texture victoryTexture;
    private Texture startTexture;
    private Texture exitTexture;
    private Texture spaceMonkeyBanerTexture;

    private GameRenderer renderer;

    private boolean running = false;
    private boolean gameOver = false;
    private boolean firstLaunch = true;

    private double timePass = 0.0;

    private Ship ship;
    private Controller controller;

    private float pulseTimer = 0.0f;
    private float prevCooldownPercentage = 1.0f;

    private AsteroidFactory asteroidFactory;
    private static final int MAX_ASTEROIDS = 60;
    private static final float ASTEROID_SPAWN_TIMER_MAX = 1.0f;
    private float asteroidSpawnTimer = 0.0f;
    private Array<Asteroid> asteroids;

    private FruitFactory fruitFactory;
    private static final int WIN_FRUIT_NUMBER = 30;
    private static final int MAX_FRUIT = 6;
    private static final float FRUIT_SPAWN_TIMER_MAX = 1.0f;
    private float fruitSpawnTimer = 0.0f;
    private Array<Fruit> fruitPool;
    private int activeFruitCount = 0;
    private int collectedFruit = 0;

    private float ui_sizeWidth = 300;
    private float ui_sizeHeight = 150;
    private Vector2 startPos;
    private Vector2 exitPos;
    private com.badlogic.gdx.graphics.g2d.BitmapFont font;

    private static final float KEY_DISPLAY_ALPHA = 0.8f;
    private static final float KEY_ICON_SIZE = 25.0f;

    public Game() {
        this.asteroids = new Array<>(MAX_ASTEROIDS);
        this.fruitPool = new Array<>(MAX_FRUIT + 5);
        this.startPos = new Vector2(WORLD_WIDTH / 2 - ui_sizeHeight, WORLD_HEIGHT / 2 - 130);
        this.exitPos = new Vector2(WORLD_WIDTH / 2 - ui_sizeHeight, WORLD_HEIGHT / 2 - 300);
    }

    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply();
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);

        defeatedTexture = new Texture(Gdx.files.internal("textures/defeated.png"));
        victoryTexture = new Texture(Gdx.files.internal("textures/victory.png"));
        startTexture = new Texture(Gdx.files.internal("textures/start.png"));
        exitTexture = new Texture(Gdx.files.internal("textures/exit.png"));
        spaceMonkeyBanerTexture = new Texture(Gdx.files.internal("textures/ui_baner2.png"));
        bgTexture = new Texture(Gdx.files.internal("textures/bg.png"));

        font = new com.badlogic.gdx.graphics.g2d.BitmapFont();
        font.getData().setScale(1.5f);

        ship = new Ship();
        controller = new Controller(ship);

        init();
    }

    public boolean init() {
        if (firstLaunch) {
            pulseTimer = 0.0f;
            firstLaunch = false;
            renderer = new GameRenderer(batch);
            asteroidFactory = new AsteroidFactory();
            fruitFactory = new FruitFactory();
        }

        ship.reset();
        gameOver = false;
        collectedFruit = 0;
        activeFruitCount = 0;
        asteroids.clear();

        return true;
    }

    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        if (!running) {
            renderMenu(delta);
        } else {
            runGame(delta);
        }
    }

    private void renderMenu(float delta) {
        batch.begin();
        batch.end();

        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(new Color(118.0f/255.0f, 66.0f/255.0f, 138.0f/255.0f, 1));
        shapeRenderer.circle(0.0f, 0.0f, 700.0f, 20);
        shapeRenderer.setColor(new Color(203.0f/255.0f, 211.0f/255.0f, 252.0f/255.0f, 1));
        shapeRenderer.circle(WORLD_WIDTH, WORLD_HEIGHT / 2, 400.0f, 20);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(WORLD_WIDTH / 2, 0.0f, 400.0f, 20);
        shapeRenderer.end();

        batch.begin();
        batch.draw(spaceMonkeyBanerTexture, WORLD_WIDTH / 2 - 300.0f, WORLD_HEIGHT - 305.0f, 600.0f, 300.0f);

        float startScale = 1.0f;
        if (checkMouseCollision(startPos, ui_sizeWidth, ui_sizeHeight)) {
            startScale = 1.05f;
            if (Gdx.input.justTouched()) {
                running = true;
            }
        }
        batch.draw(startTexture, startPos.x, startPos.y, ui_sizeWidth * startScale, ui_sizeHeight * startScale);

        float exitScale = 1.0f;
        if (checkMouseCollision(exitPos, ui_sizeWidth, ui_sizeHeight)) {
            exitScale = 1.05f;
            if (Gdx.input.justTouched()) {
                Gdx.app.exit();
            }
        }
        batch.draw(exitTexture, exitPos.x, exitPos.y, ui_sizeWidth * exitScale, ui_sizeHeight * exitScale);

        batch.end();
    }

    private void runGame(float delta) {
        Gdx.input.setCursorCatched(true);
        timePass += delta;


        if (Gdx.input.isKeyJustPressed(Keys.M)) {
            running = false;
            clear();
            init();
            Gdx.input.setCursorCatched(false);
            return;
        }

        if(Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        batch.begin();
        renderer.render(bgTexture, 0.0f, 0.0f, WORLD_WIDTH, WORLD_HEIGHT);
        if (gameOver) {
            float messageWidth = 400;
            float messageHeight = 200;
            float centerX = WORLD_WIDTH / 2;
            float centerY = WORLD_HEIGHT / 2;

            batch.draw(defeatedTexture,
                centerX - messageWidth / 2,
                centerY - messageHeight / 2,
                messageWidth, messageHeight);

            drawEndScreenInstructions(centerX, centerY, messageWidth);

            if (Gdx.input.isKeyJustPressed(Keys.R)) {
                clear();
                init();
            }

            batch.end();
            return;
        }

        if (collectedFruit >= WIN_FRUIT_NUMBER) {
            float messageWidth = 400;
            float messageHeight = 200;
            float centerX = WORLD_WIDTH / 2;
            float centerY = WORLD_HEIGHT / 2;

            batch.draw(victoryTexture,
                centerX - messageWidth / 2,
                centerY - messageHeight / 2,
                messageWidth, messageHeight);

            drawEndScreenInstructions(centerX, centerY, messageWidth);

            if (Gdx.input.isKeyJustPressed(Keys.R)) {
                clear();
                init();
            }

            batch.end();
            return;
        }

        pulseTimer += delta;
        if (pulseTimer > MathUtils.PI2) {
            pulseTimer = 0.0f;
        }

        drawKeyInstructions();
        controller.updateDash(delta);
        updateAsteroids(delta);
        updateFruits(delta);
        drawDashEffect();

        if (!controller.isInvulnerable() || (int)(controller.getInvulnerabilityTimer() * 10.0f) % 2 == 0) {
            ship.drawShip(batch);
        }

        ship.drawHealthBar(batch, shapeRenderer);
        drawScoreCounter();
        drawDashCooldown();

        batch.end();
    }

    private void drawScoreCounter() {
        float scoreWidth = 150.0f;
        float scoreHeight = 40.0f;
        float scoreX = WORLD_WIDTH / 2 - scoreWidth / 2;
        float scoreY = WORLD_HEIGHT - scoreHeight - 10.0f;

        batch.end();

        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(0.0f, 0.0f, 0.0f, 0.7f);
        shapeRenderer.rect(scoreX + 3, scoreY - 3, scoreWidth, scoreHeight);

        shapeRenderer.setColor(0.2f, 0.2f, 0.4f, 0.9f);
        shapeRenderer.rect(scoreX, scoreY, scoreWidth, scoreHeight);

        shapeRenderer.setColor(0.5f, 0.5f, 0.8f, 1.0f);
        shapeRenderer.rect(scoreX + 2, scoreY + 2, scoreWidth - 4, scoreHeight - 4,
            new Color(0.5f, 0.5f, 0.8f, 1.0f),
            new Color(0.3f, 0.3f, 0.6f, 1.0f),
            new Color(0.3f, 0.3f, 0.6f, 1.0f),
            new Color(0.5f, 0.5f, 0.8f, 1.0f));

        shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 0.7f);
        shapeRenderer.rect(scoreX + 10, scoreY + 30, scoreWidth - 20, 4);

        float progressWidth = (scoreWidth - 20) * ((float)collectedFruit / WIN_FRUIT_NUMBER);
        shapeRenderer.setColor(0.2f, 0.9f, 0.3f, 1.0f);
        shapeRenderer.rect(scoreX + 10, scoreY + 30, progressWidth, 4);

        shapeRenderer.end();

        batch.begin();

        String scoreText = "FRUITS: " + collectedFruit + "/" + WIN_FRUIT_NUMBER;
        font.setColor(1.0f, 1.0f, 1.0f, 1.0f);

        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, scoreText);
        font.draw(batch, scoreText,
            scoreX + (scoreWidth/2) - (layout.width/2),
            scoreY + (scoreHeight/2) + (layout.height/2));
    }

    private void drawKeyInstructions() {
        float startY = 260.0f;
        float leftX = 1060.0f;

        batch.end();
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.1f, 0.2f, KEY_DISPLAY_ALPHA);

        float boxWidth = 200.0f;
        float boxHeight = 240.0f;

        shapeRenderer.rect(
            leftX, startY - boxHeight + KEY_ICON_SIZE,
            boxWidth, boxHeight,
            new Color(0.2f, 0.2f, 0.3f, KEY_DISPLAY_ALPHA),
            new Color(0.2f, 0.2f, 0.3f, KEY_DISPLAY_ALPHA),
            new Color(0.1f, 0.1f, 0.15f, KEY_DISPLAY_ALPHA),
            new Color(0.1f, 0.1f, 0.15f, KEY_DISPLAY_ALPHA)
        );

        shapeRenderer.setColor(0.4f, 0.4f, 0.6f, KEY_DISPLAY_ALPHA);
        shapeRenderer.rect(leftX, startY - boxHeight + KEY_ICON_SIZE, boxWidth, 2);
        shapeRenderer.rect(leftX, startY - boxHeight + KEY_ICON_SIZE, 2, boxHeight);
        shapeRenderer.rect(leftX + boxWidth - 2, startY - boxHeight + KEY_ICON_SIZE, 2, boxHeight);
        shapeRenderer.rect(leftX, startY - boxHeight + KEY_ICON_SIZE + boxHeight - 2, boxWidth, 2);

        shapeRenderer.setColor(0.3f, 0.3f, 0.5f, KEY_DISPLAY_ALPHA);
        shapeRenderer.rect(leftX, startY - boxHeight + KEY_ICON_SIZE + boxHeight - 30, boxWidth, 30);

        shapeRenderer.end();

        batch.begin();

        font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        String titleText = "CONTROLS";
        com.badlogic.gdx.graphics.g2d.GlyphLayout titleLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, titleText);
        font.draw(batch, titleText,
            leftX + (boxWidth / 2) - (titleLayout.width / 2),
            startY - boxHeight + KEY_ICON_SIZE + boxHeight - 10);

        float offsetKey = 30;
        float currentY = startY - offsetKey;

        drawKeyInstruction(leftX + 10, currentY, "W", "Move Up");
        currentY -= offsetKey;
        drawKeyInstruction(leftX + 10, currentY, "A", "Move Left");
        currentY -= offsetKey;
        drawKeyInstruction(leftX + 10, currentY, "S", "Move Down");
        currentY -= offsetKey;
        drawKeyInstruction(leftX + 10, currentY, "D", "Move Right");
        currentY -= offsetKey;
        drawKeyInstruction(leftX + 10, currentY, "SPACE", "Dash");
        currentY -= offsetKey;
        drawKeyInstruction(leftX + 10, currentY, "M", "Menu");
    }

    private void drawKeyInstruction(float x, float y, String key, String action) {
        batch.end();

        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(0.7f, 0.7f, 0.8f, KEY_DISPLAY_ALPHA);
        float keyWidth = key.length() > 1 ? KEY_ICON_SIZE * 4.0f : KEY_ICON_SIZE;
        shapeRenderer.rect(x, y - KEY_ICON_SIZE + 5, keyWidth, KEY_ICON_SIZE,
            new Color(0.8f, 0.8f, 0.9f, KEY_DISPLAY_ALPHA),
            new Color(0.8f, 0.8f, 0.9f, KEY_DISPLAY_ALPHA),
            new Color(0.6f, 0.6f, 0.7f, KEY_DISPLAY_ALPHA),
            new Color(0.6f, 0.6f, 0.7f, KEY_DISPLAY_ALPHA));

        shapeRenderer.end();

        batch.begin();

        font.setColor(0.1f, 0.1f, 0.2f, 1.0f);
        com.badlogic.gdx.graphics.g2d.GlyphLayout keyLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, key);
        font.draw(batch, key,
            x + (keyWidth / 2) - (keyLayout.width / 2),
            y - (KEY_ICON_SIZE / 2) + (keyLayout.height -4));

        font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        font.draw(batch, action, x + keyWidth + 10, y - (KEY_ICON_SIZE / 2) + (keyLayout.height / 2));
    }

    private void drawEndScreenInstructions(float centerX, float centerY, float width) {
        float instructionY = centerY - 130.0f;

        font.setColor(1.0f, 0.1f, 0.2f, 1.0f);
        String keyText = "R";
        com.badlogic.gdx.graphics.g2d.GlyphLayout keyLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, keyText);
        font.draw(batch, keyText,
            centerX - 100 + (KEY_ICON_SIZE / 2) - (keyLayout.width / 2),
            instructionY + (keyLayout.height / 2));

        font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        String actionText = "Press to Restart";
        font.draw(batch, actionText,
            centerX - 100 + KEY_ICON_SIZE + 10,
            instructionY + (keyLayout.height / 2));
    }

    private void drawDashEffect() {
        if (!controller.isDashing()) return;

        batch.end();
        shapeRenderer.begin(ShapeType.Filled);

        float dashProgress = controller.getDashCooldownPercentage();
        Vector2 dashStartPosition = controller.getDashStartPosition();
        Vector2 dashTargetPosition = controller.getDashTargetPosition();
        Vector2 dashDirection = controller.getDashDirection();

        final int trailCount = 5;

        for (int i = 1; i <= trailCount; i++) {
            float trailFactor = dashProgress - (i * 0.1f);

            if (trailFactor >= 0.0f) {
                float trailX = smootherLerp(dashStartPosition.x, dashTargetPosition.x, trailFactor);
                float trailY = smootherLerp(dashStartPosition.y, dashTargetPosition.y, trailFactor);

                float alpha = 0.9f * (1.0f - (float)i / trailCount);
                float size = ship.getWidth() * (1.0f - (0.15f * i));

                shapeRenderer.setColor(0.8f, 0.8f, 1.0f, alpha);
                shapeRenderer.circle(trailX, trailY, size/2);
            }
        }

        if (dashProgress < 0.2f) {
            float burstSize = ship.getWidth() * (1.0f + dashProgress * 2.0f);
            float burstAlpha = 0.5f * (1.0f - dashProgress * 5.0f);

            shapeRenderer.setColor(1.0f, 1.0f, 1.0f, burstAlpha);
            shapeRenderer.circle(
                dashStartPosition.x,
                dashStartPosition.y,
                burstSize/2
            );
        }

        shapeRenderer.end();
        batch.begin();
    }

    private void drawDashCooldown() {
        float targetPercentage = controller.getDashCooldownPercentage();
        prevCooldownPercentage = smoothLerp(prevCooldownPercentage, targetPercentage, 0.1f);
        controller.setPrevCooldownPercentage(prevCooldownPercentage);

        float barWidth = 100.0f;
        float barHeight = 10.0f;
        float padding = 10.0f;

        batch.end();
        shapeRenderer.begin(ShapeType.Filled);

        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1.0f);
        shapeRenderer.rect(padding, padding, barWidth, barHeight);

        Color fillColor;
        if (controller.isDashReady()) {
            fillColor = new Color(0.0f, 1.0f, 0.4f, 1.0f);
        } else {
            fillColor = new Color(
                MathUtils.lerp(1.0f, 0.0f, prevCooldownPercentage),
                MathUtils.lerp(0.5f, 1.0f, prevCooldownPercentage),
                MathUtils.lerp(0.0f, 0.4f, prevCooldownPercentage),
                1.0f
            );
        }

        shapeRenderer.setColor(fillColor);
        shapeRenderer.rect(padding, padding, barWidth * prevCooldownPercentage, barHeight);

        if (controller.isDashReady()) {
            float pulseScale = 0.1f * (MathUtils.sin(pulseTimer * 5.0f) * 0.5f + 0.5f);
            shapeRenderer.setColor(fillColor);
            shapeRenderer.rect(
                padding - 2.0f,
                padding - 2.0f,
                barWidth + 4.0f,
                barHeight + 4.0f
            );
        }

        shapeRenderer.end();
        batch.begin();
    }

    private void updateAsteroids(float delta) {
        asteroidSpawnTimer -= delta;
        if (asteroidSpawnTimer <= 0.0f) {
            spawnAsteroid();
            asteroidSpawnTimer = ASTEROID_SPAWN_TIMER_MAX;
        }

        float playerRadius = ship.getRadius();

        for (int i = asteroids.size - 1; i >= 0; i--) {
            asteroids.get(i).update(delta);

            if (!controller.isInvulnerable() && !gameOver) {
                float asteroidRadius = asteroids.get(i).size / 2.0f;
                if (checkCollision(ship.getPosition(), playerRadius, asteroids.get(i).getPosition(), asteroidRadius)) {
                    playerTakeDamage();
                }
            }

            if (isAsteroidOutOfBounds(asteroids.get(i))) {
                asteroids.removeIndex(i);
                continue;
            }

            asteroids.get(i).draw(renderer);
        }
    }

    private void updateFruits(float delta) {
        fruitSpawnTimer -= delta;
        if (activeFruitCount < MAX_FRUIT && fruitSpawnTimer <= 0.0f) {
            spawnFruit();
            fruitSpawnTimer = FRUIT_SPAWN_TIMER_MAX;
        }

        float playerRadius = ship.getRadius();

        for (int i = fruitPool.size - 1; i >= 0; i--) {
            if (!controller.isInvulnerable() && !gameOver) {
                if (checkCollision(ship.getPosition(), playerRadius, fruitPool.get(i).getPosition(), fruitPool.get(i).size / 2.0f)) {
                    int value = fruitPool.get(i).getValuse();
                    fruitPool.removeIndex(i);
                    activeFruitCount--;
                    collectedFruit += value;
                    continue;
                }
            }

            fruitPool.get(i).draw(renderer);
        }
    }

    private void spawnAsteroid() {
        if(asteroids.size < MAX_ASTEROIDS) {
            asteroids.add(asteroidFactory.CreateAstroid(timePass, WORLD_WIDTH, WORLD_HEIGHT));
        }
    }

    private void spawnFruit() {
        fruitPool.add(fruitFactory.CreateFruit(timePass, WORLD_WIDTH, WORLD_HEIGHT, ship));
        activeFruitCount++;
    }

    private boolean isAsteroidOutOfBounds(Asteroid asteroid) {
        float padding = asteroid.size;
        Vector2 pos = asteroid.getPosition();

        return (pos.x < -padding || pos.x > WORLD_WIDTH + padding ||
            pos.y < -padding || pos.y > WORLD_HEIGHT + padding);
    }

    private boolean checkCollision(Vector2 pos1, float radius1, Vector2 pos2, float radius2) {
        float minDist = radius1 + radius2;
        return pos1.dst2(pos2) <= minDist * minDist;
    }

    private boolean checkMouseCollision(Vector2 pos, float width, float height) {
        Vector2 mousePos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(mousePos);

        return (mousePos.x >= pos.x && mousePos.x <= pos.x + width &&
            mousePos.y >= pos.y && mousePos.y <= pos.y + height);
    }

    private void playerTakeDamage() {
        if (!controller.isInvulnerable()) {
            ship.takeDamage();
            controller.setInvulnerable(true, 2);

            if (ship.getHealth() <= 0) {
                gameOver = true;
            }
        }
    }

    private float smootherLerp(float start, float end, float t) {
        t = t * t * t * (t * (t * 6 - 15) + 10);
        return start + (end - start) * t;
    }

    private float smoothLerp(float start, float end, float alpha) {
        return start + (end - start) * alpha;
    }

    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        defeatedTexture.dispose();
        victoryTexture.dispose();
        startTexture.dispose();
        exitTexture.dispose();
        spaceMonkeyBanerTexture.dispose();

        ship.dispose();

        for (Fruit fruit : fruitPool) {
            fruit.dispose();
        }

        for (Asteroid asteroid : asteroids) {
            asteroid.dispose();
        }

        font.dispose();
    }

    public void clear() {
        timePass = 0.0;
        asteroids.clear();
        fruitPool.clear();
        activeFruitCount = 0;
        collectedFruit = 0;
    }

    public void resize(int width, int height) {
        viewport.update(width, height);
    }
}
