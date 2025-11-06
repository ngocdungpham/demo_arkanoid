// File: src/main/java/com/ooparkanoid/core/engine/GameRenderer.java
package com.ooparkanoid.core.engine;

import com.ooparkanoid.object.Ball;
import com.ooparkanoid.object.Paddle;
import com.ooparkanoid.object.PowerUp.PowerUp;
import com.ooparkanoid.object.PowerUp.PowerUpEffectManager;
import com.ooparkanoid.object.Score;
import com.ooparkanoid.object.bricks.Brick;
import com.ooparkanoid.utils.Constants;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

public class GameRenderer {
    private final PowerUpEffectManager effectManager;

    // References to renderable objects
    private Paddle paddle;
    private List<Ball> balls;
    private List<Brick> bricks;
    private List<PowerUp> powerUps;
    private List<Score> scores;

    public GameRenderer(PowerUpEffectManager effectManager) {
        this.effectManager = effectManager;
    }

    public void setGameObjects(Paddle paddle, List<Ball> balls, List<Brick> bricks, List<PowerUp> powerUps, List<Score> scores) {
        this.paddle = paddle;
        this.balls = balls;
        this.bricks = bricks;
        this.powerUps = powerUps;
        this.scores = scores;
    }

    /**
     * Phương thức chính để vẽ tất cả các đối tượng game lên màn hình
     */
    public void render(GraphicsContext g) {
        if (paddle == null || balls == null || effectManager == null) {
            return;
        }

        // Xóa màn hình
        g.clearRect(0, 0, Constants.WIDTH, Constants.HEIGHT);

        // Vẽ Paddle
        if (paddle != null) {
            paddle.render(g);
        }

        // Vẽ Ball
        for (Ball b : balls) b.render(g);

        // Vẽ tất cả Bricks còn lại
        for (Brick brick : bricks) {
            brick.render(g);
        }

        // RENDER POWERUPS
        for (PowerUp p : powerUps) {
            p.render(g);
        }

        // RENDER SCORES POPUP
        for (Score text : scores) {
            text.render(g);
        }

        // ===== DISPLAY ACTIVE EFFECTS (HUD) =====
        renderActiveEffects(g);
    }

    private void renderActiveEffects(GraphicsContext g) {
        // Chỉ vẽ lên vùng chơi (Playfield) để tránh ghi đè lên HUD
        double effectTextX = Constants.PLAYFIELD_LEFT + 10;
        int yOffset = 80; // Bắt đầu ở Playfield

        g.setFont(javafx.scene.text.Font.font("Arial", 16));

        // Lặp lại logic vẽ HUD từ GameManager cũ, sử dụng effectManager

        double fastTime = effectManager.getRemainingTime("FAST_BALL");
        if (fastTime > 0) {
            g.setFill(Color.RED);
            g.fillText("Fast: " + String.format("%.1f", fastTime) + "s", effectTextX, yOffset);
            yOffset += 20;
        }

        double slowTime = effectManager.getRemainingTime("SLOW_BALL");
        if (slowTime > 0) {
            g.setFill(Color.PURPLE);
            g.fillText("Slow: " + String.format("%.1f", slowTime) + "s", effectTextX, yOffset);
            yOffset += 20;
        }

        double expandTime = effectManager.getRemainingTime("EXPAND_PADDLE");
        if (expandTime > 0) {
            g.setFill(Color.GREEN);
            g.fillText("Expand: " + String.format("%.1f", expandTime) + "s", effectTextX, yOffset);
            yOffset += 20;
        }

        double shrinkTime = effectManager.getRemainingTime("SHRINK_PADDLE");
        if (shrinkTime > 0) {
            g.setFill(Color.ORANGE);
            g.fillText("Shrink: " + String.format("%.1f", shrinkTime) + "s", effectTextX, yOffset);
            yOffset += 20;
        }

        double invincibleTime = effectManager.getRemainingTime("INVINCIBLE_BALL");
        if (invincibleTime > 0) {
            g.setFill(Color.GOLD);
            g.fillText("Invincible: " + String.format("%.1f", invincibleTime) + "s", effectTextX, yOffset);
            yOffset += 20;
        }

        double scoreMultTime = effectManager.getRemainingTime("SCORE_MULTIPLIER");
        if (scoreMultTime > 0) {
            g.setFill(Color.LIGHTGREEN);
            g.fillText("x2 Score: " + String.format("%.1f", scoreMultTime) + "s", effectTextX, yOffset);
            yOffset += 20;
        }

        double fireTime = effectManager.getRemainingTime("FIRE_BALL");
        if (fireTime > 0) {
            g.setFill(Color.ORANGERED);
            g.fillText("Fire: " + String.format("%.1f", fireTime) + "s", effectTextX, yOffset);
            yOffset += 20;
        }

        double laserTime = effectManager.getRemainingTime("LASER_PADDLE");
        if (laserTime > 0) {
            g.setFill(Color.LIGHTBLUE);
            g.fillText("Laser: " + String.format("%.1f", laserTime) + "s", effectTextX, yOffset);
            yOffset += 20;
        }
    }
}