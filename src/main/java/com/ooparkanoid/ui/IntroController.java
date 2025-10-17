package com.ooparkanoid.ui;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class IntroController implements Initializable {

    @FXML
    private ImageView logo;

    @FXML
    private Label start;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // --- Cấu hình hiệu ứng cho Logo ---
        setupLogoEffects();

        // --- Cấu hình hiệu ứng cho Label ---
        setupLabelEffects();
    }

    /**
     * Thiết lập hiệu ứng phát sáng, đổ bóng, nhịp thở và fade-in cho logo.
     */
    private void setupLogoEffects() {
        // --- Kết hợp hiệu ứng Glow và DropShadow ---
        Glow glow = new Glow(0.3);
        DropShadow shadow = new DropShadow(30, Color.CYAN);
        shadow.setInput(glow);
        logo.setEffect(shadow);

        // --- Hiệu ứng phát sáng "nhịp thở" (pulsing) ---
        Timeline pulse = new Timeline(
                new KeyFrame(Duration.seconds(0),
                        new KeyValue(glow.levelProperty(), 0.3),
                        new KeyValue(shadow.colorProperty(), Color.CYAN)
                ),
                new KeyFrame(Duration.seconds(1.2),
                        new KeyValue(glow.levelProperty(), 1.0),
                        new KeyValue(shadow.colorProperty(), Color.MEDIUMPURPLE)
                )
        );
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();

        // --- Hiệu ứng xuất hiện mờ dần (Fade-in) ---
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.5), logo);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    /**
     * Thiết lập hiệu ứng nhấp nháy cho label "PRESS SPACE TO START".
     */
    private void setupLabelEffects() {
        Timeline blinker = new Timeline(
                new KeyFrame(Duration.seconds(0.7), new KeyValue(start.opacityProperty(), 0.0)),
                new KeyFrame(Duration.seconds(1.4), new KeyValue(start.opacityProperty(), 1.0)) // Tăng thời gian để nhịp nháy chậm hơn một chút
        );
        blinker.setCycleCount(Animation.INDEFINITE);
        blinker.play();
    }
}

