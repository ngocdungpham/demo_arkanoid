// File: src/main/java/com/ooparkanoid/console/MainConsole.java
package com.ooparkanoid.console;

import com.ooparkanoid.core.state.OnlinePresenceService;
import com.ooparkanoid.core.state.PlayerContext;
import java.util.ArrayList;
import com.ooparkanoid.core.score.FirebaseScoreService;
import com.ooparkanoid.core.state.GameMode;
import com.ooparkanoid.AlertBox;
import com.ooparkanoid.graphics.ResourceManager;
import com.ooparkanoid.ui.LoginController;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import com.ooparkanoid.ui.LeaderboardController;
import com.ooparkanoid.ui.MenuController;
import com.ooparkanoid.ui.GameSceneRoot;
import com.ooparkanoid.utils.Constants;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;
import com.ooparkanoid.sound.SoundManager;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;

import java.io.IOException;
import java.net.URL;


public class MainConsole extends Application {
    private Stage stage;
    private MediaPlayer introMediaPlayer;
    private EventHandler<KeyEvent> introSpaceHandler;
    private EventHandler<MouseEvent> introMouseHandler;

    private Parent menuRoot;
    private MenuController menuController;
    private GameMode nextGameMode = GameMode.ADVENTURE;;

    @Override
    public void start(Stage stage) throws IOException {
        this.stage = stage;
        stage.setTitle("Arkanoid - Simple Brick Game");
        stage.setResizable(false);

        // BỎ QUA INTRO VÀ HIỂN THỊ MÀN HÌNH ĐĂNG NHẬP
        showLoginScreen();

        stage.show();
    }

    private void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            LoginController controller = loader.getController();

            // Gán callback khi đăng nhập thành công
            controller.setOnLoginSuccess(() -> {
                // Báo danh online VỚI UID
                OnlinePresenceService.goOnline(PlayerContext.uid);

                // Bắt đầu game (chuyển tới Menu)
                startTransition(); // Giống như hàm bạn đã có
            });

            Scene scene = new Scene(root, Constants.WIDTH, Constants.HEIGHT);
            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Game đang đóng... Báo danh offline.");

        // Báo danh offline VỚI UID
        if (PlayerContext.isLoggedIn()) {
            OnlinePresenceService.goOffline(PlayerContext.uid);
        }

        super.stop();
        Platform.exit();
        System.exit(0);
    }

    private void startTransition() {
        SoundManager.getInstance().stopMusic();
        SoundManager.getInstance().play("transition");
        Scene scene = stage.getScene();

        scene.setOnKeyPressed(null);
//        scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, introMouseHandler);
        if (introMouseHandler != null) {
            scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, introMouseHandler);
            introMouseHandler = null;
        }

//        Canvas blackOverlay = new Canvas(Constants.WIDTH, Constants.HEIGHT);
//        GraphicsContext gc = blackOverlay.getGraphicsContext2D();
//        gc.setFill(Color.BLACK);
//        gc.fillRect(0, 0, Constants.WIDTH, Constants.HEIGHT);
//        blackOverlay.setOpacity(0);

        Parent currentRoot = scene.getRoot();
//        if (currentRoot instanceof Pane) {
//            ((Pane) currentRoot).getChildren().add(blackOverlay);
//        } else if (currentRoot instanceof Group) {
//            ((Group) currentRoot).getChildren().add(blackOverlay);
//        } else {
//            Group wrapper = new Group(currentRoot, blackOverlay);
//            scene.setRoot(wrapper);
        if (currentRoot == null) {
            showNewMenu();
            return;
        }

//        FadeTransition fadeBlack = new FadeTransition(Duration.seconds(1), blackOverlay);
//        fadeBlack.setFromValue(0);
//        fadeBlack.setToValue(1);
//        fadeBlack.setInterpolator(Interpolator.EASE_IN);
        Rectangle curtain = new Rectangle(Constants.WIDTH, Constants.HEIGHT, Color.BLACK);
        Scale curtainScale = new Scale(0, 1, Constants.WIDTH / 2.0, Constants.HEIGHT / 2.0);
        curtain.getTransforms().add(curtainScale);

        StackPane transitionPane = new StackPane();
        transitionPane.getChildren().add(currentRoot);
        transitionPane.getChildren().add(curtain);
        scene.setRoot(transitionPane);

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
                        scene.setRoot(currentRoot);
                        startGame();
                        return;
                    }

//        fadeBlack.setOnFinished(e -> showNewMenu());
                    transitionPane.getChildren().set(0, menuContent);
