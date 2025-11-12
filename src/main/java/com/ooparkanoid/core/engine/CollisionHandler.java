// File: src/main/java/com/ooparkanoid/core/engine/CollisionHandler.java
package com.ooparkanoid.core.engine;

import com.ooparkanoid.core.score.FirebaseScoreService;
import com.ooparkanoid.object.Laser;
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
import com.ooparkanoid.object.bricks.CollisionArea;
import com.ooparkanoid.utils.Constants;
import javafx.scene.paint.Color;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Handles all collision detection and resolution in the Arkanoid game.
 * Manages interactions between game objects including:
 * - Ball collisions with walls, paddle, and bricks
 * - Laser collisions with bricks
 * - Power-up collisions with paddle
 * - Explosion effects and chain reactions
 * - Score updates and visual feedback
 *
 * This class acts as a bridge between game objects and the orchestrator (GameManager),
 * delegating game flow decisions through callbacks while handling physics and scoring internally.
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public class CollisionHandler {
    /** Manages game state (score, lives, level progression) */
    private final GameStateManager stateManager;

    /** Manages power-up effects and their durations */
    private final PowerUpEffectManager effectManager;

    /** List of floating score indicators for visual feedback */
    private final List<Score> scores;

    /** Random number generator for power-up drops and effects */
    private final Random random;

    // References to game objects (injected by GameManager)
    private Paddle paddle;
    private List<Ball> balls;
    private List<Brick> bricks;
    private List<PowerUp> powerUps;

    /**
     * Callback interface for delegating game flow decisions to GameManager.
     * Separates collision detection logic from game state management.
     */
    public interface GameFlowCallbacks {
        /**
         * Called when a ball falls below the screen, indicating a life should be lost.
         */
        void loseLife();

        /**
         * Called when a brick is destroyed and may drop a power-up.
         *
         * @param x X coordinate for power-up spawn
         * @param y Y coordinate for power-up spawn
         */
        void spawnPowerUp(double x, double y);
    }

    /** Callbacks to orchestrator for game flow decisions */
    private final GameFlowCallbacks callbacks;

    /**
     * Constructs a CollisionHandler with required dependencies.
     *
     * @param stateManager manages game state (score, lives, level)
     * @param effectManager manages active power-up effects
     * @param scores list for spawning score popups
     * @param callbacks interface for delegating game flow decisions
     */
    public CollisionHandler(GameStateManager stateManager,
                            PowerUpEffectManager effectManager,
                            List<Score> scores,
                            GameFlowCallbacks callbacks) {
        this.stateManager = stateManager;
        this.effectManager = effectManager;
        this.scores = scores;
        this.random = new Random();
        this.callbacks = callbacks;
    }

    /**
     * Injects game object references from GameManager.
     * Must be called before handleCollisions() to avoid null pointer exceptions.
     *
     * @param paddle the player's paddle
     * @param balls list of active balls
     * @param bricks list of bricks on the current level
     * @param powerUps list of active power-ups
     */
    public void setGameObjects(Paddle paddle, List<Ball> balls, List<Brick> bricks, List<PowerUp> powerUps) {
        this.paddle = paddle;
        this.balls = balls;
        this.bricks = bricks;
        this.powerUps = powerUps;
    }

    /**
     * Main collision detection and resolution method.
     * Handles all game object interactions in proper order:
     * 1. Ball collisions (walls, paddle, bricks)
     * 2. Laser collisions with bricks
     * 3. Power-up collisions with paddle
     *
     * @param dt delta time since last update (currently unused but available for future physics)
     */
    public void handleCollisions(double dt) {
        if (balls.isEmpty()) return;

        handleBallUpdatesAndCollisions(dt);
        handleLaserUpdatesAndCollisions();
        handlePowerUpCollisionsAndCleanUp();
    }

    /**
     * Processes all ball movements and collision checks.
     * Handles wall bounces, paddle bounces, brick destruction, and life loss.
     *
     * @param dt delta time since last update
     */
    private void handleBallUpdatesAndCollisions(double dt) {
        Iterator<Ball> ballIt = balls.iterator();
        while (ballIt.hasNext()) {
            Ball ball = ballIt.next();

            // Check collisions in order of priority
            checkWallCollision(ball);
            checkPaddleCollision(ball);
            checkBrickCollision(ball);

            // Check for life loss (ball falls below screen)
            if (checkBottomCollision(ball)) {
                ballIt.remove();
                callbacks.loseLife(); // Delegate life loss to GameManager
            }
        }
    }

    /**
     * Checks and resolves ball collisions with screen boundaries.
     * Bounces the ball off left, right, and top walls with appropriate sound effects.
     *
     * @param ball the ball to check for wall collisions
     */
    private void checkWallCollision(Ball ball) {
        // Left wall
        if (ball.getX() <= Constants.PLAYFIELD_LEFT) {
            ball.setX(Constants.PLAYFIELD_LEFT);
            ball.setDirection(-ball.getDx(), ball.getDy());
            SoundManager.getInstance().play("bounce");
        }
        // Right wall
        if (ball.getX() + ball.getWidth() >= Constants.PLAYFIELD_RIGHT) {
            ball.setX(Constants.PLAYFIELD_RIGHT - ball.getWidth());
            ball.setDirection(-ball.getDx(), ball.getDy());
            SoundManager.getInstance().play("bounce");
        }
        // Top wall
        if (ball.getY() <= 0) {
            ball.setY(0);
            ball.setDirection(ball.getDx(), -ball.getDy());
            SoundManager.getInstance().play("bounce");
        }
    }

    /**
     * Checks and resolves ball collision with the paddle.
     * Calculates bounce angle based on where the ball hits the paddle,
     * creating more dynamic gameplay (hitting near edges = steeper angle).
     *
     * @param ball the ball to check for paddle collision
     */
    private void checkPaddleCollision(Ball ball) {
        if (paddle != null && ball.intersects(paddle) && ball.getDy() > 0) {
            // Push ball above paddle to prevent sticking
            ball.setY(paddle.getY() - ball.getHeight() - 1);

            // Calculate bounce angle based on hit position
            double paddleCenter = paddle.getX() + paddle.getWidth() / 2.0;
            double ballCenter = ball.getX() + ball.getWidth() / 2.0;
            double relativeIntersect = (ballCenter - paddleCenter) / (paddle.getWidth() / 2.0);

            // Apply angle with max 60 degrees
            double maxBounceAngle = Math.toRadians(60);
            double bounceAngle = relativeIntersect * maxBounceAngle;
            double speed = ball.getSpeed();

            double newDx = speed * Math.sin(bounceAngle);
            double newDy = -Math.abs(speed * Math.cos(bounceAngle));

            ball.setDirection(newDx, newDy);
            SoundManager.getInstance().play("bounce");
        }
    }

    /**
     * Checks and resolves ball collisions with bricks.
     * Handles brick damage, destruction, scoring, explosions, and power-up drops.
     * Supports FireBall power-up which allows ball to pass through multiple bricks.
     *
     * @param ball the ball to check for brick collisions
     */
    private void checkBrickCollision(Ball ball) {
        Iterator<Brick> brickIterator = bricks.iterator();
        boolean hasFireBall = effectManager.isEffectActive("FIRE_BALL");
        int currentScore = stateManager.getScore();
        int currentLives = stateManager.getLives();

        while (brickIterator.hasNext()) {
            Brick brick = brickIterator.next();

            if (!brick.isDestroyed() && ball.collidesWith(brick)) {
                Brick.BrickType hitBrickType = brick.getType();
                boolean brickWasDestroyed = brick.isDestroyed();

                // Apply damage to brick
                brick.takeHit();

                // Bounce ball off brick (unless FireBall is active)
                if (!hasFireBall) {
                    handleBallBounceOffBrick(ball, brick);
                }

                // Handle brick destruction effects
                if (!brickWasDestroyed && brick.isDestroyed()) {
                    int multiplier = effectManager.isEffectActive("SCORE_MULTIPLIER") ? 2 : 1;
                    int points = 10 * multiplier;

                    // Update score and UI
                    currentScore += points;
                    stateManager.updateStats(currentScore, currentLives);
                    SoundManager.getInstance().play("break");
                    spawnScorePopup(Integer.toString(points), brick.getX() + brick.getWidth() / 2, brick.getY());

                    // Handle explosion effect for explosive bricks
                    if (hitBrickType == Brick.BrickType.EXPLOSIVE) {
                        handleExplosion(brick.getX(), brick.getY());
                    }

                    // Random power-up drop
                    if (random.nextDouble() < Constants.POWERUP_DROP_CHANCE) {
                        callbacks.spawnPowerUp(brick.getX() + brick.getWidth() / 2, brick.getY() + brick.getHeight() / 2);
                    }
                }

                // Stop checking collisions unless FireBall is active (allows pass-through)
                if (!hasFireBall) break;
            }
        }
    }

    /**
     * Calculates and applies bounce direction when ball hits a brick.
     * Uses overlap detection to determine which side of the brick was hit,
     * then bounces the ball appropriately and adjusts position to prevent sticking.
     *
     * @param ball the ball that hit the brick
     * @param brick the brick that was hit
     */
    private void handleBallBounceOffBrick(Ball ball, Brick brick) {
        double ballCenterX = ball.getX() + ball.getRadius();
        double ballCenterY = ball.getY() + ball.getRadius();

        double brickCenterX = brick.getX() + brick.getWidth() / 2;
        double brickCenterY = brick.getY() + brick.getHeight() / 2;

        // Calculate overlap on both axes
        double overlapX = (ball.getRadius() + brick.getWidth() / 2)
                - Math.abs(ballCenterX - brickCenterX);
        double overlapY = (ball.getRadius() + brick.getHeight() / 2)
                - Math.abs(ballCenterY - brickCenterY);

        // Bounce based on smallest overlap (side of collision)
        if (overlapX < overlapY) {
            // Horizontal collision (left or right side)
            if (ballCenterX < brickCenterX) {
                ball.setX(brick.getX() - ball.getWidth() - 1);
            } else {
                ball.setX(brick.getX() + brick.getWidth() + 1);
            }
            ball.setDirection(-ball.getDx(), ball.getDy());
        } else {
            // Vertical collision (top or bottom side)
            if (ballCenterY < brickCenterY) {
                ball.setY(brick.getY() - ball.getHeight() - 1);
            } else {
                ball.setY(brick.getY() + brick.getHeight() + 1);
            }
            ball.setDirection(ball.getDx(), -ball.getDy());
        }
    }

    /**
     * Checks and resolves collisions between lasers and bricks.
     * Handles brick damage, destruction, scoring, and explosion effects.
     * Removes lasers upon collision with bricks.
     */
    private void handleLaserUpdatesAndCollisions() {
        if (paddle == null) return;
        List<Laser> lasers = paddle.getLasers();
        if (lasers.isEmpty()) return;

        Iterator<Laser> laserIt = lasers.iterator();
        int currentScore = stateManager.getScore();
        int currentLives = stateManager.getLives();

        while (laserIt.hasNext()) {
            Laser laser = laserIt.next();

            Iterator<Brick> brickIt = bricks.iterator();
            while (brickIt.hasNext()) {
                Brick brick = brickIt.next();
                if (!brick.isDestroyed() && laser.intersects(brick)) {
                    Brick.BrickType hitBrickType = brick.getType();
                    boolean brickWasDestroyed = brick.isDestroyed();

                    brick.takeHit();
                    SoundManager.getInstance().play("laser_hit");

                    if (!brickWasDestroyed && brick.isDestroyed()) {
                        int multiplier = effectManager.isEffectActive("SCORE_MULTIPLIER") ? 2 : 1;
                        int points = 10 * multiplier;
                        currentScore += points;
                        stateManager.updateStats(currentScore, currentLives);
                        SoundManager.getInstance().play("break");
                        spawnScorePopup(Integer.toString(points), brick.getX() + brick.getWidth() / 2, brick.getY());

                        if (hitBrickType == Brick.BrickType.EXPLOSIVE) {
                            handleExplosion(brick.getX(), brick.getY());
                        }
                    }

                    laserIt.remove();
                    break;
                }
            }
        }
    }

    /**
     * Handles explosion effects from explosive bricks.
     * Destroys all bricks in a 3x3 grid area around the explosion center.
     * Awards points for destroyed bricks and may spawn power-ups.
     * Indestructible bricks are immune to explosion damage.
     *
     * @param explosionX X coordinate of explosion center
     * @param explosionY Y coordinate of explosion center
     */
    public void handleExplosion(double explosionX, double explosionY) {
        // Define explosion area (3x3 grid of bricks)
        CollisionArea explosionZone = new CollisionArea(
                explosionX - Constants.BRICK_WIDTH - Constants.BRICK_PADDING_X,
                explosionY - Constants.BRICK_HEIGHT - Constants.BRICK_PADDING_Y,
                Constants.BRICK_WIDTH * 3 + Constants.BRICK_PADDING_X * 2,
                Constants.BRICK_HEIGHT * 3 + Constants.BRICK_PADDING_Y * 2
        );

        int currentScore = stateManager.getScore();
        int currentLives = stateManager.getLives();

        for (Brick brick : bricks) {
            if (!brick.isDestroyed() && brick.intersects(explosionZone)) {
                if (brick.getType() != Brick.BrickType.INDESTRUCTIBLE) {
                    brick.takeHit();
                    if (brick.isDestroyed()) {
                        int multiplier = effectManager.isEffectActive("SCORE_MULTIPLIER") ? 2 : 1;
                        int points = 10 * multiplier;
                        currentScore += points;
                        stateManager.updateStats(currentScore, currentLives);
                        SoundManager.getInstance().play("break");
                        spawnScorePopup(Integer.toString(points), brick.getX() + brick.getWidth() / 2, brick.getY());

                        // Reduced chance for power-up drops from explosion
                        if (random.nextDouble() < Constants.POWERUP_DROP_CHANCE / 2) {
                            callbacks.spawnPowerUp(brick.getX() + brick.getWidth() / 2, brick.getY() + brick.getHeight() / 2);
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks for power-up collisions with paddle and handles collection.
     * Removes power-ups that are collected or fall off screen.
     */
    private void handlePowerUpCollisionsAndCleanUp() {
        Iterator<PowerUp> it = powerUps.iterator();

        while (it.hasNext()) {
            PowerUp powerUp = it.next();

            // Check collision with paddle
            if (!powerUp.isCollected() && powerUp.intersects(paddle)) {
                powerUp.collect();
                SoundManager.getInstance().play("powerup");
                effectManager.activateEffect(
                        powerUp.getEffect(),
                        powerUp.getDuration()
                );
            }

            // Remove if collected or out of screen
            if (powerUp.isCollected() || powerUp.getY() > Constants.HEIGHT) {
                it.remove();
            }
        }
    }

    /**
     * Checks if ball has fallen below the screen bottom.
     * If invincible power-up is active, ball bounces back instead of being lost.
     *
     * @param ball the ball to check
     * @return true if ball should be removed (life lost), false otherwise
     */
    private boolean checkBottomCollision(Ball ball) {
        if (ball.getY() + ball.getHeight() >= Constants.HEIGHT) {
            boolean invincible = effectManager.isEffectActive("INVINCIBLE_BALL");

            if (invincible) {
                // Bounce back if invincible
                ball.setY(Constants.HEIGHT - ball.getHeight());
                ball.setDirection(ball.getDx(), -Math.abs(ball.getDy()));
                return false; // Ball not lost
            } else {
                return true; // Ball lost
            }
        }
        return false;
    }

    /**
     * Spawns a floating score popup at the specified location.
     * Provides visual feedback for points earned.
     *
     * @param point score value to display
     * @param x X coordinate for popup
     * @param y Y coordinate for popup
     */
    private void spawnScorePopup(String point, double x, double y) {
        scores.add(new Score(point, x, y, Color.CYAN));
    }
}
