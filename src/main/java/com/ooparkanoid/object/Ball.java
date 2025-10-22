package com.ooparkanoid.object;
import com.ooparkanoid.object.bricks.Brick;
import javafx.scene.canvas.GraphicsContext;

public class Ball extends MovableObject {
    private double speed;
    private double dirX, dirY;      // Normalized Direction Vector;
    private double radius;
    // Constructor
    public Ball(double x, double y, double radius,  double speed, double dirX, double dirY) {
        super(x - radius, y - radius, radius*2, radius*2, 0, 0);
        this.speed = speed;
        this.setDirection(dirX,dirY);
        this.radius = radius;

    }


    public void setDirection (double nx, double ny) {
        double len = Math.sqrt(nx * nx + ny * ny);
        if (len == 0) return;
        dirX = nx / len;
        dirY = ny / len;
        this.dx = dirX * speed;
        this.dy = dirY * speed;
    }
    @Override
    public void update(double deltaTime){
        move(deltaTime);
    }
    @Override
    public void render(GraphicsContext gc) {
        gc.fillOval(x, y, width, height);
    }

    public void setSpeed(double speed) {
        System.out.println("luc dau " + this.speed);
        this.dx = speed * dirX;
        this.dy = speed * dirY;
        this.speed = speed;
        System.out.println("luc sau : " + speed);
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