//        fadeBlack.play();
            Timeline openCurtain = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(curtainScale.xProperty(), 1, Interpolator.EASE_IN)),
                    new KeyFrame(Duration.seconds(0.5), new KeyValue(curtainScale.xProperty(), 0, Interpolator.EASE_OUT))
            );

            openCurtain.setOnFinished(finishEvent -> {
                transitionPane.getChildren().remove(curtain);
                scene.setRoot(menuContent);
                menuContent.requestFocus();
            });

            openCurtain.play();
        });

        closeCurtain.play();
    }

    /**
     * Sửa lại hàm này để nó gọi fadeToBlack KHI CHỌN MENU
     */
    private void showNewMenu() {
        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/menu.fxml"));
//            Parent menuRoot = loader.load();
//            MenuController menuController = loader.getController();
//            SoundManager.getInstance().playMusic("menu.mp3");
//            // 3. Thiết lập callback
//            menuController.setOnSelectionCallback(selection -> {
//                switch (selection) {
//                    case "Adventure":
//                        SoundManager.getInstance().stopMusic();
//                        nextGameMode = GameMode.ADVENTURE;
//                        fadeToBlack(this::playIntroVideo);
//                        break;
//                    case "VERSUS":
//                        // SỬA Ở ĐÂY:
//                        // Gọi hiệu ứng mờ dần, KHI XONG thì gọi startGame
//                        SoundManager.getInstance().stopMusic();
//                        nextGameMode = GameMode.LOCAL_BATTLE;
//                        fadeToBlack(() -> playIntroVideo());
//                        break;
//                    case "CREDITS":
//                        fadeToBlack(() -> showRanking());
//                        break;
//                    case "EXIT":
//                        Platform.exit();
//                        break;
//                    default:
//                        System.out.println("Lựa chọn: " + selection);
//                        break;
//                }
//            });

            // 5. Hiển thị scene menu mới
            // BỎ COMMENT DÒNG NÀY ĐỂ HIỂN THỊ MENU
//            stage.getScene().setRoot(menuRoot);

            // (Xóa bỏ đoạn code FadeTransition bị lỗi mà bạn đã dán vào đây)

            Parent menuContent = loadMenuRoot();
            stage.getScene().setRoot(menuContent);

        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("Không thể tải menu FXML mới. Bắt đầu game...");
            startGame(); // Fallback
        }
    }

    private Parent loadMenuRoot() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/menu.fxml"));
        Parent loadedMenuRoot = loader.load();
        menuRoot = loadedMenuRoot;
        menuController = loader.getController();
        SoundManager.getInstance().playMusic("menu.mp3");

        menuController.setOnSelectionCallback(selection -> {
            switch (selection) {
                case "Adventure":
                    SoundManager.getInstance().stopMusic();
                    nextGameMode = GameMode.ADVENTURE;
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
                    System.out.println("Lựa chọn: " + selection);
                    break;
            }
        });

        return loadedMenuRoot;
    }

    /**
     * THÊM HÀM NÀY VÀO (Hàm trợ giúp từ lần hỏi trước)
     * Tạo hiệu ứng mờ dần sang màu đen trên toàn bộ màn hình.
     * @param onFinished Hành động (Runnable) sẽ được gọi khi hiệu ứng kết thúc.
     */
    private void fadeToBlack(Runnable onFinished) {
        Scene scene = stage.getScene();
        Parent currentRoot = scene.getRoot();

        // 1. Tạo Canvas
        Canvas blackOverlay = new Canvas(Constants.WIDTH, Constants.HEIGHT);
        GraphicsContext gc = blackOverlay.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, Constants.WIDTH, Constants.HEIGHT);
        blackOverlay.setOpacity(0);

        // 2. Thêm vào root
