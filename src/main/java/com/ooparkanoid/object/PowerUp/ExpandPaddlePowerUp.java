package com.ooparkanoid.object.PowerUp;

import com.ooparkanoid.core.engine.GameManager;
import com.ooparkanoid.object.Paddle;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class ExpandPaddlePowerUp extends PowerUp {
    private final double scaleFactor = 1.5;  // Hệ số tăng chiều dài
    private static ExpandPaddlePowerUp activeInstance = null; // Chỉ 1 hiệu ứng hoạt động
    private double originalWidth;
    private double remainingTime = 0;

    public ExpandPaddlePowerUp(double x, double y, double w, double h, double duration) {
        super(x, y, w, h, duration);
    }

    @Override
    public void applyEffect() {
        if (collected) return;
        collect();
        Paddle paddle = GameManager.getInstance().getPaddle();

        // Nếu chưa có hiệu ứng nào đang hoạt động
        if (activeInstance == null) {
            originalWidth = paddle.getWidth();
            paddle.setWidth(originalWidth * scaleFactor);
            remainingTime = duration;
            activeInstance = this;
            System.out.println("🌟 ExpandPaddle applied! Width = " + paddle.getWidth());
        } else {
            // Nếu đã có hiệu ứng đang chạy → reset lại thời gian
            activeInstance.remainingTime = duration;
        }
    }

    @Override
    public void update(double deltaTime) {
        super.update(deltaTime);

        // Chỉ giảm thời gian của instance đang active
        if (activeInstance == this && activeInstance.isActive()) {
            remainingTime -= deltaTime;
            if (remainingTime <= 0) {
                removeEffect();
            }
        }
    }

    @Override
    public void removeEffect() {
        if (activeInstance == this) {
            Paddle paddle = GameManager.getInstance().getPaddle();
            if (paddle != null) {
                paddle.setWidth(originalWidth);
                System.out.println("⏱️ ExpandPaddle expired! Width reset = " + originalWidth);
            }
            activeInstance = null;
        }
        active = false;
    }

    @Override
    public void render(GraphicsContext gc) {
        if (collected) return;
        gc.setFill(Color.GREEN);
        gc.fillRect(x, y, width, height);
    }

    public static double getRemainingTime() {
        return (activeInstance != null) ? activeInstance.remainingTime : 0;
    }

    public static void resetEffect() {
        activeInstance = null;
    }
}
