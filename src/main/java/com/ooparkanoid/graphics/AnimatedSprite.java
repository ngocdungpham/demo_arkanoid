package com.ooparkanoid.graphics;

import com.ooparkanoid.object.GameObject;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for animated game objects that can display multiple animations.
 * Extends GameObject to provide animation management, sprite flipping, and rendering capabilities.
 * <p>
 * Features:
 * - Multiple animation support with named animations
 * - Horizontal and vertical sprite flipping
 * - Position offset adjustments for precise sprite alignment
 * - Automatic animation switching and frame management
 * - Canvas-based rendering with transformation support
 * <p>
 * Animation Management:
 * - Stores animations in a Map with string keys for easy access
 * - Supports playing different animations by name
 * - Automatically resets animations when switching
 * - Updates animation frames based on delta time
 * <p>
 * Rendering:
 * - Handles sprite flipping through canvas transformations
 * - Applies position offsets for fine-tuned sprite positioning
 * - Preserves GraphicsContext state during rendering
 * <p>
 * Usage:
 * Extend this class to create specific animated game objects (e.g., characters, effects).
 * Add animations using addAnimation(), then control playback with playAnimation().
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public abstract class AnimatedSprite extends GameObject {
    /**
     * Map of animation names to their corresponding Animation objects
     */
    protected Map<String, Animation> animations = new HashMap<>();

    /**
     * Currently playing animation
     */
    protected Animation currentAnimation;

    /**
     * Name of the currently playing animation
     */
    protected String currentAnimationName;

    /**
     * Flag to flip sprite horizontally (left-right mirror)
     */
    protected boolean flipX = false;

    /**
     * Flag to flip sprite vertically (up-down mirror)
     */
    protected boolean flipY = false;

    /**
     * Horizontal offset for sprite positioning adjustment
     */
    protected double offsetX = 0;

    /**
     * Vertical offset for sprite positioning adjustment
     */
    protected double offsetY = 0;

    /**
     * Constructs an AnimatedSprite with specified position and dimensions.
     * Initializes the sprite with default state (no animations, no flipping, no offsets).
     *
     * @param x      the X coordinate of the sprite's position
     * @param y      the Y coordinate of the sprite's position
     * @param width  the width of the sprite in pixels
     * @param height the height of the sprite in pixels
     */
    public AnimatedSprite(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    /**
     * Adds a new animation to this sprite with the specified name.
     * If this is the first animation added, it will automatically start playing.
     *
     * @param name      the unique name identifier for this animation
     * @param animation the Animation object to add
     */
    public void addAnimation(String name, Animation animation) {
        animations.put(name, animation);
        if (currentAnimation == null) {
            playAnimation(name);
        }
    }

    /**
     * Starts playing the animation with the specified name.
     * If the animation is already playing, this method does nothing.
     * Resets the animation to its first frame when switching.
     *
     * @param name the name of the animation to play
     */
    public void playAnimation(String name) {
        if (currentAnimationName != null && currentAnimationName.equals(name)) {
            return; // Already playing this animation
        }

        // Set as current animation
        Animation animation = animations.get(name);
        if (animation != null) {
            currentAnimation = animation;
            currentAnimationName = name;
            animation.reset(); // Reset to first frame
        }
    }

    /**
     * Updates the sprite's animation state.
     * Advances the current animation frame based on elapsed time.
     *
     * @param deltaTime time elapsed since last update in seconds
     */
    @Override
    public void update(double deltaTime) {
        if (currentAnimation != null) {
            currentAnimation.update(deltaTime);
        }
    }

    /**
     * Renders the current animation frame to the graphics context.
     * Handles sprite flipping through canvas transformations and applies position offsets.
     * Preserves the GraphicsContext state to avoid affecting other rendering operations.
     *
     * @param gc the GraphicsContext to render to
     */
    @Override
    public void render(GraphicsContext gc) {
        if (currentAnimation == null) {
            return;
        }
        Image frame = currentAnimation.getCurrentFrame();
        if (frame == null) {
            return;
        }
        gc.save();
        // Apply flipping transformations if enabled
        if (flipX || flipY) {
            gc.translate(x + width / 2, y + height / 2);    // Move origin to sprite center
            gc.scale(flipX ? -1 : 1, flipY ? -1 : 1);       // Apply flip scaling
            // Draw from center with offsets
            gc.drawImage(frame, -width / 2 + offsetX, -height / 2 + offsetY, width, height);
        } else {
            gc.drawImage(frame, x + offsetX, y + offsetY, width, height);
        }
        gc.restore(); // Restore GraphicsContext to previous state
    }

    /**
     * Sets the horizontal flip state of the sprite.
     * When enabled, the sprite will be mirrored left-to-right.
     *
     * @param flipX true to enable horizontal flipping, false to disable
     */
    public void setFlipX(boolean flipX) {
        this.flipX = flipX;
    }

    /**
     * Sets the vertical flip state of the sprite.
     * When enabled, the sprite will be mirrored top-to-bottom.
     *
     * @param flipY true to enable vertical flipping, false to disable
     */
    public void setFlipY(boolean flipY) {
        this.flipY = flipY;
    }
}
