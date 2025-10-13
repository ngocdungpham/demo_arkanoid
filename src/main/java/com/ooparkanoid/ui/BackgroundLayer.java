package com.ooparkanoid.ui;

import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.geometry.Insets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A light-weight layer placed at the bottom of the scene graph that is responsible for
 * rendering static backgrounds.  The layer keeps track of a base fill (solid color or
 * gradient) as well as an ordered list of {@link BackgroundImage background images} so that
 * additional visual layouts can be stacked without interfering with each other.
 */
public final class BackgroundLayer extends StackPane {

    private BackgroundFill baseFill;
    private final List<BackgroundImage> imageLayers = new ArrayList<>();

    public BackgroundLayer() {
        setPickOnBounds(false);
        setFill(Paint.valueOf("#000000"));
    }

    /**
     * Sets the fill paint for the background layer.  Passing {@code null} clears the current
     * fill and leaves only the image layers.
     */
    public void setFill(Paint paint) {
        baseFill = (paint == null) ? null : new BackgroundFill(paint, CornerRadii.EMPTY, Insets.EMPTY);
        applyBackground();
    }

    /**
     * Replaces the existing image layers with the provided list.
     */
    public void setImageLayers(List<BackgroundImage> images) {
        imageLayers.clear();
        if (images != null && !images.isEmpty()) {
            imageLayers.addAll(images);
        }
        applyBackground();
    }

    /**
     * Adds a new image layer to the background.
     */
    public void addImageLayer(BackgroundImage image) {
        Objects.requireNonNull(image, "image");
        imageLayers.add(image);
        applyBackground();
    }

    /**
     * Removes the given image layer if it is present.
     */
    public void removeImageLayer(BackgroundImage image) {
        if (imageLayers.remove(image)) {
            applyBackground();
        }
    }

    /**
     * Returns an immutable snapshot of the image layers.
     */
    public List<BackgroundImage> getImageLayers() {
        return Collections.unmodifiableList(imageLayers);
    }

    private void applyBackground() {
        List<BackgroundFill> fills = (baseFill == null)
                ? Collections.emptyList()
                : Collections.singletonList(baseFill);
        setBackground(new Background(fills, List.copyOf(imageLayers)));
    }

    /**
     * Convenience for creating an image layer that covers the entire available area.
     */
    public static BackgroundImage cover(Image image) {
        Objects.requireNonNull(image, "image");
        BackgroundSize size = new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO,
                false, false, false, true);
        return new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, size);
    }
}