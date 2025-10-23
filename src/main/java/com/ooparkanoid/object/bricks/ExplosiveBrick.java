// File: src/main/java/com/ooparkanoid/object/bricks/ExplosiveBrick.java
package com.ooparkanoid.object.bricks;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;

public class ExplosiveBrick extends Brick {

    private final Color EXPLOSIVE_COLOR = Color.RED; // Màu đỏ cho gạch nổ

    public ExplosiveBrick(double x, double y) {
        super(x, y, 1, BrickType.EXPLOSIVE); // Gạch nổ thường chỉ cần 1 hit để phá hủy
    }

    @Override
    public void takeHit() {
        if (!isDestroyed()) {
            super.takeHit(); // Giảm hitPoints và đánh dấu destroyed nếu <= 0
            if (isDestroyed()) {
                // Khi ExplosiveBrick bị phá hủy, nó sẽ kích hoạt hiệu ứng nổ.
                // GameManager sẽ chịu trách nhiệm xử lý hiệu ứng nổ.
                System.out.println("Explosive Brick destroyed! Initiating explosion...");
                // (Không làm gì ở đây, GameManager sẽ kiểm tra isDestroyed() và xử lý)
            }
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (!isDestroyed()) {
            if (texture != null) { // <<<< SỬ DỤNG TEXTURE NẾU CÓ <<<<
                gc.drawImage(texture, x, y, width, height);
            } else { // Fallback (có thể xóa)
                gc.setFill(EXPLOSIVE_COLOR); // Vẽ màu đỏ
                gc.fillRect(x, y, width, height);
            }

        }
    }
}
