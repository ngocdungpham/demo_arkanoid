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

/**
 * Controller for the game introduction screen.
 * Manages visual effects for the logo and start prompt during the intro sequence.
 * Implements JavaFX Initializable to set up animations when the FXML is loaded.
 *
 * Visual Effects:
 * - Logo: Pulsing glow with color transitions, fade-in animation, drop shadow
 * - Start Label: Blinking opacity animation to draw attention
 *
 * Animation Details:
 * - Logo glow pulses between cyan and purple every 1.2 seconds
 * - Logo fades in over 1.5 seconds on startup
 * - Start label blinks every 1.4 seconds (0.7s fade out, 0.7s fade in)
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public class IntroController implements Initializable {

    /** Logo image view displaying the game logo */
    @FXML
    private ImageView logo;

    /** Label displaying "PRESS SPACE TO START" prompt */
    @FXML
    private Label start;

    /**
     * Initializes the controller after FXML loading.
     * Sets up all visual effects for the logo and start label.
     * Called automatically by JavaFX when the FXML is loaded.
     *
     * @param url the location used to resolve relative paths for the root object
     * @param resourceBundle the resources used to localize the root object
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configure effects for Logo
        setupLogoEffects();

        // Configure effects for Label
        setupLabelEffects();
    }

    /**
     * Sets up glowing, drop shadow, breathing, and fade-in effects for the logo.
     * Creates a layered visual effect with pulsing glow and smooth entrance animation.
     */
    private void setupLogoEffects() {
        // Combine Glow and DropShadow effects
        Glow glow = new Glow(0.3);
        DropShadow shadow = new DropShadow(30, Color.CYAN);
        shadow.setInput(glow);
        logo.setEffect(shadow);

        // Breathing glow effect (pulsing)
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

        // Fade-in effect
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.5), logo);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    /**
     * Sets up blinking effect for the "PRESS SPACE TO START" label.
     * Creates attention-grabbing animation that alternates opacity.
     */
    private void setupLabelEffects() {
        Timeline blinker = new Timeline(
                new KeyFrame(Duration.seconds(0.7), new KeyValue(start.opacityProperty(), 0.0)),
                new KeyFrame(Duration.seconds(1.4), new KeyValue(start.opacityProperty(), 1.0)) // Slightly slower blink rate
        );
        blinker.setCycleCount(Animation.INDEFINITE);
        blinker.play();
    }
}
