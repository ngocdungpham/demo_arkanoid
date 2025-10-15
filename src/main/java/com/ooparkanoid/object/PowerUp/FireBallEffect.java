package com.ooparkanoid.object.PowerUp;

public class FireBallEffect implements PowerUpEffect {

    @Override
    public void apply(GameContext context) {
        // Đánh dấu rằng FireBall đang active
        // GameManager sẽ check effect này để cho ball xuyên gạch;
        context.setFireBallActive(true);
        System.out.println("🔥 FireBall activated!");
    }

    @Override
    public void remove(GameContext context) {
        context.setFireBallActive(true);
        System.out.println("🔥 FireBall activated!");
    }

    @Override
    public String getEffectType() {
        return "FIRE_BALL";
    }
}
