package com.ooparkanoid.core.engine;

import com.ooparkanoid.graphics.ResourceManager;
import com.ooparkanoid.sound.SoundManager;
import javafx.concurrent.Task;

import java.util.HashMap;
import java.util.Map;

/**
 * Background task for loading all game assets asynchronously.
 * Loads images and sounds without blocking the JavaFX UI thread.
 * Provides progress updates for display in a loading screen.
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public class AssetLoadingTask extends Task<Void> {
    /** Minimum loading time in milliseconds for smooth user experience */
    private static final long MIN_LOAD_TIME_MS = 2000;

    /** List of all image files to preload */
    private static final String[] IMAGES_TO_LOAD = {
            // Paddle sprites
            "paddle1.png", "laser_gun.png", "paddle2.png", "paddle3.png",

            // Ball sprites
            "paddle_spawn.png", "paddle_explosion.png",
            // Ball
            "ball.png", "ball_fire.png",

            // Brick sprites
            "brick_normal.png", "brick_normal2.png",
            "brick_strong_hit1.png", "brick_strong_hit2.png", "brick_strong_hit3.png",
            "brick_enternal.png", "brick_flicker1.png", "brick_flicker2.png",
            "brick_explosive.png",

            // Other sprites
            "powerup_sheet.png", "laser.png", "google-icon.png", "google-icon1.png"
    };

    /** Map of all sound effects to preload (name -> path) */
    private static final Map<String, String> SOUNDS_TO_LOAD = new HashMap<>();

    static {
        SOUNDS_TO_LOAD.put("bounce", "/sounds/paddle.mp3");
        SOUNDS_TO_LOAD.put("break", "/sounds/break.mp3");
        SOUNDS_TO_LOAD.put("powerup", "/sounds/powerup.wav");
        SOUNDS_TO_LOAD.put("lose_life", "/sounds/lose_life.wav");
        SOUNDS_TO_LOAD.put("transition", "/sounds/transition.mp3");
        SOUNDS_TO_LOAD.put("card_transition", "/sounds/card_transition.mp3");
        SOUNDS_TO_LOAD.put("selected", "/sounds/selected.mp3");
        SOUNDS_TO_LOAD.put("pause", "/sounds/pause.mp3");
        SOUNDS_TO_LOAD.put("laser_shoot", "/sounds/laser_shoot.wav");
        SOUNDS_TO_LOAD.put("laser_hit", "/sounds/laser_hit.wav");
        SOUNDS_TO_LOAD.put("menu", "/sounds/menu.mp3");
        SOUNDS_TO_LOAD.put("collision", "/sounds/collision.mp3");
    }

    /**
     * Executes the asset loading task in the background.
     * Updates progress and status messages during loading.
     *
     * @return null upon completion
     * @throws InterruptedException if the task is interrupted
     */
    @Override
    protected Void call() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        ResourceManager rm = ResourceManager.getInstance();
        SoundManager sm = SoundManager.getInstance();

        int totalAssets = IMAGES_TO_LOAD.length + SOUNDS_TO_LOAD.size();
        int assetsLoaded = 0;

        // Load all images
        updateMessage("Loading images...");
        for (String img : IMAGES_TO_LOAD) {
            if (isCancelled()) break;
            rm.loadImage(img);
            assetsLoaded++;
            updateMessage("Loading: " + img);
            updateProgress(assetsLoaded, totalAssets);
        }

        // Load all sounds
        updateMessage("Loading sounds...");
        sm.init(); // Initialize all sounds at once (idempotent)

        // Update progress bar for each sound (for visual feedback)
        for (String soundName : SOUNDS_TO_LOAD.keySet()) {
            if (isCancelled()) break;
            assetsLoaded++;
            updateMessage("Loading: " + soundName);
            updateProgress(assetsLoaded, totalAssets);
        }

        updateMessage("Asset loading complete!");
        updateProgress(totalAssets, totalAssets);

        // Ensure minimum loading time for smooth transition
        long timeElapsed = System.currentTimeMillis() - startTime;
        if (timeElapsed < MIN_LOAD_TIME_MS) {
            long timeToWait = MIN_LOAD_TIME_MS - timeElapsed;
            Thread.sleep(timeToWait);
        }
        return null;
    }
}