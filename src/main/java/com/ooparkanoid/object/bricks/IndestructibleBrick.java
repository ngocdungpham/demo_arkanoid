// File: src/main/java/com/ooparkanoid/object/bricks/IndestructibleBrick.java
package com.ooparkanoid.object.bricks;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class IndestructibleBrick extends Brick {
    public IndestructibleBrick(double x, double y) {
        // hitPoints là -1 hoặc 1 (không quan trọng vì takeHit đã được override)
        super(x, y, -1, BrickType.INDESTRUCTIBLE);
    }

    @Override
    public void takeHit() {
        // Gạch này không bị phá hủy, nên takeHit không làm gì
        System.out.println("Indestructible brick hit!");
    }

    @Override
    public void render(GraphicsContext gc) {
        // Gạch này luôn được vẽ vì không bao giờ bị phá hủy
        gc.setFill(Color.DARKBLUE); // Màu xanh đậm cho gạch không thể phá hủy
        gc.fillRect(x, y, width, height);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(x, y, width, height);
    }
}
