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

public class CollisionHandler {
    private final GameStateManager stateManager;
    private final PowerUpEffectManager effectManager;
    private final List<Score> scores;
    private final Random random;

    // References to game objects
    private Paddle paddle;
    private List<Ball> balls;
    private List<Brick> bricks;
    private List<PowerUp> powerUps;

    // Callbacks to GameManager (Orchestrator)
    public interface GameFlowCallbacks {
        void loseLife();
        void spawnPowerUp(double x, double y);
    }

    private final GameFlowCallbacks callbacks;

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

    public void setGameObjects(Paddle paddle, List<Ball> balls, List<Brick> bricks, List<PowerUp> powerUps) {
        this.paddle = paddle;
        this.balls = balls;
        this.bricks = bricks;
        this.powerUps = powerUps;
    }

    /**
     * Xử lý tất cả va chạm trong game: Paddle, Ball, Brick, Laser, PowerUp.
     */
    public void handleCollisions(double dt) {
        if (balls.isEmpty()) return;

        handleBallUpdatesAndCollisions(dt);
        handleLaserUpdatesAndCollisions();
        handlePowerUpCollisionsAndCleanUp();
    }

    private void handleBallUpdatesAndCollisions(double dt) {
        Iterator<Ball> ballIt = balls.iterator();
        while (ballIt.hasNext()) {
            Ball ball = ballIt.next();

            // 1. Kiểm tra Va chạm với biên màn hình
            checkWallCollision(ball);

            // 2. Kiểm tra va chạm với Paddle
            checkPaddleCollision(ball);

            // 3. Kiểm tra va chạm với Bricks
            checkBrickCollision(ball);

            // 4. Kiểm tra mất mạng (rơi khỏi đáy)
            if (checkBottomCollision(ball)) {
                ballIt.remove();
                callbacks.loseLife(); // Ủy quyền việc mất mạng cho GameManager
            }
        }
    }

    private void checkWallCollision(Ball ball) {
        // Trái
        if (ball.getX() <= Constants.PLAYFIELD_LEFT) {
            ball.setX(Constants.PLAYFIELD_LEFT);
            ball.setDirection(-ball.getDx(), ball.getDy());
            SoundManager.getInstance().play("bounce");
        }
        // Phải
        if (ball.getX() + ball.getWidth() >= Constants.PLAYFIELD_RIGHT) {
            ball.setX(Constants.PLAYFIELD_RIGHT - ball.getWidth());
            ball.setDirection(-ball.getDx(), ball.getDy());
            SoundManager.getInstance().play("bounce");
        }
        // Trần
        if (ball.getY() <= 0) {
            ball.setY(0);
            ball.setDirection(ball.getDx(), -ball.getDy());
            SoundManager.getInstance().play("bounce");
        }
    }

    private void checkPaddleCollision(Ball ball) {
        if (paddle != null && ball.istersected(paddle) && ball.getDy() > 0) {
            // Đẩy bóng lên trên paddle một chút để tránh kẹt
            ball.setY(paddle.getY() - ball.getHeight() - 1);

            double paddleCenter = paddle.getX() + paddle.getWidth() / 2.0;
            double ballCenter = ball.getX() + ball.getWidth() / 2.0;
            double relativeIntersect = (ballCenter - paddleCenter) / (paddle.getWidth() / 2.0);

            double maxBounceAngle = Math.toRadians(60);
            double bounceAngle = relativeIntersect * maxBounceAngle;
            double speed = ball.getSpeed();

            double newDx = speed * Math.sin(bounceAngle);
            double newDy = -Math.abs(speed * Math.cos(bounceAngle));

            ball.setDirection(newDx, newDy);
            SoundManager.getInstance().play("bounce");
        }
    }

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

                // Gạch nhận hit
                brick.takeHit();

                // Xử lý nảy bóng nếu không phải FireBall
                if (!hasFireBall) {
                    handleBallBounceOffBrick(ball, brick);
                }

                // Xử lý khi gạch bị phá hủy (Sau khi takeHit())
                if (!brickWasDestroyed && brick.isDestroyed()) {
                    int multiplier = effectManager.isEffectActive("SCORE_MULTIPLIER") ? 2 : 1;
                    int points = 10 * multiplier;

                    // Cập nhật điểm và UI
                    currentScore += points;
                    stateManager.updateStats(currentScore, currentLives);
                    SoundManager.getInstance().play("break");
                    spawnScorePopup(Integer.toString(points), brick.getX() + brick.getWidth() / 2, brick.getY());

                    // Xử lý nổ (Explosion)
                    if (hitBrickType == Brick.BrickType.EXPLOSIVE) {
                        handleExplosion(brick.getX(), brick.getY());
                    }

                    // Rơi Power-Up
                    if (random.nextDouble() < Constants.POWERUP_DROP_CHANCE) {
                        callbacks.spawnPowerUp(brick.getX() + brick.getWidth() / 2, brick.getY() + brick.getHeight() / 2);
                    }
                }
                else if (!brick.isDestroyed()) {
                    SoundManager.getInstance().play("collision");
                }

