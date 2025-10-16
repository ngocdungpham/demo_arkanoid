// File: src/main/java/com/ooparkanoid/core/engine/GameManager.java
package com.ooparkanoid.core.engine;

import com.ooparkanoid.core.save.SaveService;
import com.ooparkanoid.core.state.GameState;
import com.ooparkanoid.core.state.GameStateManager;
import com.ooparkanoid.object.Ball;
import com.ooparkanoid.object.Paddle;
import com.ooparkanoid.object.PowerUp.GameContext;
import com.ooparkanoid.object.PowerUp.PowerUp;
import com.ooparkanoid.object.PowerUp.PowerUpEffectManager;
import com.ooparkanoid.object.PowerUp.PowerUpFactory;
import com.ooparkanoid.object.bricks.Brick; // Import Brick
import com.ooparkanoid.object.bricks.NormalBrick; // Import NormalBrick
import com.ooparkanoid.object.bricks.StrongBrick; // Import StrongBrick
import com.ooparkanoid.object.bricks.IndestructibleBrick;

import com.ooparkanoid.utils.Constants;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * GameManager chịu trách nhiệm quản lý logic game:
 * - Khởi tạo và quản lý Paddle, Ball, Bricks
 * - Cập nhật trạng thái và xử lý va chạm
 * - Vẽ các đối tượng
 */
public class GameManager {
    private static GameManager instance;
    private Paddle paddle;
    private Ball ball;
    private List<Brick> bricks; // Danh sách các khối gạch
    private List<Ball> balls = new ArrayList<>();

    private List<PowerUp> powerUps = new ArrayList<>();
    private PowerUpEffectManager effectManager;
    private GameContext gameContext;

    private int score;
    private int lives;
    private int currentLevel;
    private Random random;
    private boolean ballLaunched = false;
    private final GameStateManager stateManager;

    public GameManager() {
        this(new GameStateManager());
        bricks = new ArrayList<>();
        random = new Random();
    }

    public GameManager(GameStateManager stateManager) {
        this.stateManager = stateManager;
        bricks = new ArrayList<>();
        random = new Random();
        initializeGame();
    }

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    /**
     * Khởi tạo hoặc reset toàn bộ trạng thái game về ban đầu.
     */
    public void initializeGame() {
        // Khởi tạo Paddle
        paddle = new Paddle(
                (Constants.WIDTH - Constants.PADDLE_WIDTH) / 2.0,
                Constants.HEIGHT - 40
        );
        resetBallAndPaddlePosition();

        gameContext = new GameContext(paddle, balls);
        gameContext.setLivesModifier(amount -> {
            this.lives += amount;
            stateManager.updateStats(score, lives);
            System.out.println("❤️ Lives increased by " + amount + "! Total: " + lives);
        });

        effectManager = new PowerUpEffectManager(gameContext);
        powerUps.clear();

        score = 0;
        lives = Constants.START_LIVES; // Lấy từ Constants
        bricks.clear(); // Xóa gạch cũ nếu có
        currentLevel = 1;
        loadLevel(currentLevel); //hàm tải level từ file


        System.out.println("Game Initialized. Level: " + currentLevel
                + ", Score: " + score + ", Lives: " + lives);
        stateManager.updateStats(score, lives);
        stateManager.setStatusMessage("Destroy all the bricks!");
    }

