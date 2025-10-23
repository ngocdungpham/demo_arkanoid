package com.ooparkanoid.object.PowerUp;

import com.ooparkanoid.object.Ball;
import com.ooparkanoid.utils.Constants;
import java.util.HashMap;
import java.util.Map;

public class SlowBallEffect implements PowerUpEffect {
    private final double speedMultiplier;
    private final Map<Ball, Double> originalSpeeds = new HashMap<>();

    public SlowBallEffect(double speedMultiplier) {
        this.speedMultiplier = speedMultiplier; // e.g., 0.6 = giảm 40% tốc độ
    }

    @Override
    public void apply(GameContext context) {
        originalSpeeds.clear();
        for (Ball ball : context.getBalls()) {
            if (!originalSpeeds.containsKey(ball)) {
                originalSpeeds.put(ball, ball.getSpeed());
            }
            ball.setSpeed(originalSpeeds.get(ball) * speedMultiplier);
            ball.activateSlowBallEffect();
        }
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
            ball.resetTrainEffect();
        }
        originalSpeeds.clear();
    }

    @Override
    public String getEffectType() {
        return "SLOW_BALL";
    }
}
