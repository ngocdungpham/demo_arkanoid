package com.ooparkanoid.entities.base;

import javafx.scene.canvas.GraphicsContext;

public abstract class GameObject {
     protected double x, y, width, height;

     public GameObject(double x, double y, double width, double height) {
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
     }

     public abstract void update(double dt);
     public abstract void render(GraphicsContext gc);

     public boolean istersected(GameObject other){
         return x < other.x + other.width && x + width > other.x &&
                 y < other.y + other.height && y + height > other.y;
     }

     // getter


    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}

