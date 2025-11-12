package com.ooparkanoid.core.state;

/**
 * Represents the high-level states of the Arkanoid game application.
 * Defines the different screens and operational modes that the game can be in.
 * Used throughout the application to control UI rendering, input handling, and game logic flow.
 *
 * State Transitions:
 * - MENU → MODE_SELECT → RUNNING
 * - RUNNING → PAUSED (and back)
 * - RUNNING → GAME_OVER
 * - Any state → HOW_TO_PLAY → back to previous state
 * - Any state → INFORMATION → back to previous state
 *
 * Note: PAUSED and PAUSE appear to be duplicate states - consider consolidating.
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public enum GameState {
    /**
     * Main menu screen with game logo and navigation options.
     * Displays start game, settings, credits, and exit options.
     * User can navigate to MODE_SELECT or other screens from here.
     */
    MENU,

    /**
     * Active gameplay state where the game is running.
     * Ball is in motion, player controls paddle, collision detection active.
     * Transitions to PAUSED when paused, GAME_OVER when player loses.
     */
    RUNNING,

    /**
     * Game is temporarily paused by player action.
     * Time stops, ball freezes, player can resume or return to menu.
     * Note: Duplicate of PAUSE - consider removing one.
     */
    PAUSED,

    /**
     * Game over screen displayed when player loses all lives.
     * Shows final score, statistics, and options to restart or return to menu.
     * No active gameplay in this state.
     */
    GAME_OVER,

    /**
     * Game mode selection screen.
     * Player chooses between ADVENTURE, LOCAL_BATTLE, or other game modes.
     * Transitions to RUNNING once mode is selected and game starts.
     */
    MODE_SELECT,

    /**
     * Instructions/help screen explaining game controls and rules.
     * Modal screen that can be accessed from any state.
     * Returns to previous state when dismissed.
     */
    HOW_TO_PLAY,

    /**
     * Information/credits screen showing game credits and version info.
     * Modal screen that can be accessed from any state.
     * Returns to previous state when dismissed.
     */
    INFORMATION,

    /**
     * Alternative pause state (duplicate of PAUSED).
     * Consider consolidating with PAUSED for cleaner state management.
     * Provides pause menu with resume, restart, and menu options.
     */
    PAUSE
}