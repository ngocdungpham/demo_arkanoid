package com.ooparkanoid.object.PowerUp;

import com.ooparkanoid.object.Paddle;

public class ExpandPaddleEffect implements PowerUpEffect {
    private final double scaleFactor;
    private double originalWidth = -1;

    public ExpandPaddleEffect(double scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    @Override
    public void apply(GameContext context) {
        Paddle paddle = context.getPaddle();
        // Chỉ lưu original width lần đầu
        if (originalWidth < 0) {
            originalWidth = paddle.getWidth();
        }
        // Set width dựa trên original width
        paddle.setWidth(originalWidth * scaleFactor);
    }

    @Override
    public void remove(GameContext context) {
        Paddle paddle = context.getPaddle();
        if (paddle != null && originalWidth > 0) {
            paddle.setWidth(originalWidth);
        }
        originalWidth = -1;
    }

    @Override
    public String getEffectType() {
        return "EXPAND_PADDLE";
    }
}