// File: src/main/java/com/ooparkanoid/object/bricks/FlickerBrick.java
package com.ooparkanoid.object.bricks;

import com.ooparkanoid.utils.Constants;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.Random;

public class FlickerBrick extends Brick {

    private static final Random random = new Random();

    private double flickerTimer;
    private boolean visible;

    // Khoảng thời gian nhấp nháy ngẫu nhiên
    private final double MIN_DURATION = 1.5; // Tối thiểu 1 giây
    private final double MAX_DURATION = 5.0; // Tối đa 3 giây

    private double currentVisibleDuration; // Thời gian hiển thị hiện tại cho viên gạch này
    private double currentHiddenDuration;  // Thời gian ẩn hiện tại cho viên gạch này

    private final Color FLICKER_COLOR = Color.rgb(255, 165, 0); // Màu cam

    public FlickerBrick(double x, double y) {
        super(x, y, 1, BrickType.FLICKER);

        // Tạo thời gian hiển thị/ẩn ngẫu nhiên khi khởi tạo
        this.currentVisibleDuration = MIN_DURATION + (MAX_DURATION - MIN_DURATION) * random.nextDouble();
        this.currentHiddenDuration = MIN_DURATION + (MAX_DURATION - MIN_DURATION) * random.nextDouble();

        // Bắt đầu ở trạng thái hiển thị với thời gian ngẫu nhiên đã tạo
        this.flickerTimer = currentVisibleDuration;
        this.visible = true;
    }

    @Override
    public void update(double dt) {
        flickerTimer -= dt;

        if (flickerTimer <= 0) {
            visible = !visible; // Đảo ngược trạng thái hiển thị
            if (visible) {
                // Khi trở lại hiển thị, tạo thời gian hiển thị ngẫu nhiên mới
                currentVisibleDuration = MIN_DURATION + (MAX_DURATION - MIN_DURATION) * random.nextDouble();
                flickerTimer = currentVisibleDuration;
            } else {
                // Khi trở lại ẩn, tạo thời gian ẩn ngẫu nhiên mới
                currentHiddenDuration = MIN_DURATION + (MAX_DURATION - MIN_DURATION) * random.nextDouble();
                flickerTimer = currentHiddenDuration;
            }
        }
    }

    @Override
    public void takeHit() {
        if (visible && !isDestroyed()) {
            super.takeHit();
            if (isDestroyed()) {
                System.out.println("Flicker Brick destroyed!");
            }
        } else {
            System.out.println("Flicker Brick hit while hidden!");
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (!isDestroyed() && visible) {
            gc.setFill(FLICKER_COLOR);
            gc.fillRect(x, y, width, height);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeRect(x, y, width, height);
        }
    }
}
