package com.ooparkanoid.object.PowerUp;

import com.ooparkanoid.object.Paddle;

public class ShrinkPaddleEffect implements PowerUpEffect {
    private final double scaleFactor;
    private double originalWidth = -1;

    public ShrinkPaddleEffect(double scaleFactor) {
        this.scaleFactor = scaleFactor; // e.g., 0.6 = giảm 40% chiều rộng
    }

    @Override
    public void apply(GameContext context) {
        Paddle paddle = context.getPaddle();
        if (originalWidth < 0) {
            originalWidth = paddle.getWidth();
        }
        paddle.setWidth(originalWidth * scaleFactor);
        System.out.println("→← ShrinkPaddle effect applied!");
    }

    @Override
    public void remove(GameContext context) {
        Paddle paddle = context.getPaddle();
        if (paddle != null && originalWidth > 0) {
            paddle.setWidth(originalWidth);
        }
        originalWidth = -1;
        System.out.println("→← ShrinkPaddle effect removed!");
    }

    @Override
    public String getEffectType() {
        return "SHRINK_PADDLE";
    }
}
