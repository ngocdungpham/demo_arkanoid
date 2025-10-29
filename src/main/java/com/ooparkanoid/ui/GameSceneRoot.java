package com.ooparkanoid.ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import com.ooparkanoid.core.engine.LocalBattleManager;
import com.ooparkanoid.core.state.GameMode;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import com.ooparkanoid.AlertBox;
import com.ooparkanoid.core.engine.GameManager;
import com.ooparkanoid.core.state.GameState;
import com.ooparkanoid.core.state.GameStateManager;
import com.ooparkanoid.sound.SoundManager;
import com.ooparkanoid.utils.Constants;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableNumberValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
//import javafx.scene.paint.CycleMethod;
//import javafx.scene.paint.LinearGradient;
//import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
// import
import com.ooparkanoid.ui.NeonPauseView;
import javafx.scene.Node;


import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.Set;
import java.util.EnumSet;

public class GameSceneRoot {

    private final Scene scene;
    private final GraphicsContext graphicsContext;
    private final GameManager gameManager;
    private final LocalBattleManager battleManager;
    private final GameStateManager stateManager;
    private final AnimationTimer gameLoop;
    private StackPane leftBackgroundSection;
    private StackPane centerBackgroundSection;
    private StackPane rightBackgroundSection;
    private HBox backgroundSections;

    private VBox leftPanel;
    private Pane centerSpacer;
    private VBox adventureStats;
    private VBox battleStats;
    private GridPane hudGrid;
    private ColumnConstraints leftColumnConstraint;
    private ColumnConstraints centerColumnConstraint;
    private ColumnConstraints rightColumnConstraint;
    private HBox battleScoreboard;
    private Label battlePlayerOneLabel;
    private Label battlePlayerTwoLabel;
    private Timeline battlePlayerOneFlash;
    private Timeline battlePlayerTwoFlash;
    private static final double BATTLE_LABEL_BASE_OPACITY = 0.75;

    private final Canvas canvas;
    private final SceneLayoutFactory.LayeredScene layeredScene;
    private final BackgroundLayer backgroundLayer;
    private NeonPauseView pauseView;

//    private Label stateLabel;
//    private Label messageLabel;
//    private Button continueButton;
//
//
//    private StackPane menuOverlay;

    private final Deque<KeyCode> pressedStack = new ArrayDeque<>();
    private final Runnable onExitToMenuCallback;
    private final ObjectProperty<GameMode> currentMode = new SimpleObjectProperty<>(GameMode.ADVENTURE);
    private final Set<KeyCode> activeKeys = EnumSet.noneOf(KeyCode.class);

