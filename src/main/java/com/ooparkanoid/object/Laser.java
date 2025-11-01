package com.ooparkanoid.object;

import com.ooparkanoid.graphics.ResourceManager;
import com.ooparkanoid.utils.Constants;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Laser extends MovableObject {
    private boolean active = true;
    private Image sprite;

    public Laser(double x, double y, double speed) {
        super(x, y, Constants.LASER_WIDTH, Constants.LASER_HEIGHT, 0, -speed);
        loadGraphics();
    }

    private void loadGraphics() {
        ResourceManager rm = ResourceManager.getInstance();
        sprite = rm.loadImage("laser.png");
    }

    @Override
    public void update(double deltaTime) {
        move(deltaTime);

        if (y + height < 0) {
            active = false;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (!active) {
            return;
        }

        if (sprite != null) {
            gc.drawImage(sprite, x, y, width, height);
        } else {
            gc.setGlobalAlpha(1.0);
            gc.setFill(Color.WHITE);
            gc.fillRect(x, y, width, height);
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setActive() {
        active = false;
    }
}
