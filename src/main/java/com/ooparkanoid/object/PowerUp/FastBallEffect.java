package com.ooparkanoid.object.PowerUp;

import com.ooparkanoid.object.Ball;
import com.ooparkanoid.utils.Constants;
import java.util.HashMap;
import java.util.Map;

public class FastBallEffect implements PowerUpEffect {
    private final double speedMultiplier;
    private final Map<Ball, Double> originalSpeeds = new HashMap<>();

    public FastBallEffect(double speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    @Override
    public void apply(GameContext context) {
        originalSpeeds.clear();
        for (Ball ball : context.getBalls()) {
            // Lưu tốc độ gốc (chỉ lần đầu)
            if (!originalSpeeds.containsKey(ball)) {
                originalSpeeds.put(ball, ball.getSpeed());
            }
            // Set tốc độ mới dựa trên tốc độ GỐC (không phải tốc độ hiện tại)
            ball.setSpeed(originalSpeeds.get(ball) * speedMultiplier);
        }
        System.out.println("⚡ FastBall effect applied!");
    }

    @Override
    public void remove(GameContext context) {
        for (Ball ball : context.getBalls()) {
            Double originalSpeed = originalSpeeds.get(ball);
            if (originalSpeed != null) {
                ball.setSpeed(originalSpeed);
            } else {
                ball.setSpeed(Constants.DEFAULT_SPEED);
            }
        }
        originalSpeeds.clear();
        System.out.println("⚡ FastBall effect removed!");
    }

    @Override
    public String getEffectType() {
        return "FAST_BALL";
    }
}