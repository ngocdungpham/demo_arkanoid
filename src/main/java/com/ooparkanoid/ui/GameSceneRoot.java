package com.ooparkanoid.ui;

import com.ooparkanoid.AlertBox;
import com.ooparkanoid.core.engine.GameManager;
import com.ooparkanoid.core.state.GameState;
import com.ooparkanoid.core.state.GameStateManager;
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

import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

public class GameSceneRoot {

    private final Scene scene;
    private final GraphicsContext graphicsContext;
    private final GameManager gameManager;
    private final GameStateManager stateManager;
    private final AnimationTimer gameLoop;
    private StackPane leftBackgroundSection;
    private StackPane centerBackgroundSection;
    private StackPane rightBackgroundSection;

    private final Canvas canvas;
    private final SceneLayoutFactory.LayeredScene layeredScene;
    private final BackgroundLayer backgroundLayer;

//    private Label stateLabel;
//    private Label messageLabel;
//    private Button continueButton;
//
//
//    private StackPane menuOverlay;

    private final Deque<KeyCode> pressedStack = new ArrayDeque<>();


    public GameSceneRoot() {
        stateManager = new GameStateManager();
        gameManager = new GameManager(stateManager);

        canvas = new Canvas(Constants.WIDTH, Constants.HEIGHT);
        graphicsContext = canvas.getGraphicsContext2D();

        layeredScene = SceneLayoutFactory.createLayeredScene(canvas);
        backgroundLayer = layeredScene.backgroundLayer();

        scene = new Scene(layeredScene.root(), Constants.WIDTH, Constants.HEIGHT);
        //  scene.getStylesheets().add("/styles/theme.css");

        configureBackground();
        buildHud();
        // buildMenuOverlay();
        setupStateListeners();
        setupInputHandlers();

        gameLoop = createGameLoop();

        //  stateManager.resetToMenu();
        gameManager.initializeGame();
        stateManager.beginNewGame(gameManager.getScore(), gameManager.getLives());
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
        HBox backgroundSections = new HBox(leftBackgroundSection, centerBackgroundSection, rightBackgroundSection);
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
        VBox leftPanel = new VBox(12, createHudTitleLabel(), pointsLabel, roundTimeLabel, totalTimeLabel, livesLabel);
        leftPanel.setAlignment(Pos.TOP_LEFT);
        leftPanel.setPadding(new Insets(24, 18, 24, 24));
        leftPanel.setSpacing(12);
        leftPanel.setBackground(createPanelBackground());
        leftPanel.setPrefWidth(Constants.LEFT_PANEL_WIDTH);
        leftPanel.setMinWidth(Constants.LEFT_PANEL_WIDTH);
        leftPanel.setMaxWidth(Constants.LEFT_PANEL_WIDTH);

        Label currentRoundTitle = createHudTitleLabel();
        Label currentRoundValue = createHudValueLabel();
        currentRoundValue.textProperty().bind(stateManager.roundProperty().asString("Round %d"));

        VBox rightPanel = new VBox(10, currentRoundTitle, currentRoundValue);
        rightPanel.setAlignment(Pos.TOP_RIGHT);
        rightPanel.setPadding(new Insets(24, 24, 24, 18));
        rightPanel.setBackground(createPanelBackground());
        rightPanel.setPrefWidth(Constants.RIGHT_PANEL_WIDTH);
        rightPanel.setMinWidth(Constants.RIGHT_PANEL_WIDTH);
        rightPanel.setMaxWidth(Constants.RIGHT_PANEL_WIDTH);

        GridPane hudGrid = new GridPane();
        hudGrid.setMouseTransparent(true);
        hudGrid.setPickOnBounds(false);
        hudGrid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        hudGrid.setPrefSize(Constants.WIDTH, Constants.HEIGHT);

        ColumnConstraints leftColumn = createColumn(Constants.SIDE_PANEL_RATIO);
        ColumnConstraints centerColumn = createColumn(Constants.PLAYFIELD_RATIO);
        ColumnConstraints rightColumn = createColumn(Constants.SIDE_PANEL_RATIO);
        hudGrid.getColumnConstraints().addAll(leftColumn, centerColumn, rightColumn);

        Pane centerSpacer = new Pane();
        centerSpacer.setMinSize(0, 0);
        centerSpacer.setMouseTransparent(true);
        centerSpacer.setStyle("-fx-border-color: rgba(255,255,255,0.18); -fx-border-width: 0 2 0 2;");

        GridPane.setHalignment(leftPanel, HPos.LEFT);
        GridPane.setValignment(leftPanel, VPos.TOP);
        GridPane.setHalignment(rightPanel, HPos.RIGHT);
        GridPane.setValignment(rightPanel, VPos.TOP);
        GridPane.setHgrow(centerSpacer, Priority.ALWAYS);
        GridPane.setHgrow(leftPanel, Priority.NEVER);
        GridPane.setHgrow(rightPanel, Priority.NEVER);
        GridPane.setFillWidth(leftPanel, true);
        GridPane.setFillWidth(rightPanel, true);

        hudGrid.add(leftPanel, 0, 0);
        hudGrid.add(centerSpacer, 1, 0);
        hudGrid.add(rightPanel, 2, 0);

        BooleanBinding hudVisible = stateManager.stateProperty().isEqualTo(GameState.RUNNING);
//        hud.visibleProperty().bind(hudVisible);
//        hud.managedProperty().bind(hudVisible);
        hudGrid.visibleProperty().bind(hudVisible);
        hudGrid.managedProperty().bind(hudVisible);

        layeredScene.contentLayer().getChildren().add(hudGrid);
        StackPane.setAlignment(hudGrid, Pos.CENTER);
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
            if (newState != GameState.RUNNING && gameManager.getPaddle() != null) {
                gameManager.getPaddle().setDx(0);
            }

            if (newState == GameState.MENU && !stateManager.continueAvailableProperty().get()) {
                stateManager.setStatusMessage("Welcome to Arkanoid!");
            }

            if (newState == GameState.GAME_OVER) {
                String message = stateManager.statusMessageProperty().get();
                if (message == null || message.isBlank()) {
                    stateManager.setStatusMessage("Game Over! Final Score: " + stateManager.getScore());
                }
            }
        });
    }

    private void setupInputHandlers() {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            KeyCode code = e.getCode();

            if (code == KeyCode.ESCAPE) {
                if (stateManager.isRunning()) {
                    stateManager.pauseGame();
                } else if (stateManager.getCurrentState() == GameState.PAUSED) {
                    stateManager.resumeGame();
                }
                return;
            }

            if (code == KeyCode.ENTER) {
                if (stateManager.getCurrentState() == GameState.MENU ||
                        stateManager.getCurrentState() == GameState.GAME_OVER) {
                    gameManager.initializeGame();
                    stateManager.beginNewGame(gameManager.getScore(), gameManager.getLives());
                }
                return;
            }

            if (code == KeyCode.SPACE) {
                if (stateManager.isRunning()) {
                    if (gameManager.getPaddle().isLaserEnabled()) {
                        gameManager.getPaddle().shootLaser();
                    }
                    else {
                        gameManager.launchBall();
                    }
                }
                return;
            }

            if (!stateManager.isRunning() || gameManager.getPaddle() == null) return;
            if (!pressedStack.contains(code)) {
                pressedStack.push(code); // đưa phím mới lên đầu
            }
        });

        scene.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            pressedStack.remove(e.getCode());
            // nếu nhả A/D/LEFT/RIGHT mà không còn phím di chuyển nào -> dừng paddle
            if (!stateManager.isRunning() || gameManager.getPaddle() == null) return;
            KeyCode code = e.getCode();
            if (code == KeyCode.A || code == KeyCode.D || code == KeyCode.LEFT || code == KeyCode.RIGHT) {
                // kiểm tra xem trong stack còn phím di chuyển nào không
                boolean stillMoving = pressedStack.stream().anyMatch(k ->
                        k == KeyCode.A || k == KeyCode.D || k == KeyCode.LEFT || k == KeyCode.RIGHT);
                if (!stillMoving) gameManager.getPaddle().setDx(0);
            }
            if (code == KeyCode.B) {
                gameManager.spawnExtraBall();
            }
        });

        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (stateManager.isRunning() && event.getButton() == MouseButton.PRIMARY) {
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
                    gameManager.render(graphicsContext);
                    return;
                }

                if (stateManager.isRunning()) {
                    updatePaddleVelocity();
                    double dt = (now - lastUpdate) / 1e9;
                    gameManager.update(dt);
                }

                lastUpdate = now;
                gameManager.render(graphicsContext);
            }
        };
    }

    private void updatePaddleVelocity() {
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

    private void handleMouseMoved(MouseEvent event) {
        if (!stateManager.isRunning() || gameManager.getPaddle() == null) {
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
        gameManager.initializeGame();
        stateManager.beginNewGame(gameManager.getScore(), gameManager.getLives());
        gameManager.render(graphicsContext);
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