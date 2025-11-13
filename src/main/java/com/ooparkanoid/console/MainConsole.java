package com.ooparkanoid.console;

import com.ooparkanoid.core.engine.AssetLoadingTask;
import com.ooparkanoid.core.state.OnlinePresenceService;
import com.ooparkanoid.core.state.PlayerContext;

import java.util.ArrayList;

import com.ooparkanoid.core.score.FirebaseScoreService;
import com.ooparkanoid.core.state.GameMode;
import com.ooparkanoid.AlertBox;
import com.ooparkanoid.graphics.ResourceManager;
import com.ooparkanoid.ui.*;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.event.EventHandler;

import javafx.fxml.FXMLLoader;

import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import javafx.scene.paint.Color;

import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;

import javafx.stage.Stage;

import javafx.util.Duration;

import com.ooparkanoid.sound.SoundManager;
import com.ooparkanoid.utils.Constants;

import java.io.IOException;
import java.net.URL;

/**
 * Main application entry point and controller for the Arkanoid game.
 * Manages the complete application flow including:
 * - Intro screen with animated transitions
 * - User authentication (login/signup)
 * - Asset loading with progress display
 * - Main menu navigation
 * - Game mode selection and initialization
 * - Leaderboard display
 * - Online presence tracking
 *
 * This class orchestrates all major UI transitions and scene management
 * throughout the application lifecycle.
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public class MainConsole extends Application {
    /** Primary stage for the application */
    private Stage stage;

    /** Media player for intro video playback */
    private MediaPlayer introMediaPlayer;

    /** Event handlers for intro screen user input */
    private EventHandler<KeyEvent> introSpaceHandler;
    private EventHandler<MouseEvent> introMouseHandler;

    /** Cached menu scene components for quick navigation */
    private Parent menuRoot;
    private MenuController menuController;

    /** Game mode to launch after intro video completes */
    private GameMode nextGameMode = GameMode.ADVENTURE;

    /**
     * Application entry point. Initializes the primary stage and begins the intro sequence.
     * Flow: Intro Screen → Login/Signup → Loading → Menu → Game
     *
     * @param stage the primary stage for this application
     * @throws IOException if FXML resources cannot be loaded
     */
    @Override
    public void start(Stage stage) throws IOException {
        this.stage = stage;
        stage.setTitle("Arkanoid - Simple Brick Game");
        stage.setResizable(false);

        // Application flow: Intro.fxml → (SPACE/click) → fadeToBlack → Login
        showIntroScreen();

        stage.show();
    }

    // ==================== INTRO SCREEN ====================

    /**
     * Displays the intro screen with background music and skip functionality.
     * Users can skip by pressing SPACE or clicking the mouse.
     * Preloads sound effects for smooth gameplay experience.
     */
    private void showIntroScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Intro.fxml"));
            Parent root = loader.load();

            Scene scene = (stage.getScene() == null)
                    ? new Scene(root, Constants.WIDTH, Constants.HEIGHT)
                    : stage.getScene();
            scene.setRoot(root);
            stage.setScene(scene);

            // Initialize all sound effects early (idempotent operation)
            SoundManager.getInstance().init();

            // Start intro background music
            SoundManager.getInstance().playMusic("intro.mp3");

            // Define skip action (prevents double-triggering)
            Runnable skipIntro = () -> {
                // Remove event handlers to prevent multiple triggers
                scene.setOnKeyPressed(null);
                if (introMouseHandler != null) {
                    scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, introMouseHandler);
                    introMouseHandler = null;
                }
                SoundManager.getInstance().play("selected");
                SoundManager.getInstance().stopMusic();

                // Fade to black, then transition to login screen
                fadeToBlack(this::transitionToLogin);
            };

            // Register SPACE key handler
            introSpaceHandler = e -> {
                if (e.getCode() == KeyCode.SPACE) skipIntro.run();
            };
            scene.setOnKeyPressed(introSpaceHandler);

            // Register left mouse click handler
            introMouseHandler = e -> {
                if (e.getButton() == MouseButton.PRIMARY) skipIntro.run();
            };
            scene.addEventFilter(MouseEvent.MOUSE_PRESSED, introMouseHandler);

            root.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
            // Fallback: skip intro and go directly to login
            showLoginScreen();
        }
    }

    // ==================== AUTHENTICATION SCREENS ====================

    /**
     * Transitions to login screen with curtain opening animation.
     * Creates smooth visual effect for scene transition.
     */
    /**
     * Transitions to login screen with curtain opening animation.
     * Creates smooth visual effect for scene transition.
     */
    private void transitionToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent loginRoot = loader.load();
            LoginController controller = loader.getController();

            controller.setOnLoginSuccess(() -> {
                OnlinePresenceService.goOnline(PlayerContext.uid);
                // Preload intro video for smooth menu experience
                preloadIntroVideo();
                // Proceed to asset loading screen
                showLoadingScreen();
            });

            controller.setOnGoToSignUp(this::showSignUpScreen);

            Scene scene = stage.getScene();

            // Create curtain effect for transition
            Rectangle curtain = new Rectangle(Constants.WIDTH, Constants.HEIGHT, Color.BLACK);
            Scale curtainScale = new Scale(1, 1, Constants.WIDTH / 2.0, Constants.HEIGHT / 2.0);
            curtain.getTransforms().add(curtainScale);

            StackPane transitionPane = new StackPane(loginRoot, curtain);
            scene.setRoot(transitionPane);

            // Animate curtain opening
            Timeline openCurtain = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(curtainScale.xProperty(), 1, Interpolator.EASE_IN)),
                    new KeyFrame(Duration.seconds(0.5), new KeyValue(curtainScale.xProperty(), 0, Interpolator.EASE_OUT))
            );
            openCurtain.setOnFinished(e -> {
                // Clean up transition pane before setting new root
                transitionPane.getChildren().clear();
                scene.setRoot(loginRoot);
                loginRoot.requestFocus();
            });
            openCurtain.play();
        } catch (IOException e) {
            e.printStackTrace();
            showLoginScreen();
        }
    }

    /**
     * Shows login screen without transition animation (fallback).
     * Used when transition effects fail or as a direct entry point.
     */
    private void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            LoginController controller = loader.getController();

            controller.setOnLoginSuccess(() -> {
                OnlinePresenceService.goOnline(PlayerContext.uid);
                preloadIntroVideo();
                showLoadingScreen();
            });

            controller.setOnGoToSignUp(this::showSignUpScreen);

            Scene scene = (stage.getScene() == null)
                    ? new Scene(root, Constants.WIDTH, Constants.HEIGHT)
                    : stage.getScene();
            scene.setRoot(root);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays the signup screen for new user registration.
     * Sets up callbacks for successful signup and navigation back to login.
     */
    private void showSignUpScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/signup.fxml"));
            Parent root = loader.load();
            SignupController controller = loader.getController();

            controller.setOnSignUpSuccess(() -> {
                OnlinePresenceService.goOnline(PlayerContext.uid);
                preloadIntroVideo();
                showLoadingScreen();
            });

            controller.setOnGoToLogin(this::showLoginScreen);
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ==================== ASSET LOADING ====================

    /**
     * Displays loading screen with progress bar while assets are loaded asynchronously.
     * Loads all images and sounds in background thread to avoid UI freezing.
     */
    /**
     * Displays loading screen with progress bar while assets are loaded asynchronously.
     * Loads all images and sounds in background thread to avoid UI freezing.
     */
    private void showLoadingScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/loading.fxml"));
            Pane root = loader.load();

            ProgressBar progressBar = (ProgressBar) root.lookup("#progressBar");
            Label statusLabel = (Label) root.lookup("#statusLabel");

            stage.getScene().setRoot(root);

            AssetLoadingTask loadingTask = new AssetLoadingTask();
            progressBar.progressProperty().bind(loadingTask.progressProperty());
            statusLabel.textProperty().bind(loadingTask.messageProperty());

            loadingTask.setOnSucceeded(e -> {
                System.out.println("✅ Asset loading completed successfully");
                startTransition(); // Transition to menu with curtain effect
            });

            loadingTask.setOnFailed(e -> {
                System.err.println("❌ Error loading game assets:");
                loadingTask.getException().printStackTrace();
                AlertBox.display("Critical Error", "Unable to load game assets. Please try again.");
                Platform.exit();
            });

            new Thread(loadingTask).start();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load loading.fxml, proceeding directly to menu...");
            startTransition();
        }
    }

    // ==================== MENU TRANSITION ====================

    /**
     * Transitions from current screen to main menu with curtain animation.
     * Closes curtain on current screen, loads menu, then opens curtain.
     */
    private void startTransition() {
        SoundManager.getInstance().stopMusic();
        SoundManager.getInstance().play("transition");

        Scene scene = stage.getScene();

        // Clean up any remaining intro handlers
        scene.setOnKeyPressed(null);
        if (introMouseHandler != null) {
            scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, introMouseHandler);
            introMouseHandler = null;
        }

        Parent currentRoot = scene.getRoot();
        if (currentRoot == null) {
            showNewMenu();
            return;
        }

        // Create curtain that closes from center
        Rectangle curtain = new Rectangle(Constants.WIDTH, Constants.HEIGHT, Color.BLACK);
        Scale curtainScale = new Scale(0, 1, Constants.WIDTH / 2.0, Constants.HEIGHT / 2.0);
        curtain.getTransforms().add(curtainScale);

        StackPane transitionPane = new StackPane(currentRoot, curtain);
        scene.setRoot(transitionPane);

        // Animate curtain closing
        Timeline closeCurtain = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(curtainScale.xProperty(), 0, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.seconds(0.45), new KeyValue(curtainScale.xProperty(), 1, Interpolator.EASE_IN))
        );
        closeCurtain.setOnFinished(event -> {
            Parent menuContent;
            try {
                menuContent = loadMenuRoot();
            } catch (IOException ex) {
                ex.printStackTrace();
                transitionPane.getChildren().remove(curtain);
                startGame(); // Fallback to game if menu load fails
                return;
            }

            transitionPane.getChildren().set(0, menuContent);

            // Animate curtain opening
            Timeline openCurtain = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(curtainScale.xProperty(), 1, Interpolator.EASE_IN)),
                    new KeyFrame(Duration.seconds(0.5), new KeyValue(curtainScale.xProperty(), 0, Interpolator.EASE_OUT))
            );
            openCurtain.setOnFinished(finishEvent -> {
                // Clean up transition pane before setting new root
                transitionPane.getChildren().clear();
                scene.setRoot(menuContent);
                menuContent.requestFocus();
            });
            openCurtain.play();
        });
        closeCurtain.play();
    }

    /**
     * Shows main menu without transition animation (fallback).
     */
    private void showNewMenu() {
        try {
            Parent menuContent = loadMenuRoot();
            stage.getScene().setRoot(menuContent);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("Failed to load menu FXML. Starting game...");
            startGame();
        }
    }

    /**
     * Loads and configures the main menu.
     * Sets up menu callbacks for game mode selection and other options.
     *
     * @return the loaded menu Parent node
     * @throws IOException if menu.fxml cannot be loaded
     */

    /**
     * Loads and configures the main menu.
     * Sets up menu callbacks for game mode selection and other options.
     *
     * @return the loaded menu Parent node
     * @throws IOException if menu.fxml cannot be loaded
     */
    private Parent loadMenuRoot() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/menu.fxml"));
        Parent loadedMenuRoot = loader.load();
        menuRoot = loadedMenuRoot;
        menuController = loader.getController();

        SoundManager.getInstance().playMusic("menu.mp3");

        // Configure menu selection callbacks
        menuController.setOnSelectionCallback(selection -> {
            switch (selection) {
                case "Adventure":
                    SoundManager.getInstance().stopMusic();
                    nextGameMode = GameMode.ADVENTURE;
                    // Play intro video before starting game
                    fadeToBlack(this::playIntroVideo);
                    break;
                case "VERSUS":
                    SoundManager.getInstance().stopMusic();
                    nextGameMode = GameMode.LOCAL_BATTLE;
                    fadeToBlack(this::playIntroVideo);
                    break;
                case "CREDITS":
                    fadeToBlack(this::showRanking);
                    break;
                case "EXIT":
                    Platform.exit();
                    break;
                default:
                    System.out.println("Menu selection: " + selection);
                    break;
            }
        });

        return loadedMenuRoot;
    }

    // ==================== SCREEN TRANSITIONS ====================

    /**
     * Fades current screen to black and executes a callback action.
     * Creates smooth transition effect between major application states.
     * Properly handles cleanup of overlay elements after transition.
     *
     * @param onFinished callback to execute after fade completes
     */
    /**
     * Fades current screen to black and executes a callback action.
     * Creates smooth transition effect between major application states.
     * Properly handles cleanup of overlay elements after transition.
     *
     * @param onFinished callback to execute after fade completes
     */
    private void fadeToBlack(Runnable onFinished) {
        Scene scene = stage.getScene();
        Parent currentRoot = scene.getRoot();

        // Create black overlay canvas
        final Canvas blackOverlay = new Canvas(Constants.WIDTH, Constants.HEIGHT);
        GraphicsContext gc = blackOverlay.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, Constants.WIDTH, Constants.HEIGHT);
        blackOverlay.setOpacity(0);

        // Add overlay to current root (or wrap if needed)
        final Parent overlayContainer;
        final boolean wrapped;
        if (currentRoot instanceof Pane pane) {
            pane.getChildren().add(blackOverlay);
            overlayContainer = pane;
            wrapped = false;
        } else if (currentRoot instanceof Group group) {
            group.getChildren().add(blackOverlay);
            overlayContainer = group;
            wrapped = false;
        } else {
            Group wrapper = new Group(currentRoot, blackOverlay);
            scene.setRoot(wrapper);
            overlayContainer = wrapper;
            wrapped = true;
        }

        // Animate fade to black
        javafx.animation.FadeTransition fadeBlack =
                new javafx.animation.FadeTransition(Duration.seconds(1), blackOverlay);
        fadeBlack.setFromValue(0);
        fadeBlack.setToValue(1);
        fadeBlack.setInterpolator(Interpolator.EASE_IN);

        fadeBlack.setOnFinished(e -> {
            if (onFinished != null) onFinished.run();

            // Clean up overlay
            if (overlayContainer instanceof Pane pane) {
                pane.getChildren().remove(blackOverlay);
            } else if (overlayContainer instanceof Group group) {
                group.getChildren().remove(blackOverlay);
            }
            // Remove wrapper if it was created
            if (wrapped && scene.getRoot() == overlayContainer) {
                scene.setRoot(currentRoot);
            }
        });

        fadeBlack.play();
    }

    // ==================== INTRO VIDEO ====================

    /**
     * Preloads the intro video for smooth playback.
     * Video is loaded asynchronously to avoid blocking the UI.
     * Handles errors gracefully with fallback behavior.
     */
    private void preloadIntroVideo() {
        try {
            String videoPath = "/Videos/intro.mp4";
            URL videoUrl = getClass().getResource(videoPath);
            if (videoUrl == null) {
                System.err.println("❌ Video not found for preload: " + videoPath);
                return;
            }

            Media media = new Media(videoUrl.toExternalForm());
            introMediaPlayer = new MediaPlayer(media);
            introMediaPlayer.setAutoPlay(false);

            introMediaPlayer.setOnError(() -> {
                System.err.println("❌ Video preload error: " + introMediaPlayer.getError().getMessage());
                introMediaPlayer = null;
            });
        } catch (Exception e) {
            System.err.println("❌ Error initializing media player: " + e.getMessage());
            introMediaPlayer = null;
        }
    }

    /**
     * Plays the intro video with skip functionality.
     * Users can skip by pressing any key or clicking.
     * Automatically proceeds to game when video completes.
     */
    private void playIntroVideo() {
        if (introMediaPlayer == null) {
            System.err.println("⚠️ Video player not ready. Skipping to game.");
            startGame(nextGameMode);
            return;
        }

        MediaView mediaView = new MediaView(introMediaPlayer);
        mediaView.setFitWidth(Constants.WIDTH);
        mediaView.setFitHeight(Constants.HEIGHT);
        mediaView.setPreserveRatio(false);

        Pane videoRoot = new Pane(mediaView);
        videoRoot.setStyle("-fx-background-color: black;");

        stage.getScene().setRoot(videoRoot);

        // Auto-proceed when video ends
        introMediaPlayer.setOnEndOfMedia(() -> Platform.runLater(() -> {
            introMediaPlayer.stop();
            preloadIntroVideo(); // Reload for next use
            startGame(nextGameMode);
        }));

        // Allow skipping video
        Runnable skipAction = () -> {
            introMediaPlayer.stop();
            preloadIntroVideo();
            startGame(nextGameMode);
            stage.getScene().setOnKeyPressed(null);
            stage.getScene().removeEventFilter(MouseEvent.MOUSE_PRESSED, null);
        };

        stage.getScene().setOnKeyPressed(e -> skipAction.run());
        stage.getScene().addEventFilter(MouseEvent.MOUSE_PRESSED, e -> skipAction.run());

        introMediaPlayer.play();
    }

    // ==================== GAME INITIALIZATION ====================

    /**
     * Starts the game with default game mode.
     */
    // ==================== GAME INITIALIZATION ====================

    /**
     * Starts the game with default game mode.
     */
    private void startGame() {
        startGame(nextGameMode);
    }

    /**
     * Initializes and starts the game with specified game mode.
     * Creates new game scene and transfers control to GameSceneRoot.
     *
     * @param initialMode the game mode to start (ADVENTURE, LOCAL_BATTLE, etc.)
     */
    private void startGame(GameMode initialMode) {
        this.nextGameMode = initialMode;
        GameSceneRoot gameSceneRoot = new GameSceneRoot(this::showNewMenu, nextGameMode);
        stage.setScene(gameSceneRoot.getScene());
        stage.setResizable(false);
        stage.show();
    }

    // ==================== LEADERBOARD ====================

    /**
     * Displays the online leaderboard with top scores from Firebase.
     * Shows loading placeholder while fetching data asynchronously.
     * Handles network errors gracefully with user-friendly messages.
     */
    private void showRanking() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/leaderboard.fxml"));
        Parent leaderboardRoot;
        try {
            leaderboardRoot = loader.load();
        } catch (IOException ex) {
            ex.printStackTrace();
            returnToMenu();
            return;
        }

        LeaderboardController controller = loader.getController();
        controller.setSubtitle("Top 10 Online (Firebase)");
        controller.setBackAction(() -> Platform.runLater(() -> {
            returnToMenu();
            SoundManager.getInstance().playMusic("menu.mp3");
        }));

        // Show placeholder while loading
        controller.setScores(new ArrayList<>());

        // Fetch scores asynchronously from Firebase
        FirebaseScoreService.getTopScores()
                .thenAccept(scores -> Platform.runLater(() -> controller.setScores(scores)))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        AlertBox.display("Network Error", "Unable to load Firebase leaderboard.");
                        returnToMenu();
                    });
                    return null;
                });

        stage.getScene().setRoot(leaderboardRoot);
    }

    /**
     * Returns to the main menu from any screen.
     * Uses cached menu if available, otherwise loads fresh menu.
     */
    private void returnToMenu() {
        Scene scene = stage.getScene();
        if (menuRoot != null) {
            scene.setRoot(menuRoot);
            if (menuRoot instanceof Pane pane) {
                pane.requestFocus();
            } else {
                menuRoot.requestFocus();
            }
        } else {
            showNewMenu();
        }
    }

    // ==================== APPLICATION LIFECYCLE ====================

    /**
     * Called when application is shutting down.
     * Performs cleanup: stops sounds, sets player offline, releases resources.
     *
     * @throws Exception if cleanup fails
     */
    @Override
    public void stop() throws Exception {
        System.out.println("Application shutting down... Setting player offline.");
        SoundManager.getInstance().shutdown();
        ResourceManager resourceManager = ResourceManager.getInstance();
        resourceManager.clearCache();
        if (PlayerContext.isLoggedIn()) {
            OnlinePresenceService.goOffline(PlayerContext.uid);
        }

        super.stop();
        Platform.exit();
        System.exit(0);
    }

    /**
     * Application entry point.
     * Clears resource cache and launches the JavaFX application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {

        launch();
    }
}
