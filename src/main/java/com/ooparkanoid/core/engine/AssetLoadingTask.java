package com.ooparkanoid.core.engine;

import com.ooparkanoid.graphics.ResourceManager;
import com.ooparkanoid.sound.SoundManager;
import javafx.concurrent.Task;

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
    private static final String[] SOUNDS_TO_LOAD = {
            "background.mp3",
            "menu.mp3",
            "transition.wav",
            "bounce.wav",
            "break.wav",
            "lose_life.wav",
            "powerup.wav",
            "laser_hit.wav",
            "battle_victory.wav",
            "battle_defeat.wav"
    };


    @Override
    protected Void call() throws Exception {
        long startTime = System.currentTimeMillis();
        ResourceManager rm = ResourceManager.getInstance();
        SoundManager sm = SoundManager.getInstance();   // Tự load âm thanh khi khởi tạo

        int totalAssets = IMAGES_TO_LOAD.length + SOUNDS_TO_LOAD.length;
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

        updateMessage("Tải tài nguyên hoàn tất!");
        updateProgress(totalAssets, totalAssets);

        long timeElapsed = System.currentTimeMillis() - startTime;
        if (timeElapsed < MIN_LOAD_TIME_MS) {
            long timeToWait = MIN_LOAD_TIME_MS - timeElapsed;
            try {
                Thread.sleep(timeToWait);
            } catch (InterruptedException e) {
            }
        }
        return null;
    }
}