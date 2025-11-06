package com.ooparkanoid.core.engine;

import com.ooparkanoid.core.state.GameStateManager;
import com.ooparkanoid.graphics.ResourceManager;
import com.ooparkanoid.object.Ball;
import com.ooparkanoid.object.GameObject;
import com.ooparkanoid.object.Paddle;
import com.ooparkanoid.sound.SoundManager;
import com.ooparkanoid.utils.Constants;
import com.ooparkanoid.object.bricks.Brick;
import com.ooparkanoid.object.bricks.StrongBrick;
import com.ooparkanoid.object.bricks.ExplosiveBrick;
import com.ooparkanoid.object.bricks.FlickerBrick;
import com.ooparkanoid.factory.*; // Import tất cả các factory
import java.util.Map;            // Thêm import Map
import java.util.HashMap;          // Thêm import HashMap
import java.util.ArrayList;
import javafx.beans.property.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;

/**
 * Orchestrates the local 2-player battle mode (Pong with Bricks).
 * All logic for rotated bricks is handled internally without modifying the Brick class.
 */
public class LocalBattleManager {

    public enum ServingPlayer {
        PLAYER_ONE,
        PLAYER_TWO
    }

    /**
     * An internal container class to hold information for a rotated brick in battle mode.
     * It uses an anonymous GameObject for collision detection.
     */
    private static class BattleBrick {
        public GameObject collisionBox;
        public double rotationAngle;
        public boolean isDestroyed = false;

        public BattleBrick(double x, double y, double width, double height, double angle) {
            this.collisionBox = new GameObject(x, y, width, height) {
                @Override public void update(double dt) {}
                @Override public void render(GraphicsContext gc) {}
            };
            this.rotationAngle = angle;
        }
    }

    private static final double FIELD_MARGIN_X = 100.0;
    private static final double FIELD_MARGIN_Y = 70.0;
    private static final double PADDLE_OFFSET_X = 56.0;
    private static final double PADDLE_HEIGHT = 160.0;
    private static final double PADDLE_WIDTH = 28.0;
    private static final double BOUNDARY_PADDING = 12.0;

    private final GameStateManager stateManager;
    private final Random random = new Random();
    private final SoundManager soundManager = SoundManager.getInstance();
    private final Image brickTexture;           // Texture cho NormalBrick
    private final Image explosiveBrickTexture;

    private Paddle playerOnePaddle;
    private Paddle playerTwoPaddle;
    private Ball ball;
    private EventHandler<KeyEvent> keyPressedHandler;
    private EventHandler<KeyEvent> keyReleasedHandler;

    private final List<BattleBrick> battleBricks = new ArrayList<>();
    private final List<DefenseBar> playerOneBars = new ArrayList<>();
    private final List<DefenseBar> playerTwoBars = new ArrayList<>();
    private Map<Brick.BrickType, BrickFactory> brickFactories;
    private List<BrickFactory> availableFactories;

    private final IntegerProperty playerOneLives = new SimpleIntegerProperty();
    private final IntegerProperty playerTwoLives = new SimpleIntegerProperty();
    private final IntegerProperty playerOneScore = new SimpleIntegerProperty();
    private final IntegerProperty playerTwoScore = new SimpleIntegerProperty();
    private final DoubleProperty matchTimeSeconds = new SimpleDoubleProperty();
    private final ObjectProperty<ServingPlayer> servingPlayer = new SimpleObjectProperty<>(ServingPlayer.PLAYER_ONE);

    private ServingPlayer lastHitter = ServingPlayer.PLAYER_ONE;
    private boolean ballLaunched = false;
    private boolean matchOver = false;
    private String winnerMessage = "";

    private double fieldLeft, fieldRight, fieldTop, fieldBottom;

    public LocalBattleManager(GameStateManager stateManager) {
        this.stateManager = stateManager;
        ResourceManager resourceManager = ResourceManager.getInstance();
        this.brickTexture = resourceManager.loadImage("brick_normal.png");
        this.explosiveBrickTexture = resourceManager.loadImage("brick_explosive.png");
        initializeFactories();
    }
    /**
     * TẠO PHƯƠNG THỨC MỚI NÀY
     * Khởi tạo và đăng ký tất cả các Brick Factory cần thiết cho chế độ Battle.
     * @param rm ResourceManager để tải texture.
     */
    private void initializeFactories() {
        brickFactories = new HashMap<>();

        // Sử dụng các biến đã được khởi tạo
        brickFactories.put(Brick.BrickType.NORMAL, new NormalBrickFactory(this.brickTexture));
        brickFactories.put(Brick.BrickType.STRONG, new StrongBrickFactory());
        brickFactories.put(Brick.BrickType.EXPLOSIVE, new ExplosiveBrickFactory(this.explosiveBrickTexture));
        brickFactories.put(Brick.BrickType.FLICKER, new FlickerBrickFactory());

        availableFactories = new ArrayList<>(brickFactories.values());
    }

    public void startMatch() {
        fieldLeft = FIELD_MARGIN_X;
        fieldRight = Constants.WIDTH - FIELD_MARGIN_X;
        fieldTop = FIELD_MARGIN_Y;
        fieldBottom = Constants.HEIGHT - FIELD_MARGIN_Y;

        double paddleStartY = (fieldTop + fieldBottom) / 2.0 - PADDLE_HEIGHT / 2.0;
        playerOnePaddle = new Paddle(fieldLeft + PADDLE_OFFSET_X, paddleStartY);
        playerOnePaddle.setWidth(PADDLE_WIDTH);
        playerOnePaddle.setHeight(PADDLE_HEIGHT);
        playerOnePaddle.setOrientation(Paddle.Orientation.VERTICAL_LEFT);
        playerOnePaddle.lockHorizontalPosition(playerOnePaddle.getX());
        playerOnePaddle.setVerticalMovementBounds(fieldTop + 24, fieldBottom - 24);

        playerTwoPaddle = new Paddle(fieldRight - PADDLE_OFFSET_X - PADDLE_WIDTH, paddleStartY);
        playerTwoPaddle.setWidth(PADDLE_WIDTH);
        playerTwoPaddle.setHeight(PADDLE_HEIGHT);
        playerTwoPaddle.setOrientation(Paddle.Orientation.VERTICAL_RIGHT);
        playerTwoPaddle.lockHorizontalPosition(playerTwoPaddle.getX());
        playerTwoPaddle.setVerticalMovementBounds(fieldTop + 24, fieldBottom - 24);

        ball = new Ball((fieldLeft + fieldRight) / 2.0, (fieldTop + fieldBottom) / 2.0,
                Constants.BALL_RADIUS, Constants.DEFAULT_SPEED, 1, 0);
        ball.clearTrail();

        spawnInitialBricks();

        playerOneBars.clear();
        playerTwoBars.clear();
        createDefenseBars(playerOneBars);
        createDefenseBars(playerTwoBars);
        updateLivesFromBars();

        playerOneScore.set(0);
        playerTwoScore.set(0);
        matchTimeSeconds.set(0);
        servingPlayer.set(ServingPlayer.PLAYER_ONE);
        lastHitter = ServingPlayer.PLAYER_ONE;
        ballLaunched = false;
        matchOver = false;
        winnerMessage = "";

        attachBallToServer();
    }

