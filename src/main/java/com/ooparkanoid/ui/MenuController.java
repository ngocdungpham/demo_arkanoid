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

public class MenuController implements Initializable {

    // ---- Screen ----
    static final int W = 1280, H = 720;
    static final double LINE_Y = H / 2.25;

    // ---- Card ----
    static final double CARD_W = 250, CARD_H = 350;
    static final double SPACING = 400;                  // khoảng cách tâm–tâm

    // ---- Anim ----
    static final Duration SLIDE_MS   = Duration.millis(360);
    static final Duration HOVER_MS   = Duration.millis(120);
    static final Duration FLICKER_MS = Duration.millis(400);

    // ---- Theme (neon cyan) ----
    static final Color FILL_BASE  = Color.web("#0A1E3ACC");
    static final Color FILL_HOVER = Color.web("#0ff8ff66");
    static final Color STROKE     = Color.web("#00FFFF");
    static final Color TEXT_BASE  = Color.web("#EFFFFF");
    static final Color TEXT_PULSE = Color.web("#00FFFF");

    // ---- Background image (tùy chọn) ----
    static final String BACKGROUND_IMAGE ="/picture/menu.jpg";

    // ---- Data ----
    final List<String> items = List.of("Adventure", "VERSUS", "SETTINGS", "CREDITS","HELP", "EXIT");

    // ---- Runtime state ----
    final List<StackPane> cards = new ArrayList<>();
    int cur = 0; // chỉ số thẻ giữa
    final DoubleProperty offset = new SimpleDoubleProperty(0); // -1..0..+1 khi trượt
    Timeline centerFlicker;

    // ---- FXML refs ----
    @FXML private ImageView bg;
    @FXML private Pane cardsLayer;

    private Consumer<String> onSelectionCallback;

    // ---- THÊM PHƯƠNG THỨC SETTER NÀY ----
    public void setOnSelectionCallback(Consumer<String> callback) {
        this.onSelectionCallback = callback;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Kích thước lớp chứa
        cardsLayer.setPrefSize(W, H);
        cardsLayer.setPickOnBounds(false);

        // Background
        if (BACKGROUND_IMAGE != null) {
            var url = getClass().getResource(BACKGROUND_IMAGE);
            if (url == null) {
                throw new IllegalStateException("Không tìm thấy ảnh: " + BACKGROUND_IMAGE);
            }
            bg.setImage(new Image(url.toExternalForm(), W, H, false, true));
            bg.setFitWidth(W);
            bg.setFitHeight(H);
            bg.setPreserveRatio(false);
            bg.setMouseTransparent(true);
            bg.setViewOrder(999);
        }

        // Preload cards
        for (String title : items) {
            StackPane c = makeCard(title);
            attachMouse(c);
            c.setVisible(false);
            cards.add(c);
            cardsLayer.getChildren().add(c);
        }

        // Lần đầu + lắng nghe offset
        layoutCards(0);
        applyCenterHighlight();
        offset.addListener((o, ov, nv) -> layoutCards(nv.doubleValue()));

        // Gắn phím khi Scene sẵn sàng
        cardsLayer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) setupKeyHandlers(newScene);
        });
    }

    private void setupKeyHandlers(Scene scene) {
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case LEFT  -> slide(-1);
                case RIGHT -> slide(+1);
                case ENTER -> onSelect();
            }
        });
    }

    // ---------- UI: Card ----------
    private StackPane makeCard(String title) {
        var r = new javafx.scene.shape.Rectangle(CARD_W, CARD_H);
        r.setArcWidth(30);
        r.setArcHeight(30);
        r.setFill(FILL_BASE);
        r.setStroke(STROKE);
        r.setStrokeWidth(2.0);

        Text t = new Text(title);
        t.setFill(TEXT_BASE);
        t.setFont(Font.font("Orbitron", 34)); // nếu thiếu font sẽ fallback

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
            int slot = getSlot(card); // -1 / 0 / +1
            if (slot == 0) onSelect();
            else slide(slot);
        });
    }

    // ---------- Layout ----------
    private void layoutCards(double off) {
        double cx = W / 2.0;

        for (int i = 0; i < cards.size(); i++) {
            StackPane card = cards.get(i);

            // tương đối theo vòng (wrap) + offset
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

            // z-order: thẻ giữa nổi trên
            card.setViewOrder(2 - scale);

            // thẻ không ở giữa => trả về base
            if (Math.abs(rel) > 0.25) {
                if (!bg.getFill().equals(FILL_BASE)) bg.setFill(FILL_BASE);
                var tx = (Text) card.getChildren().get(1);
                if (!tx.getFill().equals(TEXT_BASE)) tx.setFill(TEXT_BASE);
            }
        }
    }

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
    private void slide(int dir) {
        if (offset.get() != 0) return;

        Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(offset, 0)),
                new KeyFrame(SLIDE_MS, new KeyValue(offset, dir, Interpolator.EASE_BOTH))
        );
        tl.setOnFinished(e -> {
            cur = (cur + dir + items.size()) % items.size(); // vòng
            offset.set(0);
            layoutCards(0);
            applyCenterHighlight();
        });
        tl.play();
    }

    private void onSelect() {
       // System.out.println("Selected: " + items.get(cur));
        // TODO: chuyển scene/game state tại đây
        String selection = items.get(cur);
        System.out.println("Selected: " + selection);
        if (onSelectionCallback != null) {
            onSelectionCallback.accept(selection);
        }
    }

    // ---------- Helpers ----------
    private int getSlot(StackPane card) {
        if (!card.isVisible()) return 99;
        double centerX = card.getLayoutX() + CARD_W / 2.0;
        double dx = centerX - W / 2.0;
        if (Math.abs(dx) < SPACING * 0.25) return 0;
        return (dx < 0) ? -1 : +1;
    }

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

    private static double wrapRelative(double rel, int n) {
        while (rel <= -n / 2.0) rel += n;
        while (rel >   n / 2.0) rel -= n;
        return rel;
    }

    private static <T> void animateTo(javafx.beans.property.Property<T> prop, T from, T to, Duration d) {
        Timeline t = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(prop, from)),
                new KeyFrame(d, new KeyValue(prop, to, Interpolator.EASE_BOTH))
        );
        t.play();
    }

}
