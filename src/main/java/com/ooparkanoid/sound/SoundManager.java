package com.ooparkanoid.sound;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Manages all sound effects and background music for the game.
 * Features:
 * - Thread pool to avoid blocking the main JavaFX thread
 * - Prevents sound spam by limiting concurrent playback
 * - Configurable volume control for SFX and music
 * - Singleton pattern for global access
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public class SoundManager {
    private static SoundManager instance;

    private Map<String, AudioClip> soundEffects;
    private Map<String, Long> lastPlayTime; // Tracks last play time to prevent spam
    private Map<String, Integer> playCount;  // Counts concurrent playback of same sound
    private MediaPlayer musicPlayer;

    // Thread pool for asynchronous sound playback
    private ExecutorService soundExecutor;
    // Scheduler for delayed tasks (e.g., resetting play count, restoring volume)
    private final ScheduledExecutorService scheduler;

    // Sound playback settings
    private static final long MIN_PLAY_INTERVAL = 150; // Minimum milliseconds between same sound plays
    private static final int MAX_CONCURRENT_SAME_SOUND = 1;
    private double sfxVolume = 1.0;
    private double musicVolume = 0.5;
    private boolean sfxEnabled = true;
    private boolean musicEnabled = true;

    /**
     * Private constructor for singleton pattern.
     * Initializes thread pools and data structures.
     */
    private SoundManager() {
        soundEffects = new HashMap<>();
        lastPlayTime = new HashMap<>();
        playCount = new HashMap<>();

        // Create thread pool with 4 worker threads for sound playback
        soundExecutor = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true); // Daemon threads automatically terminate when app closes
            t.setName("SoundManager-Thread");
            return t;
        });
        // Scheduler for delayed operations
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("SoundManager-Scheduler");
            return t;
        });
    }

    /**
     * Gets the singleton instance of SoundManager.
     *
     * @return the singleton SoundManager instance
     */
    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    public void init() {
        // Skip if sounds are already loaded
        if (!soundEffects.isEmpty()) {
            return;
        }

        // Load all sound effects
        loadSound("bounce", "/sounds/paddle.mp3");
        loadSound("break", "/sounds/break.mp3");
        loadSound("powerup", "/sounds/powerup.wav");
        loadSound("lose_life", "/sounds/lose_life.wav");
        loadSound("transition", "/sounds/transition.mp3");
        loadSound("card_transition", "/sounds/card_transition.mp3");
        loadSound("selected", "/sounds/selected.mp3");
        loadSound("pause", "/sounds/pause.mp3");
        loadSound("collision", "/sounds/collision.mp3");
        loadSound("laser_shoot", "/sounds/laser_shoot.wav");
        loadSound("laser_hit", "/sounds/laser_hit.wav");
        loadSound("collision", "/sounds/collision.mp3");
        System.out.println("Loaded " + soundEffects.size() + " sound effects");
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

    /**
     * Plays a sound effect asynchronously with spam protection.
     * Prevents the same sound from playing too frequently or too many times concurrently.
     *
     * @param name the sound identifier to play
     */
    public void play(String name) {
        if (!sfxEnabled) return;

        AudioClip clip = soundEffects.get(name);
        if (clip == null) {
            System.err.println("Sound not found: " + name);
            return;
        }

        // Prevent sound spam - check minimum interval between plays
        long currentTime = System.currentTimeMillis();
        long lastTime = lastPlayTime.getOrDefault(name, 0L);

        if (currentTime - lastTime < MIN_PLAY_INTERVAL) {
            return;
        }

        // Check concurrent playback limit
        int currentCount = playCount.getOrDefault(name, 0);
        if (currentCount >= MAX_CONCURRENT_SAME_SOUND) {
            // Already at max concurrent plays, skip
            return;
        }

        // Update tracking data
        lastPlayTime.put(name, currentTime);
        playCount.put(name, currentCount + 1);

        // Play sound on thread pool - avoids blocking main thread
        soundExecutor.submit(() -> {
            try {
                clip.play();
                // Decrement count after playback completes
                // AudioClip.play() is non-blocking, so we need a delay
                scheduler.schedule(() -> {
                    synchronized (playCount) {
                        int count = playCount.getOrDefault(name, 0);
                        playCount.put(name, Math.max(0, count - 1)); // Decrement, never go negative
                    }
                }, 100, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                System.err.println("Error playing sound: " + name);
            }
        });
    }

    /**
     * Plays a sound effect with custom volume.
     *
     * @param name   the sound identifier to play
     * @param volume custom volume level (0.0 to 1.0)
     */
    public void play(String name, double volume) {
        if (!sfxEnabled) return;

        AudioClip clip = soundEffects.get(name);
        if (clip == null) return;

        // Save original volume level
        double originalVolume = clip.getVolume();
        clip.setVolume(volume * sfxVolume);
        play(name);

        // Restore original volume after playback
        scheduler.schedule(() -> {
            try {
                clip.setVolume(originalVolume);
            } catch (Exception e) {
                // Ignore - clip may have been cleared
            }
        }, 300, TimeUnit.MILLISECONDS);
    }

    /**
     * Plays a sound effect immediately, bypassing spam protection.
     * Use this for critical sounds that must always play.
     *
     * @param name the sound identifier to play
     */
    public void playForce(String name) {
        if (!sfxEnabled) return;

        AudioClip clip = soundEffects.get(name);
        if (clip == null) return;

        soundExecutor.submit(() -> clip.play());
    }

    /**
     * Plays background music on loop.
     * Stops any currently playing music before starting the new track.
     *
     * @param filename the music file name (e.g., "intro.mp3")
     */
    public void playMusic(String filename) {
        if (!musicEnabled) return;

        // Stop and dispose of current music
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

    /**
     * Stops the currently playing background music.
     */
    public void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
        }
    }

    /**
     * Pauses the currently playing background music.
     */
    public void pauseMusic() {
        if (musicPlayer != null) {
            musicPlayer.pause();
        }
    }

    /**
     * Resumes paused background music.
     */
    public void resumeMusic() {
        if (musicPlayer != null && musicEnabled) {
            musicPlayer.play();
        }
    }

    // ==================== Volume Control ====================

    /**
     * Sets the volume level for all sound effects.
     * Updates the volume of all currently loaded sound clips.
     *
     * @param volume volume level (0.0 to 1.0), will be clamped to valid range
     */
    public void setSfxVolume(double volume) {
        this.sfxVolume = Math.max(0, Math.min(1, volume));
        // Update all loaded sound clips
        for (AudioClip clip : soundEffects.values()) {
            clip.setVolume(this.sfxVolume);
        }
    }

    /**
     * Sets the volume level for background music.
     *
     * @param volume volume level (0.0 to 1.0), will be clamped to valid range
     */
    public void setMusicVolume(double volume) {
        this.musicVolume = Math.max(0, Math.min(1, volume));
        if (musicPlayer != null) {
            musicPlayer.setVolume(this.musicVolume);
        }
    }

    /**
     * Gets the current sound effects volume level.
     *
     * @return volume level (0.0 to 1.0)
     */
    public double getSfxVolume() {
        return sfxVolume;
    }

    /**
     * Gets the current music volume level.
     *
     * @return volume level (0.0 to 1.0)
     */
    public double getMusicVolume() {
        return musicVolume;
    }

    // ==================== Enable/Disable ====================

    /**
     * Enables or disables all sound effects.
     *
     * @param enabled true to enable, false to disable
     */
    public void setSfxEnabled(boolean enabled) {
        this.sfxEnabled = enabled;
    }

    /**
     * Enables or disables background music.
     * Stops music immediately if disabled.
     *
     * @param enabled true to enable, false to disable
     */
    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (!enabled) {
            stopMusic();
        }
    }

    /**
     * Checks if sound effects are enabled.
     *
     * @return true if enabled, false otherwise
     */
    public boolean isSfxEnabled() {
        return sfxEnabled;
    }

    /**
     * Checks if background music is enabled.
     *
     * @return true if enabled, false otherwise
     */

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    // ==================== Cleanup ====================

    /**
     * Shuts down the SoundManager and releases all resources.
     * Stops all sounds, disposes of the music player, and terminates thread pools.
     * Should be called when the application is closing.
     */
    public void shutdown() {
        // Stop and dispose of music player
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
