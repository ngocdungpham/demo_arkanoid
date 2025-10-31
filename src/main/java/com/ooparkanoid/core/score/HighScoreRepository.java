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
 * Đọc/ghi bảng xếp hạng thành tích cao nhất.
 */
public final class HighScoreRepository {
    private static final int MAX_ENTRIES = 10;
    private static final Path SCORE_FILE = Paths.get(System.getProperty("user.home"), ".arkanoid_highscores.csv");

    private HighScoreRepository() {
    }

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

    public static synchronized void recordScore(ScoreEntry entry) {
        List<ScoreEntry> entries = loadScores();
        entries.add(entry);
        Collections.sort(entries);
        if (entries.size() > MAX_ENTRIES) {
            entries = new ArrayList<>(entries.subList(0, MAX_ENTRIES));
        }
        save(entries);
    }

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