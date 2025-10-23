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
        double circleCenterX = this.x + this.radius;
        double circleCenterY = this.y + this.radius;

        double rectX = brick.getX();
        double rectY = brick.getY();
        double rectWidth = brick.getWidth();
        double rectHeight = brick.getHeight();

        double closestX = clamp(circleCenterX, rectX, rectX + rectWidth);
        double closestY = clamp(circleCenterY, rectY, rectY + rectHeight);

        double distX = circleCenterX - closestX;
        double distY = circleCenterY - closestY;
        double distanceSquared = (distX * distX) + (distY * distY);

        return distanceSquared < (this.radius * this.radius);
    }

    public String getCollisionSide(Brick brick) {
        double ballCenterX = this.x + this.radius;
        double ballCenterY = this.y + this.radius;

        double brickCenterX = brick.getX() + brick.getWidth() / 2;
        double brickCenterY = brick.getY() + brick.getHeight() / 2;

        double overlapX = (this.radius + brick.getWidth() / 2)
                - Math.abs(ballCenterX - brickCenterX);
        double overlapY = (this.radius + brick.getHeight() / 2)
                - Math.abs(ballCenterY - brickCenterY);

        if (overlapX < overlapY) {
            return ballCenterX < brickCenterX ? "LEFT" : "RIGHT";
        } else {
            return ballCenterY < brickCenterY ? "TOP" : "BOTTOM";
        }
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
