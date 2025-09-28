package com.ooparkanoid.ui;

import com.ooparkanoid.utils.Constants;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

/**
 * GameSceneRoot sẽ quản lý Scene (Menu, Pause, Game, ...).
 * Hiện tại chỉ để placeholder, sẽ dùng từ Tuần 2–4.
 */
public class GameSceneRoot {
    private Scene scene;
    private GraphicsContext g;

    public GameSceneRoot() {
        Group root = new Group();
        Canvas canvas = new Canvas(Constants.WIDTH, Constants.HEIGHT);
        root.getChildren().add(canvas);
        scene = new Scene(root, Constants.WIDTH, Constants.HEIGHT);
        g = canvas.getGraphicsContext2D();
    }

    public Scene getScene() {
        return scene;
    }

    public GraphicsContext getGraphicsContext() {
        return g;
    }
}































