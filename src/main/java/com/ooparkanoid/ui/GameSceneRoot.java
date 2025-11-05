//package com.ooparkanoid.ui;
//
//import javafx.animation.*;
//import javafx.application.Platform;
//import javafx.beans.binding.Bindings;
//import javafx.beans.binding.BooleanBinding;
//import javafx.beans.binding.StringBinding;
//import javafx.beans.property.ObjectProperty;
//import javafx.beans.property.SimpleObjectProperty;
//import javafx.beans.value.ObservableNumberValue;
//import javafx.event.ActionEvent;
//import javafx.event.EventHandler;
//import javafx.geometry.*;
//import javafx.scene.Scene;
//import javafx.scene.canvas.Canvas;
//import javafx.scene.canvas.GraphicsContext;
//import javafx.scene.control.Button;
//import javafx.scene.control.Label;
//import javafx.scene.image.Image;
//import javafx.scene.input.*;
//import javafx.scene.layout.*;
//import javafx.scene.paint.Color;
//import javafx.scene.text.Font;
//import javafx.scene.text.FontWeight;
//import javafx.util.Duration;
//
//import com.ooparkanoid.core.engine.GameManager;
//import com.ooparkanoid.core.engine.LocalBattleManager;
//import com.ooparkanoid.core.state.GameMode;
//import com.ooparkanoid.core.state.GameState;
//import com.ooparkanoid.core.state.GameStateManager;
//import com.ooparkanoid.sound.SoundManager;
//import com.ooparkanoid.utils.Constants;
//
//import java.net.URL;
//import java.util.*;
//
///**
// * GameSceneRoot – phiên bản hoàn chỉnh với HUD Adventure dạng "card",
// * ROUND ở panel phải, hiệu ứng glow/flash, overlay Pause + GameOver,
// * và auto ẩn/hiện UI khi chuyển Adventure/Versus.
// */
//public class GameSceneRoot {
//
//    private final Scene scene;
//    private final GraphicsContext graphicsContext;
//    private final GameManager gameManager;
//    private final LocalBattleManager battleManager;
//    private final GameStateManager stateManager;
//    private final AnimationTimer gameLoop;
//
//    // BG chia 3 phần để co giãn khi đổi mode
//    private StackPane leftBackgroundSection;
//    private StackPane centerBackgroundSection;
//    private StackPane rightBackgroundSection;
//    private HBox backgroundSections;
//
//    // HUD
//    private VBox leftPanel;          // Adventure stats (cards)
//    private VBox rightPanel;         // ROUND card
//    private Pane centerSpacer;       // khung playfield border
//    private VBox adventureStats;     // container trái
//    private VBox battleStats;        // (không gắn vào trái trong Adventure)
//    private GridPane hudGrid;
//    private ColumnConstraints leftColumnConstraint;
//    private ColumnConstraints centerColumnConstraint;
//    private ColumnConstraints rightColumnConstraint;
//
//    // Versus scoreboard trên cùng
//    private HBox battleScoreboard;
//    private Label battlePlayerOneLabel;
//    private Label battlePlayerTwoLabel;
//    private Timeline battlePlayerOneFlash;
//    private Timeline battlePlayerTwoFlash;
//    private static final double BATTLE_LABEL_BASE_OPACITY = 0.75;
//
//    private final Canvas canvas;
//    private final SceneLayoutFactory.LayeredScene layeredScene;
//    private final BackgroundLayer backgroundLayer;
//    private final NeonPauseView pauseView;
//    private final GameOverView gameOverView;
//
//    private final Deque<KeyCode> pressedStack = new ArrayDeque<>();
//    private final ObjectProperty<GameMode> currentMode = new SimpleObjectProperty<>(GameMode.ADVENTURE);
//    private final Set<KeyCode> activeKeys = EnumSet.noneOf(KeyCode.class);
//    private final Runnable onExitToMenuCallback;
//    private final GameMode initialMode;
//
//    // === CTORs ===
//    public GameSceneRoot(Runnable onExitToMenuCallback) {
//        this(onExitToMenuCallback, GameMode.ADVENTURE);
//    }
//    public GameSceneRoot() {
//        this(() -> Platform.exit(), GameMode.ADVENTURE);
//    }
//
//    public GameSceneRoot(Runnable onExitToMenuCallback, GameMode initialMode) {
//        this.onExitToMenuCallback = onExitToMenuCallback;
//        this.initialMode = initialMode;
//
//        stateManager = new GameStateManager();
//        gameManager = new GameManager(stateManager);
//        battleManager = new LocalBattleManager(stateManager);
//
//        canvas = new Canvas(Constants.WIDTH, Constants.HEIGHT);
//        graphicsContext = canvas.getGraphicsContext2D();
//
//        layeredScene = SceneLayoutFactory.createLayeredScene(canvas);
//        backgroundLayer = layeredScene.backgroundLayer();
//        scene = new Scene(layeredScene.root(), Constants.WIDTH, Constants.HEIGHT);
//
//        configureBackground();
//        buildHud();
//
//        // Overlays
//        pauseView = new NeonPauseView(new NeonPauseView.Callbacks() {
//            @Override public void onResume() {
//                pauseView.hide();
//                stateManager.resumeGame();
//                scene.getRoot().requestFocus();
//            }
//            @Override public void onExit() {
//                gameLoop.stop();
//                SoundManager.getInstance().stopMusic();
//                onExitToMenuCallback.run();
//            }
//        });
//        gameOverView = new GameOverView(new GameOverView.Callbacks() {
//            @Override public void onExit() {
//                gameLoop.stop();
//                SoundManager.getInstance().stopMusic();
//                onExitToMenuCallback.run();
//            }
//        });
//        layeredScene.root().getChildren().addAll(pauseView.getView(), gameOverView.getView());
//
//        setupStateListeners();
//        setupInputHandlers();
//        currentMode.addListener((obs, o, n) -> updateLayoutForMode(n));
//
//        gameLoop = createGameLoop();
//        if (initialMode == GameMode.LOCAL_BATTLE) startBattleMode(); else startAdventureMode();
//        updateLayoutForMode(currentMode.get());
//        gameLoop.start();
//    }
//
//    // ===================== Background =====================
//    private void configureBackground() {
//        backgroundLayer.setFill(Color.BLACK);
//        backgroundLayer.setImageLayers(null);
//        backgroundLayer.getChildren().clear();
//
//        leftBackgroundSection   = createBackgroundSection(Constants.LEFT_PANEL_WIDTH,  "background-left");
//        centerBackgroundSection = createBackgroundSection(Constants.PLAYFIELD_WIDTH,   "background-center");
//        rightBackgroundSection  = createBackgroundSection(Constants.RIGHT_PANEL_WIDTH, "background-right");
//
//        BackgroundFill sideFill = new BackgroundFill(Color.rgb(8, 12, 28, 0.88), CornerRadii.EMPTY, Insets.EMPTY);
//        leftBackgroundSection.setBackground(new Background(sideFill));
//        rightBackgroundSection.setBackground(new Background(sideFill));
//
//        BackgroundFill centerFill = new BackgroundFill(Color.rgb(6, 10, 30), CornerRadii.EMPTY, Insets.EMPTY);
//        Optional<Image> backdrop = loadImage("/picture/space1.png");
//        if (backdrop.isPresent()) {
//            BackgroundImage coverImage = BackgroundLayer.cover(backdrop.get());
//            centerBackgroundSection.setBackground(new Background(new BackgroundFill[]{centerFill}, new BackgroundImage[]{coverImage}));
//        } else {
//            centerBackgroundSection.setBackground(new Background(centerFill));
//        }
//
//        Optional<Image> sideImg = loadImage("/picture/menu1.jpg");
//        sideImg.ifPresent(img -> {
//            BackgroundImage bg = BackgroundLayer.cover(img);
//            leftBackgroundSection.setBackground(new Background(new BackgroundFill[]{sideFill}, new BackgroundImage[]{bg}));
//            rightBackgroundSection.setBackground(new Background(new BackgroundFill[]{sideFill}, new BackgroundImage[]{bg}));
//        });
//
//        backgroundSections = new HBox(leftBackgroundSection, centerBackgroundSection, rightBackgroundSection);
//        backgroundSections.setPrefSize(Constants.WIDTH, Constants.HEIGHT);
//        backgroundSections.setMouseTransparent(true);
//        backgroundLayer.getChildren().add(backgroundSections);
//    }
//
//    private StackPane createBackgroundSection(double width, String styleClass) {
//        StackPane section = new StackPane();
//        section.setPrefSize(width, Constants.HEIGHT);
//        section.setMinSize(width, Constants.HEIGHT);
//        section.setMaxSize(width, Constants.HEIGHT);
//        section.getStyleClass().add(styleClass);
//        return section;
//    }
//
//    // ===================== HUD =====================
//    private void buildHud() {
//        // Bindings hiển thị giống ảnh mẫu
//        StringBinding onePScoreBinding = Bindings.createStringBinding(
//                () -> String.format("%07d", Math.max(0, stateManager.getScore())),
//                stateManager.scoreProperty());
//        StringBinding roundTimeBinding = formatDurationBinding(stateManager.roundTimeProperty(), "");
//        StringBinding gameTimeBinding  = formatDurationBinding(stateManager.totalTimeProperty(), "");
//        StringBinding livesBinding     = Bindings.createStringBinding(
//                () -> String.format("%d", Math.max(0, stateManager.livesProperty().get())),
//                stateManager.livesProperty());
//
//        // Cards bên trái
//        VBox scoreCard = createScoreCard(onePScoreBinding);           // nền đỏ nhẹ + số dương
//        VBox roundCard = createStatCard("Round Time", roundTimeBinding);
//        VBox gameCard  = createStatCard("Game Time",  gameTimeBinding);
//        VBox livesCard = createStatCard("Lives",      livesBinding);
//
//        roundCard.setBackground(Background.EMPTY);
//        gameCard.setBackground(Background.EMPTY);
//        livesCard.setBackground(Background.EMPTY);
//
//        adventureStats = new VBox(12, scoreCard, roundCard, gameCard, livesCard);
//        adventureStats.setAlignment(Pos.TOP_LEFT);
//
//        leftPanel = new VBox(12, adventureStats);
//        leftPanel.setAlignment(Pos.TOP_LEFT);
//        leftPanel.setPadding(new Insets(24, 18, 24, 24));
//        leftPanel.setBackground(createPanelBackground());
//        leftPanel.setPrefWidth(Constants.LEFT_PANEL_WIDTH);
//        leftPanel.setMinWidth(Constants.LEFT_PANEL_WIDTH);
//        leftPanel.setMaxWidth(Constants.LEFT_PANEL_WIDTH);
//
//        // Panel phải – ROUND to
//        VBox roundOnly = createRoundCard();
//        rightPanel = new VBox(roundOnly);
//        rightPanel.setAlignment(Pos.TOP_RIGHT);
//        rightPanel.setPadding(new Insets(24, 24, 24, 18));
//        rightPanel.setBackground(createPanelBackground());
//        rightPanel.setPrefWidth(Constants.RIGHT_PANEL_WIDTH);
//        rightPanel.setMinWidth(Constants.RIGHT_PANEL_WIDTH);
//        rightPanel.setMaxWidth(Constants.RIGHT_PANEL_WIDTH);
//        roundOnly.setBackground(Background.EMPTY);
//        rightPanel.setBackground(Background.EMPTY);
//        // Grid 3 cột
//        hudGrid = new GridPane();
//        hudGrid.setMouseTransparent(true);
//        hudGrid.setPickOnBounds(false);
//        hudGrid.setPrefSize(Constants.WIDTH, Constants.HEIGHT);
//
//        leftColumnConstraint   = createColumn(Constants.SIDE_PANEL_RATIO);
//        centerColumnConstraint = createColumn(Constants.PLAYFIELD_RATIO);
//        rightColumnConstraint  = createColumn(Constants.SIDE_PANEL_RATIO);
//        hudGrid.getColumnConstraints().addAll(leftColumnConstraint, centerColumnConstraint, rightColumnConstraint);
//
//        centerSpacer = new Pane();
//        centerSpacer.setMinSize(0, 0);
//        centerSpacer.setMouseTransparent(true);
//        centerSpacer.setStyle("-fx-border-color: rgba(255,255,255,0.18); -fx-border-width: 0 2 0 2;");
//
//        GridPane.setHalignment(leftPanel, HPos.LEFT);
//        GridPane.setValignment(leftPanel, VPos.TOP);
//        GridPane.setHalignment(rightPanel, HPos.RIGHT);
//        GridPane.setValignment(rightPanel, VPos.TOP);
//        GridPane.setHgrow(centerSpacer, Priority.ALWAYS);
//
//        hudGrid.add(leftPanel, 0, 0);
//        hudGrid.add(centerSpacer, 1, 0);
//        hudGrid.add(rightPanel, 2, 0);
//
//        BooleanBinding hudVisible = stateManager.stateProperty().isEqualTo(GameState.RUNNING);
//        hudGrid.visibleProperty().bind(hudVisible);
//        hudGrid.managedProperty().bind(hudVisible);
//
//        layeredScene.contentLayer().getChildren().add(hudGrid);
//        StackPane.setAlignment(hudGrid, Pos.CENTER);
//
//        createBattleScoreboard();
//    }
//
//    private Background createPanelBackground() {
//       // return new Background(new BackgroundFill(Color.color(0, 0, 0, 0.45), new CornerRadii(12), Insets.EMPTY));
//        return Background.EMPTY;
//    }
//
//    private ColumnConstraints createColumn(double ratio) {
//        ColumnConstraints column = new ColumnConstraints();
//        column.setPercentWidth(ratio * 100.0);
//        column.setHalignment(HPos.CENTER);
//        return column;
//    }
//
//    private StringBinding formatDurationBinding(ObservableNumberValue secondsProperty, String label) {
//        return Bindings.createStringBinding(() -> {
//            long totalSeconds = (long) Math.floor(secondsProperty.doubleValue());
//            long minutes = totalSeconds / 60;
//            long seconds = totalSeconds % 60;
//            return String.format("%s%02d:%02d", label, minutes, seconds);
//        }, secondsProperty);
//    }
//
//    // ======= Cards ========
//    private VBox createStatCard(String title, StringBinding valueBinding) {
//        Label titleLbl = new Label(title.toUpperCase());
//        titleLbl.setTextFill(Color.web("#FFDFDF"));
//        titleLbl.setFont(Font.font("Arial", FontWeight.BOLD, 14));
//
//        Label valueLbl = new Label();
//        valueLbl.textProperty().bind(valueBinding);
//        valueLbl.setTextFill(Color.WHITE);
//        valueLbl.setFont(Font.font("Arial", FontWeight.BOLD, 24));
//
//        VBox box = new VBox(4, titleLbl, valueLbl);
//        box.setPadding(new Insets(10, 14, 10, 14));
//        box.setBackground(new Background(new BackgroundFill(Color.color(0, 0, 0, 0.60), new CornerRadii(12), Insets.EMPTY)));
//        box.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.55), 16, 0.4, 0, 6);");
//        addCardGlowOnChange(valueLbl);
//        return box;
//    }
//
//    private VBox createScoreCard(StringBinding scoreBinding) {
//        Label titleLbl = new Label("1P SCORE");
//        titleLbl.setTextFill(Color.WHITE);
//        titleLbl.setFont(Font.font("Arial", FontWeight.BOLD, 14));
//
//        Label valueLbl = new Label();
//        valueLbl.textProperty().bind(scoreBinding);
//        valueLbl.setTextFill(Color.WHITE);
//        valueLbl.setFont(Font.font("Arial", FontWeight.BOLD, 26));
//
//        VBox box = new VBox(4, titleLbl, valueLbl);
//        box.setPadding(new Insets(10, 14, 10, 14));
//        box.setBackground(new Background(new BackgroundFill(Color.rgb(160, 35, 35, 0.75), new CornerRadii(12), Insets.EMPTY)));
//        box.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 18, 0.45, 0, 7);");
//        addCardGlowOnChange(valueLbl);
//        return box;
//    }
//
//    private VBox createRoundCard() {
//        Label rTitle = new Label("ROUND");
//        rTitle.setTextFill(Color.LIMEGREEN);
//        rTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));
//
//        Label rValue = new Label();
//        rValue.textProperty().bind(Bindings.createStringBinding(
//                () -> String.format("%02d", Math.max(1, stateManager.roundProperty().get())),
//                stateManager.roundProperty()));
//        rValue.setTextFill(Color.WHITE);
//        rValue.setFont(Font.font("Arial", FontWeight.BOLD, 64));
//
//        VBox box = new VBox(4, rTitle, rValue);
//        box.setAlignment(Pos.TOP_RIGHT);
//        box.setPadding(new Insets(18, 18, 18, 18));
//        box.setBackground(new Background(new BackgroundFill(Color.color(0, 0, 0, 0.55), new CornerRadii(14), Insets.EMPTY)));
//        box.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.55), 18, 0.45, 0, 7);");
//        addCardGlowOnChange(rValue);
//        return box;
//    }
//
//    // Hiệu ứng nhẹ khi giá trị thay đổi (score/time/lives/round)
//    private void addCardGlowOnChange(Label valueLbl) {
//        valueLbl.textProperty().addListener((obs, o, n) -> {
//            Timeline t = new Timeline(
//                    new KeyFrame(Duration.ZERO,
//                            new KeyValue(valueLbl.opacityProperty(), 0.65),
//                            new KeyValue(valueLbl.scaleXProperty(), 1.00),
//                            new KeyValue(valueLbl.scaleYProperty(), 1.00)),
//                    new KeyFrame(Duration.millis(120),
//                            new KeyValue(valueLbl.opacityProperty(), 1.0),
//                            new KeyValue(valueLbl.scaleXProperty(), 1.06),
//                            new KeyValue(valueLbl.scaleYProperty(), 1.06)),
//                    new KeyFrame(Duration.millis(260),
//                            new KeyValue(valueLbl.opacityProperty(), 1.0),
//                            new KeyValue(valueLbl.scaleXProperty(), 1.00),
//                            new KeyValue(valueLbl.scaleYProperty(), 1.00))
//            );
//            t.play();
//        });
//    }
//
//    // ===================== Versus Scoreboard =====================
//    private void createBattleScoreboard() {
//        battlePlayerOneLabel = createBattleCounterLabel("P1: 0", Color.web("#FF6F61"));
//        battlePlayerTwoLabel = createBattleCounterLabel("P2: 0", Color.web("#3FA9F5"));
//        Region spacer = new Region();
//        HBox.setHgrow(spacer, Priority.ALWAYS);
//        battleScoreboard = new HBox(60, battlePlayerOneLabel, spacer, battlePlayerTwoLabel);
//        battleScoreboard.setAlignment(Pos.TOP_CENTER);
//        battleScoreboard.setPadding(new Insets(20, 48, 0, 48));
//        battleScoreboard.setMouseTransparent(true);
//        BooleanBinding battleVisible = currentMode.isEqualTo(GameMode.LOCAL_BATTLE);
//        battleScoreboard.visibleProperty().bind(battleVisible);
//        battleScoreboard.managedProperty().bind(battleVisible);
//        layeredScene.overlayLayer().getChildren().add(battleScoreboard);
//        StackPane.setAlignment(battleScoreboard, Pos.TOP_CENTER);
//
//        battleManager.playerOneLivesProperty().addListener((obs, oldVal, newVal) -> {
//            updateBattleLabel(battlePlayerOneLabel, formatBattleCounterText("P1", newVal));
//            if (oldVal != null && newVal != null && newVal.intValue() < oldVal.intValue()) playBattleLabelFlash(battlePlayerOneLabel, true);
//            else resetBattleLabelOpacity(battlePlayerOneLabel);
//        });
//        battleManager.playerTwoLivesProperty().addListener((obs, oldVal, newVal) -> {
//            updateBattleLabel(battlePlayerTwoLabel, formatBattleCounterText("P2", newVal));
//            if (oldVal != null && newVal != null && newVal.intValue() < oldVal.intValue()) playBattleLabelFlash(battlePlayerTwoLabel, false);
//            else resetBattleLabelOpacity(battlePlayerTwoLabel);
//        });
//        resetBattleLabels();
//    }
//
//    private Label createBattleCounterLabel(String text, Color accentColor) {
//        Label label = new Label(text);
//        label.setTextFill(Color.WHITE);
//        label.setFont(Font.font("Arial", FontWeight.BOLD, 48));
//        label.setAlignment(Pos.CENTER);
//        label.setPadding(new Insets(8, 28, 8, 28));
//        label.setMinWidth(200);
//        label.setBackground(new Background(new BackgroundFill(accentColor.deriveColor(0, 1, 1, 0.38), new CornerRadii(20), Insets.EMPTY)));
//        label.setOpacity(BATTLE_LABEL_BASE_OPACITY);
//        label.setMouseTransparent(true);
//        label.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.55), 18, 0.4, 0, 6);");
//        return label;
//    }
//    private String formatBattleCounterText(String playerLabel, Number lives) { int v = lives==null?0:lives.intValue(); return String.format("%s: %d", playerLabel, Math.max(0, v)); }
//    private void updateBattleLabel(Label label, String text) { if (label!=null) label.setText(text); }
//    private void resetBattleLabelOpacity(Label label) { if (label!=null) label.setOpacity(BATTLE_LABEL_BASE_OPACITY); }
//    private void playBattleLabelFlash(Label label, boolean playerOne) {
//        if (label == null) return;
//        Timeline existing = playerOne ? battlePlayerOneFlash : battlePlayerTwoFlash;
//        if (existing != null) existing.stop();
//        Timeline t = new Timeline(
//                new KeyFrame(Duration.ZERO, new KeyValue(label.opacityProperty(), BATTLE_LABEL_BASE_OPACITY)),
//                new KeyFrame(Duration.millis(140), new KeyValue(label.opacityProperty(), 1.0)),
//                new KeyFrame(Duration.millis(360), new KeyValue(label.opacityProperty(), BATTLE_LABEL_BASE_OPACITY)));
//        t.setOnFinished(ev -> label.setOpacity(BATTLE_LABEL_BASE_OPACITY));
//        t.play();
//        if (playerOne) battlePlayerOneFlash = t; else battlePlayerTwoFlash = t;
//    }
//    private void resetBattleLabels() {
//        updateBattleLabel(battlePlayerOneLabel, formatBattleCounterText("P1", battleManager.playerOneLivesProperty().get()));
//        updateBattleLabel(battlePlayerTwoLabel, formatBattleCounterText("P2", battleManager.playerTwoLivesProperty().get()));
//        resetBattleLabelOpacity(battlePlayerOneLabel);
//        resetBattleLabelOpacity(battlePlayerTwoLabel);
//    }
//
//    // ===================== State & Input =====================
//    private void setupStateListeners() {
//        stateManager.stateProperty().addListener((obs, oldState, newState) -> {
//            if (newState != GameState.RUNNING) {
//                if (currentMode.get() == GameMode.ADVENTURE) {
//                    if (gameManager.getPaddle() != null) gameManager.getPaddle().setDx(0);
//                } else {
//                    battleManager.stopPlayers();
//                }
//            }
//            if (newState == GameState.MENU && !stateManager.continueAvailableProperty().get()) {
//                stateManager.setStatusMessage("Select a mode from the main menu to begin.");
//            }
//            if (newState == GameState.GAME_OVER) {
//                if (stateManager.statusMessageProperty().get()==null || stateManager.statusMessageProperty().get().isBlank())
//                    stateManager.setStatusMessage("Game Over! Final Score: " + stateManager.getScore());
//                gameLoop.stop();
//                SoundManager.getInstance().stopMusic();
//                gameOverView.show();
//            }
//            if (newState == GameState.PAUSED) {
//                SoundManager.getInstance().stopMusic();
//                SoundManager.getInstance().play("pause");
//                pauseView.show((StackPane) scene.getRoot());
//            } else if (newState == GameState.RUNNING) {
//                SoundManager.getInstance().playMusic("background.mp3");
//                pauseView.hide();
//                scene.getRoot().requestFocus();
//            }
//        });
//    }
//
//    private void setupInputHandlers() {
//        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
//            KeyCode code = e.getCode();
//            activeKeys.add(code);
//            if (code == KeyCode.ESCAPE) {
//                if (stateManager.isRunning()) {
//                    stateManager.pauseGame();
//                    pauseView.show((StackPane) scene.getRoot());
//                } else if (stateManager.getCurrentState() == GameState.PAUSED) {
//                    pauseView.hide();
//                    stateManager.resumeGame();
//                    scene.getRoot().requestFocus();
//                }
//                return;
//            }
//            if (code == KeyCode.F1) { startAdventureMode(); return; }
//            if (code == KeyCode.F2) { startBattleMode();    return; }
//            if (code == KeyCode.ENTER) {
//                if (stateManager.getCurrentState() == GameState.MENU) {
//                    if (currentMode.get() == GameMode.LOCAL_BATTLE) startBattleMode(); else startAdventureMode();
//                }
//                return;
//            }
//            if (code == KeyCode.SPACE) {
//                if (stateManager.isRunning()) {
//                    if (currentMode.get() == GameMode.LOCAL_BATTLE) battleManager.launchBall();
//                    else if (gameManager.getPaddle()!=null) {
//                        if (gameManager.getPaddle().isLaserEnabled()) gameManager.getPaddle().shootLaser();
//                        else gameManager.launchBall();
//                    }
//                }
//                return;
//            }
//            if (!stateManager.isRunning()) return;
//
//            if (currentMode.get() == GameMode.ADVENTURE) {
//                if (gameManager.getPaddle() == null) return;
//                if ((code==KeyCode.A || code==KeyCode.D || code==KeyCode.LEFT || code==KeyCode.RIGHT)) {
//                    if (!pressedStack.contains(code)) pressedStack.push(code);
//                }
//            } else { // Versus
//                if (code==KeyCode.W || code==KeyCode.S || code==KeyCode.UP || code==KeyCode.DOWN) applyBattleMovementFromKeys();
//            }
//        });
//
//        scene.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
//            KeyCode code = e.getCode();
//            activeKeys.remove(code);
//            pressedStack.remove(code);
//            if (!stateManager.isRunning()) {
//                if (currentMode.get() == GameMode.LOCAL_BATTLE) battleManager.stopPlayers();
//                return;
//            }
//            if (currentMode.get() == GameMode.ADVENTURE) {
//                if (gameManager.getPaddle()==null) return;
//                if (code==KeyCode.A || code==KeyCode.D || code==KeyCode.LEFT || code==KeyCode.RIGHT) {
//                    boolean still = pressedStack.stream().anyMatch(k -> k==KeyCode.A || k==KeyCode.D || k==KeyCode.LEFT || k==KeyCode.RIGHT);
//                    if (!still) gameManager.getPaddle().setDx(0);
//                }
//                if (code==KeyCode.B) gameManager.spawnExtraBall();
//            } else {
//                if (code==KeyCode.W || code==KeyCode.S || code==KeyCode.UP || code==KeyCode.DOWN) applyBattleMovementFromKeys();
//            }
//        });
//
//        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
//            if (!stateManager.isRunning() || event.getButton()!=MouseButton.PRIMARY) return;
//            if (currentMode.get()==GameMode.ADVENTURE && gameManager.getPaddle()!=null) {
//                if (gameManager.getPaddle().isLaserEnabled()) gameManager.getPaddle().shootLaser();
//                else gameManager.launchBall();
//            }
//        });
//
//        scene.setOnMouseMoved(this::handleMouseMoved);
//        scene.getRoot().requestFocus();
//    }
//
//    // ===================== Game loop =====================
//    private AnimationTimer createGameLoop() {
//        return new AnimationTimer() {
//            private long lastUpdate = 0L;
//            @Override public void handle(long now) {
//                if (lastUpdate == 0L) { lastUpdate = now; renderCurrentMode(); return; }
//                double dt = (now - lastUpdate) / 1e9;
//                if (stateManager.isRunning()) {
//                    if (currentMode.get() == GameMode.ADVENTURE) { updatePaddleVelocity(); gameManager.update(dt); }
//                    else { applyBattleMovementFromKeys(); battleManager.update(dt); }
//                }
//                lastUpdate = now; renderCurrentMode();
//            }
//        }; }
//
//    private void updatePaddleVelocity() {
//        if (currentMode.get()!=GameMode.ADVENTURE || gameManager.getPaddle()==null) return;
//        if (pressedStack.isEmpty()) { gameManager.getPaddle().setDx(0); return; }
//        KeyCode key = pressedStack.peek();
//        if (key==KeyCode.A || key==KeyCode.LEFT) gameManager.getPaddle().setDx(-Constants.PADDLE_SPEED);
//        else if (key==KeyCode.D || key==KeyCode.RIGHT) gameManager.getPaddle().setDx(Constants.PADDLE_SPEED);
//        else gameManager.getPaddle().setDx(0);
//    }
//
//    private void applyBattleMovementFromKeys() {
//        if (currentMode.get()!=GameMode.LOCAL_BATTLE) return;
//        double v1 = 0, v2 = 0;
//        if (activeKeys.contains(KeyCode.W) && !activeKeys.contains(KeyCode.S)) v1 = -Constants.PADDLE_SPEED;
//        else if (activeKeys.contains(KeyCode.S) && !activeKeys.contains(KeyCode.W)) v1 = Constants.PADDLE_SPEED;
//        if (activeKeys.contains(KeyCode.UP) && !activeKeys.contains(KeyCode.DOWN)) v2 = -Constants.PADDLE_SPEED;
//        else if (activeKeys.contains(KeyCode.DOWN) && !activeKeys.contains(KeyCode.UP)) v2 = Constants.PADDLE_SPEED;
//        battleManager.setPlayerOneVelocity(v1);
//        battleManager.setPlayerTwoVelocity(v2);
//    }
//
//    private void renderCurrentMode() {
//        if (currentMode.get()==GameMode.ADVENTURE) gameManager.render(graphicsContext); else battleManager.render(graphicsContext);
//    }
//
//    private void handleMouseMoved(MouseEvent event) {
//        if (currentMode.get()!=GameMode.ADVENTURE || !stateManager.isRunning() || gameManager.getPaddle()==null) return;
//        double targetX = event.getX() - gameManager.getPaddle().getWidth()/2;
//        double clampedX = Math.max(Constants.PLAYFIELD_LEFT, Math.min(targetX, Constants.PLAYFIELD_RIGHT - gameManager.getPaddle().getWidth()));
//        gameManager.getPaddle().setX(clampedX);
//    }
//
//    // ===================== Mode switching =====================
//    private void startAdventureMode() {
//        currentMode.set(GameMode.ADVENTURE);
//        pressedStack.clear(); activeKeys.clear();
//        gameManager.initializeGame();
//        stateManager.beginNewGame(gameManager.getScore(), gameManager.getLives());
//        stateManager.setStatusMessage("Destroy all the bricks!");
//        stateManager.setCurrentRound(1);
//        stateManager.updateTimers(0, 0);
//        renderCurrentMode();
//    }
//    private void startBattleMode() {
//        currentMode.set(GameMode.LOCAL_BATTLE);
//        pressedStack.clear(); activeKeys.clear();
//        battleManager.startMatch();
//        resetBattleLabels();
//        stateManager.beginNewGame(0, Constants.START_LIVES);
//        stateManager.setStatusMessage("Versus Battle: P1 W/S, P2 ↑/↓. Press SPACE to launch the ball.");
//        stateManager.setCurrentRound(1);
//        stateManager.updateTimers(0, 0);
//        renderCurrentMode();
//    }
//
//    private void updateLayoutForMode(GameMode mode) {
//        boolean battle = mode == GameMode.LOCAL_BATTLE;
//        if (leftPanel != null)  { leftPanel.setVisible(!battle);  leftPanel.setManaged(!battle); }
//        if (rightPanel != null) { rightPanel.setVisible(!battle); rightPanel.setManaged(!battle); }
//        if (centerSpacer != null) { centerSpacer.setVisible(!battle); centerSpacer.setManaged(!battle); }
//
//        if (leftColumnConstraint!=null && centerColumnConstraint!=null && rightColumnConstraint!=null) {
//            if (battle) {
//                leftColumnConstraint.setPercentWidth(0);
//                rightColumnConstraint.setPercentWidth(0);
//                centerColumnConstraint.setPercentWidth(100);
//            } else {
//                leftColumnConstraint.setPercentWidth(Constants.SIDE_PANEL_RATIO * 100.0);
//                rightColumnConstraint.setPercentWidth(Constants.SIDE_PANEL_RATIO * 100.0);
//                centerColumnConstraint.setPercentWidth(Constants.PLAYFIELD_RATIO * 100.0);
//            }
//        }
//        if (backgroundSections != null) {
//            if (battle) {
//                if (leftBackgroundSection!=null)  { leftBackgroundSection.setVisible(false);  leftBackgroundSection.setManaged(false); }
//                if (rightBackgroundSection!=null) { rightBackgroundSection.setVisible(false); rightBackgroundSection.setManaged(false); }
//                setSectionWidth(leftBackgroundSection, 0);
//                setSectionWidth(rightBackgroundSection, 0);
//                setSectionWidth(centerBackgroundSection, Constants.WIDTH);
//            } else {
//                if (leftBackgroundSection!=null)  { leftBackgroundSection.setVisible(true);  leftBackgroundSection.setManaged(true); }
//                if (rightBackgroundSection!=null) { rightBackgroundSection.setVisible(true); rightBackgroundSection.setManaged(true); }
//                setSectionWidth(leftBackgroundSection, Constants.LEFT_PANEL_WIDTH);
//                setSectionWidth(rightBackgroundSection, Constants.RIGHT_PANEL_WIDTH);
//                setSectionWidth(centerBackgroundSection, Constants.PLAYFIELD_WIDTH);
//            }
//        }
//    }
//
//    private void setSectionWidth(StackPane section, double width) {
//        if (section == null) return;
//        section.setPrefWidth(width);
//        section.setMinSize(width, Constants.HEIGHT);
//        section.setMaxWidth(width);
//    }
//
//    // ===================== Utils =====================
//    private Button createMenuButton(String text, EventHandler<ActionEvent> action) {
//        Button button = new Button(text);
//        button.setMaxWidth(200);
//        button.getStyleClass().addAll("btn", "btn-primary");
//        button.setOnAction(action);
//        return button;
//    }
//    private Optional<Image> loadImage(String path) {
//        URL url = getClass().getResource(path);
//        if (url == null) return Optional.empty();
//        try { return Optional.of(new Image(url.toExternalForm(), true)); } catch (IllegalArgumentException ex) { return Optional.empty(); }
//    }
//
//    // ===================== Accessors =====================
//    public Scene getScene() { return scene; }
//    public GraphicsContext getGraphicsContext() { return graphicsContext; }
//}
//
package com.ooparkanoid.ui;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableNumberValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import com.ooparkanoid.core.engine.GameManager;
import com.ooparkanoid.core.engine.LocalBattleManager;
import com.ooparkanoid.core.state.GameMode;
import com.ooparkanoid.core.state.GameState;
import com.ooparkanoid.core.state.GameStateManager;
import com.ooparkanoid.sound.SoundManager;
import com.ooparkanoid.utils.Constants;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * Lớp điều phối – ghép BackgroundSections, AdventureHud, BattleScoreboard.
 * Giữ nguyên logic game loop, input, switching mode và overlay.
 */
