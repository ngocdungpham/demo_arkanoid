package com.ooparkanoid.object;

import com.ooparkanoid.graphics.Animation;
import com.ooparkanoid.graphics.GlowTrail;
import com.ooparkanoid.graphics.ResourceManager;
import com.ooparkanoid.graphics.SpriteSheet;
import com.ooparkanoid.object.bricks.Brick;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Ball extends MovableObject {
    private double speed;
    private double dirX, dirY;      // Normalized Direction Vector;

    private Animation ballAnimation;
    private Image ballSprite;
    private boolean useAnimation = false;
    private double rotation = 0;

    private GlowTrail trail;
    private boolean showTrail = true;

    private boolean hasGlow = false;
    private Color glowColor = Color.CYAN;

    private double radius;
    // Constructor
    public Ball(double x, double y, double radius, double speed, double dirX, double dirY) {
        super(x - radius, y - radius, radius * 2, radius * 2, 0, 0);
        this.speed = speed;
        this.setDirection(dirX,dirY);
        this.radius = radius;

        this.setDirection(dirX, dirY);
        loadGraphics();
        setupTrail();
    }

    private void loadGraphics() {
        ResourceManager resourceManager = ResourceManager.getInstance();
        ballSprite = resourceManager.loadImage("ball.png");
        SpriteSheet ballSheet = resourceManager.loadSpriteSheet("ball_animation.png",
                32, 32);
        if (ballSheet != null) {
            Image[] frames = new Image[ballSheet.getFrameCount()];
            for (int i = 0; i < frames.length; i++) {
                frames[i] = ballSheet.getFrame(i);
            }
            ballAnimation = new Animation(frames, 0.05, true);
            useAnimation = true;
        }
    }

    public void setupTrail() {
        // Tạo Glow Trail
        trail = new GlowTrail(width);
        trail.setColor(Color.CYAN);
        trail.setMaxLength(30);
        trail.setGlowIntensity(1.0);

    }

    public void setDirection(double nx, double ny) {
        double len = Math.sqrt(nx * nx + ny * ny);
        if (len == 0) return;
        dirX = nx / len;
        dirY = ny / len;
        this.dx = dirX * speed;
        this.dy = dirY * speed;
    }

    @Override
    public void update(double deltaTime) {
        rotation += speed * deltaTime * 5;
        if (rotation >= 360) {
            rotation -= 360;
        }
        if (useAnimation && ballAnimation != null) {
            ballAnimation.update(deltaTime);
        }
        move(deltaTime);
        // thêm point vào trail (tâm của ball)
        if (showTrail && trail != null) {
            trail.addPoint(x + width / 2, y + height / 2);
            trail.update(deltaTime);
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        // vẽ trail
        if (showTrail && trail != null) {
            trail.render(gc);
        }
        // Vẽ ball glow ( viền ball )
        if (hasGlow) {
            renderBallGlow(gc);
        }
        // Vẽ ball
        gc.save();
        gc.translate(x + width / 2, y + height / 2);
        gc.rotate(rotation);

        if (useAnimation && ballAnimation != null) {
            Image frame = ballAnimation.getCurrentFrame();
            gc.drawImage(frame, -width / 2, -height / 2, width, height);
        } else if (ballSprite != null) {
            gc.drawImage(ballSprite, -width / 2, -height / 2, width, height);
        } else {
            gc.setFill(Color.WHITE);
            gc.fillOval(-width / 2, -height / 2, width, height);

            // Inner circle (lõi sáng)
            gc.setFill(Color.LIGHTBLUE);
            gc.fillOval(-width / 2 + 2, -height / 2 + 2, width - 4, height - 4);

            // Highlight (ánh sáng)
            gc.setGlobalAlpha(0.6);
            gc.setFill(Color.WHITE);
            gc.fillOval(-width / 4, -height / 4, width / 2, height / 2);
            gc.setGlobalAlpha(1.0);
        }
        gc.restore();
    }

    // Vẽ vòng sáng quanh Ball - Khi có PowerUp
    private void renderBallGlow(GraphicsContext gc) {
        double centerX = x + width / 2;
        double centerY = y + height / 2;

        // Outer glow
        gc.setGlobalAlpha(0.1);
        gc.setFill(glowColor);
        gc.fillOval(centerX - width * 2, centerY - height * 2, width * 4, height * 4);

        // Inner glow
        gc.setGlobalAlpha(0.2);
        gc.fillOval(centerX - width * 1.2, centerY - height * 1.2, width * 2.4, height * 2.4);
        gc.setGlobalAlpha(1.0);
    }

    // FastBall : Trial đỏ, dài, sáng
    public void activateFastBallEffect() {
        trail.setColor(Color.RED);
        trail.setMaxLength(30);
        trail.setGlowIntensity(1.5);
        setGlow(true, Color.RED);
    }

    // SLowBall : Trail xanh, ngắn
    public void activateSlowBallEffect() {
        trail.setColor(Color.LIGHTBLUE);
        trail.setMaxLength(20);
        trail.setGlowIntensity(0.7);
        setGlow(true, Color.LIGHTBLUE);
    }

    // FireBall : Trail cam đỏ , rất sáng
    public void activateFireBallEffect() {
        trail.setColor(Color.ORANGERED);
        trail.setMaxLength(40);
        trail.setGlowIntensity(1.8);
        setGlow(true, Color.ORANGERED);
    }

    // Invincibale: Trail vàng, cực sáng
    public void activateInvincibleEffect() {
        trail.setColor(Color.GOLD);
        trail.setMaxLength(30);
        trail.setGlowIntensity(2.0);
        setGlow(true, Color.GOLD);
    }

    // Reset mặc định
    public void resetTrainEffect() {
        trail.setColor(Color.CYAN);
        trail.setMaxLength(20);
        trail.setGlowIntensity(1.0);
        setGlow(false, Color.CYAN);
    }

    public void setTrailColor(Color color) {
        if (trail != null) {
            trail.setColor(color);
        }
    }

    public void setGlow(boolean hasGlow, Color color) {
        this.hasGlow = hasGlow;
        this.glowColor = color;
    }

    public void setShowTrail(boolean showTrail) {
        this.showTrail = showTrail;
        if (!showTrail && trail != null) {
            trail.clear();
        }
    }

    public void clearTrail() {
        if (trail != null) {
            trail.clear();
        }
    }

    public void setSpeed(double speed) {
        this.dx = speed * dirX;
        this.dy = speed * dirY;
        this.speed = speed;
    }

    public double getSpeed() {
        return speed;
    }

    public double getDirX() {
        return dirX;
    }

    public void setDirX(double dirX) {
        this.dirX = dirX;
    }

    public double getDirY() {
        return dirY;
    }

    public void setDirY(double dirY) {
        this.dirY = dirY;
    }

    public double getRadius() {
        return radius;
    }

    public void setPosition(double ballX, double ballY) {
        this.x = ballX;
        this.y = ballY;
    }

    public void setVelocity(double ballDX, double ballDY) {
        this.dx = ballDX;
        this.dy = ballDY;
    }
    public boolean collidesWith(Brick brick) {
        // Lấy thông tin của hình tròn
        // x, y của GameObject (Ball) là góc trên bên trái của hộp bao quanh
        // Tâm của bóng là (x + radius, y + radius)
        double circleX = this.x + this.radius;
        double circleY = this.y + this.radius;
        double circleRadius = this.radius;

        // Lấy thông tin của hình chữ nhật (gạch)
        double rectX = brick.getX();
        double rectY = brick.getY();
        double rectWidth = brick.getWidth();
        double rectHeight = brick.getHeight();

        // Tìm điểm gần nhất trên hình chữ nhật đến tâm hình tròn
        double closestX = clamp(circleX, rectX, rectX + rectWidth);
        double closestY = clamp(circleY, rectY, rectY + rectHeight);

        // Tính khoảng cách giữa điểm gần nhất và tâm hình tròn
        double distX = circleX - closestX;
        double distY = circleY - closestY;
        double distanceSquared = (distX * distX) + (distY * distY);

        // Kiểm tra xem khoảng cách bình phương có nhỏ hơn hoặc bằng bán kính bình phương không
        return distanceSquared <= (circleRadius * circleRadius);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
