package com.ooparkanoid.object.PowerUp;

public interface PowerUpEffect {
    void apply(GameContext context);

    void remove(GameContext context);

    String getEffectType();         // Dùng làm key
}