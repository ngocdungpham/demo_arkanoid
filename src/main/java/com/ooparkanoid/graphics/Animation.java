package com.ooparkanoid.graphics;

import javafx.scene.image.Image;

/**
 * Represents a sequence of images that can be played as an animation.
 * Manages frame timing, looping behavior, and provides access to current frame.
 *
 * Features:
 * - Frame-based animation with configurable timing
 * - Looping and one-shot playback modes
 * - Automatic frame advancement based on delta time
 * - Reset capability to restart animation
 * - Finished state tracking for one-shot animations
 *
 * Usage:
 * Create with array of frames, frame duration, and loop flag.
 * Call update() each frame with delta time, then getCurrentFrame() for rendering.
 * Use reset() to restart animation, isFinished() to check completion.
 *
 * Thread Safety: Not thread-safe. Should be accessed from single thread.
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public class Animation {
    /** Array of animation frames in playback order */
    private Image[] frames;

    /** Index of currently displayed frame */
    private int currentFrame = 0;

    /** Time duration each frame should be displayed (seconds) */
    private double frameDuration;

    /** Accumulated time since last frame change */
    private double timer = 0;

    /** Whether animation should loop when reaching the end */
    private boolean loop;

    /** Whether one-shot animation has completed */
    private boolean finished = false;

    /**
     * Constructs an Animation with specified frames, timing, and loop behavior.
     *
     * @param frames array of images representing animation frames in order
     * @param frameDuration time each frame should be displayed in seconds
     * @param loop true for looping animation, false for one-shot playback
     */
    public Animation(Image[] frames, double frameDuration, boolean loop) {
        this.frames = frames;
        this.frameDuration = frameDuration;
        this.loop = loop;
    }

    /**
     * Updates the animation state based on elapsed time.
     * Advances to next frame when frame duration is reached.
     * Handles looping and one-shot completion automatically.
     *
     * @param deltaTime time elapsed since last update in seconds
     */
    public void update(double deltaTime) {
        if (finished && !loop) return;
        timer += deltaTime;
        if (timer >= frameDuration) {
            timer -= frameDuration;
            currentFrame++;
            if (currentFrame >= frames.length) {
                if (loop) {
                    currentFrame = 0;
                } else {
                    currentFrame = frames.length - 1; // Stop at last frame
                    finished = true;
                }
            }
        }
    }

    /**
     * Gets the currently active animation frame for rendering.
     * Returns the image at the current frame index.
     *
     * @return the Image representing the current animation frame
     */
    public Image getCurrentFrame() {
        return frames[currentFrame];
    }

    /**
     * Resets the animation to its initial state.
     * Sets current frame to 0, resets timer, and clears finished flag.
     * Can be called to restart animation playback.
     */
    public void reset() {
        currentFrame = 0;
        timer = 0;
        finished = false;
    }

    /**
     * Checks if a one-shot animation has completed playback.
     * Always returns false for looping animations.
     *
     * @return true if animation is finished (one-shot only), false otherwise
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Sets the looping behavior of the animation.
     * Can be changed during playback to switch between loop and one-shot modes.
     *
     * @param loop true to enable looping, false for one-shot playback
     */
    public void setLoop(boolean loop) {
        this.loop = loop;
    }
}
