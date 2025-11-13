package com.ooparkanoid.ui;

import com.ooparkanoid.utils.Constants;
import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * Displays a victory screen for versus mode showing which player won.
 * Shows two halves sliding in from left and right to form a central band with the winner announcement,
 * then automatically returns to menu after 2 seconds. Similar to GameOverView but for player victories.
 *
 * Animation Sequence:
 * 1. Two colored bands slide in from left/right to meet in center (0.5s)
 * 2. "PLAYER X WINS!" text fades in (0.3s)
 * 3. Wait for 2 seconds to display
 * 4. Text fades out (0.3s) while bands slide out (0.5s)
 * 5. Automatically trigger exit callback
 *
 * Features:
 * - Smooth sliding animation with easing
 * - Color-coded for player (gold for winners)
 * - Large, stylized victory text with drop shadow
 * - Automatic timeout with callback execution
 * - Non-blocking animation with proper cleanup
 *
 * Visual Design:
 * - Gold semi-transparent band (height: 120px)
 * - White Orbitron font with gold drop shadow
 * - Transparent overlay (doesn't block other UI)
 * - Centered horizontal layout
 *
 * Usage:
 * Create with Callbacks implementation, call show(playerNumber) to display.
 * Animation runs automatically and calls onExit() when complete.
 *
 * @author Arkanoid Team
 * @version 1.0
 */
public class PlayerWinView {

    /** Root overlay container (transparent background) */
    private final StackPane root;

    /** Container limiting height for the horizontal band */
    private final Pane container;

    /** Left half of the sliding band */
    private final Pane leftHalf;

    /** Right half of the sliding band */
    private final Pane rightHalf;

    /** Victory text label */
    private final Label title;

    /** Callback interface for exit handling */
    private final Callbacks callbacks;

    /** Currently playing animation (for cleanup) */
    private Animation currentAnimation;

    // ==================== Band Configuration ====================
    /** Height of the victory band */
    private static final double BAND_HEIGHT = 120.0;

    /** Width of each sliding half */
    private static final double HALF_WIDTH = Constants.WIDTH / 2.0;

    /** Background color for the band (gold, semi-transparent) */
    private static final Color BAND_COLOR = Color.web("#FFD700").deriveColor(0, 1, 1, 0.5);

    /**
     * Callback interface for handling victory screen completion.
     * Implement onExit() to handle navigation back to menu.
     */
    public interface Callbacks {
        /** Called when the victory animation completes and screen should exit */
        void onExit();
    }

    /**
     * Constructs a PlayerWinView with the specified callback handler.
     * Initializes all UI components with proper positioning and styling.
     * Sets up the sliding band animation components.
     *
     * @param cb callback handler for exit events
     */
    public PlayerWinView(Callbacks cb) {
        this.callbacks = cb;

        // Root overlay: COMPLETELY TRANSPARENT
        root = new StackPane();
        root.setPrefSize(Constants.WIDTH, Constants.HEIGHT);
        root.setVisible(false);
        root.setMouseTransparent(true);
        root.setStyle("-fx-background-color: transparent;");

        // Container: Centered, height-limited, contains sliding halves
        container = new Pane();
        container.setPrefSize(Constants.WIDTH, BAND_HEIGHT);
        container.setMaxSize(Constants.WIDTH, BAND_HEIGHT);
        // Clip necessary to hide parts moved by TranslateX
        container.setClip(new javafx.scene.shape.Rectangle(Constants.WIDTH, BAND_HEIGHT));

        // Title label
        title = new Label("");
        title.setFont(Font.font("Orbitron", FontWeight.EXTRA_BOLD, 80));
        title.setTextFill(Color.WHITE);
        title.setEffect(new DropShadow(30, Color.web("#FFD700")));
        title.setOpacity(0.0);
        title.setStyle("-fx-font-family: 'Orbitron', 'Segoe UI', Arial;");
        title.setViewOrder(-1);
        StackPane.setAlignment(title, Pos.CENTER);

        // Left Half Pane (first piece)
        leftHalf = createHalfPane(BAND_COLOR, HALF_WIDTH, BAND_HEIGHT);
        leftHalf.setTranslateX(-HALF_WIDTH); // Start outside left
        leftHalf.setLayoutX(0); // IMPORTANT: Initial position is 0

        // Right Half Pane (second piece)
        rightHalf = createHalfPane(BAND_COLOR, HALF_WIDTH, BAND_HEIGHT);
        rightHalf.setTranslateX(HALF_WIDTH); // Start outside right
        rightHalf.setLayoutX(HALF_WIDTH); // IMPORTANT: Initial position is HALF_WIDTH

        // Add pieces to Container
        container.getChildren().addAll(leftHalf, rightHalf);

        // Add Container and Title to Root
        root.getChildren().addAll(container, title);
        StackPane.setAlignment(container, Pos.CENTER);
    }

    /**
     * Creates a pane with specified dimensions and background color.
     * Used for creating the sliding band halves.
     *
     * @param color background color for the pane
     * @param width width of the pane
     * @param height height of the pane
     * @return configured Pane with styling applied
     */
    private Pane createHalfPane(Color color, double width, double height) {
        Pane pane = new Pane();
        pane.setPrefSize(width, height);
        pane.setMaxSize(width, height);
        pane.setStyle("-fx-background-color: " + color.toString().replace("0x", "#") + ";");
        return pane;
    }

    /**
     * Shows the victory screen with sliding animation.
     * If already visible, does nothing. Stops any current animation.
     * Animation sequence: slide in → show text → wait → slide out → exit.
     *
     * @param playerNumber the number of the winning player (1 or 2)
     */
    public void show(int playerNumber) {
        if (root.isVisible()) return;

        if (currentAnimation != null) currentAnimation.stop();

        // Set victory text
        title.setText("PLAYER " + playerNumber + " WINS!");

        // Reset to initial state
        root.setVisible(true);
        // Set initial TranslateX positions
        leftHalf.setTranslateX(-HALF_WIDTH);
        rightHalf.setTranslateX(HALF_WIDTH);
        title.setOpacity(0.0);

        // Phase 1: Slide In (0.5s)
        Duration slideInDuration = Duration.millis(500);

        Timeline slideIn = new Timeline(
                // Move leftHalf from -HALF_WIDTH to 0
                new KeyFrame(slideInDuration,
                        new KeyValue(leftHalf.translateXProperty(), 0, Interpolator.EASE_BOTH)),
                // Move rightHalf from HALF_WIDTH to 0
                new KeyFrame(slideInDuration,
                        new KeyValue(rightHalf.translateXProperty(), 0, Interpolator.EASE_BOTH))
        );

        // Phase 2: Show Text, Wait, Slide Out & Exit
        slideIn.setOnFinished(e -> {
            // Show Text (0.3s)
            FadeTransition fadeInText = new FadeTransition(Duration.millis(300), title);
            fadeInText.setToValue(1.0);

            // Wait (2.0s)
            PauseTransition delay = new PauseTransition(Duration.seconds(2.0));
            delay.setOnFinished(ev -> hideAndExit());

            SequentialTransition transition = new SequentialTransition(fadeInText, delay);
            transition.play();
        });

        currentAnimation = slideIn;
        currentAnimation.play();
    }

    /**
     * Hides the victory screen with reverse sliding animation and triggers exit.
     * Called automatically after the display timeout.
     * Animation sequence: fade out text → slide out bands → hide → call callback.
     */
    private void hideAndExit() {
        // Fade out text first (0.3s)
        FadeTransition fadeOutText = new FadeTransition(Duration.millis(300), title);
        fadeOutText.setToValue(0.0);

        // Phase 3: Slide Out (0.5s)
        Duration slideOutDuration = Duration.millis(500);

        Timeline slideOut = new Timeline(
                // Move leftHalf from 0 to -HALF_WIDTH (left)
                new KeyFrame(slideOutDuration,
                        new KeyValue(leftHalf.translateXProperty(), -HALF_WIDTH, Interpolator.EASE_BOTH)),
                // Move rightHalf from 0 to HALF_WIDTH (right)
                new KeyFrame(slideOutDuration,
                        new KeyValue(rightHalf.translateXProperty(), HALF_WIDTH, Interpolator.EASE_BOTH))
        );

        slideOut.setOnFinished(e -> {
            root.setVisible(false);
            if (callbacks != null) {
                callbacks.onExit();
            }
        });

        // Play sequentially: fadeOutText → slideOut
        SequentialTransition transition = new SequentialTransition(fadeOutText, slideOut);
        currentAnimation = transition;
        currentAnimation.play();
    }

    /**
     * Gets the root node for this victory view.
     * Add this to your scene graph to display the victory screen.
     *
     * @return the root StackPane containing all victory UI elements
     */
    public Node getView() {
        return root;
    }
}

