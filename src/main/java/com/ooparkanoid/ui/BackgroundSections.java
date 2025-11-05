package com.ooparkanoid.ui;

import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import com.ooparkanoid.utils.Constants;

import java.net.URL;
import java.util.Optional;

/** Xây dựng nền chia 3 phần (trái / giữa / phải) và co giãn theo mode. */
public class BackgroundSections {
    private final HBox root;
    private final StackPane left;
    private final StackPane center;
    private final StackPane right;

    public BackgroundSections(double width, double height) {
        left = section(height, "background-left");
        center = section(height, "background-center");
        right = section(height, "background-right");

        // màu nền
        BackgroundFill sideFill = new BackgroundFill(Color.rgb(8, 12, 28, 0.88), CornerRadii.EMPTY, Insets.EMPTY);
        left.setBackground(new Background(sideFill));
        right.setBackground(new Background(sideFill));

        BackgroundFill centerFill = new BackgroundFill(Color.rgb(6, 10, 30), CornerRadii.EMPTY, Insets.EMPTY);

        // ảnh giữa
        Optional<Image> backdrop = loadImage("/picture/space1.png");
        if (backdrop.isPresent()) {
            BackgroundImage coverImage = BackgroundLayer.cover(backdrop.get());
            center.setBackground(new Background(new BackgroundFill[]{centerFill}, new BackgroundImage[]{coverImage}));
        } else {
            center.setBackground(new Background(centerFill));
        }

        // ảnh hai bên (dùng chung)
        Optional<Image> sideImg = loadImage("/picture/menu1.jpg");
        sideImg.ifPresent(img -> {
            BackgroundImage bg = BackgroundLayer.cover(img);
            left.setBackground(new Background(new BackgroundFill[]{sideFill}, new BackgroundImage[]{bg}));
            right.setBackground(new Background(new BackgroundFill[]{sideFill}, new BackgroundImage[]{bg}));
        });

        root = new HBox(left, center, right);
        root.setPrefSize(width, height);
        root.setMouseTransparent(true);

        // width mặc định Adventure
        setSectionWidth(left, Constants.LEFT_PANEL_WIDTH, height);
        setSectionWidth(center, Constants.PLAYFIELD_WIDTH, height);
        setSectionWidth(right, Constants.RIGHT_PANEL_WIDTH, height);
    }

    public HBox getRoot() { return root; }

    public void updateForMode(boolean battle) {
        if (battle) {
            left.setVisible(false);  left.setManaged(false);
            right.setVisible(false); right.setManaged(false);
            setSectionWidth(left, 0, Constants.HEIGHT);
            setSectionWidth(right, 0, Constants.HEIGHT);
            setSectionWidth(center, Constants.WIDTH, Constants.HEIGHT);
        } else {
            left.setVisible(true);  left.setManaged(true);
            right.setVisible(true); right.setManaged(true);
            setSectionWidth(left, Constants.LEFT_PANEL_WIDTH, Constants.HEIGHT);
            setSectionWidth(right, Constants.RIGHT_PANEL_WIDTH, Constants.HEIGHT);
            setSectionWidth(center, Constants.PLAYFIELD_WIDTH, Constants.HEIGHT);
        }
    }

    private StackPane section(double height, String styleClass) {
        StackPane s = new StackPane();
        s.setPrefHeight(height);
        s.setMinHeight(height);
        s.setMaxHeight(height);
        s.getStyleClass().add(styleClass);
        return s;
    }

    private void setSectionWidth(StackPane section, double width, double height) {
        section.setPrefWidth(width);
        section.setMinSize(width, height);
        section.setMaxWidth(width);
    }

    private Optional<Image> loadImage(String path) {
        URL url = getClass().getResource(path);
        if (url == null) return Optional.empty();
        try { return Optional.of(new Image(url.toExternalForm(), true)); }
        catch (IllegalArgumentException ex) { return Optional.empty(); }
    }
}
