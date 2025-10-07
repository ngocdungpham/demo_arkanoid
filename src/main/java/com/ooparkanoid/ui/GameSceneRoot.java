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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
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

    private final Scene scene;
    private final GraphicsContext g;
    private final GameManager gameManager;
    private final GameStateManager stateManager;
    private final AnimationTimer gameLoop;

    private final StackPane root;
    private final Canvas canvas;

    // UI components giữ làm field để truy cập khi cần
    private VBox menuContent;
    private Label stateLabel;
    private Label messageLabel;
    private Button continueButton;

    private final Deque<KeyCode> pressedStack = new ArrayDeque<>();

    public GameSceneRoot() {
        stateManager = new GameStateManager();
        gameManager = new GameManager(stateManager);

        root = new StackPane();
        canvas = new Canvas(Constants.WIDTH, Constants.HEIGHT);
        g = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);

        scene = new Scene(root, Constants.WIDTH, Constants.HEIGHT);

        // ---------- HUD (Score/Lives) ----------
        Label scoreLabel = new Label();
        scoreLabel.textProperty().bind(stateManager.scoreProperty().asString("Score: %d"));
        Label livesLabel = new Label();
        livesLabel.textProperty().bind(stateManager.livesProperty().asString("Lives: %d"));

        scoreLabel.setTextFill(Color.WHITE);
        livesLabel.setTextFill(Color.WHITE);
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        livesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        HBox hud = new HBox(20, scoreLabel, livesLabel);
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
            };
        }, stateManager.stateProperty()));
        stateLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        stateLabel.setTextFill(Color.WHITE);

        messageLabel = new Label();
        messageLabel.textProperty().bind(stateManager.statusMessageProperty());
        messageLabel.setWrapText(true);
        messageLabel.setTextFill(Color.WHITE);
        messageLabel.setFont(Font.font(16));
        messageLabel.setAlignment(Pos.CENTER);

        Button newGameButton = createMenuButton("New game", this::startNewGame);
        continueButton = createMenuButton("Continue", e -> stateManager.resumeGame());
        Button exitButton = createMenuButton("Exit game", e -> Platform.exit());
        Button gameModeButton = createMenuButton("Game mode",
                e -> AlertBox.display("Game mode", "Classic brick breaking mode. Destroy all bricks to win."));
        Button howToPlayButton = createMenuButton("How to play",
                e -> AlertBox.display("How to play", "Use A/D or the arrow keys to move the paddle. Keep the ball from falling! Press ESC to pause."));
        Button infoButton = createMenuButton("Information",
                e -> AlertBox.display("About", "Arkanoid demo built with JavaFX."));

        // Chỉ cho Continue khi state hợp lệ và có thể tiếp tục
        BooleanBinding canContinue = Bindings.createBooleanBinding(
                stateManager::canContinue,
                stateManager.stateProperty(),
                stateManager.continueAvailableProperty()
        );
        continueButton.disableProperty().bind(canContinue.not());

        menuContent = new VBox(15,
                stateLabel,
                messageLabel,
                newGameButton,
                continueButton,
                gameModeButton,
                howToPlayButton,
                infoButton,
                exitButton
        );
        menuContent.setAlignment(Pos.CENTER);
        menuContent.setPadding(new Insets(25));
        menuContent.setFillWidth(true);

        StackPane menuOverlay = new StackPane(menuContent);
        menuOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.75);");
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


        stateManager.resetToMenu();
        gameLoop.start();
    } // <--- đóng constructor đúng chỗ

    private Button createMenuButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button button = new Button(text);
        button.setMaxWidth(220);
        button.setOnAction(action);
        return button;
    }

//    private void setupInputHandlers() {
//        scene.setOnKeyPressed(this::handleKeyPressed);
//        scene.setOnKeyReleased(this::handleKeyReleased);
//        scene.setOnMouseMoved(this::handleMouseMoved);
//    }
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


//    private void handleKeyPressed(KeyEvent event) {
//        if (event.getCode() == KeyCode.ESCAPE) {
//            if (stateManager.isRunning()) {
//                stateManager.pauseGame();
//            } else if (stateManager.getCurrentState() == GameState.PAUSED) {
//                stateManager.resumeGame();
//            }
//            return;
//        }
//
//        if (!stateManager.isRunning() || gameManager.getPaddle() == null) return;
//
//        if (event.getCode() == KeyCode.A || event.getCode() == KeyCode.LEFT) {
//            gameManager.getPaddle().setDx(-Constants.DEFAULT_SPEED);
//        } else if (event.getCode() == KeyCode.D || event.getCode() == KeyCode.RIGHT) {
//            gameManager.getPaddle().setDx(Constants.DEFAULT_SPEED);
//        }
//    }

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

    private void startNewGame(javafx.event.ActionEvent event) {
        gameManager.initializeGame();
        stateManager.beginNewGame(gameManager.getScore(), gameManager.getLives());
        gameManager.render(g);
    }

    public Scene getScene() { return scene; }
    public GraphicsContext getGraphicsContext() { return g; }
}
