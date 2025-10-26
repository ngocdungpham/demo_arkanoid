// File: src/main/java/com/ooparkanoid/console/MainConsole.java
package com.ooparkanoid.console;

import com.ooparkanoid.graphics.ResourceManager;
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
import com.ooparkanoid.ui.MenuController;
import com.ooparkanoid.ui.GameSceneRoot;
import com.ooparkanoid.utils.Constants;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;


public class MainConsole extends Application {
    private Stage stage;
    private MediaPlayer introMediaPlayer;
    private EventHandler<KeyEvent> introSpaceHandler;
    private EventHandler<MouseEvent> introMouseHandler;

    @Override
    public void start(Stage stage) throws IOException {
        this.stage = stage;
        stage.setTitle("Arkanoid - Simple Brick Game");
        stage.setResizable(false);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/intro.fxml"));
        Parent introRoot = fxmlLoader.load();

        Scene scene = new Scene(introRoot, Constants.WIDTH, Constants.HEIGHT);
        stage.setScene(scene);
        stage.show();

        preloadIntroVideo();
        introSpaceHandler = event -> {
            if (event.getCode() == KeyCode.SPACE) {
                startTransition();
            }
        };

        introMouseHandler = event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                startTransition();
            }
        };

        scene.setOnKeyPressed(introSpaceHandler);
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, introMouseHandler);
    }

    private void startTransition() {
        Scene scene = stage.getScene();

        scene.setOnKeyPressed(null);
        scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, introMouseHandler);

        Canvas blackOverlay = new Canvas(Constants.WIDTH, Constants.HEIGHT);
        GraphicsContext gc = blackOverlay.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, Constants.WIDTH, Constants.HEIGHT);
        blackOverlay.setOpacity(0);

        Parent currentRoot = scene.getRoot();
        if (currentRoot instanceof Pane) {
            ((Pane) currentRoot).getChildren().add(blackOverlay);
        } else if (currentRoot instanceof Group) {
            ((Group) currentRoot).getChildren().add(blackOverlay);
        } else {
            Group wrapper = new Group(currentRoot, blackOverlay);
            scene.setRoot(wrapper);
        }

        FadeTransition fadeBlack = new FadeTransition(Duration.seconds(1), blackOverlay);
        fadeBlack.setFromValue(0);
        fadeBlack.setToValue(1);
        fadeBlack.setInterpolator(Interpolator.EASE_IN);

        fadeBlack.setOnFinished(e -> showNewMenu());

        fadeBlack.play();
    }

    /**
     * Sửa lại hàm này để nó gọi fadeToBlack KHI CHỌN MENU
     */
    private void showNewMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/menu.fxml"));
            Parent menuRoot = loader.load();
            MenuController menuController = loader.getController();

            // 3. Thiết lập callback
            menuController.setOnSelectionCallback(selection -> {
                switch (selection) {
                    case "Adventure":
                    case "VERSUS":
                        // SỬA Ở ĐÂY:
                        // Gọi hiệu ứng mờ dần, KHI XONG thì gọi startGame
                        fadeToBlack(() -> playIntroVideo());
                        break;
                    case "EXIT":
                        Platform.exit();
                        break;
                    default:
                        System.out.println("Lựa chọn: " + selection);
                        break;
                }
            });

            // 5. Hiển thị scene menu mới
            // BỎ COMMENT DÒNG NÀY ĐỂ HIỂN THỊ MENU
            stage.getScene().setRoot(menuRoot);

            // (Xóa bỏ đoạn code FadeTransition bị lỗi mà bạn đã dán vào đây)

        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("Không thể tải menu FXML mới. Bắt đầu game...");
            startGame(); // Fallback
        }
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
        if (currentRoot instanceof Pane) {
            ((Pane) currentRoot).getChildren().add(blackOverlay);
        } else if (currentRoot instanceof Group) {
            ((Group) currentRoot).getChildren().add(blackOverlay);
        } else {
            Group wrapper = new Group(currentRoot, blackOverlay);
            scene.setRoot(wrapper);
        }

        // 3. Tạo hiệu ứng
        FadeTransition fadeBlack = new FadeTransition(Duration.seconds(1), blackOverlay);
        fadeBlack.setFromValue(0);
        fadeBlack.setToValue(1);
        fadeBlack.setInterpolator(Interpolator.EASE_IN);

        // 4. Đặt hành động sau khi kết thúc
        fadeBlack.setOnFinished(e -> {
            if (onFinished != null) {
                onFinished.run();
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
            startGame();
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
                startGame();
            });
        });

        // Cho phép bỏ qua
        Runnable skipAction = () -> {
            introMediaPlayer.stop();
            preloadIntroVideo(); // Tải lại cho lần sau
            startGame();
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
        GameSceneRoot gameSceneRoot = new GameSceneRoot();
        stage.setScene(gameSceneRoot.getScene());
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        ResourceManager resourceManager = ResourceManager.getInstance();
        resourceManager.clearCache();
        launch();

    }
}