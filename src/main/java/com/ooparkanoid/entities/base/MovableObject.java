package com.ooparkanoid.entities.base;

import com.ooparkanoid.entities.base.GameObject;

public abstract class MovableObject extends GameObject {
    protected double dx, dy; // tốc độ theo trục x,y

    public MovableObject(double x, double y, double w, double h) {
        super(x, y, w, h);
    }

    // move()
    public void move(double dt) { x += dx * dt; y += dy * dt; }
}
