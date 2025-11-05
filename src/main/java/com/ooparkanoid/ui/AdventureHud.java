package com.ooparkanoid.ui;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableNumberValue;
import javafx.geometry.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import com.ooparkanoid.core.state.GameStateManager;
import com.ooparkanoid.utils.Constants;

/** Xây dựng HUD dạng “cards” (score/time/lives + ROUND). */
public class AdventureHud {

    private final GridPane grid;
    private final VBox leftPanel;
    private final VBox rightPanel;
    private final Pane centerSpacer;
    private final ColumnConstraints cLeft;
    private final ColumnConstraints cCenter;
    private final ColumnConstraints cRight;

    public AdventureHud(GameStateManager stateManager) {
        // Bindings
        StringBinding onePScore = Bindings.createStringBinding(
                () -> String.format("%07d", Math.max(0, stateManager.getScore())),
                stateManager.scoreProperty());
        StringBinding roundTime = formatDurationBinding(stateManager.roundTimeProperty(), "");
        StringBinding gameTime  = formatDurationBinding(stateManager.totalTimeProperty(), "");
        StringBinding lives     = Bindings.createStringBinding(
                () -> String.format("%d", Math.max(0, stateManager.livesProperty().get())),
                stateManager.livesProperty());

        // Cards trái
        VBox scoreCard = createScoreCard(onePScore);
        VBox roundCard = createStatCard("Round Time", roundTime);
        VBox gameCard  = createStatCard("Game Time",  gameTime);
        VBox livesCard = createStatCard("Lives",      lives);
        VBox adventureStats = new VBox(12, scoreCard, roundCard, gameCard, livesCard);
        adventureStats.setAlignment(Pos.TOP_LEFT);

        leftPanel = new VBox(12, adventureStats);
        leftPanel.setAlignment(Pos.TOP_LEFT);
        leftPanel.setPadding(new Insets(24, 18, 24, 24));
        leftPanel.setBackground(Background.EMPTY);
        leftPanel.setPrefWidth(Constants.LEFT_PANEL_WIDTH);
        leftPanel.setMinWidth(Constants.LEFT_PANEL_WIDTH);
        leftPanel.setMaxWidth(Constants.LEFT_PANEL_WIDTH);

        // Panel phải – ROUND
        VBox roundOnly = createRoundCard(stateManager);
        rightPanel = new VBox(roundOnly);
        rightPanel.setAlignment(Pos.TOP_RIGHT);
        rightPanel.setPadding(new Insets(24, 24, 24, 18));
        rightPanel.setBackground(Background.EMPTY);
        rightPanel.setPrefWidth(Constants.RIGHT_PANEL_WIDTH);
        rightPanel.setMinWidth(Constants.RIGHT_PANEL_WIDTH);
        rightPanel.setMaxWidth(Constants.RIGHT_PANEL_WIDTH);

        // Grid 3 cột
        grid = new GridPane();
        grid.setMouseTransparent(true);
        grid.setPickOnBounds(false);
        grid.setPrefSize(Constants.WIDTH, Constants.HEIGHT);

        cLeft   = createColumn(Constants.SIDE_PANEL_RATIO);
        cCenter = createColumn(Constants.PLAYFIELD_RATIO);
        cRight  = createColumn(Constants.SIDE_PANEL_RATIO);
        grid.getColumnConstraints().addAll(cLeft, cCenter, cRight);

        centerSpacer = new Pane();
        centerSpacer.setMinSize(0, 0);
        centerSpacer.setMouseTransparent(true);
        centerSpacer.setStyle("-fx-border-color: rgba(255,255,255,0.18); -fx-border-width: 0 2 0 2;");

        GridPane.setHalignment(leftPanel, HPos.LEFT);
        GridPane.setValignment(leftPanel, VPos.TOP);
        GridPane.setHalignment(rightPanel, HPos.RIGHT);
        GridPane.setValignment(rightPanel, VPos.TOP);
        GridPane.setHgrow(centerSpacer, Priority.ALWAYS);

        grid.add(leftPanel, 0, 0);
        grid.add(centerSpacer, 1, 0);
        grid.add(rightPanel, 2, 0);
    }

