package com.ooparkanoid.object;

/**
 * Abstract base class for game objects that can move with velocity.
 * Extends GameObject to add velocity components (dx, dy) and automatic movement updates.
 * Provides foundation for dynamic game entities like balls, projectiles, and moving enemies.
 *
 * Features:
 * - Velocity-based movement with delta time integration
 * - Automatic position updates in game loop
 * - Velocity getters/setters for external control
 * - Time-based movement for smooth animation
 *
 * Movement System:
 * - dx: Velocity in pixels per second along X-axis (positive = right, negative = left)
 * - dy: Velocity in pixels per second along Y-axis (positive = down, negative = up)
 * - Position updated using: position += velocity * deltaTime
 *
 * Game Loop Integration:
 * - update(dt) automatically calls move(dt) for position updates
 * - Override update(dt) to add custom logic while preserving movement
 * - Override move(dt) for custom movement behavior (e.g., physics, constraints)
 *
 * Usage:
 * Extend this class for any game object that needs to move.
 * Set velocity with setDx()/setDy(), then let game loop handle updates.
 *
 * Thread Safety: Not thread-safe. Should be accessed from single game thread.
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public abstract class MovableObject extends GameObject {
    /** Velocity along X-axis in pixels per second (positive = right, negative = left) */
    protected double dx;

    /** Velocity along Y-axis in pixels per second (positive = down, negative = up) */
    protected double dy;

    /**
     * Constructs a MovableObject with specified position, dimensions, and initial velocity.
     *
     * @param x the X coordinate of the object's position
     * @param y the Y coordinate of the object's position
     * @param width the width of the object in pixels
     * @param height the height of the object in pixels
     * @param dx initial velocity along X-axis in pixels per second
     * @param dy initial velocity along Y-axis in pixels per second
     */
    public MovableObject(double x, double y, double width, double height, double dx, double dy) {
        super(x, y, width, height); // Call parent GameObject constructor
        this.dx = dx;
        this.dy = dy;
    }

    /**
     * Updates the object's position based on current velocity and elapsed time.
     * Uses time-based movement for smooth, frame-rate independent animation.
     * Called automatically by update(dt) but can be called directly for custom timing.
     *
     * @param deltaTime time elapsed since last movement update in seconds
     */
    public void move(double deltaTime) {
        x += dx * deltaTime;
        y += dy * deltaTime;
    }

    /**
     * Updates the movable object's state including automatic position movement.
     * Calls move(dt) to update position, then allows subclasses to add custom logic.
     * Override this method to add behavior while preserving movement updates.
     *
     * @param deltaTime time elapsed since last update in seconds
     */
    @Override
    public void update(double deltaTime) {
        move(deltaTime);
    }

    /**
     * Gets the current velocity along the X-axis.
     *
     * @return velocity in pixels per second (positive = right, negative = left)
     */
    public double getDx() {
        return dx;
    }

    /**
     * Gets the current velocity along the Y-axis.
     *
     * @return velocity in pixels per second (positive = down, negative = up)
     */
    public double getDy() {
        return dy;
    }

    /**
     * Sets the velocity along the X-axis.
     *
     * @param dx new velocity in pixels per second (positive = right, negative = left)
     */
    public void setDx(double dx) {
        this.dx = dx;
    }

    /**
     * Sets the velocity along the Y-axis.
     *
     * @param dy new velocity in pixels per second (positive = down, negative = up)
     */
    public void setDy(double dy) {
        this.dy = dy;
    }
}
