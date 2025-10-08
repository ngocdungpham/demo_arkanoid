// SceneLayoutFactory.java (bổ sung một số tiện ích)
package com.ooparkanoid.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public final class SceneLayoutFactory {

    private SceneLayoutFactory() {}

    public static StackPane createFullScreenLayer() {
        StackPane layer = new StackPane();
        layer.setPrefSize(StackPane.USE_COMPUTED_SIZE, StackPane.USE_COMPUTED_SIZE);
        layer.getStyleClass().add("root"); // để .root trong CSS ăn vào
        return layer;
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
}
