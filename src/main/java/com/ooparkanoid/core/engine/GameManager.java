// File: src/main/java/com/ooparkanoid/core/engine/GameManager.java
package com.ooparkanoid.core.engine;

import com.ooparkanoid.object.PowerUp.*;
import com.ooparkanoid.core.save.SaveService;
import com.ooparkanoid.core.state.GameState;
import com.ooparkanoid.core.state.GameStateManager;
import com.ooparkanoid.object.Ball;
import com.ooparkanoid.object.Paddle;
import com.ooparkanoid.object.bricks.Brick; // Import Brick
import com.ooparkanoid.object.bricks.NormalBrick; // Import NormalBrick
import com.ooparkanoid.object.bricks.StrongBrick; // Import StrongBrick
import com.ooparkanoid.object.bricks.IndestructibleBrick;
import com.ooparkanoid.core.save.SaveService;

import com.ooparkanoid.object.PowerUp.GameContext;
import com.ooparkanoid.object.PowerUp.PowerUp;
import com.ooparkanoid.object.PowerUp.PowerUpEffectManager;
import com.ooparkanoid.object.PowerUp.PowerUpFactory;

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
    private static final double POWER_UP_DROP_CHANCE = 0.3;
    private Paddle paddle;
//    private Ball ball;
//    private List<Brick> bricks; // Danh sách các khối gạch
    private final List<Ball> balls;
    private final List<Brick> bricks; // Danh sách các khối gạch
    private final List<PowerUp> powerUps;

    private int score;
    private int lives;
    private int currentLevel;
    private final Random random;
    private final GameStateManager stateManager;
    private boolean ballAttachedToPaddle = true; // mặc định dính khi new game/life
    private boolean isBallLaunched;

    private GameContext gameContext;
    private PowerUpEffectManager effectManager;
    private boolean fireBallActive;
    private boolean invincibleActive;

    public GameManager() {
        this(new GameStateManager());
        initializeGame(); // Gọi hàm khởi tạo game
    }
    public GameManager(GameStateManager stateManager) {
        this.stateManager = stateManager;
        this.balls = new ArrayList<>();
        this.bricks = new ArrayList<>();
        this.powerUps = new ArrayList<>();
        this.random = new Random();
//        initializeGame(); // Gọi hàm khởi tạo game
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

        // Khởi tạo Ball và đặt lên paddle, chờ tín hiệu bắt đầu
//        ball = new Ball(
////                Constants.WIDTH / 2.0,
////                Constants.HEIGHT / 2.0,
//                paddle.getX() + paddle.getWidth() / 2.0,
//                paddle.getY() - Constants.BALL_RADIUS,
//                Constants.BALL_RADIUS,
//                Constants.BALL_SPEED, // Speed từ Constants
////                (random.nextBoolean() ? 1 : -1), // dirX ngẫu nhiên (1 hoặc -1)
////                -1 // dirY luôn hướng lên
////                Constants.BALL_SPEED
//                0,
//                -1
//        );
//        attachBallToPaddle();

        score = 0;
        lives = Constants.START_LIVES;
        currentLevel = 1;

        powerUps.clear();
        balls.clear();
        bricks.clear();
        ballAttachedToPaddle = true;
        isBallLaunched = false;
        fireBallActive = false;
        invincibleActive = false;

//        isBallLaunched = false;
//
//        // Khởi tạo thông tin game
//        score = 0;
//        lives = Constants.START_LIVES; // Lấy từ Constants
//
//        // Tạo gạch ban đầu
//        bricks.clear(); // Xóa gạch cũ nếu có
//        currentLevel = 1;
////        createInitialBricks(); // Hàm tạo gạch ban đầu
        loadLevel(currentLevel); //hàm tải level từ file
        resetBallAndPaddlePosition();

        gameContext = new GameContext(paddle, balls);
        gameContext.setLivesModifier(this::addLives);
        gameContext.setScoreMultiplier(1.0);
        gameContext.onFireBall(() -> fireBallActive = true, () -> fireBallActive = false);
        gameContext.onInvincible(() -> invincibleActive = true, () -> invincibleActive = false);
        effectManager = new PowerUpEffectManager(gameContext);

        System.out.println("Game Initialized. Level: " + currentLevel
                + ", Score: " + score + ", Lives: " + lives);
        stateManager.updateStats(score, lives);
        stateManager.setStatusMessage("Destroy all the bricks!");
    }

    /**
     * Tạo bố cục gạch ban đầu. (Không có khái niệm level phức tạp ở đây)
     */
