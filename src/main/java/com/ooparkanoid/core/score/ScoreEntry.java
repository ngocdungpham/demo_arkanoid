package com.ooparkanoid.core.score;

import java.util.Objects;

/**
 * Represents a score entry in the high score leaderboard.
 * Contains player information, score, game statistics, and timing data.
 *
 * Features:
 * - Immutable data structure (all fields final)
 * - Automatic name normalization and validation
 * - Score and rounds validation (non-negative)
 * - Natural ordering by score (highest first), then by time (lowest first)
 * - CSV serialization for persistent storage
 * - Comprehensive equals/hashCode implementation
 *
 * CSV Format: "playerName;score;roundsPlayed;totalSeconds"
 * Example: "JohnDoe;15000;5;120.5"
 *
 * Thread Safety: Immutable, thread-safe for all operations.
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public final class ScoreEntry implements Comparable<ScoreEntry> {
    /** Default player name when none provided or empty */
    private static final String DEFAULT_PLAYER_NAME = "Player";

    /** Player's display name (normalized, never null or empty) */
    private final String playerName;

    /** Final score achieved (non-negative) */
    private final int score;

    /** Number of rounds/levels completed (minimum 1) */
    private final int roundsPlayed;

    /** Total time spent in seconds (non-negative) */
    private final double totalSeconds;

    /**
     * Constructs a ScoreEntry with default player name.
     *
     * @param score final score achieved (will be clamped to >= 0)
     * @param roundsPlayed number of rounds completed (will be clamped to >= 1)
     * @param totalSeconds total time in seconds (will be clamped to >= 0.0)
     */
    public ScoreEntry(int score, int roundsPlayed, double totalSeconds) {
        this(null, score, roundsPlayed, totalSeconds);
    }

    /**
     * Constructs a ScoreEntry with specified player name.
     * Player name will be normalized (trimmed, default if empty/null).
     * All numeric values will be validated and clamped to valid ranges.
     *
     * @param playerName player's display name (null/empty will use default)
     * @param score final score achieved (will be clamped to >= 0)
     * @param roundsPlayed number of rounds completed (will be clamped to >= 1)
     * @param totalSeconds total time in seconds (will be clamped to >= 0.0)
     */
    public ScoreEntry(String playerName, int score, int roundsPlayed, double totalSeconds) {
        this.playerName = normaliseName(playerName);
        this.score = Math.max(0, score);
        this.roundsPlayed = Math.max(1, roundsPlayed);
        this.totalSeconds = Math.max(0.0, totalSeconds);
    }

    /**
     * Normalizes a player name by trimming whitespace and providing defaults.
     * Returns DEFAULT_PLAYER_NAME if input is null or empty after trimming.
     *
     * @param playerName raw player name input
     * @return normalized player name (never null or empty)
     */
    private static String normaliseName(String playerName) {
        if (playerName == null) {
            return DEFAULT_PLAYER_NAME;
        }
        String trimmed = playerName.trim();
        return trimmed.isEmpty() ? DEFAULT_PLAYER_NAME : trimmed;
    }

    /**
     * Gets the player's display name.
     *
     * @return normalized player name (never null or empty)
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Gets the final score achieved.
     *
     * @return score value (guaranteed >= 0)
     */
    public int getScore() {
        return score;
    }

    /**
     * Gets the number of rounds/levels completed.
     *
     * @return rounds played (guaranteed >= 1)
     */
    public int getRoundsPlayed() {
        return roundsPlayed;
    }

    /**
     * Gets the total time spent in seconds.
     *
     * @return total seconds (guaranteed >= 0.0)
     */
    public double getTotalSeconds() {
        return totalSeconds;
    }

    /**
     * Calculates the average time spent per round.
     *
     * @return average seconds per round, or 0.0 if roundsPlayed is 0
     */
    public double getAverageSecondsPerRound() {
        return roundsPlayed == 0 ? 0.0 : totalSeconds / roundsPlayed;
    }

    /**
     * Compares this ScoreEntry with another for ordering.
     * Primary sort: score descending (higher scores first)
     * Secondary sort: total time ascending (faster times first)
     *
     * @param other the ScoreEntry to compare with
     * @return negative if this > other, positive if this < other, 0 if equal
     */
    @Override
    public int compareTo(ScoreEntry other) {
        int scoreDiff = Integer.compare(other.score, this.score);
        if (scoreDiff != 0) {
            return scoreDiff;
        }
        return Double.compare(this.totalSeconds, other.totalSeconds);
    }

    /**
     * Checks equality with another object.
     * Two ScoreEntry objects are equal if all fields match exactly.
     *
     * @param o the object to compare with
     * @return true if objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScoreEntry that)) return false;
        return score == that.score
                && roundsPlayed == that.roundsPlayed
                && Double.compare(that.totalSeconds, totalSeconds) == 0
                && Objects.equals(playerName, that.playerName);
    }

    /**
     * Generates hash code based on all fields.
     * Consistent with equals() implementation.
     *
     * @return hash code for this ScoreEntry
     */
    @Override
    public int hashCode() {
        return Objects.hash(playerName, score, roundsPlayed, totalSeconds);
    }

    /**
     * Returns a string representation of this ScoreEntry.
     * Format: ScoreEntry{playerName='name', score=12345, roundsPlayed=3, totalSeconds=120.5}
     *
     * @return string representation for debugging/logging
     */
    @Override
    public String toString() {
        return "ScoreEntry{" +
                "playerName='" + playerName + '\'' +
                ", score=" + score +
                ", roundsPlayed=" + roundsPlayed +
                ", totalSeconds=" + totalSeconds +
                '}';
    }

    /**
     * Parses a ScoreEntry from a CSV line.
     * Supports both 4-field format (with name) and 3-field format (anonymous).
     * Returns null if parsing fails or format is invalid.
     *
     * Formats:
     * - "playerName;score;rounds;totalSeconds" (4 fields)
     * - "score;rounds;totalSeconds" (3 fields, uses default name)
     *
     * @param line CSV line to parse
     * @return parsed ScoreEntry, or null if invalid
     */
    static ScoreEntry fromLine(String line) {
        String[] parts = line.split(";");
        try {
            if (parts.length == 4) {
                String name = parts[0];
                int score = Integer.parseInt(parts[1]);
                int rounds = Integer.parseInt(parts[2]);
                double total = Double.parseDouble(parts[3]);
                return new ScoreEntry(name, score, rounds, total);
            } else if (parts.length == 3) {
                int score = Integer.parseInt(parts[0]);
                int rounds = Integer.parseInt(parts[1]);
                double total = Double.parseDouble(parts[2]);
                return new ScoreEntry(score, rounds, total);
            }
            return null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Serializes this ScoreEntry to CSV format.
     * Format: "playerName;score;roundsPlayed;totalSeconds"
     *
     * @return CSV representation of this entry
     */
    String toLine() {
        return playerName + ";" + score + ";" + roundsPlayed + ";" + totalSeconds;
    }
}