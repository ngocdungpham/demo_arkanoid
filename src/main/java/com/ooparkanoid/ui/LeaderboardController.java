package com.ooparkanoid.ui;

import com.ooparkanoid.core.score.ScoreEntry;
import com.ooparkanoid.sound.SoundManager;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import com.ooparkanoid.sound.SoundManager;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;

import java.util.List;

/**
 * Controller for the leaderboard screen displaying high scores.
 * Manages a table view of top scores with detailed tooltips and navigation controls.
 * Implements FXML controller pattern for JavaFX UI integration.
 *
 * Features:
 * - Table display of ranked scores with place, player name, and points columns
 * - Interactive tooltips showing detailed statistics (rounds, average time, total time)
 * - Keyboard navigation (ESC to go back)
 * - Mouse click focus management
 * - Responsive table layout with constrained column resizing
 * - Empty state message when no scores are available
 *
 * Table Columns:
 * - Place: Auto-generated ranking number (1st, 2nd, 3rd, etc.)
 * - Player: Player display name
 * - Points: Score value, right-aligned
 *
 * Navigation:
 * - Back button for manual navigation
 * - ESC key for keyboard shortcut
 * - Callback-based navigation to support different back destinations
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public class LeaderboardController {

    /** Root container for the leaderboard UI */
    @FXML
    private BorderPane root;

    /** Subtitle label displaying leaderboard context */
    @FXML
    private Label subtitleLabel;

    /** Table view displaying the leaderboard entries */
    @FXML
    private TableView<ScoreEntry> table;

    /** Column displaying ranking position */
    @FXML
    private TableColumn<ScoreEntry, ScoreEntry> placeColumn;

    /** Column displaying player names */
    @FXML
    private TableColumn<ScoreEntry, String> playerColumn;

    /** Column displaying score points */
    @FXML
    private TableColumn<ScoreEntry, Number> pointsColumn;

    /** Back navigation button */
    @FXML
    private Button backButton;

    /** Callback action for back navigation */
    private Runnable backAction;

    /**
     * Initializes the controller after FXML loading.
     * Sets up table configuration and input controls.
     * Called automatically by JavaFX when the FXML is loaded.
     */
    @FXML
    private void initialize() {
        configureTable();
        configureBackControls();
    }

    /**
     * Configures the table view with columns, styling, and interactive features.
     * Sets up place numbering, tooltips, and empty state message.
     */
    private void configureTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No scores recorded yet. Play the game to set records!"));

        placeColumn.setSortable(false);
        placeColumn.setReorderable(false);
        placeColumn.setStyle("-fx-alignment: CENTER;");
        placeColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        placeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(ScoreEntry entry, boolean empty) {
                super.updateItem(entry, empty);
                if (empty || entry == null) {
                    setText(null);
                } else {
                    setText(Integer.toString(getIndex() + 1));
                }
            }
        });

        playerColumn.setSortable(false);
        playerColumn.setReorderable(false);
        playerColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getPlayerName()));

        pointsColumn.setSortable(false);
        pointsColumn.setReorderable(false);
        pointsColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
        pointsColumn.setCellValueFactory(data -> new ReadOnlyIntegerWrapper(data.getValue().getScore()));

        table.setRowFactory(tv -> {
            TableRow<ScoreEntry> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldEntry, newEntry) -> {
                if (newEntry == null) {
                    row.setTooltip(null);
                } else {
                    Tooltip tooltip = new Tooltip(buildTooltip(newEntry));
                    tooltip.getStyleClass().add("leaderboard-tooltip");
                    row.setTooltip(tooltip);
                }
            });
            return row;
        });
    }

    /**
     * Configures back navigation controls including button and keyboard shortcuts.
     * Sets up ESC key handler and mouse click focus management.
     */
    private void configureBackControls() {
        backButton.setOnAction(event -> triggerBackAction());

        root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                SoundManager.getInstance().play("selected");
                triggerBackAction();
                event.consume();
            }
        });

        root.setOnMouseClicked(event -> root.requestFocus());
    }

    /**
     * Triggers the back navigation action with proper threading.
     * Ensures the callback runs on the JavaFX Application Thread.
     */
    private void triggerBackAction() {
        if (backAction == null) {
            return;
        }

        if (Platform.isFxApplicationThread()) {
            backAction.run();
        } else {
            Platform.runLater(backAction);
        }
    }

    /**
     * Sets the callback action for back navigation.
     *
     * @param backAction the Runnable to execute when navigating back
     */
    public void setBackAction(Runnable backAction) {
        this.backAction = backAction;
    }

    /**
     * Updates the leaderboard with new score entries.
     * Replaces the current table data and requests focus for keyboard navigation.
     *
     * @param entries the list of ScoreEntry objects to display
     */
    public void setScores(List<ScoreEntry> entries) {
        table.setItems(FXCollections.observableArrayList(entries));
        Platform.runLater(() -> root.requestFocus());
    }

    /**
     * Sets the subtitle text displayed above the leaderboard.
     *
     * @param subtitle the subtitle text to display
     */
    public void setSubtitle(String subtitle) {
        subtitleLabel.setText(subtitle);
    }

    /**
     * Builds a detailed tooltip string for a score entry.
     * Includes rounds played, average time per round, and total time.
     *
     * @param entry the ScoreEntry to build tooltip for
     * @return formatted tooltip string with statistics
     */
    private String buildTooltip(ScoreEntry entry) {
        return "Round: " + entry.getRoundsPlayed()
                + " • Avg/Round: " + formatDuration(entry.getAverageSecondsPerRound())
                + " • Total: " + formatDuration(entry.getTotalSeconds());
    }

    /**
     * Formats a duration in seconds to MM:SS format.
     * Rounds to nearest second and ensures non-negative values.
     *
     * @param seconds the duration in seconds to format
     * @return formatted string in "M:SS" format
     */
    private String formatDuration(double seconds) {
        long totalSeconds = Math.max(0, Math.round(seconds));
        long minutes = totalSeconds / 60;
        long remainingSeconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, remainingSeconds);
    }
}

