package com.ooparkanoid.sound;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Quản lý âm thanh với:
 * Thread pool để không block main thread
 * Giới hạn số lần phát cùng 1 sound
 * Volume control
 */
public class SoundManager {
    private static SoundManager instance;

    private Map<String, AudioClip> soundEffects;
    private Map<String, Long> lastPlayTime; // Track thời gian phát gần nhất
    private Map<String, Integer> playCount;  // Đếm số lần đang phát
    private MediaPlayer musicPlayer;

    // Thread pool cho sounds
    private ExecutorService soundExecutor;
    // Scheduler để xử lý delay (giảm playCount, restore volume, ...)
    private final ScheduledExecutorService scheduler;
    // Settings
    private static final long MIN_PLAY_INTERVAL = 150; // giữa các lần phát cùng sound
    private static final int MAX_CONCURRENT_SAME_SOUND = 1;
    private double sfxVolume = 1.0;
    private double musicVolume = 0.5;
    private boolean sfxEnabled = true;
    private boolean musicEnabled = true;

    private SoundManager() {
        soundEffects = new HashMap<>();
        lastPlayTime = new HashMap<>();
        playCount = new HashMap<>();

        // Tạo thread pool với 4 threads
        soundExecutor = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true); // Daemon thread tự động tắt khi app đóng
            t.setName("SoundManager-Thread");
            return t;
        });
        // scheduler chung (dùng cho delay)
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("SoundManager-Scheduler");
            return t;
        });

        loadAllSounds();
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    // Load tất cả sounds cần dùng vào bộ nhớ
    private void loadAllSounds() {
        // Sound effects
        loadSound("bounce", "/sounds/paddle.mp3");
        loadSound("break", "/sounds/break.mp3");
        loadSound("powerup", "/sounds/powerup.wav");
        loadSound("lose_life", "/sounds/lose_life.wav");
        loadSound("transition", "/sounds/transition.mp3");
        loadSound("card_transition", "/sounds/card_transition.mp3");
        loadSound("selected", "/sounds/selected.mp3");
        loadSound("pause", "/sounds/pause.mp3");
        loadSound("laser_shoot", "/sounds/laser_shoot.WAV");
        loadSound("laser_hit", "/sounds/laser_hit.wav");

        System.out.println("✅ Loaded " + soundEffects.size() + " sound effects");
    }

    // Load 1 sound file từ resources
    public void loadSound(String name, String path) {
        URL resource = getClass().getResource(path);
        if (resource == null) {
            System.err.println("Sound not found: " + path);
            return;
        }

        try {
            AudioClip clip = new AudioClip(resource.toExternalForm());
            clip.setVolume(sfxVolume);
            soundEffects.put(name, clip);
            lastPlayTime.put(name, 0L);
            playCount.put(name, 0);
        } catch (Exception e) {
            System.err.println("❌ Error loading sound: " + path + " - " + e.getMessage());
        }
    }

    // Phát sound effect (với giới hạn)
    public void play(String name) {
        if (!sfxEnabled) return;

        AudioClip clip = soundEffects.get(name);
        if (clip == null) {
            System.err.println("Sound not found: " + name);
            return;
        }

        // Check spam
        long currentTime = System.currentTimeMillis();
        long lastTime = lastPlayTime.getOrDefault(name, 0L);

        if (currentTime - lastTime < MIN_PLAY_INTERVAL) {
            return;
        }

        // Check Số lượng đang phát
        int currentCount = playCount.getOrDefault(name, 0);
        if (currentCount >= MAX_CONCURRENT_SAME_SOUND) {
            // Đã đủ, bỏ qua
            return;
        }

        // Update tracking
        lastPlayTime.put(name, currentTime);
        playCount.put(name, currentCount + 1);
        // Phát sound trên thread pool - không block main thread
        soundExecutor.submit(() -> {
            try {
                clip.play();
                // Giảm count sau khi phát xong
                // AudioClip.play() là non-blocking, nên cần delay
                scheduler.schedule(() -> {
                    synchronized (playCount) {
                        int count = playCount.getOrDefault(name, 0);
                        playCount.put(name, Math.max(0, count - 1)); // Giảm count, không để âm
                    }
                }, 300, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                System.err.println("Error playing sound: " + name);
            }
        });
    }

    /**
     * Phát sound với volume custom
     */
    public void play(String name, double volume) {
        if (!sfxEnabled) return;

        AudioClip clip = soundEffects.get(name);
        if (clip == null) return;

        // Lưu lại âm lượng gốc
        double originalVolume = clip.getVolume();
        clip.setVolume(volume * sfxVolume);
        play(name);

        // Restore volume sau khi phát
        scheduler.schedule(() -> {
            try {
                clip.setVolume(originalVolume);
            } catch (Exception e) {
                // Ignore
            }
        }, 300, TimeUnit.MILLISECONDS);
    }

    /**
     * Phát sound effect (bỏ qua giới hạn - dùng cho sounds quan trọng)
     */
    public void playForce(String name) {
        if (!sfxEnabled) return;

        AudioClip clip = soundEffects.get(name);
        if (clip == null) return;

        soundExecutor.submit(() -> clip.play());
    }

    // Phát nhạc nền
    public void playMusic(String filename) {
        if (!musicEnabled) return;

        // Dừng nhạc cũ
        if (musicPlayer != null) {
            musicPlayer.stop();
            musicPlayer.dispose();
        }

        URL resource = getClass().getResource("/sounds/" + filename);
        if (resource == null) {
            System.err.println("❌ Music not found: " + filename);
            return;
        }

        try {
            Media media = new Media(resource.toExternalForm());
            musicPlayer = new MediaPlayer(media);
            musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            musicPlayer.setVolume(musicVolume);
            musicPlayer.play();

            System.out.println("Playing music: " + filename);
        } catch (Exception e) {
            System.err.println("Error playing music: " + filename + " - " + e.getMessage());
        }
    }

    // Dừng nhạc nền
    public void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
        }
    }

    //  Pause/Resume nhạc
    public void pauseMusic() {
        if (musicPlayer != null) {
            musicPlayer.pause();
        }
    }

    public void resumeMusic() {
        if (musicPlayer != null && musicEnabled) {
            musicPlayer.play();
        }
    }

    // Volume control

    public void setSfxVolume(double volume) {
        this.sfxVolume = Math.max(0, Math.min(1, volume));
        // Update tất cả clips
        for (AudioClip clip : soundEffects.values()) {
            clip.setVolume(this.sfxVolume);
        }
    }

    public void setMusicVolume(double volume) {
        this.musicVolume = Math.max(0, Math.min(1, volume));
        if (musicPlayer != null) {
            musicPlayer.setVolume(this.musicVolume);
        }
    }

    public double getSfxVolume() {
        return sfxVolume;
    }

    public double getMusicVolume() {
        return musicVolume;
    }

    // Bật tắt âm thanh
    public void setSfxEnabled(boolean enabled) {
        this.sfxEnabled = enabled;
    }

    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (!enabled) {
            stopMusic();
        }
    }

    public boolean isSfxEnabled() {
        return sfxEnabled;
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    // cleanup

    // Dừng tất cả sounds và dispose resources
    public void shutdown() {
        // Stop music
        stopMusic();
        if (musicPlayer != null) {
            musicPlayer.dispose();
        }

        // Clear sounds
        soundEffects.clear();
        lastPlayTime.clear();
        playCount.clear();

        // Shutdown thread pool
        soundExecutor.shutdown();
        scheduler.shutdown();
        try {
            if (!soundExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                soundExecutor.shutdownNow();
            }
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            soundExecutor.shutdownNow();
            scheduler.shutdownNow();
        }

        System.out.println("SoundManager shutdown complete");
    }
}
