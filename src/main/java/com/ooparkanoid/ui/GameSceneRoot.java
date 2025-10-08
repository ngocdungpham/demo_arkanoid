package com.ooparkanoid.ui;

import com.ooparkanoid.AlertBox;
import com.ooparkanoid.core.engine.GameManager;
import com.ooparkanoid.core.save.SaveService;
import com.ooparkanoid.core.state.GameState;
import com.ooparkanoid.core.state.GameStateManager;
import com.ooparkanoid.utils.Constants;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayDeque;
import java.util.Deque;
import javafx.scene.input.KeyCode;

public class GameSceneRoot {

    // GameSceneRoot.java (trong constructor hoặc phương thức buildUI của bạn)
    private enum BgMode { COLOR_OR_GRADIENT, IMAGE_STATIC, IMAGE_GIF /*, VIDEO*/ }
    // Cấu hình nền: bạn có thể chuyển chế độ, và set path ảnh khi cần.
    private BgMode bgMode = BgMode.COLOR_OR_GRADIENT;
    private String bgImagePath = null; // "assets/bg_space.png" hoặc "assets/loop.gif"

    // ---- UI nodes ----
    private StackPane rootLayer;
    private VBox menuCard;
    private Button btnContinue;

    private final Scene scene;
    private final GraphicsContext g;
    private final GameManager gameManager;
    private final GameStateManager stateManager;
    private final AnimationTimer gameLoop;

    private StackPane root;
    private final Canvas canvas;

    // UI components giữ làm field để truy cập khi cần
    private VBox menuContent1;
    private VBox menuContent2;
    private Label stateLabel;
    private Label messageLabel;
    private Button continueButton;
    private SceneLayoutFactory layoutFactory;
    private final Deque<KeyCode> pressedStack = new ArrayDeque<>();



    public GameSceneRoot() {
        stateManager = new GameStateManager();
        gameManager = new GameManager(stateManager);
        root = new StackPane();
        root.getStyleClass().add("app");
        canvas = new Canvas(Constants.WIDTH, Constants.HEIGHT);
        g = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);
        scene = new Scene(root, Constants.WIDTH, Constants.HEIGHT);
//        scene.getStylesheets().add(getClass().getResource("/styles/theme.css").toExternalForm());
        scene.getStylesheets().add("/styles/theme.css");

        // ---------- HUD (Score/Lives) ----------
        Label scoreLabel = new Label();
        scoreLabel.textProperty().bind(stateManager.scoreProperty().asString("Score: %d"));
        Label livesLabel = new Label();
        livesLabel.textProperty().bind(stateManager.livesProperty().asString("Lives: %d"));

        scoreLabel.setTextFill(Color.WHITE);
        livesLabel.setTextFill(Color.WHITE);
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        livesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        /**
         * CSS.
         */
        scoreLabel.getStyleClass().add("hud");
        livesLabel.getStyleClass().add("hud");

        HBox hud = new HBox(20, scoreLabel, livesLabel);

        /**
         * Score và lives xuat hien khi running.
         */
        BooleanBinding hudVisible = stateManager.stateProperty().isEqualTo(GameState.RUNNING);
        hud.visibleProperty().bind(hudVisible);
        hud.managedProperty().bind(hudVisible);

        hud.setPadding(new Insets(15));
        hud.setAlignment(Pos.TOP_LEFT);
        root.getChildren().add(hud);
        StackPane.setAlignment(hud, Pos.TOP_LEFT);

        // ---------- Menu overlay ----------
        stateLabel = new Label();
        stateLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            GameState current = stateManager.getCurrentState();
            return switch (current) {
                case MENU -> "Arkanoid";
                case PAUSED -> "Paused";
                case GAME_OVER -> "Game Over";
                case RUNNING -> "";
                case MODE_SELECT -> null;
                case HOW_TO_PLAY -> null;
                case INFORMATION -> null;
                case PAUSE -> null;
            };
        }, stateManager.stateProperty()));
        stateLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        stateLabel.setTextFill(Color.WHITE);

        messageLabel = new Label();
        messageLabel.textProperty().bind(stateManager.statusMessageProperty());
        messageLabel.setWrapText(true);
        messageLabel.setTextFill(Color.WHITE);
//        messageLabel.setFont(Font.font(16));
        messageLabel.setAlignment(Pos.CENTER);

        Button newGameButton = createMenuButton("New game", this::startNewGame);
        newGameButton.getStyleClass().addAll("btn","btn-primary");
        continueButton = createMenuButton("Continue", e -> stateManager.resumeGame());
        continueButton.getStyleClass().addAll("btn","btn-primary");
        Button exitButton = createMenuButton("Exit game", e -> Platform.exit());
        exitButton.getStyleClass().addAll("btn","btn-primary");
        Button gameModeButton = createMenuButton("Game mode",
                e -> AlertBox.display("Game mode", "Classic brick breaking mode. Destroy all bricks to win."));
        gameModeButton.getStyleClass().addAll("btn","btn-primary");
        Button howToPlayButton = createMenuButton("How to play",
                e -> AlertBox.display("How to play", "Use A/D or the arrow keys to move the paddle. Keep the ball from falling! Press ESC to pause."));
        howToPlayButton.getStyleClass().addAll("btn","btn-primary");
        Button infoButton = createMenuButton("Information",
                e -> AlertBox.display("About", "Arkanoid demo built with JavaFX."));
        infoButton.getStyleClass().addAll("btn","btn-primary");

        // Chỉ cho Continue khi state hợp lệ và có thể tiếp tục
        BooleanBinding canContinue = Bindings.createBooleanBinding(
                stateManager::canContinue,
                stateManager.stateProperty(),
                stateManager.continueAvailableProperty()
        );
