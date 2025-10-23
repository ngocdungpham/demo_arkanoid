// File: src/main/java/com/ooparkanoid/console/MainConsole.java
package com.ooparkanoid.console;

import javafx.application.Application;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;

import com.ooparkanoid.core.engine.GameManager; // Import GameManager
import com.ooparkanoid.ui.GameSceneRoot;
import com.ooparkanoid.core.state.GameStateManager;

public class MainConsole extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Arkanoid - Simple Brick Game");
        GameSceneRoot gameSceneRoot = new GameSceneRoot();
        stage.setScene(gameSceneRoot.getScene());  //
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
