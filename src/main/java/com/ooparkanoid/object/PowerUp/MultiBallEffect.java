package com.ooparkanoid.object.PowerUp;

import com.ooparkanoid.object.Ball;
import com.ooparkanoid.utils.Constants;
import java.util.ArrayList;
import java.util.List;

public class MultiBallEffect implements PowerUpEffect {
    private final int extraBalls;
    private List<Ball> spawnedBalls = new ArrayList<>();

    public MultiBallEffect(int extraBalls) {
        this.extraBalls = extraBalls; // Số bóng thêm vào
    }

    @Override
    public void apply(GameContext context) {
        List<Ball> currentBalls = context.getBalls();
        if (currentBalls.isEmpty()) return;

        Ball templateBall = currentBalls.get(0);

        for (int i = 0; i < extraBalls; i++) {
            // Tạo góc ngẫu nhiên
            double angle = Math.random() * Math.PI - Math.PI / 2; // -90° to +90°
            double speed = templateBall.getSpeed();

            Ball newBall = new Ball(
                    templateBall.getX(),
                    templateBall.getY(),
                    Constants.BALL_RADIUS,
                    speed,
                    Math.sin(angle),
                    -Math.abs(Math.cos(angle)) // Luôn đi lên
            );

            currentBalls.add(newBall);
            spawnedBalls.add(newBall);
        }

        System.out.println("🎱 MultiBall: Spawned " + extraBalls + " extra balls!");
    }

    @Override
    public void remove(GameContext context) {
        // MultiBall không cần remove - các bóng sẽ tự biến mất khi chạm đáy
        spawnedBalls.clear();
    }

    @Override
    public String getEffectType() {
        return "MULTI_BALL";
    }
}
