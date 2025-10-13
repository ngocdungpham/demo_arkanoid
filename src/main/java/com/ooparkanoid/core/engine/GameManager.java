// File: src/main/java/com/ooparkanoid/core/engine/GameManager.java
package com.ooparkanoid.core.engine;

import com.ooparkanoid.object.Ball;
import com.ooparkanoid.object.Paddle;
import com.ooparkanoid.object.bricks.Brick; // Import Brick
import com.ooparkanoid.object.bricks.NormalBrick; // Import NormalBrick
import com.ooparkanoid.object.bricks.StrongBrick; // Import StrongBrick

import com.ooparkanoid.utils.Constants;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

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
    private Paddle paddle;
    private Ball ball;
    private List<Brick> bricks; // Danh sách các khối gạch

    private int score;
    private int lives;
    private Random random;

    private boolean isBallLaunched;
    public GameManager() {
        bricks = new ArrayList<>();
        random = new Random();
        initializeGame(); // Gọi hàm khởi tạo game
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
        isBallLaunched = false;
        score = 0;
        lives = Constants.START_LIVES; // Lấy từ Constants

        // Tạo gạch ban đầu
        bricks.clear(); // Xóa gạch cũ nếu có
        createInitialBricks(); // Hàm tạo gạch ban đầu
       resetBallAndPaddlePosition();

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
        // Cập nhật vị trí của Paddle và Ball
        paddle.update(dt);
       // ball.move(dt); // Gọi move() của Ball
        if (isBallLaunched) {
            ball.move(dt);
        } else {
            // Bóng đi theo paddle khi chưa được phóng
            ball.setX(paddle.getX() + (paddle.getWidth() / 2) - Constants.BALL_RADIUS);
        }
        // --- Xử lý Va chạm ---
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

        // Va chạm Ball - Paddle
        if (ball.intersected(paddle)) {
            // 1. Đẩy bóng ra khỏi paddle để tránh kẹt
            ball.setY(paddle.getY() - ball.getHeight() - 0.5);

            // 2. Tính toán tâm paddle và tâm bóng
            double paddleCenter = paddle.getX() + paddle.getWidth() / 2.0;
            double ballCenter   = ball.getX() + ball.getWidth() / 2.0;

            // 3. Độ lệch [-1..1]: -1 = mép trái, 0 = giữa, +1 = mép phải
            double relativeIntersect = (ballCenter - paddleCenter) / (paddle.getWidth() / 2.0);

            // 4. Dead-zone ở giữa: nếu gần tâm quá thì random sang trái/phải
            double deadZone = 0.05; // ±5% chiều rộng paddle
            if (Math.abs(relativeIntersect) < deadZone) {
                relativeIntersect = (Math.random() < 0.5 ? -deadZone : +deadZone);
            }

            // 5. Tính góc nảy
            double maxBounce = Math.toRadians(60);   // tối đa ±60°
            double bounceAngle = relativeIntersect * maxBounce;

            // 6. Giữ nguyên tốc độ hiện tại
            double speed = Math.sqrt(ball.getDx() * ball.getDx() + ball.getDy() * ball.getDy());

            // 7. Cập nhật vận tốc mới
            double newDx = speed * Math.sin(bounceAngle);
            double newDy = -Math.abs(speed * Math.cos(bounceAngle)); // luôn bật lên trên

            ball.setDirection(newDx, newDy);
        }

        // Va chạm Ball-Bricks
        // Sử dụng Iterator để có thể xóa gạch an toàn trong vòng lặp
        Iterator<Brick> brickIterator = bricks.iterator();
        while (brickIterator.hasNext()) {
            Brick brick = brickIterator.next();
            if (!brick.isDestroyed()) { // Chỉ kiểm tra va chạm với gạch chưa bị phá hủy
                if (ball.intersected(brick)) { // Sử dụng istersected của Ball
                    brick.takeHit(); // Gạch nhận một cú đánh
                    score += 10;     // Tăng điểm

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

        // Kiểm tra bóng rơi khỏi màn hình (mất mạng)
        if (ball.getY() + ball.getHeight() >= Constants.HEIGHT) {
            lives--;
            System.out.println("You lost a life! Lives remaining: " + lives);
            if (lives <= 0) {
                System.out.println("Game Over! Final Score: " + score);
                initializeGame(); // Reset game hoàn toàn sau khi Game Over
            } else {
                // Đặt lại bóng và paddle về vị trí ban đầu sau khi mất mạng
                resetBallAndPaddlePosition();
            }
        }

        // Kiểm tra tất cả gạch đã bị phá hủy (điều kiện chiến thắng)
        if (bricks.isEmpty()) {
            System.out.println("You cleared all bricks! Final Score: " + score);
            initializeGame(); // Reset game để chơi lại
        }
    }

    /**
     * Đặt lại vị trí của bóng và paddle sau khi mất mạng.
     * Bóng sẽ bắt đầu di chuyển ngay lập tức.
     */
    private void resetBallAndPaddlePosition() {
        isBallLaunched = false;
        double ballX = paddle.getX() + (paddle.getWidth() / 2) - Constants.BALL_RADIUS;
        // Vị trí Y của bóng để nằm ngay trên paddle
        double ballY = paddle.getY() - (Constants.BALL_RADIUS * 2) - 1;

        ball = new Ball(
                ballX,
                ballY,
                Constants.BALL_RADIUS,
                Constants.DEFAULT_SPEED,
                0, 0 // Tốc độ ban đầu là 0
        );
    }
    public void launchBall() {
        if (!isBallLaunched) {
            double newDx = Constants.DEFAULT_SPEED * (random.nextBoolean() ? 1 : -1);
            double newDy = -Constants.DEFAULT_SPEED;
            ball.setDirection(newDx, newDy);
            isBallLaunched = true;
        }
    }

    /**
     * Phương thức chính để vẽ tất cả các đối tượng game lên màn hình
     * @param g GraphicsContext của Canvas để vẽ
     */

    public void render(GraphicsContext g) {
        // Xóa màn hình bằng màu nền
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, Constants.WIDTH, Constants.HEIGHT);

        // Vẽ Paddle
        paddle.render(g);
        // Vẽ Ball
        ball.render(g);

        // Vẽ tất cả Bricks còn lại
        for (Brick brick : bricks) {
            brick.render(g);
        }

        // Hiển thị thông tin game
        g.setFill(Color.WHITE);
        g.fillText("Score: " + score, 10, 20);
        g.fillText("Lives: " + lives, 10, 40);
        // Không higển thị Level vì không có khái niệm level phức tạp
    }

    // --- Getters cần thiết để MainConsole có thể tương tác với Paddle và Ball ---
    public Paddle getPaddle() {
        return paddle;
    }

    public Ball getBall() { // Trả về ball để MainConsole có thể đọc/ghi trực tiếp nếu cần
        return ball;
    }

    // Các getters để MainConsole có thể đọc thông tin game (score, lives)
    public int getScore() { return score; }
    public int getLives() { return lives; }
}
