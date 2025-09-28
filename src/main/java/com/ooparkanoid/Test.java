package com.ooparkanoid;

// Ứng dụng và cửa sổ
import javafx.application.Application; // launch, override
import javafx.stage.Stage; // setTitle, setScene, show scene
import javafx.scene.Scene;

/*
Layout
StackPane xếp chồng,
VBox xếp dọc getChildren setSpacing setPadding dùng cho manu thanh công cụ
 */

import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

// Canvas và vẽ 2D
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

// Animation
// start stop handle(now) gameloop 60 fps
import javafx.animation.AnimationTimer;

// Control UI
import javafx.scene.control.Label;
import javafx.scene.control.Button;

/*
Lable nhãn văn bản: setText, setFront, setTextFill
Button nút bấm: setOnAction Start, Restart, setDisable
 */

// Sự kiện
/*
ActionEvent: sự kiện hành động -> ấn button
EventHandle <T>: giao diện nút handle
 */
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

/*
Sử lý chuột: getX, getY. Đăng ký scene.setOnMouseMoved
ScrollEvent: Sự kiện cuộn menu
 */
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

import javafx.scene.Group;
import java.awt.*;
import javafx.geometry.*;


public class Test extends Application {

    Button button;
    Stage window;
    Scene scene1,  scene2, scene3;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
//        stage.setTitle("Arkanoid");
//        button = new Button();
//        button.setText("Click me");
//
//        button.setOnAction(e -> System.out.println("Clicked"));
//
//        StackPane layout = new StackPane();
//        layout.getChildren().add(button);
//
//        Scene scene = new Scene(layout, 300, 250);
//        stage.setScene(scene);
//        stage.show();
        window = stage;
        Label label1 = new Label("Welcome to Arkanoid!");

        Button button1 = new Button("Click Me!");
        button1.setOnAction(e -> window.setScene(scene2));

        VBox layout1 = new VBox(20);
        layout1.getChildren().addAll(label1, button1);
        layout1.setAlignment(Pos.CENTER);
        scene1 = new Scene(layout1, 200, 200);


        Button button2 = new Button("Click Me to forward AlertBox!");
        button2.setOnAction(e -> AlertBox.display("Setting", "Menu"));

        Button button3 = new Button("Click Me to backward Scene1!");
        button3.setOnAction(e -> window.setScene(scene1));

        VBox layout2 = new VBox(10); // spacing = 10px
        layout2.getChildren().addAll(button2, button3);
        layout2.setAlignment(Pos.CENTER);
//        StackPane.setAlignment(button3, Pos.TOP_CENTER);
//        StackPane.setAlignment(button2, Pos.BOTTOM_CENTER);

        scene2 = new Scene(layout2, 200, 200);
//        StackPane layout3 = new StackPane();
//        layout2.getChildren().addAll(button3);
//        scene3 = new Scene(layout2, 300, 300);

        window.setScene(scene1);
        window.setTitle("Arkanoid!");
        window.show();

    }

}
