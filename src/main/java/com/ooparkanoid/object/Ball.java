package com.ooparkanoid.object;

import com.ooparkanoid.graphics.Animation;
import com.ooparkanoid.graphics.GlowTrail;
import com.ooparkanoid.graphics.ResourceManager;
import com.ooparkanoid.graphics.SpriteSheet;
import com.ooparkanoid.object.bricks.Brick;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Represents the game ball in Arkanoid.
 * The ball moves continuously, bounces off surfaces, and breaks bricks.
 * Supports visual effects including rotation, animation, glow effects, and trailing.
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public class Ball extends MovableObject {
    // Movement properties
    private double speed;
    private double dirX, dirY; // Normalized direction vector

    // Visual elements
    private Animation ballAnimation;
    private Image ballSprite;
    private boolean useAnimation = false;
    private double rotation = 0; // Current rotation angle in degrees

    // Trail effect
    private GlowTrail trail;
    private boolean showTrail = true;

    // Glow effect (activated by power-ups)
    private boolean hasGlow = false;
    private Color glowColor = Color.CYAN;

    // Physics
    private double radius;

    /**
     * Constructs a new Ball with specified properties.
     *
     * @param x initial X coordinate of ball center
     * @param y initial Y coordinate of ball center
     * @param radius ball radius in pixels
     * @param speed movement speed in pixels per second
     * @param dirX initial X direction component
     * @param dirY initial Y direction component
     */
    public Ball(double x, double y, double radius, double speed, double dirX, double dirY) {
        super(x - radius, y - radius, radius * 2, radius * 2, 0, 0);
        this.speed = speed;
        this.radius = radius;
        this.setDirection(dirX, dirY);

        loadGraphics();
        setupTrail();
    }

    /**
     * Loads ball graphics and animation.
     * Attempts to load animated sprite sheet, falls back to static image if unavailable.
     */
    private void loadGraphics() {
        ResourceManager resourceManager = ResourceManager.getInstance();
        ballSprite = resourceManager.getImage("ball.png");

        // Try to load animation sprite sheet
        SpriteSheet ballSheet = resourceManager.loadSpriteSheet("ball_animation.png", 32, 32);
        if (ballSheet != null) {
            // Extract all frames from sprite sheet
            Image[] frames = new Image[ballSheet.getFrameCount()];
            for (int i = 0; i < frames.length; i++) {
                frames[i] = ballSheet.getFrame(i);
            }
            ballAnimation = new Animation(frames, 0.05, true);
            useAnimation = true;
        } else {
            // Fallback: use static image if animation is not available
            useAnimation = false;
        }
    }

    /**
     * Initializes the visual trail effect for the ball.
     * Sets up default trail properties (color, length, intensity).
     */
    public void setupTrail() {
        trail = new GlowTrail(width);
        trail.setColor(Color.CYAN);
        trail.setMaxLength(30);
        trail.setGlowIntensity(1.0);
    }

    /**
     * Sets the ball's direction vector and updates velocity.
     * Automatically normalizes the direction vector.
     *
     * @param nx X component of direction
     * @param ny Y component of direction
     */
    public void setDirection(double nx, double ny) {
        double len = Math.sqrt(nx * nx + ny * ny);
        if (len == 0) return;

        // Normalize direction vector
        dirX = nx / len;
        dirY = ny / len;

        // Update velocity based on speed and direction
        this.dx = dirX * speed;
        this.dy = dirY * speed;
    }

    /**
     * Updates the ball state for the current frame.
     * Handles rotation animation, sprite animation, movement, and trail effects.
     *
     * @param deltaTime time elapsed since last update in seconds
     */
    @Override
    public void update(double deltaTime) {
        // Update rotation for spinning effect
        rotation += speed * deltaTime * 5;
        if (rotation >= 360) {
            rotation -= 360;
        }

        // Update sprite animation if available
        if (useAnimation && ballAnimation != null) {
            ballAnimation.update(deltaTime);
        }

        // Update position
        move(deltaTime);

        // Update trail effect
        if (showTrail && trail != null) {
            trail.addPoint(x + width / 2, y + height / 2);
            trail.update(deltaTime);
        }
    }

    /**
     * Renders the ball and its visual effects to the graphics context.
     * Draws trail, glow effect, and the ball sprite (animated or static).
     *
     * @param gc the GraphicsContext to render to
     */
    @Override
    public void render(GraphicsContext gc) {
        // Render trail effect behind ball
        if (showTrail && trail != null) {
            trail.render(gc);
        }

        // Render glow effect around ball
        if (hasGlow) {
            renderBallGlow(gc);
        }

        // Render ball with rotation
        gc.save();
        gc.translate(x + width / 2, y + height / 2);
        gc.rotate(rotation);

        if (useAnimation && ballAnimation != null) {
            // Render animated sprite
            Image frame = ballAnimation.getCurrentFrame();
            gc.drawImage(frame, -width / 2, -height / 2, width, height);
        } else if (ballSprite != null) {
            // Render static sprite
            gc.drawImage(ballSprite, -width / 2, -height / 2, width, height);
        } else {
            // Fallback: render procedural ball with gradient
            gc.setFill(Color.WHITE);
            gc.fillOval(-width / 2, -height / 2, width, height);

            // Inner circle (bright core)
            gc.setFill(Color.LIGHTBLUE);
            gc.fillOval(-width / 2 + 2, -height / 2 + 2, width - 4, height - 4);

            // Highlight (light reflection)
            gc.setGlobalAlpha(0.6);
            gc.setFill(Color.WHITE);
            gc.fillOval(-width / 4, -height / 4, width / 2, height / 2);
            gc.setGlobalAlpha(1.0);
        }
        gc.restore();
    }

    /**
     * Renders a glow effect around the ball.
     * Creates layered circular gradients for a halo effect.
     *
     * @param gc the GraphicsContext to render to
     */
    private void renderBallGlow(GraphicsContext gc) {
        double centerX = x + width / 2;
        double centerY = y + height / 2;

        // Outer glow layer (subtle)
        gc.setGlobalAlpha(0.1);
        gc.setFill(glowColor);
        gc.fillOval(centerX - width * 2, centerY - height * 2, width * 4, height * 4);

        // Inner glow layer (more visible)
        gc.setGlobalAlpha(0.2);
        gc.fillOval(centerX - width * 1.2, centerY - height * 1.2, width * 2.4, height * 2.4);
        gc.setGlobalAlpha(1.0);
    }

    // ==================== Power-up Visual Effects ====================

    /**
     * Activates the fast ball visual effect.
     * Red trail with increased length and intensity.
     */
    public void activateFastBallEffect() {
        trail.setColor(Color.RED);
        trail.setMaxLength(30);
        trail.setGlowIntensity(1.5);
        setGlow(true, Color.RED);
    }

    /**
     * Activates the slow ball visual effect.
     * Light blue trail with reduced length and intensity.
     */
    public void activateSlowBallEffect() {
        trail.setColor(Color.LIGHTBLUE);
        trail.setMaxLength(20);
        trail.setGlowIntensity(0.7);
        setGlow(true, Color.LIGHTBLUE);
    }

    /**
     * Activates the fire ball visual effect.
     * Orange-red trail with maximum length and very high intensity.
     */
    public void activateFireBallEffect() {
        trail.setColor(Color.ORANGERED);
        trail.setMaxLength(40);
        trail.setGlowIntensity(1.8);
        setGlow(true, Color.ORANGERED);
    }

    /**
     * Activates the invincible ball visual effect.
     * Golden trail with maximum intensity.
     */
    public void activateInvincibleEffect() {
        trail.setColor(Color.GOLD);
        trail.setMaxLength(30);
        trail.setGlowIntensity(2.0);
        setGlow(true, Color.GOLD);
    }

    /**
     * Resets all visual effects to default cyan trail.
     * Removes glow effect.
     */
    public void resetTrailEffect() {
        trail.setColor(Color.CYAN);
        trail.setMaxLength(30);
        trail.setGlowIntensity(1.0);
        setGlow(false, Color.CYAN);
    }

    // ==================== Trail and Glow Control ====================

    /**
     * Sets the color of the ball's trail effect.
     *
     * @param color the desired trail color
     */
    public void setTrailColor(Color color) {
        if (trail != null) {
            trail.setColor(color);
        }
    }

    /**
     * Enables or disables the glow effect around the ball.
     *
     * @param hasGlow true to enable glow, false to disable
     * @param color the glow color
     */
    public void setGlow(boolean hasGlow, Color color) {
        this.hasGlow = hasGlow;
        this.glowColor = color;
    }

    /**
     * Shows or hides the ball's trail effect.
     * Clears the trail when hiding.
     *
     * @param showTrail true to show trail, false to hide
     */
    public void setShowTrail(boolean showTrail) {
        this.showTrail = showTrail;
        if (!showTrail && trail != null) {
            trail.clear();
        }
    }

    /**
     * Clears all points from the ball's trail.
     */
    public void clearTrail() {
        if (trail != null) {
            trail.clear();
        }
    }

    // ==================== Speed and Direction Control ====================

    /**
     * Sets the ball's speed while maintaining its current direction.
     *
     * @param speed new speed in pixels per second
     */
    public void setSpeed(double speed) {
        this.speed = speed;
        this.dx = speed * dirX;
        this.dy = speed * dirY;
    }

    /**
     * Gets the current speed of the ball.
     *
     * @return speed in pixels per second
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Gets the X component of the ball's normalized direction vector.
     *
     * @return X direction component (-1.0 to 1.0)
     */
    public double getDirX() {
        return dirX;
    }

    /**
     * Sets the X component of the ball's direction vector.
     * Note: This should typically be used with setDirY to maintain proper normalization.
     *
     * @param dirX X direction component
     */
    public void setDirX(double dirX) {
        this.dirX = dirX;
    }

    /**
     * Gets the Y component of the ball's normalized direction vector.
     *
     * @return Y direction component (-1.0 to 1.0)
     */
    public double getDirY() {
        return dirY;
    }

    /**
     * Sets the Y component of the ball's direction vector.
     * Note: This should typically be used with setDirX to maintain proper normalization.
     *
     * @param dirY Y direction component
     */
    public void setDirY(double dirY) {
        this.dirY = dirY;
    }

    /**
     * Gets the radius of the ball.
     *
     * @return ball radius in pixels
     */
    public double getRadius() {
        return radius;
    }

    // ==================== Position and Velocity ====================

    /**
     * Sets the ball's position.
     *
     * @param ballX new X coordinate
     * @param ballY new Y coordinate
     */
    public void setPosition(double ballX, double ballY) {
        this.x = ballX;
        this.y = ballY;
    }

    /**
     * Sets the ball's velocity directly.
     *
     * @param ballDX velocity X component in pixels per second
     * @param ballDY velocity Y component in pixels per second
     */
    public void setVelocity(double ballDX, double ballDY) {
        this.dx = ballDX;
        this.dy = ballDY;
    }

    // ==================== Collision Detection ====================

    /**
     * Checks if the ball collides with a brick using circle-rectangle collision.
     * Uses the closest point algorithm for accurate circular collision detection.
     *
     * @param brick the brick to check collision with
     * @return true if the ball collides with the brick, false otherwise
     */
    public boolean collidesWith(Brick brick) {
        double circleCenterX = this.x + this.radius;
        double circleCenterY = this.y + this.radius;

        double rectX = brick.getX();
        double rectY = brick.getY();
        double rectWidth = brick.getWidth();
        double rectHeight = brick.getHeight();

        // Find the closest point on the rectangle to the circle center
        double closestX = clamp(circleCenterX, rectX, rectX + rectWidth);
        double closestY = clamp(circleCenterY, rectY, rectY + rectHeight);

        // Calculate distance from circle center to closest point
        double distX = circleCenterX - closestX;
        double distY = circleCenterY - closestY;
        double distanceSquared = (distX * distX) + (distY * distY);

        // Check if distance is less than radius (collision occurred)
        return distanceSquared < (this.radius * this.radius);
    }

    /**
     * Determines which side of the brick the ball collided with.
     * Used to calculate the proper bounce direction after collision.
     *
     * @param brick the brick that was collided with
     * @return "LEFT", "RIGHT", "TOP", or "BOTTOM" indicating collision side
     */
    public String getCollisionSide(Brick brick) {
        double ballCenterX = this.x + this.radius;
        double ballCenterY = this.y + this.radius;

        double brickCenterX = brick.getX() + brick.getWidth() / 2;
        double brickCenterY = brick.getY() + brick.getHeight() / 2;

        // Calculate overlap on both axes
        double overlapX = (this.radius + brick.getWidth() / 2)
                - Math.abs(ballCenterX - brickCenterX);
        double overlapY = (this.radius + brick.getHeight() / 2)
                - Math.abs(ballCenterY - brickCenterY);

        // Determine collision side based on which axis has less overlap
        if (overlapX < overlapY) {
            // Horizontal collision (left or right)
            return ballCenterX < brickCenterX ? "LEFT" : "RIGHT";
        } else {
            // Vertical collision (top or bottom)
            return ballCenterY < brickCenterY ? "TOP" : "BOTTOM";
        }
    }

    /**
     * Clamps a value between a minimum and maximum.
     * Utility method for collision detection calculations.
     *
     * @param value the value to clamp
     * @param min minimum allowed value
     * @param max maximum allowed value
     * @return the clamped value
     */
    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
