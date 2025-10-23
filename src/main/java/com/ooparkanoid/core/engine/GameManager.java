// File: src/main/java/com/ooparkanoid/core/engine/GameManager.java
package com.ooparkanoid.core.engine;
import com.ooparkanoid.object.Laser;
import com.ooparkanoid.sound.SoundManager;
import javafx.geometry.Rectangle2D;

import com.ooparkanoid.core.save.SaveService;
import com.ooparkanoid.core.state.GameState;
import com.ooparkanoid.core.state.GameStateManager;
import com.ooparkanoid.object.Ball;
import com.ooparkanoid.object.Paddle;
import com.ooparkanoid.object.PowerUp.GameContext;
import com.ooparkanoid.object.PowerUp.PowerUp;
import com.ooparkanoid.object.PowerUp.PowerUpEffectManager;
import com.ooparkanoid.object.PowerUp.PowerUpFactory;
import com.ooparkanoid.object.bricks.Brick;
import com.ooparkanoid.object.bricks.NormalBrick;
import com.ooparkanoid.object.bricks.StrongBrick;
import com.ooparkanoid.object.bricks.IndestructibleBrick;
import com.ooparkanoid.object.bricks.FlickerBrick;
import com.ooparkanoid.object.bricks.ExplosiveBrick;
import com.ooparkanoid.object.bricks.CollisionArea;

