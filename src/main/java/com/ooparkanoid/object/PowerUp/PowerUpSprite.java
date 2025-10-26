package com.ooparkanoid.object.PowerUp;

import com.ooparkanoid.graphics.Animation;
import com.ooparkanoid.graphics.ResourceManager;
import com.ooparkanoid.graphics.SpriteSheet;
import javafx.scene.image.Image;


public class PowerUpSprite {
    private static PowerUpSprite instance;
    private SpriteSheet powerUpSheet;

    private static final int FRAME_WIDTH = 40;
    private static final int FRAME_HEIGHT = 22;
    private static final int FRAME_PER_ANIMATION = 8;

    private PowerUpSprite() {
        loadSpriteSheet();
    }

    public static PowerUpSprite getInstance() {
        if (instance == null) {
            instance = new PowerUpSprite();
        }
        return instance;
    }

    private void loadSpriteSheet() {
        ResourceManager rm = ResourceManager.getInstance();
        Image sheeetImage = rm.loadImage("powerup_sheet.png");

        if (sheeetImage != null) {
            powerUpSheet = new SpriteSheet(sheeetImage, FRAME_WIDTH, FRAME_HEIGHT, 0, 0);
            System.out.println("load image powerup_sheet");
        } else {
            System.out.println("faild to load image powerup_sheet");
        }
    }

    public Animation getAnimation(int row, double frameDuration) {
        if (powerUpSheet == null) {
            return null;
        }
        Image[] frames = new Image[FRAME_PER_ANIMATION];
        for (int i = 0; i < FRAME_PER_ANIMATION; i++) {
            frames[i] = powerUpSheet.getFrame(row, i);
        }
        return new Animation(frames, frameDuration, true);
    }

    public enum PowerUpCategory {
        NONE,
        BALL_SPEED,
        PADDLE_SIZE,
        SPECIAL_ATTACK
    }

    public enum PowerUpType {
        FAST_BALL(PowerUpCategory.BALL_SPEED),
        SLOW_BALL(PowerUpCategory.BALL_SPEED),
        EXPAND_PADDLE(PowerUpCategory.PADDLE_SIZE),
        SHRINK_PADDLE(PowerUpCategory.PADDLE_SIZE),
        MULTI_BALL(PowerUpCategory.SPECIAL_ATTACK),
        FIRE_BALL(PowerUpCategory.SPECIAL_ATTACK),
        LASER_PADDLE(PowerUpCategory.SPECIAL_ATTACK),
        INVINCIBLE_BALL(PowerUpCategory.SPECIAL_ATTACK),
        SCORE_MULTIPLIER(PowerUpCategory.NONE),
        EXTRA_LIFE(PowerUpCategory.NONE);
        private final PowerUpCategory category;

        PowerUpType(PowerUpCategory category) {
            this.category = category;
        }

        public PowerUpCategory getCategory() {
            return category;
        }
    }

    public static PowerUpCategory getCategoryForEffectType(String effectType) {
        try {
            PowerUpType type = PowerUpType.valueOf(effectType);
            return type.getCategory();
        } catch (IllegalArgumentException e) {
            // Nếu String không khớp với bất kỳ enum nào
            System.err.println("Warning: No PowerUpType enum constant for string: " + effectType);
            return PowerUpCategory.NONE;
        }
    }
    private int getRowForType(PowerUpType type) {
        switch (type) {
            case FAST_BALL:
                return 9;
            case SLOW_BALL:
                return 3;
            case EXPAND_PADDLE:
                return 1;
            case SHRINK_PADDLE:
                return 2;
            case MULTI_BALL:
                return 5;
            case FIRE_BALL:
                return 8;
            case LASER_PADDLE:
                return 0;
            case INVINCIBLE_BALL:
                return 4;
            case SCORE_MULTIPLIER:
                return 6;
            case EXTRA_LIFE:
                return 7;
            default:
                return 0;
        }
    }

    public Animation getAnimationForType(PowerUpType type) {
        int row = getRowForType(type);
        return getAnimation(row, 0.1);
    }

    // Get single frame
    public Image getSingleFrame(PowerUpType type, int frameIndex) {
        if (powerUpSheet == null) return null;
        int row = getRowForType(type);
        return powerUpSheet.getFrame(row, frameIndex);
    }
}
