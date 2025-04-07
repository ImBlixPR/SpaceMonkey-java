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


public class Game {
    // Constants for window size
    public static final float WORLD_WIDTH = 1280;
    public static final float WORLD_HEIGHT = 720;

    // Game objects
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private Viewport viewport;

    // Asset references
    private Texture monkeyShipTexture;
    private Texture asteroidTexture;
    private Texture[] fruitTextures;
    private Texture healthbarTexture;
    private Texture defeatedTexture;
    private Texture victoryTexture;
    private Texture startTexture;
    private Texture exitTexture;
    private Texture spaceMonkeyBanerTexture;

    // Game state
    private boolean running = false;
    private boolean gameOver = false;
    private boolean firstLaunch = true;

    // Player ship properties
    private Vector2 monkeyPosition;
    private Vector2 monkeyVelocity;
    private float shipWidth = 100.0f;
    private float shipHeight = 100.0f;

    // Movement constants
    private static final float MAX_SPEED = 600.0f;
    private static final float ACCELERATION = 800.0f;
    private static final float DECELERATION = 400.0f;

    // Dash ability parameters
    private static final float DASH_FORCE = 1500.0f;
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

    // Health system
    private static final int MAX_HEALTH = 6;
    private int playerHealth = MAX_HEALTH;
    private boolean invulnerable = false;
    private float invulnerabilityTimer = 0.0f;
    private static final float INVULNERABILITY_DURATION = 2.0f;

    // Health bar
    private HealthBar healthBar;

    // Asteroid properties
    private static final int MAX_ASTEROIDS = 60;
    private static final float ASTEROID_MIN_SIZE = 30.0f;
    private static final float ASTEROID_MAX_SIZE = 120.0f;
    private static final float ASTEROID_MIN_SPEED = 500.0f;
    private static final float ASTEROID_MAX_SPEED = 200.0f;
    private static final float ASTEROID_SPAWN_TIMER_MAX = 1.0f;
    private float asteroidSpawnTimer = 0.0f;
    private Array<Asteroid> asteroids;

    // Fruit properties
    private static final int WIN_FRUIT_NUMBER = 30;
    private static final int MAX_FRUIT = 6;
    private static final float FRUIT_SPAWN_TIMER_MAX = 1.0f;
    private float fruitSpawnTimer = 0.0f;
    private static final int FRUIT_POOL_SIZE = MAX_FRUIT + 5;
    private Fruit[] fruitPool;
    private int activeFruitCount = 0;
    private int collectedFruit = 0;

    // UI properties
    private float ui_sizeWidth = 300;
    private float ui_sizeHeight = 150;
    private Vector2 startPos;
    private Vector2 exitPos;
    private float pulseTimer = 0.0f;
    private com.badlogic.gdx.graphics.g2d.BitmapFont font;

    private static final float KEY_DISPLAY_ALPHA = 0.8f;
    private static final float KEY_ICON_SIZE = 25.0f;


    // Asteroid class definition
    public class Asteroid {
        private Vector2 position;
        private Vector2 velocity;
        private float rotationSpeed;
        private float rotation;
        private float size;
        private boolean active;

        public Asteroid() {
            this.position = new Vector2();
            this.velocity = new Vector2();
            this.rotationSpeed = 0.0f;
            this.rotation = 0.0f;
            this.size = 0.0f;
            this.active = false;
        }

        public Vector2 getPosition() {
            return position;
        }

        public void setPosition(Vector2 position) {
            this.position.set(position);
        }

        public void update(float dt) {
            position.add(velocity.x * dt, velocity.y * dt);
            rotation += rotationSpeed * dt;
        }

        public void draw(SpriteBatch batch) {
            if (active) {
                batch.draw(
                    asteroidTexture,
                    position.x - size/2,
                    position.y - size/2,
                    size/2, size/2,
                    size, size,
                    1, 1,
                    rotation * MathUtils.radiansToDegrees,
                    0, 0,
                    asteroidTexture.getWidth(),
                    asteroidTexture.getHeight(),
                    false, false
                );
            }
        }
    }

    // Fruit class definition
    public class Fruit {
        private Vector2 position;
        private float size = 50.0f;
        private int type;
        private boolean active;

        public Fruit() {
            this.position = new Vector2();
            this.active = false;
        }

        public Vector2 getPosition() {
            return position;
        }

        public void setPosition(Vector2 position) {
            this.position.set(position);
        }

        public void draw(SpriteBatch batch) {
            if (active) {
                batch.draw(
                    fruitTextures[type],
                    position.x - size/2,
                    position.y - size/2,
                    size, size
                );
            }
        }
    }

