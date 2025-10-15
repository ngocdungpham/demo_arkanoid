package com.ooparkanoid.object.PowerUp;

import com.ooparkanoid.object.Ball;
import com.ooparkanoid.object.Paddle;

import java.util.List;
import java.util.function.Consumer;

public class GameContext {
    private final Paddle paddle;
    private final List<Ball> balls;
    private Consumer<Integer> livesModifier;

    public GameContext(Paddle paddle, List<Ball> balls) {
        this.paddle = paddle;
        this.balls = balls;
    }

    public Paddle getPaddle() {
        return paddle;
    }

    public List<Ball> getBalls() {
        return balls;
    }

    public void setLivesModifier(Consumer<Integer> modifier) {
        this.livesModifier = modifier;
    }

    public void addLives(int amount) {
        if (livesModifier != null) {
            livesModifier.accept(amount);
        }
    }

    // GameContext.java  (thêm field và setter)
    private double scoreMultiplier = 1.0;
    private Runnable fireBallOn = () -> {};
    private Runnable fireBallOff = () -> {};
    private Runnable invincibleOn = () -> {};
    private Runnable invincibleOff = () -> {};

    public void setScoreMultiplier(double m) { scoreMultiplier = m; }
    public double getScoreMultiplier() { return scoreMultiplier; }

    public void onFireBall(Runnable rOn, Runnable rOff) { fireBallOn = rOn; fireBallOff = rOff; }
    public void setFireBallActive(boolean active) { if (active) fireBallOn.run(); else fireBallOff.run(); }

    public void onInvincible(Runnable rOn, Runnable rOff) { invincibleOn = rOn; invincibleOff = rOff; }
    public void setInvincibleActive(boolean active) { if (active) invincibleOn.run(); else invincibleOff.run(); }

}