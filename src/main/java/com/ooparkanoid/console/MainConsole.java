package com.ooparkanoid.console;

// File: src/main/java/com/ooparkanoid/Main.java

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import com.ooparkanoid.object.Ball;
import com.ooparkanoid.object.Paddle;
import com.ooparkanoid.utils.Constants;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class MainConsole extends Application {
    private Paddle paddle;
    private Ball ball;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Arkanoid");
        Group root = new Group();
        Canvas canvas = new Canvas(Constants.WIDTH, Constants.HEIGHT);
        root.getChildren().add(canvas);
        Scene scene = new Scene(root, Constants.WIDTH, Constants.HEIGHT);
        stage.setScene(scene);
        stage.show();

        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Khởi tạo các đối tượng game
        paddle = new Paddle(
                (Constants.WIDTH - Constants.PADDLE_WIDTH) / 2.0,
                Constants.HEIGHT - 40
        );
        ball = new Ball(
                Constants.WIDTH / 2.0,
                Constants.HEIGHT / 2.0,
                Constants.BALL_RADIUS,
                Constants.DEFAULT_SPEED, 1, -1
        );

        // Xử lý đầu vào bàn phím
        scene.setOnKeyPressed((KeyEvent event) -> {
            if (event.getCode() == KeyCode.A || event.getCode() == KeyCode.LEFT) {
                paddle.setDx(-Constants.DEFAULT_SPEED);
            } else if (event.getCode() == KeyCode.D || event.getCode() == KeyCode.RIGHT) {
                paddle.setDx(Constants.DEFAULT_SPEED);
            }
        });
        scene.setOnKeyReleased((KeyEvent event) -> {
            if (event.getCode() == KeyCode.A || event.getCode() == KeyCode.LEFT ||
                    event.getCode() == KeyCode.D || event.getCode() == KeyCode.RIGHT) {
                paddle.setDx(0);
            }
        });

        // Xử lý đầu vào chuột
        scene.setOnMouseMoved((MouseEvent event) -> {
            paddle.setX(event.getX() - paddle.getWidth() / 2);
        });

        new AnimationTimer() {
            long last = 0;

            @Override
            public void handle(long now) {
                if (last == 0) {
                    last = now;
                    return;
                }
                double dt = (now - last) / 1e9;
                last = now;

                // Cập nhật vị trí bóng
                ball.update(dt);
                paddle.update(dt);

                // Xử lý va chạm
                if (ball.getX() <= 0 || ball.getX() + ball.getWidth() >= Constants.WIDTH) {
                    ball.setDirection(-ball.getDx(), ball.getDy());
                }
                if (ball.getY() <= 0) {
                    ball.setDirection(ball.getDx(), -ball.getDy());
                }
                if (ball.istersected(paddle)) {
                    ball.setDirection(ball.getDx(), -ball.getDy());
                }
                if (ball.getY() + ball.getHeight() >= Constants.HEIGHT) {
                    System.out.println("Game Over!");
                    this.stop(); // Dừng AnimationTimer
                }

                // Render
                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, Constants.WIDTH, Constants.HEIGHT);

                // Vẽ bóng và thanh đỡ
                gc.setFill(Color.WHITE);
                gc.fillOval(ball.getX(), ball.getY(), ball.getWidth(), ball.getHeight());
                gc.fillRect(paddle.getX(), paddle.getY(), paddle.getWidth(), paddle.getHeight());

                // Debug fps
//                gc.setFill(Color.YELLOW);
//                gc.fillText(String.format("FPS: %.1f", 1.0 / dt), 10, 20);
            }
        }.start();
    }

    public static void main(String[] args) {
        launch();
    }
}
