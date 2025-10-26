package com.ooparkanoid.ui;

import com.ooparkanoid.core.score.ScoreEntry;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
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

public class LeaderboardController {

    @FXML
    private BorderPane root;

    @FXML
    private Label subtitleLabel;

    @FXML
    private TableView<ScoreEntry> table;

    @FXML
    private TableColumn<ScoreEntry, ScoreEntry> placeColumn;

    @FXML
    private TableColumn<ScoreEntry, String> playerColumn;

    @FXML
    private TableColumn<ScoreEntry, Number> pointsColumn;

    @FXML
    private Button backButton;

    private Runnable backAction;

    @FXML
    private void initialize() {
        configureTable();
        configureBackControls();
    }

    private void configureTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Chưa có thành tích nào được lưu. Hãy chơi và lập kỷ lục!"));

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

    private void configureBackControls() {
        backButton.setOnAction(event -> triggerBackAction());

        root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                triggerBackAction();
                event.consume();
            }
        });

        root.setOnMouseClicked(event -> root.requestFocus());
    }

    private void triggerBackAction() {
        if (backAction != null) {
            backAction.run();
        }
    }

    public void setBackAction(Runnable backAction) {
        this.backAction = backAction;
    }

    public void setScores(List<ScoreEntry> entries) {
        table.setItems(FXCollections.observableArrayList(entries));
        Platform.runLater(() -> root.requestFocus());
    }

    public void setSubtitle(String subtitle) {
        subtitleLabel.setText(subtitle);
    }

    private String buildTooltip(ScoreEntry entry) {
        return "Round: " + entry.getRoundsPlayed()
                + " • TB/Round: " + formatDuration(entry.getAverageSecondsPerRound())
                + " • Tổng: " + formatDuration(entry.getTotalSeconds());
    }

    private String formatDuration(double seconds) {
        long totalSeconds = Math.max(0, Math.round(seconds));
        long minutes = totalSeconds / 60;
        long remainingSeconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, remainingSeconds);
    }
}