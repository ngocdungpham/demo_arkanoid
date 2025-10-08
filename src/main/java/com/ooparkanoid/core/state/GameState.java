package com.ooparkanoid.core.state;

/**
 *Đại diện cho các trạng thái cấp cao khác nhau của trò chơi Arkanoid.
 */
public enum GameState {
    MENU,
    RUNNING,
    PAUSED,
    GAME_OVER, MODE_SELECT, HOW_TO_PLAY, INFORMATION, PAUSE,
}