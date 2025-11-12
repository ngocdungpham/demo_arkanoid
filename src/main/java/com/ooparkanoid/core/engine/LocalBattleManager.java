package com.ooparkanoid.core.engine;

import com.ooparkanoid.core.state.GameStateManager;
import com.ooparkanoid.graphics.ResourceManager;
import com.ooparkanoid.object.Ball;
import com.ooparkanoid.object.GameObject;
import com.ooparkanoid.object.Paddle;
import com.ooparkanoid.sound.SoundManager;
import com.ooparkanoid.utils.Constants;
import com.ooparkanoid.object.bricks.Brick;
import com.ooparkanoid.object.bricks.StrongBrick;
import com.ooparkanoid.object.bricks.ExplosiveBrick;
import com.ooparkanoid.object.bricks.FlickerBrick;
import com.ooparkanoid.factory.*;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import javafx.beans.property.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;

/**
 * Orchestrates local 2-player battle mode - a competitive Pong-style brick breaker.
 *
 * Game Mechanics:
 * - Player 1 (Left): Defends left side with vertical paddle, attacks opponent's bricks
 * - Player 2 (Right): Defends right side with vertical paddle, attacks opponent's bricks
 * - Ball bounces between players, breaking bricks on collision
 * - Players lose lives when ball passes their paddle (defense bars destroyed)
 *
 * Controls:
 * - Player 1: W/S (move up/down), Space (serve ball)
 * - Player 2: Arrow Up/Down (move), Enter (serve/restart)
 *
 * Victory Conditions:
 * - Destroy all opponent's defense bars (shields)
 * - Last player standing wins
 *
 * Technical Implementation:
 * - Uses rotated collision boxes (BattleBrick) for vertical brick layouts
 * - All rotation handling is internal (no modification to base Brick class)
 * - Observable properties for JavaFX UI binding
 *
 * Design Pattern: Event-driven with JavaFX properties for reactive UI updates
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public class LocalBattleManager {

    /**
     * Enum indicating which player is currently serving the ball.
     */
    public enum ServingPlayer {
        /** Player 1 (left side) is serving */
        PLAYER_ONE,
        /** Player 2 (right side) is serving */
        PLAYER_TWO
    }

    /**
     * Internal container class for rotated bricks in battle mode.
     * Wraps a GameObject for collision detection with rotation metadata.
     * Rotation logic is handled entirely within this manager to avoid modifying Brick class.
     */
    private static class BattleBrick {
        /** Collision box for hit detection */
        public GameObject collisionBox;

        /** Rotation angle in degrees (90° for vertical orientation) */
        public double rotationAngle;

        /** Flag indicating if brick has been destroyed */
        public boolean isDestroyed = false;

        /**
         * Constructs a rotated battle brick with collision box.
         *
         * @param x X coordinate of collision box
         * @param y Y coordinate of collision box
         * @param width width of collision box (display width after rotation)
         * @param height height of collision box (display height after rotation)
         * @param angle rotation angle in degrees
         */
        public BattleBrick(double x, double y, double width, double height, double angle) {
            this.collisionBox = new GameObject(x, y, width, height) {
                @Override public void update(double dt) {}
                @Override public void render(GraphicsContext gc) {}
            };
            this.rotationAngle = angle;
        }
    }

    // ==================== Layout Constants ====================
    /** Horizontal margin from screen edges to playfield */
    private static final double FIELD_MARGIN_X = 100.0;

    /** Vertical margin from screen edges to playfield */
    private static final double FIELD_MARGIN_Y = 70.0;

    /** Distance of paddle from screen edge */
    private static final double PADDLE_OFFSET_X = 56.0;

    /** Height of vertical paddle */
    private static final double PADDLE_HEIGHT = 160.0;

    /** Width of vertical paddle */
    private static final double PADDLE_WIDTH = 28.0;

    /** Additional padding for boundary collision detection */
    private static final double BOUNDARY_PADDING = 12.0;

    // ==================== Core Components ====================
    /** Manages game state for both players */
    private final GameStateManager stateManager;

    /** Random number generator for ball physics */
    private final Random random = new Random();

    /** Sound manager for game audio */
    private final SoundManager soundManager = SoundManager.getInstance();

    /** Texture for normal bricks */
    private final Image brickTexture;

    /** Texture for explosive bricks */
    private final Image explosiveBrickTexture;

    // ==================== Game Objects ====================
    /** Player 1's paddle (left side, vertical) */
    private Paddle playerOnePaddle;

    /** Player 2's paddle (right side, vertical) */
    private Paddle playerTwoPaddle;

    /** The shared ball */
    private Ball ball;

    /** Key press event handler */
    private EventHandler<KeyEvent> keyPressedHandler;

    /** Key release event handler */
    private EventHandler<KeyEvent> keyReleasedHandler;

    /** List of rotated bricks in battle mode */
    private final List<BattleBrick> battleBricks = new ArrayList<>();

    /** Defense bars for Player 1 (life indicators) */
    private final List<DefenseBar> playerOneBars = new ArrayList<>();

    /** Defense bars for Player 2 (life indicators) */
    private final List<DefenseBar> playerTwoBars = new ArrayList<>();

    /** Registry mapping brick types to their factories */
    private Map<Brick.BrickType, BrickFactory> brickFactories;

    /** List of available brick factories for random selection */
    private List<BrickFactory> availableFactories;

    // ==================== Observable Game State ====================
    /** Player 1 remaining lives (observable for UI binding) */
    private final IntegerProperty playerOneLives = new SimpleIntegerProperty();

    /** Player 2 remaining lives (observable for UI binding) */
    private final IntegerProperty playerTwoLives = new SimpleIntegerProperty();

    /** Player 1 current score (observable for UI binding) */
    private final IntegerProperty playerOneScore = new SimpleIntegerProperty();

    /** Player 2 current score (observable for UI binding) */
    private final IntegerProperty playerTwoScore = new SimpleIntegerProperty();

    /** Match elapsed time in seconds (observable for UI binding) */
    private final DoubleProperty matchTimeSeconds = new SimpleDoubleProperty();

    /** Current serving player (observable for UI binding) */
    private final ObjectProperty<ServingPlayer> servingPlayer = new SimpleObjectProperty<>(ServingPlayer.PLAYER_ONE);

    // ==================== Match State ====================
    /** Last player to hit the ball (for scoring attribution) */
    private ServingPlayer lastHitter = ServingPlayer.PLAYER_ONE;

    /** Flag indicating if ball has been launched */
    private boolean ballLaunched = false;

    /** Flag indicating if match has ended */
    private boolean matchOver = false;

    /** Message describing match outcome */
    private String winnerMessage = "";

    /** Playfield boundaries */
    private double fieldLeft, fieldRight, fieldTop, fieldBottom;

    /**
     * Constructs a LocalBattleManager with specified state manager.
     * Loads required textures and initializes brick factories.
     *
     * @param stateManager the game state manager
     */
    public LocalBattleManager(GameStateManager stateManager) {
        this.stateManager = stateManager;
        ResourceManager resourceManager = ResourceManager.getInstance();
        this.brickTexture = resourceManager.getImage("brick_normal.png");
        this.explosiveBrickTexture = resourceManager.getImage("brick_explosive.png");
        initializeFactories();
    }

    /**
     * Initializes and registers all brick factories required for battle mode.
     * Creates factory instances for normal, strong, explosive, and flicker bricks.
     */
    private void initializeFactories() {
        brickFactories = new HashMap<>();

        // Register all brick type factories
        brickFactories.put(Brick.BrickType.NORMAL, new NormalBrickFactory(this.brickTexture));
        brickFactories.put(Brick.BrickType.STRONG, new StrongBrickFactory());
        brickFactories.put(Brick.BrickType.EXPLOSIVE, new ExplosiveBrickFactory(this.explosiveBrickTexture));
        brickFactories.put(Brick.BrickType.FLICKER, new FlickerBrickFactory());

        availableFactories = new ArrayList<>(brickFactories.values());
    }

    /**
     * Initializes and starts a new battle match.
     * Sets up playfield boundaries, creates paddles, spawns ball and bricks,
     * initializes defense bars, and resets all match state.
     */
    public void startMatch() {
        fieldLeft = FIELD_MARGIN_X;
        fieldRight = Constants.WIDTH - FIELD_MARGIN_X;
        fieldTop = FIELD_MARGIN_Y;
        fieldBottom = Constants.HEIGHT - FIELD_MARGIN_Y;

        double paddleStartY = (fieldTop + fieldBottom) / 2.0 - PADDLE_HEIGHT / 2.0;
        playerOnePaddle = new Paddle(fieldLeft + PADDLE_OFFSET_X, paddleStartY);
        playerOnePaddle.setWidth(PADDLE_WIDTH);
        playerOnePaddle.setHeight(PADDLE_HEIGHT);
        playerOnePaddle.setOrientation(Paddle.Orientation.VERTICAL_LEFT);
        playerOnePaddle.lockHorizontalPosition(playerOnePaddle.getX());
        playerOnePaddle.setVerticalMovementBounds(fieldTop + 24, fieldBottom - 24);

        playerTwoPaddle = new Paddle(fieldRight - PADDLE_OFFSET_X - PADDLE_WIDTH, paddleStartY);
        playerTwoPaddle.setWidth(PADDLE_WIDTH);
        playerTwoPaddle.setHeight(PADDLE_HEIGHT);
        playerTwoPaddle.setOrientation(Paddle.Orientation.VERTICAL_RIGHT);
        playerTwoPaddle.lockHorizontalPosition(playerTwoPaddle.getX());
        playerTwoPaddle.setVerticalMovementBounds(fieldTop + 24, fieldBottom - 24);

        ball = new Ball((fieldLeft + fieldRight) / 2.0, (fieldTop + fieldBottom) / 2.0,
                Constants.BALL_RADIUS, Constants.DEFAULT_SPEED, 1, 0);
        ball.clearTrail();

        spawnInitialBricks();

        playerOneBars.clear();
        playerTwoBars.clear();
        createDefenseBars(playerOneBars);
        createDefenseBars(playerTwoBars);
        updateLivesFromBars();

        playerOneScore.set(0);
        playerTwoScore.set(0);
        matchTimeSeconds.set(0);
        servingPlayer.set(ServingPlayer.PLAYER_ONE);
        lastHitter = ServingPlayer.PLAYER_ONE;
        ballLaunched = false;
        matchOver = false;
        winnerMessage = "";

        attachBallToServer();
    }

    /**
     * Creates defense bars (life indicators) for a player.
     * Each bar represents one life, initialized to full health.
     *
     * @param target the list to populate with defense bars
     */
    private void createDefenseBars(List<DefenseBar> target) {
        target.clear();
        for (int i = 0; i < Constants.START_LIVES; i++) {
            target.add(new DefenseBar());
        }
    }

    /**
     * Activates battle mode by registering keyboard event handlers to the scene.
     * Sets up controls for both players (WASD/Arrow keys + serve buttons).
     * Must be called from outside (e.g., from GameScreen) to enable player input.
     *
     * Player 1 Controls:
     * - W: Move paddle up
     * - S: Move paddle down
     * - Space: Serve ball
     *
     * Player 2 Controls:
     * - Up Arrow: Move paddle up
     * - Down Arrow: Move paddle down
     * - Enter: Serve ball / Restart match
     *
     * @param scene the main game scene to attach handlers to
     */
    public void activate(Scene scene) {
        System.out.println("Activating Battle Mode Key Handlers...");

        // Create event handlers (only once)
        if (keyPressedHandler == null) {
            keyPressedHandler = event -> {
                // Handle key press logic
                switch (event.getCode()) {
                    case W:
                        setPlayerOneVelocity(-Constants.PADDLE_SPEED);
                        break;
                    case S:
                        setPlayerOneVelocity(Constants.PADDLE_SPEED);
                        break;
                    case UP:
                        setPlayerTwoVelocity(-Constants.PADDLE_SPEED);
                        break;
                    case DOWN:
                        setPlayerTwoVelocity(Constants.PADDLE_SPEED);
                        break;
                    case SPACE:
                        launchBall();
                        break;
                    case ENTER:
                        if (isMatchOver()) {
                            startMatch();
                        }
                        break;
                }
            };
        }

        if (keyReleasedHandler == null) {
            keyReleasedHandler = event -> {
                // Handle key release logic
                switch (event.getCode()) {
                    case W:
                    case S:
                        setPlayerOneVelocity(0);
                        break;
                    case UP:
                    case DOWN:
                        setPlayerTwoVelocity(0);
                        break;
                }
            };
        }

        // Attach handlers to scene
        scene.setOnKeyPressed(keyPressedHandler);
        scene.setOnKeyReleased(keyReleasedHandler);
    }

    /**
     * Deactivates battle mode by removing keyboard event handlers from the scene.
     * Should be called when exiting battle mode (e.g., from GameScreen).
     *
     * @param scene the main game scene to detach handlers from
     */
    public void deactivate(Scene scene) {
        System.out.println("Deactivating Battle Mode Key Handlers...");
        // Remove handlers by setting them to null
        scene.setOnKeyPressed(null);
        scene.setOnKeyReleased(null);
    }

    /**
     * Spawns initial brick cluster at center of playfield.
     * Creates a 7x4 grid with random brick placement (30% spawn chance per cell).
     * All bricks are rotated 90° for vertical orientation.
     */
    private void spawnInitialBricks() {
        battleBricks.clear();
        final int brickDisplayWidth = Constants.BRICK_HEIGHT;
        final int brickDisplayHeight = Constants.BRICK_WIDTH;
        int rows = 7;
        int columns = 4;
        double brickSpawnChance = 0.3;

        double totalWidth = columns * brickDisplayWidth + (columns - 1) * Constants.BRICK_PADDING_X;
        double totalHeight = rows * brickDisplayHeight + (rows - 1) * Constants.BRICK_PADDING_Y;
        double startX = (fieldLeft + fieldRight) / 2.0 - totalWidth / 2.0;
        double startY = (fieldTop + fieldBottom) / 2.0 - totalHeight / 2.0;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                if (random.nextDouble() < brickSpawnChance) {

                    double x = startX + col * (brickDisplayWidth + Constants.BRICK_PADDING_X);
                    double y = startY + row * (brickDisplayHeight + Constants.BRICK_PADDING_Y);

                    battleBricks.add(new BattleBrick(x, y, brickDisplayWidth, brickDisplayHeight, 90));
                }
            }
        }
    }

    /**
     * Main update loop for battle mode.
     * Updates all game objects, handles collisions, and manages match flow.
     * Only processes updates when match is active (not game over).
     *
     * @param dt delta time in seconds since last update
     */
    public void update(double dt) {
        if (matchOver) return;
        matchTimeSeconds.set(matchTimeSeconds.get() + dt);

        if (playerOnePaddle != null) playerOnePaddle.update(dt);
        if (playerTwoPaddle != null) playerTwoPaddle.update(dt);

        if (!ballLaunched) {
            attachBallToServer();
            return;
        }

        ball.update(dt);
        handlePaddleCollisions();
        handleBrickCollisions();
        keepBallInsideVerticalBounds();
        handleBoundaryCollisions();
    }

    /**
     * Keeps ball within vertical playfield bounds.
     * Bounces ball off top and bottom boundaries.
     */
    private void keepBallInsideVerticalBounds() {
        if (ball.getY() + ball.getHeight() >= fieldBottom) {
            ball.setY(fieldBottom - ball.getHeight() - 1);
            ball.setDirection(ball.getDx(), -Math.abs(ball.getDy()));
        } else if (ball.getY() <= fieldTop) {
            ball.setY(fieldTop + 1);
            ball.setDirection(ball.getDx(), Math.abs(ball.getDy()));
        }
    }

    /**
     * Handles ball collisions with player paddles.
     * Updates last hitter for scoring attribution.
     */
    private void handlePaddleCollisions() {
        if (playerOnePaddle != null && ball.intersects(playerOnePaddle) && ball.getDx() < 0) {
            bounceFromPaddle(playerOnePaddle, true);
            lastHitter = ServingPlayer.PLAYER_ONE;
        }
        if (playerTwoPaddle != null && ball.intersects(playerTwoPaddle) && ball.getDx() > 0) {
            bounceFromPaddle(playerTwoPaddle, false);
            lastHitter = ServingPlayer.PLAYER_TWO;
        }
    }

    /**
     * Handles ball collisions with bricks.
     * Destroys bricks, awards points to last hitter, and respawns brick cluster when empty.
     */
    private void handleBrickCollisions() {
        Iterator<BattleBrick> iterator = battleBricks.iterator();
        while (iterator.hasNext()) {
            BattleBrick battleBrick = iterator.next();
            if (battleBrick.isDestroyed) {
                iterator.remove();
                continue;
            }

            if (ball.intersects(battleBrick.collisionBox)) {
                double ballCenterX = ball.getX() + ball.getRadius();
                double ballCenterY = ball.getY() + ball.getRadius();
                double brickCenterX = battleBrick.collisionBox.getX() + battleBrick.collisionBox.getWidth() / 2;
                double brickCenterY = battleBrick.collisionBox.getY() + battleBrick.collisionBox.getHeight() / 2;
                double overlapX = (ball.getRadius() + battleBrick.collisionBox.getWidth() / 2) - Math.abs(ballCenterX - brickCenterX);
                double overlapY = (ball.getRadius() + battleBrick.collisionBox.getHeight() / 2) - Math.abs(ballCenterY - brickCenterY);

                if (overlapX < overlapY) ball.setDirection(-ball.getDx(), ball.getDy());
                else ball.setDirection(ball.getDx(), -ball.getDy());

                battleBrick.isDestroyed = true;
                soundManager.play("break");
                iterator.remove();

                if (lastHitter == ServingPlayer.PLAYER_ONE) playerOneScore.set(playerOneScore.get() + 10);
                else playerTwoScore.set(playerTwoScore.get() + 10);

                if (battleBricks.isEmpty()) spawnInitialBricks();
                break;
            }
        }
    }

    /**
     * Handles ball passing paddle boundaries (life loss scenario).
     * Destroys defense bars and checks for match end conditions.
     */
    private void handleBoundaryCollisions() {
        if (ball.getDx() < 0 && ball.getX() <= fieldLeft) {
            destroyBar(findNextActiveBar(playerOneBars), ServingPlayer.PLAYER_ONE);
        } else if (ball.getDx() > 0 && ball.getX() + ball.getWidth() >= fieldRight) {
            destroyBar(findNextActiveBar(playerTwoBars), ServingPlayer.PLAYER_TWO);
        }
    }

    /**
     * Finds the next active (non-destroyed) defense bar from a list.
     *
     * @param bars list of defense bars to search
     * @return first non-destroyed bar, or null if all are destroyed
     */
    private DefenseBar findNextActiveBar(List<DefenseBar> bars) {
        for (DefenseBar bar : bars) if (!bar.isDestroyed()) return bar;
        return null;
    }

    /**
     * Destroys a defense bar and handles life loss logic.
     * Respawns bricks, plays sound effects, checks for game over, and repositions ball.
     *
     * @param bar the defense bar to destroy
     * @param player the player who lost the defense bar
     */
    private void destroyBar(DefenseBar bar, ServingPlayer player) {
        if (bar == null || bar.isDestroyed()) return;
        bar.destroy();
        soundManager.play("break");
        soundManager.play("lose_life");
        updateLivesFromBars();
        spawnInitialBricks();

        if (player == ServingPlayer.PLAYER_ONE) {
            if (playerOneLives.get() <= 0) {
                endMatch(ServingPlayer.PLAYER_TWO, "Player 2 wins the battle!");
                return;
            }
            stateManager.setStatusMessage(String.format("Player 1 lost a shield! %d remaining.", playerOneLives.get()));
            ball.setX(fieldLeft + BOUNDARY_PADDING);
            ball.setDirection(Math.max(0.25, Math.abs(ball.getDx())), ball.getDy());
        } else {
            if (playerTwoLives.get() <= 0) {
                endMatch(ServingPlayer.PLAYER_ONE, "Player 1 wins the battle!");
                return;
            }
            stateManager.setStatusMessage(String.format("Player 2 lost a shield! %d remaining.", playerTwoLives.get()));
            ball.setX(fieldRight - ball.getWidth() - BOUNDARY_PADDING);
            ball.setDirection(-Math.max(0.25, Math.abs(ball.getDx())), ball.getDy());
        }
    }

    /**
     * Calculates and applies bounce physics when ball hits a paddle.
     * Implements angle-based bouncing based on where ball hits paddle (center vs edge).
     *
     * @param paddle the paddle that was hit
     * @param sendRightward true to bounce ball rightward, false for leftward
     */
    private void bounceFromPaddle(Paddle paddle, boolean sendRightward) {
        double paddleCenter = paddle.getY() + paddle.getHeight() / 2.0;
        double ballCenter = ball.getY() + ball.getHeight() / 2.0;
        double relativeIntersect = Math.max(-1, Math.min(1, (ballCenter - paddleCenter) / (paddle.getHeight() / 2.0)));
        double bounceAngle = relativeIntersect * Math.toRadians(60);
        double directionX = Math.cos(bounceAngle);
        double directionY = Math.sin(bounceAngle);

        if (sendRightward) {
            ball.setX(paddle.getX() + paddle.getWidth() + 1);
            ball.setDirection(Math.abs(directionX), directionY);
        } else {
            ball.setX(paddle.getX() - ball.getWidth() - 1);
            ball.setDirection(-Math.abs(directionX), directionY);
        }
        soundManager.play("bounce");
    }

    /**
     * Ends the match with a winner declaration.
     * Stops ball movement, plays victory/defeat sounds, and displays result message.
     *
     * @param winner the winning player
     * @param message the victory message to display
     */
    private void endMatch(ServingPlayer winner, String message) {
        matchOver = true;
        winnerMessage = message;
        ballLaunched = false;
        if (ball != null) ball.setVelocity(0, 0);

        if (winner == ServingPlayer.PLAYER_ONE) soundManager.play("battle_victory");
        else soundManager.play("battle_defeat");

        stateManager.markGameOver();
        stateManager.setStatusMessage(message + " Press ENTER to restart.");
        stopPlayers();
    }

    /**
     * Attaches ball to serving player's paddle before launch.
     * Positions ball next to server's paddle and stops ball movement.
     */
    private void attachBallToServer() {
        if (ball == null) return;
        Paddle server = servingPlayer.get() == ServingPlayer.PLAYER_ONE ? playerOnePaddle : playerTwoPaddle;
        if (server == null) return;

        double ballY = server.getY() + server.getHeight() / 2.0 - ball.getHeight() / 2.0;
        double ballX = servingPlayer.get() == ServingPlayer.PLAYER_ONE
                ? server.getX() + server.getWidth() + 6
                : server.getX() - ball.getWidth() - 6;
        ball.setPosition(ballX, ballY);
        ball.setVelocity(0, 0);
    }

    /**
     * Renders all battle mode game objects to the graphics context.
     * Draws playfield, center line, bricks (with rotation), paddles, and ball.
     *
     * @param gc the GraphicsContext to render to
     */
    public void render(GraphicsContext gc) {
        if (gc == null) return;
        gc.clearRect(0, 0, Constants.WIDTH, Constants.HEIGHT);

        double fieldWidth = fieldRight - fieldLeft;
        double fieldHeight = fieldBottom - fieldTop;
        gc.setFill(Color.color(0.08, 0.12, 0.28, 0.55));
        gc.fillRoundRect(fieldLeft, fieldTop, fieldWidth, fieldHeight, 30, 30);

        double centerX = (fieldLeft + fieldRight) / 2.0;
        gc.setStroke(Color.color(1, 1, 1, 0.25));
        gc.setLineWidth(2);
        gc.setLineDashes(18, 18);
        gc.strokeLine(centerX, fieldTop + 18, centerX, fieldBottom - 18);
        gc.setLineDashes(null);

        for (BattleBrick battleBrick : battleBricks) {
            if (battleBrick.isDestroyed) continue;
            gc.save();
            GameObject box = battleBrick.collisionBox;
            double x = box.getX();
            double y = box.getY();
            double textureWidth = Constants.BRICK_WIDTH;
            double textureHeight = Constants.BRICK_HEIGHT;
            double brickCenterX = x + box.getWidth() / 2.0;
            double brickCenterY = y + box.getHeight() / 2.0;

            Rotate r = new Rotate(battleBrick.rotationAngle, brickCenterX, brickCenterY);
            gc.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());

            double drawX = x - (textureWidth - box.getWidth()) / 2.0;
            double drawY = y - (textureHeight - box.getHeight()) / 2.0;

            if (this.brickTexture != null) {
                gc.drawImage(this.brickTexture, drawX, drawY, textureWidth, textureHeight);
            }
            gc.restore();
        }

        if (playerOnePaddle != null) playerOnePaddle.render(gc);
        if (playerTwoPaddle != null) playerTwoPaddle.render(gc);
        if (ball != null) ball.render(gc);
    }

    /**
     * Launches the ball from serving player's paddle with random trajectory.
     * Ball direction is determined by serving player (left or right).
     * Ensures vertical component is sufficient to avoid purely horizontal movement.
     */
    public void launchBall() {
        if (matchOver || ballLaunched) return;
        double verticalComponent = (random.nextDouble() * 1.4) - 0.7;
        if (Math.abs(verticalComponent) < 0.2) {
            verticalComponent = Math.copySign(0.2, verticalComponent == 0 ? random.nextDouble() - 0.5 : verticalComponent);
        }
        double horizontalComponent = servingPlayer.get() == ServingPlayer.PLAYER_ONE ? 1 : -1;
        ball.setDirection(horizontalComponent, verticalComponent);
        ballLaunched = true;
        stateManager.setStatusMessage("Battle on!");
    }

    /**
     * Stops both players' paddles immediately.
     * Sets vertical velocity to zero for both paddles.
     */
    public void stopPlayers() {
        if (playerOnePaddle != null) playerOnePaddle.setDy(0);
        if (playerTwoPaddle != null) playerTwoPaddle.setDy(0);
    }

    /**
     * Sets Player 1's paddle vertical velocity.
     *
     * @param dy vertical velocity (positive = down, negative = up)
     */
    public void setPlayerOneVelocity(double dy) {
        if (playerOnePaddle != null) playerOnePaddle.setDy(dy);
    }

    /**
     * Sets Player 2's paddle vertical velocity.
     *
     * @param dy vertical velocity (positive = down, negative = up)
     */
    public void setPlayerTwoVelocity(double dy) {
        if (playerTwoPaddle != null) playerTwoPaddle.setDy(dy);
    }

    /**
     * Updates lives counters based on remaining defense bars.
     * Counts non-destroyed bars for each player.
     */
    private void updateLivesFromBars() {
        int p1 = (int) playerOneBars.stream().filter(b -> !b.isDestroyed()).count();
        int p2 = (int) playerTwoBars.stream().filter(b -> !b.isDestroyed()).count();
        playerOneLives.set(p1);
        playerTwoLives.set(p2);
    }

    // ==================== Observable Property Getters ====================
    /**
     * Gets Player 1's lives property for UI binding.
     *
     * @return observable integer property for Player 1 lives
     */
    public IntegerProperty playerOneLivesProperty() { return playerOneLives; }

    /**
     * Gets Player 2's lives property for UI binding.
     *
     * @return observable integer property for Player 2 lives
     */
    public IntegerProperty playerTwoLivesProperty() { return playerTwoLives; }

    /**
     * Gets Player 1's score property for UI binding.
     *
     * @return observable integer property for Player 1 score
     */
    public IntegerProperty playerOneScoreProperty() { return playerOneScore; }

    /**
     * Gets Player 2's score property for UI binding.
     *
     * @return observable integer property for Player 2 score
     */
    public IntegerProperty playerTwoScoreProperty() { return playerTwoScore; }

    /**
     * Gets match time property for UI binding.
     *
     * @return observable double property for elapsed match time in seconds
     */
    public DoubleProperty matchTimeProperty() { return matchTimeSeconds; }

    /**
     * Gets serving player property for UI binding.
     *
     * @return observable property indicating current server
     */
    public ObjectProperty<ServingPlayer> servingPlayerProperty() { return servingPlayer; }

    // ==================== Game State Getters ====================
    /**
     * Checks if the match has ended.
     *
     * @return true if match is over, false otherwise
     */
    public boolean isMatchOver() { return matchOver; }

    /**
     * Gets the victory message displayed at match end.
     *
     * @return winner message string
     */
    public String getWinnerMessage() { return winnerMessage; }

    /**
     * Gets Player 1's paddle.
     *
     * @return Player 1's Paddle instance
     */
    public Paddle getPlayerOnePaddle() { return playerOnePaddle; }

    /**
     * Gets Player 2's paddle.
     *
     * @return Player 2's Paddle instance
     */
    public Paddle getPlayerTwoPaddle() { return playerTwoPaddle; }

    /**
     * Simple defense bar class representing one life for a player.
     * Each bar can be destroyed once, representing life loss.
     */
    /**
     * Simple defense bar class representing one life for a player.
     * Each bar can be destroyed once, representing life loss.
     */
    private static final class DefenseBar {
        /** Flag indicating if this defense bar has been destroyed */
        private boolean destroyed = false;

        /**
         * Destroys this defense bar (life lost).
         */
        private void destroy() { destroyed = true; }

        /**
         * Checks if this defense bar has been destroyed.
         *
         * @return true if destroyed, false otherwise
         */
        private boolean isDestroyed() { return destroyed; }
    }
}
