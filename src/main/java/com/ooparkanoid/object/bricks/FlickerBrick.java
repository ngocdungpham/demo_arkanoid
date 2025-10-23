// File: src/main/java/com/ooparkanoid/object/bricks/FlickerBrick.java
package com.ooparkanoid.object.bricks;

import com.ooparkanoid.utils.Constants;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.Random;
import javafx.scene.image.Image;
import com.ooparkanoid.graphics.ResourceManager; // Đảm bảo import này

public class FlickerBrick extends Brick {

    private static final Random random = new Random();

    private double flickerTimer;
    private boolean visible; // Trạng thái hiển thị/ẩn

    // Khoảng thời gian nhấp nháy ngẫu nhiên
    private final double MIN_DURATION = 1.0; // Tối thiểu 1 giây
    private final double MAX_DURATION = 3.0; // Tối đa 3 giây (điều chỉnh cho phù hợp)

    private double currentVisibleDuration;
    private double currentHiddenDuration;

    private static Image flickerTexture1; // Texture khi gạch visible (hoặc trạng thái 1)
    private static Image flickerTexture2; // Texture khi gạch hidden (hoặc trạng thái 2)

    static {
        ResourceManager rm = ResourceManager.getInstance();
        flickerTexture1 = rm.loadImage("brick_flicker1.png"); // Ảnh cho trạng thái hiển thị
        flickerTexture2 = rm.loadImage("brick_flicker2.png"); // Ảnh cho trạng thái ẩn
    }

    private final Color FLICKER_COLOR = Color.rgb(255, 165, 0); // Màu cam (chỉ dùng cho fallback)

    public FlickerBrick(double x, double y) {
        super(x, y, 1, BrickType.FLICKER);

        this.currentVisibleDuration = MIN_DURATION + (MAX_DURATION - MIN_DURATION) * random.nextDouble();
        this.currentHiddenDuration = MIN_DURATION + (MAX_DURATION - MIN_DURATION) * random.nextDouble();

        this.flickerTimer = currentVisibleDuration;
        this.visible = true; // Bắt đầu ở trạng thái hiển thị

        // Gán texture ban đầu dựa trên trạng thái visible
        setTexture(visible ? flickerTexture1 : flickerTexture2);
    }

    @Override
    public void update(double dt) {
        flickerTimer -= dt;

        if (flickerTimer <= 0) {
            visible = !visible; // Đảo ngược trạng thái hiển thị

            // <<<< DI CHUYỂN DÒNG NÀY RA ĐÂY >>>>
            setTexture(visible ? flickerTexture1 : flickerTexture2);

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
        if (visible && !isDestroyed()) { // Vẫn chỉ nhận hit khi visible
            super.takeHit();
            if (isDestroyed()) {
                System.out.println("Flicker Brick destroyed!");
            }
        } else {
            System.out.println("Flicker Brick hit while hidden (showing texture2)!");
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        // <<<< XÓA ĐIỀU KIỆN `&& visible` Ở ĐÂY >>>>
        if (!isDestroyed()) { // Chỉ vẽ nếu chưa bị phá hủy
            if (texture != null) {
                gc.drawImage(texture, x, y, width, height);
            } else { // Fallback (có thể xóa)
                gc.setFill(FLICKER_COLOR);
                gc.fillRect(x, y, width, height);
            }
        }
    }
}
