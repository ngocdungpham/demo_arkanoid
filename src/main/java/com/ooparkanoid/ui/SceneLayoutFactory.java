package com.ooparkanoid.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class SceneLayoutFactory {

    private SceneLayoutFactory() {}

    public static LayeredScene createLayeredScene(Node gameContent) {
        Objects.requireNonNull(gameContent, "gameContent");

        BackgroundLayer backgroundLayer = new BackgroundLayer();
        StackPane contentLayer = new StackPane(gameContent);
        StackPane overlayLayer = new StackPane();
        overlayLayer.setPickOnBounds(false);

        StackPane root = new StackPane(backgroundLayer, contentLayer, overlayLayer);
        root.getStyleClass().add("app");

        return new LayeredScene(root, backgroundLayer, contentLayer, overlayLayer);
    }

    public static VBox createMenuCard(Node... children) {
        VBox card = new VBox(14);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("menu-card");
        card.getChildren().addAll(children);
        return card;
    }

    public static Label createHeading(String text) {
        Label lb = new Label(text);
        lb.getStyleClass().add("heading-xl");
        return lb;
    }

    public static Label createSubtitle(String text) {
        Label lb = new Label(text);
        lb.getStyleClass().add("subtitle");
        return lb;
    }

    public static Button createPillButton(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("btn");
        return b;
    }

    public static final class LayeredScene {
        private final StackPane root;
        private final BackgroundLayer backgroundLayer;
        private final StackPane contentLayer;
        private final StackPane overlayLayer;
        private final Map<String, Node> overlayRegistry = new LinkedHashMap<>();

        private LayeredScene(StackPane root,
                              BackgroundLayer backgroundLayer,
                              StackPane contentLayer,
                              StackPane overlayLayer) {
            this.root = root;
            this.backgroundLayer = backgroundLayer;
            this.contentLayer = contentLayer;
            this.overlayLayer = overlayLayer;
        }

        public StackPane root() {
            return root;
        }

        public BackgroundLayer backgroundLayer() {
            return backgroundLayer;
        }

        public StackPane contentLayer() {
            return contentLayer;
        }

        public StackPane overlayLayer() {
            return overlayLayer;
        }

        public void registerOverlay(String key, Node overlay) {
            Objects.requireNonNull(key, "key");
            Objects.requireNonNull(overlay, "overlay");

            Node existing = overlayRegistry.put(key, overlay);
            if (existing != null) {
                detach(existing);
            }
            attach(overlay);
        }

        public void unregisterOverlay(String key) {
            Node node = overlayRegistry.remove(key);
            if (node != null) {
                detach(node);
            }
        }

        public Node getOverlay(String key) {
            return overlayRegistry.get(key);
        }

        public Map<String, Node> getRegisteredOverlays() {
            return Collections.unmodifiableMap(overlayRegistry);
        }

        public void clearOverlays() {
            overlayRegistry.values().forEach(this::detach);
            overlayRegistry.clear();
        }

        private void attach(Node overlay) {
            detachFromParent(overlay);
            overlayLayer.getChildren().add(overlay);
        }

        private void detach(Node overlay) {
            overlayLayer.getChildren().remove(overlay);
        }

        private static void detachFromParent(Node node) {
            Pane parent = (Pane) node.getParent();
            if (parent != null) {
                parent.getChildren().remove(node);
            }
        }
    }
}
