// File: src/main/java/com/ooparkanoid/object/bricks/StrongBrick.java
package com.ooparkanoid.object.bricks;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import com.ooparkanoid.graphics.ResourceManager;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.image.Image;

public class StrongBrick extends Brick {
    private static Map<Integer, Image> strongBrickTextures = new HashMap<>();

    // Load textures một lần khi lớp được nạp
    // Load textures một lần khi lớp được nạp
    static {
        ResourceManager rm = ResourceManager.getInstance();
        strongBrickTextures.put(3, rm.loadImage("brick_strong_hit3.png"));
        strongBrickTextures.put(2, rm.loadImage("brick_strong_hit2.png"));
        strongBrickTextures.put(1, rm.loadImage("brick_strong_hit1.png"));
    }


    public StrongBrick(double x, double y) {
        super(x, y, 3, BrickType.STRONG); // <--- Cập nhật: Thêm BrickType.STRONG
        this.texture = strongBrickTextures.get(this.hitPoints);
    }

    @Override
    public void takeHit() {
        super.takeHit(); // Giảm hitPoints
        if (!isDestroyed()) {
            this.texture = strongBrickTextures.get(this.hitPoints); // Cập nhật texture sau khi nhận hit
        } else {
            this.texture = null; // Gạch bị phá hủy không còn texture
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (!isDestroyed() && texture != null) {
            gc.drawImage(texture, x, y, width, height);
            // gc.setStroke(Color.BLACK);
            // gc.setLineWidth(1);
            // gc.strokeRect(x, y, width, height);
        }
    }
}
