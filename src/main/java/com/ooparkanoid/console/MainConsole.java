// File: src/main/java/com/ooparkanoid/console/MainConsole.java
package com.ooparkanoid.console;

import javafx.animation.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
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
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class MainConsole extends Application {
    private Stage stage;
    private GameSceneRoot gameSceneRoot;
    // Không còn paddle và ball trực tiếp ở đây, mà sẽ thông qua GameManager
    // private GameManager gameManager;
    //   private GameStateManager stateManager;
    // GraphicsContext vẫn giữ ở đây để MainConsole có thể truyền cho GameManager
    //  private GraphicsContext gc;

    @Override
    public void start(Stage stage) throws IOException {
        this.stage = stage;
        stage.setTitle("Arkanoid - Simple Brick Game");
        stage.setResizable(false);

        // --- 1️⃣ Load intro scene ---
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/intro.fxml"));
        Parent introRoot = fxmlLoader.load();

        Scene scene = new Scene(introRoot, Constants.WIDTH, Constants.HEIGHT);
        stage.setScene(scene);
        stage.show();

        // --- 2️⃣ Nhấn SPACE để chuyển sang game ---
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                startTransition(introRoot);
            }
        });
    }

    private void startTransition(Parent introRoot) {
        Canvas blackOverlay = new Canvas(Constants.WIDTH, Constants.HEIGHT);
        GraphicsContext gc = blackOverlay.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, Constants.WIDTH, Constants.HEIGHT);
        blackOverlay.setOpacity(0); // ban đầu trong suốt

        if (introRoot instanceof Group) {
            ((Group) introRoot).getChildren().add(blackOverlay);
        } else {
            Group wrapper = new Group(introRoot, blackOverlay);
            stage.getScene().setRoot(wrapper);
        }

        // --- 2️⃣ Fade in overlay (từ trong suốt → đen) ---
        FadeTransition fadeBlack = new FadeTransition(Duration.seconds(1), blackOverlay);
        fadeBlack.setFromValue(0);
        fadeBlack.setToValue(1);
        fadeBlack.setInterpolator(Interpolator.EASE_IN);

        // --- 3️⃣ Khi màn đen phủ kín, chuyển sang game ---
        fadeBlack.setOnFinished(e -> startGame());

        fadeBlack.play();
    }

    /**
     * 3️⃣ Hàm này sẽ chạy sau khi intro biến mất.
     */
    private void startGame() {
        GameSceneRoot gameSceneRoot = new GameSceneRoot();
        stage.setScene(gameSceneRoot.getScene());  // ✅ chỉ cần dòng này thôi
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}