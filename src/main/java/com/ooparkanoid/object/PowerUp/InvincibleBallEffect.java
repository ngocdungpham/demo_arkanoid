package com.ooparkanoid.object.PowerUp;

public class InvincibleBallEffect implements PowerUpEffect {

    @Override
    public void apply(GameContext context) {
        System.out.println("🛡️ Invincible Ball activated! Ball won't die!");
    }

    @Override
    public void remove(GameContext context) {
        System.out.println("🛡️ Invincible Ball expired!");
    }

    @Override
    public String getEffectType() {
        return "INVINCIBLE_BALL";
    }
}