//        if (currentRoot instanceof Pane) {
//            ((Pane) currentRoot).getChildren().add(blackOverlay);
//        } else if (currentRoot instanceof Group) {
//            ((Group) currentRoot).getChildren().add(blackOverlay);
        // 2. Thêm vào root và ghi nhớ container để gỡ bỏ sau này
        Parent overlayContainer = null;
        boolean wrapped = false;
        if (currentRoot instanceof Pane pane) {
            pane.getChildren().add(blackOverlay);
            overlayContainer = pane;
        } else if (currentRoot instanceof Group group) {
            group.getChildren().add(blackOverlay);
            overlayContainer = group;
        } else {
            Group wrapper = new Group(currentRoot, blackOverlay);
            scene.setRoot(wrapper);
            overlayContainer = wrapper;
            wrapped = true;
        }

        // 3. Tạo hiệu ứng
        FadeTransition fadeBlack = new FadeTransition(Duration.seconds(1), blackOverlay);
        fadeBlack.setFromValue(0);
        fadeBlack.setToValue(1);
        fadeBlack.setInterpolator(Interpolator.EASE_IN);

        Parent finalOverlayContainer = overlayContainer;
        boolean finalWrapped = wrapped;


        // 4. Đặt hành động sau khi kết thúc
        fadeBlack.setOnFinished(e -> {
            if (onFinished != null) {
                onFinished.run();
            }
            if (finalOverlayContainer instanceof Pane pane) {
                pane.getChildren().remove(blackOverlay);
            } else if (finalOverlayContainer instanceof Group group) {
                group.getChildren().remove(blackOverlay);
            }

            if (finalWrapped && scene.getRoot() == finalOverlayContainer) {
                scene.setRoot(currentRoot);
            }
        });

        // 5. Chạy
        fadeBlack.play();
    }



    /**
     * Phát một video MP4 giới thiệu, sau đó bắt đầu game.
     */
    private void preloadIntroVideo() {
        try {
            // Đường dẫn chính xác từ hình ảnh của bạn
            String videoPath = "/Videos/intro.mp4";
            URL videoUrl = getClass().getResource(videoPath);

            if (videoUrl == null) {
                System.err.println("Không tìm thấy video để preload: " + videoPath);
                return;
            }

            Media media = new Media(videoUrl.toExternalForm());
            introMediaPlayer = new MediaPlayer(media);
            introMediaPlayer.setAutoPlay(false); // Không tự phát

            introMediaPlayer.setOnError(() -> {
                System.err.println("Lỗi khi preload video: " + introMediaPlayer.getError().getMessage());
                introMediaPlayer = null;
            });

        } catch (Exception e) {
            System.err.println("Lỗi khi khởi tạo media player: " + e.getMessage());
            introMediaPlayer = null;
        }
    }

    /**
     * Phát video đã được tải trước (pre-loaded).
     * Hàm này không thay đổi.
     */
    private void playIntroVideo() {
        if (introMediaPlayer == null) {
            System.err.println("Video player chưa sẵn sàng. Bỏ qua và vào game.");
//            startGame();
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

        // Hành động khi video kết thúc
        introMediaPlayer.setOnEndOfMedia(() -> {
            Platform.runLater(() -> {
                introMediaPlayer.stop();
                preloadIntroVideo(); // Tải lại cho lần sau
//                startGame();
                startGame(nextGameMode);
            });
        });

        // Cho phép bỏ qua
        Runnable skipAction = () -> {
            introMediaPlayer.stop();
            preloadIntroVideo(); // Tải lại cho lần sau
//            startGame();
            startGame(nextGameMode);
            stage.getScene().setOnKeyPressed(null);
            stage.getScene().removeEventFilter(MouseEvent.MOUSE_PRESSED, null);
        };

        stage.getScene().setOnKeyPressed(e -> skipAction.run());
        stage.getScene().addEventFilter(MouseEvent.MOUSE_PRESSED, e -> skipAction.run());

        // Bắt đầu phát
        introMediaPlayer.play();
    }

    /**
     * Hàm này giữ nguyên
     */
    private void startGame() {
      //  GameSceneRoot gameSceneRoot = new GameSceneRoot();
        startGame(nextGameMode);
    }

    private void startGame(GameMode initialMode) {
        GameSceneRoot gameSceneRoot = new GameSceneRoot(this::showNewMenu, nextGameMode);
        stage.setScene(gameSceneRoot.getScene());
        stage.setResizable(false);
        stage.show();
    }

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
        controller.setSubtitle("Top 10 Online (Firebase)"); // Đặt tiêu đề
        controller.setBackAction(this::returnToMenu);

        // Đặt placeholder "Đang tải..."
        controller.setScores(new ArrayList<>());

        // Bắt đầu tải dữ liệu online từ Firebase
        FirebaseScoreService.getTopScores()
                .thenAccept(scores -> {
                    // Khi tải xong, cập nhật UI trên luồng JavaFX
                    Platform.runLater(() -> {
                        controller.setScores(scores);
                    });
                })
                .exceptionally(e -> {
                    // Nếu có lỗi mạng
                    Platform.runLater(() -> {
                        AlertBox.display("Lỗi Mạng", "Không thể tải bảng xếp hạng Firebase.");
                        returnToMenu();
                    });
                    return null;
                });

        // Hiển thị scene ngay
        Scene scene = stage.getScene();
        scene.setRoot(leaderboardRoot);
    }

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

    public static void main(String[] args) {
        ResourceManager resourceManager = ResourceManager.getInstance();
        resourceManager.clearCache();
        launch();
    }
}