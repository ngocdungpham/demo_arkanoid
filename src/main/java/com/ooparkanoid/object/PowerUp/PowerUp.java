package com.ooparkanoid.object.PowerUp;

import com.ooparkanoid.graphics.Animation;
import com.ooparkanoid.graphics.ResourceManager;
import com.ooparkanoid.object.GameObject;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class PowerUp extends GameObject {
    private final PowerUpEffect effect;
    private final Color color;
    private final double duration;
    private boolean collected = false;
    private double fallSpeed = 100;

    private Image sprite;
    private double rotation = 0;

    private boolean showSpawnEffect = true;
    private double spawnEffectTimer = 0;
    private static final double SPAWN_EFFECT_DURATION = 0.3;

    public PowerUp(double x, double y, double w, double h,
                   PowerUpEffect effect, Color color, double duration) {
        super(x, y, w, h);
        this.effect = effect;
        this.color = color;
        this.duration = duration;
    }

    // Constructor với sprite
    public PowerUp(double x, double y, double w, double h,
                   PowerUpEffect effect, String spritePath, double duration) {
        super(x, y, w, h);
        this.effect = effect;
        this.color = Color.WHITE;
        this.duration = duration;
        loadGraphics(spritePath);
    }

    private void loadGraphics(String path) {
        ResourceManager rm = ResourceManager.getInstance();
        sprite = rm.loadImage(path);
    }
    public void update(double deltaTime) {
        if (collected) {
            return;
        }
        y += fallSpeed * deltaTime;
        rotation += 120 * deltaTime;
        if (rotation >= 360) {
            rotation -= 360;
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

    @Override
    public void render(GraphicsContext gc) {
        if (collected) {
            return;
        }

        gc.save();
        gc.translate(x + width / 2, y + height / 2);
        gc.rotate(rotation);

        if (sprite != null) {
            gc.drawImage(sprite, -width / 2, -height / 2, width, height);
        } else {
            gc.setFill(color);
            gc.fillOval(-width / 2, - height / 2, width, height);
            gc.setStroke(Color.WHITE);
            gc.strokeOval(-width / 2, -height / 2, width, height);
        }
        gc.restore();
    }
}
