package com.ooparkanoid.graphics;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

/**
 * Utility class for extracting individual frames from sprite sheet images.
 * Provides methods to access frames by index, row/column coordinates, or entire rows.
 * Handles sprite sheet layout calculations including margins and spacing.
 *
 * Sprite Sheet Layout:
 * - Frames are arranged in a grid with specified spacing and margins
 * - Margin: Empty space around the entire sprite sheet
 * - Spacing: Empty space between individual frames
 * - Frame dimensions are uniform across the entire sheet
 *
 * Usage:
 * Create with sprite sheet image and layout parameters, then access frames
 * using getFrame() methods. Supports both linear indexing and 2D coordinates.
 *
 * Performance Notes:
 * - Frames are extracted on-demand (not pre-cached)
 * - Each getFrame() call creates a new WritableImage
 * - Consider caching frequently used frames for better performance
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public class SpriteSheet {
    /** The source sprite sheet image containing all frames */
    private Image spriteSheet;

    /** Width of each individual frame in pixels */
    private int frameWidth;

    /** Height of each individual frame in pixels */
    private int frameHeight;

    /** Horizontal and vertical spacing between frames in pixels */
    private int spacing;

    /** Margin around the entire sprite sheet in pixels */
    private int margin;

    /** Number of frame columns in the sprite sheet */
    private int columns;

    /** Number of frame rows in the sprite sheet */
    private int rows;

    /**
     * Constructs a SpriteSheet with specified layout parameters.
     * Automatically calculates the number of rows and columns based on image dimensions.
     *
     * @param sheet the sprite sheet image containing all frames
     * @param frameWidth width of each individual frame in pixels
     * @param frameHeight height of each individual frame in pixels
     * @param spacing spacing between frames in pixels (both horizontal and vertical)
     * @param margin margin around the sprite sheet in pixels
     */
    public SpriteSheet(Image sheet, int frameWidth, int frameHeight, int spacing, int margin) {
        this.spriteSheet = sheet;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.spacing = spacing;
        this.margin = margin;
        this.columns = (int) ((sheet.getWidth()) / (frameWidth));
        this.rows = (int) ((sheet.getHeight()) / (frameHeight));
    }

    /**
     * Extracts a frame from the sprite sheet using linear indexing.
     * Frames are numbered left-to-right, top-to-bottom starting from 0.
     *
     * @param index the linear index of the frame (0-based)
     * @return the extracted frame as a new Image, or null if index is out of bounds
     */
    public Image getFrame(int index) {
        int col = index % columns;
        int row = index / columns;
        return getFrame(row, col);
    }

    /**
     * Extracts a frame from the sprite sheet using row and column coordinates.
     * Coordinates start from (0,0) at the top-left corner.
     *
     * @param row the row index of the frame (0-based)
     * @param col the column index of the frame (0-based)
     * @return the extracted frame as a new Image, or null if coordinates are out of bounds
     */
    public Image getFrame(int row, int col) {
        if (row >= rows || col >= columns) {
            return null;
        }
        PixelReader reader = spriteSheet.getPixelReader();
        int x = margin + col * (frameWidth + spacing);
        int y = margin + row * (frameHeight + spacing);
        return new WritableImage(reader, x, y, frameWidth, frameHeight);
    }

    /**
     * Extracts all frames from a specific row as an array.
     * Useful for animations that span an entire row of the sprite sheet.
     *
     * @param row the row index to extract frames from (0-based)
     * @return array of Images containing all frames in the specified row
     */
    public Image[] getRowFrames(int row) {
        Image[] frames = new Image[columns];
        for (int i = 0; i < columns; i++) {
            frames[i] = getFrame(row, i);
        }
        return frames;
    }

    /**
     * Gets the total number of frames in the sprite sheet.
     * Calculated as rows × columns.
     *
     * @return total number of frames available in this sprite sheet
     */
    public int getFrameCount() {
        return columns * rows;
    }
}
