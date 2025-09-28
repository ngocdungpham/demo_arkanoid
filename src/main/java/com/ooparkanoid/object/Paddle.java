package com.ooparkanoid.object;

import com.ooparkanoid.object.MovableObject;
//import com.ooparkanoid.entities.powerups.PowerUp;
import com.ooparkanoid.utils.Constants;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Paddle extends MovableObject {
    private double speed;
//        private PowerUp currentPowerUp;

    public Paddle(double x, double y, double speed, double dx, double dy) {
        super(x, y, Constants.PADDLE_WIDTH, Constants.PADDLE_HEIGHT, dx, dy);
        this.speed = speed;
    }

    public void moveRight(double dt) {
        dx -= speed;
        move(dt);
        dx = 0;
        clamp();
    }

    private void clamp() {
        if (x < 0) {
            x = 0;
        }
        if (x + width > Constants.WIDTH) x = Constants.WIDTH - width;
    }
    @Override public void update(double dt) {}
    @Override public void render(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.fillRect(x, y, width, height);
    }
}
