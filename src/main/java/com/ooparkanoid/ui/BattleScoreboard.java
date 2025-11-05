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

public class BattleScoreboard {

    private final HBox root;
    private final Label p1;
    private final Label p2;

    private static final double BASE_OPACITY = 0.75;
    private Timeline p1Flash;
    private Timeline p2Flash;

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

    public HBox getRoot() { return root; }

    public void bindTo(LocalBattleManager battleManager, ObjectProperty<GameMode> modeProp) {
        BooleanBinding visible = modeProp.isEqualTo(GameMode.LOCAL_BATTLE);
        root.visibleProperty().bind(visible);
        root.managedProperty().bind(visible);

        // Listeners cập nhật counter + flash
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

        // init
        resetCounters(battleManager);
    }

    public void resetCounters(LocalBattleManager battleManager) {
        updateCounter(p1, "P1", battleManager.playerOneLivesProperty().get());
        updateCounter(p2, "P2", battleManager.playerTwoLivesProperty().get());
        p1.setOpacity(BASE_OPACITY);
        p2.setOpacity(BASE_OPACITY);
    }

    // ===== helpers =====
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

    private void updateCounter(Label lbl, String who, Number lives) {
        int v = lives == null ? 0 : lives.intValue();
        lbl.setText(String.format("%s: %d", who, Math.max(0, v)));
    }

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
