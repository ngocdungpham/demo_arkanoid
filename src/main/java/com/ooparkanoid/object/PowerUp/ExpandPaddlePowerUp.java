//package com.ooparkanoid.object.PowerUp;
//
//import com.ooparkanoid.core.engine.GameManager;
//import com.ooparkanoid.object.GameObject;
//import com.ooparkanoid.object.Paddle;
//import javafx.scene.canvas.GraphicsContext;
//import javafx.scene.paint.Color;
//
//public class ExpandPaddlePowerUp extends PowerUp {
//    private final double scaleFactor = 1.5;  // Hệ số tăng chiều dài
//    private double originalWidth;
//
//    public ExpandPaddlePowerUp(double x, double y, double w, double h, double duration) {
//        super(x, y, w, h, duration);
//    }
//
//    @Override
//    public void applyEffect(GameObject target) {
//        if (collected) return;
//        if (!(target instanceof Paddle)) return;
//
//        collect(); // đánh dấu đã nhặt
//        Paddle paddle = (Paddle) target;
//
//        if (!active) {
//            originalWidth = paddle.getWidth();
//            paddle.setWidth(originalWidth * scaleFactor);
//            active = true;
//            System.out.println("🌟 ExpandPaddle applied! New width = " + paddle.getWidth());
//        }
//    }
//
//    @Override
//    public void removeEffect(GameObject target) {
//        if (!active) return;
//        if (!(target instanceof Paddle)) return;
//
//        Paddle paddle = (Paddle) target;
//        paddle.setWidth(originalWidth);
//        active = false;
//        System.out.println("⏱️ ExpandPaddle expired! Width reset = " + originalWidth);
//    }
//
//    @Override
//    public void update(double deltaTime) {
//        super.update(deltaTime);
//
//        // Nếu hết thời gian, tự gỡ hiệu ứng
//        if (isExpired()) {
//            removeEffect(GameManager.getInstance().getPaddle());
//        }
//    }
//
//    @Override
//    public void render(GraphicsContext gc) {
//        if (collected) return;
//        gc.setFill(Color.GREEN);
//        gc.fillRect(x, y, width, height);
//    }
//}
