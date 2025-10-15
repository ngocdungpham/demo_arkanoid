// File: src/main/java/com/ooparkanoid/object/bricks/Brick.java
package com.ooparkanoid.object.bricks;

import com.ooparkanoid.object.GameObject;
import com.ooparkanoid.utils.Constants;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class Brick extends GameObject {

    // --- Enum mới cho loại gạch ---
    public enum BrickType {
        NORMAL,
        STRONG,
        // TODO: Thêm các loại gạch đặc biệt khác ở đây (ví dụ: INDESTRUCTIBLE, EXPLOSIVE)
        INDESTRUCTIBLE //Thêm loại gạch mới
    }

    protected int hitPoints;
    protected boolean destroyed;
    protected BrickType type; // <--- Thuộc tính type mới

    public Brick(double x, double y, int hitPoints, BrickType type) { // <--- Cập nhật constructor
        super(x, y, Constants.BRICK_WIDTH, Constants.BRICK_HEIGHT);
        this.hitPoints = hitPoints;
        this.destroyed = false;
        this.type = type; // <--- Gán type
    }

    public void takeHit() {
        //Chỉ giảm hitPoints nếu không phải INDESTRUCTIBLE
        if (!destroyed && type != BrickType.INDESTRUCTIBLE) {
            hitPoints--;
            if (hitPoints <= 0) {
                destroyed = true;
            }
        }
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public abstract void render(GraphicsContext gc);

    @Override
    public void update(double dt) {
        // Bricks typically don't update their position or internal state over time
    }

    // Getters
    public int getHitPoints() {
        return hitPoints;
    }

    public BrickType getType() { // <--- Getter cho type
        return type;
    }
}
