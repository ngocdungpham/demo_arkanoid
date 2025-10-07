package com.ooparkanoid.ui;


import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

    public class MenuScene extends Scene {
        public MenuScene(Stage stage, Runnable onStartGame) {
            super(new VBox(20), 800, 600);
            VBox layout = (VBox) getRoot();
            layout.setAlignment(Pos.CENTER);
            layout.setStyle("-fx-background-color: #2c3e50;"); // Màu nền tối

            // Tiêu đề
            Label title = new Label("ARKANOID PLUS");
            title.setFont(Font.font("Arial", FontWeight.BOLD, 48));
            title.setTextFill(Color.WHITE);

            // Các nút bấm
            Button startButton = createButton("Start Game");
            Button exitButton = createButton("Exit");

            // Xử lý sự kiện nút bấm
            startButton.setOnAction(e -> onStartGame.run());
            exitButton.setOnAction(e -> stage.close());

            layout.getChildren().addAll(title, startButton, exitButton);
        }

        private Button createButton(String text) {
            Button button = new Button(text);
            button.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
            button.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-border-color: #2980b9; -fx-border-width: 2px;");
            button.setPrefSize(200, 50);
            return button;
        }

}
