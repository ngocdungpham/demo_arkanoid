package com.ooparkanoid.object.PowerUp;

public class FireBallEffect implements PowerUpEffect {

    @Override
    public void apply(GameContext context) {
        // Đánh dấu rằng FireBall đang active
        // GameManager sẽ check effect này để cho ball xuyên gạch
        System.out.println("🔥 FireBall activated! Ball pierces through bricks!");
    }

    @Override
    public void remove(GameContext context) {
        System.out.println("🔥 FireBall deactivated!");
    }

    @Override
    public String getEffectType() {
        return "FIRE_BALL";
    }
}