    public GameSceneRoot(Runnable onExitToMenuCallback) {

    public GameSceneRoot() {
        this(GameMode.ADVENTURE);
    }

    public GameSceneRoot(GameMode initialMode) {
        stateManager = new GameStateManager();
        gameManager = new GameManager(stateManager);
        this.onExitToMenuCallback = onExitToMenuCallback;
        battleManager = new LocalBattleManager(stateManager);
//
        canvas = new Canvas(Constants.WIDTH, Constants.HEIGHT);
        graphicsContext = canvas.getGraphicsContext2D();

        layeredScene = SceneLayoutFactory.createLayeredScene(canvas);
        backgroundLayer = layeredScene.backgroundLayer();

        scene = new Scene(layeredScene.root(), Constants.WIDTH, Constants.HEIGHT);
        //  scene.getStylesheets().add("/styles/theme.css");

        configureBackground();
        buildHud();
        // === Overlay Pause ===
        pauseView = new NeonPauseView(new NeonPauseView.Callbacks() {
            @Override public void onResume() {
                // Ẩn overlay rồi resume
                pauseView.hide();
                stateManager.resumeGame();
                // Trả focus cho scene gốc để nhận phím
                scene.getRoot().requestFocus();
            }
            @Override public void onExit() {
                // Tuỳ ý: về menu chính hoặc thoát game
                // Ví dụ: Platform.exit();
               // Platform.exit();
                gameLoop.stop(); // Dừng vòng lặp game
                SoundManager.getInstance().stopMusic();
                onExitToMenuCallback.run(); // Gọi hàm quay về menu được truyền từ MainConsole
            }
        });

// Đặt overlay lên trên cùng (root của layeredScene là StackPane)
        ((StackPane) layeredScene.root()).getChildren().add(pauseView.getView());

        // buildMenuOverlay();
        setupStateListeners();
        setupInputHandlers();
        currentMode.addListener((obs, oldMode, newMode) -> updateLayoutForMode(newMode));


        gameLoop = createGameLoop();

//        //  stateManager.resetToMenu();
////        gameManager.initializeGame();
////        stateManager.beginNewGame(gameManager.getScore(), gameManager.getLives());
//        startAdventureMode();
        if (initialMode == GameMode.LOCAL_BATTLE) {
            startBattleMode();
        } else {
            startAdventureMode();
        }
        updateLayoutForMode(currentMode.get());
        gameLoop.start();
    }

    private void configureBackground() {
//        LinearGradient gradient = new LinearGradient(
//                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
//                new Stop(0.0, Color.rgb(14, 27, 64)),
//                new Stop(1.0, Color.rgb(8, 8, 24))
//        );
//        backgroundLayer.setFill(gradient);

        backgroundLayer.setFill(Color.BLACK);
        backgroundLayer.setImageLayers(null);
        backgroundLayer.getChildren().clear();

        leftBackgroundSection = createBackgroundSection(Constants.LEFT_PANEL_WIDTH, "background-left");
        centerBackgroundSection = createBackgroundSection(Constants.PLAYFIELD_WIDTH, "background-center");
        rightBackgroundSection = createBackgroundSection(Constants.RIGHT_PANEL_WIDTH, "background-right");

        BackgroundFill sideFill = new BackgroundFill(Color.rgb(8, 12, 28, 0.88), CornerRadii.EMPTY, Insets.EMPTY);
        leftBackgroundSection.setBackground(new Background(sideFill));
        rightBackgroundSection.setBackground(new Background(sideFill));

        BackgroundFill centerFill = new BackgroundFill(Color.rgb(6, 10, 30), CornerRadii.EMPTY, Insets.EMPTY);
        Optional<Image> backdrop = loadImage("/picture/space1.png");
        if (backdrop.isPresent()) {
            BackgroundImage coverImage = BackgroundLayer.cover(backdrop.get());
            centerBackgroundSection.setBackground(new Background(
                    new BackgroundFill[]{centerFill},
                    new BackgroundImage[]{coverImage}
            ));
        } else {
            centerBackgroundSection.setBackground(new Background(centerFill));
        }

        // Ảnh cho bên trái
        Optional<Image> leftImage = loadImage("/picture/menu1.jpg");
        if (leftImage.isPresent()) {
            BackgroundImage leftBg = BackgroundLayer.cover(leftImage.get());
            leftBackgroundSection.setBackground(new Background(
                    new BackgroundFill[]{sideFill},
                    new BackgroundImage[]{leftBg}
            ));
        } else {
            leftBackgroundSection.setBackground(new Background(sideFill));
        }

        // Ảnh cho bên phải
        Optional<Image> rightImage = loadImage("/picture/menu1.jpg");
        if (rightImage.isPresent()) {
            BackgroundImage rightBg = BackgroundLayer.cover(rightImage.get());
            rightBackgroundSection.setBackground(new Background(
                    new BackgroundFill[]{sideFill},
                    new BackgroundImage[]{rightBg}
            ));
        } else {
            rightBackgroundSection.setBackground(new Background(sideFill));
        }

//        loadImage("/picture/space1.jpg")
//                .map(BackgroundLayer::cover)
//                .ifPresent(backgroundLayer::addImageLayer);
//        HBox backgroundSections = new HBox(leftBackgroundSection, centerBackgroundSection, rightBackgroundSection);
        backgroundSections = new HBox(leftBackgroundSection, centerBackgroundSection, rightBackgroundSection);
        backgroundSections.setPrefSize(Constants.WIDTH, Constants.HEIGHT);
        backgroundSections.setMinSize(Constants.WIDTH, Constants.HEIGHT);
        backgroundSections.setMaxSize(Constants.WIDTH, Constants.HEIGHT);
        backgroundSections.setMouseTransparent(true);

        backgroundLayer.getChildren().add(backgroundSections);
    }

    private void buildHud() {
//        Label scoreLabel = new Label();
//        scoreLabel.textProperty().bind(stateManager.scoreProperty().asString("Score: %d"));
//        scoreLabel.setTextFill(Color.WHITE);
//        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
//        scoreLabel.getStyleClass().add("hud");
        Label pointsLabel = createHudValueLabel();
        pointsLabel.textProperty().bind(stateManager.scoreProperty().asString("Point: %d"));

//        Label livesLabel = new Label();
//        livesLabel.textProperty().bind(stateManager.livesProperty().asString("Lives: %d"));
        Label roundTimeLabel = createHudValueLabel();
        roundTimeLabel.textProperty().bind(formatDurationBinding(stateManager.roundTimeProperty(), "Time round"));

//        livesLabel.setTextFill(Color.WHITE);
//        livesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        Label totalTimeLabel = createHudValueLabel();
        totalTimeLabel.textProperty().bind(formatDurationBinding(stateManager.totalTimeProperty(), "All time"));

//        livesLabel.getStyleClass().add("hud");
        Label livesLabel = createHudValueLabel();
        livesLabel.textProperty().bind(stateManager.livesProperty().asString("Lives: %d"));

//        HBox hud = new HBox(20, scoreLabel, livesLabel);
//        hud.setPadding(new Insets(15));
//        hud.setAlignment(Pos.TOP_LEFT);
//        VBox leftPanel = new VBox(12, createHudTitleLabel(), pointsLabel, roundTimeLabel, totalTimeLabel, livesLabel);
        Label adventureTitle = createHudTitleLabel();
        adventureTitle.setText("Adventure Stats");
//        VBox adventureStats = new VBox(12, adventureTitle, pointsLabel, roundTimeLabel, totalTimeLabel, livesLabel);
        adventureStats = new VBox(12, adventureTitle, pointsLabel, roundTimeLabel, totalTimeLabel, livesLabel);
        adventureStats.setAlignment(Pos.TOP_LEFT);

        Label battleTitle = createHudTitleLabel();
//        battleTitle.setText("Solo Battle");
        battleTitle.setText("Versus Battle");

        Label playerOneLivesLabel = createHudValueLabel();
        playerOneLivesLabel.textProperty().bind(Bindings.createStringBinding(
                () -> String.format("Player 1 Lives: %d", battleManager.playerOneLivesProperty().get()),
                battleManager.playerOneLivesProperty(), currentMode));

        Label playerTwoLivesLabel = createHudValueLabel();
        playerTwoLivesLabel.textProperty().bind(Bindings.createStringBinding(
                () -> String.format("Player 2 Lives: %d", battleManager.playerTwoLivesProperty().get()),
                battleManager.playerTwoLivesProperty(), currentMode));

        Label matchTimeLabel = createHudValueLabel();
        matchTimeLabel.textProperty().bind(formatDurationBinding(battleManager.matchTimeProperty(), "Match Time"));

        Label battleScoreLabel = createHudValueLabel();
        battleScoreLabel.textProperty().bind(Bindings.createStringBinding(
//                () -> String.format("Bricks Broken P1: %d | P2: %d",
//                        battleManager.playerOneScoreProperty().get(),
//                        battleManager.playerTwoScoreProperty().get()),
//                battleManager.playerOneScoreProperty(),
//                battleManager.playerTwoScoreProperty(),
                () -> String.format("Shields P1: %d | P2: %d",
                        battleManager.playerOneLivesProperty().get(),
                        battleManager.playerTwoLivesProperty().get()),
                battleManager.playerOneLivesProperty(),
                battleManager.playerTwoLivesProperty(),
                currentMode));

        Label servingLabel = createHudValueLabel();
        servingLabel.textProperty().bind(Bindings.createStringBinding(() -> {
                    if (currentMode.get() != GameMode.LOCAL_BATTLE) {
                        return "";
                    }
                    return "Serving: " +
                            (battleManager.servingPlayerProperty().get() == LocalBattleManager.ServingPlayer.PLAYER_ONE
                                    ? "Player 1"
                                    : "Player 2");
                },
                battleManager.servingPlayerProperty(),
                currentMode));

        VBox battleStats = new VBox(12,
                battleTitle,
                playerOneLivesLabel,
                playerTwoLivesLabel,
                matchTimeLabel,
                battleScoreLabel,
                servingLabel);
        battleStats.setAlignment(Pos.TOP_LEFT);

        adventureStats.visibleProperty().bind(currentMode.isEqualTo(GameMode.ADVENTURE));
        adventureStats.managedProperty().bind(adventureStats.visibleProperty());

        battleStats.visibleProperty().bind(currentMode.isEqualTo(GameMode.LOCAL_BATTLE));
        battleStats.managedProperty().bind(battleStats.visibleProperty());

//        VBox leftPanel = new VBox();
        leftPanel = new VBox();
        leftPanel.getChildren().addAll(adventureStats, battleStats);

        leftPanel.setAlignment(Pos.TOP_LEFT);
        leftPanel.setPadding(new Insets(24, 18, 24, 24));
        leftPanel.setSpacing(12);
        leftPanel.setBackground(createPanelBackground());
        leftPanel.setPrefWidth(Constants.LEFT_PANEL_WIDTH);
        leftPanel.setMinWidth(Constants.LEFT_PANEL_WIDTH);
        leftPanel.setMaxWidth(Constants.LEFT_PANEL_WIDTH);

//        Label currentRoundTitle = createHudTitleLabel();
//        Label currentRoundValue = createHudValueLabel();
//        currentRoundValue.textProperty().bind(stateManager.roundProperty().asString("Round %d"));
//
//        VBox rightPanel = new VBox(10, currentRoundTitle, currentRoundValue);

        VBox rightPanel = new VBox(10, currentRoundTitle, currentRoundValue);
        rightPanel.setAlignment(Pos.TOP_RIGHT);
        rightPanel.setPadding(new Insets(24, 24, 24, 18));
        rightPanel.setBackground(createPanelBackground());
        rightPanel.setPrefWidth(Constants.RIGHT_PANEL_WIDTH);
        rightPanel.setMinWidth(Constants.RIGHT_PANEL_WIDTH);
        rightPanel.setMaxWidth(Constants.RIGHT_PANEL_WIDTH);

//        GridPane hudGrid = new GridPane();
        hudGrid = new GridPane();
        hudGrid.setMouseTransparent(true);
        hudGrid.setPickOnBounds(false);
        hudGrid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        hudGrid.setPrefSize(Constants.WIDTH, Constants.HEIGHT);

//        ColumnConstraints leftColumn = createColumn(Constants.SIDE_PANEL_RATIO);
//        ColumnConstraints centerColumn = createColumn(Constants.PLAYFIELD_RATIO);
//        ColumnConstraints rightColumn = createColumn(Constants.SIDE_PANEL_RATIO);
//        hudGrid.getColumnConstraints().addAll(leftColumn, centerColumn, rightColumn);
        leftColumnConstraint = createColumn(Constants.SIDE_PANEL_RATIO);
        centerColumnConstraint = createColumn(Constants.PLAYFIELD_RATIO);
        rightColumnConstraint = createColumn(Constants.SIDE_PANEL_RATIO);
        hudGrid.getColumnConstraints().addAll(leftColumnConstraint, centerColumnConstraint, rightColumnConstraint);

//        Pane centerSpacer = new Pane();
        centerSpacer = new Pane();
        centerSpacer.setMinSize(0, 0);
        centerSpacer.setMouseTransparent(true);
        centerSpacer.setStyle("-fx-border-color: rgba(255,255,255,0.18); -fx-border-width: 0 2 0 2;");

        GridPane.setHalignment(leftPanel, HPos.LEFT);
        GridPane.setValignment(leftPanel, VPos.TOP);
        GridPane.setHgrow(centerSpacer, Priority.ALWAYS);
        GridPane.setHgrow(leftPanel, Priority.NEVER);
        GridPane.setFillWidth(leftPanel, true);

        hudGrid.add(leftPanel, 0, 0);
        hudGrid.add(centerSpacer, 1, 0);

        BooleanBinding hudVisible = stateManager.stateProperty().isEqualTo(GameState.RUNNING);
//        hud.visibleProperty().bind(hudVisible);
//        hud.managedProperty().bind(hudVisible);
        hudGrid.visibleProperty().bind(hudVisible);
        hudGrid.managedProperty().bind(hudVisible);

        layeredScene.contentLayer().getChildren().add(hudGrid);
        StackPane.setAlignment(hudGrid, Pos.CENTER);
        createBattleScoreboard();
    }

    private Background createPanelBackground() {
        return new Background(new BackgroundFill(Color.color(0, 0, 0, 0.55), new CornerRadii(12), Insets.EMPTY));
    }

    private StackPane createBackgroundSection(double width, String styleClass) {
        StackPane section = new StackPane();
        section.setPrefSize(width, Constants.HEIGHT);
        section.setMinSize(width, Constants.HEIGHT);
        section.setMaxSize(width, Constants.HEIGHT);
        section.getStyleClass().add(styleClass);
        return section;
    }

    private Label createHudValueLabel() {
        Label label = new Label();
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        label.getStyleClass().add("hud");
        return label;
    }

    private Label createHudTitleLabel() {
        Label label = new Label();
        label.setTextFill(Color.LIGHTGRAY);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        return label;
    }

    private void createBattleScoreboard() {
        battlePlayerOneLabel = createBattleCounterLabel(
                formatBattleCounterText("P1", battleManager.playerOneLivesProperty().get()),
                Color.web("#FF6F61"));
        battlePlayerTwoLabel = createBattleCounterLabel(
                formatBattleCounterText("P2", battleManager.playerTwoLivesProperty().get()),
                Color.web("#3FA9F5"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        battleScoreboard = new HBox(60, battlePlayerOneLabel, spacer, battlePlayerTwoLabel);
        battleScoreboard.setAlignment(Pos.TOP_CENTER);
        battleScoreboard.setPadding(new Insets(20, 48, 0, 48));
        battleScoreboard.setMouseTransparent(true);

        BooleanBinding battleVisible = currentMode.isEqualTo(GameMode.LOCAL_BATTLE);
        battleScoreboard.visibleProperty().bind(battleVisible);
        battleScoreboard.managedProperty().bind(battleVisible);

        layeredScene.overlayLayer().getChildren().add(battleScoreboard);
        StackPane.setAlignment(battleScoreboard, Pos.TOP_CENTER);

        battleManager.playerOneLivesProperty().addListener((obs, oldVal, newVal) -> {
            updateBattleLabel(battlePlayerOneLabel, formatBattleCounterText("P1", newVal));
            if (oldVal != null && newVal != null && newVal.intValue() < oldVal.intValue()) {
                playBattleLabelFlash(battlePlayerOneLabel, true);
            } else {
                resetBattleLabelOpacity(battlePlayerOneLabel);
            }
        });

        battleManager.playerTwoLivesProperty().addListener((obs, oldVal, newVal) -> {
            updateBattleLabel(battlePlayerTwoLabel, formatBattleCounterText("P2", newVal));
            if (oldVal != null && newVal != null && newVal.intValue() < oldVal.intValue()) {
                playBattleLabelFlash(battlePlayerTwoLabel, false);
            } else {
                resetBattleLabelOpacity(battlePlayerTwoLabel);
            }
        });

        resetBattleLabels();
    }

    private Label createBattleCounterLabel(String text, Color accentColor) {
        Label label = new Label(text);
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        label.setAlignment(Pos.CENTER);
        label.setPadding(new Insets(8, 28, 8, 28));
        label.setMinWidth(200);
        label.setBackground(new Background(new BackgroundFill(
                accentColor.deriveColor(0, 1, 1, 0.38), new CornerRadii(20), Insets.EMPTY)));
        label.setOpacity(BATTLE_LABEL_BASE_OPACITY);
        label.setMouseTransparent(true);
        label.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.55), 18, 0.4, 0, 6);");
        return label;
    }

    private String formatBattleCounterText(String playerLabel, Number lives) {
        int value = lives == null ? 0 : lives.intValue();
        return String.format("%s: %d", playerLabel, Math.max(0, value));
    }

    private void updateBattleLabel(Label label, String text) {
        if (label != null) {
            label.setText(text);
        }
    }

    private void resetBattleLabelOpacity(Label label) {
        if (label != null) {
            label.setOpacity(BATTLE_LABEL_BASE_OPACITY);
        }
    }

    private void playBattleLabelFlash(Label label, boolean playerOne) {
        if (label == null) {
            return;
        }
        Timeline existing = playerOne ? battlePlayerOneFlash : battlePlayerTwoFlash;
        if (existing != null) {
            existing.stop();
        }
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(label.opacityProperty(), BATTLE_LABEL_BASE_OPACITY)),
                new KeyFrame(Duration.millis(140), new KeyValue(label.opacityProperty(), 1.0)),
                new KeyFrame(Duration.millis(360), new KeyValue(label.opacityProperty(), BATTLE_LABEL_BASE_OPACITY))
        );
        timeline.setOnFinished(event -> label.setOpacity(BATTLE_LABEL_BASE_OPACITY));
        timeline.play();
        if (playerOne) {
            battlePlayerOneFlash = timeline;
        } else {
            battlePlayerTwoFlash = timeline;
        }
    }

    private void resetBattleLabels() {
        updateBattleLabel(battlePlayerOneLabel,
                formatBattleCounterText("P1", battleManager.playerOneLivesProperty().get()));
        updateBattleLabel(battlePlayerTwoLabel,
                formatBattleCounterText("P2", battleManager.playerTwoLivesProperty().get()));
        resetBattleLabelOpacity(battlePlayerOneLabel);
        resetBattleLabelOpacity(battlePlayerTwoLabel);
    }

    private ColumnConstraints createColumn(double ratio) {
        ColumnConstraints column = new ColumnConstraints();
        column.setPercentWidth(ratio * 100.0);
        column.setHalignment(HPos.CENTER);
        return column;
    }

