package com.ooparkanoid.core.save;

import java.io.*;
import java.nio.file.*;
import java.util.Optional;
import java.util.Properties;

/**
 * Service for managing game save/load operations.
 * Provides functionality to persist and restore game state between sessions.
 *
 * Save Format:
 * - Uses Java Properties file format (.properties)
 * - Stored in user's home directory as .arkanoid_save.properties
 * - Contains game state snapshot (level, score, lives, ball/paddle positions)
 *
 * Features:
 * - Automatic save file creation in user home directory
 * - Optional-based load mechanism (handles missing files gracefully)
 * - Property-based serialization (human-readable format)
 *
 * Usage Example:
 * <pre>
 * // Save game state
 * GameSnapshot snapshot = new GameSnapshot();
 * snapshot.level = 3;
 * snapshot.score = 1500;
 * SaveService.save(snapshot);
 *
 * // Load game state
 * Optional&lt;GameSnapshot&gt; loaded = SaveService.load();
 * loaded.ifPresent(state -> restoreGame(state));
 * </pre>
 *
 * Thread Safety: Not thread-safe. Concurrent save/load operations may cause data corruption.
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public final class SaveService {
    /** Path to the save file in user's home directory */
    private static final Path SAVE_FILE = Paths.get(System.getProperty("user.home"), ".arkanoid_save.properties");

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private SaveService(){}

    /**
     * Checks if a save file exists.
     *
     * @return true if save file exists, false otherwise
     */
    public static boolean exists() {
        return Files.exists(SAVE_FILE);
    }

    /**
     * Deletes the save file if it exists.
     * Useful for clearing game progress or resetting to fresh state.
     * Silently ignores if file doesn't exist or deletion fails.
     */
    public static void deleteIfExists() {
        try {
            Files.deleteIfExists(SAVE_FILE);
        } catch (IOException ignored) {}
    }

    /**
     * Saves a game state snapshot to persistent storage.
     * Creates parent directories if they don't exist.
     * Overwrites existing save file if present.
     *
     * Saved Properties:
     * - level: Current level number
     * - score: Current score
     * - lives: Remaining lives
     * - ballX, ballY: Ball position coordinates
     * - ballDX, ballDY: Ball velocity components
     * - paddleX: Paddle X position
     *
     * @param s the game snapshot to save
     * @throws RuntimeException if save fails (prints stack trace and continues)
     */
    public static void save(GameSnapshot s) {
        try {
            Properties p = new Properties();
            p.setProperty("level", String.valueOf(s.level));
            p.setProperty("score", String.valueOf(s.score));
            p.setProperty("lives", String.valueOf(s.lives));
            p.setProperty("ballX", String.valueOf(s.ballX));
            p.setProperty("ballY", String.valueOf(s.ballY));
            p.setProperty("ballDX", String.valueOf(s.ballDX));
            p.setProperty("ballDY", String.valueOf(s.ballDY));
            p.setProperty("paddleX", String.valueOf(s.paddleX));

            Files.createDirectories(SAVE_FILE.getParent());
            try (OutputStream os = Files.newOutputStream(SAVE_FILE)) {
                p.store(os, "Arkanoid save");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads a game state snapshot from persistent storage.
     * Returns empty Optional if save file doesn't exist or loading fails.
     * Uses default values for missing properties (level=1, score=0, lives=3, etc.).
     *
     * @return Optional containing loaded GameSnapshot, or empty if load fails
     */
    public static Optional<GameSnapshot> load() {
        if (!exists()) return Optional.empty();

        try (InputStream is = Files.newInputStream(SAVE_FILE)) {
            Properties p = new Properties();
            p.load(is);

            GameSnapshot s = new GameSnapshot();
            s.level = Integer.parseInt(p.getProperty("level", "1"));
            s.score = Integer.parseInt(p.getProperty("score", "0"));
            s.lives = Integer.parseInt(p.getProperty("lives", "3"));
            s.ballX = Double.parseDouble(p.getProperty("ballX", "0"));
            s.ballY = Double.parseDouble(p.getProperty("ballY", "0"));
            s.ballDX = Double.parseDouble(p.getProperty("ballDX", "0"));
            s.ballDY = Double.parseDouble(p.getProperty("ballDY", "0"));
            s.paddleX = Double.parseDouble(p.getProperty("paddleX", "0"));

            return Optional.of(s);
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Represents a snapshot of game state at a specific point in time.
     * Contains all essential data needed to restore a game session.
     *
     * Fields are public for easy access (this is a simple data transfer object).
     * Consider adding more fields as game features expand (power-ups, time, etc.).
     */
    public static class GameSnapshot {
        /** Current level number (1-based) */
        public int level;

        /** Current player score */
        public int score;

        /** Remaining lives */
        public int lives;

        /** Ball X coordinate */
        public double ballX;

        /** Ball Y coordinate */
        public double ballY;

        /** Ball X velocity component */
        public double ballDX;

        /** Ball Y velocity component */
        public double ballDY;

        /** Paddle X position */
        public double paddleX;
    }
}
