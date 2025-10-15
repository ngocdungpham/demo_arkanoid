package com.ooparkanoid.object.PowerUp;

public class ScoreMultiplierEffect implements PowerUpEffect {
    private final double multiplier;

    public ScoreMultiplierEffect(double multiplier) {
        this.multiplier = multiplier; // e.g., 2.0 = x2 điểm
    }

    @Override
    public void apply(GameContext context) {
        System.out.println("💰 Score Multiplier x" + multiplier + " activated!");
    }

    @Override
    public void remove(GameContext context) {
        System.out.println("💰 Score Multiplier expired!");
    }

    @Override
    public String getEffectType() {
        return "SCORE_MULTIPLIER";
    }

    public double getMultiplier() {
        return multiplier;
    }
}