//        layeredScene.contentLayer().getChildren().add(hud);
//        StackPane.setAlignment(hud, Pos.TOP_LEFT);
    private StringBinding formatDurationBinding(ObservableNumberValue secondsProperty, String label) {
        return Bindings.createStringBinding(() -> {
            long totalSeconds = (long) Math.floor(secondsProperty.doubleValue());
            long minutes = totalSeconds / 60;
            long seconds = totalSeconds % 60;
            return String.format("%s: %02d:%02d", label, minutes, seconds);
        }, secondsProperty);
    }

//    private void buildMenuOverlay() {
//        stateLabel = new Label();
//        stateLabel.textProperty().bind(Bindings.createStringBinding(() -> {
//            GameState current = stateManager.getCurrentState();
//            return switch (current) {
//                case MENU -> "Arkanoid";
//                case PAUSED -> "Paused";
//                case GAME_OVER -> "Game Over";
//                case RUNNING, MODE_SELECT, HOW_TO_PLAY, INFORMATION, PAUSE -> "";
//            };
//        }, stateManager.stateProperty()));
//        stateLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
//        stateLabel.setTextFill(Color.WHITE);
//        stateLabel.getStyleClass().add("overlay-title");
//
//        messageLabel = new Label();
//        messageLabel.textProperty().bind(stateManager.statusMessageProperty());
//        messageLabel.setWrapText(true);
//        messageLabel.setTextFill(Color.WHITE);
//        messageLabel.setAlignment(Pos.CENTER);
//        messageLabel.getStyleClass().add("overlay-subtitle");
//
//        Button newGameButton = createMenuButton("New game", this::startNewGame);
//        Button gameModeButton = createMenuButton("Game mode", e ->
//                AlertBox.display("Game mode", "Classic brick breaking mode. Destroy all bricks to win."));
//        Button howToPlayButton = createMenuButton("How to play", e ->
//                AlertBox.display("How to play", "Use A/D or the arrow keys to move the paddle. Keep the ball from falling!\nPress ESC to pause."));
//        Button infoButton = createMenuButton("Information", e ->
//                AlertBox.display("About", "Arkanoid demo built with JavaFX."));
//        Button exitButton = createMenuButton("Exit game", e -> Platform.exit());
//        continueButton = createMenuButton("Continue", e -> stateManager.resumeGame());
//
//        BooleanBinding canContinue = Bindings.createBooleanBinding(
//                stateManager::canContinue,
//                stateManager.stateProperty(),
//                stateManager.continueAvailableProperty()
//        );
//        continueButton.visibleProperty().bind(canContinue);
//        continueButton.managedProperty().bind(continueButton.visibleProperty());
//
//        VBox menuContent = new VBox(14,
//                stateLabel,
//                messageLabel,
//                newGameButton,
//                continueButton,
//                gameModeButton,
//                howToPlayButton,
//                infoButton,
//                exitButton
//        );
//
//        menuContent.setAlignment(Pos.CENTER);
//        menuContent.setPadding(new Insets(40));
//        menuContent.setFillWidth(true);
//        menuOverlay = new StackPane(menuContent);
//        menuOverlay.getStyleClass().add("overlay");
//        layeredScene.registerOverlay("menu", menuOverlay);
//
//        BooleanBinding menuVisible = stateManager.stateProperty().isNotEqualTo(GameState.RUNNING);
//        menuOverlay.visibleProperty().bind(menuVisible);
//        menuOverlay.managedProperty().bind(menuVisible);
//        menuOverlay.mouseTransparentProperty().bind(menuVisible.not());
//    }

    private void setupStateListeners() {
        stateManager.stateProperty().addListener((obs, oldState, newState) -> {
//            if (newState != GameState.RUNNING && gameManager.getPaddle() != null) {
//                gameManager.getPaddle().setDx(0);

            if (newState != GameState.RUNNING) {
                if (currentMode.get() == GameMode.ADVENTURE) {
                    if (gameManager.getPaddle() != null) {
                        gameManager.getPaddle().setDx(0);
                    }
                } else {
                    battleManager.stopPlayers();
                }
            }

            if (newState == GameState.MENU && !stateManager.continueAvailableProperty().get()) {
////                stateManager.setStatusMessage("Welcome to Arkanoid!");
//                stateManager.setStatusMessage("Press F1 for Adventure or F2 for Solo Battle.");
                stateManager.setStatusMessage("Select a mode from the main menu to begin.");
            }

            if (newState == GameState.GAME_OVER) {
                String message = stateManager.statusMessageProperty().get();
                if (message == null || message.isBlank()) {
                    stateManager.setStatusMessage("Game Over! Final Score: " + stateManager.getScore());
                }
            }
            if (newState == GameState.PAUSED) {
                SoundManager.getInstance().stopMusic();
                SoundManager.getInstance().play("pause");
                pauseView.show((StackPane) scene.getRoot());
            } else if (newState == GameState.RUNNING) {
                SoundManager.getInstance().playMusic("background.mp3");
                pauseView.hide();
                scene.getRoot().requestFocus();
            }
        });
    }

    private void setupInputHandlers() {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            KeyCode code = e.getCode();
            activeKeys.add(code);

            if (code == KeyCode.ESCAPE) {
                if (stateManager.isRunning()) {
                    stateManager.pauseGame();
                    pauseView.show((StackPane) scene.getRoot());
                } else if (stateManager.getCurrentState() == GameState.PAUSED) {
                    // ẩn overlay + resume
                    pauseView.hide();
                    stateManager.resumeGame();
                    scene.getRoot().requestFocus();
                }
                return;
            }

            if (code == KeyCode.F1) {
                startAdventureMode();
                return;
            }

            if (code == KeyCode.F2) {
                startBattleMode();
                return;
            }


            if (code == KeyCode.ENTER) {
                if (stateManager.getCurrentState() == GameState.MENU ||
                        stateManager.getCurrentState() == GameState.GAME_OVER) {
//                    gameManager.initializeGame();
//                    stateManager.beginNewGame(gameManager.getScore(), gameManager.getLives());
                    if (currentMode.get() == GameMode.LOCAL_BATTLE) {
                        startBattleMode();
                    } else {
                        startAdventureMode();
                    }
                }
                return;
            }

            if (code == KeyCode.SPACE) {
                if (stateManager.isRunning()) {
//                    if (gameManager.getPaddle().isLaserEnabled()) {
//                        gameManager.getPaddle().shootLaser();
//                    }
//                    else {
//                        gameManager.launchBall();
                    if (currentMode.get() == GameMode.LOCAL_BATTLE) {
                        battleManager.launchBall();
                    } else if (gameManager.getPaddle() != null) {
                        if (gameManager.getPaddle().isLaserEnabled()) {
                            gameManager.getPaddle().shootLaser();
                        }
                        else {
                            gameManager.launchBall();
                        }
                    }
                }
                return;
            }

//            if (!stateManager.isRunning() || gameManager.getPaddle() == null) return;
//            if (!pressedStack.contains(code)) {
//                pressedStack.push(code); // đưa phím mới lên đầu
            if (!stateManager.isRunning()) {
                return;
            }

            if (currentMode.get() == GameMode.ADVENTURE) {
                if (gameManager.getPaddle() == null) {
                    return;
                }
                if (!pressedStack.contains(code)) {
                    pressedStack.push(code); // đưa phím mới lên đầu
                }
//            } else if (code == KeyCode.A || code == KeyCode.D || code == KeyCode.LEFT || code == KeyCode.RIGHT) {
            } else if (code == KeyCode.W || code == KeyCode.S || code == KeyCode.UP || code == KeyCode.DOWN) {
                applyBattleMovementFromKeys();
            }
        });

        scene.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
