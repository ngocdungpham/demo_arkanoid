// File: src/main/java/com/ooparkanoid/object/bricks/StrongBrick.java
package com.ooparkanoid.object.bricks;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class StrongBrick extends Brick {
    public StrongBrick(double x, double y) {
        super(x, y, 3, BrickType.STRONG); // <--- Cập nhật: Thêm BrickType.STRONG
    }

    @Override
    public void render(GraphicsContext gc) {
        if (!isDestroyed()) {
            // Màu sắc thay đổi tùy thuộc vào hitPoints còn lại
            if (hitPoints == 3) {
                gc.setFill(Color.DARKGRAY);
            } else if (hitPoints == 2) {
                gc.setFill(Color.GRAY);
            } else { // hitPoints == 1
                gc.setFill(Color.LIGHTGRAY);
            }
            gc.fillRect(x, y, width, height);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1);
            gc.strokeRect(x, y, width, height);
        }
    }
}
