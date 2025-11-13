package com.ooparkanoid.ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import com.ooparkanoid.core.engine.LocalBattleManager;
import com.ooparkanoid.core.state.GameMode;

/**
 * Displays live score counters for local battle mode with visual feedback.
 * Shows Player 1 and Player 2 life counters with color-coded styling and flash animations.
 * Automatically updates when lives change and provides visual feedback for damage taken.
 *
 * Features:
 * - Real-time life counter updates for both players
 * - Color-coded player labels (red for P1, blue for P2)
 * - Flash animation when player takes damage
 * - Automatic visibility management based on game mode
 * - Semi-transparent background with drop shadows for readability
 *
 * Visual Design:
 * - Large, bold Arial font for high visibility
 * - Rounded corner backgrounds with player-specific colors
 * - Flash animation (opacity pulse) on life loss
 * - Centered horizontal layout with spacer between players
 *
 * Usage:
 * Create instance, call bindTo() with LocalBattleManager and GameMode property.
 * Add getRoot() to scene graph. Counters update automatically.
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public class BattleScoreboard {

    /** Root container using HBox for horizontal layout */
    private final HBox root;

    /** Player 1 life counter label */
    private final Label p1;

    /** Player 2 life counter label */
    private final Label p2;

    /** Base opacity for normal display state */
    private static final double BASE_OPACITY = 0.75;

    /** Flash animation timeline for Player 1 */
    private Timeline p1Flash;

    /** Flash animation timeline for Player 2 */
    private Timeline p2Flash;

    /**
     * Constructs a BattleScoreboard with default styling and layout.
     * Creates player labels with initial "P1: 0" and "P2: 0" text.
     * Sets up horizontal layout with centered alignment and spacing.
     */
    public BattleScoreboard() {
        p1 = createCounterLabel("P1: 0", Color.web("#FF6F61"));
        p2 = createCounterLabel("P2: 0", Color.web("#3FA9F5"));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        root = new HBox(60, p1, spacer, p2);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(0, 0, 0, 0));
        root.setMouseTransparent(true);
    }

    /**
     * Gets the root HBox container for this scoreboard.
     *
     * @return the HBox containing the scoreboard layout
     */
    public HBox getRoot() {
        return root;
    }

    /**
     * Binds the scoreboard to a LocalBattleManager and GameMode property.
     * Sets up automatic visibility management and life counter updates.
     * Listeners will update counters and trigger flash animations on life loss.
     *
     * @param battleManager the LocalBattleManager providing life data
     * @param modeProp the GameMode property for visibility control
     */
    public void bindTo(LocalBattleManager battleManager, ObjectProperty<GameMode> modeProp) {
        BooleanBinding visible = modeProp.isEqualTo(GameMode.LOCAL_BATTLE);
        root.visibleProperty().bind(visible);
        root.managedProperty().bind(visible);

        // Set up listeners for counter updates and flash animations
        battleManager.playerOneLivesProperty().addListener((obs, oldVal, newVal) -> {
            updateCounter(p1, "P1", newVal);
            if (oldVal != null && newVal != null && newVal.intValue() < oldVal.intValue()) flash(p1, true);
            else p1.setOpacity(BASE_OPACITY);
        });
        battleManager.playerTwoLivesProperty().addListener((obs, oldVal, newVal) -> {
            updateCounter(p2, "P2", newVal);
            if (oldVal != null && newVal != null && newVal.intValue() < oldVal.intValue()) flash(p2, false);
            else p2.setOpacity(BASE_OPACITY);
        });

        // Initialize counters with current values
        resetCounters(battleManager);
    }

    /**
     * Resets the scoreboard counters to current battle manager values.
     * Updates both player labels and resets opacity to base level.
     *
     * @param battleManager the LocalBattleManager providing current life values
     */
    public void resetCounters(LocalBattleManager battleManager) {
        updateCounter(p1, "P1", battleManager.playerOneLivesProperty().get());
        updateCounter(p2, "P2", battleManager.playerTwoLivesProperty().get());
        p1.setOpacity(BASE_OPACITY);
        p2.setOpacity(BASE_OPACITY);
    }

    // ==================== Helper Methods ====================

    /**
     * Creates a styled counter label for a player.
     * Applies consistent styling with player-specific accent color.
     *
     * @param text initial text for the label
     * @param accent player-specific accent color for background
     * @return configured Label with styling applied
     */
    private Label createCounterLabel(String text, Color accent) {
        Label label = new Label(text);
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        label.setAlignment(Pos.CENTER);
        label.setPadding(new Insets(8, 28, 8, 28));
        label.setMinWidth(200);
        label.setBackground(new Background(new BackgroundFill(accent.deriveColor(0, 1, 1, 0.38), new CornerRadii(20), Insets.EMPTY)));
        label.setOpacity(BASE_OPACITY);
        label.setMouseTransparent(true);
        label.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.55), 18, 0.4, 0, 6);");
        return label;
    }

    /**
     * Updates a counter label with new life value.
     * Formats the text as "Player: lives" with non-negative clamping.
     *
     * @param lbl the label to update
     * @param who player identifier ("P1" or "P2")
     * @param lives new life count value
     */
    private void updateCounter(Label lbl, String who, Number lives) {
        int v = lives == null ? 0 : lives.intValue();
        lbl.setText(String.format("%s: %d", who, Math.max(0, v)));
    }

    /**
     * Triggers a flash animation on a label when player takes damage.
     * Creates a brief opacity pulse to draw attention to life loss.
     * Stops any existing flash animation for the same player.
     *
     * @param label the label to animate
     * @param isP1 true if this is Player 1's label, false for Player 2
     */
    private void flash(Label label, boolean isP1) {
        Timeline existing = isP1 ? p1Flash : p2Flash;
        if (existing != null) existing.stop();
        Timeline t = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(label.opacityProperty(), BASE_OPACITY)),
                new KeyFrame(Duration.millis(140), new KeyValue(label.opacityProperty(), 1.0)),
                new KeyFrame(Duration.millis(360), new KeyValue(label.opacityProperty(), BASE_OPACITY))
        );
        t.setOnFinished(ev -> label.setOpacity(BASE_OPACITY));
        t.play();
        if (isP1) p1Flash = t; else p2Flash = t;
    }
}
