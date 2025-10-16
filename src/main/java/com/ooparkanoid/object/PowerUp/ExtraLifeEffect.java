package com.ooparkanoid.object.PowerUp;

public class ExtraLifeEffect implements PowerUpEffect {
    private final int livesToAdd;

    public ExtraLifeEffect(int livesToAdd) {
        this.livesToAdd = livesToAdd;
    }

    @Override
    public void apply(GameContext context) {
        context.addLives(livesToAdd);
    }

    @Override
    public void remove(GameContext context) {
        // Instant effect, không cần remove
    }

    @Override
    public String getEffectType() {
        return "EXTRA_LIFE";
    }
}