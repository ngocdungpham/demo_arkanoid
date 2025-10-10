package com.ooparkanoid.object.PowerUp;

import com.ooparkanoid.core.engine.GameManager;
import com.ooparkanoid.object.Ball;
import com.ooparkanoid.utils.Constants;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.List;

public class FastBallPowerUp extends PowerUp {
    private final double speedMultiplier = 1.5;
    // Chỉ có 1 hiệu ứng FastBall đang hoạt động tại 1 thời điểm
    private static FastBallPowerUp activeInstance = null;
    private double remainingTime = 0;
    public FastBallPowerUp(double x, double y, double w, double h, double duration) {
        super(x, y, w, h, duration);
    }

    @Override
    public void applyEffect() {
        if (collected) return;
        collect();
        if (activeInstance == null) {
            // Áp dụng cho tất cả ball
            List<Ball> balls = GameManager.getInstance().getBalls();
            for (Ball b : balls) {
                b.setSpeed(b.getSpeed() * speedMultiplier);
            }
            remainingTime = duration;
            activeInstance = this;
        } else {
            // Nếu buff đã có → reset thời gian
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
            List<Ball> balls = GameManager.getInstance().getBalls();
            for (Ball b : balls) {
                b.setSpeed(Constants.DEFAULT_SPEED);
            }

            System.out.println("FastBall expired -> speed reset");
            activeInstance = null;
        }
        active = false;
    }

    @Override
    public void render(GraphicsContext gc) {
        if (collected) return;
        gc.setFill(Color.RED);
        gc.fillRect(x, y, width, height);
    }

    public static double getRemainingTime() {
        return (activeInstance != null) ? activeInstance.remainingTime : 0;
    }
    public static void resetEffect() {
        activeInstance = null;
    }
}
