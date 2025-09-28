package com.ooparkanoid.core.engine;
//import
import com.ooparkanoid.utils.Constants;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * GameWorld chịu trách nhiệm quản lý logic game:
 * - Paddle, Ball, Brick, PowerUp (sẽ thêm dần)
 * - Update() và Render()
 */
public class GameManager {
    private double paddlex;
    private double paddley;

    public GameManager() {
        reset();
    }

    public void reset() {
        paddlex = (Constants.WIDTH - Constants.PADDLE_WIDTH) / 2.0;
        paddley = Constants.HEIGHT - 40;
    }

    public void update(double dt) {
        // update ball, check collision, power_up
    }

    public void render(GraphicsContext g) {
        // Demo
        g.setFill(Color.WHITE);
        g.fillRect(paddlex, paddley, Constants.PADDLE_WIDTH, Constants.PADDLE_HEIGHT);
    }

}