    private void createDefenseBars(List<DefenseBar> target) {
        target.clear();
        for (int i = 0; i < Constants.START_LIVES; i++) {
            target.add(new DefenseBar());
        }
    }
    /**
     * Kích hoạt chế độ Battle.
     * Phương thức này đăng ký các bộ xử lý sự kiện bàn phím vào Scene.
     * SẼ ĐƯỢC GỌI TỪ BÊN NGOÀI (ví dụ: từ GameScreen).
     * @param scene Scene chính của game.
     */
    public void activate(Scene scene) {
        System.out.println("Activating Battle Mode Key Handlers...");

        // Tạo các bộ xử lý sự kiện (chỉ một lần)
        if (keyPressedHandler == null) {
            keyPressedHandler = event -> {
                // Logic khi phím được nhấn
                switch (event.getCode()) {
                    case W:
                        setPlayerOneVelocity(-Constants.PADDLE_SPEED);
                        break;
                    case S:
                        setPlayerOneVelocity(Constants.PADDLE_SPEED);
                        break;
                    case UP:
                        setPlayerTwoVelocity(-Constants.PADDLE_SPEED);
                        break;
                    case DOWN:
                        setPlayerTwoVelocity(Constants.PADDLE_SPEED);
                        break;
                    case SPACE:
                        launchBall();
                        break;
                    case ENTER:
                        if (isMatchOver()) {
                            startMatch();
                        }
                        break;
                }
            };
        }

        if (keyReleasedHandler == null) {
            keyReleasedHandler = event -> {
                // Logic khi phím được nhả ra
                switch (event.getCode()) {
                    case W:
                    case S:
                        setPlayerOneVelocity(0);
                        break;
                    case UP:
                    case DOWN:
                        setPlayerTwoVelocity(0);
                        break;
                }
            };
        }

        // Gắn các bộ xử lý vào Scene
        scene.setOnKeyPressed(keyPressedHandler);
        scene.setOnKeyReleased(keyReleasedHandler);
    }

    /**
     * Hủy kích hoạt chế độ Battle.
     * Phương thức này gỡ bỏ các bộ xử lý sự kiện khỏi Scene.
     * SẼ ĐƯỢỢC GỌI TỪ BÊN NGOÀI (ví dụ: từ GameScreen).
     * @param scene Scene chính của game.
     */
    public void deactivate(Scene scene) {
        System.out.println("Deactivating Battle Mode Key Handlers...");
        // Gỡ bỏ bằng cách set chúng về null
        scene.setOnKeyPressed(null);
        scene.setOnKeyReleased(null);
    }

