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

    public void bounceOff (GameObject other) {

    }

    public boolean checkCollision(GameObject other) {
        return istersected(other);
    }

    @Override
    public void move(double deltaTime){
        super.move(deltaTime);
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.fillOval(x, y, width, height);
    }
}
