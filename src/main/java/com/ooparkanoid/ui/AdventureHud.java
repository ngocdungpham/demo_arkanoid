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

/**
 * Heads-Up Display (HUD) component for the Adventure game mode.
 * Displays game statistics in card-based layout with left panel (stats) and right panel (round).
 * Provides reactive UI updates through JavaFX bindings to GameStateManager properties.
 *
 * Layout Structure:
 * - Left Panel: Score, Round Time, Game Time, Lives (vertical stack of cards)
 * - Center: Spacer with subtle border for playfield separation
 * - Right Panel: Current Round number (prominent display)
 *
 * Features:
 * - Reactive data binding for automatic UI updates
 * - Card-based design with drop shadows and glow effects
 * - Formatted display (zero-padded scores, MM:SS time format)
 * - Visibility controls for mode switching
 * - Responsive column width adjustments
 *
 * Visual Design:
 * - Semi-transparent dark backgrounds for readability
 * - Color-coded cards (red for score, green for round)
 * - Glow effects on value changes for visual feedback
 * - Professional typography with Arial font family
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public class AdventureHud {

    /** Main grid container for the HUD layout */
    private final GridPane grid;

    /** Left panel containing game statistics cards */
    private final VBox leftPanel;

    /** Right panel containing round display */
    private final VBox rightPanel;

    /** Center spacer pane with border styling */
    private final Pane centerSpacer;

    /** Column constraint for left panel */
    private final ColumnConstraints cLeft;

    /** Column constraint for center spacer */
    private final ColumnConstraints cCenter;

    /** Column constraint for right panel */
    private final ColumnConstraints cRight;

    /**
     * Constructs an AdventureHud with reactive bindings to game state.
     * Creates all UI components, sets up data bindings, and configures layout.
     *
     * @param stateManager the game state manager providing observable properties
     */
    public AdventureHud(GameStateManager stateManager) {
        // Create reactive data bindings for UI updates
        StringBinding onePScore = Bindings.createStringBinding(
                () -> String.format("%07d", Math.max(0, stateManager.getScore())),
                stateManager.scoreProperty());
        StringBinding roundTime = formatDurationBinding(stateManager.roundTimeProperty(), "");
        StringBinding gameTime  = formatDurationBinding(stateManager.totalTimeProperty(), "");
        StringBinding lives     = Bindings.createStringBinding(
                () -> String.format("%d", Math.max(0, stateManager.livesProperty().get())),
                stateManager.livesProperty());

        // Create left panel cards
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

        // Create right panel with round display
        VBox roundOnly = createRoundCard(stateManager);
        rightPanel = new VBox(roundOnly);
        rightPanel.setAlignment(Pos.TOP_RIGHT);
        rightPanel.setPadding(new Insets(24, 24, 24, 18));
        rightPanel.setBackground(Background.EMPTY);
        rightPanel.setPrefWidth(Constants.RIGHT_PANEL_WIDTH);
        rightPanel.setMinWidth(Constants.RIGHT_PANEL_WIDTH);
        rightPanel.setMaxWidth(Constants.RIGHT_PANEL_WIDTH);

        // Set up main grid layout (3 columns)
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

    /**
     * Gets the main grid container for this HUD.
     *
     * @return the GridPane containing all HUD elements
     */
    public GridPane getGrid() {
        return grid;
    }

    /**
     * Controls visibility of adventure HUD elements.
     * Used when switching between different game modes.
     *
     * @param visible true to show adventure HUD, false to hide
     */
    public void setAdventureVisible(boolean visible) {
        leftPanel.setVisible(visible);  leftPanel.setManaged(visible);
        rightPanel.setVisible(visible); rightPanel.setManaged(visible);
        centerSpacer.setVisible(visible); centerSpacer.setManaged(visible);
    }

    /**
     * Adjusts column width percentages for responsive layout.
     * Allows dynamic resizing of HUD panels based on game requirements.
     *
     * @param leftPct percentage width for left panel (0-100)
     * @param centerPct percentage width for center spacer (0-100)
     * @param rightPct percentage width for right panel (0-100)
     */
    public void setColumnPercents(double leftPct, double centerPct, double rightPct) {
        cLeft.setPercentWidth(leftPct);
        cCenter.setPercentWidth(centerPct);
        cRight.setPercentWidth(rightPct);
    }

    // ==================== Helper Methods ====================

    /**
     * Creates a column constraint with specified width ratio.
     *
     * @param ratio the width ratio for this column
     * @return configured ColumnConstraints object
     */
    private ColumnConstraints createColumn(double ratio) {
        ColumnConstraints c = new ColumnConstraints();
        c.setPercentWidth(ratio * 100.0);
        c.setHalignment(HPos.CENTER);
        return c;
    }

    /**
     * Creates a formatted duration binding for time display.
     * Converts seconds to MM:SS format with optional prefix label.
     *
     * @param secondsProperty observable property containing seconds value
     * @param label optional prefix label (can be empty string)
     * @return StringBinding with formatted time display
     */
    private StringBinding formatDurationBinding(ObservableNumberValue secondsProperty, String label) {
        return Bindings.createStringBinding(() -> {
            long totalSeconds = (long) Math.floor(secondsProperty.doubleValue());
            long minutes = totalSeconds / 60;
            long seconds = totalSeconds % 60;
            return String.format("%s%02d:%02d", label, minutes, seconds);
        }, secondsProperty);
    }

    /**
     * Creates a standard statistics card with title and reactive value.
     * Used for Round Time, Game Time, and Lives displays.
     *
     * @param title the card title text
     * @param valueBinding reactive binding for the displayed value
     * @return configured VBox card container
     */
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
        box.setStyle(null); // Remove drop shadow if desired
        //UiUtils.addCardGlowOnChange(valueLbl);
        return box;
    }

    /**z
     * Creates the score display card with distinctive red styling.
     * Features larger font and red background to emphasize score importance.
     *
     * @param scoreBinding reactive binding for the score value
     * @return configured VBox score card container
     */
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

        //UiUtils.addCardGlowOnChange(valueLbl);
        return box;
    }

    /**
     * Creates the round display card with prominent green styling.
     * Features large font size to make current round highly visible.
     *
     * @param stateManager game state manager for round property binding
     * @return configured VBox round card container
     */
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
        box.setStyle(null); // Remove drop shadow if desired
        //UiUtils.addCardGlowOnChange(rValue);
        return box;
    }
}