//    private void createInitialBricks() {
//        int rows = 5; // Số hàng gạch
//        // Tính số cột gạch tối đa có thể vừa trên màn hình
//        int cols = (int)(Constants.WIDTH / (Constants.BRICK_WIDTH + Constants.BRICK_PADDING_X));
//
//        // Tính toán vị trí X bắt đầu để canh giữa các hàng gạch trên màn hình
//        double totalBricksWidth = cols * Constants.BRICK_WIDTH + (cols - 1) * Constants.BRICK_PADDING_X;
//        double startX = (Constants.WIDTH - totalBricksWidth) / 2;
//
//        for (int r = 0; r < rows; r++) {
//            for (int c = 0; c < cols; c++) {
//                double brickX = startX + c * (Constants.BRICK_WIDTH + Constants.BRICK_PADDING_X);
//                double brickY = Constants.BRICK_OFFSET_TOP + r * (Constants.BRICK_HEIGHT + Constants.BRICK_PADDING_Y);
//
//                // Xen kẽ NormalBrick và StrongBrick
//                if (r % 2 == 0) {
//                    bricks.add(new NormalBrick(brickX, brickY));
//                } else {
//                    bricks.add(new StrongBrick(brickX, brickY));
//                }
//            }
//        }
//    }

    private void loadLevel(int levelNum) {
        bricks.clear(); // Xóa tất cả gạch cũ

        // Tên file map, ví dụ: "/levels/level1.txt"
        String levelFilePath = Constants.LEVELS_FOLDER + "level" + levelNum + ".txt";

        try (InputStream is = getClass().getResourceAsStream(levelFilePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

                if (is == null) {
                    throw new IllegalArgumentException("Level file not found: " + levelFilePath);
                }

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
            }
        catch (Exception e) {
            System.err.println("Error loading level " + levelNum + ": " + e.getMessage());
            // Nếu có lỗi khi tải level, có thể reset game hoặc chuyển sang Game Over
            // Để đơn giản, chúng ta sẽ in lỗi và tiếp tục.
            // Có thể dùng initializeGame() để reset game nếu không tải được level.
        }
    }
    /**
     * Phương thức cập nhật logic game mỗi frame.
     * @param dt Thời gian trôi qua kể từ frame trước (giây)
     */
    public void update(double dt) {
        if (!stateManager.isRunning()) {
            return;
        }
        // Cập nhật vị trí của Paddle
        paddle.update(dt);
//        ball.move(dt); // Gọi move() của Ball

//        if (ball == null) {
//            return;
//        }

        if (effectManager != null) {
            effectManager.update(dt);
        }

//        if (ballAttachedToPaddle) {
//            alignBallWithPaddle();
//            return;
//        }
        updatePowerUps(dt);

        if (balls.isEmpty()) {
            return;
        }

//        // Cập nhật vị trí của Ball khi đang bay
//        ball.move(dt);

        Ball primaryBall = getPrimaryBall();
        for (Ball ball : balls) {
            if (ball == primaryBall && ballAttachedToPaddle) {
                alignBallWithPaddle(ball);
            } else {
                ball.move(dt);
            }
        }
        handleBallCollisions();

        // --- Xử lý Va chạm ---
        // Va chạm Ball-Walls (Tường trái, phải, trần)
//        if (ball.getX() <= 0 || ball.getX() + ball.getWidth() >= Constants.WIDTH) {
//            // SỬA: Dùng getDx() và getDy()
//            ball.setDirection(-ball.getDx(), ball.getDy());
//        }
//        if (ball.getY() <= 0) { // Va chạm trần
//            // SỬA: Dùng getDx() và getDy()
//            ball.setDirection(ball.getDx(), -ball.getDy());
//        }
        // Trái
//        if (ball.getX() <= 0) {
//            ball.setX(0);
//            ball.setDirection(-ball.getDx(), ball.getDy());
//        }
//        // Phải
//        if (ball.getX() + ball.getWidth() >= Constants.WIDTH) {
//            ball.setX(Constants.WIDTH - ball.getWidth());
//            ball.setDirection(-ball.getDx(), ball.getDy());
//        }
//        // Trần
//        if (ball.getY() <= 0) {
//            ball.setY(0);
//            ball.setDirection(ball.getDx(), -ball.getDy());
//        }
        if (balls.isEmpty()) {
            handleLifeLost();
            if (!stateManager.isRunning()) {
                return;
            }
        }
        checkLevelCompletion();
    }

    private void updatePowerUps(double dt) {
        Iterator<PowerUp> iterator = powerUps.iterator();
        while (iterator.hasNext()) {
            PowerUp powerUp = iterator.next();
            if (!powerUp.isCollected()) {
                powerUp.update(dt);

                if (powerUp.getY() > Constants.HEIGHT) {
                    iterator.remove();
                    continue;
                }

                if (paddle != null && paddle.istersected(powerUp)) {
                    powerUp.collect();
                    if (effectManager != null) {
                        effectManager.activateEffect(powerUp.getEffect(), powerUp.getDuration());
                    }
                    iterator.remove();
                }
            } else {
                iterator.remove();
            }
        }
    }

        // Va chạm Ball-Paddle
//        if (ball.istersected(paddle)) { // Sử dụng istersected của Ball
//            // Đảm bảo bóng không bị kẹt trong paddle bằng cách đẩy bóng lên
//            ball.setY(paddle.getY() - ball.getHeight());
//
//            // Tính toán góc nảy (đơn giản, chỉ đảo hướng Y)
//            // SỬA: Dùng getDx() và getDy()
//            ball.setDirection(ball.getDx(), -ball.getDy());
//        }
//        // Va chạm Ball-Paddle
//        if (ball.istersected(paddle)) { // Giả sử bạn có hàm intersects()
//            // Đẩy bóng lên trên paddle một chút để tránh kẹt
//            ball.setY(paddle.getY() - ball.getHeight() - 1);

    private void handleBallCollisions() {
        Ball primaryBall = getPrimaryBall();
        Iterator<Ball> iterator = balls.iterator();

        while (iterator.hasNext()) {
            Ball ball = iterator.next();
            if (ball == primaryBall && ballAttachedToPaddle) {
                continue;
            }

            // Tính toán tâm paddle và tâm bóng
//            double paddleCenter = paddle.getX() + paddle.getWidth() / 2.0;
//            double ballCenter   = ball.getX() + ball.getWidth() / 2.0;
            if (ball.getX() <= 0) {
                ball.setX(0);
                ball.setDirection(-ball.getDx(), ball.getDy());
            }

//                // Xác định độ lệch của bóng so với tâm paddle [-1..1]
//            double relativeIntersect = (ballCenter - paddleCenter) / (paddle.getWidth() / 2.0);
            if (ball.getX() + ball.getWidth() >= Constants.WIDTH) {
                ball.setX(Constants.WIDTH - ball.getWidth());
                ball.setDirection(-ball.getDx(), ball.getDy());
            }

//            // Giới hạn góc nảy tối đa (±60°)
//            double maxBounceAngle = Math.toRadians(60);
//            double bounceAngle = relativeIntersect * maxBounceAngle;
            if (ball.getY() <= 0) {
                ball.setY(0);
                ball.setDirection(ball.getDx(), -ball.getDy());
            }

//            // Giữ nguyên tốc độ bóng (nên có hằng số speed hoặc hàm getSpeed())
//            double speed = Constants.BALL_SPEED;
            if (ball.getY() + ball.getHeight() >= Constants.HEIGHT) {
                if (invincibleActive) {
                    ball.setY(Constants.HEIGHT - ball.getHeight());
                    ball.setDirection(ball.getDx(), -Math.abs(ball.getDy()));
                } else {
                    iterator.remove();
                    continue;
                }
            }
//            // Cập nhật vận tốc mới dựa trên góc nảy
//            double newDx = speed * Math.sin(bounceAngle);
//            double newDy = -Math.abs(speed * Math.cos(bounceAngle)); // đảm bảo luôn đi lên
                handlePaddleCollision(ball);
                handleBrickCollisions(ball);
        }
    }
//            ball.setDirection(newDx, newDy);
//    }

        private void handlePaddleCollision(Ball ball) {
            if (paddle == null || !ball.istersected(paddle)) {
                return;
            }
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
        }
        // Va chạm Ball-Bricks
        // Sử dụng Iterator để có thể xóa gạch an toàn trong vòng lặp

        private void handleBrickCollisions(Ball ball){
            Iterator<Brick> brickIterator = bricks.iterator();
            while (brickIterator.hasNext()) {
                Brick brick = brickIterator.next();

//        Iterator<Brick> brickIterator = bricks.iterator();
//        while (brickIterator.hasNext()) {
//            Brick brick = brickIterator.next();
//            if (!brick.isDestroyed()) { // Chỉ kiểm tra va chạm với gạch chưa bị phá hủy
//                if (ball.istersected(brick)) { // Sử dụng istersected của Ball
//                    brick.takeHit(); // Gạch nhận một cú đánh
//                    if (brick.isDestroyed()) {
//                        score += 10;     // Tăng điểm
//                        stateManager.updateStats(score, lives);
//                        System.out.println("Brick destroyed! Score: " + score);
//                        brickIterator.remove(); // Xóa gạch đã bị phá hủy
//                    } else if (brick.getType() == Brick.BrickType.INDESTRUCTIBLE) {
//                        System.out.println("Indestructible brick hit!");
//                    }
                if (!brick.isDestroyed() && ball.istersected(brick)) {
                    brick.takeHit();
                    if (brick.isDestroyed()) {
                        addScore(10);
                        System.out.println("Brick destroyed! Score: " + score);
                        spawnPowerUp(brick);
                        brickIterator.remove();
                    } else if (brick.getType() == Brick.BrickType.INDESTRUCTIBLE) {
                        System.out.println("Indestructible brick hit!");
                    }

                    // Logic va chạm bóng với gạch (đơn giản, chỉ đảo ngược hướng Y)
                    // SỬA: Dùng getDx() và getDy()
                    if (!fireBallActive || brick.getType() == Brick.BrickType.INDESTRUCTIBLE) {
                        ball.setDirection(ball.getDx(), -ball.getDy());

//                    if (brick.isDestroyed()) {
//                        System.out.println("Brick destroyed! Score: " + score);
//                        brickIterator.remove(); // Xóa gạch đã bị phá hủy khỏi danh sách
//                    }
                        // Giả định bóng chỉ va chạm với một gạch mỗi frame để đơn giản
                        break;
                    }
                }
            }
        }

        // Kiểm tra bóng rơi khỏi màn hình (mất mạng)
//        if (ball.getY() + ball.getHeight() >= Constants.HEIGHT) {
//            lives--;
//            System.out.println("You lost a life! Lives remaining: " + lives);
//            stateManager.updateStats(score, lives);
//            if (lives <= 0) {
//                System.out.println("Game Over! Final Score: " + score);
////                initializeGame(); // Reset game hoàn toàn sau khi Game Over
//                stateManager.setStatusMessage("Game Over! Final Score: " + score);
//                stateManager.markGameOver();
//                return;
//            } else {
        private void spawnPowerUp(Brick brick){
            if (random.nextDouble() > POWER_UP_DROP_CHANCE) {
                return;
            }
//                // Đặt lại bóng và paddle về vị trí ban đầu sau khi mất mạng
//                resetBallAndPaddlePosition();
//                stateManager.setStatusMessage("Lives remaining: " + lives);
//            }
            double spawnX = brick.getX() + brick.getWidth() / 2.0 - 15;
            double spawnY = brick.getY() + brick.getHeight() / 2.0 - 15;
            PowerUp powerUp = PowerUpFactory.createRandomPowerUp(spawnX, spawnY);
            if (powerUp != null) {
                powerUps.add(powerUp);
            }
        }

        // Kiểm tra tất cả gạch đã bị phá hủy (điều kiện chiến thắng)
//        if (bricks.isEmpty()) {
//            System.out.println("You cleared all bricks! Final Score: " + score);
////            initializeGame(); // Reset game để chơi lại
//            stateManager.setStatusMessage("You cleared all bricks! Final Score: " + score);
//            stateManager.markGameOver();
//            return;
//        }

        private void addScore(int basePoints) {
            double multiplier = gameContext != null ? gameContext.getScoreMultiplier() : 1.0;
            int pointsToAdd = (int) Math.round(basePoints * multiplier);
            score += pointsToAdd;
            stateManager.updateStats(score, lives);
        }

        private void handleLifeLost() {
            lives--;
            System.out.println("You lost a life! Lives remaining: " + lives);

            if (effectManager != null) {
                effectManager.clearAll();
            }
            powerUps.clear();

            stateManager.updateStats(score, lives);
            if (lives <= 0) {
                System.out.println("Game Over! Final Score: " + score);
                stateManager.setStatusMessage("Game Over! Final Score: " + score);
                stateManager.markGameOver();
            } else {
                resetBallAndPaddlePosition();
                stateManager.setStatusMessage("Lives remaining: " + lives);
            }
        }

        private void addLives(int amount) {
            if (amount == 0) {
                return;
            }

            lives += amount;
            System.out.println("Lives updated: " + lives);
            stateManager.updateStats(score, lives);
            stateManager.setStatusMessage("Lives: " + lives);
        }

        private void checkLevelCompletion() {
            boolean allDestroyableBricksDestroyed = true;
            for (Brick brick : bricks) {
                if (brick.getType() != Brick.BrickType.INDESTRUCTIBLE && !brick.isDestroyed()) {
                    allDestroyableBricksDestroyed = false;
                    break;
                }
            }
            if (allDestroyableBricksDestroyed) {
                System.out.println("You cleared all destroyable bricks! Final Score: " + score);
                currentLevel++;
                powerUps.clear();
                if (effectManager != null) {
                    effectManager.clearAll();
                }

                if (currentLevel > Constants.MAX_LEVELS) {
                    System.out.println("Congratulations! All levels completed!");
                    initializeGame(); // Reset game
                } else {
                    bricks.clear();
                    loadLevel(currentLevel);
                    resetBallAndPaddlePosition();
                    System.out.println("Starting Level " + currentLevel);
                }
            }

//        boolean allDestroyableBricksDestroyed = true;
//        for (Brick brick : bricks) {
//            if (brick.getType() != Brick.BrickType.INDESTRUCTIBLE && !brick.isDestroyed()) {
//                allDestroyableBricksDestroyed = false;
//                break;
//            }
//        }

            // Chuyển level
//            currentLevel++;
//            if (currentLevel > Constants.MAX_LEVELS) { // Kiểm tra nếu đã hết các level
//                System.out.println("Congratulations! All levels completed!");
//                initializeGame(); // Reset game
//            } else {
//                bricks.clear(); // Xóa gạch cũ
//                loadLevel(currentLevel); // Tải level mới
//                resetBallAndPaddlePosition(); // Đặt lại bóng/paddle cho level mới
//                System.out.println("Starting Level " + currentLevel);
//            }
        }

        private Ball getPrimaryBall() {
            return balls.isEmpty() ? null : balls.get(0);

        }

    /**
     * Đặt lại vị trí của bóng và paddle sau khi mất mạng.
     * Bóng sẽ bắt đầu di chuyển ngay lập tức.
     */
    private void resetBallAndPaddlePosition() {
        paddle.setX((Constants.WIDTH - Constants.PADDLE_WIDTH) / 2.0); // Đặt paddle giữa màn hình
        paddle.setDx(0); // Dừng paddle

        // Khởi tạo lại bóng với constructor hiện có của bạn
//        ball = new Ball(
////                Constants.WIDTH / 2.0,
////                Constants.HEIGHT / 2.0,
            balls.clear();
            Ball newBall = new Ball(
                paddle.getX() + paddle.getWidth() / 2.0,
                paddle.getY() - Constants.BALL_RADIUS,
                Constants.BALL_RADIUS,
                Constants.BALL_SPEED,
//                (random.nextBoolean() ? 1 : -1),
                0,
                -1
        );
            balls.add(newBall);
            attachBallToPaddle();
    }

    private void attachBallToPaddle() {
        Ball ball = getPrimaryBall();
        if (ball == null) {
            return;
        }

        ballAttachedToPaddle = true;
        isBallLaunched = false;
        alignBallWithPaddle(ball);
    }

    private void alignBallWithPaddle(Ball ball) {
        if (ball == null || paddle == null) {
            return;
        }

        double ballX = paddle.getX() + paddle.getWidth() / 2.0 - ball.getWidth() / 2.0;
        double ballY = paddle.getY() - ball.getHeight();
        ball.setPosition(ballX, ballY);
        ball.setVelocity(0, 0);
    }
//    private void resetBallAndPaddlePosition() {
//        isBallLaunched = false;
//        double ballX = paddle.getX() + (paddle.getWidth() / 2) - Constants.BALL_RADIUS;
//        // Vị trí Y của bóng để nằm ngay trên paddle
//        double ballY = paddle.getY() - (Constants.BALL_RADIUS * 2) - 1;
//
//        ball = new Ball(
//                ballX,
//                ballY,
//                Constants.BALL_RADIUS,
//                Constants.DEFAULT_SPEED,
//                0, 0 // Tốc độ ban đầu là 0
//        );
//    }

    /**
     * Phương thức chính để vẽ tất cả các đối tượng game lên màn hình
     * @param g GraphicsContext của Canvas để vẽ
     */

    public void render(GraphicsContext g) {
        // Xóa màn hình bằng màu nền
//        g.setFill(Color.BLACK);
//        g.fillRect(0, 0, Constants.WIDTH, Constants.HEIGHT);
// Xóa nội dung canvas để lộ lớp nền bên dưới.
        if (effectManager == null) {
            // bỏ qua phần HUD power-up, vẫn render phần khác để không crash
            return;
        }
        g.clearRect(0, 0, Constants.WIDTH, Constants.HEIGHT);
        // Vẽ Paddle
        if (paddle != null) {
            paddle.render(g);
        }
        // Vẽ Ball
//        if (ball != null) {
//            ball.render(g);
//        }
        for (Ball ball : balls) {
            ball.render(g);
        }

        // Vẽ tất cả Bricks còn lại
        for (Brick brick : bricks) {
            brick.render(g);
        }

        for (PowerUp powerUp : powerUps) {
            powerUp.render(g);
        }

        // Hiển thị thông tin game
//        g.setFill(Color.WHITE);
//        g.fillText("Score: " + score, 10, 20);
//        g.fillText("Lives: " + lives, 10, 40);
//        // Không hiển thị Level vì không có khái niệm level phức tạp
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

    public Ball getBall() {
        // Trả về ball để MainConsole có thể đọc/ghi trực tiếp nếu cần
//        return ball;
        return getPrimaryBall();
    }

    public boolean isBallAttachedToPaddle() {
        return ballAttachedToPaddle;
    }

    public void releaseBall() {

        Ball ball = getPrimaryBall();
        if (ball == null || paddle == null || !stateManager.isRunning() || !ballAttachedToPaddle) {
            return;
        }

        ballAttachedToPaddle = false;
        isBallLaunched = true;
        double dirX = random.nextBoolean() ? 1 : -1;
        ball.setDirection(dirX, -1);
    }

    // Các getters để MainConsole có thể đọc thông tin game (score, lives)
    public int getScore() { return score; }
    public int getLives() { return lives; }

    public GameStateManager getStateManager() {
        return stateManager;
    }

    // ===== Tạo snapshot để lưu =====
    public SaveService.GameSnapshot createSnapshot() {
        SaveService.GameSnapshot s = new SaveService.GameSnapshot();
        s.score = getScore();
        s.lives = getLives();
        Ball ball = getPrimaryBall();
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

        Ball ball = getPrimaryBall();
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
