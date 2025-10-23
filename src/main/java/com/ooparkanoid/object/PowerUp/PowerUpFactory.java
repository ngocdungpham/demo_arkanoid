package com.ooparkanoid.object.PowerUp;

import javafx.scene.paint.Color;
import java.util.Random;

public class PowerUpFactory {
    private static final Random random = new Random();
    private static final double DEFAULT_DURATION = 10.0;
    private static final double INSTANT_DURATION = 0.1; // Cho instant effects



    /**
     * Tạo random powerUp với 75% buff, 25% debuff
     */
    public static PowerUp createRandomPowerUp(double x, double y) {
        boolean isBuff = random.nextDouble() < 0.6; // 60% buff
        if (isBuff) {
            PowerUpSprite.PowerUpType[] buffs = {
                    PowerUpSprite.PowerUpType.FAST_BALL,
                    PowerUpSprite.PowerUpType.EXPAND_PADDLE,
                    PowerUpSprite.PowerUpType.MULTI_BALL,
                    PowerUpSprite.PowerUpType.INVINCIBLE_BALL,
                    PowerUpSprite.PowerUpType.SCORE_MULTIPLIER,
                    PowerUpSprite.PowerUpType.EXTRA_LIFE,
                    PowerUpSprite.PowerUpType.FIRE_BALL,
                    PowerUpSprite.PowerUpType.LASER_PADDLE
            };
            return createPowerUp(x, y, buffs[random.nextInt(buffs.length)]);
        } else {
            PowerUpSprite.PowerUpType[] debuffs = {
                    PowerUpSprite.PowerUpType.SLOW_BALL,
                    PowerUpSprite.PowerUpType.SHRINK_PADDLE
            };
            return createPowerUp(x, y, debuffs[random.nextInt(debuffs.length)]);
        }
    }

    /**
     * Tạo powerUp theo type cụ thể
     */
    public static PowerUp createPowerUp(double x, double y, PowerUpSprite.PowerUpType type) {
        return switch (type) {
            // BUFFS
            case FAST_BALL -> new PowerUp(x, y, 40, 20,
                    new FastBallEffect(1.25),
                    type,
                    DEFAULT_DURATION);
            case EXPAND_PADDLE -> new PowerUp(x, y, 40, 20,
                    new ExpandPaddleEffect(1.35),
                    type,
                    DEFAULT_DURATION);
            case MULTI_BALL -> new PowerUp(x, y, 40, 20,
                    new MultiBallEffect(2),
                    type,
                    INSTANT_DURATION);
            case INVINCIBLE_BALL -> new PowerUp(x, y, 40, 20,
                    new InvincibleBallEffect(),
                    type,
                    DEFAULT_DURATION);
            case SCORE_MULTIPLIER -> new PowerUp(x, y, 40, 20,
                    new ScoreMultiplierEffect(2.0),
                    type,
                    DEFAULT_DURATION);
            case EXTRA_LIFE -> new PowerUp(x, y, 40, 20,
                    new ExtraLifeEffect(1),
                    type,
                    INSTANT_DURATION);
            case FIRE_BALL -> new PowerUp(x, y, 40, 20,
                    new FireBallEffect(),
                    type,
                    DEFAULT_DURATION);
            case LASER_PADDLE -> new PowerUp(x, y, 40, 20,
                    new LaserPaddleEffect(),
                    type,
                    DEFAULT_DURATION);

            // DEBUFFS
            case SLOW_BALL -> new PowerUp(x, y, 40, 20,
                    new SlowBallEffect(0.6),
                    type,
                    DEFAULT_DURATION);
            case SHRINK_PADDLE -> new PowerUp(x, y, 40, 20,
                    new ShrinkPaddleEffect(0.6),
                    type,
                    DEFAULT_DURATION);
        };
    }
}