    private void spawnInitialBricks() {
        battleBricks.clear();
        final int brickDisplayWidth = Constants.BRICK_HEIGHT;
        final int brickDisplayHeight = Constants.BRICK_WIDTH;
        int rows = 7;
        int columns = 4;
        double brickSpawnChance = 0.3;

        double totalWidth = columns * brickDisplayWidth + (columns - 1) * Constants.BRICK_PADDING_X;
        double totalHeight = rows * brickDisplayHeight + (rows - 1) * Constants.BRICK_PADDING_Y;
        double startX = (fieldLeft + fieldRight) / 2.0 - totalWidth / 2.0;
        double startY = (fieldTop + fieldBottom) / 2.0 - totalHeight / 2.0;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                if (random.nextDouble() < brickSpawnChance) {

                    double x = startX + col * (brickDisplayWidth + Constants.BRICK_PADDING_X);
                    double y = startY + row * (brickDisplayHeight + Constants.BRICK_PADDING_Y);

                    battleBricks.add(new BattleBrick(x, y, brickDisplayWidth, brickDisplayHeight, 90));
                }
            }
        }
    }

    public void update(double dt) {
        if (matchOver) return;
        matchTimeSeconds.set(matchTimeSeconds.get() + dt);

        if (playerOnePaddle != null) playerOnePaddle.update(dt);
        if (playerTwoPaddle != null) playerTwoPaddle.update(dt);

        if (!ballLaunched) {
            attachBallToServer();
            return;
        }

        ball.update(dt);
        handlePaddleCollisions();
        handleBrickCollisions();
        keepBallInsideVerticalBounds();
        handleBoundaryCollisions();
    }

    private void keepBallInsideVerticalBounds() {
        if (ball.getY() + ball.getHeight() >= fieldBottom) {
            ball.setY(fieldBottom - ball.getHeight() - 1);
            ball.setDirection(ball.getDx(), -Math.abs(ball.getDy()));
        } else if (ball.getY() <= fieldTop) {
            ball.setY(fieldTop + 1);
            ball.setDirection(ball.getDx(), Math.abs(ball.getDy()));
        }
    }

    private void handlePaddleCollisions() {
        if (playerOnePaddle != null && ball.istersected(playerOnePaddle) && ball.getDx() < 0) {
            bounceFromPaddle(playerOnePaddle, true);
            lastHitter = ServingPlayer.PLAYER_ONE;
        }
        if (playerTwoPaddle != null && ball.istersected(playerTwoPaddle) && ball.getDx() > 0) {
            bounceFromPaddle(playerTwoPaddle, false);
            lastHitter = ServingPlayer.PLAYER_TWO;
        }
    }

    private void handleBrickCollisions() {
        Iterator<BattleBrick> iterator = battleBricks.iterator();
        while (iterator.hasNext()) {
            BattleBrick battleBrick = iterator.next();
            if (battleBrick.isDestroyed) {
                iterator.remove();
                continue;
            }

            if (ball.istersected(battleBrick.collisionBox)) {
                double ballCenterX = ball.getX() + ball.getRadius();
                double ballCenterY = ball.getY() + ball.getRadius();
                double brickCenterX = battleBrick.collisionBox.getX() + battleBrick.collisionBox.getWidth() / 2;
                double brickCenterY = battleBrick.collisionBox.getY() + battleBrick.collisionBox.getHeight() / 2;
                double overlapX = (ball.getRadius() + battleBrick.collisionBox.getWidth() / 2) - Math.abs(ballCenterX - brickCenterX);
                double overlapY = (ball.getRadius() + battleBrick.collisionBox.getHeight() / 2) - Math.abs(ballCenterY - brickCenterY);

                if (overlapX < overlapY) ball.setDirection(-ball.getDx(), ball.getDy());
                else ball.setDirection(ball.getDx(), -ball.getDy());

                battleBrick.isDestroyed = true;
                soundManager.play("break");
                iterator.remove();

                if (lastHitter == ServingPlayer.PLAYER_ONE) playerOneScore.set(playerOneScore.get() + 10);
                else playerTwoScore.set(playerTwoScore.get() + 10);

                if (battleBricks.isEmpty()) spawnInitialBricks();
                break;
            }
        }
    }

    private void handleBoundaryCollisions() {
        if (ball.getDx() < 0 && ball.getX() <= fieldLeft) {
            destroyBar(findNextActiveBar(playerOneBars), ServingPlayer.PLAYER_ONE);
        } else if (ball.getDx() > 0 && ball.getX() + ball.getWidth() >= fieldRight) {
            destroyBar(findNextActiveBar(playerTwoBars), ServingPlayer.PLAYER_TWO);
        }
    }

    private DefenseBar findNextActiveBar(List<DefenseBar> bars) {
        for (DefenseBar bar : bars) if (!bar.isDestroyed()) return bar;
        return null;
    }

    private void destroyBar(DefenseBar bar, ServingPlayer player) {
        if (bar == null || bar.isDestroyed()) return;
        bar.destroy();
        soundManager.play("break");
        soundManager.play("lose_life");
        updateLivesFromBars();
        spawnInitialBricks();

        if (player == ServingPlayer.PLAYER_ONE) {
            if (playerOneLives.get() <= 0) {
                endMatch(ServingPlayer.PLAYER_TWO, "Player 2 wins the battle!");
                return;
            }
            stateManager.setStatusMessage(String.format("Player 1 lost a shield! %d remaining.", playerOneLives.get()));
            ball.setX(fieldLeft + BOUNDARY_PADDING);
            ball.setDirection(Math.max(0.25, Math.abs(ball.getDx())), ball.getDy());
        } else {
            if (playerTwoLives.get() <= 0) {
                endMatch(ServingPlayer.PLAYER_ONE, "Player 1 wins the battle!");
                return;
            }
            stateManager.setStatusMessage(String.format("Player 2 lost a shield! %d remaining.", playerTwoLives.get()));
            ball.setX(fieldRight - ball.getWidth() - BOUNDARY_PADDING);
            ball.setDirection(-Math.max(0.25, Math.abs(ball.getDx())), ball.getDy());
        }
    }

    private void bounceFromPaddle(Paddle paddle, boolean sendRightward) {
        double paddleCenter = paddle.getY() + paddle.getHeight() / 2.0;
        double ballCenter = ball.getY() + ball.getHeight() / 2.0;
        double relativeIntersect = Math.max(-1, Math.min(1, (ballCenter - paddleCenter) / (paddle.getHeight() / 2.0)));
        double bounceAngle = relativeIntersect * Math.toRadians(60);
        double directionX = Math.cos(bounceAngle);
        double directionY = Math.sin(bounceAngle);

        if (sendRightward) {
            ball.setX(paddle.getX() + paddle.getWidth() + 1);
            ball.setDirection(Math.abs(directionX), directionY);
        } else {
            ball.setX(paddle.getX() - ball.getWidth() - 1);
            ball.setDirection(-Math.abs(directionX), directionY);
        }
        soundManager.play("bounce");
    }

    private void endMatch(ServingPlayer winner, String message) {
        matchOver = true;
        winnerMessage = message;
        ballLaunched = false;
        if (ball != null) ball.setVelocity(0, 0);

        if (winner == ServingPlayer.PLAYER_ONE) soundManager.play("battle_victory");
        else soundManager.play("battle_defeat");

        stateManager.markGameOver();
        stateManager.setStatusMessage(message + " Press ENTER to restart.");
        stopPlayers();
    }

    private void attachBallToServer() {
        if (ball == null) return;
        Paddle server = servingPlayer.get() == ServingPlayer.PLAYER_ONE ? playerOnePaddle : playerTwoPaddle;
        if (server == null) return;

        double ballY = server.getY() + server.getHeight() / 2.0 - ball.getHeight() / 2.0;
        double ballX = servingPlayer.get() == ServingPlayer.PLAYER_ONE
                ? server.getX() + server.getWidth() + 6
                : server.getX() - ball.getWidth() - 6;
        ball.setPosition(ballX, ballY);
        ball.setVelocity(0, 0);
    }

    public void render(GraphicsContext gc) {
        if (gc == null) return;
        gc.clearRect(0, 0, Constants.WIDTH, Constants.HEIGHT);

        double fieldWidth = fieldRight - fieldLeft;
        double fieldHeight = fieldBottom - fieldTop;
        gc.setFill(Color.color(0.08, 0.12, 0.28, 0.55));
        gc.fillRoundRect(fieldLeft, fieldTop, fieldWidth, fieldHeight, 30, 30);

        double centerX = (fieldLeft + fieldRight) / 2.0;
        gc.setStroke(Color.color(1, 1, 1, 0.25));
        gc.setLineWidth(2);
        gc.setLineDashes(18, 18);
        gc.strokeLine(centerX, fieldTop + 18, centerX, fieldBottom - 18);
        gc.setLineDashes(null);

        for (BattleBrick battleBrick : battleBricks) {
            if (battleBrick.isDestroyed) continue;
            gc.save();
            GameObject box = battleBrick.collisionBox;
            double x = box.getX();
            double y = box.getY();
            double textureWidth = Constants.BRICK_WIDTH;
            double textureHeight = Constants.BRICK_HEIGHT;
            double brickCenterX = x + box.getWidth() / 2.0;
            double brickCenterY = y + box.getHeight() / 2.0;

            Rotate r = new Rotate(battleBrick.rotationAngle, brickCenterX, brickCenterY);
            gc.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());

            double drawX = x - (textureWidth - box.getWidth()) / 2.0;
            double drawY = y - (textureHeight - box.getHeight()) / 2.0;

            if (this.brickTexture != null) {
                gc.drawImage(this.brickTexture, drawX, drawY, textureWidth, textureHeight);
            }
            gc.restore();
        }

        if (playerOnePaddle != null) playerOnePaddle.render(gc);
        if (playerTwoPaddle != null) playerTwoPaddle.render(gc);
        if (ball != null) ball.render(gc);
    }

    public void launchBall() {
        if (matchOver || ballLaunched) return;
        double verticalComponent = (random.nextDouble() * 1.4) - 0.7;
        if (Math.abs(verticalComponent) < 0.2) {
            verticalComponent = Math.copySign(0.2, verticalComponent == 0 ? random.nextDouble() - 0.5 : verticalComponent);
        }
        double horizontalComponent = servingPlayer.get() == ServingPlayer.PLAYER_ONE ? 1 : -1;
        ball.setDirection(horizontalComponent, verticalComponent);
        ballLaunched = true;
        stateManager.setStatusMessage("Battle on!");
    }

    public void stopPlayers() {
        if (playerOnePaddle != null) playerOnePaddle.setDy(0);
        if (playerTwoPaddle != null) playerTwoPaddle.setDy(0);
    }

    public void setPlayerOneVelocity(double dy) { if (playerOnePaddle != null) playerOnePaddle.setDy(dy); }
    public void setPlayerTwoVelocity(double dy) { if (playerTwoPaddle != null) playerTwoPaddle.setDy(dy); }

    private void updateLivesFromBars() {
        int p1 = (int) playerOneBars.stream().filter(b -> !b.isDestroyed()).count();
        int p2 = (int) playerTwoBars.stream().filter(b -> !b.isDestroyed()).count();
        playerOneLives.set(p1);
        playerTwoLives.set(p2);
    }

    // --- Getters for UI Binding ---
    public IntegerProperty playerOneLivesProperty() { return playerOneLives; }
    public IntegerProperty playerTwoLivesProperty() { return playerTwoLives; }
    public IntegerProperty playerOneScoreProperty() { return playerOneScore; }
    public IntegerProperty playerTwoScoreProperty() { return playerTwoScore; }
    public DoubleProperty matchTimeProperty() { return matchTimeSeconds; }
    public ObjectProperty<ServingPlayer> servingPlayerProperty() { return servingPlayer; }
    public boolean isMatchOver() { return matchOver; }
    public String getWinnerMessage() { return winnerMessage; }
    public Paddle getPlayerOnePaddle() { return playerOnePaddle; }
    public Paddle getPlayerTwoPaddle() { return playerTwoPaddle; }

    private static final class DefenseBar {
        private boolean destroyed = false;
        private void destroy() { destroyed = true; }
        private boolean isDestroyed() { return destroyed; }
    }
}
















