package com.ooparkanoid.object.PowerUp;

import com.ooparkanoid.graphics.Animation;
import com.ooparkanoid.graphics.ResourceManager;
import com.ooparkanoid.object.GameObject;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class PowerUp extends GameObject {
    private final PowerUpEffect effect;
    private final PowerUpSprite.PowerUpType type;
    private final Color color;
    private final double duration;
    private boolean collected = false;
    private double fallSpeed = 100;

    private Animation animation;
    private boolean hasAnimation = false;

    // Constructor với sprite
    public PowerUp(double x, double y, double w, double h,
                   PowerUpEffect effect, PowerUpSprite.PowerUpType type, double duration) {
        super(x, y, w, h);
        this.effect = effect;
        this.color = Color.WHITE;
        this.type = type;
        this.duration = duration;
        loadAnimation(type);
    }

    private void loadAnimation(PowerUpSprite.PowerUpType type) {
        PowerUpSprite powerUpSprite = PowerUpSprite.getInstance();
        animation = powerUpSprite.getAnimationForType(type);

        if (animation != null) {
            hasAnimation = true;
        }
    }
    public void update(double deltaTime) {
        if (collected) {
            return;
        }
        y += fallSpeed * deltaTime;

        if (hasAnimation && animation != null) {
            animation.update(deltaTime);
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (collected) {
            return;
        }

        if (hasAnimation && animation != null) {
            Image frame = animation.getCurrentFrame();
            gc.drawImage(frame, x, y, width, height);
        } else {
            gc.setFill(color);
            gc.fillOval(-width / 2, - height / 2, width, height);
            gc.setStroke(Color.WHITE);
            gc.strokeOval(-width / 2, -height / 2, width, height);
        }
    }

    public void collect() {
        collected = true;
    }

    public boolean isCollected() {
        return collected;
    }

    public PowerUpEffect getEffect() {
        return effect;
    }

    public double getDuration() {
        return duration;
    }

    public PowerUpSprite.PowerUpType getType() {
        return type;
    }
}
