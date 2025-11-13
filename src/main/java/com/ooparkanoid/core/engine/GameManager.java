
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


public class GameManager implements CollisionHandler.GameFlowCallbacks {
    private static GameManager instance;
    private Paddle paddle;
    private List<Ball> balls = new ArrayList<>();
    private List<Brick> bricks;
    private final List<Score> scores = new ArrayList<>();
    private final List<PowerUp> powerUps = new ArrayList<>();

    // --- Components ---
    private final GameStateManager stateManager;
    private LevelManager levelManager;
    private PowerUpEffectManager effectManager;
    private GameContext gameContext;
    private CollisionHandler collisionHandler;
    private GameRenderer gameRenderer;

    private double roundTimeElapsed;
    private double totalTimeElapsed;

    // FIX: Đã loại bỏ 'private int score' và 'private int lives'.
    // stateManager sẽ là nguồn đáng tin cậy duy nhất.

    private int currentLevel;
    private Random random;
    private boolean ballLaunched = false;
    private boolean isLosingLife = false;

    // Textures
    private Image normalBrickTexture;
    private Image indestructibleBrickTexture;
    private Image explosiveBrickTexture;

    public GameManager() {
        this(new GameStateManager());
    }

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

    private void loadBrickTextures() {
        ResourceManager rm = ResourceManager.getInstance();
        normalBrickTexture = rm.getImage("brick_normal.png");
        indestructibleBrickTexture = rm.getImage("brick_enternal.png");
        explosiveBrickTexture = rm.getImage("brick_explosive.png");
        // ... (load other textures)
    }

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    public void initializeGame() {
        double paddleStartX = Constants.PLAYFIELD_LEFT
                + (Constants.PLAYFIELD_WIDTH - Constants.PADDLE_WIDTH) / 2.0;
        paddle = new Paddle(paddleStartX, Constants.HEIGHT - 40);

        balls.clear();
        scores.clear();
        powerUps.clear();

        gameContext = new GameContext(paddle, balls);
        gameContext.setLivesModifier(amount -> {
            // FIX: Đọc và ghi vào stateManager
            int currentLives = stateManager.getLives();
            int currentScore = stateManager.getScore();
            currentLives += amount;
            stateManager.updateStats(currentScore, currentLives);
            System.out.println("❤️ Lives increased by " + amount + "! Total: " + currentLives);
        });

        this.effectManager = new PowerUpEffectManager(gameContext);

        this.collisionHandler = new CollisionHandler(stateManager, effectManager, this.scores, this);
        this.gameRenderer = new GameRenderer(effectManager);

        // FIX: Khởi tạo điểm và mạng trực tiếp vào stateManager
        int score = 0;
        int lives = Constants.START_LIVES;
        currentLevel = 1;
        roundTimeElapsed = 0;
        totalTimeElapsed = 0;
        isLosingLife = false;
        ballLaunched = false;
        loadLevel(currentLevel);
        resetBallAndPaddlePosition();

        collisionHandler.setGameObjects(paddle, balls, bricks, powerUps);
        gameRenderer.setGameObjects(paddle, balls, bricks, powerUps, scores);

        // Ghi trạng thái ban đầu
        stateManager.updateStats(score, lives);
        stateManager.setCurrentRound(currentLevel);
        stateManager.updateTimers(roundTimeElapsed, totalTimeElapsed);
        stateManager.setStatusMessage("Destroy all the bricks!");
    }

    private void loadLevel(int levelNum) {
        bricks = levelManager.createLevel(levelNum);
        if (this.bricks.isEmpty()) {
            System.err.println("Failed to load level " + levelNum + ". No bricks were created.");
        }
    }

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

        roundTimeElapsed += dt;
        totalTimeElapsed += dt;
        stateManager.updateTimers(roundTimeElapsed, totalTimeElapsed);

        paddle.update(dt);
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

        effectManager.update(dt);
        scores.removeIf(Score::isFinished);
        bricks.removeIf(Brick::isDestroyed);

        if (ballLaunched) {
            collisionHandler.handleCollisions(dt);
        }

        checkGameFlowConditions();
    }

    private void checkGameFlowConditions() {
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
                System.out.println("Congratulations! All levels completed!");
                recordHighScore(Constants.MAX_LEVELS);
                initializeGame();
            } else {
                System.out.println("Starting Level " + currentLevel);
                loadLevel(currentLevel);
                paddle.reset();
                resetBallAndPaddlePosition();
                roundTimeElapsed = 0;
                stateManager.setCurrentRound(currentLevel);
                stateManager.updateTimers(roundTimeElapsed, totalTimeElapsed);

                collisionHandler.setGameObjects(paddle, balls, bricks, powerUps);
                gameRenderer.setGameObjects(paddle, balls, bricks, powerUps, scores);
            }
            return;
        }

        // FIX: Đọc mạng từ stateManager
        if (stateManager.getLives() <= 0 && !isLosingLife && !paddle.isSpawning() ) {
            stateManager.setStatusMessage("Game Over! Final Score: " + stateManager.getScore());
            recordHighScore();
            stateManager.markGameOver();
        }
    }

    // --- Implement interface CollisionHandler.GameFlowCallbacks ---
    @Override
    public void loseLife() {
        if (!balls.isEmpty() || isLosingLife) return;
        isLosingLife = true;
        paddle.destroy();
        effectManager.clearAll();
        powerUps.clear();
        SoundManager.getInstance().play("lose_life");
    }

    @Override
    public void spawnPowerUp(double x, double y) {
        PowerUp powerUp = PowerUpFactory.createRandomPowerUp(x, y);
        if (powerUp != null) {
            powerUps.add(powerUp);
        }
    }
    // --- End implement interface ---

    private void recordHighScore() {
        recordHighScore(currentLevel);
    }

    private void recordHighScore(int roundsPlayed) {
        int clampedRounds = Math.max(1, Math.min(roundsPlayed, Constants.MAX_LEVELS));

        // FIX: Lấy điểm cuối cùng từ stateManager
        int finalScore = stateManager.getScore();
        ScoreEntry entry = new ScoreEntry(PlayerContext.playerName, finalScore, clampedRounds, totalTimeElapsed);

        FirebaseScoreService.submitScore(entry);
    }

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

    public void render(GraphicsContext g) {
        gameRenderer.render(g);
    }

    // --- Getters & Public Methods ---
    public Paddle getPaddle() {
        return paddle;
    }

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

    public List<Ball> getBalls() {
        return balls;
    }

    // FIX: Các getter này nên lấy từ stateManager
    public int getScore() {
        return stateManager.getScore();
    }

    public int getLives() {
        return stateManager.getLives();
    }

    public GameStateManager getStateManager() {
        return stateManager;
    }
}