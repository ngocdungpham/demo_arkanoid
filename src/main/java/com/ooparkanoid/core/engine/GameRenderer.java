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

/**
 * Handles rendering of all game objects and UI elements.
 * Responsible for drawing game objects to the canvas and displaying active power-up effects.
 * Separates rendering logic from game logic following Single Responsibility Principle.
 *
 * Rendering order (back to front):
 * 1. Clear canvas
 * 2. Bricks
 * 3. Paddle
 * 4. Balls
 * 5. Power-ups
 * 6. Score popups
 * 7. Active effects HUD
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public class GameRenderer {
    /** Manager for tracking active power-up effects and their durations */
    private final PowerUpEffectManager effectManager;

    // ==================== Renderable Game Objects ====================
    /** Player-controlled paddle */
    private Paddle paddle;

    /** Active balls in play */
    private List<Ball> balls;

    /** Bricks in current level */
    private List<Brick> bricks;

    /** Active power-ups falling on screen */
    private List<PowerUp> powerUps;

    /** Floating score indicators for visual feedback */
    private List<Score> scores;

    /**
     * Constructs a GameRenderer with specified effect manager.
     *
     * @param effectManager the power-up effect manager for displaying active effects
     */
    public GameRenderer(PowerUpEffectManager effectManager) {
        this.effectManager = effectManager;
    }

    /**
     * Injects game object references for rendering.
     * Must be called before render() to avoid null pointer exceptions.
     *
     * @param paddle the player's paddle
     * @param balls list of active balls
     * @param bricks list of bricks in current level
     * @param powerUps list of active power-ups
     * @param scores list of floating score indicators
     */
    public void setGameObjects(Paddle paddle, List<Ball> balls, List<Brick> bricks, List<PowerUp> powerUps, List<Score> scores) {
        this.paddle = paddle;
        this.balls = balls;
        this.bricks = bricks;
        this.powerUps = powerUps;
        this.scores = scores;
    }

    /**
     * Main rendering method that draws all game objects to the canvas.
     * Renders objects in proper z-order (back to front) and includes HUD elements.
     *
     * @param g the GraphicsContext to draw to
     */
    public void render(GraphicsContext g) {
        if (paddle == null || balls == null || effectManager == null) {
            return;
        }

        // Clear canvas
        g.clearRect(0, 0, Constants.WIDTH, Constants.HEIGHT);

        // Render paddle
        if (paddle != null) {
            paddle.render(g);
        }

        // Render all active balls
        for (Ball b : balls) {
            b.render(g);
        }

        // Render all bricks
        for (Brick brick : bricks) {
            brick.render(g);
        }

        // Render falling power-ups
        for (PowerUp p : powerUps) {
            p.render(g);
        }

        // Render floating score popups
        for (Score text : scores) {
            text.render(g);
        }

        // Render HUD overlay showing active effects
        renderActiveEffects(g);
    }

    /**
     * Renders the HUD overlay showing active power-up effects and their remaining time.
     * Displays effect names with color-coded text in the playfield area.
     * Each effect type has a unique color for easy identification.
     *
     * @param g the GraphicsContext to draw to
     */
    private void renderActiveEffects(GraphicsContext g) {
        // Position HUD within playfield to avoid overlapping with UI
        double effectTextX = Constants.PLAYFIELD_LEFT + 10;
        int yOffset = 80;

        g.setFont(javafx.scene.text.Font.font("Arial", 16));

        // Fast Ball effect (red)
        double fastTime = effectManager.getRemainingTime("FAST_BALL");
        if (fastTime > 0) {
            g.setFill(Color.RED);
        //    g.fillText("Fast: " + String.format("%.1f", fastTime) + "s", effectTextX, yOffset);
            yOffset += 20;
        }

        // Slow Ball effect (purple)
        double slowTime = effectManager.getRemainingTime("SLOW_BALL");
        if (slowTime > 0) {
            g.setFill(Color.PURPLE);
          //  g.fillText("Slow: " + String.format("%.1f", slowTime) + "s", effectTextX, yOffset);
            yOffset += 20;
        }

        // Expand Paddle effect (green)
        double expandTime = effectManager.getRemainingTime("EXPAND_PADDLE");
        if (expandTime > 0) {
            g.setFill(Color.GREEN);
        //    g.fillText("Expand: " + String.format("%.1f", expandTime) + "s", effectTextX, yOffset);
            yOffset += 20;
        }

        // Shrink Paddle effect (orange)
        double shrinkTime = effectManager.getRemainingTime("SHRINK_PADDLE");
        if (shrinkTime > 0) {
            g.setFill(Color.ORANGE);
         //   g.fillText("Shrink: " + String.format("%.1f", shrinkTime) + "s", effectTextX, yOffset);
            yOffset += 20;
        }

        // Invincible Ball effect (gold)
        double invincibleTime = effectManager.getRemainingTime("INVINCIBLE_BALL");
        if (invincibleTime > 0) {
            g.setFill(Color.GOLD);
          //  g.fillText("Invincible: " + String.format("%.1f", invincibleTime) + "s", effectTextX, yOffset);
            yOffset += 20;
        }

        // Score Multiplier effect (light green)
        double scoreMultTime = effectManager.getRemainingTime("SCORE_MULTIPLIER");
        if (scoreMultTime > 0) {
            g.setFill(Color.LIGHTGREEN);
           // g.fillText("x2 Score: " + String.format("%.1f", scoreMultTime) + "s", effectTextX, yOffset);
            yOffset += 20;
        }

        // Fire Ball effect (orange-red)
        double fireTime = effectManager.getRemainingTime("FIRE_BALL");
        if (fireTime > 0) {
            g.setFill(Color.ORANGERED);
           // g.fillText("Fire: " + String.format("%.1f", fireTime) + "s", effectTextX, yOffset);
            yOffset += 20;
        }

        // Laser Paddle effect (light blue)
        double laserTime = effectManager.getRemainingTime("LASER_PADDLE");
        if (laserTime > 0) {
            g.setFill(Color.LIGHTBLUE);
          //  g.fillText("Laser: " + String.format("%.1f", laserTime) + "s", effectTextX, yOffset);
            yOffset += 20;
        }
    }
}