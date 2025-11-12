package com.ooparkanoid.core.score;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Repository for managing local high score persistence.
 * Provides functionality to load, save, and maintain a leaderboard of top scores.
 *
 * Features:
 * - Persistent storage in user's home directory (.arkanoid_highscores.csv)
 * - Automatic sorting by score (highest first)
 * - Limited to top 10 entries to prevent file bloat
 * - Thread-safe operations with synchronized methods
 * - Graceful error handling with fallback behavior
 *
 * File Format: CSV with one score entry per line
 * Storage Location: ~/.arkanoid_highscores.csv
 *
 * Thread Safety: All public methods are synchronized for concurrent access safety.
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public final class HighScoreRepository {
    /** Maximum number of high score entries to maintain */
    private static final int MAX_ENTRIES = 10;

    /** Path to the high scores file in user's home directory */
    private static final Path SCORE_FILE = Paths.get(System.getProperty("user.home"), ".arkanoid_highscores.csv");

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private HighScoreRepository() {
    }

    /**
     * Loads all high score entries from persistent storage.
     * Returns an empty list if no scores file exists.
     * Automatically sorts entries by score (highest first) and limits to MAX_ENTRIES.
     *
     * @return list of ScoreEntry objects, sorted by score descending, limited to MAX_ENTRIES
     */
    public static synchronized List<ScoreEntry> loadScores() {
        List<ScoreEntry> entries = new ArrayList<>();
        if (!Files.exists(SCORE_FILE)) {
            return entries;
        }
        try (BufferedReader reader = Files.newBufferedReader(SCORE_FILE)) {
            String line;
            while ((line = reader.readLine()) != null) {
                ScoreEntry entry = ScoreEntry.fromLine(line.trim());
                if (entry != null) {
                    entries.add(entry);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.sort(entries);
        if (entries.size() > MAX_ENTRIES) {
            return new ArrayList<>(entries.subList(0, MAX_ENTRIES));
        }
        return entries;
    }

    /**
     * Records a new score entry to the high scores list.
     * Adds the entry, sorts the list, and saves only the top MAX_ENTRIES entries.
     * If the new score doesn't make the top 10, it will be discarded.
     *
     * @param entry the score entry to record (must not be null)
     */
    public static synchronized void recordScore(ScoreEntry entry) {
        List<ScoreEntry> entries = loadScores();
        entries.add(entry);
        Collections.sort(entries);
        if (entries.size() > MAX_ENTRIES) {
            entries = new ArrayList<>(entries.subList(0, MAX_ENTRIES));
        }
        save(entries);
    }

    /**
     * Saves a list of score entries to persistent storage.
     * Creates parent directories if they don't exist.
     * Overwrites existing file with new sorted entries.
     *
     * @param entries the list of score entries to save (should be pre-sorted and limited)
     */
    private static void save(List<ScoreEntry> entries) {
        try {
            if (SCORE_FILE.getParent() != null) {
                Files.createDirectories(SCORE_FILE.getParent());
            }
            try (BufferedWriter writer = Files.newBufferedWriter(SCORE_FILE)) {
                for (ScoreEntry entry : entries) {
                    writer.write(entry.toLine());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}