//        continueButton.disableProperty().bind(canContinue.not());
        continueButton.visibleProperty().bind(canContinue);
        continueButton.managedProperty().bind(continueButton.visibleProperty());

        menuContent1 = new VBox(14,
                stateLabel,
                messageLabel,
                newGameButton,
                continueButton,
                gameModeButton,
                howToPlayButton,
                infoButton,
                exitButton
        );

        menuContent1.setAlignment(Pos.CENTER);
        menuContent1.setPadding(new Insets(40));
        menuContent1.setFillWidth(true);

        StackPane menuOverlay = new StackPane(menuContent1);

        menuOverlay.getStyleClass().add("overlay");
        stateLabel.getStyleClass().add("overlay-title");
        messageLabel.getStyleClass().add("overlay-subtitle");

//        menuOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.75);");
        root.getChildren().add(menuOverlay);

        BooleanBinding menuVisible = stateManager.stateProperty().isNotEqualTo(GameState.RUNNING);
        menuOverlay.visibleProperty().bind(menuVisible);
        menuOverlay.managedProperty().bind(menuVisible);

        // ---------- State listeners ----------
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

        // ---------- Input ----------
        setupInputHandlers();

        // ---------- Game loop ----------
        gameLoop = new AnimationTimer() {
            private long lastUpdate = 0L;

            @Override
            public void handle(long now) {
                if (lastUpdate == 0L) {
                    lastUpdate = now;
                    gameManager.render(g);
                    return;
                }

                if (stateManager.isRunning()) {
                    // 1) Đọc phím mới nhất trong stack để đặt vận tốc paddle
                    if (gameManager.getPaddle() != null) {
                        if (!pressedStack.isEmpty()) {
                            KeyCode key = pressedStack.peek(); // phím mới nhất
                            if (key == KeyCode.A || key == KeyCode.LEFT) {
                                gameManager.getPaddle().setDx(-Constants.PADDLE_SPEED);
                            } else if (key == KeyCode.D || key == KeyCode.RIGHT) {
                                gameManager.getPaddle().setDx(Constants.PADDLE_SPEED);
                            } else {
                                gameManager.getPaddle().setDx(0);
                            }
                        } else {
                            gameManager.getPaddle().setDx(0);
                        }
                    }

                    // 2) Update vật lý
                    double dt = (now - lastUpdate) / 1e9;
                    gameManager.update(dt);
                }

                lastUpdate = now;

                // 3) Render
                gameManager.render(g);

                // 4) (tuỳ chọn) xử lý Game Over tự reset hoặc hiển thị overlay
                if (stateManager.getCurrentState() == GameState.GAME_OVER) {
                    // ở đây bạn có overlay menu rồi, thường không reset tự động.
                    // Nếu muốn tự reset thì mở comment:
                    // gameManager.initializeGame();
                    // stateManager.beginNewGame(gameManager.getScore(), gameManager.getLives());
                }
            }
        };

        // (4) Áp dụng CSS và dựng giao diện menu mới
//        scene.getStylesheets().add(getClass().getResource("/styles/theme.css").toExternalForm());
//        buildModernMenuUI(); // 🎯 Gọi ở đây
        bgMode = BgMode.IMAGE_STATIC;
        bgImagePath = getClass().getResource("/picture/space.png").toExternalForm();

        stateManager.resetToMenu();
        gameLoop.start();
    } // <--- đóng constructor đúng chỗ

    private Button createMenuButton(String text, EventHandler<ActionEvent> action) {
        Button button = new Button(text);
        button.setMaxWidth(200);
        button.setOnAction(action);
        return button;
    }

    private void setupInputHandlers() {
        // Bắt phím theo stack (ưu tiên phím vừa nhấn gần nhất)
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            KeyCode code = e.getCode();

            // Phím chức năng chung
            if (code == KeyCode.ESCAPE) {
                if (stateManager.isRunning()) stateManager.pauseGame();
                else if (stateManager.getCurrentState() == GameState.PAUSED) stateManager.resumeGame();
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

            // Phím di chuyển chỉ khi RUNNING
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
        });

        scene.setOnMouseMoved(this::handleMouseMoved);
        // đảm bảo scene có focus khi đóng overlay
        scene.getRoot().requestFocus();
    }

        private void handleKeyReleased(KeyEvent event) {
            if (!stateManager.isRunning() || gameManager.getPaddle() == null) return;

            KeyCode code = event.getCode();
            if (code == KeyCode.A || code == KeyCode.D || code == KeyCode.LEFT || code == KeyCode.RIGHT) {
                gameManager.getPaddle().setDx(0);
            }
        }

        private void handleMouseMoved(MouseEvent event) {
            if (!stateManager.isRunning() || gameManager.getPaddle() == null) return;

            gameManager.getPaddle().setX(event.getX() - gameManager.getPaddle().getWidth() / 2);
        }

        private void startNewGame(ActionEvent event) {
            gameManager.initializeGame();
            stateManager.beginNewGame(gameManager.getScore(), gameManager.getLives());
            gameManager.render(g);
        }



        public Scene getScene() { return scene; }
        public GraphicsContext getGraphicsContext() { return g; }
}
