package com.ooparkanoid.ui;

import com.ooparkanoid.utils.Constants;
import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * Hiển thị màn hình Game Over nhỏ gọn với hiệu ứng 2 mảnh ghép lại theo chiều ngang (dải giữa)
 * và tự động quay về menu sau 2s. Phần còn lại trong suốt.
 */
public class GameOverView {

    private final StackPane root;
    private final Pane container; // Container giới hạn chiều cao cho dải ngang
    private final Pane leftHalf;
    private final Pane rightHalf;
    private final Label title;
    private final Callbacks callbacks;
    private Animation currentAnimation;

    // --- Cấu hình dải ngang ---
    private static final double BAND_HEIGHT = 120.0; // Chiều cao của dải Game Over
    private static final double HALF_WIDTH = Constants.WIDTH / 2.0; // Rộng của mỗi mảnh ghép
    private static final Color BAND_COLOR = Color.web("#880000").deriveColor(0, 1, 1, 0.5); // Đỏ đậm mờ

    public interface Callbacks {
        void onExit();
    }

    public GameOverView(Callbacks cb) {
        this.callbacks = cb;

        // 1. Root overlay: TRONG SUỐT HOÀN TOÀN
        root = new StackPane();
        root.setPrefSize(Constants.WIDTH, Constants.HEIGHT);
        root.setVisible(false);
        root.setMouseTransparent(true);
        root.setStyle("-fx-background-color: transparent;");

        // 2. Container: Đặt ở giữa màn hình, giới hạn chiều cao, chứa 2 mảnh ghép
        container = new Pane();
        container.setPrefSize(Constants.WIDTH, BAND_HEIGHT);
        container.setMaxSize(Constants.WIDTH, BAND_HEIGHT);
        // Clip là cần thiết để ẩn phần bị TranslateX (ví dụ: khi leftHalf ở -HALF_WIDTH)
        container.setClip(new javafx.scene.shape.Rectangle(Constants.WIDTH, BAND_HEIGHT));

        // 3. Title label
        title = new Label("GAME OVER");
        title.setFont(Font.font("Orbitron", FontWeight.EXTRA_BOLD, 80));
        title.setTextFill(Color.WHITE);
        title.setEffect(new DropShadow(30, Color.web("#FF0000")));
        title.setOpacity(0.0);
        title.setStyle("-fx-font-family: 'Orbitron', 'Segoe UI', Arial;");
        title.setViewOrder(-1);
        StackPane.setAlignment(title, Pos.CENTER);

        // 4. Left Half Pane (Mảnh ghép 1)
        leftHalf = createHalfPane(BAND_COLOR, HALF_WIDTH, BAND_HEIGHT);
        leftHalf.setTranslateX(-HALF_WIDTH); // Bắt đầu ở ngoài bên trái
        leftHalf.setLayoutX(0); // <--- QUAN TRỌNG: Đặt vị trí ban đầu là 0

        // 5. Right Half Pane (Mảnh ghép 2)
        rightHalf = createHalfPane(BAND_COLOR, HALF_WIDTH, BAND_HEIGHT);
        rightHalf.setTranslateX(HALF_WIDTH); // Bắt đầu ở ngoài bên phải
        rightHalf.setLayoutX(HALF_WIDTH); // <--- QUAN TRỌNG: Đặt vị trí ban đầu là HALF_WIDTH

        // Thêm các mảnh ghép vào Container
        container.getChildren().addAll(leftHalf, rightHalf);

        // Thêm Container và Title vào Root
        root.getChildren().addAll(container, title);
        StackPane.setAlignment(container, Pos.CENTER);
    }

    // Hàm tạo Pane với kích thước và màu sắc cụ thể
    private Pane createHalfPane(Color color, double width, double height) {
        Pane pane = new Pane();
        pane.setPrefSize(width, height);
        pane.setMaxSize(width, height);
        pane.setStyle("-fx-background-color: " + color.toString().replace("0x", "#") + ";");
        return pane;
    }

    public void show() {
        if (root.isVisible()) return;

        if (currentAnimation != null) currentAnimation.stop();

        // 1. Đặt lại trạng thái ban đầu
        root.setVisible(true);
        // Thiết lập vị trí TranslateX ban đầu
        leftHalf.setTranslateX(-HALF_WIDTH);
        rightHalf.setTranslateX(HALF_WIDTH);
        title.setOpacity(0.0);

        // 2. Phase 1: Slide In (0.5s)
        Duration slideInDuration = Duration.millis(500);

        Timeline slideIn = new Timeline(
                // Dịch chuyển leftHalf từ -HALF_WIDTH về 0
                new KeyFrame(slideInDuration,
                        new KeyValue(leftHalf.translateXProperty(), 0, Interpolator.EASE_BOTH)),
                // Dịch chuyển rightHalf từ HALF_WIDTH về 0
                new KeyFrame(slideInDuration,
                        new KeyValue(rightHalf.translateXProperty(), 0, Interpolator.EASE_BOTH))
        );

        // 3. Phase 2: Show Text, Wait, Slide Out & Exit
        slideIn.setOnFinished(e -> {
            // A. Show Text (0.3s)
            FadeTransition fadeInText = new FadeTransition(Duration.millis(300), title);
            fadeInText.setToValue(1.0);

            // B. Wait (2.0s)
            PauseTransition delay = new PauseTransition(Duration.seconds(2.0));
            delay.setOnFinished(ev -> hideAndExit());

            SequentialTransition transition = new SequentialTransition(fadeInText, delay);
            transition.play();
        });

        currentAnimation = slideIn;
        currentAnimation.play();
    }

    private void hideAndExit() {
        // 1. Fade out text first (0.3s)
        FadeTransition fadeOutText = new FadeTransition(Duration.millis(300), title);
        fadeOutText.setToValue(0.0);

        // 2. Phase 3: Slide Out (0.5s)
        Duration slideOutDuration = Duration.millis(500);

        Timeline slideOut = new Timeline(
                // Dịch chuyển leftHalf từ 0 về -HALF_WIDTH (trái)
                new KeyFrame(slideOutDuration,
                        new KeyValue(leftHalf.translateXProperty(), -HALF_WIDTH, Interpolator.EASE_BOTH)),
                // Dịch chuyển rightHalf từ 0 về HALF_WIDTH (phải)
                new KeyFrame(slideOutDuration,
                        new KeyValue(rightHalf.translateXProperty(), HALF_WIDTH, Interpolator.EASE_BOTH))
        );

        slideOut.setOnFinished(e -> {
            root.setVisible(false);
            if (callbacks != null) {
                callbacks.onExit();
            }
        });

        // Chơi tuần tự: fadeOutText -> slideOut
        SequentialTransition transition = new SequentialTransition(fadeOutText, slideOut);
        currentAnimation = transition;
        currentAnimation.play();
    }

    public Node getView() { return root; }
}