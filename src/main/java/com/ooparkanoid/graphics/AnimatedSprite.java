package com.ooparkanoid.graphics;

import com.ooparkanoid.object.GameObject;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;


import java.util.HashMap;
import java.util.Map;

// GameObject có thể có nhiều animations khác nhau
public abstract class AnimatedSprite extends GameObject {
    protected Map<String, Animation> animations = new HashMap<>();
    protected Animation currentAnimation;
    protected String currentAnimationName;

    // lật ngang
    protected boolean flipX = false;
    protected boolean flipY = false;

    // Offset để căn chỉnh
    protected double offsetX = 0;
    protected double offsetY = 0;

    public AnimatedSprite(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    // Thêm animation vào sprite
    public void addAnimation(String name, Animation animation) {
        animations.put(name, animation);
        if (currentAnimation == null) {
            playAnimation(name);
        }
    }

    public void playAnimation(String name) {
        if (currentAnimationName != null && currentAnimationName.equals(name)) {
            return; // đang chạy return;
        }

        // gán thành animation hiện tại
        Animation animation = animations.get(name);
        if (animation != null) {
            currentAnimation = animation;
            currentAnimationName = name;
            animation.reset(); // đưa về khung hình đầu
        }
    }

    @Override
    public void update(double deltaTime) {
        if (currentAnimation != null) {
            currentAnimation.update(deltaTime);
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (currentAnimation == null) {
            return;
        }
        Image frame = currentAnimation.getCurrentFrame();
        if (frame == null) {
            return;
        }
        gc.save();
        // Nếu Flip
        if (flipX || flipY) {
            gc.translate(x + width / 2, y + height / 2);    // chuyển tọa độ đến trung tâm của đối tượng
            gc.scale(flipX ? -1 : 1, flipY ? -1 : 1);
            // chuyển vị trí vẽ về góc
            gc.drawImage(frame, -width / 2 + offsetX, -height / 2 + offsetY, width, height);
        } else {
            gc.drawImage(frame, x + offsetX, y + offsetY, width, height);
        }
        gc.restore(); // đưa gc về trạng thái trước khi lật (save)
    }

    public void setFlipX(boolean flipX) {
        this.flipX = flipX;
    }

    public void setFlipY(boolean flipY) {
        this.flipY = flipY;
    }
}

