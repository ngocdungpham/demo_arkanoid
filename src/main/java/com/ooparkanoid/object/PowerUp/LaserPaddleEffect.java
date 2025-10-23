package com.ooparkanoid.object.PowerUp;

import com.ooparkanoid.object.Paddle;

public class LaserPaddleEffect implements PowerUpEffect{
    @Override
    public void apply(GameContext context) {
        Paddle paddle = context.getPaddle();
        paddle.setLaserEnabled(true);
    }
    @Override
    public void remove(GameContext context) {
        Paddle paddle = context.getPaddle();
        paddle.setLaserEnabled(false);
    }
    @Override
    public String getEffectType() {
        return "LASER_PADDLE";
    }
}