    // Health bar class definition
    public class HealthBar {
        private Vector2 position;
        private float width = 200.0f;
        private float height = 100.0f;
        private boolean[] lifeCountActive;

        public HealthBar() {
            this.position = new Vector2(5, WORLD_HEIGHT - height - 5);
            this.lifeCountActive = new boolean[MAX_HEALTH];
            for (int i = 0; i < MAX_HEALTH; i++) {
                this.lifeCountActive[i] = true;
            }
        }

        public void draw(SpriteBatch batch, ShapeRenderer shapeRenderer) {
            // Draw health units
            float healthWidth = (width - 40.0f) / MAX_HEALTH;
            float healthHeight = height - 10.0f;

            batch.end();
            shapeRenderer.begin(ShapeType.Filled);

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
    }

    // Constructor
    public Game() {
        this.dashDirection = new Vector2();
        this.dashStartPosition = new Vector2();
        this.dashTargetPosition = new Vector2();
        this.monkeyPosition = new Vector2(100.0f, 100.0f);
        this.monkeyVelocity = new Vector2(0.0f, 0.0f);

        // Initialize game arrays
        this.asteroids = new Array<>(MAX_ASTEROIDS);
        this.fruitPool = new Fruit[FRUIT_POOL_SIZE];
        for (int i = 0; i < FRUIT_POOL_SIZE; i++) {
            fruitPool[i] = new Fruit();
        }

        // Initialize UI positions
        this.startPos = new Vector2(WORLD_WIDTH / 2 - ui_sizeHeight, WORLD_HEIGHT / 2 - 130);
        this.exitPos = new Vector2(WORLD_WIDTH / 2 - ui_sizeHeight, WORLD_HEIGHT / 2 - 300 );
    }

    // Initialize the game
    public void create() {
        // Create graphics objects
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply();
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);

        // Load textures
        monkeyShipTexture = new Texture(Gdx.files.internal("textures/monkeySpaceShip.png"));
        asteroidTexture = new Texture(Gdx.files.internal("textures/asteroid.png"));
        fruitTextures = new Texture[2];
        fruitTextures[0] = new Texture(Gdx.files.internal("textures/fruit1.png"));
        fruitTextures[1] = new Texture(Gdx.files.internal("textures/fruit2.png"));
        healthbarTexture = new Texture(Gdx.files.internal("textures/healthbar.png"));
        defeatedTexture = new Texture(Gdx.files.internal("textures/defeated.png"));
        victoryTexture = new Texture(Gdx.files.internal("textures/victory.png"));
        startTexture = new Texture(Gdx.files.internal("textures/start.png"));
        exitTexture = new Texture(Gdx.files.internal("textures/exit.png"));
        spaceMonkeyBanerTexture = new Texture(Gdx.files.internal("textures/ui_baner2.png"));

        // Initialize the font for score display
        font = new com.badlogic.gdx.graphics.g2d.BitmapFont();
        font.getData().setScale(1.5f); // Make the font larger

        // Create health bar
        healthBar = new HealthBar();

        // Initialize game state
        init();
    }



    // Initialize or reset the game state
    public boolean init() {
        if (firstLaunch) {
            // Reset dash state
            isDashing = false;
            dashTimer = 0.0f;
            dashCooldownTimer = 0.0f;
            dashReady = true;
            dashStartPosition.set(0.0f, 0.0f);
            dashTargetPosition.set(0.0f, 0.0f);
            prevCooldownPercentage = 1.0f;

            pulseTimer = 0.0f;

            firstLaunch = false;
        }

        // Reset ship position
        monkeyPosition.set(100.0f, 100.0f);
        monkeyVelocity.set(0.0f, 0.0f);

        // Reset game state
        playerHealth = MAX_HEALTH;
        invulnerable = false;
        invulnerabilityTimer = 0.0f;
        gameOver = false;
        collectedFruit = 0;
        activeFruitCount = 0;

        // Reset all fruit
        for (int i = 0; i < FRUIT_POOL_SIZE; i++) {
            fruitPool[i].active = false;
        }

        // Reset asteroid pool
        asteroids.clear();

        // Initialize health bar (reset active status)
        for (int i = 0; i < MAX_HEALTH; i++) {
            healthBar.lifeCountActive[i] = true;
        }

        return true;
    }

    // Main game loop
    public void render(float delta) {
        // Clear screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update camera
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        if (!running) {
            renderMenu(delta);
        } else {

            runGame(delta);

        }
    }

