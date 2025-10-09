// File: src/main/java/com/ooparkanoid/console/MainConsole.java
package com.ooparkanoid.console;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import com.ooparkanoid.core.engine.GameManager; // Import GameManager
import com.ooparkanoid.core.state.GameState;
import com.ooparkanoid.ui.GameSceneRoot;
import com.ooparkanoid.core.state.GameStateManager;
import com.ooparkanoid.utils.Constants;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.util.ArrayDeque;
import java.util.Deque;

public class MainConsole extends Application {
    // Không còn paddle và ball trực tiếp ở đây, mà sẽ thông qua GameManager
    private GameManager gameManager;
    private GameStateManager stateManager;
    // GraphicsContext vẫn giữ ở đây để MainConsole có thể truyền cho GameManager
    private GraphicsContext gc;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Arkanoid - Simple Brick Game");

        Group root = new Group();
        GameSceneRoot gameSceneRoot = new GameSceneRoot();

        Canvas canvas = new Canvas(Constants.WIDTH, Constants.HEIGHT);
        root.getChildren().add(canvas);
        Scene scene = new Scene(root, Constants.WIDTH, Constants.HEIGHT);

//        stage.setScene(scene);
        stage.setScene(gameSceneRoot.getScene());

        stage.setResizable(false); // Thường là không cho thay đổi kích thước cửa sổ game
        stage.show();

        gc = canvas.getGraphicsContext2D();

        stateManager = new GameStateManager();
        gameManager = new GameManager(stateManager); // Khởi tạo GameManager
        gameManager.initializeGame();
        stateManager.beginNewGame(gameManager.getScore(), gameManager.getLives());

        Deque<KeyCode> pressedStack = new ArrayDeque<>();

        scene.setOnKeyPressed(e -> {
            KeyCode code = e.getCode();
            // tránh add trùng
            if (!pressedStack.contains(code)) {
                pressedStack.push(code); // đưa lên đầu stack
            }
        });

        scene.setOnKeyReleased(e -> {
            pressedStack.remove(e.getCode()); // xóa khỏi stack
        });


        // Xử lý đầu vào bàn phím (sẽ gọi các phương thức trên paddle thông qua GameManager)
//        scene.setOnKeyPressed((KeyEvent event) -> {
//            if (event.getCode() == KeyCode.A || event.getCode() == KeyCode.LEFT) {
//                gameManager.getPaddle().setDx(-Constants.DEFAULT_SPEED);
//            } else if (event.getCode() == KeyCode.D || event.getCode() == KeyCode.RIGHT) {
//                gameManager.getPaddle().setDx(Constants.DEFAULT_SPEED);
//            }
//        });
//        scene.setOnKeyReleased((KeyEvent event) -> {
//            if (event.getCode() == KeyCode.A || event.getCode() == KeyCode.LEFT ||
//                    event.getCode() == KeyCode.D || event.getCode() == KeyCode.RIGHT) {
//                gameManager.getPaddle().setDx(0);
//            }
//        });

        // Xử lý đầu vào chuột (sẽ gọi các phương thức trên paddle thông qua GameManager)
        scene.setOnMouseMoved((MouseEvent event) -> {
            gameManager.getPaddle().setX(event.getX() - gameManager.getPaddle().getWidth() / 2);
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
                        gameManager.getPaddle().setDx(-Constants.PADDLE_SPEED);
                        System.out.println(pressedStack);
                    } else if (key == KeyCode.D || key == KeyCode.RIGHT) {
                        gameManager.getPaddle().setDx(Constants.PADDLE_SPEED);
                        System.out.println(pressedStack);
                    } else {
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

                gc.setFill(Color.WHITE);
//                gc.fillText("Score: " + stateManager.getScore(), 10, 20);
//                gc.fillText("Lives: " + stateManager.getLives(), 10, 40);

                if (stateManager.getCurrentState() == GameState.GAME_OVER) {
                    gameManager.initializeGame();
                    stateManager.beginNewGame(gameManager.getScore(), gameManager.getLives());
                }

                // --- Logic kiểm tra "Game Over!" có thể được đưa vào GameManager ---
                // Tuy nhiên, nếu bạn muốn giữ nó ở đây, bạn có thể gọi:
//                 if (gameManager.isGameOver()) {
//                     System.out.println("Game Over! Final Score: " + gameManager.getScore());
//                     this.stop(); // Dừng AnimationTimer
//                 }
                // Hiện tại, GameManager sẽ tự reset nếu thua.
            }
        }.start();
    }

    public static void main(String[] args) {
        launch();
    }
}
