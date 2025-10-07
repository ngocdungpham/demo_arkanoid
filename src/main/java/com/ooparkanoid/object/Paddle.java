// File: src/main/java/com/ooparkanoid/object/Paddle.java
package com.ooparkanoid.object;

import com.ooparkanoid.utils.Constants;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Paddle extends MovableObject {
    public Paddle(double x, double y) {
        super(x, y, Constants.PADDLE_WIDTH, Constants.PADDLE_HEIGHT, 0, 0);
    }

    // Thêm các phương thức setter cho dx và dy để có thể điều khiển tốc độ từ bên ngoài
    public void setDx(double dx) {
        this.dx = dx;
    }

    public void setDy(double dy) {
        this.dy = dy;
    }

    @Override
    public void update(double dt) {
        // Cập nhật vị trí dựa trên vận tốc và thời gian
        x += dx * dt;

        // Giới hạn thanh đỡ trong màn hình
        if (x < 0) {
            x = 0;
        }
        if (x + width > Constants.WIDTH) {
            x = Constants.WIDTH - width;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.fillRect(x, y, width, height);
    }

    public void setHeight(double height) {
        this.height = height;
    }
    public void setWidth(double width) {
        this.width = width;
    }
}