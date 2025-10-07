package com.ooparkanoid.core.state;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.input.KeyCode;


/**
 *  Tập trung vào các chuyển trạng thái trò chơi và cung cấp các thuộc tính quan sát được để lớp UI có thể
 * phản ứng với các thay đổi như điểm số, mạng sống và trạng thái cấp cao (menu, tạm dừng, kết thúc trò chơi,...).
 */

public class GameStateManager {
    private final ObjectProperty<GameState> currentState = new SimpleObjectProperty<>(GameState.MENU);
    private final IntegerProperty score = new SimpleIntegerProperty();
    private final IntegerProperty lives = new SimpleIntegerProperty();
    private final BooleanProperty continueAvailable = new SimpleBooleanProperty(false);
    private final StringProperty statusMessage = new SimpleStringProperty("Welcome to Arkanoid!");


    /**
     * Cập nhật các giá trị bảng điểm được hiển thị trong giao diện người dùng.
     */
    public void updateStats(int score, int lives) {
        this.score.set(score);
        this.lives.set(lives);
    }

    /**
     * Đánh dấu rằng một phiên trò chơi hoàn toàn mới sắp bắt đầu
     */
    public void  beginNewGame(int initialScore, int initialLives) {
        updateStats(initialScore, initialLives);
        continueAvailable.set(true);
        statusMessage.set("Destroy all the bricks!");
        currentState.set(GameState.RUNNING);
    }

    /**
     * Hiển thị menu chính mà không làm mất tiến trình hiện tại.
     */
    public void showMenu() {
        currentState.set(GameState.MENU);
        statusMessage.set("Game menu");
    }

    /**
     * reset lại meunu và hủy tiến trình tiếp tục.
     */
    public void resetToMenu() {
        continueAvailable.set(false);
        showMenu();
    }

    /**
     * Đặt thêm dừng game.
     */
    public void pauseGame() {
        if (currentState.get() == GameState.RUNNING) {
            currentState.set(GameState.PAUSED);
            statusMessage.set("Game paused");
        }
    }

    /**
     * chạy lại.
     */
    public void resumeGame() {
        if (currentState.get() == GameState.PAUSED) {
            currentState.set(GameState.RUNNING);
            statusMessage.set("Back to the action!");
        }
    }

    /**
     * chuyển trạng thái game về kết thúc.
     */
    public void markGameOver() {
        continueAvailable.set(false);
        currentState.set(GameState.GAME_OVER);
        statusMessage.set("Game over");
    }

    public boolean isRunning() {
        return currentState.get() == GameState.RUNNING;
    }

    public boolean canContinue() {
        return continueAvailable.get() && currentState.get() != GameState.RUNNING;
    }

    public ObjectProperty<GameState> stateProperty() {
        return currentState;
    }

    public GameState getCurrentState() {
        return currentState.get();
    }
    public IntegerProperty scoreProperty() {
        return score;
    }

    public IntegerProperty livesProperty() {
        return lives;
    }

    public int getScore() {
        return score.get();
    }

    public int getLives() {
        return lives.get();
    }

    public BooleanProperty continueAvailableProperty() {
        return continueAvailable;
    }

    public StringProperty statusMessageProperty() {
        return statusMessage;
    }

    public void setStatusMessage(String message) {
        statusMessage.set(message);
    }


}