    // Menu rendering
    private void renderMenu(float delta) {
        batch.begin();

        // Draw background circles
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

        // Draw UI elements
        batch.draw(spaceMonkeyBanerTexture, WORLD_WIDTH / 2 - 300.0f, WORLD_HEIGHT - 305.0f, 600.0f, 300.0f);

        // Draw and handle start button
        float startScale = 1.0f;
        if (checkMouseCollision(startPos, ui_sizeWidth/2 - 30, 20.0f)) {
            startScale = 1.05f;
            if (Gdx.input.justTouched()) {
                running = true;

                //running = true;
            }
        }
        batch.draw(startTexture, startPos.x, startPos.y, ui_sizeWidth * startScale, ui_sizeHeight * startScale);

        // Draw and handle exit button
        float exitScale = 1.0f;
        if (checkMouseCollision(exitPos, ui_sizeWidth / 2 - 30 , 20.0f)) {
            exitScale = 1.05f;
            if (Gdx.input.justTouched()) {
                Gdx.app.exit();
            }
        }
        batch.draw(exitTexture, exitPos.x, exitPos.y, ui_sizeWidth * exitScale, ui_sizeHeight * exitScale);

        batch.end();
    }


    // Main game logic
    private void runGame(float delta) {
        // Hide mouse cursor in game
        Gdx.input.setCursorCatched(true);

        // Return to menu if M key pressed
        if (Gdx.input.isKeyJustPressed(Keys.M)) {
            running = false;
            clear();
            init();
            Gdx.input.setCursorCatched(false);
            return;
        }
        if(Gdx.input.isKeyJustPressed(Keys.ESCAPE))
        {
            Gdx.app.exit();
        }

        batch.begin();

        // Handle game over state
        if (gameOver) {
            float messageWidth = 400;
            float messageHeight = 200;
            float centerX = WORLD_WIDTH / 2;
            float centerY = WORLD_HEIGHT / 2;

            batch.draw(defeatedTexture,
                centerX - messageWidth / 2,
                centerY - messageHeight / 2,
                messageWidth, messageHeight);

            // Draw key instructions for restart
            drawEndScreenInstructions(centerX, centerY, messageWidth);

            if (Gdx.input.isKeyJustPressed(Keys.R)) {
                clear();
                init();
            }

            batch.end();
            return;
        }

        // Handle victory state
        if (collectedFruit >= WIN_FRUIT_NUMBER) {
            float messageWidth = 400;
            float messageHeight = 200;
            float centerX = WORLD_WIDTH / 2;
            float centerY = WORLD_HEIGHT / 2;

            batch.draw(victoryTexture,
                centerX - messageWidth / 2,
                centerY - messageHeight / 2,
                messageWidth, messageHeight);

            // Draw key instructions for restart
            drawEndScreenInstructions(centerX, centerY, messageWidth);

            if (Gdx.input.isKeyJustPressed(Keys.R)) {
                clear();
                init();
            }

            batch.end();
            return;
        }

        // Update timers
        pulseTimer += delta;
        if (pulseTimer > MathUtils.PI2) {
            pulseTimer = 0.0f;
        }

        // Update invulnerability
        if (invulnerable) {
            invulnerabilityTimer -= delta;
            if (invulnerabilityTimer <= 0.0f) {
                invulnerable = false;
            }
        }

        // Update dash cooldown
        if (!dashReady) {
            dashCooldownTimer -= delta;
            if (dashCooldownTimer <= 0.0f) {
                dashReady = true;
            }
        }
        // Draw key instructions
        drawKeyInstructions();

        // Handle dash ability
        updateDash(delta);

        // Update and draw asteroids
        updateAsteroids(delta);

        // Update and draw fruits
        updateFruits(delta);

        // Draw dash effects
        drawDashEffect();

        // Draw player ship (with invulnerability blinking)
        if (!invulnerable || (int)(invulnerabilityTimer * 10.0f) % 2 == 0) {
            batch.draw(
                monkeyShipTexture,
                monkeyPosition.x - shipWidth/2,
                monkeyPosition.y - shipHeight/2,
                shipWidth, shipHeight
            );
        }

        // Draw health bar
        healthBar.draw(batch, shapeRenderer);

        // Draw score counter
        drawScoreCounter();



        // Draw dash cooldown indicator
        drawDashCooldown();

        batch.end();
    }

