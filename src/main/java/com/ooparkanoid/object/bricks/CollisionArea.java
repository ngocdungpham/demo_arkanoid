// File: src/main/java/com/ooparkanoid/object/CollisionArea.java
package com.ooparkanoid.object.bricks;
import com.ooparkanoid.object.GameObject;

import javafx.scene.canvas.GraphicsContext;

/**
 * Một lớp GameObject đơn giản đại diện cho một vùng hình chữ nhật dùng để kiểm tra va chạm.
 * Nó không có logic cập nhật hay vẽ.
 */
public class CollisionArea extends GameObject {

    public CollisionArea(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    @Override
    public void update(double dt) {
        // Vùng va chạm tĩnh, không cần cập nhật
    }

    @Override
    public void render(GraphicsContext gc) {
        // Vùng va chạm không cần được vẽ ra
    }
}
