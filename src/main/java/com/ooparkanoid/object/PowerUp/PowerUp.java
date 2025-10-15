package com.ooparkanoid.object.PowerUp;
import com.ooparkanoid.object.GameObject;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class PowerUp extends GameObject {
    private final PowerUpEffect effect;
    private final Color color;
    private final double duration;
    private boolean collected = false;
    private double fallSpeed = 100;


    public PowerUp(double x, double y, double w, double h,
                   PowerUpEffect effect, Color color, double duration) {
        super(x, y, w, h);
        this.effect = effect;
        this.color = color;
        this.duration = duration;
    }

    @Override
    public void update(double deltaTime) {
        if (!collected) {
            y += fallSpeed * deltaTime;
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
        if (!collected) {
            // Vẽ nền
            gc.setFill(color);
            gc.fillRect(x, y, width, height);

            // Vẽ border trắng
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeRect(x, y, width, height);
        }
    }
}