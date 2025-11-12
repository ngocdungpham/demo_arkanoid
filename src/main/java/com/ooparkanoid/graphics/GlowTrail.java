package com.ooparkanoid.graphics;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineJoin;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Creates a glowing trail effect behind moving objects (typically balls).
 * Renders multiple layered glow effects with fading opacity and varying thickness.
 * Provides customizable colors, intensity, and trail length for visual enhancement.
 *
 * Features:
 * - Multi-layered glow rendering (outer glow, inner glow, core)
 * - Automatic point interpolation for smooth trails
 * - Configurable trail length and fade-out timing
 * - Color customization with automatic glow color derivation
 * - Performance-optimized with maximum trail length limits
 * - Enable/disable toggle for conditional rendering
 *
 * Rendering Layers:
 * 1. Outer Glow: Largest, most transparent layer for ambient glow
 * 2. Inner Glow: Medium layer with primary glow color
 * 3. Core: Smallest, brightest layer for trail core
 *
 * Usage:
 * Create instance with ball size, call addPoint() each frame with position,
 * then render() to draw the trail. Customize with setColor() or setColors().
 *
 * Performance Notes:
 * - Trail points automatically expire after 1 second
 * - Maximum trail length prevents memory growth
 * - Interpolation adds intermediate points for smoothness
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public class GlowTrail {
    /** Queue of trail points in chronological order (oldest first) */
    private Queue<TrailPoint> points = new LinkedList<>();

    /** Maximum number of trail points to maintain */
    private int maxLength = 30;

    /** Color for the trail core (brightest, most opaque) */
    private Color coreColor = Color.WHITE;

    /** Color for the inner glow layer */
    private Color glowColor = Color.CYAN;

    /** Color for the outer glow layer (most transparent) */
    private Color outerGlowColor = Color.BLUE;

    /** Size of the ball this trail follows (affects trail thickness) */
    private double ballSize;

    /** Multiplier for glow opacity and intensity */
    private double glowIntensity = 1.0;

    /** Whether trail rendering is enabled */
    private boolean enabled = true;

    /** Previous ball position for interpolation calculations */
    private double lastX = -1, lastY = -1;

    /**
     * Represents a single point in the trail with position and timestamp.
     * Used internally to track trail segments and implement fade-out behavior.
     */
    private static class TrailPoint {
        /** X coordinate of this trail point */
        double x;

        /** Y coordinate of this trail point */
        double y;

        /** Timestamp when this point was created (milliseconds) */
        long timestamp;

        /**
         * Constructs a trail point with position and creation time.
         *
         * @param x X coordinate
         * @param y Y coordinate
         * @param timestamp creation timestamp in milliseconds
         */
        TrailPoint(double x, double y, long timestamp) {
            this.x = x;
            this.y = y;
            this.timestamp = timestamp;
        }
    }

    /**
     * Constructs a GlowTrail with specified ball size.
     * Ball size affects the thickness of trail segments.
     *
     * @param ballSize the diameter of the ball this trail follows
     */
    public GlowTrail(double ballSize) {
        this.ballSize = ballSize;
    }

    /**
     * Adds a new point to the trail at the specified position.
     * Automatically interpolates intermediate points if movement distance is significant.
     * Maintains trail length limits and removes expired points.
     *
     * @param x X coordinate of the new trail point
     * @param y Y coordinate of the new trail point
     */
    public void addPoint(double x, double y) {
        if (!enabled) {
            return;
        }

        // Interpolate points if movement distance is significant
        if (lastX >= 0 && lastY >= 0) {
            double distance = Math.sqrt(Math.pow(x - lastX, 2) + Math.pow(y - lastY, 2));

            // Add intermediate points for smooth trails
            if (distance > 1) {
                int steps = (int) (distance / 5);
                for (int i = 1; i <= steps; i++) {
                    double t = (double) i / (steps + 1);
                    double interpX = lastX + (x - lastX) * t;
                    double interpY = lastY + (y - lastY) * t;
                    points.add(new TrailPoint(interpX, interpY, System.currentTimeMillis()));
                }
            }
        }

        // Add current position point
        points.add(new TrailPoint(x, y, System.currentTimeMillis()));
        lastX = x;
        lastY = y;

        // Maintain trail length limit
        while (points.size() > maxLength) {
            points.poll();
        }
    }

    /**
     * Renders the complete glow trail to the graphics context.
     * Draws three layered glow effects with varying opacity and thickness.
     * Preserves GraphicsContext state during rendering.
     *
     * @param gc the GraphicsContext to render the trail to
     */
    public void render(GraphicsContext gc) {
        if (!enabled || points.isEmpty()) {
            return;
        }

        // Convert queue to array for efficient iteration
        TrailPoint[] array = points.toArray(new TrailPoint[0]);

        // Render three glow layers from back to front
        renderOuterGlow(gc, array);
        renderInnerGlow(gc, array);
        renderCore(gc, array);
    }

    /**
     * Renders the outer glow layer (largest, most transparent).
     * Creates ambient glow effect around the trail.
     *
     * @param gc the GraphicsContext to render to
     * @param points array of trail points to render
     */
    private void renderOuterGlow(GraphicsContext gc, TrailPoint[] points) {
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        for (int i = 0; i < points.length - 1; i++) {
            TrailPoint p1 = points[i];
            TrailPoint p2 = points[i + 1];

            // Calculate opacity based on position in trail (older = more transparent)
            double progress = i * 1.0 / points.length;
            double alpha = (0.05 + progress * 0.1) * glowIntensity;

            // Calculate thickness (larger for outer glow)
            double thickness = ballSize * 1.0 * (0.2 + progress * 0.8);

            gc.setGlobalAlpha(alpha);
            gc.setStroke(outerGlowColor);
            gc.setLineWidth(thickness);
            gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
        }
        gc.setGlobalAlpha(1.0);
    }

    /**
     * Renders the inner glow layer (medium size and opacity).
     * Creates primary glow effect with main glow color.
     *
     * @param gc the GraphicsContext to render to
     * @param points array of trail points to render
     */
    private void renderInnerGlow(GraphicsContext gc, TrailPoint[] points) {
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        for (int i = 0; i < points.length - 1; i++) {
            TrailPoint p1 = points[i];
            TrailPoint p2 = points[i + 1];

            double progress = i * 1.0 / points.length;
            double alpha = (0.05 + progress * 0.1) * glowIntensity;
            double thickness = ballSize * 0.7 * (0.3 + progress * 0.7);

            gc.setGlobalAlpha(alpha);
            gc.setStroke(glowColor);
            gc.setLineWidth(thickness);
            gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
        }
        gc.setGlobalAlpha(1.0);
    }

    /**
     * Renders the core trail layer (smallest, brightest).
     * Creates the central bright trail effect.
     *
     * @param gc the GraphicsContext to render to
     * @param points array of trail points to render
     */
    private void renderCore(GraphicsContext gc, TrailPoint[] points) {
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        for (int i = 0; i < points.length - 1; i++) {
            TrailPoint p1 = points[i];
            TrailPoint p2 = points[i + 1];

            double progress = i * 1.0 / points.length;
            double alpha = (0.1 + progress * 0.4) * glowIntensity;
            double thickness = ballSize * 0.4 * (0.4 + progress * 0.6);

            gc.setGlobalAlpha(alpha);
            gc.setStroke(coreColor);
            gc.setLineWidth(thickness);
            gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
        }
        gc.setGlobalAlpha(1.0);
    }

    /**
     * Updates the trail by removing expired points.
     * Points older than 1 second are automatically removed.
     * Should be called regularly to maintain trail freshness.
     *
     * @param deltaTime time elapsed since last update (currently unused)
     */
    public void update(double deltaTime) {
        long currentTime = System.currentTimeMillis();
        points.removeIf(p -> (currentTime - p.timestamp) > 1000);
    }

    /**
     * Clears all trail points and resets interpolation state.
     * Trail will be empty after calling this method.
     */
    public void clear() {
        points.clear();
        lastX = -1;
        lastY = -1;
    }

    /**
     * Sets trail colors using a single base color.
     * Automatically derives glow colors from the base color.
     * Core remains white, glow uses the specified color, outer glow is darker.
     *
     * @param color the base color for glow effects
     */
    public void setColor(Color color) {
        this.coreColor = Color.WHITE;
        this.glowColor = color;
        this.outerGlowColor = color.deriveColor(0, 1.0, 0.6, 1.0);
    }

    /**
     * Sets trail colors with full customization.
     * Allows independent control of all three glow layers.
     *
     * @param core color for the trail core (brightest layer)
     * @param glow color for the inner glow layer
     * @param outerGlow color for the outer glow layer (most transparent)
     */
    public void setColors(Color core, Color glow, Color outerGlow) {
        this.coreColor = core;
        this.glowColor = glow;
        this.outerGlowColor = outerGlow;
    }

    /**
     * Sets the maximum number of trail points to maintain.
     * Higher values create longer trails but use more memory.
     *
     * @param maxLength maximum number of trail points (recommended: 20-50)
     */
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * Sets the glow intensity multiplier.
     * Affects opacity of all glow layers. Clamped to reasonable range.
     *
     * @param intensity glow intensity multiplier (0.1 to 2.0)
     */
    public void setGlowIntensity(double intensity) {
        this.glowIntensity = Math.max(0.1, Math.min(2.0, intensity));
    }

    /**
     * Enables or disables trail rendering.
     * When disabled, addPoint() becomes a no-op and render() draws nothing.
     * Useful for performance optimization or conditional effects.
     *
     * @param enabled true to enable trail rendering, false to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}