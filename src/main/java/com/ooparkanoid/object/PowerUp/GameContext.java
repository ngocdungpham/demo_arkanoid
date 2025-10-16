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
}