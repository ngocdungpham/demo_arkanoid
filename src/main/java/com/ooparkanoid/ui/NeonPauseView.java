// File: com.ooparkanoid.ui.NeonPauseView.java
package com.ooparkanoid.ui;

import com.ooparkanoid.sound.SoundManager;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * Interactive pause menu overlay with neon visual effects and keyboard/mouse navigation.
 * Displays a framed pause screen with resume/exit options, featuring animated selector,
 * hover effects, and smooth transitions. Designed for seamless game interruption.
 *
 * Features:
 * - Framed overlay with custom background image (480x234)
 * - Animated neon selector that follows keyboard/mouse input
 * - Scale animations on menu item selection
 * - Keyboard navigation (WASD/Arrow keys, Enter/Space to select, ESC to resume)
 * - Mouse hover and click support
 * - Fade-in/out transitions for smooth appearance
 * - Sound effects for user interactions
 * - Semi-transparent background overlay
 *
 * Visual Design:
 * - Neon cyan color scheme (#3BE0FF)
 * - Segoe UI font family with bold weights
 * - Drop shadow effects for depth
 * - Rounded selector rectangle with glow
 * - Scale animations for selected items
 *
 * Navigation:
 * - UP/W: Move selector up
 * - DOWN/S: Move selector down
 * - ENTER/SPACE/Z: Select current item
 * - ESC/X/BACKSPACE: Resume game
 * - Mouse hover: Move selector to hovered item
 * - Mouse click: Select hovered item
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public class NeonPauseView {

    /** Frame width in pixels */
    private static final double FRAME_W = 480;

    /** Frame height in pixels */
    private static final double FRAME_H = 234;

    /** Path to the frame background image */
    private static final String FRAME_IMAGE =
            NeonPauseView.class.getResource("/picture/frame_pause1.png").toExternalForm();

    /** Primary neon color for UI elements */
    private static final Color NEON = Color.web("#3BE0FF");

    /** Font family for UI text */
    private static final String FONT_FAMILY = "Segoe UI, Roboto, Arial";

    /** Horizontal padding for menu items */
    private static final double SIDE_PADDING = 50;

    /** Vertical spacing between menu items */
    private static final double ITEM_SPACING = 20;

    /** Height of the selector highlight rectangle */
    private static final double HIGHLIGHT_HEIGHT = 44;

    /** Resume game button */
    private Button resumeBtn;

    /** Exit game button */
    private Button exitBtn;

    /** Animated selector rectangle */
    private Rectangle selector;

    /** Array of selectable menu items */
    private Region[] items;

    /** Current selected item index */
    private int index = 0;

    /** Root overlay container */
    private final StackPane root;

    /**
     * Callback interface for handling pause menu actions.
     * Implement these methods to respond to user selections.
     */
    public interface Callbacks {
        /** Called when user selects resume game */
        void onResume();

        /** Called when user selects exit game */
        void onExit();
    }

    /**
     * Constructs a NeonPauseView with the specified callback handler.
     * Initializes all UI components, animations, and input handlers.
     * Sets up the framed pause menu with interactive elements.
     *
     * @param cb callback handler for menu actions
     */
    public NeonPauseView(Callbacks cb) {
        // Root overlay covering entire screen
        root = new StackPane();
        root.setStyle("-fx-background-color: rgba(0,0,0,0.86);");
        root.setPickOnBounds(true);   // Block clicks from passing through
        root.setMouseTransparent(false);
        root.setVisible(false);

        // Frame 480x234
        ImageView frame = new ImageView(new Image(FRAME_IMAGE));
        frame.setFitWidth(FRAME_W);
        frame.setFitHeight(FRAME_H);
        frame.setPreserveRatio(false);
        frame.setMouseTransparent(true);

        // Title
        Label title = new Label("Pause");
        title.setFont(Font.font(FONT_FAMILY, FontWeight.EXTRA_BOLD, 30));
        title.setTextFill(Color.WHITE);
        title.setEffect(shadow(0.9));
        StackPane.setAlignment(title, Pos.TOP_CENTER);
        StackPane.setMargin(title, new Insets(4, 0, 0, 0));

        // Buttons
        resumeBtn = neonButton("Resume Game");
        exitBtn   = neonButton("Exit Game");
        resumeBtn.setOnAction(e -> {
            SoundManager.getInstance().play("selected");
            cb.onResume();
        });
        exitBtn.setOnAction(e -> {
            SoundManager.getInstance().play("selected");
            cb.onExit();
        });

        VBox menu = new VBox(ITEM_SPACING, resumeBtn, exitBtn);
        menu.setAlignment(Pos.CENTER);
        menu.setPadding(new Insets(0, SIDE_PADDING, 0, SIDE_PADDING));
        menu.setMaxWidth(FRAME_W);
        menu.setMouseTransparent(false);

        // Neon selector
        selector = new Rectangle();
        selector.setWidth(FRAME_W - SIDE_PADDING * 2);
        selector.setHeight(HIGHLIGHT_HEIGHT);
        selector.setArcWidth(10);
        selector.setArcHeight(10);
        selector.setStroke(NEON);
        selector.setStrokeWidth(2.2);
        selector.setFill(Color.color(0, 0.12, 0.20, 0.28));
        selector.setEffect(glow(NEON, 28));
        selector.setMouseTransparent(true);

        StackPane selectorLayer = new StackPane(selector);
        selectorLayer.setPickOnBounds(false);
        selectorLayer.setMouseTransparent(true);

        // Content within frame
        StackPane content = new StackPane(selectorLayer, menu);
        content.setMaxSize(FRAME_W, FRAME_H);

        // Board: Fixed 480x234 to prevent stretching to full screen
        StackPane board = new StackPane(frame, title, content);
        board.setPrefSize(FRAME_W, FRAME_H);
        board.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        board.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        StackPane.setAlignment(board, Pos.CENTER);

        // Root contains only board
        root.getChildren().setAll(board);
        StackPane.setAlignment(board, Pos.CENTER);

        // Items & initial selection visuals
        items = new Region[]{resumeBtn, exitBtn};
        wireHoverHandlers();          // Ensure selector follows mouse
        applySelectionVisuals();
        root.sceneProperty().addListener((obs, o, sc) -> {
            if (sc != null) relocateSelector(items[index], false);
        });

        // Keyboard navigation when overlay is visible
        root.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case DOWN, S -> move(1);
                case UP, W -> move(-1);
                case ENTER, SPACE, Z -> {
                    if (index == 0) cb.onResume();
                    else          cb.onExit();
                    e.consume(); // Prevent event propagation
                }
                case ESCAPE, X, BACK_SPACE -> cb.onResume();
            }
        });
    }

    /**
     * Gets the root node for this pause view.
     * Add this to your scene graph to display the pause menu.
     *
     * @return the root StackPane containing all pause UI elements
     */
    public Node getView() {
        return root;
    }

    /**
     * Shows the pause menu overlay on the specified game root.
     * Adds the overlay to the scene if not already present.
     * Animates fade-in and focuses for keyboard input.
     *
     * @param gameRoot the root pane of the game scene
     */
    public void show(StackPane gameRoot) {
        if (!gameRoot.getChildren().contains(root)) {
            gameRoot.getChildren().add(root);
        }
        root.setVisible(true);
        root.requestFocus();
        relocateSelector(items[index], false);
        root.setOpacity(0);
        new Timeline(new KeyFrame(Duration.millis(160),
                new KeyValue(root.opacityProperty(), 1, Interpolator.EASE_BOTH))).play();
    }

    /**
     * Hides the pause menu with fade-out animation.
     * Sets visibility to false after animation completes.
     */
    public void hide() {
        Timeline tl = new Timeline(new KeyFrame(Duration.millis(120),
                new KeyValue(root.opacityProperty(), 0, Interpolator.EASE_BOTH)));
        tl.setOnFinished(ev -> root.setVisible(false));
        tl.play();
    }

    // ================== Helper Methods ==================

    /**
     * Creates a styled neon button for the pause menu.
     * Applies consistent styling with transparent background and drop shadow.
     *
     * @param text the button text to display
     * @return configured Button with neon styling
     */
    private Button neonButton(String text) {
        Button b = new Button(text);
        b.setBackground(Background.EMPTY);
        b.setBorder(Border.EMPTY);
        b.setPadding(new Insets(8, 14, 8, 14));
        b.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 20));
        b.setTextFill(Color.WHITE);
        b.setEffect(shadow(0.75));
        b.setFocusTraversable(false);
        return b;
    }

    /**
     * Wires mouse hover handlers for all menu items.
     * Ensures selector follows mouse movement and handles click selection.
     */
    private void wireHoverHandlers() {
        for (int i = 0; i < items.length; i++) {
            final int idx = i;
            Region r = items[i];
            r.setPickOnBounds(true); // Stable hitbox when scaled
            r.setOnMouseEntered(e -> setIndex(idx));
            r.setOnMouseMoved(e -> { if (index != idx) setIndex(idx); });
            r.setOnMousePressed(e -> {
                if (e.isPrimaryButtonDown()) {
                    if (idx == 0) ((Button) items[0]).fire();
                    else          ((Button) items[1]).fire();
                }
            });
        }
    }

    /**
     * Moves the selector by the specified delta.
     * Wraps around menu items for continuous navigation.
     *
     * @param delta the number of positions to move (positive = down, negative = up)
     */
    private void move(int delta) {
        int next = (index + delta + items.length) % items.length;
        setIndex(next);
    }

    /**
     * Sets the currently selected menu item index.
     * Updates visual effects and plays transition sound.
     *
     * @param newIndex the new selection index
     */
    private void setIndex(int newIndex) {
        if (newIndex == index) return;
        SoundManager.getInstance().play("card_transition");
        index = newIndex;
        applySelectionVisuals();
        relocateSelector(items[index], true);
    }

    /**
     * Applies visual effects for the current selection state.
     * Scales selected item and adjusts glow effects.
     */
    private void applySelectionVisuals() {
        for (int i = 0; i < items.length; i++) {
            Region r = items[i];
            boolean selected = (i == index);
            double targetScale = selected ? 1.5 : 1.0;
            double targetGlow  = selected ? 1.15 : 0.8;

            Timeline t = new Timeline(
                    new KeyFrame(Duration.millis(150),
                            new KeyValue(r.scaleXProperty(), targetScale, Interpolator.EASE_BOTH),
                            new KeyValue(r.scaleYProperty(), targetScale, Interpolator.EASE_BOTH)
                    )
            );
            t.play();
            r.setEffect(shadow(targetGlow));
        }
    }

    /**
     * Relocates the selector rectangle to align with the target region.
     * Centers the selector on the target item within its parent container.
     *
     * @param target the region to align selector with
     * @param animate true to animate the movement, false for instant positioning
     */
    private void relocateSelector(Region target, boolean animate) {
        if (selector.getParent() == null) return;

        Scene sc = target.getScene();
        if (sc == null) return;

        var tb = target.localToScene(target.getBoundsInLocal());
        double targetCenterY = (tb.getMinY() + tb.getMaxY()) / 2.0;

        var parent = (Region) selector.getParent();
        var pb = parent.localToScene(parent.getBoundsInLocal());
        double parentCenterY = (pb.getMinY() + pb.getMaxY()) / 2.0;

        double offsetY = targetCenterY - parentCenterY;

        if (!animate) {
            selector.setTranslateY(offsetY);
            return;
        }
        Timeline tl = new Timeline(
                new KeyFrame(Duration.millis(210),
                        new KeyValue(selector.translateYProperty(), offsetY, Interpolator.EASE_BOTH)
                )
        );
        tl.play();
    }

    /**
     * Creates a drop shadow effect with specified strength.
     * Used for text and button styling throughout the pause menu.
     *
     * @param strength the shadow strength multiplier (0.0 to 1.0)
     * @return configured DropShadow effect
     */
    private static DropShadow shadow(double strength) {
        DropShadow ds = new DropShadow();
        ds.setRadius(12);
        ds.setOffsetX(0);
        ds.setOffsetY(0);
        ds.setColor(Color.color(0, 0, 0, 0.9));
        ds.setSpread(0.15 * strength);
        return ds;
    }

    /**
     * Creates a glow effect with specified color and radius.
     * Combines DropShadow and Glow effects for neon appearance.
     *
     * @param c the glow color
     * @param radius the glow radius
     * @return configured Glow effect
     */
    private static javafx.scene.effect.Glow glow(Color c, double radius) {
        javafx.scene.effect.DropShadow outer =
                new javafx.scene.effect.DropShadow(radius, c.deriveColor(0, 1, 1, 0.85));
        outer.setSpread(0.45);
        return new javafx.scene.effect.Glow(0.25) {{ setInput(outer); }};
    }
}
