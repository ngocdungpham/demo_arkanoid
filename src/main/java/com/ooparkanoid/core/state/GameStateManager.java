package com.ooparkanoid.core.state;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Manages the overall game state and provides observable properties for UI binding.
 *
 * Responsibilities:
 * - Centralized game state management (menu, running, paused, game over)
 * - Observable properties for reactive UI updates (score, lives, timers)
 * - State transition logic with validation
 * - Continue/resume game functionality
 * - Status message broadcasting for UI notifications
 *
 * Observable Properties:
 * - currentState: Current GameState (MENU, RUNNING, PAUSED, GAME_OVER)
 * - score: Player's current score
 * - lives: Remaining lives
 * - continueAvailable: Whether player can continue from paused state
 * - statusMessage: Current status text for UI display
 * - roundTimeSeconds: Time elapsed in current round
 * - totalTimeSeconds: Total game time across all rounds
 * - currentRound: Current level/round number
 *
 * Thread Safety: Not thread-safe. Should be accessed only from JavaFX Application Thread.
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public class GameStateManager {
    /** Current game state (observable for UI binding) */
    private final ObjectProperty<GameState> currentState = new SimpleObjectProperty<>(GameState.MENU);

    /** Current player score (observable for UI binding) */
    private final IntegerProperty score = new SimpleIntegerProperty();

    /** Remaining lives (observable for UI binding) */
    private final IntegerProperty lives = new SimpleIntegerProperty();

    /** Flag indicating if player can continue from paused state (observable for UI binding) */
    private final BooleanProperty continueAvailable = new SimpleBooleanProperty(false);

    /** Status message displayed to player (observable for UI binding) */
    private final StringProperty statusMessage = new SimpleStringProperty("Welcome to Arkanoid!");

    /** Time elapsed in current round in seconds (observable for UI binding) */
    private final DoubleProperty roundTimeSeconds = new SimpleDoubleProperty();

    /** Total time elapsed across all rounds in seconds (observable for UI binding) */
    private final DoubleProperty totalTimeSeconds = new SimpleDoubleProperty();

    /** Current round/level number, 1-based (observable for UI binding) */
    private final IntegerProperty currentRound = new SimpleIntegerProperty(1);

    /**
     * Updates the score and lives displayed in the UI.
     * Triggers property change events for UI listeners.
     *
     * @param score new score value
     * @param lives new lives count
     */
    public void updateStats(int score, int lives) {
        this.score.set(score);
        this.lives.set(lives);
    }

    /**
     * Begins a completely new game session.
     * Resets state, initializes score/lives, and transitions to RUNNING state.
     * Clears continue availability to prevent resuming old session.
     *
     * @param initialScore starting score (typically 0)
     * @param initialLives starting lives count (typically 3)
     */
    public void beginNewGame(int initialScore, int initialLives) {
        updateStats(initialScore, initialLives);
        continueAvailable.set(false);
        statusMessage.set("Destroy all the bricks!");
        currentState.set(GameState.RUNNING);
    }

    /**
     * Updates the round and total time counters.
     * Used by game loop to update elapsed time displays in UI.
     *
     * @param roundSeconds time elapsed in current round
     * @param totalSeconds total time elapsed across all rounds
     */
    public void updateTimers(double roundSeconds, double totalSeconds) {
        this.roundTimeSeconds.set(roundSeconds);
        this.totalTimeSeconds.set(totalSeconds);
    }

    /**
     * Sets the current round/level number.
     * Updates the UI to reflect level progression.
     *
     * @param round the current round number (1-based)
     */
    public void setCurrentRound(int round) {
        this.currentRound.set(round);
    }

    /**
     * Displays the main menu without losing current game progress.
     * Continue option will remain available if game was previously paused.
     */
    public void showMenu() {
        currentState.set(GameState.MENU);
        statusMessage.set("Game menu");
    }

    /**
     * Resets to main menu and clears continue option.
     * Discards any saved game progress. Use when starting fresh.
     */
    public void resetToMenu() {
        continueAvailable.set(false);
        showMenu();
    }

    /**
     * Pauses the currently running game.
     * Only works if game is in RUNNING state. Sets continue flag to true.
     * Game loop should check isRunning() to stop updates.
     */
    public void pauseGame() {
        if (currentState.get() == GameState.RUNNING) {
            currentState.set(GameState.PAUSED);
            continueAvailable.set(true);
            statusMessage.set("Game paused");
        }
    }

    /**
     * Resumes a paused game.
     * Only works if game is in PAUSED state. Returns to RUNNING state.
     * Game loop will resume processing updates.
     */
    public void resumeGame() {
        if (currentState.get() == GameState.PAUSED) {
            currentState.set(GameState.RUNNING);
            statusMessage.set("Back to the action!");
        }
    }

    /**
     * Toggles between running and paused states.
     * Convenience method for pause/resume toggle button.
     */
    public void togglePause() {
        if (currentState.get() == GameState.RUNNING) {
            pauseGame();
        } else if (currentState.get() == GameState.PAUSED) {
            resumeGame();
        }
    }

    /**
     * Marks the game as over.
     * Transitions to GAME_OVER state and clears continue option.
     * Called when player loses all lives or completes all levels.
     */
    public void markGameOver() {
        continueAvailable.set(false);
        currentState.set(GameState.GAME_OVER);
        statusMessage.set("Game over");
    }

    /**
     * Marks the game as won.
     * Transitions to GAME_WON state and clears continue option.
     * Called when player completes all levels.
     */
    public void markGameWon() {
        continueAvailable.set(false);
        currentState.set(GameState.GAME_WON);
        statusMessage.set("Congratulations! You won!");
    }

    /**
     * Checks if the game is currently in RUNNING state.
     * Game loop should only process updates when this returns true.
     *
     * @return true if game is running, false otherwise
     */
    public boolean isRunning() {
        return currentState.get() == GameState.RUNNING;
    }

    /**
     * Checks if player can continue from a paused/stopped game.
     * Returns false if game is currently running or no saved state exists.
     *
     * @return true if continue is available and game is not running
     */
    public boolean canContinue() {
        return continueAvailable.get() && currentState.get() != GameState.RUNNING;
    }

    // ==================== Observable Property Getters ====================

    /**
     * Gets the current game state property for UI binding.
     *
     * @return observable ObjectProperty containing current GameState
     */
    public ObjectProperty<GameState> stateProperty() {
        return currentState;
    }

    /**
     * Gets the current game state value.
     *
     * @return current GameState enum value
     */
    public GameState getCurrentState() {
        return currentState.get();
    }

    /**
     * Gets the score property for UI binding.
     *
     * @return observable IntegerProperty containing current score
     */
    public IntegerProperty scoreProperty() {
        return score;
    }

    /**
     * Gets the lives property for UI binding.
     *
     * @return observable IntegerProperty containing remaining lives
     */
    public IntegerProperty livesProperty() {
        return lives;
    }

    /**
     * Gets the current score value.
     *
     * @return current score
     */
    public int getScore() {
        return score.get();
    }

    /**
     * Gets the current lives value.
     *
     * @return remaining lives
     */
    public int getLives() {
        return lives.get();
    }

    /**
     * Gets the continue available property for UI binding.
     *
     * @return observable BooleanProperty indicating if continue is available
     */
    public BooleanProperty continueAvailableProperty() {
        return continueAvailable;
    }

    /**
     * Gets the status message property for UI binding.
     *
     * @return observable StringProperty containing status message
     */
    public StringProperty statusMessageProperty() {
        return statusMessage;
    }

    /**
     * Sets the status message displayed to the player.
     *
     * @param message new status message text
     */
    public void setStatusMessage(String message) {
        statusMessage.set(message);
    }

    /**
     * Gets the round time property for UI binding.
     *
     * @return observable DoubleProperty containing round elapsed time in seconds
     */
    public DoubleProperty roundTimeProperty() {
        return roundTimeSeconds;
    }

    /**
     * Gets the total time property for UI binding.
     *
     * @return observable DoubleProperty containing total elapsed time in seconds
     */
    public DoubleProperty totalTimeProperty() {
        return totalTimeSeconds;
    }

    /**
     * Gets the current round property for UI binding.
     *
     * @return observable IntegerProperty containing current round number
     */
    public IntegerProperty roundProperty() {
        return currentRound;
    }

    /**
     * Sets the continue available flag.
     * Controls whether player can resume from a saved state.
     *
     * @param available true to enable continue option, false to disable
     */
    public void setContinueAvailable(boolean available) {
        continueAvailable.set(available);
    }

    /**
     * Sets the current game state directly.
     * Use with caution - prefer using specific state transition methods
     * (beginNewGame, pauseGame, resumeGame, markGameOver) for proper validation.
     *
     * @param gameState new game state to set
     */
    public void setState(GameState gameState) {
        currentState.set(gameState);
    }
}
