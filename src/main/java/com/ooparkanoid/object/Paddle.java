// File: src/main/java/com/ooparkanoid/object/Paddle.java
package com.ooparkanoid.object;

import com.ooparkanoid.graphics.Animation;
import com.ooparkanoid.graphics.ResourceManager;
import com.ooparkanoid.graphics.SpriteSheet;
import com.ooparkanoid.sound.SoundManager;
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

    private enum State {
        SPAWNING,
        LIVE,
        DESTROYED
    }

    private State currentState = State.LIVE;

    private Image paddleSprite;
    private Image laserGunSprite;
    private Image paddleSpriteVerticalLeft; // paddle left side
    private Image paddleSpriteVerticalRight; // paddle right side
    private SpriteSheet explosionSheet;
    private SpriteSheet spawnSheet;

    private Animation explosionAnimation;
    private Animation spawnAnimation;

    private static final int EXPLOSION_FRAME_WIDTH = 140;
    private static final int EXPLOSION_FRAME_HEIGHT = 142;

    private static final int SPAWN_FRAME_WIDTH = 93;
    private static final int SPAWN_FRAME_HEIGHT = 38;

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
        paddleSprite = rm.getImage("paddle1.png");
        laserGunSprite = rm.getImage("laser_gun.png");
        paddleSpriteVerticalLeft = loadWithFallback(rm, "paddle_left.png", "paddle3.png"); // dọc (ảnh bạn gửi)
        paddleSpriteVerticalRight = loadWithFallback(rm, "paddle_right.png", "paddle2.png");
        Image expSheetImg = rm.getImage("paddle_explosion.png");
        if (expSheetImg != null) {
            explosionSheet = new SpriteSheet(
                    expSheetImg,
                    EXPLOSION_FRAME_WIDTH,
                    EXPLOSION_FRAME_HEIGHT,
                    0, 0);
            explosionAnimation = loadAnimationFromSheet(explosionSheet, 7, 0.1, false);
        } else {
            System.err.println("Failed to load paddle_explosion.png");
        }

        Image spawnSheetImg = rm.getImage("paddle_spawn.png");
        if (spawnSheetImg != null) {
            spawnSheet = new SpriteSheet(
                    spawnSheetImg,
                    SPAWN_FRAME_WIDTH,
                    SPAWN_FRAME_HEIGHT,
                    0, 0);
            spawnAnimation = loadAnimationFromSheet(spawnSheet, 4, 0.3, false);
        } else {
            System.err.println("Failed to load paddle_spawn_sheet.png");
        }
    }

    private Animation loadAnimationFromSheet(SpriteSheet sheet, int countFrame, double frameDuration, boolean loop) {
        Image[] frames = new Image[countFrame];
        for (int i = 0; i < countFrame; i++) {
            frames[i] = sheet.getFrame(i);
        }
        return new Animation(frames, frameDuration, loop);
    }


    private Image loadWithFallback(ResourceManager rm, String preferred, String fallback) {
        Image image = rm.getImage(preferred);
        if (image == null && fallback != null) {
            image = rm.getImage(fallback);
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

    public void destroy() {
        if (currentState == State.DESTROYED) return;
        currentState = State.DESTROYED;
        explosionAnimation.reset();
        spawnAnimation.reset();
        SoundManager.getInstance().play("lose_life");
    }

    public boolean isSpawning() {
        return currentState == State.SPAWNING;
    }

    public boolean isDestroyed() {
        return currentState == State.DESTROYED;
    }

    public boolean isExplosionFinished() {
        if (currentState != State.DESTROYED) return false;

        if (explosionAnimation == null) {
            return true;
        }
        return explosionAnimation.isFinished();
    }

    public void reset() {
        currentState = State.LIVE;
    }


    @Override
    public void update(double dt) {
        // Cập nhật vị trí dựa trên vận tốc và thời gian
        switch (currentState) {
            case LIVE:
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

                if (lockedX == null) {
                    if (x < boundLeft) {
                        x = boundLeft;
                    }
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
                return;
            case SPAWNING:
                if (spawnAnimation != null) {
                    spawnAnimation.update(dt);
                    if (spawnAnimation.isFinished()) {
                        currentState = State.LIVE; // Chuyển sang LIVE khi xong
                    }
                } else {
                    currentState = State.LIVE; // Nếu không có anim, xong ngay
                }
                return;
            case DESTROYED:
                if (explosionAnimation != null) {
                    explosionAnimation.update(dt);
                    if (explosionAnimation.isFinished()) {
                        currentState = State.SPAWNING;
                        SoundManager.getInstance().play("transition");
                    }
                }
                return;
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
        switch (currentState) {
            case LIVE:
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
                return;
            case SPAWNING:
                if (spawnAnimation != null) {
                    Image frame = spawnAnimation.getCurrentFrame();
                    if (frame != null) {
                        double frameWidth = frame.getWidth();
                        double frameHeight = frame.getHeight();
                        gc.drawImage(frame, x + (width - frameWidth) / 2,
                                y + (height - frameHeight) / 2);
                    }
                }
                return;
            case DESTROYED:
                if (explosionAnimation != null) {
                    Image frame = explosionAnimation.getCurrentFrame();
                    if (frame != null) {
                        double frameWidth = frame.getWidth();
                        double frameHeight = frame.getHeight();
                        gc.drawImage(frame, x + (width - frameWidth) / 2,
                                y + (height - frameHeight) / 2);
                    }
                }
                return;
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