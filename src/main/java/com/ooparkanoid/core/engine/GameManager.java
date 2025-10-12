// File: src/main/java/com/ooparkanoid/core/engine/GameManager.java
package com.ooparkanoid.core.engine;

import com.ooparkanoid.core.save.SaveService;
import com.ooparkanoid.core.state.GameState;
import com.ooparkanoid.core.state.GameStateManager;
import com.ooparkanoid.object.Ball;
import com.ooparkanoid.object.Paddle;
import com.ooparkanoid.object.PowerUp.ExpandPaddlePowerUp;
import com.ooparkanoid.object.PowerUp.FastBallPowerUp;
import com.ooparkanoid.object.PowerUp.PowerUp;
import com.ooparkanoid.object.bricks.Brick; // Import Brick
import com.ooparkanoid.object.bricks.NormalBrick; // Import NormalBrick
import com.ooparkanoid.object.bricks.StrongBrick; // Import StrongBrick
import com.ooparkanoid.core.save.SaveService;

import com.ooparkanoid.utils.Constants;
import javafx.scene.canvas.GraphicsContext;
//import javafx.scene.paint.Color;

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
    private List<PowerUp> powerUps = new ArrayList<>();
    private List<Ball> balls = new ArrayList<>();
    private int score;
    private int lives;
    private Random random;
    private boolean ballLaunched = false;
    private final GameStateManager stateManager;
    private boolean ballAttachedToPaddle = true; // mặc định dính khi new game/life

    public GameManager() {
        instance = this;
        this(new GameStateManager());
    }
    public GameManager(GameStateManager stateManager) {
        this.stateManager = stateManager;
        bricks = new ArrayList<>();
        random = new Random();
        initializeGame(); // Gọi hàm khởi tạo game
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

        // Khởi tạo Ball theo constructor hiện tại của bạn
        // Ball sẽ bắt đầu di chuyển ngay lập tức khi được tạo
        balls.clear();
        ball = new Ball(
                Constants.WIDTH / 2.0,
                Constants.HEIGHT / 2.0,
                Constants.BALL_RADIUS,
                Constants.DEFAULT_SPEED, // Speed từ Constants
                0 , -1 // dirY luôn hướng lên
        );
        balls.add(ball);
        // Khởi tạo thông tin game
        score = 0;
        ballLaunched = false;
        lives = Constants.START_LIVES; // Lấy từ Constants
        bricks.clear(); // Xóa gạch cũ nếu có
        createInitialBricks(); // Hàm tạo gạch ban đầu
        powerUps.clear();
        FastBallPowerUp.resetEffect();

        System.out.println("Game Initialized. Score: " + score + ", Lives: " + lives);
        stateManager.updateStats(score, lives);
        stateManager.setStatusMessage("Destroy all the bricks!");
    }

    /**
     * Tạo bố cục gạch ban đầu. (Không có khái niệm level phức tạp ở đây)
     */
    private void createInitialBricks() {
        int rows = 5; // Số hàng gạch
        // Tính số cột gạch tối đa có thể vừa trên màn hình
        int cols = (int)(Constants.WIDTH / (Constants.BRICK_WIDTH + Constants.BRICK_PADDING_X));

        // Tính toán vị trí X bắt đầu để canh giữa các hàng gạch trên màn hình
        double totalBricksWidth = cols * Constants.BRICK_WIDTH + (cols - 1) * Constants.BRICK_PADDING_X;
        double startX = (Constants.WIDTH - totalBricksWidth) / 2;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double brickX = startX + c * (Constants.BRICK_WIDTH + Constants.BRICK_PADDING_X);
                double brickY = Constants.BRICK_OFFSET_TOP + r * (Constants.BRICK_HEIGHT + Constants.BRICK_PADDING_Y);

                // Xen kẽ NormalBrick và StrongBrick
                if (r % 2 == 0) {
                    bricks.add(new NormalBrick(brickX, brickY));
                } else {
                    bricks.add(new StrongBrick(brickX, brickY));
                }
            }
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
            // Va chạm Ball-Paddle
            if (ball.istersected(paddle)) {
                // Đẩy bóng lên trên paddle một chút để tránh kẹt
                ball.setY(paddle.getY() - ball.getHeight() - 1);


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

        // Va chạm Ball-Paddle
//        if (ball.istersected(paddle)) { // Sử dụng istersected của Ball
//            // Đảm bảo bóng không bị kẹt trong paddle bằng cách đẩy bóng lên
//            ball.setY(paddle.getY() - ball.getHeight());
//
//            // Tính toán góc nảy (đơn giản, chỉ đảo hướng Y)
//            // SỬA: Dùng getDx() và getDy()
//            ball.setDirection(ball.getDx(), -ball.getDy());
//        }
        // Va chạm Ball-Paddle
        if (ball.istersected(paddle)) { // Giả sử bạn có hàm intersects()
            // Đẩy bóng lên trên paddle một chút để tránh kẹt
            ball.setY(paddle.getY() - ball.getHeight() - 1);

            // Tính toán tâm paddle và tâm bóng
            double paddleCenter = paddle.getX() + paddle.getWidth() / 2.0;
            double ballCenter   = ball.getX() + ball.getWidth() / 2.0;

            // Xác định độ lệch của bóng so với tâm paddle [-1..1]
            double relativeIntersect = (ballCenter - paddleCenter) / (paddle.getWidth() / 2.0);

            // Giới hạn góc nảy tối đa (±60°)
            double maxBounceAngle = Math.toRadians(60);
            double bounceAngle = relativeIntersect * maxBounceAngle;

            // Giữ nguyên tốc độ bóng (nên có hằng số speed hoặc hàm getSpeed())
            double speed = Constants.BALL_SPEED;

            // Cập nhật vận tốc mới dựa trên góc nảy
            double newDx = speed * Math.sin(bounceAngle);
            double newDy = -Math.abs(speed * Math.cos(bounceAngle)); // đảm bảo luôn đi lên

            ball.setDirection(newDx, newDy);
        }

                // Tính toán tâm paddle và tâm bóng
                double paddleCenter = paddle.getX() + paddle.getWidth() / 2.0;
                double ballCenter   = ball.getX() + ball.getWidth() / 2.0;

                // Xác định độ lệch của bóng so với tâm paddle [-1..1]
                double relativeIntersect = (ballCenter - paddleCenter) / (paddle.getWidth() / 2.0);

                // Giới hạn góc nảy tối đa (±60°)
                double maxBounceAngle = Math.toRadians(60);
                double bounceAngle = relativeIntersect * maxBounceAngle;

                // Giữ nguyên tốc độ bóng (nên có hằng số speed hoặc hàm getSpeed())
                double speed = ball.getSpeed();

                // Cập nhật vận tốc mới dựa trên góc nảy
                double newDx = speed * Math.sin(bounceAngle);
                double newDy = -Math.abs(speed * Math.cos(bounceAngle)); // đảm bảo luôn đi lên

                ball.setDirection(newDx, newDy);
            }

        // Va chạm Ball-Bricks
        // Sử dụng Iterator để có thể xóa gạch an toàn trong vòng lặp
        Iterator<Brick> brickIterator = bricks.iterator();
        while (brickIterator.hasNext()) {
            Brick brick = brickIterator.next();
            if (!brick.isDestroyed()) { // Chỉ kiểm tra va chạm với gạch chưa bị phá hủy
                if (ball.istersected(brick)) { // Sử dụng istersected của Ball
                    brick.takeHit(); // Gạch nhận một cú đánh
                    score += 10;     // Tăng điểm
                    stateManager.updateStats(score, lives);
                    // Logic va chạm bóng với gạch (đơn giản, chỉ đảo ngược hướng Y)
                    // SỬA: Dùng getDx() và getDy()
                    ball.setDirection(ball.getDx(), -ball.getDy());

                        if (brick.isDestroyed()) {
                            System.out.println("Brick destroyed! Score: " + score);
                            brickIterator.remove(); // Xóa gạch đã bị phá hủy khỏi danh sách
                        }
                        // Giả định bóng chỉ va chạm với một gạch mỗi frame để đơn giản
                        break;
                    }
                }
            }
            // Nếu bóng rơi khỏi màn hình
            if (ball.getY() + ball.getHeight() >= Constants.HEIGHT) {
                ballIt.remove();
            }
        }



        // update powerups
        for (Iterator<PowerUp> it = powerUps.iterator(); it.hasNext();) {
            PowerUp p = it.next();
            p.update(dt);

            if (!p.isActive() && !p.isCollected() && paddle.istersected(p)) {
                p.applyEffect();
            }

            // Nếu PowerUp đã hết duration -> removeEffect + xóa object
            if (p.isExpired()) {
                p.removeEffect();
                it.remove();
            }

            // Nếu powerup rơi ra ngoài màn hình và chưa collect -> xóa
            if (!p.isCollected() && p.getY() > Constants.HEIGHT) {
                it.remove();
            }
        }
        // Kiểm tra bóng rơi khỏi màn hình (mất mạng)
        if (balls.isEmpty()) {
            lives--;
            System.out.println("You lost a life! Lives remaining: " + lives);
            stateManager.updateStats(score, lives);
            if (lives <= 0) {
                System.out.println("Game Over! Final Score: " + score);
//                initializeGame(); // Reset game hoàn toàn sau khi Game Over
                stateManager.setStatusMessage("Game Over! Final Score: " + score);
                stateManager.markGameOver();
                return;
                initializeGame();
            } else {

                // Đặt lại bóng và paddle về vị trí ban đầu sau khi mất mạng
                resetBallAndPaddlePosition();
                stateManager.setStatusMessage("Lives remaining: " + lives);
            }
        }

        // Kiểm tra tất cả gạch đã bị phá hủy (điều kiện chiến thắng)
        if (bricks.isEmpty()) {
            System.out.println("You cleared all bricks! Final Score: " + score);
//            initializeGame(); // Reset game để chơi lại
            stateManager.setStatusMessage("You cleared all bricks! Final Score: " + score);
            stateManager.markGameOver();
            return;
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
        Ball newBall = new Ball(
                Constants.WIDTH / 2.0,
                Constants.HEIGHT / 2.0,
                Constants.BALL_RADIUS,
                Constants.DEFAULT_SPEED,
                0,
                -1
        );
        balls.add(newBall);
        ballLaunched = false;
        powerUps.clear();
        FastBallPowerUp.resetEffect();
    }

    /**
     * Phương thức chính để vẽ tất cả các đối tượng game lên màn hình
     * @param g GraphicsContext của Canvas để vẽ
     */

    public void render(GraphicsContext g) {
        // Xóa màn hình bằng màu nền
//        g.setFill(Color.BLACK);
//        g.fillRect(0, 0, Constants.WIDTH, Constants.HEIGHT);
// Xóa nội dung canvas để lộ lớp nền bên dưới.
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

        for (PowerUp p : powerUps) {
            if (p.isCollected()) continue;
            p.render(g);
        }
        // Hiển thị thông tin game
        g.setFill(Color.WHITE);
        g.fillText("Score: " + score, 10, 20);
        g.fillText("Lives: " + lives, 10, 40);
        if (FastBallPowerUp.getRemainingTime() > 0) {
            g.setFill(Color.RED);
            g.fillText("FastBall :" + String.format("%f", FastBallPowerUp.getRemainingTime())
                    , 10, 30);
        }
        // Không hiển thị Level vì không có khái niệm level phức tạp
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
    public List<Ball> getBalls() { return balls; }
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