public class GameSceneRoot {

    private final Scene scene;
    private final GraphicsContext graphicsContext;
    private final GameManager gameManager;
    private final LocalBattleManager battleManager;
    private final GameStateManager stateManager;
    private final AnimationTimer gameLoop;

    private final Canvas canvas;
    private final SceneLayoutFactory.LayeredScene layeredScene;
    private final BackgroundLayer backgroundLayer;

    // Background (3 phần, co giãn theo mode)
    private final BackgroundSections backgroundSections;

    // HUD Adventure (trái/phải/khung center)
    private final AdventureHud adventureHud;

    // Versus scoreboard
    private final BattleScoreboard battleScoreboard;

    // Overlay (đã có sẵn trong project của bạn)
    private final NeonPauseView pauseView;
    private final GameOverView gameOverView;

    private final Deque<KeyCode> pressedStack = new ArrayDeque<>();
    private final Set<KeyCode> activeKeys = EnumSet.noneOf(KeyCode.class);
    private final ObjectProperty<GameMode> currentMode = new SimpleObjectProperty<>(GameMode.ADVENTURE);

    private final Runnable onExitToMenuCallback;
    private final GameMode initialMode;

    // === CTORs ===
    public GameSceneRoot(Runnable onExitToMenuCallback) {
        this(onExitToMenuCallback, GameMode.ADVENTURE);
    }
    public GameSceneRoot() {
        this(Platform::exit, GameMode.ADVENTURE);
    }
    public GameSceneRoot(Runnable onExitToMenuCallback, GameMode initialMode) {
        this.onExitToMenuCallback = onExitToMenuCallback;
        this.initialMode = initialMode;

        stateManager = new GameStateManager();
        gameManager = new GameManager(stateManager);
        battleManager = new LocalBattleManager(stateManager);

        canvas = new Canvas(Constants.WIDTH, Constants.HEIGHT);
        graphicsContext = canvas.getGraphicsContext2D();

        layeredScene = SceneLayoutFactory.createLayeredScene(canvas);
        backgroundLayer = layeredScene.backgroundLayer();
        scene = new Scene(layeredScene.root(), Constants.WIDTH, Constants.HEIGHT);

        // ===== Background =====
        backgroundLayer.setFill(Color.BLACK);
        backgroundLayer.setImageLayers(null);
        backgroundLayer.getChildren().clear();
        backgroundSections = new BackgroundSections(Constants.WIDTH, Constants.HEIGHT);
        backgroundLayer.getChildren().add(backgroundSections.getRoot());

        // ===== HUD Adventure =====
        adventureHud = new AdventureHud(stateManager);
        layeredScene.contentLayer().getChildren().add(adventureHud.getGrid());
        StackPane.setAlignment(adventureHud.getGrid(), Pos.CENTER);

        // HUD chỉ hiện khi RUNNING
        BooleanBinding hudVisible = stateManager.stateProperty().isEqualTo(GameState.RUNNING);
        adventureHud.getGrid().visibleProperty().bind(hudVisible);
        adventureHud.getGrid().managedProperty().bind(hudVisible);

        // ===== Battle scoreboard (overlay) =====
        battleScoreboard = new BattleScoreboard();
        battleScoreboard.bindTo(battleManager, currentMode);
        layeredScene.overlayLayer().getChildren().add(battleScoreboard.getRoot());
        StackPane.setAlignment(battleScoreboard.getRoot(), Pos.TOP_CENTER);
        StackPane.setMargin(battleScoreboard.getRoot(), new Insets(20, 48, 0, 48));

        // ===== Overlays =====
        pauseView = new NeonPauseView(new NeonPauseView.Callbacks() {
            @Override public void onResume() {
                pauseView.hide();
                stateManager.resumeGame();
                scene.getRoot().requestFocus();
            }
            @Override public void onExit() {
                gameLoop.stop();
                SoundManager.getInstance().stopMusic();
                onExitToMenuCallback.run();
            }
        });
        gameOverView = new GameOverView(new GameOverView.Callbacks() {
            @Override public void onExit() {
                gameLoop.stop();
                SoundManager.getInstance().stopMusic();
                onExitToMenuCallback.run();
            }
        });
        layeredScene.root().getChildren().addAll(pauseView.getView(), gameOverView.getView());

        // ===== State & input =====
        setupStateListeners();
        setupInputHandlers();

        currentMode.addListener((obs, o, n) -> updateLayoutForMode(n));

        // ===== Game loop =====
        gameLoop = createGameLoop();
        if (initialMode == GameMode.LOCAL_BATTLE) startBattleMode(); else startAdventureMode();
        updateLayoutForMode(currentMode.get());
        gameLoop.start();
    }

