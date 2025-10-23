// File: src/main/java/com/ooparkanoid/object/Paddle.java
package com.ooparkanoid.object;

import com.ooparkanoid.graphics.Animation;
import com.ooparkanoid.graphics.ResourceManager;
import com.ooparkanoid.utils.Constants;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Paddle extends MovableObject {
    private Image paddleSprite;

    private boolean laserEnabled = false;
    private List<Laser> lasers = new ArrayList<>();
    private double shootCooldown = 0;
    private static final double SHOOT_DELAY = 0.3;

    public Paddle(double x, double y) {
        super(x, y, Constants.PADDLE_WIDTH, Constants.PADDLE_HEIGHT, 0, 0);
        loadGraphics();
    }

    public void loadGraphics() {
        ResourceManager rm = ResourceManager.getInstance();
        paddleSprite = rm.loadImage("paddle.png");
    }

    // Thêm các phương thức setter cho dx và dy để có thể điều khiển tốc độ từ bên ngoài
    public void setDx(double dx) {
        this.dx = dx;
    }

    public void setDy(double dy) {
        this.dy = dy;
    }

    @Override
    public void update(double dt) {
        // Cập nhật vị trí dựa trên vận tốc và thời gian
        move(dt);

        if (shootCooldown > 0) {
            shootCooldown -= dt;
        }

        Iterator<Laser> it = lasers.iterator();
        while (it.hasNext()) {
            Laser laser = it.next();
            laser.update(dt);
            if (!laser.isActive()) {
                it.remove();
            }
        }

        // Giới hạn thanh đỡ trong màn hình
//        if (x < 0) {
//            x = 0;
        if (x < Constants.PLAYFIELD_LEFT) {
            x = Constants.PLAYFIELD_LEFT;
        }
//        if (x + width > Constants.WIDTH) {
//            x = Constants.WIDTH - width;
        if (x + width > Constants.PLAYFIELD_RIGHT) {
            x = Constants.PLAYFIELD_RIGHT - width;
        }
    }

    public void shootLaser() {
        if (!laserEnabled || shootCooldown > 0) {
            return;
        }
        double laserSpeed = 500;

        Laser leftLaser = new Laser(x + width * 0.25, y - 25, laserSpeed);
        Laser rightLaser = new Laser(x + width * 0.75, y - 25, laserSpeed);
        lasers.add(leftLaser);
        lasers.add(rightLaser);
        shootCooldown = SHOOT_DELAY;
    }

    @Override
    public void render(GraphicsContext gc) {
        if (paddleSprite != null) {
            gc.drawImage(paddleSprite, x, y, width, height);
        } else {
            gc.setFill(Color.WHITE);
            gc.fillRect(x, y, width, height);
        }

        if (laserEnabled) {
            rederLaserPaddle(gc);
        }
        for (Laser laser : lasers) {
            laser.render(gc);
        }
    }

    private void rederLaserPaddle(GraphicsContext gc) {
        gc.setFill(Color.DARKGRAY);
        gc.fillRect(x + width * 0.25 - 3, y - 10, 6, 10);
        gc.fillRect(x + width * 0.75 - 3, y - 10, 6, 10);

        // Glow effect
        if (shootCooldown <= 0) {
            gc.setGlobalAlpha(0.5);
            gc.setFill(Color.CYAN);
            gc.fillRect(x + width * 0.25 - 4, y - 11, 8, 12);
            gc.fillRect(x + width * 0.75 - 4, y - 11, 8, 12);
            gc.setGlobalAlpha(1.0);
        }
    }

    public void setLaserEnabled(boolean enabled) {
        this.laserEnabled = enabled;
        if (!enabled) {
            lasers.clear();
        }
    }

    public boolean isLaserEnabled() {
        return laserEnabled;
    }

    public List<Laser> getLasers() {
        return lasers;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public void setWidth(double width) {
        this.width = width;
    }
}