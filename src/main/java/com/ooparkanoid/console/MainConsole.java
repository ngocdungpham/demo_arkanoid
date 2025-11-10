////// File: src/main/java/com/ooparkanoid/console/MainConsole.java
////package com.ooparkanoid.console;
////
////import com.ooparkanoid.core.state.OnlinePresenceService;
////import com.ooparkanoid.core.state.PlayerContext;
////import java.util.ArrayList;
////import com.ooparkanoid.core.score.FirebaseScoreService;
////import com.ooparkanoid.core.state.GameMode;
////import com.ooparkanoid.AlertBox;
////import com.ooparkanoid.graphics.ResourceManager;
////import com.ooparkanoid.ui.*;
////import javafx.animation.*;
////import javafx.application.Application;
////import javafx.application.Platform;
////import javafx.event.EventHandler;
////import javafx.fxml.FXMLLoader;
////import javafx.scene.Group;
////import javafx.scene.Parent;
////import javafx.scene.Scene;
////import javafx.scene.canvas.Canvas;
////import javafx.scene.canvas.GraphicsContext;
////import javafx.scene.input.MouseButton;
////import javafx.scene.input.MouseEvent;
////import javafx.scene.input.KeyEvent;
////import javafx.scene.layout.Pane;
////import javafx.scene.media.Media;
////import javafx.scene.media.MediaPlayer;
////import javafx.scene.media.MediaView;
////import javafx.scene.paint.Color;
////import javafx.stage.Stage;
////import com.ooparkanoid.utils.Constants;
////import javafx.scene.input.KeyCode;
////import javafx.util.Duration;
////import com.ooparkanoid.sound.SoundManager;
////import javafx.scene.layout.StackPane;
////import javafx.scene.shape.Rectangle;
////import javafx.scene.transform.Scale;
////
////import java.io.IOException;
////import java.net.URL;
////
////
////public class MainConsole extends Application {
////    private Stage stage;
////    private MediaPlayer introMediaPlayer;
////    private EventHandler<KeyEvent> introSpaceHandler;
////    private EventHandler<MouseEvent> introMouseHandler;
////
////    private Parent menuRoot;
////    private MenuController menuController;
////    private GameMode nextGameMode = GameMode.ADVENTURE;;
////
////    @Override
////    public void start(Stage stage) throws IOException {
////        this.stage = stage;
////        stage.setTitle("Arkanoid - Simple Brick Game");
////        stage.setResizable(false);
////
////        // BỎ QUA INTRO VÀ HIỂN THỊ MÀN HÌNH ĐĂNG NHẬP
////        showLoginScreen();
////
////        stage.show();
////    }
////
////    private void showLoginScreen() {
////        try {
////            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
////            Parent root = loader.load();
////            LoginController controller = loader.getController();
////
////            // Gán callback khi đăng nhập thành công
////            controller.setOnLoginSuccess(() -> {
////                // Báo danh online VỚI UID
////                OnlinePresenceService.goOnline(PlayerContext.uid);
////
////                // Bắt đầu game (chuyển tới Menu)
////                startTransition(); // Giống như hàm bạn đã có
////
////            });
////
////            controller.setOnGoToSignUp(() -> {
////                showSignUpScreen(); // Gọi hàm hiển thị màn hình đăng ký
////            });
////
////            Scene scene = (stage.getScene() == null)
////                    ? new Scene(root, Constants.WIDTH, Constants.HEIGHT)
////                    : stage.getScene();
////            scene.setRoot(root);
////            stage.setScene(scene);
////
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
////    }
////
////    private void showSignUpScreen() {
////        try {
////            FXMLLoader loader = new FXMLLoader(getClass().getResource("/signup.fxml"));
////            Parent root = loader.load();
////            SignupController controller = loader.getController();
////
////            // Gán callback khi đăng ký thành công
////            controller.setOnSignUpSuccess(() -> {
////                OnlinePresenceService.goOnline(PlayerContext.uid);
////                startTransition(); // Chuyển vào game
////            });
////
////            // Gán callback khi nhấn link "Login"
////            controller.setOnGoToLogin(() -> {
////                showLoginScreen(); // Quay lại màn hình đăng nhập
////            });
////
////            // Đặt root mới cho scene hiện tại
////            stage.getScene().setRoot(root);
////
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
////    }
////
////    @Override
////    public void stop() throws Exception {
////        System.out.println("Game đang đóng... Báo danh offline.");
////        // Shutdown SoundManager
////        SoundManager.getInstance().shutdown();
////
////        // Báo danh offline VỚI UID
////        if (PlayerContext.isLoggedIn()) {
////            OnlinePresenceService.goOffline(PlayerContext.uid);
////        }
////
////        super.stop();
////        Platform.exit();
////        System.exit(0);
////    }
////
////    private void startTransition() {
////        SoundManager.getInstance().stopMusic();
////        SoundManager.getInstance().play("transition");
////        Scene scene = stage.getScene();
////
////        scene.setOnKeyPressed(null);
//////        scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, introMouseHandler);
////        if (introMouseHandler != null) {
////            scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, introMouseHandler);
////            introMouseHandler = null;
////        }
////
//////        Canvas blackOverlay = new Canvas(Constants.WIDTH, Constants.HEIGHT);
//////        GraphicsContext gc = blackOverlay.getGraphicsContext2D();
//////        gc.setFill(Color.BLACK);
//////        gc.fillRect(0, 0, Constants.WIDTH, Constants.HEIGHT);
//////        blackOverlay.setOpacity(0);
////
////        Parent currentRoot = scene.getRoot();
//////        if (currentRoot instanceof Pane) {
//////            ((Pane) currentRoot).getChildren().add(blackOverlay);
//////        } else if (currentRoot instanceof Group) {
//////            ((Group) currentRoot).getChildren().add(blackOverlay);
//////        } else {
//////            Group wrapper = new Group(currentRoot, blackOverlay);
//////            scene.setRoot(wrapper);
////        if (currentRoot == null) {
////            showNewMenu();
////            return;
////        }
////
//////        FadeTransition fadeBlack = new FadeTransition(Duration.seconds(1), blackOverlay);
//////        fadeBlack.setFromValue(0);
//////        fadeBlack.setToValue(1);
//////        fadeBlack.setInterpolator(Interpolator.EASE_IN);
////        Rectangle curtain = new Rectangle(Constants.WIDTH, Constants.HEIGHT, Color.BLACK);
////        Scale curtainScale = new Scale(0, 1, Constants.WIDTH / 2.0, Constants.HEIGHT / 2.0);
////        curtain.getTransforms().add(curtainScale);
////
////        StackPane transitionPane = new StackPane();
////        transitionPane.getChildren().add(currentRoot);
////        transitionPane.getChildren().add(curtain);
////        scene.setRoot(transitionPane);
////
////        Timeline closeCurtain = new Timeline(
////                new KeyFrame(Duration.ZERO, new KeyValue(curtainScale.xProperty(), 0, Interpolator.EASE_OUT)),
////                new KeyFrame(Duration.seconds(0.45), new KeyValue(curtainScale.xProperty(), 1, Interpolator.EASE_IN))
////        );
////
////        closeCurtain.setOnFinished(event -> {
////            Parent menuContent;
////            try {
////                menuContent = loadMenuRoot();
////            } catch (IOException ex) {
////                ex.printStackTrace();
////                transitionPane.getChildren().remove(curtain);
////                scene.setRoot(currentRoot);
////                startGame();
////                return;
////            }
////
//////        fadeBlack.setOnFinished(e -> showNewMenu());
////            transitionPane.getChildren().set(0, menuContent);
//////        fadeBlack.play();
////            Timeline openCurtain = new Timeline(
////                    new KeyFrame(Duration.ZERO, new KeyValue(curtainScale.xProperty(), 1, Interpolator.EASE_IN)),
////                    new KeyFrame(Duration.seconds(0.5), new KeyValue(curtainScale.xProperty(), 0, Interpolator.EASE_OUT))
////            );
////
////            openCurtain.setOnFinished(finishEvent -> {
////                transitionPane.getChildren().remove(curtain);
////                scene.setRoot(menuContent);
////                menuContent.requestFocus();
////            });
////
////            openCurtain.play();
////        });
////
////        closeCurtain.play();
////    }
////
////    /**
////     * Sửa lại hàm này để nó gọi fadeToBlack KHI CHỌN MENU
////     */
////    private void showNewMenu() {
////        try {
//////            FXMLLoader loader = new FXMLLoader(getClass().getResource("/menu.fxml"));
//////            Parent menuRoot = loader.load();
//////            MenuController menuController = loader.getController();
//////            SoundManager.getInstance().playMusic("menu.mp3");
//////            // 3. Thiết lập callback
//////            menuController.setOnSelectionCallback(selection -> {
//////                switch (selection) {
//////                    case "Adventure":
//////                        SoundManager.getInstance().stopMusic();
//////                        nextGameMode = GameMode.ADVENTURE;
//////                        fadeToBlack(this::playIntroVideo);
//////                        break;
//////                    case "VERSUS":
//////                        // SỬA Ở ĐÂY:
//////                        // Gọi hiệu ứng mờ dần, KHI XONG thì gọi startGame
//////                        SoundManager.getInstance().stopMusic();
//////                        nextGameMode = GameMode.LOCAL_BATTLE;
//////                        fadeToBlack(() -> playIntroVideo());
//////                        break;
//////                    case "CREDITS":
//////                        fadeToBlack(() -> showRanking());
//////                        break;
//////                    case "EXIT":
//////                        Platform.exit();
//////                        break;
//////                    default:
//////                        System.out.println("Lựa chọn: " + selection);
//////                        break;
//////                }
//////            });
////
////            // 5. Hiển thị scene menu mới
////            // BỎ COMMENT DÒNG NÀY ĐỂ HIỂN THỊ MENU
//////            stage.getScene().setRoot(menuRoot);
////
////            // (Xóa bỏ đoạn code FadeTransition bị lỗi mà bạn đã dán vào đây)
////
////            Parent menuContent = loadMenuRoot();
////            stage.getScene().setRoot(menuContent);
////
////        } catch (IOException ex) {
////            ex.printStackTrace();
////            System.err.println("Không thể tải menu FXML mới. Bắt đầu game...");
////            startGame(); // Fallback
////        }
////    }
////
////    private Parent loadMenuRoot() throws IOException {
////        FXMLLoader loader = new FXMLLoader(getClass().getResource("/menu.fxml"));
////        Parent loadedMenuRoot = loader.load();
////        menuRoot = loadedMenuRoot;
////        menuController = loader.getController();
////        SoundManager.getInstance().playMusic("menu.mp3");
////
////        menuController.setOnSelectionCallback(selection -> {
////            switch (selection) {
////                case "Adventure":
////                    SoundManager.getInstance().stopMusic();
////                    nextGameMode = GameMode.ADVENTURE;
////                    fadeToBlack(this::playIntroVideo);
////                    break;
////                case "VERSUS":
////                    SoundManager.getInstance().stopMusic();
////                    nextGameMode = GameMode.LOCAL_BATTLE;
////                    fadeToBlack(this::playIntroVideo);
////                    break;
////                case "CREDITS":
////                    fadeToBlack(this::showRanking);
////                    break;
////                case "EXIT":
////                    Platform.exit();
////                    break;
////                default:
////                    System.out.println("Lựa chọn: " + selection);
////                    break;
////            }
////        });
////
////        return loadedMenuRoot;
////    }
////
////    /**
////     * THÊM HÀM NÀY VÀO (Hàm trợ giúp từ lần hỏi trước)
////     * Tạo hiệu ứng mờ dần sang màu đen trên toàn bộ màn hình.
////     * @param onFinished Hành động (Runnable) sẽ được gọi khi hiệu ứng kết thúc.
////     */
////    private void fadeToBlack(Runnable onFinished) {
////        Scene scene = stage.getScene();
////        Parent currentRoot = scene.getRoot();
////
////        // 1. Tạo Canvas
////        Canvas blackOverlay = new Canvas(Constants.WIDTH, Constants.HEIGHT);
////        GraphicsContext gc = blackOverlay.getGraphicsContext2D();
////        gc.setFill(Color.BLACK);
////        gc.fillRect(0, 0, Constants.WIDTH, Constants.HEIGHT);
////        blackOverlay.setOpacity(0);
////
////        // 2. Thêm vào root
//////        if (currentRoot instanceof Pane) {
//////            ((Pane) currentRoot).getChildren().add(blackOverlay);
//////        } else if (currentRoot instanceof Group) {
//////            ((Group) currentRoot).getChildren().add(blackOverlay);
////        // 2. Thêm vào root và ghi nhớ container để gỡ bỏ sau này
////        Parent overlayContainer = null;
////        boolean wrapped = false;
////        if (currentRoot instanceof Pane pane) {
////            pane.getChildren().add(blackOverlay);
////            overlayContainer = pane;
////        } else if (currentRoot instanceof Group group) {
////            group.getChildren().add(blackOverlay);
////            overlayContainer = group;
////        } else {
////            Group wrapper = new Group(currentRoot, blackOverlay);
////            scene.setRoot(wrapper);
////            overlayContainer = wrapper;
////            wrapped = true;
////        }
////
////        // 3. Tạo hiệu ứng
////        FadeTransition fadeBlack = new FadeTransition(Duration.seconds(1), blackOverlay);
////        fadeBlack.setFromValue(0);
////        fadeBlack.setToValue(1);
////        fadeBlack.setInterpolator(Interpolator.EASE_IN);
////
////        Parent finalOverlayContainer = overlayContainer;
////        boolean finalWrapped = wrapped;
////
////
////        // 4. Đặt hành động sau khi kết thúc
////        fadeBlack.setOnFinished(e -> {
////            if (onFinished != null) {
////                onFinished.run();
////            }
////            if (finalOverlayContainer instanceof Pane pane) {
////                pane.getChildren().remove(blackOverlay);
////            } else if (finalOverlayContainer instanceof Group group) {
////                group.getChildren().remove(blackOverlay);
////            }
////
////            if (finalWrapped && scene.getRoot() == finalOverlayContainer) {
////                scene.setRoot(currentRoot);
////            }
////        });
////
////        // 5. Chạy
////        fadeBlack.play();
////    }
////
////
////
////    /**
////     * Phát một video MP4 giới thiệu, sau đó bắt đầu game.
////     */
////    private void preloadIntroVideo() {
////        try {
////            // Đường dẫn chính xác từ hình ảnh của bạn
////            String videoPath = "/Videos/intro.mp4";
////            URL videoUrl = getClass().getResource(videoPath);
////
////            if (videoUrl == null) {
////                System.err.println("Không tìm thấy video để preload: " + videoPath);
////                return;
////            }
////
////            Media media = new Media(videoUrl.toExternalForm());
////            introMediaPlayer = new MediaPlayer(media);
////            introMediaPlayer.setAutoPlay(false); // Không tự phát
////
////            introMediaPlayer.setOnError(() -> {
////                System.err.println("Lỗi khi preload video: " + introMediaPlayer.getError().getMessage());
////                introMediaPlayer = null;
////            });
////
////        } catch (Exception e) {
////            System.err.println("Lỗi khi khởi tạo media player: " + e.getMessage());
////            introMediaPlayer = null;
////        }
////    }
////
////    /**
////     * Phát video đã được tải trước (pre-loaded).
////     * Hàm này không thay đổi.
////     */
////    private void playIntroVideo() {
////        if (introMediaPlayer == null) {
////            System.err.println("Video player chưa sẵn sàng. Bỏ qua và vào game.");
//////            startGame();
////            startGame(nextGameMode);
////            return;
////        }
////
////        MediaView mediaView = new MediaView(introMediaPlayer);
////
////        mediaView.setFitWidth(Constants.WIDTH);
////        mediaView.setFitHeight(Constants.HEIGHT);
////        mediaView.setPreserveRatio(false);
////
////        Pane videoRoot = new Pane(mediaView);
////        videoRoot.setStyle("-fx-background-color: black;");
////
////        stage.getScene().setRoot(videoRoot);
////
////        // Hành động khi video kết thúc
////        introMediaPlayer.setOnEndOfMedia(() -> {
////            Platform.runLater(() -> {
////                introMediaPlayer.stop();
////                preloadIntroVideo(); // Tải lại cho lần sau
//////                startGame();
////                startGame(nextGameMode);
////            });
////        });
////
////        // Cho phép bỏ qua
////        Runnable skipAction = () -> {
////            introMediaPlayer.stop();
////            preloadIntroVideo(); // Tải lại cho lần sau
//////            startGame();
////            startGame(nextGameMode);
////            stage.getScene().setOnKeyPressed(null);
////            stage.getScene().removeEventFilter(MouseEvent.MOUSE_PRESSED, null);
////        };
////
////        stage.getScene().setOnKeyPressed(e -> skipAction.run());
////        stage.getScene().addEventFilter(MouseEvent.MOUSE_PRESSED, e -> skipAction.run());
////
////        // Bắt đầu phát
////        introMediaPlayer.play();
////    }
////
////    /**
////     * Hàm này giữ nguyên
////     */
////    private void startGame() {
////        //  GameSceneRoot gameSceneRoot = new GameSceneRoot();
////        startGame(nextGameMode);
////    }
////
////    private void startGame(GameMode initialMode) {
////        GameSceneRoot gameSceneRoot = new GameSceneRoot(this::showNewMenu, nextGameMode);
////        stage.setScene(gameSceneRoot.getScene());
////        stage.setResizable(false);
////        stage.show();
////    }
////
////    private void showRanking() {
////        FXMLLoader loader = new FXMLLoader(getClass().getResource("/leaderboard.fxml"));
////        Parent leaderboardRoot;
////        try {
////            leaderboardRoot = loader.load();
////        } catch (IOException ex) {
////            ex.printStackTrace();
////            returnToMenu();
////            return;
////        }
////
////        LeaderboardController controller = loader.getController();
////        controller.setSubtitle("Top 10 Online (Firebase)"); // Đặt tiêu đề
//////        controller.setBackAction(this::returnToMenu);
////        controller.setBackAction(() -> Platform.runLater(() -> {
////            returnToMenu();
////            SoundManager.getInstance().playMusic("menu.mp3");
////        }));
////
////        // Đặt placeholder "Đang tải..."
////        controller.setScores(new ArrayList<>());
////
////        // Bắt đầu tải dữ liệu online từ Firebase
////        FirebaseScoreService.getTopScores()
////                .thenAccept(scores -> {
////                    // Khi tải xong, cập nhật UI trên luồng JavaFX
////                    Platform.runLater(() -> {
////                        controller.setScores(scores);
////                    });
////                })
////                .exceptionally(e -> {
////                    // Nếu có lỗi mạng
////                    Platform.runLater(() -> {
////                        AlertBox.display("Lỗi Mạng", "Không thể tải bảng xếp hạng Firebase.");
////                        returnToMenu();
////                    });
////                    return null;
////                });
////
////        // Hiển thị scene ngay
////        Scene scene = stage.getScene();
////        scene.setRoot(leaderboardRoot);
////    }
////
////    private void returnToMenu() {
////        Scene scene = stage.getScene();
////        if (menuRoot != null) {
////            scene.setRoot(menuRoot);
////            if (menuRoot instanceof Pane pane) {
////                pane.requestFocus();
////            } else {
////                menuRoot.requestFocus();
////            }
////        } else {
////            showNewMenu();
////        }
////    }
////
////    public static void main(String[] args) {
////        ResourceManager resourceManager = ResourceManager.getInstance();
////        resourceManager.clearCache();
////        launch();
////    }
////}
////package com.ooparkanoid.console;
////
////import com.ooparkanoid.core.state.OnlinePresenceService;
////import com.ooparkanoid.core.state.PlayerContext;
////import java.util.ArrayList;
////import com.ooparkanoid.core.score.FirebaseScoreService;
////import com.ooparkanoid.core.state.GameMode;
////import com.ooparkanoid.AlertBox;
////import com.ooparkanoid.graphics.ResourceManager;
////import com.ooparkanoid.ui.*;
////import javafx.animation.*;
////import javafx.application.Application;
////import javafx.application.Platform;
////import javafx.event.EventHandler;
////import javafx.fxml.FXMLLoader;
////import javafx.scene.Group;
////import javafx.scene.Parent;
////import javafx.scene.Scene;
////import javafx.scene.canvas.Canvas;
////import javafx.scene.canvas.GraphicsContext;
////import javafx.scene.input.MouseButton;
////import javafx.scene.input.MouseEvent;
////import javafx.scene.input.KeyEvent;
////import javafx.scene.layout.Pane;
////import javafx.scene.media.Media;
////import javafx.scene.media.MediaPlayer;
////import javafx.scene.media.MediaView;
////import javafx.scene.paint.Color;
////import javafx.stage.Stage;
////import com.ooparkanoid.utils.Constants;
////import javafx.scene.input.KeyCode;
////import javafx.util.Duration;
////import com.ooparkanoid.sound.SoundManager;
////import javafx.scene.layout.StackPane;
////import javafx.scene.shape.Rectangle;
////import javafx.scene.transform.Scale;
////
////import java.io.IOException;
////import java.net.URL;
////
////
////public class MainConsole extends Application {
////    private Stage stage;
////    // private MediaPlayer introMediaPlayer; // ĐÃ XÓA VÌ VIDEO KHÔNG TỒN TẠI
////    private EventHandler<KeyEvent> introSpaceHandler;
////    private EventHandler<MouseEvent> introMouseHandler;
////
////    private Parent menuRoot;
////    private MenuController menuController;
////    private GameMode nextGameMode = GameMode.ADVENTURE;;
////
////    @Override
////    public void start(Stage stage) throws IOException {
////        this.stage = stage;
////        stage.setTitle("Arkanoid - Simple Brick Game");
////        stage.setResizable(false);
////
////        // preloadIntroVideo(); // ĐÃ XÓA
////        showIntroScreen();
////        stage.show();
////    }
////
////    /**
////     * (HÀM MỚI) Hiển thị màn hình Intro FXML đầu tiên.
////     */
////    private void showIntroScreen() {
////        try {
////            // Đảm bảo bạn có file /Intro.fxml trỏ đến IntroController
////            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Intro.fxml"));
////            Parent root = loader.load();
////
////            Scene scene = (stage.getScene() == null)
////                    ? new Scene(root, Constants.WIDTH, Constants.HEIGHT)
////                    : stage.getScene();
////            scene.setRoot(root);
////            stage.setScene(scene);
////
////            // THÊM MỚI: Bật nhạc nền cho Intro
////            SoundManager.getInstance().playMusic("intro.mp3");
////
////            // Thêm handler cho phím SPACE
////            introSpaceHandler = e -> {
////                if (e.getCode() == KeyCode.SPACE) {
////                    scene.removeEventHandler(KeyEvent.KEY_PRESSED, introSpaceHandler);
////                    if (introMouseHandler != null) {
////                        scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, introMouseHandler);
////                    }
////                    // THÊM MỚI: Tắt nhạc Intro trước khi chuyển cảnh
////                    SoundManager.getInstance().stopMusic();
////                    fadeToBlack(this::transitionToLogin);
////                }
////            };
////
////            // Thêm handler cho click chuột (giống SPACE)
////            introMouseHandler = e -> {
////                scene.removeEventHandler(KeyEvent.KEY_PRESSED, introSpaceHandler);
////                scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, introMouseHandler);
////                // THÊM MỚI: Tắt nhạc Intro trước khi chuyển cảnh
////                SoundManager.getInstance().stopMusic();
////                fadeToBlack(this::transitionToLogin);
////            };
////
////            scene.addEventHandler(KeyEvent.KEY_PRESSED, introSpaceHandler);
////            scene.addEventFilter(MouseEvent.MOUSE_PRESSED, introMouseHandler);
////
////        } catch (IOException e) {
////            e.printStackTrace();
////            showLoginScreen();
////        }
////    }
////
////    /**
////     * Chuyển từ màn hình đen (sau Intro) sang Login bằng hiệu ứng "Mở rèm".
////     */
////    private void transitionToLogin() {
////        try {
////            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
////            Parent loginRoot = loader.load();
////            LoginController controller = loader.getController();
////
////            controller.setOnLoginSuccess(() -> {
////                OnlinePresenceService.goOnline(PlayerContext.uid);
////                transitionToMenu();
////            });
////
////            controller.setOnGoToSignUp(this::showSignUpScreen);
////
////            Scene scene = stage.getScene();
////
////            Rectangle curtain = new Rectangle(Constants.WIDTH, Constants.HEIGHT, Color.BLACK);
////            Scale curtainScale = new Scale(1, 1, Constants.WIDTH / 2.0, Constants.HEIGHT / 2.0);
////            curtain.getTransforms().add(curtainScale);
////
////            StackPane transitionPane = new StackPane();
////            transitionPane.getChildren().add(loginRoot);
////            transitionPane.getChildren().add(curtain);
////            scene.setRoot(transitionPane);
////
////            Timeline openCurtain = new Timeline(
////                    new KeyFrame(Duration.ZERO, new KeyValue(curtainScale.xProperty(), 1, Interpolator.EASE_IN)),
////                    new KeyFrame(Duration.seconds(0.5), new KeyValue(curtainScale.xProperty(), 0, Interpolator.EASE_OUT))
////            );
////
////            openCurtain.setOnFinished(finishEvent -> {
////                transitionPane.getChildren().remove(curtain);
////                scene.setRoot(loginRoot);
////                loginRoot.requestFocus();
////            });
////
////            openCurtain.play();
////
////        } catch (IOException e) {
////            e.printStackTrace();
////            showLoginScreen();
////        }
////    }
////
////
////    /**
////     * (Hàm cũ - không hiệu ứng) Dùng làm fallback.
////     */
////    private void showLoginScreen() {
////        try {
////            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
////            Parent root = loader.load();
////            LoginController controller = loader.getController();
////
////            controller.setOnLoginSuccess(() -> {
////                OnlinePresenceService.goOnline(PlayerContext.uid);
////                transitionToMenu();
////            });
////
////            controller.setOnGoToSignUp(() -> {
////                showSignUpScreen();
////            });
////
////            Scene scene = (stage.getScene() == null)
////                    ? new Scene(root, Constants.WIDTH, Constants.HEIGHT)
////                    : stage.getScene();
////            scene.setRoot(root);
////            stage.setScene(scene);
////
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
////    }
////
////    private void showSignUpScreen() {
////        try {
////            FXMLLoader loader = new FXMLLoader(getClass().getResource("/signup.fxml"));
////            Parent root = loader.load();
////            SignupController controller = loader.getController();
////
////            controller.setOnSignUpSuccess(() -> {
////                OnlinePresenceService.goOnline(PlayerContext.uid);
////                transitionToMenu();
////            });
////
////            controller.setOnGoToLogin(() -> {
////                showLoginScreen();
////            });
////
////            stage.getScene().setRoot(root);
////
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
////    }
////
////    @Override
////    public void stop() throws Exception {
////        System.out.println("Game đang đóng... Báo danh offline.");
////        SoundManager.getInstance().shutdown();
////
////        if (PlayerContext.isLoggedIn()) {
////            OnlinePresenceService.goOffline(PlayerContext.uid);
////        }
////
////        super.stop();
////        Platform.exit();
////        System.exit(0);
////    }
////
////    /**
////     * (ĐỔI TÊN TỪ startTransition)
////     * Thực hiện hiệu ứng "Đóng rèm" trên root hiện tại (Login),
////     * sau đó tải Menu, và "Mở rèm" để hiển thị Menu.
////     */
////    private void transitionToMenu() {
////        SoundManager.getInstance().stopMusic(); // Đảm bảo nhạc (nếu có) đã tắt
////        SoundManager.getInstance().play("transition.mp3"); // SỬA: Đảm bảo gọi đúng tên file nếu cần
////        Scene scene = stage.getScene();
////
////        if (introSpaceHandler != null) {
////            scene.removeEventHandler(KeyEvent.KEY_PRESSED, introSpaceHandler);
////            introSpaceHandler = null;
////        }
////        if (introMouseHandler != null) {
////            scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, introMouseHandler);
////            introMouseHandler = null;
////        }
////
////        Parent currentRoot = scene.getRoot();
////        if (currentRoot == null) {
////            showNewMenu();
////            return;
////        }
////
////        Rectangle curtain = new Rectangle(Constants.WIDTH, Constants.HEIGHT, Color.BLACK);
////        Scale curtainScale = new Scale(0, 1, Constants.WIDTH / 2.0, Constants.HEIGHT / 2.0);
////        curtain.getTransforms().add(curtainScale);
////
////        StackPane transitionPane = new StackPane();
////        transitionPane.getChildren().add(currentRoot);
////        transitionPane.getChildren().add(curtain);
////        scene.setRoot(transitionPane);
////
////        Timeline closeCurtain = new Timeline(
////                new KeyFrame(Duration.ZERO, new KeyValue(curtainScale.xProperty(), 0, Interpolator.EASE_OUT)),
////                new KeyFrame(Duration.seconds(0.45), new KeyValue(curtainScale.xProperty(), 1, Interpolator.EASE_IN))
////        );
////
////        closeCurtain.setOnFinished(event -> {
////            Parent menuContent;
////            try {
////                menuContent = loadMenuRoot();
////            } catch (IOException ex) {
////                ex.printStackTrace();
////                transitionPane.getChildren().remove(curtain);
////                scene.setRoot(currentRoot);
////                startGame(); // Fallback
////                return;
////            }
////
////            transitionPane.getChildren().set(0, menuContent);
////
////            Timeline openCurtain = new Timeline(
////                    new KeyFrame(Duration.ZERO, new KeyValue(curtainScale.xProperty(), 1, Interpolator.EASE_IN)),
////                    new KeyFrame(Duration.seconds(0.5), new KeyValue(curtainScale.xProperty(), 0, Interpolator.EASE_OUT))
////            );
////
////            openCurtain.setOnFinished(finishEvent -> {
////                transitionPane.getChildren().remove(curtain);
////                scene.setRoot(menuContent);
////                menuContent.requestFocus();
////            });
////
////            openCurtain.play();
////        });
////
////        closeCurtain.play();
////    }
////
////    private void showNewMenu() {
////        try {
////            Parent menuContent = loadMenuRoot();
////            stage.getScene().setRoot(menuContent);
////
////        } catch (IOException ex) {
////            ex.printStackTrace();
////            System.err.println("Không thể tải menu FXML mới. Bắt đầu game...");
////            startGame(); // Fallback
////        }
////    }
////
////    private Parent loadMenuRoot() throws IOException {
////        FXMLLoader loader = new FXMLLoader(getClass().getResource("/menu.fxml"));
////        Parent loadedMenuRoot = loader.load();
////        menuRoot = loadedMenuRoot;
////        menuController = loader.getController();
////        SoundManager.getInstance().playMusic("menu.mp3");
////
////        menuController.setOnSelectionCallback(selection -> {
////            switch (selection) {
////                case "Adventure":
////                    SoundManager.getInstance().stopMusic();
////                    nextGameMode = GameMode.ADVENTURE;
////                    // THAY ĐỔI: Bỏ qua video, vào thẳng game
////                    fadeToBlack(this::startGame);
////                    break;
////                case "VERSUS":
////                    SoundManager.getInstance().stopMusic();
////                    nextGameMode = GameMode.LOCAL_BATTLE;
////                    // THAY ĐỔI: Bỏ qua video, vào thẳng game
////                    fadeToBlack(this::startGame);
////                    break;
////                case "CREDITS":
////                    fadeToBlack(this::showRanking);
////                    break;
////                case "EXIT":
////                    Platform.exit();
////                    break;
////                default:
////                    System.out.println("Lựa chọn: " + selection);
////                    break;
////            }
////        });
////
////        return loadedMenuRoot;
////    }
////
////    private void fadeToBlack(Runnable onFinished) {
////        Scene scene = stage.getScene();
////        Parent currentRoot = scene.getRoot();
////
////        Canvas blackOverlay = new Canvas(Constants.WIDTH, Constants.HEIGHT);
////        GraphicsContext gc = blackOverlay.getGraphicsContext2D();
////        gc.setFill(Color.BLACK);
////        gc.fillRect(0, 0, Constants.WIDTH, Constants.HEIGHT);
////        blackOverlay.setOpacity(0);
////
////        Parent overlayContainer = null;
////        boolean wrapped = false;
////        if (currentRoot instanceof Pane pane) {
////            pane.getChildren().add(blackOverlay);
////            overlayContainer = pane;
////        } else if (currentRoot instanceof Group group) {
////            group.getChildren().add(blackOverlay);
////            overlayContainer = group;
////        } else {
////            Group wrapper = new Group(currentRoot, blackOverlay);
////            scene.setRoot(wrapper);
////            overlayContainer = wrapper;
////            wrapped = true;
////        }
////
////        FadeTransition fadeBlack = new FadeTransition(Duration.seconds(1), blackOverlay);
////        fadeBlack.setFromValue(0);
////        fadeBlack.setToValue(1);
////        fadeBlack.setInterpolator(Interpolator.EASE_IN);
////
////        Parent finalOverlayContainer = overlayContainer;
////        boolean finalWrapped = wrapped;
////
////        fadeBlack.setOnFinished(e -> {
////            if (onFinished != null) {
////                onFinished.run();
////            }
////        });
////
////        fadeBlack.play();
////    }
////
////
////    /*
////     * CÁC HÀM LIÊN QUAN ĐẾN VIDEO ĐÃ BỊ XÓA HOẶC THAY ĐỔI
////     */
////
////    // private void preloadIntroVideo() { ... } // ĐÃ BỊ VÔ HIỆU HÓA (xem start())
////
////    // private void playIntroVideo() { ... } // ĐÃ BỊ VÔ HIỆU HÓA (xem loadMenuRoot())
////
////    /**
////     * THAY ĐỔI: Cần một hàm startGame() không có tham số để
////     * fadeToBlack(this::startGame) hoạt động
////     */
////    private void startGame() {
////        startGame(nextGameMode);
////    }
////
////    private void startGame(GameMode initialMode) {
////        // CẬP NHẬT: Gán lại nextGameMode phòng trường hợp
////        // hàm này được gọi trực tiếp
////        this.nextGameMode = initialMode;
////
////        GameSceneRoot gameSceneRoot = new GameSceneRoot(this::showNewMenu, nextGameMode);
////        stage.setScene(gameSceneRoot.getScene());
////        stage.setResizable(false);
////        stage.show();
////    }
////
////    private void showRanking() {
////        FXMLLoader loader = new FXMLLoader(getClass().getResource("/leaderboard.fxml"));
////        Parent leaderboardRoot;
////        try {
////            leaderboardRoot = loader.load();
////        } catch (IOException ex) {
////            ex.printStackTrace();
////            returnToMenu();
////            return;
////        }
////
////        LeaderboardController controller = loader.getController();
////        controller.setSubtitle("Top 10 Online (Firebase)");
////        controller.setBackAction(() -> Platform.runLater(() -> {
////            returnToMenu();
////            SoundManager.getInstance().playMusic("menu.mp3");
////        }));
////
////        controller.setScores(new ArrayList<>());
////
////        FirebaseScoreService.getTopScores()
////                .thenAccept(scores -> {
////                    Platform.runLater(() -> {
////                        controller.setScores(scores);
////                    });
////                })
////                .exceptionally(e -> {
////                    Platform.runLater(() -> {
////                        AlertBox.display("Lỗi Mạng", "Không thể tải bảng xếp hạng Firebase.");
////                        returnToMenu();
////                    });
////                    return null;
////                });
////
////        Scene scene = stage.getScene();
////        scene.setRoot(leaderboardRoot);
////    }
////
////    private void returnToMenu() {
////        Scene scene = stage.getScene();
////        if (menuRoot != null) {
////            scene.setRoot(menuRoot);
////            if (menuRoot instanceof Pane pane) {
////                pane.requestFocus();
////            } else {
////                menuRoot.requestFocus();
////            }
////        } else {
////            showNewMenu();
////        }
////    }
////
////    public static void main(String[] args) {
////        ResourceManager resourceManager = ResourceManager.getInstance();
////        resourceManager.clearCache();
////        launch();
////    }
////}
//package com.ooparkanoid.console;
//
//import com.ooparkanoid.core.state.OnlinePresenceService;
//import com.ooparkanoid.core.state.PlayerContext;
//import java.util.ArrayList;
//import com.ooparkanoid.core.score.FirebaseScoreService;
//import com.ooparkanoid.core.state.GameMode;
//import com.ooparkanoid.AlertBox;
//import com.ooparkanoid.graphics.ResourceManager;
//import com.ooparkanoid.ui.*;
//import javafx.animation.*;
//import javafx.application.Application;
//import javafx.application.Platform;
//import javafx.event.EventHandler;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Group;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.scene.canvas.Canvas;
//import javafx.scene.canvas.GraphicsContext;
//import javafx.scene.input.MouseButton;
//import javafx.scene.input.MouseEvent;
//import javafx.scene.input.KeyEvent;
//import javafx.scene.layout.Pane;
//import javafx.scene.media.Media;
//import javafx.scene.media.MediaPlayer; // KHÔI PHỤC IMPORT
//import javafx.scene.media.MediaView;   // KHÔI PHỤC IMPORT
//import javafx.scene.paint.Color;
//import javafx.stage.Stage;
//import com.ooparkanoid.utils.Constants;
//import javafx.scene.input.KeyCode;
//import javafx.util.Duration;
//import com.ooparkanoid.sound.SoundManager;
//import javafx.scene.layout.StackPane;
//import javafx.scene.shape.Rectangle;
//import javafx.scene.transform.Scale;
//
//import java.io.IOException;
//import java.net.URL;
//
//
//public class MainConsole extends Application {
//    private Stage stage;
//    private MediaPlayer introMediaPlayer; // KHÔI PHỤC DÒNG NÀY
//    private EventHandler<KeyEvent> introSpaceHandler;
//    private EventHandler<MouseEvent> introMouseHandler;
//
//    private Parent menuRoot;
//    private MenuController menuController;
//    private GameMode nextGameMode = GameMode.ADVENTURE;;
//
//    @Override
//    public void start(Stage stage) throws IOException {
//        this.stage = stage;
//        stage.setTitle("Arkanoid - Simple Brick Game");
//        stage.setResizable(false);
//
//        // HIỂN THỊ MÀN HÌNH INTRO ĐẦU TIÊN
//        showIntroScreen(); // <-- ĐÃ SỬA
//
//        stage.show();
//    }
//
//    /**
//     * Hiển thị màn hình Intro FXML đầu tiên.
//     */
//    private void showIntroScreen() {
//        try {
//            // 1. Tải FXML
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Intro.fxml"));
//            Parent root = loader.load(); // Tệp này sẽ sử dụng IntroController
//
//            // 2. Lấy Scene
//            Scene scene = (stage.getScene() == null)
//                    ? new Scene(root, Constants.WIDTH, Constants.HEIGHT)
//                    : stage.getScene();
//            scene.setRoot(root);
//
//            // 3. Phát nhạc intro
//            SoundManager.getInstance().playMusic("intro.mp3");
//
//            // 4. Định nghĩa hành động khi bỏ qua intro
//            Runnable skipIntroAction = () -> {
//                // Dọn dẹp các trình xử lý sự kiện NGAY LẬP TỨC
//                // để người dùng không thể nhấn 2 lần
//                scene.setOnKeyPressed(null);
//                if (introMouseHandler != null) {
//                    scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, introMouseHandler);
//                    introMouseHandler = null;
//                }
//
//                // Phát âm thanh và dừng nhạc
//                SoundManager.getInstance().play("selected");
//                SoundManager.getInstance().stopMusic();
//
//                // THAY ĐỔI: Gọi hiệu ứng mờ dần
//                // Khi mờ đen xong, nó sẽ tự động gọi this::showLoginScreen
//                fadeToBlack(this::showLoginScreen);
//            };
//
//            // 5. Gán sự kiện (sử dụng các biến bạn đã khai báo)
//            introSpaceHandler = (KeyEvent event) -> {
//                if (event.getCode() == KeyCode.SPACE) {
//                    skipIntroAction.run();
//                }
//            };
//
//            introMouseHandler = (MouseEvent event) -> {
//                if (event.getButton() == MouseButton.PRIMARY) {
//                    skipIntroAction.run();
//                }
//            };
//
//            scene.setOnKeyPressed(introSpaceHandler);
//            scene.addEventFilter(MouseEvent.MOUSE_PRESSED, introMouseHandler);
//
//            // 6. Hiển thị
//            stage.setScene(scene);
//            root.requestFocus(); // Quan trọng để nhận sự kiện phím
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            // Xử lý lỗi: Nếu không tải được Intro.fxml, vào thẳng đăng nhập
//            System.err.println("Không thể tải Intro.fxml, vào thẳng màn hình đăng nhập.");
//            showLoginScreen();
//        }
//    }
//
//    /**
//     * Chuyển từ màn hình đen (sau Intro) sang Login bằng hiệu ứng "Mở rèm".
//     */
//    private void transitionToLogin() {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
//            Parent loginRoot = loader.load();
//            LoginController controller = loader.getController();
//
//            controller.setOnLoginSuccess(() -> {
//                OnlinePresenceService.goOnline(PlayerContext.uid);
//                transitionToMenu();
//            });
//
//            controller.setOnGoToSignUp(this::showSignUpScreen);
//
//            Scene scene = stage.getScene();
//
//            Rectangle curtain = new Rectangle(Constants.WIDTH, Constants.HEIGHT, Color.BLACK);
//            Scale curtainScale = new Scale(1, 1, Constants.WIDTH / 2.0, Constants.HEIGHT / 2.0);
//            curtain.getTransforms().add(curtainScale);
//
//            StackPane transitionPane = new StackPane();
//            transitionPane.getChildren().add(loginRoot);
//            transitionPane.getChildren().add(curtain);
//            scene.setRoot(transitionPane);
//
//            Timeline openCurtain = new Timeline(
//                    new KeyFrame(Duration.ZERO, new KeyValue(curtainScale.xProperty(), 1, Interpolator.EASE_IN)),
//                    new KeyFrame(Duration.seconds(0.5), new KeyValue(curtainScale.xProperty(), 0, Interpolator.EASE_OUT))
//            );
//
//            openCurtain.setOnFinished(finishEvent -> {
//                transitionPane.getChildren().remove(curtain);
//                scene.setRoot(loginRoot);
//                loginRoot.requestFocus();
//            });
//
//            openCurtain.play();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            showLoginScreen();
//        }
//    }
//
//
//    /**
//     * (Hàm cũ - không hiệu ứng) Dùng làm fallback.
//     */
//    private void showLoginScreen() {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
//            Parent root = loader.load();
//            LoginController controller = loader.getController();
//
//            controller.setOnLoginSuccess(() -> {
//                OnlinePresenceService.goOnline(PlayerContext.uid);
//                preloadIntroVideo();
//                transitionToMenu();
//            });
//
//            controller.setOnGoToSignUp(() -> {
//                showSignUpScreen();
//            });
//
//            Scene scene = (stage.getScene() == null)
//                    ? new Scene(root, Constants.WIDTH, Constants.HEIGHT)
//                    : stage.getScene();
//            scene.setRoot(root);
//            stage.setScene(scene);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void showSignUpScreen() {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/signup.fxml"));
//            Parent root = loader.load();
//            SignupController controller = loader.getController();
//
//            controller.setOnSignUpSuccess(() -> {
//                OnlinePresenceService.goOnline(PlayerContext.uid);
//                preloadIntroVideo();
//                transitionToMenu();
//            });
//
//            controller.setOnGoToLogin(() -> {
//                showLoginScreen();
//            });
//
//            stage.getScene().setRoot(root);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void stop() throws Exception {
//        System.out.println("Game đang đóng... Báo danh offline.");
//        SoundManager.getInstance().shutdown();
//
//        if (PlayerContext.isLoggedIn()) {
//            OnlinePresenceService.goOffline(PlayerContext.uid);
//        }
//
//        super.stop();
//        Platform.exit();
//        System.exit(0);
//    }
//
//    /**
//     * Thực hiện hiệu ứng "Đóng rèm" (tách đôi)
//     */
//    private void transitionToMenu() {
//        SoundManager.getInstance().stopMusic();
//        SoundManager.getInstance().play("transition.mp3"); // Đảm bảo bạn có file "transition.mp3"
//        Scene scene = stage.getScene();
//
//        if (introSpaceHandler != null) {
//            scene.removeEventHandler(KeyEvent.KEY_PRESSED, introSpaceHandler);
//            introSpaceHandler = null;
//        }
//        if (introMouseHandler != null) {
//            scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, introMouseHandler);
//            introMouseHandler = null;
//        }
//
//        Parent currentRoot = scene.getRoot();
//        if (currentRoot == null) {
//            showNewMenu();
//            return;
//        }
//
//        Rectangle curtain = new Rectangle(Constants.WIDTH, Constants.HEIGHT, Color.BLACK);
//        Scale curtainScale = new Scale(0, 1, Constants.WIDTH / 2.0, Constants.HEIGHT / 2.0);
//        curtain.getTransforms().add(curtainScale);
//
//        StackPane transitionPane = new StackPane();
//        transitionPane.getChildren().add(currentRoot);
//        transitionPane.getChildren().add(curtain);
//        scene.setRoot(transitionPane);
//
//        Timeline closeCurtain = new Timeline(
//                new KeyFrame(Duration.ZERO, new KeyValue(curtainScale.xProperty(), 0, Interpolator.EASE_OUT)),
//                new KeyFrame(Duration.seconds(0.45), new KeyValue(curtainScale.xProperty(), 1, Interpolator.EASE_IN))
//        );
//
//        closeCurtain.setOnFinished(event -> {
//            Parent menuContent;
//            try {
//                menuContent = loadMenuRoot();
//            } catch (IOException ex) {
//                ex.printStackTrace();
//                transitionPane.getChildren().remove(curtain);
//               // scene.setRoot(currentRoot);
//                startGame(); // Fallback
//                return;
//            }
//
//            transitionPane.getChildren().set(0, menuContent);
//
//            Timeline openCurtain = new Timeline(
//                    new KeyFrame(Duration.ZERO, new KeyValue(curtainScale.xProperty(), 1, Interpolator.EASE_IN)),
//                    new KeyFrame(Duration.seconds(0.5), new KeyValue(curtainScale.xProperty(), 0, Interpolator.EASE_OUT))
//            );
//
//            openCurtain.setOnFinished(finishEvent -> {
//                transitionPane.getChildren().remove(curtain);
//                scene.setRoot(menuContent);
//                menuContent.requestFocus();
//            });
//
//            openCurtain.play();
//        });
//
//        closeCurtain.play();
//    }
//
//    private void showNewMenu() {
//        try {
//            Parent menuContent = loadMenuRoot();
//            stage.getScene().setRoot(menuContent);
//
//        } catch (IOException ex) {
//            ex.printStackTrace();
//            System.err.println("Không thể tải menu FXML mới. Bắt đầu game...");
//            startGame(); // Fallback
//        }
//    }
//
//    private Parent loadMenuRoot() throws IOException {
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("/menu.fxml"));
//        Parent loadedMenuRoot = loader.load();
//        menuRoot = loadedMenuRoot;
//        menuController = loader.getController();
//        SoundManager.getInstance().playMusic("menu.mp3");
//
//        menuController.setOnSelectionCallback(selection -> {
//            switch (selection) {
//                case "Adventure":
//                    SoundManager.getInstance().stopMusic();
//                    nextGameMode = GameMode.ADVENTURE;
//                    // KHÔI PHỤC: Gọi hàm phát video
//                    fadeToBlack(this::playIntroVideo);
//                    break;
//                case "VERSUS":
//                    SoundManager.getInstance().stopMusic();
//                    nextGameMode = GameMode.LOCAL_BATTLE;
//                    // KHÔI PHỤC: Gọi hàm phát video
//                    fadeToBlack(this::playIntroVideo);
//                    break;
//                case "CREDITS":
//                    fadeToBlack(this::showRanking);
//                    break;
//                case "EXIT":
//                    Platform.exit();
//                    break;
//                default:
//                    System.out.println("Lựa chọn: " + selection);
//                    break;
//            }
//        });
//
//        return loadedMenuRoot;
//    }
//
////    private void fadeToBlack(Runnable onFinished) {
////        Scene scene = stage.getScene();
////        Parent currentRoot = scene.getRoot();
////
////        Canvas blackOverlay = new Canvas(Constants.WIDTH, Constants.HEIGHT);
////        GraphicsContext gc = blackOverlay.getGraphicsContext2D();
////        gc.setFill(Color.BLACK);
////        gc.fillRect(0, 0, Constants.WIDTH, Constants.HEIGHT);
////        blackOverlay.setOpacity(0);
////
////        Parent overlayContainer = null;
////        boolean wrapped = false;
////        if (currentRoot instanceof Pane pane) {
////            pane.getChildren().add(blackOverlay);
////            overlayContainer = pane;
////        } else if (currentRoot instanceof Group group) {
////            group.getChildren().add(blackOverlay);
////            overlayContainer = group;
////        } else {
////            Group wrapper = new Group(currentRoot, blackOverlay);
////            scene.setRoot(wrapper);
////            overlayContainer = wrapper;
////            wrapped = true;
////        }
////
////        FadeTransition fadeBlack = new FadeTransition(Duration.seconds(1), blackOverlay);
////        fadeBlack.setFromValue(0);
////        fadeBlack.setToValue(1);
////        fadeBlack.setInterpolator(Interpolator.EASE_IN);
////
////        Parent finalOverlayContainer = overlayContainer;
////        boolean finalWrapped = wrapped;
////
////        fadeBlack.setOnFinished(e -> {
////            if (onFinished != null) {
////                onFinished.run();
////            }
////        });
////
////        fadeBlack.play();
////    }
//    /**
//     * Hiệu ứng mờ dần sang màu đen.
//     * @param onFinished Hành động được gọi khi hiệu ứng kết thúc.
//     */
//    private void fadeToBlack(Runnable onFinished) {
//        Scene scene = stage.getScene();
//        Parent currentRoot = scene.getRoot();
//
//        // Thêm 'final' để lambda 'setOnFinished' có thể truy cập
//        final Canvas blackOverlay = new Canvas(Constants.WIDTH, Constants.HEIGHT);
//        GraphicsContext gc = blackOverlay.getGraphicsContext2D();
//        gc.setFill(Color.BLACK);
//        gc.fillRect(0, 0, Constants.WIDTH, Constants.HEIGHT);
//        blackOverlay.setOpacity(0);
//
//        // Thêm 'final'
//        final Parent overlayContainer;
//        final boolean wrapped;
//        if (currentRoot instanceof Pane pane) {
//            pane.getChildren().add(blackOverlay);
//            overlayContainer = pane;
//            wrapped = false;
//        } else if (currentRoot instanceof Group group) {
//            group.getChildren().add(blackOverlay);
//            overlayContainer = group;
//            wrapped = false;
//        } else {
//            Group wrapper = new Group(currentRoot, blackOverlay);
//            scene.setRoot(wrapper);
//            overlayContainer = wrapper;
//            wrapped = true;
//        }
//
//        FadeTransition fadeBlack = new FadeTransition(Duration.seconds(1), blackOverlay);
//        fadeBlack.setFromValue(0);
//        fadeBlack.setToValue(1);
//        fadeBlack.setInterpolator(Interpolator.EASE_IN);
//
//        // 'setOnFinished' SẼ ĐƯỢC CẬP NHẬT
//        fadeBlack.setOnFinished(e -> {
//            if (onFinished != null) {
//                // Chạy hành động (ví dụ: showRanking())
//                // showRanking() sẽ thay thế root của scene
//                onFinished.run();
//            }
//
//            // --- PHẦN SỬA LỖI (CLEANUP) ---
//            // Gỡ bỏ lớp che màu đen khỏi (menuRoot)
//            // để khi chúng ta gọi returnToMenu(), nó không bị đen.
//            if (overlayContainer instanceof Pane pane) {
//                pane.getChildren().remove(blackOverlay);
//            } else if (overlayContainer instanceof Group group) {
//                group.getChildren().remove(blackOverlay);
//            }
//
//            // Nếu chúng ta đã bọc root, hãy gỡ bọc
//            // (Chỉ gỡ bọc nếu scene KHÔNG bị thay thế bởi onFinished)
//            if (wrapped && scene.getRoot() == overlayContainer) {
//                scene.setRoot(currentRoot);
//            }
//            // --- KẾT THÚC PHẦN SỬA LỖI ---
//        });
//
//        fadeBlack.play();
//    }
//
//
//    /*
//     * CÁC HÀM LIÊN QUAN ĐẾN VIDEO ĐÃ ĐƯỢC KHÔI PHỤC
//     */
//
//    /**
//     * KHÔI PHỤC HÀM NÀY
//     * Tải trước video MP4 giới thiệu.
//     */
//    private void preloadIntroVideo() {
//        try {
//            // Đường dẫn này PHẢI tồn tại: "src/main/resources/Videos/intro.mp4"
//            String videoPath = "/Videos/intro.mp4";
//            URL videoUrl = getClass().getResource(videoPath);
//
//            if (videoUrl == null) {
//                System.err.println("Không tìm thấy video để preload: " + videoPath);
//                return;
//            }
//
//            Media media = new Media(videoUrl.toExternalForm());
//            introMediaPlayer = new MediaPlayer(media);
//            introMediaPlayer.setAutoPlay(false);
//
//            introMediaPlayer.setOnError(() -> {
//                System.err.println("Lỗi khi preload video: " + introMediaPlayer.getError().getMessage());
//                introMediaPlayer = null;
//            });
//
//        } catch (Exception e) {
//            System.err.println("Lỗi khi khởi tạo media player: " + e.getMessage());
//            introMediaPlayer = null;
//        }
//    }
//
//    /**
//     * KHÔI PHỤC HÀM NÀY
//     * Phát video đã được tải trước (pre-loaded).
//     */
//    private void playIntroVideo() {
//        if (introMediaPlayer == null) {
//            System.err.println("Video player chưa sẵn sàng (File có thể bị thiếu). Bỏ qua và vào game.");
//            startGame(nextGameMode); // Bỏ qua nếu video lỗi
//            return;
//        }
//
//        MediaView mediaView = new MediaView(introMediaPlayer);
//
//        mediaView.setFitWidth(Constants.WIDTH);
//        mediaView.setFitHeight(Constants.HEIGHT);
//        mediaView.setPreserveRatio(false);
//
//        Pane videoRoot = new Pane(mediaView);
//        videoRoot.setStyle("-fx-background-color: black;");
//
//        stage.getScene().setRoot(videoRoot);
//
//        // Hành động khi video kết thúc
//        introMediaPlayer.setOnEndOfMedia(() -> {
//            Platform.runLater(() -> {
//                introMediaPlayer.stop();
//                preloadIntroVideo(); // Tải lại cho lần sau
//                startGame(nextGameMode); // Gọi hàm có tham số
//            });
//        });
//
//        // Cho phép bỏ qua
//        Runnable skipAction = () -> {
//            introMediaPlayer.stop();
//            preloadIntroVideo(); // Tải lại cho lần sau
//            startGame(nextGameMode); // Gọi hàm có tham số
//            stage.getScene().setOnKeyPressed(null);
//            stage.getScene().removeEventFilter(MouseEvent.MOUSE_PRESSED, null);
//        };
//
//        stage.getScene().setOnKeyPressed(e -> skipAction.run());
//        stage.getScene().addEventFilter(MouseEvent.MOUSE_PRESSED, e -> skipAction.run());
//
//        // Bắt đầu phát
//        introMediaPlayer.play();
//    }
//
//    /**
//     * Hàm này dùng cho fallback
//     */
//    private void startGame() {
//        startGame(nextGameMode);
//    }
//
//    private void startGame(GameMode initialMode) {
//        this.nextGameMode = initialMode;
//
//        GameSceneRoot gameSceneRoot = new GameSceneRoot(this::showNewMenu, nextGameMode);
//        stage.setScene(gameSceneRoot.getScene());
//        stage.setResizable(false);
//        stage.show();
//    }
//
//    private void showRanking() {
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("/leaderboard.fxml"));
//        Parent leaderboardRoot;
//        try {
//            leaderboardRoot = loader.load();
//        } catch (IOException ex) {
//            ex.printStackTrace();
//            returnToMenu();
//            return;
//        }
//
//        LeaderboardController controller = loader.getController();
//        controller.setSubtitle("Top 10 Online (Firebase)");
//        controller.setBackAction(() -> Platform.runLater(() -> {
//            returnToMenu();
//            SoundManager.getInstance().playMusic("menu.mp3");
//        }));
//
//        controller.setScores(new ArrayList<>());
//
//        FirebaseScoreService.getTopScores()
//                .thenAccept(scores -> {
//                    Platform.runLater(() -> {
//                        controller.setScores(scores);
//                    });
//                })
//                .exceptionally(e -> {
//                    Platform.runLater(() -> {
//                        AlertBox.display("Lỗi Mạng", "Không thể tải bảng xếp hạng Firebase.");
//                        returnToMenu();
//                    });
//                    return null;
//                });
//
//        Scene scene = stage.getScene();
//        scene.setRoot(leaderboardRoot);
//    }
//
//    private void returnToMenu() {
//        Scene scene = stage.getScene();
//        if (menuRoot != null) {
//            scene.setRoot(menuRoot);
//            if (menuRoot instanceof Pane pane) {
//                pane.requestFocus();
//            } else {
//                menuRoot.requestFocus();
//            }
//        } else {
//            showNewMenu();
//        }
//    }
//
//    public static void main(String[] args) {
//        ResourceManager resourceManager = ResourceManager.getInstance();
//        resourceManager.clearCache();
//        launch();
//    }
//}
// File: src/main/java/com/ooparkanoid/console/MainConsole.java
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

