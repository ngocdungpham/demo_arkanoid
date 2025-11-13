package com.ooparkanoid.core.engine;

import com.ooparkanoid.core.score.FirebaseScoreService;
import com.ooparkanoid.object.Score;
import com.ooparkanoid.sound.SoundManager;
import com.ooparkanoid.core.state.PlayerContext;

import com.ooparkanoid.core.save.SaveService;
import com.ooparkanoid.core.score.ScoreEntry;
import com.ooparkanoid.core.state.GameStateManager;
import com.ooparkanoid.object.Ball;
import com.ooparkanoid.object.Paddle;
import com.ooparkanoid.object.PowerUp.GameContext;
import com.ooparkanoid.object.PowerUp.PowerUp;
import com.ooparkanoid.object.PowerUp.PowerUpEffectManager;
import com.ooparkanoid.object.PowerUp.PowerUpFactory;
import com.ooparkanoid.object.bricks.Brick;

import com.ooparkanoid.utils.Constants;
import javafx.scene.canvas.GraphicsContext;

import com.ooparkanoid.graphics.ResourceManager;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Central orchestrator for the Arkanoid game logic.
 * Manages the game loop, coordinates all game systems, and delegates responsibilities to specialized components:
 * - Game state management (score, lives, level progression)
 * - Collision detection and resolution
 * - Level loading and progression
 * - Power-up effects and timers
 * - Rendering coordination
 * - Game flow control (life loss, level completion, game over)
 * <p>
 * This class follows the Single Responsibility Principle by delegating specific tasks
 * to dedicated managers (CollisionHandler, LevelManager, PowerUpEffectManager, GameRenderer).
 * <p>
 * Design Pattern: Implements Singleton pattern and Observer pattern (via callbacks).
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public class GameManager implements CollisionHandler.GameFlowCallbacks {
    /**
     * Singleton instance
     */
    private static GameManager instance;

    // ==================== Game Objects ====================
    /**
     * Player-controlled paddle
     */
    private Paddle paddle;

    /**
     * Active balls in play
     */
    private List<Ball> balls = new ArrayList<>();

    /**
     * Bricks in current level
     */
    private List<Brick> bricks;

    /**
     * Floating score indicators for visual feedback
     */
    private final List<Score> scores = new ArrayList<>();

    /**
     * Active power-ups falling on screen
     */
    private final List<PowerUp> powerUps = new ArrayList<>();

    // ==================== Core Systems ====================
    /**
     * Manages game state (score, lives, UI updates) - single source of truth
     */
    private final GameStateManager stateManager;

    /**
     * Handles level loading and brick creation
     */
    private LevelManager levelManager;

    /**
     * Manages power-up effects and their durations
     */
    private PowerUpEffectManager effectManager;

    /**
     * Provides context to power-up effects (access to paddle and balls)
     */
    private GameContext gameContext;

    /**
     * Handles all collision detection and resolution
     */
    private CollisionHandler collisionHandler;

    /**
     * Handles rendering of game objects and UI elements
     */
    private GameRenderer gameRenderer;

    // ==================== Game State ====================
    /**
     * Time elapsed in current round/level (seconds)
     */
    private double roundTimeElapsed;

    /**
     * Total time elapsed across all rounds (seconds)
     */
    private double totalTimeElapsed;

    /**
     * Current level number (1-based)
     */
    private int currentLevel;

    /**
     * Random number generator for game logic
     */
    private Random random;

    /**
     * Flag indicating whether ball has been launched from paddle
     */
    private boolean ballLaunched = false;
    private boolean isLosingLife = false;

    // ==================== Visual Assets ====================
    /**
     * Texture for normal bricks
     */
    private Image normalBrickTexture;

    /**
     * Texture for indestructible bricks
     */
    private Image indestructibleBrickTexture;

    /**
     * Texture for explosive bricks
     */
    private Image explosiveBrickTexture;

    public GameManager() {
        this(new GameStateManager());
    }

    /**
     * Constructs a GameManager with specified state manager.
     * Initializes all game systems and loads resources.
     *
     * @param stateManager the state manager for tracking game progress
     */
    public GameManager(GameStateManager stateManager) {
        this.stateManager = stateManager;
        this.bricks = new ArrayList<>();
        this.random = new Random();

        loadBrickTextures();

        this.levelManager = new LevelManager(
                normalBrickTexture,
                indestructibleBrickTexture,
                explosiveBrickTexture
        );

        this.effectManager = new PowerUpEffectManager(null);
        this.collisionHandler = new CollisionHandler(stateManager, effectManager, this.scores, this);
        this.gameRenderer = new GameRenderer(effectManager);

        initializeGame();
    }

    /**
     * Loads all brick textures from the resource manager.
     * Textures are cached for efficient rendering.
     */
    private void loadBrickTextures() {
        ResourceManager rm = ResourceManager.getInstance();
        normalBrickTexture = rm.getImage("brick_normal.png");
        indestructibleBrickTexture = rm.getImage("brick_enternal.png");
        explosiveBrickTexture = rm.getImage("brick_explosive.png");
    }

    /**
     * Gets the singleton instance of GameManager.
     * Creates a new instance if none exists.
     *
     * @return the singleton GameManager instance
     */
    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    /**
     * Initializes or resets the entire game to starting state.
     * Sets up paddle, balls, bricks, and all game systems.
     * Loads the first level and prepares for gameplay.
     * <p>
     * This method is called on game start and when restarting after game over.
     */
    public void initializeGame() {
        // Initialize paddle at center bottom
        double paddleStartX = Constants.PLAYFIELD_LEFT
                + (Constants.PLAYFIELD_WIDTH - Constants.PADDLE_WIDTH) / 2.0;
        paddle = new Paddle(paddleStartX, Constants.HEIGHT - 40);

        // Clear all game object lists
        balls.clear();
        scores.clear();
        powerUps.clear();

        // Set up game context for power-up effects
        gameContext = new GameContext(paddle, balls);
        gameContext.setLivesModifier(amount -> {
            // Modify lives through state manager (single source of truth)
            int currentLives = stateManager.getLives();
            int currentScore = stateManager.getScore();
            currentLives += amount;
            stateManager.updateStats(currentScore, currentLives);
            System.out.println("❤️ Lives increased by " + amount + "! Total: " + currentLives);
        });

        // Reinitialize game systems with new context
        this.effectManager = new PowerUpEffectManager(gameContext);
        this.collisionHandler = new CollisionHandler(stateManager, effectManager, this.scores, this);
        this.gameRenderer = new GameRenderer(effectManager);

        // Initialize game state
        int score = 0;
        int lives = Constants.START_LIVES;
        currentLevel = 1;
        roundTimeElapsed = 0;
        totalTimeElapsed = 0;
        isLosingLife = false;
        ballLaunched = false;

        // Load first level and reset ball position
        loadLevel(currentLevel);
        resetBallAndPaddlePosition();

        // Wire up game systems with game objects
        collisionHandler.setGameObjects(paddle, balls, bricks, powerUps);
        gameRenderer.setGameObjects(paddle, balls, bricks, powerUps, scores);

        // Update UI with initial state
        stateManager.updateStats(score, lives);
        stateManager.setCurrentRound(currentLevel);
        stateManager.updateTimers(roundTimeElapsed, totalTimeElapsed);
        stateManager.setStatusMessage("Destroy all the bricks!");
    }

    /**
     * Loads a specific level by number.
     * Clears existing bricks and creates new level layout.
     *
     * @param levelNum the level number to load (1-based)
     */
    private void loadLevel(int levelNum) {
        bricks = levelManager.createLevel(levelNum);
        if (this.bricks.isEmpty()) {
            System.err.println("Failed to load level " + levelNum + ". No bricks were created.");
        }
    }

    /**
     * Main game loop update method called every frame.
     * Updates all game objects, handles collisions, and checks game flow conditions.
     * Only processes updates when game is running (not paused or game over).
     *
     * @param dt delta time in seconds since last frame
     */
    public void update(double dt) {
        if (!stateManager.isRunning()) {
            return;
        }
        paddle.update(dt);

        if (isLosingLife) {
            // Chỉ update paddle (để chạy animation nổ)
            if (paddle.isDestroyed()) {
                return;
            }
            isLosingLife = false;
            int currentScore = stateManager.getScore();
            int currentLives = stateManager.getLives();

            currentLives--;
            stateManager.updateStats(currentScore, currentLives);

            if (currentLives > 0) {
                resetBallAndPaddlePosition(); // Reset game
                stateManager.setStatusMessage("Lives remaining: " + currentLives);
            }
            // DỪNG update game chính khi đang nổ
            return;
        }

        // Update timers
        roundTimeElapsed += dt;
        totalTimeElapsed += dt;
        stateManager.updateTimers(roundTimeElapsed, totalTimeElapsed);

        // Update all game objects
        paddle.update(dt);

        // Update balls (stick to paddle if not launched yet)
        for (Ball b : balls) {
            if (!ballLaunched) {
                b.setX(paddle.getX() + paddle.getWidth() / 2 - b.getWidth() / 2);
                b.setY(paddle.getY() - b.getHeight() - 2);
            }
            b.update(dt);
        }

        for (PowerUp p : powerUps) p.update(dt);
        for (Score s : scores) s.update(dt);
        for (Brick b : bricks) b.update(dt);

        // Update power-up effects and timers
        effectManager.update(dt);

        // Clean up finished objects
        scores.removeIf(Score::isFinished);
        bricks.removeIf(Brick::isDestroyed);

        // Handle collisions only when ball is in play
        if (ballLaunched) {
            collisionHandler.handleCollisions(dt);
        }

        // Check for level completion or game over
        checkGameFlowConditions();
    }

    /**
     * Checks game flow conditions (level completion, game over).
     * Handles level progression and game over logic.
     * Called every frame after all updates.
     */
    private void checkGameFlowConditions() {
        // Check if all destroyable bricks are destroyed (level complete)
        boolean allDestroyableBricksDestroyed = true;
        for (Brick brick : bricks) {
            if (brick.getType() != Brick.BrickType.INDESTRUCTIBLE && !brick.isDestroyed()) {
                allDestroyableBricksDestroyed = false;
                break;
            }
        }

        if (allDestroyableBricksDestroyed) {
            currentLevel++;
            powerUps.clear();
            effectManager.clearAll();

            if (currentLevel > Constants.MAX_LEVELS) {
                // All levels completed - Victory!
                System.out.println("Congratulations! All levels completed!");
                recordHighScore(Constants.MAX_LEVELS);
                initializeGame(); // Restart game
            } else {
                // Load next level
                System.out.println("Starting Level " + currentLevel);
                loadLevel(currentLevel);
                paddle.reset();
                resetBallAndPaddlePosition();
                roundTimeElapsed = 0;
                stateManager.setCurrentRound(currentLevel);
                stateManager.updateTimers(roundTimeElapsed, totalTimeElapsed);

                // Rewire systems with new level data
                collisionHandler.setGameObjects(paddle, balls, bricks, powerUps);
                gameRenderer.setGameObjects(paddle, balls, bricks, powerUps, scores);
            }
            return;
        }

        // FIX: Đọc mạng từ stateManager
        if (stateManager.getLives() <= 0 && !isLosingLife && !paddle.isSpawning()) {
            // Check for game over (no lives remaining)
            if (stateManager.getLives() <= 0) {
                stateManager.setStatusMessage("Game Over! Final Score: " + stateManager.getScore());
                recordHighScore();
                stateManager.markGameOver();
            }
        }
    }

    // ==================== GameFlowCallbacks Implementation ====================

    /**
     * Handles life loss when all balls fall off screen.
     * Clears active effects, decrements lives, and either resets ball position or triggers game over.
     * Called by CollisionHandler via callback interface when last ball is lost.
     * <p>
     * Implementation of GameFlowCallbacks.loseLife()
     */
    @Override
    public void loseLife() {
        if (!balls.isEmpty() || isLosingLife) return;
        isLosingLife = true;
        paddle.destroy();
        if (!balls.isEmpty()) return;
    }

    /**
     * Spawns a random power-up at the specified location.
     * Called by CollisionHandler when a brick is destroyed and power-up drop is triggered.
     * <p>
     * Implementation of GameFlowCallbacks.spawnPowerUp()
     *
     * @param x X coordinate for power-up spawn (typically brick center)
     * @param y Y coordinate for power-up spawn (typically brick center)
     */
    @Override
    public void spawnPowerUp(double x, double y) {
        PowerUp powerUp = PowerUpFactory.createRandomPowerUp(x, y);
        if (powerUp != null) {
            powerUps.add(powerUp);
        }
    }

    // ==================== High Score Management ====================

    /**
     * Records the current high score to Firebase.
     * Uses current level as rounds played.
     */
    private void recordHighScore() {
        recordHighScore(currentLevel);
    }

    /**
     * Records high score to Firebase with specified round count.
     * Clamps rounds to valid range (1 to MAX_LEVELS) and submits score entry.
     *
     * @param roundsPlayed number of rounds/levels completed
     */
    private void recordHighScore(int roundsPlayed) {
        int clampedRounds = Math.max(1, Math.min(roundsPlayed, Constants.MAX_LEVELS));

        // Get final score from state manager (single source of truth)
        int finalScore = stateManager.getScore();
        ScoreEntry entry = new ScoreEntry(PlayerContext.playerName, finalScore, clampedRounds, totalTimeElapsed);

        FirebaseScoreService.submitScore(entry);
    }

    // ==================== Position Reset ====================

    /**
     * Resets ball and paddle to starting positions.
     * Places paddle at center of playfield and creates a new ball on the paddle.
     * Ball must be launched again by player input.
     * Called after life loss or level completion.
     */
    private void resetBallAndPaddlePosition() {
        double paddleStartX = Constants.PLAYFIELD_LEFT
                + (Constants.PLAYFIELD_WIDTH - Constants.PADDLE_WIDTH) / 2.0;
        paddle.setX(paddleStartX);
        paddle.setDx(0);
        balls.clear();
        Ball newBall = new Ball(
                Constants.PLAYFIELD_LEFT + Constants.PLAYFIELD_WIDTH / 2.0,
                Constants.HEIGHT / 2.0,
                Constants.BALL_RADIUS,
                Constants.DEFAULT_SPEED,
                0, -1
        );
        balls.add(newBall);
        ballLaunched = false;
    }

    // ==================== Rendering ====================

    /**
     * Renders all game objects and UI elements to the graphics context.
     * Delegates to GameRenderer for actual drawing operations.
     *
     * @param g the GraphicsContext to render to
     */
    public void render(GraphicsContext g) {
        gameRenderer.render(g);
    }

    // ==================== Public API ====================

    /**
     * Gets the player's paddle.
     *
     * @return the Paddle instance
     */
    public Paddle getPaddle() {
        return paddle;
    }

    /**
     * Launches the ball from the paddle with initial velocity.
     * Ball is propelled upward with a random horizontal direction for variety.
     * Can only be called once per ball life until reset.
     */
    public void launchBall() {
        if (isLosingLife || paddle.isSpawning()) { // (isSpawning() từ Paddle.java)
            return;
        }
        if (!ballLaunched) {
            ballLaunched = true;
            for (Ball b : balls) {
                b.setDirection(random.nextDouble() * 1.4 - 0.7, -1);
            }
        }
    }

    /**
     * Spawns an extra ball above the paddle.
     * Extra ball will have random initial direction if ball is already launched.
     * Used by multi-ball power-up.
     */
    public void spawnExtraBall() {
        Ball newBall = new Ball(
                paddle.getX() + paddle.getWidth() / 2.0,
                paddle.getY() - 20,
                Constants.BALL_RADIUS,
                Constants.DEFAULT_SPEED,
                -1, -1
        );
        if (ballLaunched) {
            newBall.setDirection(random.nextDouble() > 0.5 ? 0.7 : -0.7, -1);
        }
        balls.add(newBall);
    }

    /**
     * Gets the list of active balls.
     *
     * @return list of Ball objects
     */
    public List<Ball> getBalls() {
        return balls;
    }

    /**
     * Gets the current score from state manager.
     *
     * @return current score
     */
    public int getScore() {
        return stateManager.getScore();
    }

    /**
     * Gets the current lives from state manager.
     *
     * @return remaining lives
     */
    public int getLives() {
        return stateManager.getLives();
    }

    /**
     * Gets the game state manager.
     *
     * @return the GameStateManager instance
     */
    public GameStateManager getStateManager() {
        return stateManager;
    }
}