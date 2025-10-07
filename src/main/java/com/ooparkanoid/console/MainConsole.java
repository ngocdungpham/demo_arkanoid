// File: src/main/java/com/ooparkanoid/console/MainConsole.java
package com.ooparkanoid.console;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import com.ooparkanoid.core.engine.GameManager; // Import GameManager
import com.ooparkanoid.utils.Constants;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.util.ArrayDeque;
import java.util.Deque;

public class MainConsole extends Application {
    private GameManager gameManager;
    private GraphicsContext gc;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Arkanoid - Simple Brick Game");
        Group root = new Group();
        Canvas canvas = new Canvas(Constants.WIDTH, Constants.HEIGHT);
        root.getChildren().add(canvas);
        Scene scene = new Scene(root, Constants.WIDTH, Constants.HEIGHT);
        stage.setScene(scene);
        stage.setResizable(false); // Thường là không cho thay đổi kích thước cửa sổ game
        stage.show();

        gc = canvas.getGraphicsContext2D();

        gameManager = new GameManager(); // Khởi tạo GameManager

        Deque<KeyCode> pressedStack = new ArrayDeque<>();

        scene.setOnKeyPressed(e -> {
            KeyCode code = e.getCode();
            // tránh add trùng
            if (!pressedStack.contains(code)) {
                pressedStack.push(code); // đưa lên đầu stack
            }
            if (code == KeyCode.ENTER) {
                gameManager.launchBall();
            }

            // ✅ Khi nhấn B → thêm bóng mới để test
            if (code == KeyCode.B) {
                gameManager.spawnExtraBall();
            }
        });

        scene.setOnKeyReleased(e -> {
            pressedStack.remove(e.getCode()); // xóa khỏi stack
        });


        // Xử lý đầu vào chuột (sẽ gọi các phương thức trên paddle thông qua GameManager)
        scene.setOnMouseMoved((MouseEvent event) -> {
            gameManager.getPaddle().setX(event.getX() - gameManager.getPaddle().getWidth() / 2);
        });

        scene.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                gameManager.launchBall();
            }
        });
        // Vòng lặp game chính
        new AnimationTimer() {
            long last = 0;

            @Override
            public void handle(long now) {
                if (last == 0) {
                    last = now;
                    return;
                }

                if (!pressedStack.isEmpty()) {
                    KeyCode key = pressedStack.peek(); // lấy phím mới nhất
                    if (key == KeyCode.A || key == KeyCode.LEFT) {
                        gameManager.getPaddle().setDx(-Constants.DEFAULT_SPEED);
                    } else if (key == KeyCode.D || key == KeyCode.RIGHT) {
                        gameManager.getPaddle().setDx(Constants.DEFAULT_SPEED);
                    }
                } else {
                    gameManager.getPaddle().setDx(0);
                }

                double dt = (now - last) / 1e9; // Thời gian trôi qua giữa các frame (giây)
                last = now;

                // UỶ QUYỀN cho GameManager cập nhật và render
                gameManager.update(dt);
                gameManager.render(gc);

<<<<<<< Updated upstream
                // --- Logic kiểm tra "Game Over!" có thể được đưa vào GameManager ---
                // Tuy nhiên, nếu bạn muốn giữ nó ở đây, bạn có thể gọi:
                // if (gameManager.isGameOver()) {
                //     System.out.println("Game Over! Final Score: " + gameManager.getScore());
                //     this.stop(); // Dừng AnimationTimer
                // }
                // Hiện tại, GameManager sẽ tự reset nếu thua.
=======
>>>>>>> Stashed changes
            }
        }.start();
    }

    public static void main(String[] args) {
        launch();
    }
}
