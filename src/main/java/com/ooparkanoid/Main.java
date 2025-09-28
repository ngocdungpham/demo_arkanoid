////package com.ooparkanoid;
////
////import javafx.application.Application;
////import javafx.scene.Scene;
////import javafx.scene.control.Label;
////import javafx.stage.Stage;
////
////public class Main extends Application {
////    @Override
////    public void start(Stage stage) {
////        Label label = new Label("Hello Arkanoid!");
////        Scene scene = new Scene(label, 400, 300);
////        stage.setScene(scene);
////        stage.setTitle("Arkanoid OOP Project");
////        stage.show();
////    }
////
////    public static void main(String[] args) {
////        launch();
////    }
////}
//package com.ooparkanoid;
//
//import javafx.animation.AnimationTimer;
//import javafx.application.Application;
//import javafx.scene.Group;
//import javafx.scene.Scene;
//import javafx.scene.canvas.Canvas;
//import javafx.scene.canvas.GraphicsContext;
//import javafx.scene.paint.Color;
//import javafx.stage.Stage;
//
//public class Main extends Application {
//    public static final int WIDTH = 800;
//    public static final int HEIGHT = 600;
//    @Override
//    public void start(Stage stage) {
//        stage.setTitle("Arkanoid OOP Project");
//        Group root = new Group();
//        Canvas canvas = new Canvas(WIDTH, HEIGHT);
//        root.getChildren().add(canvas);
//        Scene scene = new Scene(root, WIDTH, HEIGHT);
//        stage.setScene(scene);
//        stage.show();
//
//        GraphicsContext g = canvas.getGraphicsContext2D();
//
//        // demo paddle position
//        double paddleX = (WIDTH - 100) / 2.0;
//        double paddleY = HEIGHT - 40;
//        double paddleW = 100, paddleH = 16;
//
//        new AnimationTimer() {
//            long last = 0;
//
//            @Override
//            public void handle(long now) {
//                if (last == 0) { last = now; return; }
//                double dt = (now - last) / 1e9; // seconds
//                last = now;
//
//                // clear
//                g.setFill(Color.BLACK);
//                g.fillRect(0, 0, WIDTH, HEIGHT);
//
//                // draw paddle
//                g.setFill(Color.WHITE);
//                g.fillRect(paddleX, paddleY, paddleW, paddleH);
//
//                // FPS
//                g.setFill(Color.YELLOW);
//                g.fillText(String.format("FPS: %.1f", 1.0 / dt), 10, 20);
//            }
//        }.start();
//    }
//
//    public static void main(String[] args) {
//        launch();
//    }
//}
package com.ooparkanoid;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import com.ooparkanoid.core.engine.GameManager;
import com.ooparkanoid.utils.Constants;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Arkanoid");
        Group root = new Group();
        Canvas canvas = new Canvas(Constants.WIDTH, Constants.HEIGHT);
        root.getChildren().add(canvas);
        Scene scene = new Scene(root, Constants.WIDTH, Constants.HEIGHT);
        stage.setScene(scene);
        stage.show();

        GraphicsContext gc = canvas.getGraphicsContext2D();
        GameManager gameWorld = new GameManager();

        new AnimationTimer() {
            long last = 0;

            @Override
            public void handle(long now) {
                if (last == 0) {
                    last = now;
                    return;
                } else {
                    double dt = (now - last) / 1e9;
                    last = now;

                    // Update
                    gameWorld.update(dt);

                    // Render
                    gc.setFill(Color.BLACK);
                    gc.fillRect(0, 0, Constants.WIDTH, Constants.HEIGHT);
                    gameWorld.render(gc);

                    // Debug fps
                    gc.setFill(Color.YELLOW);
                    gc.fillText(String.format("FPS: %.1f", 1.0 / dt), 10, 20);
                }
            }
        }.start();
    }

    public static void main(String[] args) {
        launch();
    }
}
