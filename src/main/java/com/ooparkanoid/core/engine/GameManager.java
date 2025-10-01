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

        // Khởi tạo Ball theo constructor hiện tại của bạn
        // Ball sẽ bắt đầu di chuyển ngay lập tức khi được tạo
        ball = new Ball(
                Constants.WIDTH / 2.0,
                Constants.HEIGHT / 2.0,
                Constants.BALL_RADIUS,
                Constants.DEFAULT_SPEED, // Speed từ Constants
                (random.nextBoolean() ? 1 : -1), // dirX ngẫu nhiên (1 hoặc -1)
                -1 // dirY luôn hướng lên
        );

        // Khởi tạo thông tin game
        score = 0;
        lives = Constants.START_LIVES; // Lấy từ Constants

        // Tạo gạch ban đầu
        bricks.clear(); // Xóa gạch cũ nếu có
        createInitialBricks(); // Hàm tạo gạch ban đầu

        System.out.println("Game Initialized. Score: " + score + ", Lives: " + lives);
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
        ball.move(dt); // Gọi move() của Ball

        // --- Xử lý Va chạm ---
        // Va chạm Ball-Walls (Tường trái, phải, trần)
        if (ball.getX() <= 0 || ball.getX() + ball.getWidth() >= Constants.WIDTH) {
            // SỬA: Dùng getDx() và getDy()
            ball.setDirection(-ball.getDx(), ball.getDy());
        }
        if (ball.getY() <= 0) { // Va chạm trần
            // SỬA: Dùng getDx() và getDy()
            ball.setDirection(ball.getDx(), -ball.getDy());
        }

        // Va chạm Ball-Paddle
        if (ball.istersected(paddle)) { // Sử dụng istersected của Ball
            // Đảm bảo bóng không bị kẹt trong paddle bằng cách đẩy bóng lên
            ball.setY(paddle.getY() - ball.getHeight());

            // Tính toán góc nảy (đơn giản, chỉ đảo hướng Y)
            // SỬA: Dùng getDx() và getDy()
            ball.setDirection(ball.getDx(), -ball.getDy());
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
        paddle.setX((Constants.WIDTH - Constants.PADDLE_WIDTH) / 2.0); // Đặt paddle giữa màn hình
        paddle.setDx(0); // Dừng paddle

        // Khởi tạo lại bóng với constructor hiện có của bạn
        ball = new Ball(
                Constants.WIDTH / 2.0,
                Constants.HEIGHT / 2.0,
                Constants.BALL_RADIUS,
                Constants.DEFAULT_SPEED,
                (random.nextBoolean() ? 1 : -1),
                -1
        );
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
        // Không hiển thị Level vì không có khái niệm level phức tạp
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