                // Dừng kiểm tra va chạm nếu va chạm đã xảy ra (và không phải FireBall đang xuyên qua)
                if (!hasFireBall) break;
            }
        }
    }

    private void handleBallBounceOffBrick(Ball ball, Brick brick) {
        double ballCenterX = ball.getX() + ball.getRadius();
        double ballCenterY = ball.getY() + ball.getRadius();

        double brickCenterX = brick.getX() + brick.getWidth() / 2;
        double brickCenterY = brick.getY() + brick.getHeight() / 2;

        double overlapX = (ball.getRadius() + brick.getWidth() / 2)
                - Math.abs(ballCenterX - brickCenterX);
        double overlapY = (ball.getRadius() + brick.getHeight() / 2)
                - Math.abs(ballCenterY - brickCenterY);

        if (overlapX < overlapY) {
            if (ballCenterX < brickCenterX) {
                ball.setX(brick.getX() - ball.getWidth() - 1);
            } else {
                ball.setX(brick.getX() + brick.getWidth() + 1);
            }
            ball.setDirection(-ball.getDx(), ball.getDy());
        } else {
            if (ballCenterY < brickCenterY) {
                ball.setY(brick.getY() - ball.getHeight() - 1);
            } else {
                ball.setY(brick.getY() + brick.getHeight() + 1);
            }
            ball.setDirection(ball.getDx(), -ball.getDy());
        }
    }

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
                if (!brick.isDestroyed() && laser.istersected(brick)) {
                    Brick.BrickType hitBrickType = brick.getType();
                    boolean brickWasDestroyed = brick.isDestroyed();

                    brick.takeHit();

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
                    } else {
                        SoundManager.getInstance().play("laser_hit");
                    }

                    laserIt.remove();
                    break;
                }
            }
        }
    }

    /**
     * Xử lý hiệu ứng nổ: phá hủy các gạch xung quanh (3x3).
     */
    public void handleExplosion(double explosionX, double explosionY) {
        CollisionArea explosionZone = new CollisionArea(
                explosionX - Constants.BRICK_WIDTH - Constants.BRICK_PADDING_X,
                explosionY - Constants.BRICK_HEIGHT - Constants.BRICK_PADDING_Y,
                Constants.BRICK_WIDTH * 3 + Constants.BRICK_PADDING_X * 2,
                Constants.BRICK_HEIGHT * 3 + Constants.BRICK_PADDING_Y * 2
        );

        int currentScore = stateManager.getScore();
        int currentLives = stateManager.getLives();

        for (Brick brick : bricks) {
            if (!brick.isDestroyed() && brick.istersected(explosionZone)) {
                if (brick.getType() != Brick.BrickType.INDESTRUCTIBLE) {
                    brick.takeHit();
                    if (brick.isDestroyed()) {
                        int multiplier = effectManager.isEffectActive("SCORE_MULTIPLIER") ? 2 : 1;
                        int points = 10 * multiplier;
                        currentScore += points;
                        stateManager.updateStats(currentScore, currentLives);
                        SoundManager.getInstance().play("break");
                        spawnScorePopup(Integer.toString(points), brick.getX() + brick.getWidth() / 2, brick.getY());

                        if (random.nextDouble() < Constants.POWERUP_DROP_CHANCE / 2) {
                            callbacks.spawnPowerUp(brick.getX() + brick.getWidth() / 2, brick.getY() + brick.getHeight() / 2);
                        }
                    }
                }
            }
        }
    }

    private void handlePowerUpCollisionsAndCleanUp() {
        Iterator<PowerUp> it = powerUps.iterator();

        while (it.hasNext()) {
            PowerUp powerUp = it.next();

            // Check collision with paddle
            if (!powerUp.isCollected() && powerUp.istersected(paddle)) {
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

    private boolean checkBottomCollision(Ball ball) {
        if (ball.getY() + ball.getHeight() >= Constants.HEIGHT) {
            boolean invincible = effectManager.isEffectActive("INVINCIBLE_BALL");

            if (invincible) {
                // Bounce back if invincible
                ball.setY(Constants.HEIGHT - ball.getHeight());
                ball.setDirection(ball.getDx(), -Math.abs(ball.getDy()));
                return false; // Không bị mất bóng
            } else {
                return true; // Bị mất bóng
            }
        }
        return false;
    }

    private void spawnScorePopup(String point, double x, double y) {
        scores.add(new Score(point, x, y, Color.CYAN));
    }
}