import com.ooparkanoid.utils.Constants;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import com.ooparkanoid.graphics.ResourceManager;
import javafx.scene.image.Image;

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
    private List<Brick> bricks;
    private List<Ball> balls = new ArrayList<>();

    private List<PowerUp> powerUps = new ArrayList<>();
    private PowerUpEffectManager effectManager;
    private GameContext gameContext;
    private double roundTimeElapsed;
    private double totalTimeElapsed;

    private int score;
    private int lives;
    private int currentLevel;
    private Random random;
    private boolean ballLaunched = false;
    private final GameStateManager stateManager;

    private Image normalBrickTexture;
    private Image strongBrickTexture3; // 2 hit points remaining
    private Image strongBrickTexture2; // 2 hit points remaining
    private Image strongBrickTexture1; // 1 hit point remaining
    private Image indestructibleBrickTexture;
    private Image flickerBrickTexture1;
    private Image flickerBrickTexture2;
    private Image explosiveBrickTexture;

    public GameManager() {
        this(new GameStateManager());
        bricks = new ArrayList<>();
        random = new Random();
        loadBrickTextures();
    }

    public GameManager(GameStateManager stateManager) {
        this.stateManager = stateManager;
        bricks = new ArrayList<>();
        random = new Random();
        loadBrickTextures();
        initializeGame();
    }

    private void loadBrickTextures() {
        ResourceManager rm = ResourceManager.getInstance();
        normalBrickTexture = rm.loadImage("brick_normal.png");
        strongBrickTexture3 = rm.loadImage("brick_strong_hit1.png");
        strongBrickTexture2 = rm.loadImage("brick_strong_hit2.png");
        strongBrickTexture1 = rm.loadImage("brick_strong_hit3.png");
        indestructibleBrickTexture = rm.loadImage("brick_indestructible.png");
        flickerBrickTexture1 = rm.loadImage("brick_flicker1.png");
        flickerBrickTexture2 = rm.loadImage("brick_flicker2.png");
        explosiveBrickTexture = rm.loadImage("brick_explosive.png");
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
        double paddleStartX = Constants.PLAYFIELD_LEFT
                + (Constants.PLAYFIELD_WIDTH - Constants.PADDLE_WIDTH) / 2.0;
        paddle = new Paddle(
//                (Constants.WIDTH - Constants.PADDLE_WIDTH) / 2.0,
                paddleStartX,
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

        SoundManager.getInstance().playMusic("background.mp3");

        score = 0;
        lives = Constants.START_LIVES; // Lấy từ Constants
        bricks.clear(); // Xóa gạch cũ nếu có
        currentLevel = 1;
        roundTimeElapsed = 0;
        totalTimeElapsed = 0;
        loadLevel(currentLevel); //hàm tải level từ file


        System.out.println("Game Initialized. Level: " + currentLevel
                + ", Score: " + score + ", Lives: " + lives);
        stateManager.updateStats(score, lives);
        stateManager.setCurrentRound(currentLevel);
        stateManager.updateTimers(roundTimeElapsed, totalTimeElapsed);
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
//            double startX = (Constants.WIDTH - totalBricksWidth) / 2;
            double startX = Constants.PLAYFIELD_LEFT + (Constants.PLAYFIELD_WIDTH - totalBricksWidth) / 2;

            while ((line = reader.readLine()) != null) {
                for (int col = 0; col < line.length(); col++) {
                    char brickChar = line.charAt(col);
                    double brickX = startX + col * (Constants.BRICK_WIDTH + Constants.BRICK_PADDING_X);
                    double brickY = Constants.BRICK_OFFSET_TOP + row * (Constants.BRICK_HEIGHT + Constants.BRICK_PADDING_Y);

                    Brick newBrick = null;
                    switch (brickChar) {
                        case 'N':
                            newBrick = new NormalBrick(brickX, brickY);
                            newBrick.setTexture(normalBrickTexture);
                            break;
                        case 'S':
                            newBrick = new StrongBrick(brickX, brickY);
                            newBrick.setTexture(strongBrickTexture1);
                            break;
                        case '#':
                            newBrick = new IndestructibleBrick(brickX, brickY);
                            newBrick.setTexture(indestructibleBrickTexture);
                            break;
                        case 'F':
                            newBrick = new FlickerBrick(brickX, brickY);
                            newBrick.setTexture(flickerBrickTexture1);
                            break;
                        case 'X': // <--- Thêm case cho ExplosiveBrick
                            newBrick = new ExplosiveBrick(brickX, brickY);
                            newBrick.setTexture(explosiveBrickTexture);
                            break;
                        case ' ':
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
        roundTimeElapsed += dt;
        totalTimeElapsed += dt;
        stateManager.updateTimers(roundTimeElapsed, totalTimeElapsed);
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
            ball.update(dt);
            // Trái
//            if (ball.getX() <= 0) {
//                ball.setX(0);
            if (ball.getX() <= Constants.PLAYFIELD_LEFT) {
                ball.setX(Constants.PLAYFIELD_LEFT);
                ball.setDirection(-ball.getDx(), ball.getDy());
                SoundManager.getInstance().play("bounce");
            }
            // Phải
//            if (ball.getX() + ball.getWidth() >= Constants.WIDTH) {
//                ball.setX(Constants.WIDTH - ball.getWidth());
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
                SoundManager.getInstance().play("bounce");
            }

            // Va chạm Ball-Bricks
            // Sử dụng Iterator để có thể xóa gạch an toàn trong vòng lặp
            // Ball-Brick collisions
            Iterator<Brick> brickIterator = bricks.iterator();
            boolean hasFireBall = effectManager.getRemainingTime("FIRE_BALL") > 0;
            while (brickIterator.hasNext()) {
                Brick brick = brickIterator.next();
                brick.update(dt);
                if (!brick.isDestroyed() && ball.collidesWith(brick)) {

                    Brick.BrickType hitBrickType = brick.getType();
                    boolean brickWasDestroyed = brick.isDestroyed();
                    brick.takeHit();

                    if (!brickWasDestroyed && brick.isDestroyed()) {
                        int multiplier = 1;
                        double scoreMultTime = effectManager.getRemainingTime("SCORE_MULTIPLIER");
                        if (scoreMultTime > 0) {
                            multiplier = 2;
                        }

                        score += 10 * multiplier;
                        stateManager.updateStats(score, lives);
                        System.out.println(hitBrickType + " Brick destroyed! Score: " + score);

                        SoundManager.getInstance().play("break");
                        // XỬ LÝ NỔ NẾU LÀ EXPLOSIVE BRICK

                        if (hitBrickType == Brick.BrickType.EXPLOSIVE) {
                            System.out.println("Explosive Brick detonated!");
                            handleExplosion(brick.getX(), brick.getY());
                        }

                        if (random.nextDouble() < Constants.POWERUP_DROP_CHANCE) {
                            spawnPowerUp(
                                    brick.getX() + brick.getWidth() / 2,
                                    brick.getY() + brick.getHeight() / 2
                            );
                        }

                        brickIterator.remove();
                    } else if (hitBrickType == Brick.BrickType.INDESTRUCTIBLE) {
                        System.out.println("Indestructible brick hit!");
                    }

                    if (!hasFireBall) {
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
                        break;
                    }
                }
            }
            double clampedX = Math.max(
                    Constants.PLAYFIELD_LEFT,
                    Math.min(ball.getX(), Constants.PLAYFIELD_RIGHT - ball.getWidth())
            );
            ball.setX(clampedX);
            if (ball.getY() + ball.getHeight() >= Constants.HEIGHT) {
                boolean invincible = effectManager.getRemainingTime("INVINCIBLE_BALL") > 0;

                if (!invincible) {
                    ballIt.remove();
                } else {
                    // Bounce back if invincible
                    ball.setY(Constants.HEIGHT - ball.getHeight());
                    ball.setDirection(ball.getDx(), -Math.abs(ball.getDy()));
                }
            }
        }
        updateLasers(dt);
        // UPDATE POWERUPS
        updatePowerUps(dt);

        // UPDATE EFFECTS
        effectManager.update(dt);

        // Kiểm tra bóng rơi khỏi màn hình (mất mạng)
        if (balls.isEmpty()) {
            lives--;
            effectManager.clearAll();
            powerUps.clear();
            SoundManager.getInstance().play("lose_life");
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
                roundTimeElapsed = 0;
                stateManager.setCurrentRound(currentLevel);
                stateManager.updateTimers(roundTimeElapsed, totalTimeElapsed);
                System.out.println("Starting Level " + currentLevel);
            }
        }

    }

    private void updateLasers(double dt) {
        List<Laser> lasers = paddle.getLasers();
        if (lasers.isEmpty()) return;
        Iterator<Laser> laserIt = lasers.iterator();
        while (laserIt.hasNext()) {
            Laser laser = laserIt.next();
            Iterator<Brick> brickIt = bricks.iterator();
            while (brickIt.hasNext()) {
                Brick brick = brickIt.next();
                if (!brick.isDestroyed() && laser.istersected(brick)) {
                    Brick.BrickType hitBrickType = brick.getType();
                    boolean brickWasDestroyed = brick.isDestroyed();
                    brick.takeHit();
                    lasers.remove(laser);
                    SoundManager.getInstance().play("laser_hit");
                    if (!brickWasDestroyed && brick.isDestroyed()) {
                        int multiplier = 1;
                        double scoreMultTime = effectManager.getRemainingTime("SCORE_MULTIPLIER");
                        if (scoreMultTime > 0) {
                            multiplier = 2;
                        }
                        score += 10 * multiplier;
                        stateManager.updateStats(score, lives);
                        SoundManager.getInstance().play("break");
                        // Handle explosion
                        if (hitBrickType == Brick.BrickType.EXPLOSIVE) {
                            handleExplosion(brick.getX(), brick.getY());
                        }
                        // Spawn powerup
                        if (random.nextDouble() < Constants.POWERUP_DROP_CHANCE) {
                            spawnPowerUp(
                                    brick.getX() + brick.getWidth() / 2,
                                    brick.getY() + brick.getHeight() / 2
                            );
                        }
                        brickIt.remove();
                    }
                    break;
                }
            }
        }
    }
    /**
     * Xử lý hiệu ứng nổ khi một ExplosiveBrick bị phá hủy.
     * Sẽ tìm và phá hủy các gạch trong ô 3x3 xung quanh vị trí nổ.
     * @param explosionX Tọa độ X của tâm vụ nổ (gạch nổ)
     * @param explosionY Tọa độ Y của tâm vụ nổ (gạch nổ)
     */
    private void handleExplosion(double explosionX, double explosionY) {
        // Tạo một vùng bao quanh 3x3
        double explosionRadiusX = Constants.BRICK_WIDTH * 1.5 + Constants.BRICK_PADDING_X; // Khoảng 1.5 gạch mỗi bên
        double explosionRadiusY = Constants.BRICK_HEIGHT * 1.5 + Constants.BRICK_PADDING_Y; // Khoảng 1.5 gạch mỗi bên

        // Vùng hình chữ nhật đại diện cho khu vực nổ
        // Tạo một đối tượng CollisionArea đại diện cho khu vực nổ
        CollisionArea explosionZone = new CollisionArea(
                explosionX - Constants.BRICK_WIDTH - Constants.BRICK_PADDING_X, // X trái của khu vực nổ
                explosionY - Constants.BRICK_HEIGHT - Constants.BRICK_PADDING_Y, // Y trên của khu vực nổ
                Constants.BRICK_WIDTH * 3 + Constants.BRICK_PADDING_X * 2,       // Chiều rộng 3 gạch + 2 padding
                Constants.BRICK_HEIGHT * 3 + Constants.BRICK_PADDING_Y * 2       // Chiều cao 3 gạch + 2 padding
        );

        Iterator<Brick> it = bricks.iterator();
        while (it.hasNext()) {
            Brick brick = it.next();
            if (!brick.isDestroyed() && brick.istersected(explosionZone)) { // Sử dụng istersected của bạn
                // Kiểm tra lại để không phá hủy gạch INDESTRUCTIBLE
                if (brick.getType() != Brick.BrickType.INDESTRUCTIBLE) {

                    // --- Đây là dòng quan trọng ---
                    boolean wasDestroyedBeforeHit = brick.isDestroyed(); // Luôn false ở đây vì đã check ở if trên
                    brick.takeHit(); // <--- Gạch chỉ nhận MỘT hit từ vụ nổ

                    // Nếu gạch bị phá hủy SAU cú hit này
                    if (!wasDestroyedBeforeHit && brick.isDestroyed()) { // Điều kiện này ĐÃ LÀM CHÍNH XÁC NHỮNG GÌ BẠN MUỐN
                        score += 10; // Tăng điểm cho mỗi gạch bị nổ
                        stateManager.updateStats(score, lives);
                        System.out.println("Brick destroyed by explosion! Score: " + score);
                        SoundManager.getInstance().play("break");
                        it.remove(); // Xóa gạch khỏi danh sách

                        // Có thể spawn PowerUp từ các gạch bị nổ phụ nếu muốn
                        if (random.nextDouble() < Constants.POWERUP_DROP_CHANCE / 2) { // Giảm tỷ lệ spawn
                            spawnPowerUp(brick.getX() + brick.getWidth() / 2, brick.getY() + brick.getHeight() / 2);
                        }
                    }
                }
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
//        paddle.setX((Constants.WIDTH - Constants.PADDLE_WIDTH) / 2.0); // Đặt paddle giữa màn hình
        double paddleStartX = Constants.PLAYFIELD_LEFT
                + (Constants.PLAYFIELD_WIDTH - Constants.PADDLE_WIDTH) / 2.0;
        paddle.setX(paddleStartX); // Đặt paddle giữa vùng chơi
        paddle.setDx(0); // Dừng paddle

        // Khởi tạo lại bóng với constructor hiện có của bạn
        balls.clear();
        Ball newBall = new Ball(
//                Constants.WIDTH / 2.0,
                Constants.PLAYFIELD_LEFT + Constants.PLAYFIELD_WIDTH / 2.0,
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
        double effectTextX = Constants.PLAYFIELD_LEFT + 10;


        double fastTime = effectManager.getRemainingTime("FAST_BALL");
        if (fastTime > 0) {
            g.setFill(Color.RED);
            g.fillText("Fast: " + String.format("%.1f", fastTime) + "s", 10, yOffset);
//            g.fillText("Fast: " + String.format("%.1f", fastTime) + "s", effectTextX, yOffset);
            yOffset += 20;

        }

        double slowTime = effectManager.getRemainingTime("SLOW_BALL");
        if (slowTime > 0) {
            g.setFill(Color.PURPLE);
            g.fillText("Slow: " + String.format("%.1f", slowTime) + "s", 10, yOffset);
//            g.fillText("Slow: " + String.format("%.1f", slowTime) + "s", effectTextX, yOffset);
            yOffset += 20;
        }

        double expandTime = effectManager.getRemainingTime("EXPAND_PADDLE");
        if (expandTime > 0) {
            g.setFill(Color.GREEN);
            g.fillText("Expand: " + String.format("%.1f", expandTime) + "s", 10, yOffset);
//            g.fillText("Expand: " + String.format("%.1f", expandTime) + "s", effectTextX, yOffset);
            yOffset += 20;
        }

        double shrinkTime = effectManager.getRemainingTime("SHRINK_PADDLE");
        if (shrinkTime > 0) {
            g.setFill(Color.ORANGE);
            g.fillText("Shrink: " + String.format("%.1f", shrinkTime) + "s", 10, yOffset);
//            g.fillText("Shrink: " + String.format("%.1f", shrinkTime) + "s", effectTextX, yOffset);
            yOffset += 20;
        }

        double invincibleTime = effectManager.getRemainingTime("INVINCIBLE_BALL");
        if (invincibleTime > 0) {
            g.setFill(Color.GOLD);
            g.fillText("Invincible: " + String.format("%.1f", invincibleTime) + "s", 10, yOffset);
//            g.fillText("Invincible: " + String.format("%.1f", invincibleTime) + "s", effectTextX, yOffset);
            yOffset += 20;
        }

        double scoreMultTime = effectManager.getRemainingTime("SCORE_MULTIPLIER");
        if (scoreMultTime > 0) {
            g.setFill(Color.LIGHTGREEN);
            g.fillText("x2 Score: " + String.format("%.1f", scoreMultTime) + "s", 10, yOffset);
//            g.fillText("Invincible: " + String.format("%.1f", invincibleTime) + "s", effectTextX, yOffset);
            yOffset += 20;
        }

        double fireTime = effectManager.getRemainingTime("FIRE_BALL");
        if (fireTime > 0) {
            g.setFill(Color.ORANGERED);
            g.fillText("Fire: " + String.format("%.1f", fireTime) + "s", 10, yOffset);
//            g.fillText("Fire: " + String.format("%.1f", fireTime) + "s", effectTextX, yOffset);
            yOffset += 20;
        }

        double laserTime = effectManager.getRemainingTime("LASER_PADDLE");
        if (laserTime > 0) {
            g.setFill(Color.LIGHTBLUE);
            g.fillText("Laser: " + String.format("%.1f", laserTime) + "s", 10, yOffset);
//            g.fillText("Laser: " + String.format("%.1f", laserTime) + "s", effectTextX, yOffset);
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
