package com.ooparkanoid.utils;

/**
 * Utility class containing constant values used throughout the Arkanoid game.
 * These constants define screen dimensions, layout ratios, game object properties,
 * and gameplay settings to ensure consistency and ease of maintenance.
 */
public class Constants {
    // ---- Screen Dimensions ----
    /** Screen width in pixels. */
    public static final int WIDTH = 1280;
    /** Screen height in pixels. */
    public static final int HEIGHT = 720;

    // ---- Layout Ratios ----
    /** Ratio of side panel width to total screen width (2/9). */
    public static final double SIDE_PANEL_RATIO = 2.0 / 9.0;
    /** Ratio of playfield width to total screen width (5/9). */
    public static final double PLAYFIELD_RATIO = 5.0 / 9.0;

    /** Width of the left side panel in pixels. */
    public static final double LEFT_PANEL_WIDTH = WIDTH * SIDE_PANEL_RATIO;
    /** Width of the right side panel in pixels. */
    public static final double RIGHT_PANEL_WIDTH = WIDTH * SIDE_PANEL_RATIO;
    /** Width of the playfield area in pixels. */
    public static final double PLAYFIELD_WIDTH = WIDTH * PLAYFIELD_RATIO;
    /** Left boundary of the playfield in pixels. */
    public static final double PLAYFIELD_LEFT = LEFT_PANEL_WIDTH;
    /** Right boundary of the playfield in pixels. */
    public static final double PLAYFIELD_RIGHT = PLAYFIELD_LEFT + PLAYFIELD_WIDTH;

    // ---- Paddle Properties ----
    /** Paddle width in pixels. */
    public static final int PADDLE_WIDTH = 100;
    /** Paddle height in pixels. */
    public static final int PADDLE_HEIGHT = 20;
    /** Paddle movement speed in pixels per second. */
    public static final double PADDLE_SPEED = 420;

    // ---- Ball Properties ----
    /** Ball radius in pixels. */
    public static final int BALL_RADIUS = 8;
    /** Ball movement speed in pixels per second. */
    public static final int BALL_SPEED = 1000;

    // ---- Laser Properties ----
    /** Laser beam width in pixels. */
    public static final int LASER_WIDTH = 10;
    /** Laser beam height in pixels. */
    public static final int LASER_HEIGHT = 30;

    // ---- Default Game Settings ----
    /** Initial number of lives for the player. */
    public static final int START_LIVES = 3;
    /** Time limit for starting lives in seconds (if applicable). */
    public static final int START_LIVES_TIME = 60;
    /** Default speed for game objects in pixels per second. */
    public static final double DEFAULT_SPEED = 400.0;

    // ---- Brick Properties ----
    /** Brick width in pixels. */
    public static final int BRICK_WIDTH = 70;
    /** Brick height in pixels. */
    public static final int BRICK_HEIGHT = 20;

    /** Horizontal padding between bricks in pixels. */
    public static final int BRICK_PADDING_X = 5;
    /** Vertical padding between bricks in pixels. */
    public static final int BRICK_PADDING_Y = 2;
    /** Vertical offset from the top of the screen to the first row of bricks in pixels. */
    public static final int BRICK_OFFSET_TOP = 50;
    /** Path to the folder containing level files. */
    public static final String LEVELS_FOLDER = "/levels/";
    /** Maximum number of levels available. */
    public static final int MAX_LEVELS = 6;
    /** Probability of a power-up dropping when a brick is destroyed (0.0 to 1.0). */
    public static final double POWERUP_DROP_CHANCE = 0.25;
    /** Maximum number of brick columns per level. */
    public static final int MAX_COLS_PER_LEVEL = 9;
    /** Maximum number of brick rows per level. */
    public static final int MAX_ROWS_PER_LEVEL = 15;
}