//package com.ooparkanoid.core.engine;
//
//import com.ooparkanoid.core.state.GameStateManager;
//import com.ooparkanoid.object.Ball;
//import com.ooparkanoid.object.Paddle;
////import com.ooparkanoid.object.bricks.Brick;
////import com.ooparkanoid.object.bricks.NormalBrick;
//import com.ooparkanoid.sound.SoundManager;
//import com.ooparkanoid.utils.Constants;
////import com.ooparkanoid.graphics.ResourceManager;
//import javafx.beans.property.DoubleProperty;
//import javafx.beans.property.IntegerProperty;
//import javafx.beans.property.ObjectProperty;
//import javafx.beans.property.SimpleDoubleProperty;
//import javafx.beans.property.SimpleIntegerProperty;
//import javafx.beans.property.SimpleObjectProperty;
//import javafx.scene.canvas.GraphicsContext;
////import javafx.scene.image.Image;
//import javafx.scene.paint.Color;
////
////
////import javafx.scene.text.Font;
////import javafx.scene.text.FontWeight;
//import java.util.ArrayList;
////import java.util.Iterator;
//import java.util.List;
//import java.util.Random;
//
//import com.ooparkanoid.object.bricks.Brick;
//import com.ooparkanoid.object.bricks.NormalBrick;
//import com.ooparkanoid.graphics.ResourceManager;
//import javafx.scene.image.Image;
//import java.util.Iterator;
//import javafx.scene.transform.Rotate;
///**
// * Orchestrates the local 2-player battle mode.
// */
//public class LocalBattleManager {
//
//    public enum ServingPlayer {
//        PLAYER_ONE,
//        PLAYER_TWO
//    }
//    private final List<BattleBrick> battleBricks = new ArrayList<>();
//    private ServingPlayer lastHitter = ServingPlayer.PLAYER_ONE;
//    private final Image brickTexture;
//
//    private static final double FIELD_MARGIN_X = 100.0;
//    private static final double FIELD_MARGIN_Y = 70.0;
////    private static final double BAR_HEIGHT = 18.0;
////    private static final double BAR_GAP = 16.0;
////    private static final double BAR_GLOW_DURATION = 0.35;
////    private static final double BAR_SHAKE_DURATION = 0.28;
////    private static final double BAR_SHAKE_INTENSITY = 6.0;
//    private static final double PADDLE_OFFSET_X = 56.0;
//    private static final double PADDLE_HEIGHT = 160.0;
//    private static final double PADDLE_WIDTH = 28.0;
//    private static final double BOUNDARY_PADDING = 12.0;
//
//
//    private final GameStateManager stateManager;
//    private final Random random = new Random();
//    private final SoundManager soundManager = SoundManager.getInstance();
//
//    private Paddle playerOnePaddle;
//    private Paddle playerTwoPaddle;
//    private Ball ball;
////    private final List<Brick> bricks = new ArrayList<>();
//    private final List<DefenseBar> playerOneBars = new ArrayList<>();
//    private final List<DefenseBar> playerTwoBars = new ArrayList<>();
//
//
//    private final IntegerProperty playerOneLives = new SimpleIntegerProperty();
//    private final IntegerProperty playerTwoLives = new SimpleIntegerProperty();
//    private final IntegerProperty playerOneScore = new SimpleIntegerProperty();
//    private final IntegerProperty playerTwoScore = new SimpleIntegerProperty();
//    private final DoubleProperty matchTimeSeconds = new SimpleDoubleProperty();
//    private final ObjectProperty<ServingPlayer> servingPlayer =
//            new SimpleObjectProperty<>(ServingPlayer.PLAYER_ONE);
//
////    private ServingPlayer lastHitter = ServingPlayer.PLAYER_ONE;
//    private boolean ballLaunched = false;
//    private boolean matchOver = false;
//    private String winnerMessage = "";
//
////    private final Image brickTexture;
//    private double fieldLeft;
//    private double fieldRight;
//    private double fieldTop;
//    private double fieldBottom;
//    // --- BƯỚC 1: TẠO LỚP INNER CLASS ---
//    /**
//     * Một lớp container đơn giản để giữ một đối tượng Brick
//     * và thông tin về góc xoay của nó trong chế độ Battle.
//     */
//    private static class BattleBrick {
//        public Brick brick;
//        public double rotationAngle;
//
//        public BattleBrick(Brick brick, double rotationAngle) {
//            this.brick = brick;
//            this.rotationAngle = rotationAngle;
//        }
//    }
//
//
//    /**
//     * Tạo một cụm gạch ngẫu nhiên ở khu vực trung tâm của sân chơi.
//     */
//    private void spawnInitialBricks() {
//        battleBricks.clear(); // Xóa danh sách cũ
//
//        // Kích thước logic của gạch vẫn là 70x20
//        // Nhưng cách bố trí sẽ dựa trên kích thước hiển thị (xoay)
//        final int brickDisplayWidth = Constants.BRICK_HEIGHT;  // 20
//        final int brickDisplayHeight = Constants.BRICK_WIDTH;   // 70
//
//        int rows = 2;
//        int columns = 7;
//        double brickSpawnChance = 0.75;
//
//        // Tính toán bố cục dựa trên kích thước hiển thị
//        double totalWidth = columns * brickDisplayWidth + (columns - 1) * Constants.BRICK_PADDING_Y;
//        double totalHeight = rows * brickDisplayHeight + (rows - 1) * Constants.BRICK_PADDING_X;
//        double startX = (fieldLeft + fieldRight) / 2.0 - totalWidth / 2.0;
//        double startY = (fieldTop + fieldBottom) / 2.0 - totalHeight / 2.0;
//
//        for (int row = 0; row < rows; row++) {
//            for (int col = 0; col < columns; col++) {
//                if (random.nextDouble() < brickSpawnChance) {
//                    // Tọa độ thực của gạch không cần điều chỉnh phức tạp nữa
//                    // vì chúng ta sẽ xử lý việc xoay và tịnh tiến khi vẽ
//                    double x = startX + col * (brickDisplayWidth + Constants.BRICK_PADDING_Y);
//                    double y = startY + row * (brickDisplayHeight + Constants.BRICK_PADDING_X);
//
//                    NormalBrick brick = new NormalBrick(x, y);
//                    // QUAN TRỌNG: Ghi đè kích thước của đối tượng brick để va chạm chính xác hơn
//                    brick.setWidth(brickDisplayWidth);
//                    brick.setHeight(brickDisplayHeight);
//
//                    if (brickTexture != null) {
//                        brick.setTexture(brickTexture);
//                    }
//
//                    // Thêm một BattleBrick mới vào danh sách, chứa gạch và góc xoay 90 độ
//                    battleBricks.add(new BattleBrick(brick, 90));
//                }
//            }
//        }
//    }
//
//    public LocalBattleManager(GameStateManager stateManager) {
//        this.stateManager = stateManager;
////        ResourceManager resourceManager = ResourceManager.getInstance();
////        this.brickTexture = resourceManager.loadImage("brick_normal.png");
//        ResourceManager resourceManager = ResourceManager.getInstance();
//        this.brickTexture = resourceManager.loadImage("brick_normal.png");
//    }
//
//    public void startMatch() {
//        fieldLeft = FIELD_MARGIN_X;
//        fieldRight = Constants.WIDTH - FIELD_MARGIN_X;
//        fieldTop = FIELD_MARGIN_Y;
//        fieldBottom = Constants.HEIGHT - FIELD_MARGIN_Y;
//
////        double paddleStartX = Constants.PLAYFIELD_LEFT
////                + (Constants.PLAYFIELD_WIDTH - Constants.PADDLE_WIDTH) / 2.0;
//
//
////        double paddleStartX = fieldLeft + (fieldRight - fieldLeft - Constants.PADDLE_WIDTH) / 2.0;
//        double paddleStartY = (fieldTop + fieldBottom) / 2.0 - PADDLE_HEIGHT / 2.0;
//
////        playerOnePaddle = new Paddle(paddleStartX, Constants.HEIGHT - 60);
////        playerTwoPaddle = new Paddle(paddleStartX, 60);
////        playerOnePaddle = new Paddle(paddleStartX, fieldBottom - 60);
////        playerTwoPaddle = new Paddle(paddleStartX, fieldTop + 40);
//        playerOnePaddle = new Paddle(fieldLeft + PADDLE_OFFSET_X, paddleStartY);
//        playerOnePaddle.setWidth(PADDLE_WIDTH);
//        playerOnePaddle.setHeight(PADDLE_HEIGHT);
////        playerOnePaddle.setMovementBounds(playerOnePaddle.getX(), playerOnePaddle.getX());
//        playerOnePaddle.setOrientation(Paddle.Orientation.VERTICAL_LEFT);
//        playerOnePaddle.lockHorizontalPosition(playerOnePaddle.getX());
//        playerOnePaddle.setMovementBounds(playerOnePaddle.getX(),
//                playerOnePaddle.getX() + playerOnePaddle.getWidth());
//        playerOnePaddle.setVerticalMovementBounds(fieldTop + 24, fieldBottom - 24);
//
////        ball = new Ball(Constants.WIDTH / 2.0,
////                Constants.HEIGHT / 2.0,
////        playerOnePaddle.setMovementBounds(fieldLeft, fieldRight);
////        playerTwoPaddle.setMovementBounds(fieldLeft, fieldRight);
//        playerTwoPaddle = new Paddle(fieldRight - PADDLE_OFFSET_X - PADDLE_WIDTH, paddleStartY);
//        playerTwoPaddle.setWidth(PADDLE_WIDTH);
//        playerTwoPaddle.setHeight(PADDLE_HEIGHT);
////        playerTwoPaddle.setMovementBounds(playerTwoPaddle.getX(), playerTwoPaddle.getX());
//        playerTwoPaddle.setOrientation(Paddle.Orientation.VERTICAL_RIGHT);
//        playerTwoPaddle.lockHorizontalPosition(playerTwoPaddle.getX());
//        playerTwoPaddle.setMovementBounds(playerTwoPaddle.getX(),
//                playerTwoPaddle.getX() + playerTwoPaddle.getWidth());
//        playerTwoPaddle.setVerticalMovementBounds(fieldTop + 24, fieldBottom - 24);
//
//        ball = new Ball((fieldLeft + fieldRight) / 2.0,
//                (fieldTop + fieldBottom) / 2.0,
//                Constants.BALL_RADIUS,
//                Constants.DEFAULT_SPEED,
////                0,
////                -1);
//                1,
//                0);
//        ball.clearTrail();
//        bricks.clear();
//        spawnInitialBricks(); // Chúng ta sẽ tạo hàm này
//
//
//
////        playerOneLives.set(Constants.START_LIVES);
////        playerTwoLives.set(Constants.START_LIVES);
//        playerOneBars.clear();
//        playerTwoBars.clear();
////        createDefenseBars(playerOneBars, false);
////        createDefenseBars(playerTwoBars, true);
//        createDefenseBars(playerOneBars);
//        createDefenseBars(playerTwoBars);
//
//        updateLivesFromBars();
//        playerOneScore.set(0);
//        playerTwoScore.set(0);
//        matchTimeSeconds.set(0);
//
//        servingPlayer.set(ServingPlayer.PLAYER_ONE);
////        lastHitter = ServingPlayer.PLAYER_ONE;
//        ballLaunched = false;
//        matchOver = false;
//        winnerMessage = "";
//        ballLaunched = false;
//
////        bricks.clear();
////        spawnInitialBricks();
//        attachBallToServer();
//    }
//
////    private void spawnInitialBricks() {
////        int rows = 3;
////        int columns = Math.max(4, (int) Math.floor(
////                (Constants.PLAYFIELD_WIDTH - 120)
////                        / (Constants.BRICK_WIDTH + Constants.BRICK_PADDING_X)));
////
////        double totalWidth = columns * Constants.BRICK_WIDTH
////                + (columns - 1) * Constants.BRICK_PADDING_X;
////        double startX = Constants.PLAYFIELD_LEFT
////                + (Constants.PLAYFIELD_WIDTH - totalWidth) / 2.0;
////        double startY = Constants.HEIGHT / 2.0
////                - (rows / 2.0) * (Constants.BRICK_HEIGHT + Constants.BRICK_PADDING_Y);
////
////        for (int row = 0; row < rows; row++) {
////            double y = startY + row * (Constants.BRICK_HEIGHT + Constants.BRICK_PADDING_Y);
////            for (int col = 0; col < columns; col++) {
////                double x = startX + col * (Constants.BRICK_WIDTH + Constants.BRICK_PADDING_X);
////                NormalBrick brick = new NormalBrick(x, y);
////                if (brickTexture != null) {
////                    brick.setTexture(brickTexture);
////                }
////                bricks.add(brick);
////    private void createDefenseBars(List<DefenseBar> target, boolean topPlayer) {
////        double barWidth = fieldRight - fieldLeft;
//private void createDefenseBars(List<DefenseBar> target) {
//    target.clear();
//        for (int i = 0; i < Constants.START_LIVES; i++) {
////            double y;
////            if (topPlayer) {
////                y = fieldTop + i * (BAR_HEIGHT + BAR_GAP);
////            } else {
////                y = fieldBottom - (i + 1) * BAR_HEIGHT - i * BAR_GAP;
////            }
////            target.add(new DefenseBar(fieldLeft, y, barWidth, BAR_HEIGHT, topPlayer));
//            target.add(new DefenseBar());
//        }
//    }
//
//    public void update(double dt) {
//        if (matchOver) {
//            return;
//        }
//
//        matchTimeSeconds.set(matchTimeSeconds.get() + dt);
//
//        if (playerOnePaddle != null) {
//            playerOnePaddle.update(dt);
//        }
//        if (playerTwoPaddle != null) {
//            playerTwoPaddle.update(dt);
//        }
////        for (DefenseBar bar : playerOneBars) {
////            bar.update(dt);
////        }
////        for (DefenseBar bar : playerTwoBars) {
////            bar.update(dt);
////        }
//
//        if (!ballLaunched) {
//            attachBallToServer();
//            return;
//        }
//
//        ball.update(dt);
////        keepBallInsideHorizontalBounds();
//        handlePaddleCollisions();
////        handleBrickCollisions();
////        checkOutOfBounds();
////        handleDefenseBarCollisions();
//        handleBrickCollisions();
//        handleBoundaryCollisions();
//        keepBallInsideVerticalBounds();
////    }
//
////    private void keepBallInsideHorizontalBounds() {
//////        if (ball.getX() <= Constants.PLAYFIELD_LEFT) {
//////            ball.setX(Constants.PLAYFIELD_LEFT);
////        if (ball.getX() <= fieldLeft) {
////            ball.setX(fieldLeft);
////            ball.setDirection(Math.abs(ball.getDirX()), ball.getDirY());
//////        } else if (ball.getX() + ball.getWidth() >= Constants.PLAYFIELD_RIGHT) {
//////            ball.setX(Constants.PLAYFIELD_RIGHT - ball.getWidth());
////        } else if (ball.getX() + ball.getWidth() >= fieldRight) {
////            ball.setX(fieldRight - ball.getWidth());
////            ball.setDirection(-Math.abs(ball.getDirX()), ball.getDirY());
////        }
//        handleBoundaryCollisions();
//    }
//
//    private void keepBallInsideVerticalBounds() {
////        if (ball.getY() + ball.getHeight() >= Constants.HEIGHT) {
////            ball.setY(Constants.HEIGHT - ball.getHeight() - 1);
//        if (ball.getY() + ball.getHeight() >= fieldBottom) {
//            ball.setY(fieldBottom - ball.getHeight() - 1);
//            ball.setDirection(ball.getDirX(), -Math.abs(ball.getDirY()));
////        } else if (ball.getY() <= 0) {
////            ball.setY(1);
//        } else if (ball.getY() <= fieldTop) {
//            ball.setY(fieldTop + 1);
//            ball.setDirection(ball.getDirX(), Math.abs(ball.getDirY()));
//        }
//    }
//
//    private void handlePaddleCollisions() {
//        if (playerOnePaddle != null && ball.istersected(playerOnePaddle) && ball.getDx() < 0) {
//            bounceFromPaddle(playerOnePaddle, true);
//            lastHitter = ServingPlayer.PLAYER_ONE;
//        }
//        if (playerTwoPaddle != null && ball.istersected(playerTwoPaddle) && ball.getDx() > 0) {
//            bounceFromPaddle(playerTwoPaddle, false);
//            lastHitter = ServingPlayer.PLAYER_TWO;
//        }
//    }
//    /**
//     * Xử lý va chạm giữa bóng và các viên gạch.
//     */
//    private void handleBrickCollisions() {
//        Iterator<BattleBrick> iterator = battleBricks.iterator();
//        while (iterator.hasNext()) {
//            BattleBrick battleBrick = iterator.next();
//            Brick brick = battleBrick.brick; // Lấy gạch từ container
//
//            if (brick.isDestroyed()) {
//                iterator.remove();
//                continue;
//            }
//
//            if (ball.collidesWith(brick)) { // Va chạm vẫn kiểm tra với đối tượng brick
//                // ... (logic nảy bóng, cộng điểm không đổi) ...
//
//                if (brick.isDestroyed()) {
//                    // ...
//                    iterator.remove(); // Xóa BattleBrick khỏi danh sách
//                    // ...
//                }
//
//                if (battleBricks.isEmpty()) { // Kiểm tra danh sách mới
//                    spawnInitialBricks();
//                }
//
//                break;
//            }
//        }
//    }
//
//////    private void handleBrickCollisions() {
//////        Iterator<Brick> iterator = bricks.iterator();
//////        while (iterator.hasNext()) {
//////            Brick brick = iterator.next();
//////            if (brick.isDestroyed()) {
//////                iterator.remove();
//////                continue;
////private void handleDefenseBarCollisions() {
////    if (ball.getDy() > 0) {
////        DefenseBar hit = findCollidingBar(playerOneBars);
////        if (hit != null) {
////            destroyBar(hit, ServingPlayer.PLAYER_ONE);
////            return;
////        }
////        DefenseBar nearest = findNearestActiveBar(playerOneBars, false);
////        if (nearest != null && ball.getY() + ball.getHeight() >= nearest.getY()) {
////            destroyBar(nearest, ServingPlayer.PLAYER_ONE);
////        }
////    } else if (ball.getDy() < 0) {
////        DefenseBar hit = findCollidingBar(playerTwoBars);
////        if (hit != null) {
////            destroyBar(hit, ServingPlayer.PLAYER_TWO);
////            return;
////        }
////        DefenseBar nearest = findNearestActiveBar(playerTwoBars, true);
////        if (nearest != null && ball.getY() <= nearest.getY() + nearest.getHeight()) {
////            destroyBar(nearest, ServingPlayer.PLAYER_TWO);
//private void handleBoundaryCollisions() {
//    if (ball.getDx() < 0 && ball.getX() <= fieldLeft) {
//        DefenseBar next = findNextActiveBar(playerOneBars);
//        destroyBar(next, ServingPlayer.PLAYER_ONE);
//    } else if (ball.getDx() > 0 && ball.getX() + ball.getWidth() >= fieldRight) {
//        DefenseBar next = findNextActiveBar(playerTwoBars);
//        destroyBar(next, ServingPlayer.PLAYER_TWO);
//        }
//    }
////}
//
////            if (ball.collidesWith(brick)) {
////                String side = ball.getCollisionSide(brick);
////                switch (side) {
////                    case "LEFT" -> {
////                        ball.setX(brick.getX() - ball.getWidth() - 1);
////                        ball.setDirection(-Math.abs(ball.getDirX()), ball.getDirY());
////                    }
////                    case "RIGHT" -> {
////                        ball.setX(brick.getX() + brick.getWidth() + 1);
////                        ball.setDirection(Math.abs(ball.getDirX()), ball.getDirY());
////                    }
////                    case "TOP" -> {
////                        ball.setY(brick.getY() - ball.getHeight() - 1);
////                        ball.setDirection(ball.getDirX(), -Math.abs(ball.getDirY()));
////                    }
////                    case "BOTTOM" -> {
////                        ball.setY(brick.getY() + brick.getHeight() + 1);
////                        ball.setDirection(ball.getDirX(), Math.abs(ball.getDirY()));
////                    }
////                }
//
////    private DefenseBar findCollidingBar(List<DefenseBar> bars) {
//    private DefenseBar findNextActiveBar(List<DefenseBar> bars) {
//        for (DefenseBar bar : bars) {
////            if (bar.intersects(ball)) {
//            if (!bar.isDestroyed()) {
//                return bar;
//            }
//        }
//        return null;
//    }
////                brick.takeHit();
////                if (brick.isDestroyed()) {
////                    SoundManager.getInstance().play("break");
////                    iterator.remove();
////                    if (lastHitter == ServingPlayer.PLAYER_ONE) {
////                        playerOneScore.set(playerOneScore.get() + 1);
////                    } else {
////                        playerTwoScore.set(playerTwoScore.get() + 1);
////                    }
//
////    private DefenseBar findNearestActiveBar(List<DefenseBar> bars, boolean topPlayer) {
////        DefenseBar candidate = null;
////        for (DefenseBar bar : bars) {
////            if (bar.isDestroyed()) {
////                continue;
////            }
////            if (candidate == null) {
////                candidate = bar;
////                continue;
////            }
////            if (topPlayer) {
////                if (bar.getY() > candidate.getY()) {
////                    candidate = bar;
////                }
////            } else {
////                if (bar.getY() < candidate.getY()) {
////                    candidate = bar;
////                }
//////                break;
////            }
////        }
////        return candidate;
////    }
//
//    private void destroyBar(DefenseBar bar, ServingPlayer player) {
//        if (bar == null || bar.isDestroyed()) {
//            return;
//        }
//        bar.destroy();
//        soundManager.play("break");
//        soundManager.play("lose_life");
//
//        updateLivesFromBars();
//        spawnInitialBricks();
//
////        if (bricks.isEmpty()) {
////            spawnInitialBricks();
//        if (player == ServingPlayer.PLAYER_ONE) {
//            int remaining = playerOneLives.get();
//            if (remaining <= 0) {
//                endMatch(ServingPlayer.PLAYER_TWO, "Player 2 wins the battle!");
//                return;
//            }
//            stateManager.setStatusMessage(String.format("Player 1 lost a shield! %d remaining.", remaining));
////            ball.setDirection(ball.getDirX(), -Math.abs(ball.getDirY()));
////            ball.setY(bar.getY() - ball.getHeight() - 2);
//            double horizontal = Math.max(0.25, Math.abs(ball.getDirX()));
//            ball.setX(fieldLeft + BOUNDARY_PADDING);
//            ball.setDirection(horizontal, ball.getDirY());
//        } else {
//            int remaining = playerTwoLives.get();
//            if (remaining <= 0) {
//                endMatch(ServingPlayer.PLAYER_ONE, "Player 1 wins the battle!");
//                return;
//            }
//            stateManager.setStatusMessage(String.format("Player 2 lost a shield! %d remaining.", remaining));
////            ball.setDirection(ball.getDirX(), Math.abs(ball.getDirY()));
////            ball.setY(bar.getY() + bar.getHeight() + 2);
//            double horizontal = -Math.max(0.25, Math.abs(ball.getDirX()));
//            ball.setX(fieldRight - ball.getWidth() - BOUNDARY_PADDING);
//            ball.setDirection(horizontal, ball.getDirY());
//        }
//    }
//
////    private void bounceFromPaddle(Paddle paddle, boolean sendDownward) {
////        double paddleCenter = paddle.getX() + paddle.getWidth() / 2.0;
////        double ballCenter = ball.getX() + ball.getWidth() / 2.0;
////        double relativeIntersect = (ballCenter - paddleCenter) / (paddle.getWidth() / 2.0);
//    private void bounceFromPaddle(Paddle paddle, boolean sendRightward) {
//        double paddleCenter = paddle.getY() + paddle.getHeight() / 2.0;
//        double ballCenter = ball.getY() + ball.getHeight() / 2.0;
//        double relativeIntersect = (ballCenter - paddleCenter) / (paddle.getHeight() / 2.0);
//        relativeIntersect = Math.max(-1, Math.min(1, relativeIntersect));
//        double maxBounceAngle = Math.toRadians(60);
//        double bounceAngle = relativeIntersect * maxBounceAngle;
//
////        double directionX = Math.sin(bounceAngle);
////        double directionY = Math.cos(bounceAngle);
////        if (sendDownward) {
////            ball.setY(paddle.getY() + paddle.getHeight() + 1);
////            ball.setDirection(directionX, Math.abs(directionY));
//        double directionX = Math.cos(bounceAngle);
//        double directionY = Math.sin(bounceAngle);
//        if (sendRightward) {
//            ball.setX(paddle.getX() + paddle.getWidth() + 1);
//            ball.setDirection(Math.abs(directionX), directionY);
//        } else {
////            ball.setY(paddle.getY() - ball.getHeight() - 1);
////            ball.setDirection(directionX, -Math.abs(directionY));
//            ball.setX(paddle.getX() - ball.getWidth() - 1);
//            ball.setDirection(-Math.abs(directionX), directionY);
//        }
////        SoundManager.getInstance().play("bounce");
//        soundManager.play("bounce");
//    }
//
////    private void checkOutOfBounds() {
////        if (ball.getY() > Constants.HEIGHT) {
////            playerOneLostLife();
////        } else if (ball.getY() + ball.getHeight() < 0) {
////            playerTwoLostLife();
////        }
////    }
////
////    private void playerOneLostLife() {
////        playerOneLives.set(playerOneLives.get() - 1);
////        SoundManager.getInstance().play("lose_life");
////        if (playerOneLives.get() <= 0) {
////            endMatch("Player 2 wins the battle!");
////            return;
////        }
////        servingPlayer.set(ServingPlayer.PLAYER_ONE);
//    private void endMatch(ServingPlayer winner, String message) {
//        matchOver = true;
//        winnerMessage = message;
//        ballLaunched = false;
////        stateManager.setStatusMessage("Player 1 lost a life! Press SPACE to relaunch.");
////    }
//        if (ball != null) {
//            ball.setVelocity(0, 0);
//        }
//
////    private void playerTwoLostLife() {
////        playerTwoLives.set(playerTwoLives.get() - 1);
////        SoundManager.getInstance().play("lose_life");
////        if (playerTwoLives.get() <= 0) {
////            endMatch("Player 1 wins the battle!");
////            return;
//        if (winner == ServingPlayer.PLAYER_ONE) {
//            soundManager.play("battle_victory");
//            soundManager.play("battle_defeat");
//        } else {
//            soundManager.play("battle_defeat");
//            soundManager.play("battle_victory");
//        }
////        servingPlayer.set(ServingPlayer.PLAYER_TWO);
////        ballLaunched = false;
////        stateManager.setStatusMessage("Player 2 lost a life! Press SPACE to relaunch.");
////    }
//
////    private void endMatch(String message) {
////        matchOver = true;
////        winnerMessage = message;
////        ballLaunched = false;
////        stateManager.setStatusMessage(message + " Press ENTER to restart or F1 for Adventure.");
//        stateManager.markGameOver();
//        stateManager.setStatusMessage(message + " Press ENTER to restart.");
//        stopPlayers();
//    }
//
//    private void attachBallToServer() {
//        if (ball == null) {
//            return;
//        }
//        Paddle server = servingPlayer.get() == ServingPlayer.PLAYER_ONE
//                ? playerOnePaddle
//                : playerTwoPaddle;
//        if (server == null) {
//            return;
//        }
////        double ballX = server.getX() + server.getWidth() / 2.0 - ball.getWidth() / 2.0;
////        double ballY = servingPlayer.get() == ServingPlayer.PLAYER_ONE
////                ? server.getY() - ball.getHeight() - 4
////                : server.getY() + server.getHeight() + 4;
//        double ballY = server.getY() + server.getHeight() / 2.0 - ball.getHeight() / 2.0;
//        double ballX = servingPlayer.get() == ServingPlayer.PLAYER_ONE
//                ? server.getX() + server.getWidth() + 6
//                : server.getX() - ball.getWidth() - 6;
//        ball.setPosition(ballX, ballY);
//        ball.setVelocity(0, 0);
//    }
//
//    public void render(GraphicsContext gc) {
//        if (gc == null) {
//            return;
//        }
//        gc.clearRect(0, 0, Constants.WIDTH, Constants.HEIGHT);
//
//        double fieldWidth = fieldRight - fieldLeft;
//        double fieldHeight = fieldBottom - fieldTop;
//
//        gc.setFill(Color.color(0.08, 0.12, 0.28, 0.55));
//        gc.fillRoundRect(fieldLeft, fieldTop, fieldWidth, fieldHeight, 30, 30);
//
//        double centerX = (fieldLeft + fieldRight) / 2.0;
//        gc.setStroke(Color.color(1, 1, 1, 0.25));
//        gc.setLineWidth(2);
//////        gc.strokeLine(Constants.PLAYFIELD_LEFT,
//////                Constants.HEIGHT / 2.0,
//////                Constants.PLAYFIELD_RIGHT,
//////                Constants.HEIGHT / 2.0);
////        gc.strokeLine(fieldLeft, (fieldTop + fieldBottom) / 2.0,
////                fieldRight, (fieldTop + fieldBottom) / 2.0);
////
//////        for (Brick brick : bricks) {
//////            brick.render(gc);
////        for (DefenseBar bar : playerTwoBars) {
////            bar.render(gc);
////        }
//
//        for (BattleBrick battleBrick : battleBricks) {
//            Brick brick = battleBrick.brick;
//            if (brick.isDestroyed()) {
//                continue;
//            }
//
//            gc.save(); // Lưu trạng thái bút vẽ
//
//            // Lấy các thông số của viên gạch
//            double x = brick.getX();
//            double y = brick.getY();
//            // LƯU Ý: Lấy kích thước thật của texture, không phải kích thước đã xoay
//            double textureWidth = Constants.BRICK_WIDTH;
//            double textureHeight = Constants.BRICK_HEIGHT;
//
//            // Tính tâm xoay. Tâm sẽ là tâm của "vùng hiển thị"
//            double centerX = x + brick.getWidth() / 2.0;
//            double centerY = y + brick.getHeight() / 2.0;
//
//            // Tạo phép xoay
//            Rotate r = new Rotate(battleBrick.rotationAngle, centerX, centerY);
//            gc.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());
//
//            // Vẽ texture với kích thước GỐC để không bị méo
//            // Nhưng vẽ tại vị trí đã được tính toán cho bố cục xoay
//            // Cần một chút điều chỉnh để tâm của texture khớp với tâm của vùng hiển thị
//            double drawX = x - (textureWidth - brick.getWidth()) / 2.0;
//            double drawY = y - (textureHeight - brick.getHeight()) / 2.0;
//
//            Image texture = brick.getTexture(); // Giả sử có getter getTexture()
//            if (texture != null) {
//                gc.drawImage(texture, drawX, drawY, textureWidth, textureHeight);
//            }
//
//            gc.restore(); // Khôi phục lại bút vẽ về trạng thái ban đầu
//        }
//        gc.setLineDashes(18, 18);
//        gc.strokeLine(centerX, fieldTop + 18, centerX, fieldBottom - 18);
//        gc.setLineDashes(null);
//
//        for (Brick brick : bricks) {
//            brick.render(gc);
//        }
//
//        if (playerOnePaddle != null) {
//            playerOnePaddle.render(gc);
//        }
//        if (playerTwoPaddle != null) {
//            playerTwoPaddle.render(gc);
//        }
//
//
////        for (DefenseBar bar : playerOneBars) {
////            bar.render(gc);
////        }
//
//        if (ball != null) {
//            ball.render(gc);
//        }
////        gc.setFill(Color.WHITE);
////        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
////        gc.fillText("P2 Shields: " + playerTwoLives.get(), fieldLeft + 20, fieldTop - 20);
////        gc.fillText("P1 Shields: " + playerOneLives.get(), fieldLeft + 20, fieldBottom + 40);
//    }
//
//    public void launchBall() {
//        if (matchOver || ballLaunched) {
//            return;
//        }
////        double horizontalComponent = (random.nextDouble() * 1.4) - 0.7;
////        if (Math.abs(horizontalComponent) < 0.2) {
////            horizontalComponent = Math.copySign(0.2, horizontalComponent == 0
//        double verticalComponent = (random.nextDouble() * 1.4) - 0.7;
//        if (Math.abs(verticalComponent) < 0.2) {
//            verticalComponent = Math.copySign(0.2, verticalComponent == 0
//                    ? random.nextDouble() - 0.5
////                    : horizontalComponent);
//                    : verticalComponent);
//        }
////        double verticalComponent = servingPlayer.get() == ServingPlayer.PLAYER_ONE ? -1 : 1;
//        double horizontalComponent = servingPlayer.get() == ServingPlayer.PLAYER_ONE ? 1 : -1;
//        ball.setDirection(horizontalComponent, verticalComponent);
////        lastHitter = servingPlayer.get();
//
//        ballLaunched = true;
//        stateManager.setStatusMessage("Battle on!");
//    }
//
//    public void stopPlayers() {
//        if (playerOnePaddle != null) {
//            playerOnePaddle.setDx(0);
//            playerOnePaddle.setDy(0);
//        }
//        if (playerTwoPaddle != null) {
//            playerTwoPaddle.setDx(0);
//            playerTwoPaddle.setDy(0);
//        }
//    }
//
////    public void setPlayerOneVelocity(double dx) {
//    public void setPlayerOneVelocity(double dy) {
//        if (playerOnePaddle != null) {
//            playerOnePaddle.setDy(dy);
//        }
//    }
//
////    public void setPlayerTwoVelocity(double dx) {
//    public void setPlayerTwoVelocity(double dy) {
//        if (playerTwoPaddle != null) {
//            playerTwoPaddle.setDy(dy);
//        }
//    }
//
//    private void updateLivesFromBars() {
//        int p1 = 0;
//        for (DefenseBar bar : playerOneBars) {
//            if (!bar.isDestroyed()) {
//                p1++;
//            }
//        }
//        int p2 = 0;
//        for (DefenseBar bar : playerTwoBars) {
//            if (!bar.isDestroyed()) {
//                p2++;
//            }
//        }
//        playerOneLives.set(p1);
//        playerTwoLives.set(p2);
//    }
//
//    public IntegerProperty playerOneLivesProperty() {
//        return playerOneLives;
//    }
//
//    public IntegerProperty playerTwoLivesProperty() {
//        return playerTwoLives;
//    }
//
//    public IntegerProperty playerOneScoreProperty() {
//        return playerOneScore;
//    }
//
//    public IntegerProperty playerTwoScoreProperty() {
//        return playerTwoScore;
//    }
//
//    public DoubleProperty matchTimeProperty() {
//        return matchTimeSeconds;
//    }
//
//    public ObjectProperty<ServingPlayer> servingPlayerProperty() {
//        return servingPlayer;
//    }
//
//    public boolean isMatchOver() {
//        return matchOver;
//    }
//
//    public String getWinnerMessage() {
//        return winnerMessage;
//    }
//
//    public Paddle getPlayerOnePaddle() {
//        return playerOnePaddle;
//    }
//
//    public Paddle getPlayerTwoPaddle() {
//        return playerTwoPaddle;
//    }
//
//    private static final class DefenseBar {
////        private final double x;
////        private final double width;
////        private final double height;
////        private final boolean topPlayer;
////        private double y;
//        private boolean destroyed = false;
//////        private double glowTimer = 0;
//////        private double shakeTimer = 0;
//
//
////        private DefenseBar(double x, double y, double width, double height, boolean topPlayer) {
////            this.x = x;
////            this.y = y;
////            this.width = width;
////            this.height = height;
////            this.topPlayer = topPlayer;
////        }
////
////        private void update(double dt) {
////            if (glowTimer > 0) {
////                glowTimer = Math.max(0, glowTimer - dt);
////            }
////            if (shakeTimer > 0) {
////                shakeTimer = Math.max(0, shakeTimer - dt);
////            }
////        }
////
////        private boolean intersects(Ball ball) {
////            if (destroyed) {
////                return false;
////            }
////            return ball.getX() < x + width && ball.getX() + ball.getWidth() > x
////                    && ball.getY() < y + height && ball.getY() + ball.getHeight() > y;
////        }
//
//        private void destroy() {
//            destroyed = true;
////            glowTimer = BAR_GLOW_DURATION;
////            shakeTimer = BAR_SHAKE_DURATION;
//        }
//
//        private boolean isDestroyed() {
//            return destroyed;
//        }
//
////        private double getY() {
////            return y;
////        }
////
////        private double getHeight() {
////            return height;
////        }
////
////        private void render(GraphicsContext gc) {
////            if (destroyed && glowTimer <= 0) {
////                return;
////            }
////
////            double offset = 0;
////            if (shakeTimer > 0) {
////                double progress = 1.0 - (shakeTimer / BAR_SHAKE_DURATION);
////                double oscillation = Math.sin(progress * Math.PI * 6);
////                offset = oscillation * BAR_SHAKE_INTENSITY;
////                if (!topPlayer) {
////                    offset = -offset;
////                }
////            }
////
////            Color baseColor = topPlayer ? Color.web("#3FA9F5") : Color.web("#FF6F61");
////
////            gc.save();
////            gc.translate(0, offset);
////
////            if (!destroyed) {
////                gc.setFill(baseColor.deriveColor(0, 1, 1, 0.9));
////                gc.fillRoundRect(x, y, width, height, 16, 16);
////                gc.setGlobalAlpha(0.35);
////                gc.setFill(Color.WHITE);
////                double highlightY = topPlayer ? y : y + height * 0.55;
////                gc.fillRoundRect(x, highlightY, width, height * 0.45, 14, 14);
////            } else {
////                double alpha = Math.max(0, glowTimer / BAR_GLOW_DURATION);
////                gc.setGlobalAlpha(alpha);
////                gc.setFill(baseColor.brighter());
////                gc.fillRoundRect(x, y, width, height, 20, 20);
////                gc.setGlobalAlpha(alpha * 0.6);
////                gc.setFill(Color.WHITE);
////                gc.fillRoundRect(x - 6, y - 4, width + 12, height + 8, 24, 24);
////            }
////
////            gc.restore();
////            gc.setGlobalAlpha(1.0);
////        }
//    }
//}
