package com.ooparkanoid.object;

public abstract class MovableObject extends GameObject {
    protected double dx, dy; // tốc độ theo trục x,y

    public MovableObject(double x, double y, double width, double height, double dx, double dy) {
        super(x, y, width, height); // constructor của GameObject
        this.dx = dx;
        this.dy = dy;
    }
    // move()
    public void move(double deltaTime) {
        x += dx * deltaTime;
        y += dy * deltaTime;
    }
    @Override
    public void update(double deltaTime){
        move(deltaTime);
    }
}
