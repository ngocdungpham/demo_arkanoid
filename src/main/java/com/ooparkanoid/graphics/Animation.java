package com.ooparkanoid.graphics;


import javafx.scene.image.Image;

public class Animation {
    private Image[] frames;
    private int currentFrame = 0;
    private double frameDuration;       // Thời gian mỗi frame
    private double timer = 0;
    private boolean loop;   // lặp lại animation
    private boolean finished = false;

    public Animation(Image[] frames, double frameDuration, boolean loop) {
        this.frames = frames;
        this.frameDuration = frameDuration;
        this.loop = loop;
    }

    public void update(double deltaTime) {
        if (finished && !loop) return;
        timer += deltaTime;
        if (timer >= frameDuration) {
            timer -= frameDuration;
            currentFrame++;
            if (currentFrame >= frames.length) {
                if (loop) {
                    currentFrame = 0;
                } else {
                    currentFrame = frames.length - 1; // Dừng ở cuối
                    finished = true;
                }
            }
        }
    }

    // Lấy frame hiện tải
    public Image getCurrentFrame() {
        return frames[currentFrame];
    }

    public void reset() {
        currentFrame = 0;
        timer = 0;
        finished = false;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }
}
