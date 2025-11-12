// File: src/main/java/com/ooparkanoid/object/Paddle.java
package com.ooparkanoid.object;

import com.ooparkanoid.graphics.Animation;
import com.ooparkanoid.graphics.ResourceManager;
import com.ooparkanoid.utils.Constants;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents the player-controlled paddle in the Arkanoid game.
 * The paddle can move horizontally or be positioned vertically on either side.
 * Supports laser shooting capability as a power-up feature.
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public class Paddle extends MovableObject {
    /**
     * Defines the orientation of the paddle.
     */
    public enum Orientation {
        /** Paddle moves horizontally at the bottom of the screen */
        HORIZONTAL,
        /** Paddle positioned vertically on the left side */
        VERTICAL_LEFT,
        /** Paddle positioned vertically on the right side */
        VERTICAL_RIGHT
    }

    // Sprite images for different paddle states
    private Image paddleSprite;
    private Image laserGunSprite;
    private Image paddleSpriteVerticalLeft;
    private Image paddleSpriteVerticalRight;

    // Paddle state
    private Orientation orientation = Orientation.HORIZONTAL;
    private Double lockedX = null; // For vertical orientation, locks X position

    // Laser shooting capability
    private boolean laserEnabled = false;
    private List<Laser> lasers = new ArrayList<>();
    private double shootCooldown = 0;
    private static final double SHOOT_DELAY = 0.3; // Seconds between laser shots

    // Movement boundaries
    private double boundLeft = Constants.PLAYFIELD_LEFT;
    private double boundRight = Constants.PLAYFIELD_RIGHT;
    private double boundTop = 0;
    private double boundBottom = Constants.HEIGHT;

    /**
     * Constructs a new Paddle at the specified position.
     *
     * @param x initial X coordinate
     * @param y initial Y coordinate
     */
    public Paddle(double x, double y) {
        super(x, y, Constants.PADDLE_WIDTH, Constants.PADDLE_HEIGHT, 0, 0);
        loadGraphics();
    }

    /**
     * Loads paddle sprites from the resource manager.
     * Uses fallback mechanism for missing vertical orientation sprites.
     */
    public void loadGraphics() {
        ResourceManager rm = ResourceManager.getInstance();
        paddleSprite = rm.getImage("paddle1.png");
        laserGunSprite = rm.getImage("laser_gun.png");
        paddleSpriteVerticalLeft = loadWithFallback(rm, "paddle_left.png", "paddle3.png");
        paddleSpriteVerticalRight = loadWithFallback(rm, "paddle_right.png", "paddle2.png");
    }

    /**
     * Loads an image with fallback support.
     * Attempts to load the preferred image first, falls back to alternative if unavailable.
     *
     * @param rm the ResourceManager instance
     * @param preferred the preferred image filename
     * @param fallback the fallback image filename
     * @return the loaded image, or null if both fail
     */
    private Image loadWithFallback(ResourceManager rm, String preferred, String fallback) {
        Image image = rm.getImage(preferred);
        if (image == null && fallback != null) {
            image = rm.getImage(fallback);
        }
        return image;
    }

    /**
     * Sets the horizontal velocity of the paddle.
     *
     * @param dx horizontal velocity in pixels per second
     */
    public void setDx(double dx) {
        this.dx = dx;
    }

    /**
     * Sets the vertical velocity of the paddle.
     *
     * @param dy vertical velocity in pixels per second
     */
    public void setDy(double dy) {
        this.dy = dy;
    }

    /**
     * Updates the paddle state for the current frame.
     * Handles movement, boundary checking, laser cooldown, and laser updates.
     *
     * @param dt delta time in seconds since last update
     */
    @Override
    public void update(double dt) {
        // Update position based on velocity and time
        move(dt);

        // Maintain locked position for vertical orientations
        if (lockedX != null) {
            x = lockedX;
        }

        // Update laser cooldown timer
        if (shootCooldown > 0) {
            shootCooldown -= dt;
        }

        // Update all active lasers and remove inactive ones
        Iterator<Laser> it = lasers.iterator();
        while (it.hasNext()) {
            Laser laser = it.next();
            laser.update(dt);
            if (!laser.isActive()) {
                it.remove();
            }
        }

        // Enforce movement boundaries
        if (lockedX == null) {
            if (x < boundLeft) {
                x = boundLeft;
            }
            if (x + width > boundRight) {
                x = boundRight - width;
            }
        }
        if (y < boundTop) {
            y = boundTop;
        }
        if (y + height > boundBottom) {
            y = boundBottom - height;
        }
    }

    /**
     * Sets the vertical movement boundaries for the paddle.
     *
     * @param top the top boundary Y coordinate
     * @param bottom the bottom boundary Y coordinate
     */
    public void setVerticalMovementBounds(double top, double bottom) {
        this.boundTop = top;
        this.boundBottom = bottom;
    }

    /**
     * Sets the horizontal movement boundaries for the paddle.
     *
     * @param left the left boundary X coordinate
     * @param right the right boundary X coordinate
     */
    public void setMovementBounds(double left, double right) {
        this.boundLeft = left;
        this.boundRight = right;
    }

    /**
     * Fires laser shots from the paddle if laser power-up is enabled.
     * Respects cooldown period between shots.
     */
    public void shootLaser() {
        if (!laserEnabled || shootCooldown > 0) {
            return;
        }
        double laserSpeed = 500;

        // Create two lasers, one from each side of the paddle
        Laser leftLaser = new Laser(x + width * 0.25, y - 25, laserSpeed);
        Laser rightLaser = new Laser(x + width * 0.75, y - 25, laserSpeed);
        lasers.add(leftLaser);
        lasers.add(rightLaser);
        shootCooldown = SHOOT_DELAY;
    }

    /**
     * Renders the paddle and its lasers to the graphics context.
     * Automatically selects the correct sprite based on current orientation.
     *
     * @param gc the GraphicsContext to render to
     */
    @Override
    public void render(GraphicsContext gc) {
        // Select sprite based on current orientation
        Image spriteToRender;
        switch (orientation) {
            case VERTICAL_LEFT -> spriteToRender = paddleSpriteVerticalLeft != null
                    ? paddleSpriteVerticalLeft
                    : paddleSprite;
            case VERTICAL_RIGHT -> spriteToRender = paddleSpriteVerticalRight != null
                    ? paddleSpriteVerticalRight
                    : paddleSprite;
            default -> spriteToRender = paddleSprite;
        }

        // Render paddle sprite or fallback to colored rectangle
        if (spriteToRender != null) {
            gc.drawImage(spriteToRender, x, y, width, height);
        } else {
            gc.setFill(Color.WHITE);
            gc.fillRect(x, y, width, height);
        }

        // Render laser guns if power-up is active
        if (laserEnabled) {
            renderLaserPaddle(gc);
        }

        // Render all active laser shots
        for (Laser laser : lasers) {
            laser.render(gc);
        }
    }

    /**
     * Renders the laser gun attachments on the paddle.
     * Shows visual indicators of laser readiness with glow effects.
     *
     * @param gc the GraphicsContext to render to
     */
    private void renderLaserPaddle(GraphicsContext gc) {
        // Draw laser gun sprites or fallback rectangles
        if (laserGunSprite != null) {
            gc.drawImage(laserGunSprite, x + width * 0.25 - 5, y - 15, 10, 15);
            gc.drawImage(laserGunSprite, x + width * 0.75 - 5, y - 15, 10, 15);
        } else {
            gc.setFill(Color.DARKGRAY);
            gc.fillRect(x + width * 0.25 - 3, y - 10, 6, 10);
            gc.fillRect(x + width * 0.75 - 3, y - 10, 6, 10);
        }

        // Glow effect when laser is ready to fire
        if (shootCooldown <= 0) {
            gc.setGlobalAlpha(0.5);
            gc.setFill(Color.CYAN);
            gc.fillRect(x + width * 0.25 - 4, y - 11, 8, 12);
            gc.fillRect(x + width * 0.75 - 4, y - 11, 8, 12);
            gc.setGlobalAlpha(1.0);
        }
    }

    /**
     * Enables or disables the laser shooting capability.
     * Clears all active lasers when disabled.
     *
     * @param enabled true to enable laser shooting, false to disable
     */
    public void setLaserEnabled(boolean enabled) {
        this.laserEnabled = enabled;
        if (!enabled) {
            lasers.clear();
        }
    }

    /**
     * Checks if laser shooting is currently enabled.
     *
     * @return true if laser is enabled, false otherwise
     */
    public boolean isLaserEnabled() {
        return laserEnabled;
    }

    /**
     * Gets the list of all active laser shots.
     *
     * @return list of Laser objects currently in flight
     */
    public List<Laser> getLasers() {
        return lasers;
    }

    /**
     * Sets the height of the paddle.
     *
     * @param height new height in pixels
     */
    public void setHeight(double height) {
        this.height = height;
    }

    /**
     * Sets the width of the paddle.
     *
     * @param width new width in pixels
     */
    public void setWidth(double width) {
        this.width = width;
    }

    /**
     * Sets the orientation of the paddle.
     * Automatically adjusts movement constraints based on orientation.
     *
     * @param orientation the new orientation (HORIZONTAL, VERTICAL_LEFT, or VERTICAL_RIGHT)
     */
    public void setOrientation(Orientation orientation) {
        this.orientation = orientation == null ? Orientation.HORIZONTAL : orientation;
        if (this.orientation != Orientation.HORIZONTAL) {
            this.dx = 0; // Stop horizontal movement for vertical orientations
        } else {
            unlockHorizontalPosition();
        }
    }

    /**
     * Gets the current orientation of the paddle.
     *
     * @return the current Orientation
     */
    public Orientation getOrientation() {
        return orientation;
    }

    /**
     * Locks the paddle to a specific horizontal position.
     * Used for vertical orientations to prevent horizontal drift.
     *
     * @param xPosition the X coordinate to lock to
     */
    public void lockHorizontalPosition(double xPosition) {
        this.lockedX = xPosition;
        this.x = xPosition;
        this.dx = 0;
    }

    /**
     * Unlocks the horizontal position, allowing free horizontal movement.
     * Typically called when switching back to horizontal orientation.
     */
    public void unlockHorizontalPosition() {
        this.lockedX = null;
    }
}