    private void drawScoreCounter() {
        // Score display dimensions and position
        float scoreWidth = 150.0f;
        float scoreHeight = 40.0f;
        float scoreX = WORLD_WIDTH / 2 - scoreWidth / 2;
        float scoreY = WORLD_HEIGHT - scoreHeight - 10.0f;

        // We need to switch to ShapeRenderer
        batch.end();

        // Draw the score background
        shapeRenderer.begin(ShapeType.Filled);

        // Shadow / outline effect
        shapeRenderer.setColor(0.0f, 0.0f, 0.0f, 0.7f);
        shapeRenderer.rect(scoreX + 3, scoreY - 3, scoreWidth, scoreHeight);

        // Main background
        shapeRenderer.setColor(0.2f, 0.2f, 0.4f, 0.9f);
        shapeRenderer.rect(scoreX, scoreY, scoreWidth, scoreHeight);

        // Inner border
        shapeRenderer.setColor(0.5f, 0.5f, 0.8f, 1.0f);
        shapeRenderer.rect(scoreX + 2, scoreY + 2, scoreWidth - 4, scoreHeight - 4,
            new Color(0.5f, 0.5f, 0.8f, 1.0f),
            new Color(0.3f, 0.3f, 0.6f, 1.0f),
            new Color(0.3f, 0.3f, 0.6f, 1.0f),
            new Color(0.5f, 0.5f, 0.8f, 1.0f));

        // Progress bar background
        shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 0.7f);
        shapeRenderer.rect(scoreX + 10, scoreY + 30, scoreWidth - 20, 4);

        // Progress bar fill - visual indication of progress
        float progressWidth = (scoreWidth - 20) * ((float)collectedFruit / WIN_FRUIT_NUMBER);
        shapeRenderer.setColor(0.2f, 0.9f, 0.3f, 1.0f);
        shapeRenderer.rect(scoreX + 10, scoreY + 30, progressWidth, 4);

        shapeRenderer.end();

        // Switch back to SpriteBatch for text
        batch.begin();

        // Format the score text
        String scoreText = "FRUITS: " + collectedFruit + "/" + WIN_FRUIT_NUMBER;

        // Set font color
        font.setColor(1.0f, 1.0f, 1.0f, 1.0f);