//            pressedStack.remove(e.getCode());
//            // nếu nhả A/D/LEFT/RIGHT mà không còn phím di chuyển nào -> dừng paddle
//            if (!stateManager.isRunning() || gameManager.getPaddle() == null) return;
            KeyCode code = e.getCode();
//            if (code == KeyCode.A || code == KeyCode.D || code == KeyCode.LEFT || code == KeyCode.RIGHT) {
//                // kiểm tra xem trong stack còn phím di chuyển nào không
//                boolean stillMoving = pressedStack.stream().anyMatch(k ->
//                        k == KeyCode.A || k == KeyCode.D || k == KeyCode.LEFT || k == KeyCode.RIGHT);
//                if (!stillMoving) gameManager.getPaddle().setDx(0);
            activeKeys.remove(code);
            pressedStack.remove(code);

            if (!stateManager.isRunning()) {
                if (currentMode.get() == GameMode.LOCAL_BATTLE) {
                    battleManager.stopPlayers();
                }
                return;
            }
//            if (code == KeyCode.B) {
//                gameManager.spawnExtraBall();
            if (currentMode.get() == GameMode.ADVENTURE) {
                if (gameManager.getPaddle() == null) {
                    return;
                }
                if (code == KeyCode.A || code == KeyCode.D || code == KeyCode.LEFT || code == KeyCode.RIGHT) {
                    // kiểm tra xem trong stack còn phím di chuyển nào không
                    boolean stillMoving = pressedStack.stream().anyMatch(k ->
                            k == KeyCode.A || k == KeyCode.D || k == KeyCode.LEFT || k == KeyCode.RIGHT);
                    if (!stillMoving) gameManager.getPaddle().setDx(0);
                }
                if (code == KeyCode.B) {
                    gameManager.spawnExtraBall();
                }
//            } else if (code == KeyCode.A || code == KeyCode.D || code == KeyCode.LEFT || code == KeyCode.RIGHT) {
            } else if (code == KeyCode.W || code == KeyCode.S || code == KeyCode.UP || code == KeyCode.DOWN) {
                applyBattleMovementFromKeys();
            }
        });

        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
