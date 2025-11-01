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

public class NeonPauseView {

    // --- Kích thước khung ---
    private static final double FRAME_W = 480;
    private static final double FRAME_H = 234;

    // --- Ảnh khung ---
    private static final String FRAME_IMAGE =
            NeonPauseView.class.getResource("/picture/frame_pause1.png").toExternalForm();

    // --- Style ---
    private static final Color NEON = Color.web("#3BE0FF");
    private static final String FONT_FAMILY = "Segoe UI, Roboto, Arial";

    private static final double SIDE_PADDING = 50;
    private static final double ITEM_SPACING = 20;
    private static final double HIGHLIGHT_HEIGHT = 44;

    private Button resumeBtn, exitBtn;
    private Rectangle selector;
    private Region[] items;
    private int index = 0;

    private final StackPane root;

    public interface Callbacks {
        void onResume();
        void onExit();
    }

    public NeonPauseView(Callbacks cb) {
        // --- Root overlay che toàn màn ---
        root = new StackPane();
        root.setStyle("-fx-background-color: rgba(0,0,0,0.86);");
        root.setPickOnBounds(true);   // chặn click xuyên xuống game
        root.setMouseTransparent(false);
        root.setVisible(false);

        // --- Frame 480x234 ---
        ImageView frame = new ImageView(new Image(FRAME_IMAGE));
        frame.setFitWidth(FRAME_W);
        frame.setFitHeight(FRAME_H);
        frame.setPreserveRatio(false);
        frame.setMouseTransparent(true);

        // --- Title ---
        Label title = new Label("Pause");
        title.setFont(Font.font(FONT_FAMILY, FontWeight.EXTRA_BOLD, 30));
        title.setTextFill(Color.WHITE);
        title.setEffect(shadow(0.9));
        StackPane.setAlignment(title, Pos.TOP_CENTER);
        StackPane.setMargin(title, new Insets(4, 0, 0, 0));

        // --- Buttons ---
        resumeBtn = neonButton("Resume Game");
        exitBtn   = neonButton("Exit Game");
        resumeBtn.setOnAction(e -> {
            SoundManager.getInstance().play("selected"); // <--- DÒNG MỚI
            cb.onResume();
        });
        exitBtn.setOnAction(e -> {
            SoundManager.getInstance().play("selected"); // <--- DÒNG MỚI
            cb.onExit();
        });

        VBox menu = new VBox(ITEM_SPACING, resumeBtn, exitBtn);
        menu.setAlignment(Pos.CENTER);
        menu.setPadding(new Insets(0, SIDE_PADDING, 0, SIDE_PADDING));
        menu.setMaxWidth(FRAME_W);
        menu.setMouseTransparent(false);

        // --- Selector neon ---
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

        // Ràng bề rộng selector theo menu (nếu sau này đổi padding)
       // selector.widthProperty().bind(menu.widthProperty());

        // --- Content trong khung ---
        StackPane content = new StackPane(selectorLayer, menu);
        content.setMaxSize(FRAME_W, FRAME_H);

        // --- Board: cố định 480x234 để không bị kéo full màn ---
        StackPane board = new StackPane(frame, title, content);
        board.setPrefSize(FRAME_W, FRAME_H);
        board.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        board.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        StackPane.setAlignment(board, Pos.CENTER);

        // Root chỉ chứa board
        root.getChildren().setAll(board);
        StackPane.setAlignment(board, Pos.CENTER);

        // --- Items & hiệu ứng chọn ban đầu ---
        items = new Region[]{resumeBtn, exitBtn};
        wireHoverHandlers();          // đảm bảo selector bám theo chuột
        applySelectionVisuals();
        root.sceneProperty().addListener((obs, o, sc) -> {
            if (sc != null) relocateSelector(items[index], false);
        });

        // --- Phím điều hướng khi overlay hiện ---
        root.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case DOWN, S -> move(1);
                case UP, W -> move(-1);
                case ENTER, SPACE, Z -> {
                    if (index == 0) cb.onResume();
                    else          cb.onExit();
                    e.consume(); // <--- THÊM DÒNG NÀY ĐỂ CHẶN SỰ KIỆN PHÍM
                }
                case ESCAPE, X, BACK_SPACE -> cb.onResume();
            }
        });
    }

    public Node getView() { return root; }

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

    public void hide() {
        Timeline tl = new Timeline(new KeyFrame(Duration.millis(120),
                new KeyValue(root.opacityProperty(), 0, Interpolator.EASE_BOTH)));
        tl.setOnFinished(ev -> root.setVisible(false));
        tl.play();
    }

    // ================== Helpers ==================
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

    private void wireHoverHandlers() {
        for (int i = 0; i < items.length; i++) {
            final int idx = i;
            Region r = items[i];
            r.setPickOnBounds(true); // ổn định hitbox khi scale
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

    private void move(int delta) {
        int next = (index + delta + items.length) % items.length;
        setIndex(next);
    }

    private void setIndex(int newIndex) {
        if (newIndex == index) return;
        SoundManager.getInstance().play("card_transition");
        index = newIndex;
        applySelectionVisuals();
        relocateSelector(items[index], true);
    }

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

    // Canh selector theo trung tâm của target và của parent chứa selector
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

    private static DropShadow shadow(double strength) {
        DropShadow ds = new DropShadow();
        ds.setRadius(12);
        ds.setOffsetX(0);
        ds.setOffsetY(0);
        ds.setColor(Color.color(0, 0, 0, 0.9));
        ds.setSpread(0.15 * strength);
        return ds;
    }

    private static javafx.scene.effect.Glow glow(Color c, double radius) {
        javafx.scene.effect.DropShadow outer =
                new javafx.scene.effect.DropShadow(radius, c.deriveColor(0, 1, 1, 0.85));
        outer.setSpread(0.45);
        return new javafx.scene.effect.Glow(0.25) {{ setInput(outer); }};
    }
}
