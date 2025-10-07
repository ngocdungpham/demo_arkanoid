// File: src/main/java/com/ooparkanoid/console/MainConsole.java
package com.ooparkanoid.console;

import com.ooparkanoid.ui.MenuScene;
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
    // Không còn paddle và ball trực tiếp ở đây, mà sẽ thông qua GameManager
    private GameManager gameManager;
    // GraphicsContext vẫn giữ ở đây để MainConsole có thể truyền cho GameManager
    private GraphicsContext gc;
    private AnimationTimer gameLoop;
    @Override
    public void start(Stage stage) {
//        stage.setTitle("Arkanoid - Simple Brick Game");
//        Group root = new Group();
//        Canvas canvas = new Canvas(Constants.WIDTH, Constants.HEIGHT);
//        root.getChildren().add(canvas);
//        Scene scene = new Scene(root, Constants.WIDTH, Constants.HEIGHT);
//        stage.setScene(scene);
//        stage.setResizable(false); // Thường là không cho thay đổi kích thước cửa sổ game
//        stage.show();
        gameManager = new GameManager();

        // Tạo màn hình menu
        MenuScene menuScene = new MenuScene(stage, () -> {
            stage.setScene(createScene(stage));
        });

        // Hiển thị menu ban đầu
        stage.setScene(menuScene);
        stage.setTitle("Arkanoid Plus");
        stage.setResizable(false);
        stage.show();
    }
    private Scene createScene(Stage stage) {
        Group root = new Group();
        Canvas canvas = new Canvas(Constants.WIDTH, Constants.HEIGHT);
        root.getChildren().add(canvas);
        Scene scene = new Scene(root, Constants.WIDTH, Constants.HEIGHT);
        gc = canvas.getGraphicsContext2D();

        //gameManager = new GameManager(); // Khởi tạo GameManager
        gameManager.initializeGame();

        Deque<KeyCode> pressedStack = new ArrayDeque<>();

        scene.setOnKeyPressed(e -> {
            KeyCode code = e.getCode();
            // tránh add trùng
            if (!pressedStack.contains(code)) {
                pressedStack.push(code); // đưa lên đầu stack
            }
            if (code == KeyCode.SPACE) {
                gameManager.launchBall();
            }
        });

        scene.setOnKeyReleased(e -> {
            pressedStack.remove(e.getCode()); // xóa khỏi stack
        });

        scene.setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                gameManager.launchBall();
            }
        });
        // Xử lý đầu vào chuột (sẽ gọi các phương thức trên paddle thông qua GameManager)
        scene.setOnMouseMoved((MouseEvent event) -> {
            gameManager.getPaddle().setX(event.getX() - gameManager.getPaddle().getWidth() / 2);
        });

        // Vòng lặp game chính
        gameLoop = new AnimationTimer() {
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
                    }else{
                        gameManager.getPaddle().setDx(0);
                    }
                } else {
                    gameManager.getPaddle().setDx(0);
                }

                double dt = (now - last) / 1e9; // Thời gian trôi qua giữa các frame (giây)
                last = now;

                // UỶ QUYỀN cho GameManager cập nhật và render
                gameManager.update(dt);
                gameManager.render(gc);

                // --- Logic kiểm tra "Game Over!" có thể được đưa vào GameManager ---
                // Tuy nhiên, nếu bạn muốn giữ nó ở đây, bạn có thể gọi:
                // if (gameManager.isGameOver()) {
                //     System.out.println("Game Over! Final Score: " + gameManager.getScore());
                //     this.stop(); // Dừng AnimationTimer
                // }
                // Hiện tại, GameManager sẽ tự reset nếu thua.

            }
        };
        gameLoop.start();
        return scene;
    }
    public static void main(String[] args) {
        launch();
    }
}
