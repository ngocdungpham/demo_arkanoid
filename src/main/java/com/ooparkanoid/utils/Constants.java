package com.ooparkanoid.utils;

public class Constants {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

    // Paddle
    public static final int PADDLE_WIDTH = 800;
    public static final int PADDLE_HEIGHT = 12;
    public static final double PADDLE_SPEED = 420;

    // Ball
    public static final int BALL_RADIUS = 8;
    public static final int BALL_SPEED = 1000;

    // Default settings
    public static final int START_LIVES = 3;
    public static final int START_LIVES_TIME = 60;
    public static final double DEFAULT_SPEED = 400.0;

    // Bricks
    public static final int BRICK_WIDTH = 70;
    public static final int BRICK_HEIGHT = 20;
    public static final int BRICK_PADDING_X = 10; // Khoảng cách giữa các gạch theo chiều ngang
    public static final int BRICK_PADDING_Y = 5;  // Khoảng cách giữa các gạch theo chiều dọc
    public static final int BRICK_OFFSET_TOP = 50; // Khoảng cách từ đỉnh màn hình đến hàng gạch đầu tiên
    public static final String LEVELS_FOLDER = "/levels/"; // Thư mục chứa các file level
    public static final int MAX_LEVELS = 4; // Số lượng level tối đa bạn đã tạo file
}
