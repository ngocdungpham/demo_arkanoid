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
    public enum Orientation {
        HORIZONTAL,
        VERTICAL_LEFT,
        VERTICAL_RIGHT
    }

    private Image paddleSprite;
    private Image laserGunSprite;
    private Image paddleSpriteVerticalLeft; // paddle left side
    private Image paddleSpriteVerticalRight; // paddle right side

    private Orientation orientation = Orientation.HORIZONTAL;
    private Double lockedX = null;

    private boolean laserEnabled = false;
    private List<Laser> lasers = new ArrayList<>();
    private double shootCooldown = 0;
    private static final double SHOOT_DELAY = 0.3;

    private double boundLeft = Constants.PLAYFIELD_LEFT;
    private double boundRight = Constants.PLAYFIELD_RIGHT;
    private double boundTop = 0;
    private double boundBottom = Constants.HEIGHT;

    public Paddle(double x, double y) {
        super(x, y, Constants.PADDLE_WIDTH, Constants.PADDLE_HEIGHT, 0, 0);
        loadGraphics();
    }

    public void loadGraphics() {
        ResourceManager rm = ResourceManager.getInstance();
        paddleSprite = rm.loadImage("paddle1.png");
        laserGunSprite = rm.loadImage("laser_gun.png");
//        paddleSpriteVerticalLeft = rm.loadImage("paddle3.png"); // dọc (ảnh bạn gửi)
//        paddleSpriteVerticalRight = rm.loadImage("paddle2.png");
        paddleSpriteVerticalLeft = loadWithFallback(rm, "paddle_left.png", "paddle3.png"); // dọc (ảnh bạn gửi)
        paddleSpriteVerticalRight = loadWithFallback(rm, "paddle_right.png", "paddle2.png");
    }

    private Image loadWithFallback(ResourceManager rm, String preferred, String fallback) {
        Image image = rm.loadImage(preferred);
        if (image == null && fallback != null) {
            image = rm.loadImage(fallback);
        }
        return image;
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

        if (lockedX != null) {
            x = lockedX;
        }

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
//        if (x < Constants.PLAYFIELD_LEFT) {
//            x = Constants.PLAYFIELD_LEFT;
//        if (x < boundLeft) {
//            x = boundLeft;
//        }
        if (lockedX == null) {
            if (x < boundLeft) {
                x = boundLeft;
            }
//        if (x + width > Constants.WIDTH) {
//            x = Constants.WIDTH - width;
//        if (x + width > Constants.PLAYFIELD_RIGHT) {
//            x = Constants.PLAYFIELD_RIGHT - width;
//        if (x + width > boundRight) {
//            x = boundRight - width;
            if (x + width > boundRight) {
                x = boundRight - width;
            }
        }
        if (y < boundTop) {
            y = boundTop;
        }
        if (y + height > boundBottom) {
            y = boundBottom - height;
        }
    }

    public void setVerticalMovementBounds(double top, double bottom) {
        this.boundTop = top;
        this.boundBottom = bottom;
    }

    public void setMovementBounds(double left, double right) {
        this.boundLeft = left;
        this.boundRight = right;
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
//        if (paddleSprite != null) {
//            gc.drawImage(paddleSprite, x, y, width, height);
        Image spriteToRender;
        switch (orientation) {
            case VERTICAL_LEFT -> spriteToRender = paddleSpriteVerticalLeft != null
                    ? paddleSpriteVerticalLeft
                    : paddleSprite;
            case VERTICAL_RIGHT -> spriteToRender = paddleSpriteVerticalRight != null
                    ? paddleSpriteVerticalRight
                    : paddleSprite;
            default -> spriteToRender = paddleSprite;
        }

        if (spriteToRender != null) {
            gc.drawImage(spriteToRender, x, y, width, height);
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
        if (laserGunSprite != null) {
            gc.drawImage(laserGunSprite, x + width * 0.25 - 5, y - 15, 10, 15);
            gc.drawImage(laserGunSprite, x + width * 0.75 - 5, y - 15, 10, 15);
        } else {
            gc.setFill(Color.DARKGRAY);
            gc.fillRect(x + width * 0.25 - 3, y - 10, 6, 10);
            gc.fillRect(x + width * 0.75 - 3, y - 10, 6, 10);
        }

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

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation == null ? Orientation.HORIZONTAL : orientation;
        if (this.orientation != Orientation.HORIZONTAL) {
            this.dx = 0;
        } else {
            unlockHorizontalPosition();
        }
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void lockHorizontalPosition(double xPosition) {
        this.lockedX = xPosition;
        this.x = xPosition;
        this.dx = 0;
    }

    public void unlockHorizontalPosition() {
        this.lockedX = null;
    }
}