//            if (stateManager.isRunning() && event.getButton() == MouseButton.PRIMARY) {
            if (!stateManager.isRunning() || event.getButton() != MouseButton.PRIMARY) {
                return;
            }

            if (currentMode.get() == GameMode.ADVENTURE && gameManager.getPaddle() != null) {
                if (gameManager.getPaddle().isLaserEnabled()) {
                    gameManager.getPaddle().shootLaser();
                }
                else {
                    gameManager.launchBall();
                }
            }
        });

        scene.setOnMouseMoved(this::handleMouseMoved);
        // đảm bảo scene có focus khi đóng overlay
        scene.getRoot().requestFocus();
    }

    private AnimationTimer createGameLoop() {
        return new AnimationTimer() {
            private long lastUpdate = 0L;
            @Override
            public void handle(long now) {
                if (lastUpdate == 0L) {
                    lastUpdate = now;
//                    gameManager.render(graphicsContext);
                    renderCurrentMode();
                    return;
                }

                double dt = (now - lastUpdate) / 1e9;

                if (stateManager.isRunning()) {
//                    updatePaddleVelocity();
//                    double dt = (now - lastUpdate) / 1e9;
//                    gameManager.update(dt);
                    if (currentMode.get() == GameMode.ADVENTURE) {
                        updatePaddleVelocity();
                        gameManager.update(dt);
                    } else {
                        applyBattleMovementFromKeys();
                        battleManager.update(dt);
                    }
                }

                lastUpdate = now;
//                gameManager.render(graphicsContext);
                renderCurrentMode();
            }
        };
    }

    private void updatePaddleVelocity() {
        if (currentMode.get() != GameMode.ADVENTURE) {
            return;
        }
        if (gameManager.getPaddle() == null) {
            return;
        }

        if (pressedStack.isEmpty()) {
            gameManager.getPaddle().setDx(0);
            return;
        }

        KeyCode key = pressedStack.peek();
        if (key == KeyCode.A || key == KeyCode.LEFT) {
            gameManager.getPaddle().setDx(-Constants.PADDLE_SPEED);
        } else if (key == KeyCode.D || key == KeyCode.RIGHT) {
            gameManager.getPaddle().setDx(Constants.PADDLE_SPEED);
        } else {
            gameManager.getPaddle().setDx(0);
        }
    }

    private void applyBattleMovementFromKeys() {
        if (currentMode.get() != GameMode.LOCAL_BATTLE) {
            return;
        }

        double playerOneVelocity = 0;
//        if (activeKeys.contains(KeyCode.A) && !activeKeys.contains(KeyCode.D)) {
        if (activeKeys.contains(KeyCode.W) && !activeKeys.contains(KeyCode.S)) {
            playerOneVelocity = -Constants.PADDLE_SPEED;
//        } else if (activeKeys.contains(KeyCode.D) && !activeKeys.contains(KeyCode.A)) {
        } else if (activeKeys.contains(KeyCode.S) && !activeKeys.contains(KeyCode.W)) {
            playerOneVelocity = Constants.PADDLE_SPEED;
        }

        double playerTwoVelocity = 0;
//        if (activeKeys.contains(KeyCode.LEFT) && !activeKeys.contains(KeyCode.RIGHT)) {
        if (activeKeys.contains(KeyCode.UP) && !activeKeys.contains(KeyCode.DOWN)) {
            playerTwoVelocity = -Constants.PADDLE_SPEED;
//        } else if (activeKeys.contains(KeyCode.RIGHT) && !activeKeys.contains(KeyCode.LEFT)) {
        } else if (activeKeys.contains(KeyCode.DOWN) && !activeKeys.contains(KeyCode.UP)) {
            playerTwoVelocity = Constants.PADDLE_SPEED;
        }

        battleManager.setPlayerOneVelocity(playerOneVelocity);
        battleManager.setPlayerTwoVelocity(playerTwoVelocity);
    }

    private void renderCurrentMode() {
        if (currentMode.get() == GameMode.ADVENTURE) {
            gameManager.render(graphicsContext);
        } else {
            battleManager.render(graphicsContext);
        }
    }

    private void updateLayoutForMode(GameMode mode) {
        boolean battle = mode == GameMode.LOCAL_BATTLE;

        if (leftPanel != null) {
            leftPanel.setVisible(!battle);
            leftPanel.setManaged(!battle);
        }

        if (centerSpacer != null) {
            centerSpacer.setVisible(!battle);
            centerSpacer.setManaged(!battle);
        }

        if (leftColumnConstraint != null && centerColumnConstraint != null && rightColumnConstraint != null) {
            if (battle) {
                leftColumnConstraint.setPercentWidth(0);
                rightColumnConstraint.setPercentWidth(0);
                centerColumnConstraint.setPercentWidth(100);
            } else {
                leftColumnConstraint.setPercentWidth(Constants.SIDE_PANEL_RATIO * 100.0);
                rightColumnConstraint.setPercentWidth(Constants.SIDE_PANEL_RATIO * 100.0);
                centerColumnConstraint.setPercentWidth(Constants.PLAYFIELD_RATIO * 100.0);
            }
        }

        if (backgroundSections != null) {
            if (battle) {
                if (leftBackgroundSection != null) {
                    leftBackgroundSection.setVisible(false);
                    leftBackgroundSection.setManaged(false);
                }
                if (rightBackgroundSection != null) {
                    rightBackgroundSection.setVisible(false);
                    rightBackgroundSection.setManaged(false);
                }
                setSectionWidth(leftBackgroundSection, 0);
                setSectionWidth(rightBackgroundSection, 0);
                setSectionWidth(centerBackgroundSection, Constants.WIDTH);
            } else {
                if (leftBackgroundSection != null) {
                    leftBackgroundSection.setVisible(true);
                    leftBackgroundSection.setManaged(true);
                }
                if (rightBackgroundSection != null) {
                    rightBackgroundSection.setVisible(true);
                    rightBackgroundSection.setManaged(true);
                }
                setSectionWidth(leftBackgroundSection, Constants.LEFT_PANEL_WIDTH);
                setSectionWidth(rightBackgroundSection, Constants.RIGHT_PANEL_WIDTH);
                setSectionWidth(centerBackgroundSection, Constants.PLAYFIELD_WIDTH);
            }
        }
    }

    private void setSectionWidth(StackPane section, double width) {
        if (section == null) {
            return;
        }
        section.setPrefWidth(width);
        section.setMinWidth(width);
        section.setMaxWidth(width);
    }

    private void handleMouseMoved(MouseEvent event) {
//        if (!stateManager.isRunning() || gameManager.getPaddle() == null) {
        if (currentMode.get() != GameMode.ADVENTURE || !stateManager.isRunning() || gameManager.getPaddle() == null) {
            return;
        }
//        gameManager.getPaddle().setX(event.getX() - gameManager.getPaddle().getWidth() / 2);
        double targetX = event.getX() - gameManager.getPaddle().getWidth() / 2;
        double clampedX = Math.max(
                Constants.PLAYFIELD_LEFT,
                Math.min(targetX, Constants.PLAYFIELD_RIGHT - gameManager.getPaddle().getWidth())
        );
        gameManager.getPaddle().setX(clampedX);
    }

    private void startNewGame(ActionEvent event) {
        startAdventureMode();
    }

    private void startAdventureMode() {
        currentMode.set(GameMode.ADVENTURE);
        pressedStack.clear();
        activeKeys.clear();
        gameManager.initializeGame();
        stateManager.beginNewGame(gameManager.getScore(), gameManager.getLives());
//        gameManager.render(graphicsContext);
        stateManager.setStatusMessage("Destroy all the bricks!");
        stateManager.setCurrentRound(1);
        stateManager.updateTimers(0, 0);
        renderCurrentMode();
    }

    private void startBattleMode() {
        currentMode.set(GameMode.LOCAL_BATTLE);
        pressedStack.clear();
        activeKeys.clear();
        battleManager.startMatch();
        resetBattleLabels();
        stateManager.beginNewGame(0, Constants.START_LIVES);
//        stateManager.setStatusMessage("Solo Battle: First to lose all lives loses! Press SPACE to launch.");
//        stateManager.setStatusMessage("Versus Battle: P1 A/D, P2 ←/→. Press SPACE to launch the ball.");
        stateManager.setStatusMessage("Versus Battle: P1 W/S, P2 ↑/↓. Press SPACE to launch the ball.");
        stateManager.setCurrentRound(1);
        stateManager.updateTimers(0, 0);
        renderCurrentMode();
    }

    private Button createMenuButton(String text, EventHandler<ActionEvent> action) {
        Button button = new Button(text);
        button.setMaxWidth(200);
        button.getStyleClass().addAll("btn", "btn-primary");
        button.setOnAction(action);
        return button;
    }

    private Optional<Image> loadImage(String path) {
        URL url = getClass().getResource(path);
        if (url == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(new Image(url.toExternalForm(), true));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public Scene getScene() {
        return scene;
    }

    public GraphicsContext getGraphicsContext() {
        return graphicsContext;
    }
}