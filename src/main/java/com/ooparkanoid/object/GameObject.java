package com.ooparkanoid.object;

import javafx.scene.canvas.GraphicsContext;

/**
 * Abstract base class for all game objects in the Arkanoid game.
 * Provides common properties and methods for position, dimensions, and rendering.
 * All game entities (balls, paddles, bricks, power-ups) extend this class.
 *
 * Features:
 * - Position and dimension management (x, y, width, height)
 * - Abstract update and render methods for game loop integration
 * - Collision detection with axis-aligned bounding box (AABB) intersection
 * - Fluent setter methods for chaining
 *
 * Coordinate System:
 * - Origin (0,0) is at the top-left corner of the game canvas
 * - X increases to the right, Y increases downward
 * - All coordinates are in pixels
 *
 * Game Loop Integration:
 * - update(dt): Called each frame with delta time for state updates
 * - render(gc): Called each frame to draw the object on the canvas
 *
 * Collision Detection:
 * Uses AABB (Axis-Aligned Bounding Box) intersection for performance.
 * Override intersects() for more complex collision shapes if needed.
 *
 * Thread Safety: Not thread-safe. Should be accessed from single game thread.
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public abstract class GameObject {
    /** X coordinate of the object's position (top-left corner) */
    protected double x;

    /** Y coordinate of the object's position (top-left corner) */
    protected double y;

    /** Width of the object in pixels */
    protected double width;

    /** Height of the object in pixels */
    protected double height;

    /**
     * Constructs a GameObject with default position and dimensions.
     * All values are initialized to 0. Use setters to configure.
     */
    public GameObject() {
    }

    /**
     * Constructs a GameObject with specified position and dimensions.
     *
     * @param x the X coordinate of the object's position
     * @param y the Y coordinate of the object's position
     * @param width the width of the object in pixels
     * @param height the height of the object in pixels
     */
    public GameObject(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Updates the game object's state based on elapsed time.
     * Called once per frame by the game loop. Implement game logic here.
     *
     * @param dt time elapsed since last update in seconds
     */
    public abstract void update(double dt);

    /**
     * Renders the game object to the graphics context.
     * Called once per frame by the game loop. Implement drawing logic here.
     *
     * @param gc the GraphicsContext to render to
     */
    public abstract void render(GraphicsContext gc);

    /**
     * Checks if this game object intersects with another game object.
     * Uses axis-aligned bounding box (AABB) collision detection for performance.
     * Override this method for more complex collision shapes (circles, polygons).
     *
     * @param other the other GameObject to check collision with
     * @return true if the objects intersect, false otherwise
     */
    public boolean intersects(GameObject other) {
        return x < other.x + other.width && x + width > other.x &&
                y < other.y + other.height && y + height > other.y;
    }

    // ==================== Getters ====================

    /**
     * Gets the X coordinate of this object's position.
     *
     * @return the X coordinate in pixels
     */
    public double getX() {
        return x;
    }

    /**
     * Gets the Y coordinate of this object's position.
     *
     * @return the Y coordinate in pixels
     */
    public double getY() {
        return y;
    }

    /**
     * Gets the width of this object.
     *
     * @return the width in pixels
     */
    public double getWidth() {
        return width;
    }

    /**
     * Gets the height of this object.
     *
     * @return the height in pixels
     */
    public double getHeight() {
        return height;
    }

    // ==================== Setters ====================

    /**
     * Sets the X coordinate of this object's position.
     *
     * @param x the new X coordinate in pixels
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Sets the Y coordinate of this object's position.
     *
     * @param y the new Y coordinate in pixels
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Sets the width of this object.
     *
     * @param width the new width in pixels
     */
    public void setWidth(double width) {
        this.width = width;
    }

    /**
     * Sets the height of this object.
     *
     * @param height the new height in pixels
     */
    public void setHeight(double height) {
        this.height = height;
    }
}
