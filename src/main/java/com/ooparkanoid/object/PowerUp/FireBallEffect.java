package com.ooparkanoid.object.PowerUp;

public class FireBallEffect implements PowerUpEffect {

    @Override
    public void apply(GameContext context) {
        // Đánh dấu rằng FireBall đang active
        // GameManager sẽ check effect này để cho ball xuyên gạch;
    }

    @Override
    public void remove(GameContext context) {
    }

    @Override
    public String getEffectType() {
        return "FIRE_BALL";
    }
}
