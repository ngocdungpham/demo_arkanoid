package com.ooparkanoid.object.PowerUp;

import javafx.scene.paint.Color;
import java.util.Random;

public class PowerUpFactory {
    private static final Random random = new Random();
    private static final double DEFAULT_DURATION = 10.0;
    private static final double INSTANT_DURATION = 0.1; // Cho instant effects

    public enum PowerUpType {
        // BUFFS (Tốt)
        FAST_BALL,
        EXPAND_PADDLE,
        MULTI_BALL,
        INVINCIBLE_BALL,
        SCORE_MULTIPLIER,
        EXTRA_LIFE,
        FIRE_BALL,

        // DEBUFFS (Xấu)
        SLOW_BALL,
        SHRINK_PADDLE
    }

    /**
     * Tạo random powerup với 75% buff, 25% debuff
     */
    public static PowerUp createRandomPowerUp(double x, double y) {
        boolean isBuff = random.nextDouble() < 0.75; // 75% buff

        if (isBuff) {
            PowerUpType[] buffs = {
                    PowerUpType.FAST_BALL,
                    PowerUpType.EXPAND_PADDLE,
                    PowerUpType.MULTI_BALL,
                    PowerUpType.INVINCIBLE_BALL,
                    PowerUpType.SCORE_MULTIPLIER,
                    PowerUpType.EXTRA_LIFE,
                    PowerUpType.FIRE_BALL
            };
            return createPowerUp(x, y, buffs[random.nextInt(buffs.length)]);
        } else {
            PowerUpType[] debuffs = {
                    PowerUpType.SLOW_BALL,
                    PowerUpType.SHRINK_PADDLE
            };
            return createPowerUp(x, y, debuffs[random.nextInt(debuffs.length)]);
        }
    }

    /**
     * Tạo powerup theo type cụ thể
     */
    public static PowerUp createPowerUp(double x, double y, PowerUpType type) {
        switch (type) {
            // === BUFFS ===
            case FAST_BALL:
                return new PowerUp(x, y, 30, 30,
                        new FastBallEffect(1.5),
                        Color.RED,
                        DEFAULT_DURATION);

            case EXPAND_PADDLE:
                return new PowerUp(x, y, 30, 30,
                        new ExpandPaddleEffect(1.5),
                        Color.GREEN,
                        DEFAULT_DURATION);

            case MULTI_BALL:
                return new PowerUp(x, y, 30, 30,
                        new MultiBallEffect(2),
                        Color.CYAN,
                        INSTANT_DURATION);

            case INVINCIBLE_BALL:
                return new PowerUp(x, y, 30, 30,
                        new InvincibleBallEffect(),
                        Color.GOLD,
                        8.0); // 8 seconds

            case SCORE_MULTIPLIER:
                return new PowerUp(x, y, 30, 30,
                        new ScoreMultiplierEffect(2.0),
                        Color.LIGHTGREEN,
                        DEFAULT_DURATION);

            case EXTRA_LIFE:
                return new PowerUp(x, y, 30, 30,
                        new ExtraLifeEffect(1),
                        Color.PINK,
                        INSTANT_DURATION);

            case FIRE_BALL:
                return new PowerUp(x, y, 30, 30,
                        new FireBallEffect(),
                        Color.ORANGERED,
                        DEFAULT_DURATION);

            // === DEBUFFS ===
            case SLOW_BALL:
                return new PowerUp(x, y, 30, 30,
                        new SlowBallEffect(0.6),
                        Color.PURPLE,
                        DEFAULT_DURATION);

            case SHRINK_PADDLE:
                return new PowerUp(x, y, 30, 30,
                        new ShrinkPaddleEffect(0.6),
                        Color.ORANGE,
                        DEFAULT_DURATION);

            default:
                return null;
        }
    }

    // === Convenience methods ===
    public static PowerUp createFastBall(double x, double y) {
        return createPowerUp(x, y, PowerUpType.FAST_BALL);
    }

    public static PowerUp createSlowBall(double x, double y) {
        return createPowerUp(x, y, PowerUpType.SLOW_BALL);
    }

    public static PowerUp createExpandPaddle(double x, double y) {
        return createPowerUp(x, y, PowerUpType.EXPAND_PADDLE);
    }

    public static PowerUp createShrinkPaddle(double x, double y) {
        return createPowerUp(x, y, PowerUpType.SHRINK_PADDLE);
    }

    public static PowerUp createMultiBall(double x, double y) {
        return createPowerUp(x, y, PowerUpType.MULTI_BALL);
    }

    public static PowerUp createInvincibleBall(double x, double y) {
        return createPowerUp(x, y, PowerUpType.INVINCIBLE_BALL);
    }

    public static PowerUp createScoreMultiplier(double x, double y) {
        return createPowerUp(x, y, PowerUpType.SCORE_MULTIPLIER);
    }

    public static PowerUp createExtraLife(double x, double y) {
        return createPowerUp(x, y, PowerUpType.EXTRA_LIFE);
    }

    public static PowerUp createFireBall(double x, double y) {
        return createPowerUp(x, y, PowerUpType.FIRE_BALL);
    }
}