        // Center the text
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, scoreText);
        font.draw(batch, scoreText,
            scoreX + (scoreWidth/2) - (layout.width/2),
            scoreY + (scoreHeight/2) + (layout.height/2));
    }

    private void drawKeyInstructions() {
        float startY = 260.0f;
        float leftX = 1060.0f;

        // End SpriteBatch to use ShapeRenderer
        batch.end();

        // Start shape renderer for backgrounds
        shapeRenderer.begin(ShapeType.Filled);

        // Draw semi-transparent background
        shapeRenderer.setColor(0.1f, 0.1f, 0.2f, KEY_DISPLAY_ALPHA);

        // Calculate background width based on longest instruction
        float boxWidth = 200.0f;
        float boxHeight = 240.0f; // Height for all key instructions

        // Main background panel for all keys
        shapeRenderer.rect(
            leftX, startY - boxHeight + KEY_ICON_SIZE,
            boxWidth, boxHeight,
            new Color(0.2f, 0.2f, 0.3f, KEY_DISPLAY_ALPHA),
            new Color(0.2f, 0.2f, 0.3f, KEY_DISPLAY_ALPHA),
            new Color(0.1f, 0.1f, 0.15f, KEY_DISPLAY_ALPHA),
            new Color(0.1f, 0.1f, 0.15f, KEY_DISPLAY_ALPHA)
        );

        // Draw border
        shapeRenderer.setColor(0.4f, 0.4f, 0.6f, KEY_DISPLAY_ALPHA);
        // Draw rounded corners for the border - this is a simplified version
        shapeRenderer.rect(leftX, startY - boxHeight + KEY_ICON_SIZE, boxWidth, 2); // Top
        shapeRenderer.rect(leftX, startY - boxHeight + KEY_ICON_SIZE, 2, boxHeight); // Left
        shapeRenderer.rect(leftX + boxWidth - 2, startY - boxHeight + KEY_ICON_SIZE, 2, boxHeight); // Right
        shapeRenderer.rect(leftX, startY - boxHeight + KEY_ICON_SIZE + boxHeight - 2, boxWidth, 2); // Bottom

        // Title background
        shapeRenderer.setColor(0.3f, 0.3f, 0.5f, KEY_DISPLAY_ALPHA);
        shapeRenderer.rect(leftX, startY - boxHeight + KEY_ICON_SIZE + boxHeight - 30, boxWidth, 30);

        shapeRenderer.end();


        batch.begin();

        // Draw title
        font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        String titleText = "CONTROLS";
        com.badlogic.gdx.graphics.g2d.GlyphLayout titleLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, titleText);
        font.draw(batch, titleText,
            leftX + (boxWidth / 2) - (titleLayout.width / 2),
            startY - boxHeight + KEY_ICON_SIZE + boxHeight - 10);

        float offsetKey = 30;
        // Draw key instructions
        float currentY = startY - offsetKey;


        // Movement keys
        drawKeyInstruction(leftX + 10, currentY, "W", "Move Up");
        currentY -= offsetKey;
        drawKeyInstruction(leftX + 10, currentY, "A", "Move Left");
        currentY -= offsetKey;
        drawKeyInstruction(leftX + 10, currentY, "S", "Move Down");
        currentY -= offsetKey;
        drawKeyInstruction(leftX + 10, currentY, "D", "Move Right");
        currentY -= offsetKey;

        // Action keys
        drawKeyInstruction(leftX + 10, currentY, "SPACE", "Dash");
        currentY -= offsetKey;
        drawKeyInstruction(leftX + 10, currentY, "M", "Menu");
    }

    private void drawKeyInstruction(float x, float y, String key, String action) {

        batch.end();

        // Draw key background
        shapeRenderer.begin(ShapeType.Filled);

        // Key background
        shapeRenderer.setColor(0.7f, 0.7f, 0.8f, KEY_DISPLAY_ALPHA);
        float keyWidth = key.length() > 1 ? KEY_ICON_SIZE * 4.0f : KEY_ICON_SIZE;
        shapeRenderer.rect(x, y - KEY_ICON_SIZE + 5, keyWidth, KEY_ICON_SIZE,
            new Color(0.8f, 0.8f, 0.9f, KEY_DISPLAY_ALPHA),
            new Color(0.8f, 0.8f, 0.9f, KEY_DISPLAY_ALPHA),
            new Color(0.6f, 0.6f, 0.7f, KEY_DISPLAY_ALPHA),
            new Color(0.6f, 0.6f, 0.7f, KEY_DISPLAY_ALPHA));

        shapeRenderer.end();


        batch.begin();

        // Draw key letter
        font.setColor(0.1f, 0.1f, 0.2f, 1.0f);
        com.badlogic.gdx.graphics.g2d.GlyphLayout keyLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, key);
        font.draw(batch, key,
            x + (keyWidth / 2) - (keyLayout.width / 2),
            y - (KEY_ICON_SIZE / 2) + (keyLayout.height -4));

        // Draw action text
        font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        font.draw(batch, action, x + keyWidth + 10, y - (KEY_ICON_SIZE / 2) + (keyLayout.height / 2));
    }

    private void drawEndScreenInstructions(float centerX, float centerY, float width) {
        // Calculate position for key instruction
        float instructionY = centerY - 130.0f;


        batch.end();

        batch.begin();

        // Draw key letter
        font.setColor(1.0f, 0.1f, 0.2f, 1.0f);
        String keyText = "R";
        com.badlogic.gdx.graphics.g2d.GlyphLayout keyLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, keyText);
        font.draw(batch, keyText,
            centerX - 100 + (KEY_ICON_SIZE / 2) - (keyLayout.width / 2),
            instructionY + (keyLayout.height / 2));

        // Draw action text
        font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        String actionText = "Press to Restart";
        com.badlogic.gdx.graphics.g2d.GlyphLayout actionLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, actionText);
        font.draw(batch, actionText,
            centerX - 100 + KEY_ICON_SIZE + 10,
            instructionY + (keyLayout.height / 2));
    }

    // Update dash movement and input
    private void updateDash(float delta) {
        if (isDashing) {
            // Update dash timer
            dashTimer += delta;

            if (dashTimer >= DASH_DURATION) {
                // End dash
                isDashing = false;
                // Set the final position and resume normal movement
                monkeyPosition.set(dashTargetPosition);
            } else {
                // Calculate interpolation for smooth dash movement
                float t = dashTimer / DASH_DURATION;
                monkeyPosition.set(
                    smootherLerp(dashStartPosition.x, dashTargetPosition.x, t),
                    smootherLerp(dashStartPosition.y, dashTargetPosition.y, t)
                );

                // Reset velocity at end of dash to avoid sudden changes
                if (t > 0.9f) {
                    monkeyVelocity.set(dashDirection).scl(MAX_SPEED * 0.7f);
                }
            }
        } else {
            // Check for dash input
            if (dashReady && Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                // Get current movement direction
                Vector2 dashDir = new Vector2(0, 0);

                // Get input direction
                if (Gdx.input.isKeyPressed(Keys.W)) dashDir.y += 1.0f;
                if (Gdx.input.isKeyPressed(Keys.S)) dashDir.y -= 1.0f;
                if (Gdx.input.isKeyPressed(Keys.A)) dashDir.x -= 1.0f;
                if (Gdx.input.isKeyPressed(Keys.D)) dashDir.x += 1.0f;

                // If no direction input, dash in current velocity direction
                if (dashDir.len() < 0.1f && monkeyVelocity.len() > 0.1f) {
                    dashDir.set(monkeyVelocity).nor();
                }

                // If we have a valid direction, start dash
                if (dashDir.len() > 0.1f) {
                    dashDirection.set(dashDir).nor();

                    // Calculate dash start and end positions
                    dashStartPosition.set(monkeyPosition);

                    // Determine dash distance
                    float dashDistance = 300.0f;
                    dashTargetPosition.set(dashStartPosition).add(
                        dashDirection.x * dashDistance,
                        dashDirection.y * dashDistance
                    );

                    // Start dash
                    isDashing = true;
                    dashReady = false;
                    dashTimer = 0.0f;
                    dashCooldownTimer = DASH_COOLDOWN;

                    // Give invulnerability during dash
                    invulnerable = true;
                    invulnerabilityTimer = DASH_DURATION + 0.2f;
                }
            }

            // Normal movement controls
            if (!isDashing) {
                Vector2 inputDirection = new Vector2(0, 0);

                if (Gdx.input.isKeyPressed(Keys.W)) inputDirection.y += 1.0f;
                if (Gdx.input.isKeyPressed(Keys.S)) inputDirection.y -= 1.0f;
                if (Gdx.input.isKeyPressed(Keys.A)) inputDirection.x -= 1.0f;
                if (Gdx.input.isKeyPressed(Keys.D)) inputDirection.x += 1.0f;

                // Apply acceleration if there's input
                if (inputDirection.len() > 0.0f) {
                    inputDirection.nor();
                    monkeyVelocity.add(
                        inputDirection.x * ACCELERATION * delta,
                        inputDirection.y * ACCELERATION * delta
                    );

                    // Clamp velocity to maximum speed
                    if (monkeyVelocity.len() > MAX_SPEED) {
                        monkeyVelocity.nor().scl(MAX_SPEED);
                    }
                } else {
                    // Decelerate when no input
                    if (monkeyVelocity.len() > 0.0f) {
                        Vector2 deceleration = new Vector2(monkeyVelocity).nor().scl(DECELERATION * delta);

                        // Make sure we don't overshoot zero
                        if (deceleration.len() > monkeyVelocity.len()) {
                            monkeyVelocity.set(0, 0);
                        } else {
                            monkeyVelocity.sub(deceleration);
                        }
                    }
                }

                // Update position based on velocity
                monkeyPosition.add(
                    monkeyVelocity.x * delta,
                    monkeyVelocity.y * delta
                );

                // Keep the ship within screen bounds
                clampPositionToScreen(monkeyPosition);
            }
        }
    }

    // Update and draw asteroids
    private void updateAsteroids(float delta) {
        // Update spawn timer
        asteroidSpawnTimer -= delta;
        if (asteroidSpawnTimer <= 0.0f) {
            spawnAsteroid();
            asteroidSpawnTimer = ASTEROID_SPAWN_TIMER_MAX;
        }

        // Get player collision radius
        float playerRadius = shipWidth / 2.5f;

        // Update each asteroid
        for (Asteroid asteroid : asteroids) {
            if (asteroid.active) {
                // Update position and rotation
                asteroid.update(delta);

                // Check collision with player
                if (!invulnerable && !gameOver) {
                    float asteroidRadius = asteroid.size / 2.0f;
                    if (checkCollision(monkeyPosition, playerRadius, asteroid.position, asteroidRadius)) {
                        playerTakeDamage();
                    }
                }

                // Check if out of bounds
                if (isAsteroidOutOfBounds(asteroid)) {
                    asteroid.active = false;
                }

                // Draw the asteroid
                asteroid.draw(batch);
            }
        }
    }

    // Update and draw fruits
    private void updateFruits(float delta) {
        // Update spawn timer
        fruitSpawnTimer -= delta;
        if (activeFruitCount < MAX_FRUIT && fruitSpawnTimer <= 0.0f) {
            spawnFruit();
            fruitSpawnTimer = FRUIT_SPAWN_TIMER_MAX;
        }

        // Get player collision radius
        float playerRadius = shipWidth / 2.5f;

        // Update each fruit
        for (Fruit fruit : fruitPool) {
            if (fruit.active) {
                // Check for collision with player
                if (!invulnerable && !gameOver) {
                    if (checkCollision(monkeyPosition, playerRadius, fruit.position, fruit.size / 2.0f)) {
                        // Deactivate fruit
                        fruit.active = false;
                        activeFruitCount--;
                        collectedFruit++;
                    }
                }

                // Draw the fruit
                fruit.draw(batch);
            }
        }
    }

    // Draw the dash cooldown indicator
    private void drawDashCooldown() {
        // Calculate current cooldown percentage
        float targetPercentage = dashReady ? 1.0f : (1.0f - (dashCooldownTimer / DASH_COOLDOWN));

        // Smoothly lerp to target percentage
        prevCooldownPercentage = smoothLerp(prevCooldownPercentage, targetPercentage, 0.1f);

        // Draw indicator in corner
        float barWidth = 100.0f;
        float barHeight = 10.0f;
        float padding = 10.0f;

        batch.end();
        shapeRenderer.begin(ShapeType.Filled);

        // Background bar
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1.0f);
        shapeRenderer.rect(padding, padding, barWidth, barHeight);

        // Filled portion
        Color fillColor;
        if (dashReady) {
            fillColor = new Color(0.0f, 1.0f, 0.4f, 1.0f); // Green
        } else {
            // Transition color from orange to green
            fillColor = new Color(
                MathUtils.lerp(1.0f, 0.0f, prevCooldownPercentage),
                MathUtils.lerp(0.5f, 1.0f, prevCooldownPercentage),
                MathUtils.lerp(0.0f, 0.4f, prevCooldownPercentage),
                1.0f
            );
        }

        shapeRenderer.setColor(fillColor);
        shapeRenderer.rect(padding, padding, barWidth * prevCooldownPercentage, barHeight);

        // Add pulsing effect when ready
        if (dashReady) {
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

    // Draw visual effects during dash
    private void drawDashEffect() {
        if (!isDashing) return;

        batch.end();
        shapeRenderer.begin(ShapeType.Filled);

        // Calculate how far into the dash we are (0 to 1)
        float dashProgress = dashTimer / DASH_DURATION;

        // Number of trail segments
        final int trailCount = 5;

        for (int i = 1; i <= trailCount; i++) {
            // Calculate position along the dash path
            float trailFactor = dashProgress - (i * 0.1f);

            // Only draw trail segments within the dash path
            if (trailFactor >= 0.0f) {
                // Calculate trail position
                float trailX = smootherLerp(dashStartPosition.x, dashTargetPosition.x, trailFactor);
                float trailY = smootherLerp(dashStartPosition.y, dashTargetPosition.y, trailFactor);

                // Calculate alpha and size based on position in trail
                float alpha = 0.9f * (1.0f - (float)i / trailCount);
                float size = shipWidth * (1.0f - (0.15f * i));

                // Draw a semi-transparent circle at trail position
                shapeRenderer.setColor(0.8f, 0.8f, 1.0f, alpha);
                shapeRenderer.circle(trailX, trailY, size/2);

                //shapeRenderer.rect(trailX - size/2 , trailY- size / 2, size, size);
            }
        }

        // Add a burst effect at the start of the dash
      if (dashProgress < 0.2f) {
            float burstSize = shipWidth * (1.0f + dashProgress * 2.0f);
            float burstAlpha = 0.5f * (1.0f - dashProgress * 5.0f);

            shapeRenderer.setColor(1.0f, 1.0f, 1.0f, burstAlpha);
            shapeRenderer.circle(
                dashStartPosition.x,
                dashStartPosition.y ,
                burstSize/2
            );
        }

        shapeRenderer.end();
        batch.begin();
    }

    // Spawn a new asteroid
    private void spawnAsteroid() {
        // Create new asteroid if needed
        Asteroid asteroid;
        if (asteroids.size < MAX_ASTEROIDS) {
            asteroid = new Asteroid();
            asteroids.add(asteroid);
        } else {
            // Find an inactive asteroid
            asteroid = null;
            for (Asteroid a : asteroids) {
                if (!a.active) {
                    asteroid = a;
                    break;
                }
            }

            // If no inactive asteroid found, return
            if (asteroid == null) return;
        }

        // Determine size
        float size = MathUtils.random(ASTEROID_MIN_SIZE, ASTEROID_MAX_SIZE);

        // Determine spawn position (outside screen)
        Vector2 position = new Vector2();

        // Decide which side to spawn from
        int spawnSide = MathUtils.random(0, 3); // 0=top, 1=right, 2=bottom, 3=left
        switch (spawnSide) {
            case 0: // Top
                position.set(MathUtils.random(0, WORLD_WIDTH), -size);
                break;
            case 1: // Right
                position.set(WORLD_WIDTH + size, MathUtils.random(0, WORLD_HEIGHT));
                break;
            case 2: // Bottom
                position.set(MathUtils.random(0, WORLD_WIDTH), WORLD_HEIGHT + size);
                break;
            case 3: // Left
                position.set(-size, MathUtils.random(0, WORLD_HEIGHT));
                break;
        }

        // Calculate direction toward center-ish with some randomness
        Vector2 centerPoint = new Vector2(
            WORLD_WIDTH * (0.3f + MathUtils.random() * 0.4f),
            WORLD_HEIGHT * (0.3f + MathUtils.random() * 0.4f)
        );

        Vector2 direction = new Vector2(centerPoint).sub(position).nor();

        // Create speeds and rotation
        float speed = MathUtils.random(ASTEROID_MIN_SPEED, ASTEROID_MAX_SPEED);
        float rotationSpeed = MathUtils.random(-MathUtils.PI, MathUtils.PI) * 0.5f;

        // Set asteroid properties
        asteroid.position.set(position);
        asteroid.velocity.set(direction).scl(speed);
        asteroid.rotationSpeed = rotationSpeed;
        asteroid.size = size;
        asteroid.active = true;
    }

    // Spawn a new fruit
    private void spawnFruit() {
        // Only spawn if we haven't reached the maximum active fruits
        if (activeFruitCount >= MAX_FRUIT) return;

        // Find an inactive fruit
        Fruit fruit = null;
        for (Fruit f : fruitPool) {
            if (!f.active) {
                fruit = f;
                break;
            }
        }

        // If no inactive fruit found, don't spawn
        if (fruit == null) return;

        // Determine spawn position
        float x = MathUtils.random(0.1f, 0.9f) * WORLD_WIDTH;
        float y = MathUtils.random(0.1f, 0.9f) * WORLD_HEIGHT;

        // Select fruit type
        int fruitType = MathUtils.random(0, 1);

        // Set fruit properties
        fruit.position.set(x, y);
        fruit.type = fruitType;
        fruit.active = true;
        activeFruitCount++;
    }

    // Check if asteroid is outside the screen with a margin
    private boolean isAsteroidOutOfBounds(Asteroid asteroid) {
        if (!asteroid.active) return false;

        float size = asteroid.size;
        float margin = size * 2.0f; // Extra margin

        return (asteroid.position.x < -margin ||
            asteroid.position.x > WORLD_WIDTH + margin ||
            asteroid.position.y < -margin ||
            asteroid.position.y > WORLD_HEIGHT + margin);
    }

    // Handle player taking damage
    private void playerTakeDamage() {
        if (!invulnerable && !gameOver) {
            playerHealth--;

            // Check if player is dead
            if (playerHealth <= 0) {
                gameOver = true;
            } else {
                // Activate invulnerability period
                invulnerable = true;
                invulnerabilityTimer = INVULNERABILITY_DURATION;
            }
        }
    }

    // Check collision between two circles
    private boolean checkCollision(Vector2 pos1, float radius1, Vector2 pos2, float radius2) {
        // Calculate distance between centers
        float dx = pos1.x - pos2.x;
        float dy = pos1.y - pos2.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // Check if circles overlap
        return distance < (radius1 + radius2);
    }

    // Check if mouse position collides with a circle
    private boolean checkMouseCollision(Vector2 circlePos, float radiusX, float radiusY) {
        // Get mouse position in world coordinates
        Vector2 mousePos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(mousePos);

        // Check if mouse is inside the circle
        float dx = mousePos.x - (circlePos.x + radiusX);
        float dy = mousePos.y - (circlePos.y + radiusY);
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        return distance < radiusX;
    }

    // Helper function to keep positions within screen bounds
    private void clampPositionToScreen(Vector2 position) {
        // Wrap around screen edges
        if (position.x < -shipWidth)
            position.x = WORLD_WIDTH;
        if (position.y < -shipHeight)
            position.y = WORLD_HEIGHT;
        if (position.x > WORLD_WIDTH + shipWidth)
            position.x = -shipWidth;
        if (position.y > WORLD_HEIGHT + shipHeight)
            position.y = -shipHeight;
    }

    // Clear and reset game objects
    private void clear() {
        // Clear all asteroids
        for (Asteroid asteroid : asteroids) {
            asteroid.active = false;
        }

        // Clear all fruit
        for (Fruit fruit : fruitPool) {
            fruit.active = false;
        }
        activeFruitCount = 0;
    }

    // Resize viewport when window is resized
    public void resize(int width, int height) {
        viewport.update(width, height);
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
    }

    // Dispose resources when game is closed
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        monkeyShipTexture.dispose();
        asteroidTexture.dispose();
        for (Texture texture : fruitTextures) {
            texture.dispose();
        }
        healthbarTexture.dispose();
        defeatedTexture.dispose();
        victoryTexture.dispose();
        startTexture.dispose();
        exitTexture.dispose();
        spaceMonkeyBanerTexture.dispose();
        font.dispose();
    }

    // Utility methods for smooth interpolation
    private float smoothLerp(float start, float end, float t) {
        return start + (end - start) * t;
    }

    private float smootherLerp(float start, float end, float t) {
        // Smootherstep interpolation: 6t^5 - 15t^4 + 10t^3
        t = t * t * t * (t * (t * 6 - 15) + 10);
        return start + (end - start) * t;
    }
}
