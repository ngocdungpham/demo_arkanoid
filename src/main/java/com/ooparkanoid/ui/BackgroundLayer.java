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
 * A lightweight layer placed at the bottom of the scene graph responsible for rendering static backgrounds.
 * Manages a base fill (solid color or gradient) and an ordered list of background images for layered visual layouts.
 * Designed to be non-interactive and efficient for background rendering without interfering with other UI elements.
 *
 * Key Features:
 * - Base fill support (solid colors, gradients) with optional transparency
 * - Multiple image layers with z-ordering (last added appears on top)
 * - Automatic background reconstruction on layer changes
 * - Immutable external API for thread safety
 * - Optimized for static backgrounds (no animation support)
 *
 * Usage:
 * Create instance, set base fill if needed, then add image layers.
 * Position at bottom of scene graph for proper layering.
 *
 * Performance Notes:
 * - Background reconstruction only occurs when layers change
 * - Uses JavaFX Background API for efficient rendering
 * - Minimal memory footprint for static content
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public final class BackgroundLayer extends StackPane {

    /** Base fill for the background (solid color or gradient) */
    private BackgroundFill baseFill;

    /** Ordered list of background image layers (last added appears on top) */
    private final List<BackgroundImage> imageLayers = new ArrayList<>();

    /**
     * Constructs a BackgroundLayer with default black fill.
     * Initializes as non-interactive (pickOnBounds = false) for background use.
     */
    public BackgroundLayer() {
        setPickOnBounds(false);
        setFill(Paint.valueOf("#000000"));
    }

    /**
     * Sets the base fill paint for the background layer.
     * Passing null clears the current fill and leaves only the image layers.
     * Triggers automatic background reconstruction.
     *
     * @param paint the Paint to use for base fill (Color, LinearGradient, RadialGradient, etc.), or null to clear
     */
    public void setFill(Paint paint) {
        baseFill = (paint == null) ? null : new BackgroundFill(paint, CornerRadii.EMPTY, Insets.EMPTY);
        applyBackground();
    }

    /**
     * Replaces all existing image layers with the provided list.
     * Clears current layers and adds all provided images in order.
     * Null or empty list results in no image layers.
     *
     * @param images the new list of BackgroundImage layers, or null/empty to clear all layers
     */
    public void setImageLayers(List<BackgroundImage> images) {
        imageLayers.clear();
        if (images != null && !images.isEmpty()) {
            imageLayers.addAll(images);
        }
        applyBackground();
    }

    /**
     * Adds a new image layer to the background on top of existing layers.
     * The new layer will appear above all previously added layers.
     *
     * @param image the BackgroundImage to add (must not be null)
     * @throws NullPointerException if image is null
     */
    public void addImageLayer(BackgroundImage image) {
        Objects.requireNonNull(image, "image");
        imageLayers.add(image);
        applyBackground();
    }

    /**
     * Removes the specified image layer if it exists in the current layers.
     * If the layer is found and removed, triggers background reconstruction.
     *
     * @param image the BackgroundImage to remove
     * @return true if the layer was found and removed, false otherwise
     */
    public boolean removeImageLayer(BackgroundImage image) {
        if (imageLayers.remove(image)) {
            applyBackground();
            return true;
        }
        return false;
    }

    /**
     * Returns an immutable view of the current image layers.
     * Changes to the returned list will not affect the background layer.
     * Use this for read-only access to inspect current layers.
     *
     * @return unmodifiable List of current BackgroundImage layers
     */
    public List<BackgroundImage> getImageLayers() {
        return Collections.unmodifiableList(imageLayers);
    }

    /**
     * Applies the current base fill and image layers to the JavaFX Background.
     * Called automatically whenever layers change.
     * Reconstructs the Background object with current state.
     */
    private void applyBackground() {
        List<BackgroundFill> fills = (baseFill == null)
                ? Collections.emptyList()
                : Collections.singletonList(baseFill);
        setBackground(new Background(fills, List.copyOf(imageLayers)));
    }

    /**
     * Static convenience method for creating a full-coverage background image.
     * Creates a BackgroundImage that scales to cover the entire available area
     * while maintaining aspect ratio (no distortion).
     *
     * @param image the Image to use for the background
     * @return BackgroundImage configured for full area coverage
     * @throws NullPointerException if image is null
     */
    public static BackgroundImage cover(Image image) {
        Objects.requireNonNull(image, "image");
        BackgroundSize size = new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO,
                false, false, false, true);
        return new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, size);
    }
}