package com.ooparkanoid.object.PowerUp;

import com.ooparkanoid.object.Ball;

public class FireBallEffect implements PowerUpEffect {

    @Override
    public void apply(GameContext context) {
        // Đánh dấu rằng FireBall đang active
        // GameManager sẽ check effect này để cho ball xuyên gạch;
        for (Ball ball : context.getBalls()) {
            ball.activateFireBallEffect();
        }
    }

    @Override
    public void remove(GameContext context) {
        for (Ball ball : context.getBalls()) {
            ball.resetTrailEffect();
        }
    }

    @Override
    public String getEffectType() {
        return "FIRE_BALL";
    }
}