    // ===================== State & Input =====================
    private void setupStateListeners() {
        stateManager.stateProperty().addListener((obs, oldState, newState) -> {
            if (newState != GameState.RUNNING) {
                if (currentMode.get() == GameMode.ADVENTURE) {
                    if (gameManager.getPaddle() != null) gameManager.getPaddle().setDx(0);
                } else {
                    battleManager.stopPlayers();
                }
            }
            if (newState == GameState.MENU && !stateManager.continueAvailableProperty().get()) {
                stateManager.setStatusMessage("Select a mode from the main menu to begin.");
            }
            if (newState == GameState.GAME_OVER) {
                if (stateManager.statusMessageProperty().get()==null || stateManager.statusMessageProperty().get().isBlank())
                    stateManager.setStatusMessage("Game Over! Final Score: " + stateManager.getScore());
                gameLoop.stop();
                SoundManager.getInstance().stopMusic();
                gameOverView.show();
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
            switch (code) {
                case ESCAPE -> {
                    if (stateManager.isRunning()) {
                        stateManager.pauseGame();
                        pauseView.show((StackPane) scene.getRoot());
                    } else if (stateManager.getCurrentState() == GameState.PAUSED) {
                        pauseView.hide();
                        stateManager.resumeGame();
                        scene.getRoot().requestFocus();
                    }
                    return;
                }
                case F1 -> { startAdventureMode(); return; }
                case F2 -> { startBattleMode();    return; }
                case ENTER -> {
                    if (stateManager.getCurrentState() == GameState.MENU) {
                        if (currentMode.get() == GameMode.LOCAL_BATTLE) startBattleMode(); else startAdventureMode();
                    }
                    return;
                }
                case SPACE -> {
                    if (stateManager.isRunning()) {
                        if (currentMode.get() == GameMode.LOCAL_BATTLE) battleManager.launchBall();
                        else if (gameManager.getPaddle()!=null) {
                            if (gameManager.getPaddle().isLaserEnabled()) gameManager.getPaddle().shootLaser();
                            else gameManager.launchBall();
                        }
                    }
                    return;
                }
                default -> {}
            }
            if (!stateManager.isRunning()) return;

            if (currentMode.get() == GameMode.ADVENTURE) {
                if (gameManager.getPaddle() == null) return;
                if ((code==KeyCode.A || code==KeyCode.D || code==KeyCode.LEFT || code==KeyCode.RIGHT)) {
                    if (!pressedStack.contains(code)) pressedStack.push(code);
                }
            } else { // Versus
                if (code==KeyCode.W || code==KeyCode.S || code==KeyCode.UP || code==KeyCode.DOWN) applyBattleMovementFromKeys();
            }
        });

        scene.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            KeyCode code = e.getCode();
            activeKeys.remove(code);
            pressedStack.remove(code);
            if (!stateManager.isRunning()) {
                if (currentMode.get() == GameMode.LOCAL_BATTLE) battleManager.stopPlayers();
                return;
            }
            if (currentMode.get() == GameMode.ADVENTURE) {
                if (gameManager.getPaddle()==null) return;
                if (code==KeyCode.A || code==KeyCode.D || code==KeyCode.LEFT || code==KeyCode.RIGHT) {
                    boolean still = pressedStack.stream().anyMatch(k -> k==KeyCode.A || k==KeyCode.D || k==KeyCode.LEFT || k==KeyCode.RIGHT);
                    if (!still) gameManager.getPaddle().setDx(0);
                }
                if (code==KeyCode.B) gameManager.spawnExtraBall();
            } else {
                if (code==KeyCode.W || code==KeyCode.S || code==KeyCode.UP || code==KeyCode.DOWN) applyBattleMovementFromKeys();
            }
        });

        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (!stateManager.isRunning() || event.getButton()!=MouseButton.PRIMARY) return;
            if (currentMode.get()==GameMode.ADVENTURE && gameManager.getPaddle()!=null) {
                if (gameManager.getPaddle().isLaserEnabled()) gameManager.getPaddle().shootLaser();
                else gameManager.launchBall();
            }
        });

        scene.setOnMouseMoved(this::handleMouseMoved);
        scene.getRoot().requestFocus();
    }

    // ===================== Game loop =====================
    private AnimationTimer createGameLoop() {
        return new AnimationTimer() {
            private long lastUpdate = 0L;
            @Override public void handle(long now) {
                if (lastUpdate == 0L) { lastUpdate = now; renderCurrentMode(); return; }
                double dt = (now - lastUpdate) / 1e9;
                if (stateManager.isRunning()) {
                    if (currentMode.get() == GameMode.ADVENTURE) { updatePaddleVelocity(); gameManager.update(dt); }
                    else { applyBattleMovementFromKeys(); battleManager.update(dt); }
                }
                lastUpdate = now; renderCurrentMode();
            }
        };
    }

    private void updatePaddleVelocity() {
        if (currentMode.get()!=GameMode.ADVENTURE || gameManager.getPaddle()==null) return;
        if (pressedStack.isEmpty()) { gameManager.getPaddle().setDx(0); return; }
        KeyCode key = pressedStack.peek();
        if (key==KeyCode.A || key==KeyCode.LEFT) gameManager.getPaddle().setDx(-Constants.PADDLE_SPEED);
        else if (key==KeyCode.D || key==KeyCode.RIGHT) gameManager.getPaddle().setDx(Constants.PADDLE_SPEED);
        else gameManager.getPaddle().setDx(0);
    }

    private void applyBattleMovementFromKeys() {
        if (currentMode.get()!=GameMode.LOCAL_BATTLE) return;
        double v1 = 0, v2 = 0;
        if (activeKeys.contains(KeyCode.W) && !activeKeys.contains(KeyCode.S)) v1 = -Constants.PADDLE_SPEED;
        else if (activeKeys.contains(KeyCode.S) && !activeKeys.contains(KeyCode.W)) v1 = Constants.PADDLE_SPEED;
        if (activeKeys.contains(KeyCode.UP) && !activeKeys.contains(KeyCode.DOWN)) v2 = -Constants.PADDLE_SPEED;
        else if (activeKeys.contains(KeyCode.DOWN) && !activeKeys.contains(KeyCode.UP)) v2 = Constants.PADDLE_SPEED;
        battleManager.setPlayerOneVelocity(v1);
        battleManager.setPlayerTwoVelocity(v2);
    }

    private void renderCurrentMode() {
        if (currentMode.get()==GameMode.ADVENTURE) gameManager.render(graphicsContext); else battleManager.render(graphicsContext);
    }

    private void handleMouseMoved(MouseEvent event) {
        if (currentMode.get()!=GameMode.ADVENTURE || !stateManager.isRunning() || gameManager.getPaddle()==null) return;
        double targetX = event.getX() - gameManager.getPaddle().getWidth()/2;
        double clampedX = Math.max(Constants.PLAYFIELD_LEFT, Math.min(targetX, Constants.PLAYFIELD_RIGHT - gameManager.getPaddle().getWidth()));
        gameManager.getPaddle().setX(clampedX);
    }

    // ===================== Mode switching =====================
    private void startAdventureMode() {
        currentMode.set(GameMode.ADVENTURE);
        pressedStack.clear(); activeKeys.clear();
        gameManager.initializeGame();
        stateManager.beginNewGame(gameManager.getScore(), gameManager.getLives());
        stateManager.setStatusMessage("Destroy all the bricks!");
        stateManager.setCurrentRound(1);
        stateManager.updateTimers(0, 0);
        renderCurrentMode();
    }
    private void startBattleMode() {
        currentMode.set(GameMode.LOCAL_BATTLE);
        pressedStack.clear(); activeKeys.clear();
        battleManager.startMatch();
        battleScoreboard.resetCounters(battleManager);
        stateManager.beginNewGame(0, Constants.START_LIVES);
        stateManager.setStatusMessage("Versus Battle: P1 W/S, P2 ↑/↓. Press SPACE to launch the ball.");
        stateManager.setCurrentRound(1);
        stateManager.updateTimers(0, 0);
        renderCurrentMode();
    }

    private void updateLayoutForMode(GameMode mode) {
        boolean battle = mode == GameMode.LOCAL_BATTLE;

        // HUD Adventure
        adventureHud.setAdventureVisible(!battle);

        // Grid column ratios
        if (battle) {
            adventureHud.setColumnPercents(0, 100, 0);
        } else {
            adventureHud.setColumnPercents(
                    Constants.SIDE_PANEL_RATIO * 100.0,
                    Constants.PLAYFIELD_RATIO * 100.0,
                    Constants.SIDE_PANEL_RATIO * 100.0
            );
        }

        // Background 3 phần
        backgroundSections.updateForMode(battle);
    }

    // ===================== Accessors =====================
    public Scene getScene() { return scene; }
    public GraphicsContext getGraphicsContext() { return graphicsContext; }
}
