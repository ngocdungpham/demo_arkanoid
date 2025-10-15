package com.ooparkanoid.object;

import javafx.scene.canvas.GraphicsContext;

public class Ball extends MovableObject {
    private double speed;
    private double dirX, dirY;      // Normalized Direction Vector;
    // Constructor
    public Ball(double x, double y, double radius,  double speed, double dirX, double dirY) {
        super(x - radius, y - radius, radius*2, radius*2, 0, 0);
        this.speed = speed;
        this.setDirection(dirX,dirY);
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

    public void setPosition(double ballX, double ballY) {
        this.x = ballX;
        this.y = ballY;
    }

    public void setVelocity(double ballDX, double ballDY) {
        this.dx = ballDX;
        this.dy = ballDY;
    }
}
