package com.ooparkanoid.ui;

import com.ooparkanoid.object.Paddle;
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
 * Main game scene coordinator that integrates all UI components and game logic.
 * Orchestrates BackgroundSections, AdventureHud, BattleScoreboard, and overlay views.
 * Manages game loop, input handling, mode switching, and state transitions.
 * <p>
 * Architecture:
 * - Layered scene structure with background, content, and overlay layers
 * - Reactive UI updates through JavaFX bindings
 * - Event-driven input handling with key state management
 * - Mode-specific rendering and input logic
 * - Automatic state synchronization between UI and game logic
 * <p>
 * Game Modes:
 * - ADVENTURE: Single-player brick breaking with HUD display
 * - LOCAL_BATTLE: Two-player versus mode with scoreboard
 * <p>
 * Key Features:
 * - Seamless mode switching (F1/F2 keys)
 * - Pause/resume functionality (ESC key)
 * - Mouse and keyboard input support
 * - Automatic game over and pause overlay management
 * - Background music and sound effect integration
 * <p>
 * Input Controls:
 * - ESC: Pause/Resume
 * - F1: Switch to Adventure mode
 * - F2: Switch to Battle mode
 * - ENTER: Start game from menu
 * - SPACE: Launch ball / Shoot laser
 * - WASD/Arrow Keys: Paddle movement
 * - Mouse: Paddle positioning (Adventure mode)
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public class GameSceneRoot {

    /**
     * Main JavaFX scene containing all game UI
     */
    private final Scene scene;

    /**
     * Graphics context for canvas rendering
     */
    private final GraphicsContext graphicsContext;

    /**
     * Game manager for Adventure mode
     */
    private final GameManager gameManager;

    /**
     * Battle manager for Local Battle mode
     */
    private final LocalBattleManager battleManager;

    /**
     * State manager for game state coordination
     */
    private final GameStateManager stateManager;

    /**
     * Main game loop animation timer
     */
    private final AnimationTimer gameLoop;

    /**
     * Canvas for game rendering
     */
    private final Canvas canvas;

    /**
     * Layered scene structure with background/content/overlay layers
     */
    private final SceneLayoutFactory.LayeredScene layeredScene;

    /**
     * Background layer for static backgrounds
     */
    private final BackgroundLayer backgroundLayer;

    /**
     * Three-section background that adapts to game modes
     */
    private final BackgroundSections backgroundSections;

    /**
     * HUD display for Adventure mode
     */
    private final AdventureHud adventureHud;

    /**
     * Scoreboard for Battle mode
     */
    private final BattleScoreboard battleScoreboard;

    /**
     * Pause overlay view
     */
    private final NeonPauseView pauseView;

    /**
     * Game over overlay view
     */
    private final GameOverView gameOverView;

    /**
     * Stack of pressed keys for movement priority
     */
    private final Deque<KeyCode> pressedStack = new ArrayDeque<>();

    /**
     * Set of currently active keys
     */
    private final Set<KeyCode> activeKeys = EnumSet.noneOf(KeyCode.class);

    /**
     * Current game mode property
     */
    private final ObjectProperty<GameMode> currentMode = new SimpleObjectProperty<>(GameMode.ADVENTURE);

    /**
     * Callback for exiting to main menu
     */
    private final Runnable onExitToMenuCallback;

    /**
     * Initial game mode
     */
    private final GameMode initialMode;

    // ==================== Constructors ====================

    /**
     * Constructs a GameSceneRoot with default exit callback and Adventure mode.
     */
    public GameSceneRoot() {
        this(Platform::exit, GameMode.ADVENTURE);
    }

    /**
     * Constructs a GameSceneRoot with specified exit callback and default Adventure mode.
     *
     * @param onExitToMenuCallback callback to execute when exiting to menu
     */
    public GameSceneRoot(Runnable onExitToMenuCallback) {
        this(onExitToMenuCallback, GameMode.ADVENTURE);
    }

    /**
     * Constructs a GameSceneRoot with specified exit callback and initial game mode.
     * Initializes all game managers, UI components, and event handlers.
     *
     * @param onExitToMenuCallback callback to execute when exiting to menu
     * @param initialMode          the initial game mode to start with
     */
    public GameSceneRoot(Runnable onExitToMenuCallback, GameMode initialMode) {
        this.onExitToMenuCallback = onExitToMenuCallback;
        this.initialMode = initialMode;

        // Initialize core game systems
        stateManager = new GameStateManager();
        gameManager = new GameManager(stateManager);
        battleManager = new LocalBattleManager(stateManager);

        // Set up rendering canvas
        canvas = new Canvas(Constants.WIDTH, Constants.HEIGHT);
        graphicsContext = canvas.getGraphicsContext2D();

        // Create layered scene structure
        layeredScene = SceneLayoutFactory.createLayeredScene(canvas);
        backgroundLayer = layeredScene.backgroundLayer();
        scene = new Scene(layeredScene.root(), Constants.WIDTH, Constants.HEIGHT);

        // Configure background layer
        backgroundLayer.setFill(Color.BLACK);
        backgroundLayer.setImageLayers(null);
        backgroundLayer.getChildren().clear();

        // Add adaptive background sections
        backgroundSections = new BackgroundSections(Constants.WIDTH, Constants.HEIGHT);
        backgroundLayer.getChildren().add(backgroundSections.getRoot());

        // Add Adventure HUD to content layer
        adventureHud = new AdventureHud(stateManager);
        layeredScene.contentLayer().getChildren().add(adventureHud.getGrid());
        StackPane.setAlignment(adventureHud.getGrid(), Pos.CENTER);

        // HUD visibility binding - only show when running
        BooleanBinding hudVisible = stateManager.stateProperty().isEqualTo(GameState.RUNNING);
        adventureHud.getGrid().visibleProperty().bind(hudVisible);
        adventureHud.getGrid().managedProperty().bind(hudVisible);

        // Add Battle scoreboard to overlay layer
        battleScoreboard = new BattleScoreboard();
        battleScoreboard.bindTo(battleManager, currentMode);
        layeredScene.overlayLayer().getChildren().add(battleScoreboard.getRoot());
        StackPane.setAlignment(battleScoreboard.getRoot(), Pos.TOP_CENTER);
        StackPane.setMargin(battleScoreboard.getRoot(), new Insets(20, 48, 0, 48));

        // Add overlay views (pause and game over)
        pauseView = new NeonPauseView(new NeonPauseView.Callbacks() {
            @Override
            public void onResume() {
                pauseView.hide();
                stateManager.resumeGame();
                scene.getRoot().requestFocus();
            }

            @Override
            public void onExit() {
                gameLoop.stop();
                SoundManager.getInstance().stopMusic();
                onExitToMenuCallback.run();
            }
        });

        gameOverView = new GameOverView(new GameOverView.Callbacks() {
            @Override
            public void onExit() {
                gameLoop.stop();
                SoundManager.getInstance().stopMusic();
                onExitToMenuCallback.run();
            }
        });

        layeredScene.root().getChildren().addAll(pauseView.getView(), gameOverView.getView());

        // Set up state listeners and input handlers
        setupStateListeners();
        setupInputHandlers();

        // Mode change listener for layout updates
        currentMode.addListener((obs, o, n) -> updateLayoutForMode(n));

        // Initialize game loop and start appropriate mode
        gameLoop = createGameLoop();
        if (initialMode == GameMode.LOCAL_BATTLE) startBattleMode();
        else startAdventureMode();
        updateLayoutForMode(currentMode.get());
        gameLoop.start();
    }

    // ==================== State Management ====================

    /**
     * Sets up listeners for game state changes.
     * Handles transitions between running, paused, game over, and menu states.
     * Manages audio playback, overlay visibility, and paddle movement.
     */
    private void setupStateListeners() {
        stateManager.stateProperty().addListener((obs, oldState, newState) -> {
            // Stop paddle movement when not running
            if (newState != GameState.RUNNING) {
                if (currentMode.get() == GameMode.ADVENTURE) {
                    if (gameManager.getPaddle() != null)
                        gameManager.getPaddle().setDx(0);
                } else {
                    battleManager.stopPlayers();
                }
            }

            // Update status messages for menu state
            if (newState == GameState.MENU && !stateManager.continueAvailableProperty().get()) {
                stateManager.setStatusMessage("Select a mode from the main menu to begin.");
            }

            // Handle game over state
            if (newState == GameState.GAME_OVER) {
                if (stateManager.statusMessageProperty().get() == null || stateManager.statusMessageProperty().get().isBlank())
                    stateManager.setStatusMessage("Game Over! Final Score: " + stateManager.getScore());
                gameLoop.stop();
                SoundManager.getInstance().stopMusic();
                gameOverView.show();
            }

            // Handle pause state
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

    /**
     * Sets up comprehensive input event handlers.
     * Handles keyboard and mouse input for game control, mode switching, and UI navigation.
     * Uses event filters for global input capture and state-based input processing.
     */
    private void setupInputHandlers() {
        // Key press handler
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            KeyCode code = e.getCode();
            activeKeys.add(code);

            // Global controls (work in any state)
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
                case F1 -> {
                    startAdventureMode();
                    return;
                }
                case F2 -> {
                    startBattleMode();
                    return;
                }
                case ENTER -> {
                    if (stateManager.getCurrentState() == GameState.MENU) {
                        if (currentMode.get() == GameMode.LOCAL_BATTLE) startBattleMode();
                        else startAdventureMode();
                    }
                    return;
                }
                case SPACE -> {
                    if (stateManager.isRunning()) {
                        if (currentMode.get() == GameMode.LOCAL_BATTLE) battleManager.launchBall();
                        else if (gameManager.getPaddle() != null) {
                            if (gameManager.getPaddle().isLaserEnabled()) gameManager.getPaddle().shootLaser();
                            else gameManager.launchBall();
                        }
                    }
                    return;
                }
                default -> {
                }
            }

            // Mode-specific input (only when running)
            if (!stateManager.isRunning()) return;

            if (currentMode.get() == GameMode.ADVENTURE) {
                // Adventure mode: WASD/Arrow keys for paddle movement
                if (gameManager.getPaddle() == null) return;
                if ((code == KeyCode.A || code == KeyCode.D || code == KeyCode.LEFT || code == KeyCode.RIGHT)) {
                    if (!pressedStack.contains(code)) pressedStack.push(code);
                }
            } else {
                // Battle mode: WASD/Arrow keys for paddle movement
                if (code == KeyCode.W || code == KeyCode.S || code == KeyCode.UP || code == KeyCode.DOWN)
                    applyBattleMovementFromKeys();
            }
        });

        // Key release handler
        scene.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            KeyCode code = e.getCode();
            activeKeys.remove(code);
            pressedStack.remove(code);

            if (!stateManager.isRunning()) {
                if (currentMode.get() == GameMode.LOCAL_BATTLE) battleManager.stopPlayers();
                return;
            }

            if (currentMode.get() == GameMode.ADVENTURE) {
                // Adventure mode: Stop paddle if no movement keys pressed
                if (gameManager.getPaddle() == null) return;
                if (code == KeyCode.A || code == KeyCode.D || code == KeyCode.LEFT || code == KeyCode.RIGHT) {
                    boolean still = pressedStack.stream().anyMatch(k -> k == KeyCode.A || k == KeyCode.D || k == KeyCode.LEFT || k == KeyCode.RIGHT);
                    if (!still) gameManager.getPaddle().setDx(0);
                }
                // Debug: Spawn extra ball with B key
                if (code == KeyCode.B) gameManager.spawnExtraBall();
            } else {
                // Battle mode: Update paddle movement
                if (code == KeyCode.W || code == KeyCode.S || code == KeyCode.UP || code == KeyCode.DOWN)
                    applyBattleMovementFromKeys();
            }
        });

        // Mouse click handler
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (!stateManager.isRunning() || event.getButton() != MouseButton.PRIMARY) return;
            if (currentMode.get() == GameMode.ADVENTURE && gameManager.getPaddle() != null) {
                if (gameManager.getPaddle().isLaserEnabled()) gameManager.getPaddle().shootLaser();
                else gameManager.launchBall();
            }
        });

        // Mouse movement handler for paddle positioning
        scene.setOnMouseMoved(this::handleMouseMoved);

        // Request initial focus
        scene.getRoot().requestFocus();
    }

    // ==================== Game Loop ====================

    /**
     * Creates the main game loop animation timer.
     * Handles frame updates, input processing, and rendering for both game modes.
     * Uses high-resolution timing for smooth gameplay.
     *
     * @return configured AnimationTimer for the game loop
     */
    private AnimationTimer createGameLoop() {
        return new AnimationTimer() {
            private long lastUpdate = 0L;

            @Override
            public void handle(long now) {
                if (lastUpdate == 0L) {
                    lastUpdate = now;
                    renderCurrentMode();
                    return;
                }

                // Calculate delta time in seconds
                double dt = (now - lastUpdate) / 1e9;

                // Update game logic when running
                if (stateManager.isRunning()) {
                    if (currentMode.get() == GameMode.ADVENTURE) {
                        updatePaddleVelocity();
                        gameManager.update(dt);
                    } else {
                        applyBattleMovementFromKeys();
                        battleManager.update(dt);
                    }
                }

                lastUpdate = now;
                renderCurrentMode();
            }
        };
    }

    /**
     * Updates paddle velocity based on pressed keys for Adventure mode.
     * Uses key press stack for movement priority (last pressed key takes precedence).
     */
    private void updatePaddleVelocity() {
        if (currentMode.get() != GameMode.ADVENTURE || gameManager.getPaddle() == null) return;
        if (pressedStack.isEmpty()) {
            gameManager.getPaddle().setDx(0);
            return;
        }

        KeyCode key = pressedStack.peek();
        if (key == KeyCode.A || key == KeyCode.LEFT) gameManager.getPaddle().setDx(-Constants.PADDLE_SPEED);
        else if (key == KeyCode.D || key == KeyCode.RIGHT) gameManager.getPaddle().setDx(Constants.PADDLE_SPEED);
        else gameManager.getPaddle().setDx(0);
    }

    /**
     * Applies paddle movement for Battle mode based on active keys.
     * Handles simultaneous key presses for both players.
     */
    private void applyBattleMovementFromKeys() {
        if (currentMode.get() != GameMode.LOCAL_BATTLE) return;

        double v1 = 0, v2 = 0;

        // Player 1 (W/S keys)
        if (activeKeys.contains(KeyCode.W) && !activeKeys.contains(KeyCode.S)) v1 = -Constants.PADDLE_SPEED;
        else if (activeKeys.contains(KeyCode.S) && !activeKeys.contains(KeyCode.W)) v1 = Constants.PADDLE_SPEED;

        // Player 2 (Arrow keys)
        if (activeKeys.contains(KeyCode.UP) && !activeKeys.contains(KeyCode.DOWN)) v2 = -Constants.PADDLE_SPEED;
        else if (activeKeys.contains(KeyCode.DOWN) && !activeKeys.contains(KeyCode.UP)) v2 = Constants.PADDLE_SPEED;

        battleManager.setPlayerOneVelocity(v1);
        battleManager.setPlayerTwoVelocity(v2);
    }

    /**
     * Renders the current game mode to the canvas.
     * Delegates rendering to the appropriate game manager.
     */
    private void renderCurrentMode() {
        if (currentMode.get() == GameMode.ADVENTURE) gameManager.render(graphicsContext);
        else battleManager.render(graphicsContext);
    }

    /**
     * Handles mouse movement for paddle positioning in Adventure mode.
     * Moves paddle to follow mouse cursor horizontally within playfield bounds.
     *
     * @param event the mouse event containing cursor position
     */
    private void handleMouseMoved(MouseEvent event) {
        if (currentMode.get() != GameMode.ADVENTURE || !stateManager.isRunning()
                || gameManager.getPaddle() == null || !gameManager.getPaddle().isLive())
            return;

        double targetX = event.getX() - gameManager.getPaddle().getWidth() / 2;
        double clampedX = Math.max(Constants.PLAYFIELD_LEFT,
                Math.min(targetX, Constants.PLAYFIELD_RIGHT - gameManager.getPaddle().getWidth()));
        gameManager.getPaddle().setX(clampedX);
    }

    // ==================== Mode Switching ====================

    /**
     * Switches to Adventure mode and initializes a new game session.
     * Resets input state, initializes game manager, and updates UI.
     */
    private void startAdventureMode() {
        currentMode.set(GameMode.ADVENTURE);
        pressedStack.clear();
        activeKeys.clear();

        gameManager.initializeGame();
        stateManager.beginNewGame(gameManager.getScore(), gameManager.getLives());
        stateManager.setStatusMessage("Destroy all the bricks!");
        stateManager.setCurrentRound(1);
        stateManager.updateTimers(0, 0);

        renderCurrentMode();
    }

    /**
     * Switches to Battle mode and initializes a new match.
     * Resets input state, initializes battle manager, and updates UI.
     */
    private void startBattleMode() {
        currentMode.set(GameMode.LOCAL_BATTLE);
        pressedStack.clear();
        activeKeys.clear();

        battleManager.startMatch();
        battleScoreboard.resetCounters(battleManager);
        stateManager.beginNewGame(0, Constants.START_LIVES);
        stateManager.setStatusMessage("Versus Battle: P1 W/S, P2 ↑/↓. Press SPACE to launch the ball.");
        stateManager.setCurrentRound(1);
        stateManager.updateTimers(0, 0);

        renderCurrentMode();
    }

    /**
     * Updates the UI layout for the specified game mode.
     * Adjusts HUD visibility, column ratios, and background sections.
     *
     * @param mode the game mode to configure layout for
     */
    private void updateLayoutForMode(GameMode mode) {
        boolean battle = mode == GameMode.LOCAL_BATTLE;

        // Toggle Adventure HUD visibility
        adventureHud.setAdventureVisible(!battle);

        // Adjust column width ratios
        if (battle) {
            adventureHud.setColumnPercents(0, 100, 0);
        } else {
            adventureHud.setColumnPercents(
                    Constants.SIDE_PANEL_RATIO * 100.0,
                    Constants.PLAYFIELD_RATIO * 100.0,
                    Constants.SIDE_PANEL_RATIO * 100.0
            );
        }

        // Update background sections layout
        backgroundSections.updateForMode(battle);
    }

    // ==================== Accessors ====================

    /**
     * Gets the main JavaFX scene.
     *
     * @return the Scene containing all game UI
     */
    public Scene getScene() {
        return scene;
    }

    /**
     * Gets the graphics context for canvas rendering.
     *
     * @return the GraphicsContext for drawing operations
     */
    public GraphicsContext getGraphicsContext() {
        return graphicsContext;
    }
}