public class MainConsole extends Application {
    private Stage stage;

    // Intro video
    private MediaPlayer introMediaPlayer;

    // Intro input handlers
    private EventHandler<KeyEvent> introSpaceHandler;
    private EventHandler<MouseEvent> introMouseHandler;

    // Menu cache
    private Parent menuRoot;
    private MenuController menuController;

    private GameMode nextGameMode = GameMode.ADVENTURE;

    @Override
    public void start(Stage stage) throws IOException {
        this.stage = stage;
        stage.setTitle("Arkanoid - Simple Brick Game");
        stage.setResizable(false);

        // Luồng mới: Intro.fxml -> (SPACE/click) -> fadeToBlack -> transitionToLogin
        showIntroScreen();

        stage.show();
    }

    /* =========================
       1) INTRO SCREEN (FXML)
       ========================= */
    private void showIntroScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Intro.fxml"));
            Parent root = loader.load();

            Scene scene = (stage.getScene() == null)
                    ? new Scene(root, Constants.WIDTH, Constants.HEIGHT)
                    : stage.getScene();
            scene.setRoot(root);
            stage.setScene(scene);

            // Nhạc intro
            SoundManager.getInstance().playMusic("intro.mp3");

            // Hành động bỏ qua intro
            Runnable skipIntro = () -> {
                // Chặn double-trigger
                scene.setOnKeyPressed(null);
                if (introMouseHandler != null) {
                    scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, introMouseHandler);
                    introMouseHandler = null;
                }
                SoundManager.getInstance().play("selected");
                SoundManager.getInstance().stopMusic();

