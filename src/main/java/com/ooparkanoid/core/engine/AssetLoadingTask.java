package com.ooparkanoid.core.engine;

import com.ooparkanoid.graphics.ResourceManager;
import com.ooparkanoid.sound.SoundManager;
import javafx.concurrent.Task;

import java.util.HashMap;
import java.util.Map;

/**
 * Một luồng chuyên dụng để tải TẤT CẢ tài nguyên game
 * hình ảnh, âm thanh mà không làm "đơ" giao diện chính.
 */
public class AssetLoadingTask extends Task<Void> {
    private static final long MIN_LOAD_TIME_MS = 2000;
    // Danh sách TẤT CẢ các file hình ảnh
    private static final String[] IMAGES_TO_LOAD = {
            // Paddle
            "paddle1.png", "laser_gun.png", "paddle2.png", "paddle3.png",
            "paddle_spawn.png", "paddle_explosion.png",
            // Ball
            "ball.png", "ball_fire.png",

            // Bricks
            "brick_normal.png", "brick_normal2.png",
            "brick_strong_hit1.png", "brick_strong_hit2.png", "brick_strong_hit3.png",
            "brick_enternal.png", "brick_flicker1.png", "brick_flicker2.png",
            "brick_explosive.png",
            // khác
            "powerup_sheet.png", "laser.png", "google-icon.png", "google-icon1.png"

    };

    // Danh sách TẤT CẢ các file âm thanh
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


    @Override
    protected Void call() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        ResourceManager rm = ResourceManager.getInstance();
        SoundManager sm = SoundManager.getInstance();

        int totalAssets = IMAGES_TO_LOAD.length + SOUNDS_TO_LOAD.size();
        int assetsLoaded = 0;

        // Tải hình ảnh
        updateMessage("Đang tải hình ảnh...");
        for (String img : IMAGES_TO_LOAD) {
            if (isCancelled()) break;
            rm.loadImage(img);
            assetsLoaded++;
            updateMessage("Đang tải: " + img);
            updateProgress(assetsLoaded, totalAssets);
        }

        updateMessage("Đang tải âm thanh...");
        for (Map.Entry<String, String> entry : SOUNDS_TO_LOAD.entrySet()) {
            if (isCancelled()) break;
            String name = entry.getKey();
            String path = entry.getValue();
            sm.loadSound(name, path);       // load sound
            assetsLoaded++;
            updateMessage("Đang tải: " + name);
            updateProgress(assetsLoaded, totalAssets);
        }

        updateMessage("Tải tài nguyên hoàn tất!");
        updateProgress(totalAssets, totalAssets);

        long timeElapsed = System.currentTimeMillis() - startTime;
        if (timeElapsed < MIN_LOAD_TIME_MS) {
            long timeToWait = MIN_LOAD_TIME_MS - timeElapsed;
            Thread.sleep(timeToWait);
        }
        return null;
    }
}