    public GridPane getGrid() { return grid; }

    public void setAdventureVisible(boolean visible) {
        leftPanel.setVisible(visible);  leftPanel.setManaged(visible);
        rightPanel.setVisible(visible); rightPanel.setManaged(visible);
        centerSpacer.setVisible(visible); centerSpacer.setManaged(visible);
    }

    public void setColumnPercents(double leftPct, double centerPct, double rightPct) {
        cLeft.setPercentWidth(leftPct);
        cCenter.setPercentWidth(centerPct);
        cRight.setPercentWidth(rightPct);
    }

    // ====== helpers ======
    private ColumnConstraints createColumn(double ratio) {
        ColumnConstraints c = new ColumnConstraints();
        c.setPercentWidth(ratio * 100.0);
        c.setHalignment(HPos.CENTER);
        return c;
    }

    private StringBinding formatDurationBinding(ObservableNumberValue secondsProperty, String label) {
        return Bindings.createStringBinding(() -> {
            long totalSeconds = (long) Math.floor(secondsProperty.doubleValue());
            long minutes = totalSeconds / 60;
            long seconds = totalSeconds % 60;
            return String.format("%s%02d:%02d", label, minutes, seconds);
        }, secondsProperty);
    }

    private VBox createStatCard(String title, StringBinding valueBinding) {
        Label titleLbl = new Label(title.toUpperCase());
        titleLbl.setTextFill(Color.web("#FFDFDF"));
        titleLbl.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Label valueLbl = new Label();
        valueLbl.textProperty().bind(valueBinding);
        valueLbl.setTextFill(Color.WHITE);
        valueLbl.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        VBox box = new VBox(4, titleLbl, valueLbl);
        box.setPadding(new Insets(10, 14, 10, 14));
        box.setBackground(new Background(new BackgroundFill(Color.color(0, 0, 0, 0.60), new CornerRadii(12), Insets.EMPTY)));
        box.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.55), 16, 0.4, 0, 6);");
        box.setBackground(Background.EMPTY);
        box.setStyle(null); // nếu muốn bỏ luôn drop shadow
        UiUtils.addCardGlowOnChange(valueLbl);
        return box;
    }

    private VBox createScoreCard(StringBinding scoreBinding) {
        Label titleLbl = new Label("1P SCORE");
        titleLbl.setTextFill(Color.WHITE);
        titleLbl.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Label valueLbl = new Label();
        valueLbl.textProperty().bind(scoreBinding);
        valueLbl.setTextFill(Color.WHITE);
        valueLbl.setFont(Font.font("Arial", FontWeight.BOLD, 26));

        VBox box = new VBox(4, titleLbl, valueLbl);
        box.setPadding(new Insets(10, 14, 10, 14));
        box.setBackground(new Background(new BackgroundFill(Color.rgb(160, 35, 35, 0.75), new CornerRadii(12), Insets.EMPTY)));
        box.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 18, 0.45, 0, 7);");

        UiUtils.addCardGlowOnChange(valueLbl);
        return box;
    }

    private VBox createRoundCard(GameStateManager stateManager) {
        Label rTitle = new Label("ROUND");
        rTitle.setTextFill(Color.LIMEGREEN);
        rTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        Label rValue = new Label();
        rValue.textProperty().bind(Bindings.createStringBinding(
                () -> String.format("%02d", Math.max(1, stateManager.roundProperty().get())),
                stateManager.roundProperty()));
        rValue.setTextFill(Color.WHITE);
        rValue.setFont(Font.font("Arial", FontWeight.BOLD, 64));

        VBox box = new VBox(4, rTitle, rValue);
        box.setAlignment(Pos.TOP_RIGHT);
        box.setPadding(new Insets(18, 18, 18, 18));
        box.setBackground(new Background(new BackgroundFill(Color.color(0, 0, 0, 0.55), new CornerRadii(14), Insets.EMPTY)));
        box.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.55), 18, 0.45, 0, 7);");
        box.setBackground(Background.EMPTY);
        box.setStyle(null); // nếu muốn bỏ luôn drop shadow
        UiUtils.addCardGlowOnChange(rValue);
        return box;
    }
}