    private void loadLevel(int levelNum) {
        bricks.clear(); // Xóa tất cả gạch cũ
        // Tên file map, ví dụ: "/levels/level1.txt"
        String levelFilePath = Constants.LEVELS_FOLDER + "level" + levelNum + ".txt";

        InputStream is = getClass().getResourceAsStream(levelFilePath);
        if (is == null) {
            throw new IllegalArgumentException("Level file not found: " + levelFilePath);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            int row = 0;
            // Tính toán vị trí X bắt đầu để canh giữa các hàng gạch trên màn hình
            // Lấy một dòng bất kỳ để tính chiều rộng map (giả định các dòng có cùng độ dài)
            reader.mark(1000); // Đánh dấu vị trí hiện tại của reader
            String firstLine = reader.readLine();
            reader.reset(); // Quay lại đầu file

            if (firstLine == null) {
                System.out.println("Level file is empty: " + levelFilePath);
                return;
            }
            int colsInMap = firstLine.trim().length();
            double totalBricksWidth = colsInMap * Constants.BRICK_WIDTH + (colsInMap - 1) * Constants.BRICK_PADDING_X;
            double startX = (Constants.WIDTH - totalBricksWidth) / 2;

            while ((line = reader.readLine()) != null) {
                for (int col = 0; col < line.length(); col++) {
                    char brickChar = line.charAt(col);
                    double brickX = startX + col * (Constants.BRICK_WIDTH + Constants.BRICK_PADDING_X);
                    double brickY = Constants.BRICK_OFFSET_TOP + row * (Constants.BRICK_HEIGHT + Constants.BRICK_PADDING_Y);

                    Brick newBrick = null;
                    switch (brickChar) {
                        case 'N':
                            newBrick = new NormalBrick(brickX, brickY);
                            break;
                        case 'S':
                            newBrick = new StrongBrick(brickX, brickY);
                            break;
                        case '#': // Gạch không phá hủy
                            newBrick = new IndestructibleBrick(brickX, brickY);
                            break;
                        case ' ': // Ô trống
                            // Không làm gì, không tạo gạch
                            break;
                        default:
                            System.err.println("Unknown brick char in level " + levelNum + ": " + brickChar);
                            break;
                    }
                    if (newBrick != null) {
                        bricks.add(newBrick);
                    }
                }
                row++;
            }
            System.out.println("Level " + levelNum + " loaded successfully from " + levelFilePath);
        } catch (Exception e) {
            System.err.println("Error loading level " + levelNum + ": " + e.getMessage());
            // Nếu có lỗi khi tải level, có thể reset game hoặc chuyển sang Game Over
            // Để đơn giản, chúng ta sẽ in lỗi và tiếp tục.
            // Có thể dùng initializeGame() để reset game nếu không tải được level.
        }
    }
    /**
     * Phương thức cập nhật logic game mỗi frame.
     *
     * @param dt Thời gian trôi qua kể từ frame trước (giây)
     */
    public void update(double dt) {
        if (!stateManager.isRunning()) {
            return;
        }
        // Cập nhật vị trí của Paddle và Ball
        paddle.update(dt);
        Iterator<Ball> ballIt = balls.iterator();
        while (ballIt.hasNext()) {
            Ball ball = ballIt.next();
            if (!ballLaunched) {
                ball.setX(paddle.getX() + paddle.getWidth() / 2 - ball.getWidth() / 2);
                ball.setY(paddle.getY() - ball.getHeight() - 2);
                continue; // bỏ qua phần move & va chạm
            }
            ball.move(dt);
            // Trái
            if (ball.getX() <= 0) {
                ball.setX(0);
                ball.setDirection(-ball.getDx(), ball.getDy());
            }
            // Phải
            if (ball.getX() + ball.getWidth() >= Constants.WIDTH) {
                ball.setX(Constants.WIDTH - ball.getWidth());
                ball.setDirection(-ball.getDx(), ball.getDy());
            }
            // Trần
            if (ball.getY() <= 0) {
                ball.setY(0);
                ball.setDirection(ball.getDx(), -ball.getDy());
            }
            if (ball.istersected(paddle)) { // Giả sử bạn có hàm intersects()
                // Đẩy bóng lên trên paddle một chút để tránh kẹt
                ball.setY(paddle.getY() - ball.getHeight() - 1);
                // Tính toán tâm paddle và tâm bóng
                double paddleCenter = paddle.getX() + paddle.getWidth() / 2.0;
                double ballCenter = ball.getX() + ball.getWidth() / 2.0;

                // Xác định độ lệch của bóng so với tâm paddle [-1..1]
                double relativeIntersect = (ballCenter - paddleCenter) / (paddle.getWidth() / 2.0);

                // Giới hạn góc nảy tối đa (±60°)
                double maxBounceAngle = Math.toRadians(60);
                double bounceAngle = relativeIntersect * maxBounceAngle;

                double speed = ball.getSpeed();

                // Cập nhật vận tốc mới dựa trên góc nảy
                double newDx = speed * Math.sin(bounceAngle);
                double newDy = -Math.abs(speed * Math.cos(bounceAngle)); // đảm bảo luôn đi lên

                ball.setDirection(newDx, newDy);
            }

            // Va chạm Ball-Bricks
            // Sử dụng Iterator để có thể xóa gạch an toàn trong vòng lặp
            // Ball-Brick collisions
            Iterator<Brick> brickIterator = bricks.iterator();
            boolean hasFireBall = effectManager.getRemainingTime("FIRE_BALL") > 0;
            while (brickIterator.hasNext()) {
                Brick brick = brickIterator.next();
                if (!brick.isDestroyed() && ball.istersected(brick)) {
                    brick.takeHit();

                    if (brick.isDestroyed()) {
                        // ===== CHECK SCORE MULTIPLIER =====
                        int multiplier = 1;
                        double scoreMultTime = effectManager.getRemainingTime("SCORE_MULTIPLIER");
                        if (scoreMultTime > 0) {
                            multiplier = 2;
                        }

                        score += 10 * multiplier;
                        stateManager.updateStats(score, lives);
                        System.out.println("Brick destroyed! Score: " + score);

                        // ===== SPAWN POWERUP (25% chance) =====
                        if (random.nextDouble() < 0.25) {
                            spawnPowerUp(
                                    brick.getX() + brick.getWidth() / 2,
                                    brick.getY() + brick.getHeight() / 2
                            );
                        }

                        brickIterator.remove();
                    } else if (brick.getType() == Brick.BrickType.INDESTRUCTIBLE) {
                        System.out.println("Indestructible brick hit!");
                    }

                    if (!hasFireBall) {
                        ball.setDirection(ball.getDx(), -ball.getDy());
                        break;
                    }
                }
            }
            // Ball out of bounds
            if (ball.getY() + ball.getHeight() >= Constants.HEIGHT) {
                // ===== CHECK INVINCIBLE BALL =====
                boolean invincible = effectManager.getRemainingTime("INVINCIBLE_BALL") > 0;

                if (!invincible) {
                    ballIt.remove();
                } else {
                    // Bounce back if invincible
                    ball.setY(Constants.HEIGHT - ball.getHeight());
                    ball.setDirection(ball.getDx(), -Math.abs(ball.getDy()));
                    System.out.println("🛡️ Invincible ball bounced back!");
                }
            }
        }
        // UPDATE POWERUPS
        updatePowerUps(dt);

        // UPDATE EFFECTS
        effectManager.update(dt);

        // Kiểm tra bóng rơi khỏi màn hình (mất mạng)
        if (balls.isEmpty()) {
            lives--;
            effectManager.clearAll();
            powerUps.clear();
            System.out.println("You lost a life! Lives remaining: " + lives);
            stateManager.updateStats(score, lives);
            if (lives <= 0) {
                System.out.println("Game Over! Final Score: " + score);
                stateManager.setStatusMessage("Game Over! Final Score: " + score);
                stateManager.markGameOver();
                return;
            } else {
                resetBallAndPaddlePosition();
                stateManager.setStatusMessage("Lives remaining: " + lives);
            }
        }

        boolean allDestroyableBricksDestroyed = true;
        for (Brick brick : bricks) {
            if (brick.getType() != Brick.BrickType.INDESTRUCTIBLE && !brick.isDestroyed()) {
                allDestroyableBricksDestroyed = false;
                break;
            }
        }
        if (allDestroyableBricksDestroyed) {
            System.out.println("You cleared all destroyable bricks! Final Score: " + score);
            // Chuyển level
            currentLevel++;
            powerUps.clear();
            effectManager.clearAll();
            if (currentLevel > Constants.MAX_LEVELS) { // Kiểm tra nếu đã hết các level
                System.out.println("Congratulations! All levels completed!");
                initializeGame(); // Reset game
            } else {
                bricks.clear(); // Xóa gạch cũ
                loadLevel(currentLevel); // Tải level mới
                resetBallAndPaddlePosition(); // Đặt lại bóng/paddle cho level mới
                System.out.println("Starting Level " + currentLevel);
            }
        }

    }

