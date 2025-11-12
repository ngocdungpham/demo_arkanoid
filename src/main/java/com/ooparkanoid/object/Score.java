package com.ooparkanoid.object;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Floating score display that appears when points are awarded in the game.
 * Extends GameObject to provide animated, fading score text that floats upward.
 * Used for visual feedback when bricks are destroyed or bonuses are collected.
 *
 * Features:
 * - Floating upward animation with configurable speed
 * - Fade-out effect over time for smooth disappearance
 * - Automatic centering on specified position
 * - Color customization for different score types
 * - Lifetime management with finished state tracking
 *
 * Animation Behavior:
 * - Moves upward at constant velocity (dy = -30 pixels/second)
 * - Fades out over 0.8 seconds using alpha transparency
 * - Automatically removes itself when lifetime expires
 *
 * Usage:
 * Create with text, position, and color when score is awarded.
 * Add to game object list, update/render each frame.
 * Remove when isFinished() returns true.
 *
 * Visual Design:
 * - Tahoma Bold 16pt font for clear readability
 * - Centered text positioning
 * - Smooth fade-out prevents abrupt disappearance
 *
 * Thread Safety: Not thread-safe. Should be accessed from single game thread.
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public class Score extends GameObject {
    /** The score text to display (e.g., "+100", "BONUS") */
    private String text;

    /** Vertical velocity in pixels per second (negative = upward) */
    private double dy;

    /** Remaining lifetime in seconds (decreases over time) */
    private double life;

    /** Initial lifetime value for fade calculations */
    private double initialLife;

    /** Color of the score text */
    private Color color;

    /** Font used for rendering score text */
    private static final Font FONT = Font.font("Tahoma", FontWeight.BOLD, 16);

    /**
     * Constructs a Score object with specified text, position, and color.
     * Automatically centers the text horizontally at the given position.
     * Sets up default animation parameters (upward movement, 0.8s lifetime).
     *
     * @param text the score text to display (e.g., "+100", "BONUS")
     * @param centerX the X coordinate to center the text on
     * @param topY the Y coordinate for the top of the text
     * @param color the color of the score text
     */
    public Score(String text, double centerX, double topY, Color color) {
        super();
        Text tempText = new Text(text);
        tempText.setFont(FONT);
        double textWidth = tempText.getLayoutBounds().getWidth();
        double textHeight = tempText.getLayoutBounds().getHeight();
        this.setWidth(tempText.getLayoutBounds().getWidth());
        this.setHeight(tempText.getLayoutBounds().getHeight());
        this.setX(centerX - this.getWidth() / 2);
        this.setY(topY);

        this.text = text;
        this.color = color;
        this.dy = -30;
        this.life = 0.8;
        this.initialLife = this.life;
    }

    /**
     * Updates the score's animation state.
     * Decreases lifetime and moves the score upward based on elapsed time.
     *
     * @param dt time elapsed since last update in seconds
     */
    @Override
    public void update(double dt) {
        life -= dt;
        y += dy * dt;
    }

    /**
     * Renders the score text with fade-out effect.
     * Uses alpha transparency based on remaining lifetime.
     * Does not render if the score has finished its animation.
     *
     * @param gc the GraphicsContext to render to
     */
    @Override
    public void render(GraphicsContext gc) {
        if (isFinished()) return;
        double alpha = Math.max(0, life / initialLife);
        gc.save();
        gc.setGlobalAlpha(alpha);
        gc.setFont(FONT);
        gc.setFill(color);
        gc.fillText(text, x, y);
        gc.restore();
    }

    /**
     * Checks if the score animation has completed.
     * Returns true when lifetime has expired and score should be removed.
     *
     * @return true if animation is finished, false otherwise
     */
    public boolean isFinished() {
        return life <= 0;
    }
}
