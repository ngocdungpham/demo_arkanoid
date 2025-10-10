package com.ooparkanoid.object.PowerUp;
import com.ooparkanoid.object.GameObject;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class PowerUp extends GameObject {
    protected boolean active = false;     // Đang có hiệu lực hay không
    protected boolean collected = false;  // Đã được nhặt chưa
    protected double duration;            // Thời gian hiệu ứng còn lại
    protected double timer = 0;           // Đếm thời gian trôi
    protected double fallSpeed = 100;     // Tốc độ rơi xuống

    public PowerUp(double x, double y, double w, double h, double duration) {
        super(x, y, w, h);
        this.duration = duration;
    }

    public abstract void applyEffect();
    public abstract void removeEffect();

    // Cập nhật trạng thái power-up
    public void update(double deltaTime) {
        if (!active) {
            y += fallSpeed * deltaTime;
        } else {
            timer += deltaTime;
        }
    }

    // Kiểm tra xem hiệu ứng đã hết thời gian chưa
    public boolean isExpired() {
        return active && timer >= duration;
    }

    // Kích hoạt khi được nhặt
    public void collect() {
        this.collected = true;
        this.active = true;
        this.timer = 0;
    }

    public boolean isActive() { return active; }
    public boolean isCollected() { return collected; }

    @Override
    public void render(GraphicsContext gc) {
        if (collected) return; // Không vẽ nếu đã được nhặt
        gc.setFill(Color.BLUE);
        gc.fillRect(x, y, width, height);
    }
}
