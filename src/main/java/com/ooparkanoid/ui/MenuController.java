package com.ooparkanoid.ui;

import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;


import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import com.ooparkanoid.sound.SoundManager;

/**
 * Controller for the main menu UI in the Arkanoid game.
 * Manages a carousel of menu cards with sliding animations, hover effects, and selection handling.
 * Supports keyboard navigation (left/right arrows for sliding, enter for selection) and mouse interactions.
 * Integrates with SoundManager for audio feedback and allows external callback for menu selections.
 */
public class MenuController implements Initializable {

    // ---- Screen Constants ----
    /** Screen width in pixels. */
    static final int W = 1280;
    /** Screen height in pixels. */
    static final int H = 720;
    /** Y-position for the card alignment line. */
    static final double LINE_Y = H / 2.25;

    // ---- Card Constants ----
    /** Card width in pixels. */
    static final double CARD_W = 250;
    /** Card height in pixels. */
    static final double CARD_H = 350;
    /** Spacing between card centers in pixels. */
    static final double SPACING = 400;

    // ---- Animation Constants ----
    /** Duration for card sliding animations. */
    static final Duration SLIDE_MS = Duration.millis(360);
    /** Duration for hover animations. */
    static final Duration HOVER_MS = Duration.millis(120);
    /** Duration for center card flicker animation. */
    static final Duration FLICKER_MS = Duration.millis(400);

    // ---- Theme Colors (Neon Cyan) ----
    /** Base fill color for cards. */
    static final Color FILL_BASE = Color.web("#0A1E3ACC");
    /** Hover fill color for cards. */
    static final Color FILL_HOVER = Color.web("#0ff8ff66");
    /** Stroke color for cards. */
    static final Color STROKE = Color.web("#00FFFF");
    /** Base text color. */
    static final Color TEXT_BASE = Color.web("#EFFFFF");
    /** Pulsing text color for center card. */
    static final Color TEXT_PULSE = Color.web("#00FFFF");

    // ---- Background Image ----
    /** Path to the background image resource. */
    static final String BACKGROUND_IMAGE ="/picture/menu.jpg";

    // ---- Menu Data ----
    /** List of menu item titles. */
    final List<String> items = List.of("Adventure", "VERSUS", "CREDITS", "QUIT");

    // ---- Runtime State ----
    /** List of card UI elements. */
    final List<StackPane> cards = new ArrayList<>();
    /** Index of the currently centered card. */
    int cur = 0;
    /** Animation offset property for sliding (-1 to +1). */
    final DoubleProperty offset = new SimpleDoubleProperty(0);
    /** Timeline for center card flicker animation. */
    Timeline centerFlicker;

    // ---- FXML References ----
    /** Background image view. */
    @FXML private ImageView bg;
    /** Pane containing the cards. */
    @FXML private Pane cardsLayer;

    /** Callback for handling menu selection. */
    private Consumer<String> onSelectionCallback;

    /**
     * Sets the callback to be invoked when a menu item is selected.
     * @param callback Consumer accepting the selected menu item string.
     */
    public void setOnSelectionCallback(Consumer<String> callback) {
        this.onSelectionCallback = callback;
    }


