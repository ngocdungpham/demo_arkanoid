package com.ooparkanoid.core.score;

import java.util.Objects;

/**
 * Đại diện cho một thành tích được lưu lại trong bảng xếp hạng.
 */
public final class ScoreEntry implements Comparable<ScoreEntry> {
    private static final String DEFAULT_PLAYER_NAME = "Player";

    private final String playerName;
    private final int score;
    private final int roundsPlayed;
    private final double totalSeconds;

    public ScoreEntry(int score, int roundsPlayed, double totalSeconds) {
        this(null, score, roundsPlayed, totalSeconds);
    }

    public ScoreEntry(String playerName, int score, int roundsPlayed, double totalSeconds) {
        this.playerName = normaliseName(playerName);
        this.score = Math.max(0, score);
        this.roundsPlayed = Math.max(1, roundsPlayed);
        this.totalSeconds = Math.max(0.0, totalSeconds);
    }

    private static String normaliseName(String playerName) {
        if (playerName == null) {
            return DEFAULT_PLAYER_NAME;
        }
        String trimmed = playerName.trim();
        return trimmed.isEmpty() ? DEFAULT_PLAYER_NAME : trimmed;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getScore() {
        return score;
    }

    public int getRoundsPlayed() {
        return roundsPlayed;
    }

    public double getTotalSeconds() {
        return totalSeconds;
    }

    public double getAverageSecondsPerRound() {
        return roundsPlayed == 0 ? 0.0 : totalSeconds / roundsPlayed;
    }

    @Override
    public int compareTo(ScoreEntry other) {
        int scoreDiff = Integer.compare(other.score, this.score);
        if (scoreDiff != 0) {
            return scoreDiff;
        }
        return Double.compare(this.totalSeconds, other.totalSeconds);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScoreEntry that)) return false;
        return score == that.score
                && roundsPlayed == that.roundsPlayed
                && Double.compare(that.totalSeconds, totalSeconds) == 0
                && Objects.equals(playerName, that.playerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerName, score, roundsPlayed, totalSeconds);
    }

    @Override
    public String toString() {
        return "ScoreEntry{" +
                "playerName='" + playerName + '\'' +
                ", score=" + score +
                ", roundsPlayed=" + roundsPlayed +
                ", totalSeconds=" + totalSeconds +
                '}';
    }

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

    String toLine() {
        return playerName + ";" + score + ";" + roundsPlayed + ";" + totalSeconds;
    }
}