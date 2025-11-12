package com.ooparkanoid.core.state;

/**
 * Defines the available gameplay modes in Arkanoid.
 * Each mode represents a distinct gameplay experience with different objectives,
 * mechanics, and victory conditions.
 *
 * This enum is used throughout the application to configure game behavior,
 * UI elements, and scoring systems based on the selected mode.
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public enum GameMode {
    /**
     * Classic single-player adventure mode.
     * Player progresses through increasingly difficult levels by destroying all bricks.
     * Features: progressive difficulty, power-ups, scoring system, lives system.
     * Victory: Complete all levels or achieve high score.
     * Defeat: Lose all lives before completing levels.
     */
    ADVENTURE,

    /**
     * Competitive local multiplayer battle mode.
     * Two players compete on a split-screen field with vertical paddles.
     * Features: real-time competition, defense bars, brick destruction scoring.
     * Victory: Destroy all opponent's defense bars.
     * Defeat: Lose all defense bars to opponent.
     */
    LOCAL_BATTLE
}