    // POWERUP UPDATE LOGIC
    private void updatePowerUps(double dt) {
        Iterator<PowerUp> it = powerUps.iterator();

        while (it.hasNext()) {
            PowerUp powerUp = it.next();
            powerUp.update(dt);

            // Check collision with paddle
            if (!powerUp.isCollected() && paddle.istersected(powerUp)) {
                powerUp.collect();
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

    // SPAWN POWERUP
    private void spawnPowerUp(double x, double y) {
        PowerUp powerUp = PowerUpFactory.createRandomPowerUp(x, y);
        if (powerUp != null) {
            powerUps.add(powerUp);
            System.out.println("💎 PowerUp spawned at (" + x + ", " + y + ")");
        }
    }
    /**
     * Đặt lại vị trí của bóng và paddle sau khi mất mạng.
     * Bóng sẽ bắt đầu di chuyển ngay lập tức.
     */
    private void resetBallAndPaddlePosition() {
        paddle.setX((Constants.WIDTH - Constants.PADDLE_WIDTH) / 2.0); // Đặt paddle giữa màn hình
        paddle.setDx(0); // Dừng paddle

        // Khởi tạo lại bóng với constructor hiện có của bạn
        balls.clear();
        Ball newBall = new Ball(
                Constants.WIDTH / 2.0,
                Constants.HEIGHT / 2.0,
                Constants.BALL_RADIUS,
                Constants.DEFAULT_SPEED,
                0, -1
        );
        ball = newBall;
        balls.add(newBall);
        ballLaunched = false;
    }

    /**
     * Phương thức chính để vẽ tất cả các đối tượng game lên màn hình
     *
     * @param g GraphicsContext của Canvas để vẽ
     */

    public void render(GraphicsContext g) {
        g.clearRect(0, 0, Constants.WIDTH, Constants.HEIGHT);
        // Vẽ Paddle
        if (paddle != null) {
            paddle.render(g);
        }
        // Vẽ Ball
        for (Ball b : balls) b.render(g);

        // Vẽ tất cả Bricks còn lại
        for (Brick brick : bricks) {
            brick.render(g);
        }
        // RENDER POWERUPS
        for (PowerUp p : powerUps) {
            p.render(g);
        }

        // Không hiển thị Level vì không có khái niệm level phức tạp
        // ===== DISPLAY ACTIVE EFFECTS =====
        int yOffset = 80;

        double fastTime = effectManager.getRemainingTime("FAST_BALL");
        if (fastTime > 0) {
            g.setFill(Color.RED);
            g.fillText("⚡ Fast: " + String.format("%.1f", fastTime) + "s", 10, yOffset);
            yOffset += 20;
        }

        double slowTime = effectManager.getRemainingTime("SLOW_BALL");
        if (slowTime > 0) {
            g.setFill(Color.PURPLE);
            g.fillText("🐌 Slow: " + String.format("%.1f", slowTime) + "s", 10, yOffset);
            yOffset += 20;
        }

        double expandTime = effectManager.getRemainingTime("EXPAND_PADDLE");
        if (expandTime > 0) {
            g.setFill(Color.GREEN);
            g.fillText("↔ Expand: " + String.format("%.1f", expandTime) + "s", 10, yOffset);
            yOffset += 20;
        }

        double shrinkTime = effectManager.getRemainingTime("SHRINK_PADDLE");
        if (shrinkTime > 0) {
            g.setFill(Color.ORANGE);
            g.fillText("→← Shrink: " + String.format("%.1f", shrinkTime) + "s", 10, yOffset);
            yOffset += 20;
        }

        double invincibleTime = effectManager.getRemainingTime("INVINCIBLE_BALL");
        if (invincibleTime > 0) {
            g.setFill(Color.GOLD);
            g.fillText("🛡️ Invincible: " + String.format("%.1f", invincibleTime) + "s", 10, yOffset);
            yOffset += 20;
        }

        double scoreMultTime = effectManager.getRemainingTime("SCORE_MULTIPLIER");
        if (scoreMultTime > 0) {
            g.setFill(Color.LIGHTGREEN);
            g.fillText("💰 x2 Score: " + String.format("%.1f", scoreMultTime) + "s", 10, yOffset);
            yOffset += 20;
        }

        double fireTime = effectManager.getRemainingTime("FIRE_BALL");
        if (fireTime > 0) {
            g.setFill(Color.ORANGERED);
            g.fillText("🔥 Fire: " + String.format("%.1f", fireTime) + "s", 10, yOffset);
            yOffset += 20;
        }
    }

    // --- Getters cần thiết để MainConsole có thể tương tác với Paddle và Ball ---
    public Paddle getPaddle() {
        return paddle;
    }

    public void launchBall() {
        if (!ballLaunched) {
            ballLaunched = true;
            for (Ball b : balls) {
                // Cho bóng bay lên góc ngẫu nhiên một chút
                b.setDirection(0, -1);
            }
            System.out.println("Ball launched!");
        }
    }

    public void spawnExtraBall() {
        Ball newBall = new Ball(
                paddle.getX() + paddle.getWidth() / 2.0,
                paddle.getY() - 20,
                Constants.BALL_RADIUS,
                Constants.DEFAULT_SPEED,
                1, -1
        );

        if (ballLaunched) {
            newBall.setDirection(Math.random() > 0.5 ? 0 : 0, -1);
        }

        balls.add(newBall);
    }

    public List<Ball> getBalls() {
        return balls;
    }

    public int getScore() {
        return score;
    }

    public int getLives() {
        return lives;
    }

    public GameStateManager getStateManager() {
        return stateManager;
    }

    // ===== Tạo snapshot để lưu =====
    public SaveService.GameSnapshot createSnapshot() {
        SaveService.GameSnapshot s = new SaveService.GameSnapshot();
        s.score = getScore();
        s.lives = getLives();
        if (ball != null) {
            s.ballX = ball.getX();
            s.ballY = ball.getY();
            s.ballDX = ball.getDx();
            s.ballDY = ball.getDy();
        }
        if (paddle != null) {
            s.paddleX = paddle.getX();
        }
        return s;
    }

    // ===== Khôi phục từ snapshot =====
    public void restoreFromSnapshot(SaveService.GameSnapshot s) {
        this.score = s.score;
        this.lives = s.lives;

        if (ball != null) {
            ball.setPosition(s.ballX, s.ballY);
            ball.setVelocity(s.ballDX, s.ballDY);
        }
        if (paddle != null) {
            paddle.setX(s.paddleX);
        }
        // nếu bạn có đồng bộ scoreboard qua stateManager thì gọi:
        // stateManager.updateStats(score, lives);
    }

    // ===== Hàm sẵn có của bạn để bắt đầu game mới =====
    public void startNewGame() {
        // ... reset level/score/lives, spawn ball/paddle, v.v.
    }
}