                // Mờ đen rồi mở rèm vào Login
                fadeToBlack(this::transitionToLogin);
            };

            // SPACE
            introSpaceHandler = e -> {
                if (e.getCode() == KeyCode.SPACE) skipIntro.run();
            };
            scene.setOnKeyPressed(introSpaceHandler);

            // CLICK trái
            introMouseHandler = e -> {
                if (e.getButton() == MouseButton.PRIMARY) skipIntro.run();
            };
            scene.addEventFilter(MouseEvent.MOUSE_PRESSED, introMouseHandler);

            root.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
            // Fallback
            showLoginScreen();
        }
    }

    /* =========================
       2) LOGIN / SIGNUP SCREENS
       ========================= */
    // Mở rèm vào Login (hiệu ứng)
    private void transitionToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent loginRoot = loader.load();
            LoginController controller = loader.getController();

            controller.setOnLoginSuccess(() -> {
                OnlinePresenceService.goOnline(PlayerContext.uid);
                // Khởi tạo video sớm để chọn menu phát được ngay
                preloadIntroVideo();
                // Sau login -> màn hình Loading (tải tài nguyên) -> rồi vào Menu
                showLoadingScreen();
            });

            controller.setOnGoToSignUp(this::showSignUpScreen);

            Scene scene = stage.getScene();

            Rectangle curtain = new Rectangle(Constants.WIDTH, Constants.HEIGHT, Color.BLACK);
            Scale curtainScale = new Scale(1, 1, Constants.WIDTH / 2.0, Constants.HEIGHT / 2.0);
            curtain.getTransforms().add(curtainScale);

            StackPane transitionPane = new StackPane(loginRoot, curtain);
            scene.setRoot(transitionPane);

            Timeline openCurtain = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(curtainScale.xProperty(), 1, Interpolator.EASE_IN)),
                    new KeyFrame(Duration.seconds(0.5), new KeyValue(curtainScale.xProperty(), 0, Interpolator.EASE_OUT))
            );
            openCurtain.setOnFinished(e -> {
                transitionPane.getChildren().remove(curtain);
                scene.setRoot(loginRoot);
                loginRoot.requestFocus();
            });
            openCurtain.play();
        } catch (IOException e) {
            e.printStackTrace();
            showLoginScreen();
        }
    }

    // Fallback không hiệu ứng
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

    /* =========================
       3) LOADING (ASSET TASK)
       ========================= */
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
                System.out.println("Tải tài nguyên đa luồng thành công");
                startTransition(); // vào Menu (đóng/mở rèm)
            });

            loadingTask.setOnFailed(e -> {
                System.err.println("Lỗi khi tải tài nguyên game:");
                loadingTask.getException().printStackTrace();
                AlertBox.display("Lỗi nghiêm trọng", "Không thể tải tài nguyên game. Vui lòng thử lại.");
                Platform.exit();
            });

            new Thread(loadingTask).start();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Không thể tải loading.fxml, vào thẳng menu...");
            startTransition();
        }
    }

    /* =========================
       4) TRANSITION -> MENU
       ========================= */
    // Đóng rèm từ màn hình hiện tại, load Menu, mở rèm
    private void startTransition() {
        SoundManager.getInstance().stopMusic();
        // tên file SFX: tuỳ bạn "transition" hoặc "transition.mp3"
        SoundManager.getInstance().play("transition");

        Scene scene = stage.getScene();

        // Dọn những handler intro (nếu còn)
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

        Rectangle curtain = new Rectangle(Constants.WIDTH, Constants.HEIGHT, Color.BLACK);
        Scale curtainScale = new Scale(0, 1, Constants.WIDTH / 2.0, Constants.HEIGHT / 2.0);
        curtain.getTransforms().add(curtainScale);

        StackPane transitionPane = new StackPane(currentRoot, curtain);
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
                startGame(); // Fallback
                return;
            }

            transitionPane.getChildren().set(0, menuContent);

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

    private void showNewMenu() {
        try {
            Parent menuContent = loadMenuRoot();
            stage.getScene().setRoot(menuContent);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("Không thể tải menu FXML mới. Bắt đầu game...");
            startGame();
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
                    // Giữ flow phát video intro trước khi vào game
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

    /* =========================
       5) FADE TO BLACK (CLEANUP)
       ========================= */
    private void fadeToBlack(Runnable onFinished) {
        Scene scene = stage.getScene();
        Parent currentRoot = scene.getRoot();

        final Canvas blackOverlay = new Canvas(Constants.WIDTH, Constants.HEIGHT);
        GraphicsContext gc = blackOverlay.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, Constants.WIDTH, Constants.HEIGHT);
        blackOverlay.setOpacity(0);

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

        javafx.animation.FadeTransition fadeBlack =
                new javafx.animation.FadeTransition(Duration.seconds(1), blackOverlay);
        fadeBlack.setFromValue(0);
        fadeBlack.setToValue(1);
        fadeBlack.setInterpolator(Interpolator.EASE_IN);

        fadeBlack.setOnFinished(e -> {
            if (onFinished != null) onFinished.run();

            // cleanup lớp che
            if (overlayContainer instanceof Pane pane) {
                pane.getChildren().remove(blackOverlay);
            } else if (overlayContainer instanceof Group group) {
                group.getChildren().remove(blackOverlay);
            }
            // gỡ wrapper nếu vẫn còn
            if (wrapped && scene.getRoot() == overlayContainer) {
                scene.setRoot(currentRoot);
            }
        });

        fadeBlack.play();
    }

    /* =========================
       6) INTRO VIDEO (PRELOAD/PLAY)
       ========================= */
    private void preloadIntroVideo() {
        try {
            String videoPath = "/Videos/intro.mp4"; // đảm bảo tồn tại trong resources
            URL videoUrl = getClass().getResource(videoPath);
            if (videoUrl == null) {
                System.err.println("Không tìm thấy video để preload: " + videoPath);
                return;
            }

            Media media = new Media(videoUrl.toExternalForm());
            introMediaPlayer = new MediaPlayer(media);
            introMediaPlayer.setAutoPlay(false);

            introMediaPlayer.setOnError(() -> {
                System.err.println("Lỗi preload video: " + introMediaPlayer.getError().getMessage());
                introMediaPlayer = null;
            });
        } catch (Exception e) {
            System.err.println("Lỗi khi khởi tạo media player: " + e.getMessage());
            introMediaPlayer = null;
        }
    }

    private void playIntroVideo() {
        if (introMediaPlayer == null) {
            System.err.println("Video player chưa sẵn sàng. Bỏ qua và vào game.");
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

        // Khi kết thúc, nạp lại và vào game
        introMediaPlayer.setOnEndOfMedia(() -> Platform.runLater(() -> {
            introMediaPlayer.stop();
            preloadIntroVideo();
            startGame(nextGameMode);
        }));

        // Cho phép bỏ qua video
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

    /* =========================
       7) GAME / RANKING / EXIT
       ========================= */
    private void startGame() {
        startGame(nextGameMode);
    }

    private void startGame(GameMode initialMode) {
        this.nextGameMode = initialMode;
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
        controller.setSubtitle("Top 10 Online (Firebase)");
        controller.setBackAction(() -> Platform.runLater(() -> {
            returnToMenu();
            SoundManager.getInstance().playMusic("menu.mp3");
        }));

        // placeholder
        controller.setScores(new ArrayList<>());

        FirebaseScoreService.getTopScores()
                .thenAccept(scores -> Platform.runLater(() -> controller.setScores(scores)))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        AlertBox.display("Lỗi Mạng", "Không thể tải bảng xếp hạng Firebase.");
                        returnToMenu();
                    });
                    return null;
                });

        stage.getScene().setRoot(leaderboardRoot);
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

    /* =========================
       8) LIFECYCLE
       ========================= */
    @Override
    public void stop() throws Exception {
        System.out.println("Game đang đóng... Báo danh offline.");
        SoundManager.getInstance().shutdown();

        if (PlayerContext.isLoggedIn()) {
            OnlinePresenceService.goOffline(PlayerContext.uid);
        }

        super.stop();
        Platform.exit();
        System.exit(0);
    }

    public static void main(String[] args) {
        ResourceManager resourceManager = ResourceManager.getInstance();
        resourceManager.clearCache();
        launch();
    }
}
