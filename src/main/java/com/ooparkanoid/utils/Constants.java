package com.ooparkanoid.utils;

public class Constants {
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;

    // Layout ratios (total = 9 parts)
    public static final double SIDE_PANEL_RATIO = 2.0 / 9.0;
    public static final double PLAYFIELD_RATIO = 5.0 / 9.0;

    public static final double LEFT_PANEL_WIDTH = WIDTH * SIDE_PANEL_RATIO;
    public static final double RIGHT_PANEL_WIDTH = WIDTH * SIDE_PANEL_RATIO;
    public static final double PLAYFIELD_WIDTH = WIDTH * PLAYFIELD_RATIO;
    public static final double PLAYFIELD_LEFT = LEFT_PANEL_WIDTH;
    public static final double PLAYFIELD_RIGHT = PLAYFIELD_LEFT + PLAYFIELD_WIDTH;


    // Paddle
    public static final int PADDLE_WIDTH = 100;
    public static final int PADDLE_HEIGHT = 20;
    public static final double PADDLE_SPEED = 420;

    // Ball
    public static final int BALL_RADIUS = 8;
    public static final int BALL_SPEED = 1000;

    // Laser
    public static final int LASER_WIDTH = 10;
    public static final int LASER_HEIGHT = 30;
    // Default settings
    public static final int START_LIVES = 3;
    public static final int START_LIVES_TIME = 60;
    public static final double DEFAULT_SPEED = 400.0;

    // Bricks

    public static final int BRICK_WIDTH = 70;
    public static final int BRICK_HEIGHT = 20;

    public static final int BRICK_PADDING_X = 5; // Khoảng cách giữa các gạch theo chiều ngang
    public static final int BRICK_PADDING_Y = 2;  // Khoảng cách giữa các gạch theo chiều dọc
    public static final int BRICK_OFFSET_TOP = 50; // Khoảng cách từ đỉnh màn hình đến hàng gạch đầu tiên
    public static final String LEVELS_FOLDER = "/levels/"; // Thư mục chứa các file level
    public static final int MAX_LEVELS = 6;// Số lượng level tối đa bạn đã tạo file
    public static final double POWERUP_DROP_CHANCE = 1;
    public static final int MAX_COLS_PER_LEVEL = 9;
    public static final int MAX_ROWS_PER_LEVEL = 15;
}
