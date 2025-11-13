package com.ooparkanoid.ui;

import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import com.ooparkanoid.utils.Constants;

import java.net.URL;
import java.util.Optional;

/**
 * Creates a three-section background layout (left/center/right) that adapts to different game modes.
 * Manages background images and colors for each section with dynamic width adjustments.
 * Designed for seamless mode switching between Adventure (three panels) and Battle (single center panel).
 *
 * Layout Structure:
 * - Left Panel: Statistics and UI elements (Adventure mode only)
 * - Center Panel: Main playfield with space backdrop
 * - Right Panel: Round display and additional UI (Adventure mode only)
 *
 * Features:
 * - Dynamic panel visibility and sizing based on game mode
 * - Layered backgrounds (solid colors + images) for visual depth
 * - Automatic image loading with fallback to solid colors
 * - Responsive layout that adapts to window dimensions
 * - Mouse-transparent for proper UI interaction
 *
 * Visual Design:
 * - Dark space-themed color palette (blues and blacks)
 * - Semi-transparent side panels for readability
 * - Full-coverage background images with aspect ratio preservation
 * - Smooth transitions between Adventure and Battle modes
 *
 * Usage:
 * Create with desired dimensions, then call updateForMode() to switch layouts.
 * Add to scene graph as background layer.
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public class BackgroundSections {
    /** Root container using HBox for horizontal layout */
    private final HBox root;

    /** Left section panel (statistics area in Adventure mode) */
    private final StackPane left;

    /** Center section panel (main playfield) */
    private final StackPane center;

    /** Right section panel (round display in Adventure mode) */
    private final StackPane right;

    /**
     * Constructs a BackgroundSections with specified dimensions.
     * Initializes all three panels with appropriate backgrounds and styling.
     * Sets up default Adventure mode layout with three visible panels.
     *
     * @param width total width of the background layout
     * @param height total height of the background layout
     */
    public BackgroundSections(double width, double height) {
        left = section(height, "background-left");
        center = section(height, "background-center");
        right = section(height, "background-right");

        // Configure side panel backgrounds (semi-transparent dark blue)
        BackgroundFill sideFill = new BackgroundFill(Color.rgb(8, 12, 28, 0.88), CornerRadii.EMPTY, Insets.EMPTY);
        left.setBackground(new Background(sideFill));
        right.setBackground(new Background(sideFill));

        // Configure center panel background (darker blue)
        BackgroundFill centerFill = new BackgroundFill(Color.rgb(6, 10, 30), CornerRadii.EMPTY, Insets.EMPTY);

        // Load and apply center backdrop image
        Optional<Image> backdrop = loadImage("/picture/space1.png");
        if (backdrop.isPresent()) {
            BackgroundImage coverImage = BackgroundLayer.cover(backdrop.get());
            center.setBackground(new Background(new BackgroundFill[]{centerFill}, new BackgroundImage[]{coverImage}));
        } else {
            center.setBackground(new Background(centerFill));
        }

        // Load and apply side panel background images
        Optional<Image> sideImg = loadImage("/picture/menu1.jpg");
        sideImg.ifPresent(img -> {
            BackgroundImage bg = BackgroundLayer.cover(img);
            left.setBackground(new Background(new BackgroundFill[]{sideFill}, new BackgroundImage[]{bg}));
            right.setBackground(new Background(new BackgroundFill[]{sideFill}, new BackgroundImage[]{bg}));
        });

        root = new HBox(left, center, right);
        root.setPrefSize(width, height);
        root.setMouseTransparent(true);

        // Set default Adventure mode widths
        setSectionWidth(left, Constants.LEFT_PANEL_WIDTH, height);
        setSectionWidth(center, Constants.PLAYFIELD_WIDTH, height);
        setSectionWidth(right, Constants.RIGHT_PANEL_WIDTH, height);
    }

    /**
     * Gets the root HBox container for this background layout.
     *
     * @return the HBox containing all three background sections
     */
    public HBox getRoot() {
        return root;
    }

    /**
     * Updates the layout for the specified game mode.
     * Switches between Adventure mode (three panels) and Battle mode (single center panel).
     * Adjusts panel visibility and widths accordingly.
     *
     * @param battle true for Battle mode (center panel only), false for Adventure mode (three panels)
     */
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

    /**
     * Creates a section panel with specified height and CSS style class.
     *
     * @param height the height of the section panel
     * @param styleClass the CSS style class to apply
     * @return configured StackPane section panel
     */
    private StackPane section(double height, String styleClass) {
        StackPane s = new StackPane();
        s.setPrefHeight(height);
        s.setMinHeight(height);
        s.setMaxHeight(height);
        s.getStyleClass().add(styleClass);
        return s;
    }

    /**
     * Sets the width dimensions for a section panel.
     *
     * @param section the StackPane section to resize
     * @param width the new width for the section
     * @param height the height to maintain
     */
    private void setSectionWidth(StackPane section, double width, double height) {
        section.setPrefWidth(width);
        section.setMinSize(width, height);
        section.setMaxWidth(width);
    }

    /**
     * Loads an image from the classpath resources.
     * Returns empty Optional if image cannot be loaded or path is invalid.
     *
     * @param path the resource path to the image file
     * @return Optional containing the loaded Image, or empty if loading failed
     */
    private Optional<Image> loadImage(String path) {
        URL url = getClass().getResource(path);
        if (url == null) return Optional.empty();
        try {
            return Optional.of(new Image(url.toExternalForm(), true));
        }
        catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