    /**
     * Initializes the controller after FXML loading.
     * Sets up the background, preloads cards, configures layouts, and attaches event handlers.
     * @param location The location used to resolve relative paths.
     * @param resources The resources used to localize the root object.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configure the cards layer size and interaction
        cardsLayer.setPrefSize(W, H);
        cardsLayer.setPickOnBounds(false);

        // Load and configure background image if specified
        if (BACKGROUND_IMAGE != null) {
            var url = getClass().getResource(BACKGROUND_IMAGE);
            if (url == null) {
                throw new IllegalStateException("Background image not found: " + BACKGROUND_IMAGE);
            }
            bg.setImage(new Image(url.toExternalForm(), W, H, false, true));
            bg.setFitWidth(W);
            bg.setFitHeight(H);
            bg.setPreserveRatio(false);
            bg.setMouseTransparent(true);
            bg.setViewOrder(999);
        }

        // Create and add cards to the layer
        for (String title : items) {
            StackPane c = makeCard(title);
            attachMouse(c);
            c.setVisible(false);
            cards.add(c);
            cardsLayer.getChildren().add(c);
        }

        // Initial layout and listener for offset changes
        layoutCards(0);
        applyCenterHighlight();
        offset.addListener((o, ov, nv) -> layoutCards(nv.doubleValue()));

        // Set up keyboard handlers when scene is available
        cardsLayer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) setupKeyHandlers(newScene);
        });
    }

    /**
     * Configures keyboard event handlers for the scene.
     * @param scene The scene to attach handlers to.
     */
    private void setupKeyHandlers(Scene scene) {
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case LEFT  -> slide(-1);
                case RIGHT -> slide(+1);
                case ENTER -> onSelect();
            }
        });
    }

    // ---------- UI: Card Creation ----------
    /**
     * Creates a new card UI element with the given title.
     * @param title The text to display on the card.
     * @return A StackPane representing the card.
     */
    private StackPane makeCard(String title) {
        var r = new javafx.scene.shape.Rectangle(CARD_W, CARD_H);
        r.setArcWidth(30);
        r.setArcHeight(30);
        r.setFill(FILL_BASE);
        r.setStroke(STROKE);
        r.setStrokeWidth(2.0);

        Text t = new Text(title);
        t.setFill(TEXT_BASE);
        t.setFont(Font.font("Orbitron", 34)); // Fallback if font is unavailable

        StackPane sp = new StackPane(r, t);
        sp.setAlignment(Pos.CENTER);
        sp.setPrefSize(CARD_W, CARD_H);
        sp.setCache(true);
        sp.setCacheHint(CacheHint.SPEED);

        DropShadow glow = new DropShadow(22, STROKE);
        glow.setSpread(0.18);
        sp.setEffect(glow);

        return sp;
    }

    /**
     * Attaches mouse event handlers to a card for hover and click interactions.
     * @param card The card to attach handlers to.
     */
    private void attachMouse(StackPane card) {
        card.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            if (offset.get() != 0) return;
            card.setCursor(Cursor.HAND);
            bumpCard(card, 1.08, 3.0, FILL_HOVER, TEXT_BASE);
        });
        card.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            card.setCursor(Cursor.DEFAULT);
            var bg = (javafx.scene.shape.Rectangle) card.getChildren().get(0);
            var tx = (Text) card.getChildren().get(1);
            animateTo(bg.fillProperty(), (Color) bg.getFill(), FILL_BASE, HOVER_MS);
            animateTo(tx.fillProperty(), (Color) tx.getFill(), TEXT_BASE, HOVER_MS);
            layoutCards(offset.get());
        });
        card.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (offset.get() != 0) return;
            int slot = getSlot(card); // -1 (left), 0 (center), +1 (right)
            if (slot == 0) onSelect();
            else slide(slot);
        });
    }

    // ---------- Layout Management ----------
    /**
     * Updates the layout and appearance of all cards based on the current offset.
     * Handles positioning, scaling, opacity, and visual states.
     * @param off The animation offset (-1 to +1).
     */
    private void layoutCards(double off) {
        double cx = W / 2.0;

        for (int i = 0; i < cards.size(); i++) {
            StackPane card = cards.get(i);

            // Calculate relative position with wrapping
            double rel = wrapRelative(i - cur - off, cards.size());

            if (Math.abs(rel) > 1.25) {
                card.setVisible(false);
                continue;
            }
            card.setVisible(true);

            double x = cx + rel * SPACING;
            double scale = 0.85 + 0.25 * Math.max(0, 1 - Math.abs(rel));
            double opacity = 0.60 + 0.40 * Math.max(0, 1 - Math.abs(rel));

            card.setLayoutX(x - CARD_W / 2.0);
            card.setLayoutY(LINE_Y - CARD_H / 2.0);
            card.setScaleX(scale);
            card.setScaleY(scale);
            card.setOpacity(opacity);

            var bg = (javafx.scene.shape.Rectangle) card.getChildren().get(0);
            bg.setStrokeWidth(scale > 1.05 ? 3.0 : 2.0);

            // Adjust z-order: center card on top
            card.setViewOrder(2 - scale);

            // Reset non-center cards to base state
            if (Math.abs(rel) > 0.25) {
                if (!bg.getFill().equals(FILL_BASE)) bg.setFill(FILL_BASE);
                var tx = (Text) card.getChildren().get(1);
                if (!tx.getFill().equals(TEXT_BASE)) tx.setFill(TEXT_BASE);
            }
        }
    }

    /**
     * Applies a flickering highlight animation to the center card.
     */
    private void applyCenterHighlight() {
        if (centerFlicker != null) {
            centerFlicker.stop();
            centerFlicker = null;
        }
        StackPane center = cards.get(cur);
        if (!center.isVisible()) return;

        var bg = (javafx.scene.shape.Rectangle) center.getChildren().get(0);
        var tx = (Text) center.getChildren().get(1);

        centerFlicker = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(tx.fillProperty(), TEXT_BASE),
                        new KeyValue(bg.strokeWidthProperty(), 3.2)
                ),
                new KeyFrame(FLICKER_MS.divide(2),
                        new KeyValue(tx.fillProperty(), TEXT_PULSE),
                        new KeyValue(bg.strokeWidthProperty(), 3.6)
                ),
                new KeyFrame(FLICKER_MS,
                        new KeyValue(tx.fillProperty(), TEXT_BASE),
                        new KeyValue(bg.strokeWidthProperty(), 3.2)
                )
        );
        centerFlicker.setCycleCount(Animation.INDEFINITE);
        centerFlicker.setAutoReverse(true);
        centerFlicker.play();
    }

    // ---------- Navigation ----------
    /**
     * Animates a slide to the next or previous card.
     * @param dir Direction: -1 for left, +1 for right.
     */
    private void slide(int dir) {
        if (offset.get() != 0) return;

        SoundManager.getInstance().play("card_transition");
        Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(offset, 0)),
                new KeyFrame(SLIDE_MS, new KeyValue(offset, dir, Interpolator.EASE_BOTH))
        );
        tl.setOnFinished(e -> {
            cur = (cur + dir + items.size()) % items.size(); // Wrap around
            offset.set(0);
            layoutCards(0);
            applyCenterHighlight();
        });
        tl.play();
    }

    /**
     * Handles selection of the current center card.
     * Plays sound and invokes the callback if set.
     */
    private void onSelect() {
        SoundManager.getInstance().play("selected");
       // System.out.println("Selected: " + items.get(cur));
        // TODO: chuyển scene/game state tại đây
        String selection = items.get(cur);
        System.out.println("Selected: " + selection);
        if (onSelectionCallback != null) {
            onSelectionCallback.accept(selection);
        }
    }

    // ---------- Utility Methods ----------
    /**
     * Determines the slot position of a card relative to center.
     * @param card The card to check.
     * @return -1 (left), 0 (center), +1 (right), or 99 if invisible.
     */
    private int getSlot(StackPane card) {
        if (!card.isVisible()) return 99;
        double centerX = card.getLayoutX() + CARD_W / 2.0;
        double dx = centerX - W / 2.0;
        if (Math.abs(dx) < SPACING * 0.25) return 0;
        return (dx < 0) ? -1 : +1;
    }

    /**
     * Animates a bump effect on a card for hover.
     * @param card The card to animate.
     * @param scaleBoost Scale multiplier.
     * @param strokeW New stroke width.
     * @param fillTo Target fill color.
     * @param textTo Target text color.
     */
    private void bumpCard(StackPane card, double scaleBoost, double strokeW, Color fillTo, Color textTo) {
        var bg = (javafx.scene.shape.Rectangle) card.getChildren().get(0);
        var tx = (Text) card.getChildren().get(1);
        double sx = card.getScaleX(), sy = card.getScaleY();

        Timeline t = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(card.scaleXProperty(), sx),
                        new KeyValue(card.scaleYProperty(), sy),
                        new KeyValue(bg.strokeWidthProperty(), bg.getStrokeWidth()),
                        new KeyValue(bg.fillProperty(), bg.getFill()),
                        new KeyValue(tx.fillProperty(), tx.getFill())
                ),
                new KeyFrame(HOVER_MS,
                        new KeyValue(card.scaleXProperty(), sx * scaleBoost, Interpolator.EASE_OUT),
                        new KeyValue(card.scaleYProperty(), sy * scaleBoost, Interpolator.EASE_OUT),
                        new KeyValue(bg.strokeWidthProperty(), strokeW, Interpolator.EASE_OUT),
                        new KeyValue(bg.fillProperty(), fillTo, Interpolator.EASE_OUT),
                        new KeyValue(tx.fillProperty(), textTo, Interpolator.EASE_OUT)
                )
        );
        t.play();
    }

    /**
     * Wraps a relative index to the nearest equivalent within [-n/2, n/2].
     * @param rel The relative value.
     * @param n The total number of items.
     * @return The wrapped relative value.
     */
    private static double wrapRelative(double rel, int n) {
        while (rel <= -n / 2.0) rel += n;
        while (rel > n / 2.0) rel -= n;
        return rel;
    }

    /**
     * Animates a property from one value to another over a duration.
     * @param <T> The property type.
     * @param prop The property to animate.
     * @param from Starting value.
     * @param to Ending value.
     * @param d Animation duration.
     */
    private static <T> void animateTo(javafx.beans.property.Property<T> prop, T from, T to, Duration d) {
        Timeline t = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(prop, from)),
                new KeyFrame(d, new KeyValue(prop, to, Interpolator.EASE_BOTH))
        );
        t.play();
    }

}
