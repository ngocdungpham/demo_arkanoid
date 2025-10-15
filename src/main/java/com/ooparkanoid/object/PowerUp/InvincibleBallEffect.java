package com.ooparkanoid.object.PowerUp;

public class InvincibleBallEffect implements PowerUpEffect {

    @Override
    public void apply(GameContext context) {
        context.setInvincibleActive(true);
        System.out.println("🛡️ Invincible Ball activated!");
    }

    @Override
    public void remove(GameContext context) {
        context.setInvincibleActive(false);
        System.out.println("🛡️ Invincible Ball expired!");
    }

    @Override
    public String getEffectType() {
        return "INVINCIBLE_BALL";
    }
}
