// File: src/main/java/com/ooparkanoid/object/bricks/NormalBrick.java
package com.ooparkanoid.object.bricks;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class NormalBrick extends Brick {
    public NormalBrick(double x, double y) {
        super(x, y, 1, BrickType.NORMAL); // <--- Cập nhật: Thêm BrickType.NORMAL
    }

    @Override
    public void render(GraphicsContext gc) {
        if (!isDestroyed()) {
            gc.setFill(Color.BLUE);
            gc.fillRect(x, y, width, height);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1);
            gc.strokeRect(x, y, width, height);
        }
    }
}
