// File: src/main/java/com/ooparkanoid/object/bricks/ExplosiveBrick.java
package com.ooparkanoid.object.bricks;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

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
            gc.setFill(EXPLOSIVE_COLOR); // Vẽ màu đỏ
            gc.fillRect(x, y, width, height);
            gc.setStroke(Color.ORANGE); // Viền cam để trông giống nổ hơn
            gc.setLineWidth(2);
            gc.strokeRect(x, y, width, height);
            // Có thể thêm một ký hiệu nhỏ như 'X' hoặc '*' ở giữa
            gc.setFill(Color.WHITE);
            gc.setFont(gc.getFont().font("Arial", javafx.scene.text.FontWeight.BOLD, 10));
            gc.fillText("X", x + width / 2 - 4, y + height / 2 + 4);
        }
    }
}
