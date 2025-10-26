package com.ooparkanoid.core.engine;

import com.ooparkanoid.core.state.GameStateManager;
import com.ooparkanoid.object.Ball;
import com.ooparkanoid.object.Paddle;
//import com.ooparkanoid.object.bricks.Brick;
//import com.ooparkanoid.object.bricks.NormalBrick;
import com.ooparkanoid.sound.SoundManager;
import com.ooparkanoid.utils.Constants;
//import com.ooparkanoid.graphics.ResourceManager;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.GraphicsContext;
//import javafx.scene.image.Image;
import javafx.scene.paint.Color;
//
//
//import javafx.scene.text.Font;
//import javafx.scene.text.FontWeight;
import java.util.ArrayList;
//import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Orchestrates the local 2-player battle mode.
 */
public class LocalBattleManager {

    public enum ServingPlayer {
        PLAYER_ONE,
        PLAYER_TWO
    }

    private static final double FIELD_MARGIN_X = 120.0;
    private static final double FIELD_MARGIN_Y = 90.0;
//    private static final double BAR_HEIGHT = 18.0;
//    private static final double BAR_GAP = 16.0;
//    private static final double BAR_GLOW_DURATION = 0.35;
//    private static final double BAR_SHAKE_DURATION = 0.28;
//    private static final double BAR_SHAKE_INTENSITY = 6.0;
    private static final double PADDLE_OFFSET_X = 56.0;
    private static final double PADDLE_HEIGHT = 160.0;
    private static final double PADDLE_WIDTH = 28.0;
    private static final double BOUNDARY_PADDING = 12.0;


    private final GameStateManager stateManager;
    private final Random random = new Random();
    private final SoundManager soundManager = SoundManager.getInstance();

    private Paddle playerOnePaddle;
    private Paddle playerTwoPaddle;
    private Ball ball;
//    private final List<Brick> bricks = new ArrayList<>();
    private final List<DefenseBar> playerOneBars = new ArrayList<>();
    private final List<DefenseBar> playerTwoBars = new ArrayList<>();


    private final IntegerProperty playerOneLives = new SimpleIntegerProperty();
    private final IntegerProperty playerTwoLives = new SimpleIntegerProperty();
    private final IntegerProperty playerOneScore = new SimpleIntegerProperty();
    private final IntegerProperty playerTwoScore = new SimpleIntegerProperty();
    private final DoubleProperty matchTimeSeconds = new SimpleDoubleProperty();
    private final ObjectProperty<ServingPlayer> servingPlayer =
            new SimpleObjectProperty<>(ServingPlayer.PLAYER_ONE);

//    private ServingPlayer lastHitter = ServingPlayer.PLAYER_ONE;
    private boolean ballLaunched = false;
    private boolean matchOver = false;
    private String winnerMessage = "";

//    private final Image brickTexture;
    private double fieldLeft;
    private double fieldRight;
    private double fieldTop;
    private double fieldBottom;

    public LocalBattleManager(GameStateManager stateManager) {
        this.stateManager = stateManager;
//        ResourceManager resourceManager = ResourceManager.getInstance();
//        this.brickTexture = resourceManager.loadImage("brick_normal.png");
    }

    public void startMatch() {
//        double paddleStartX = Constants.PLAYFIELD_LEFT
//                + (Constants.PLAYFIELD_WIDTH - Constants.PADDLE_WIDTH) / 2.0;
        fieldLeft = FIELD_MARGIN_X;
        fieldRight = Constants.WIDTH - FIELD_MARGIN_X;
        fieldTop = FIELD_MARGIN_Y;
        fieldBottom = Constants.HEIGHT - FIELD_MARGIN_Y;

//        double paddleStartX = fieldLeft + (fieldRight - fieldLeft - Constants.PADDLE_WIDTH) / 2.0;
        double paddleStartY = (fieldTop + fieldBottom) / 2.0 - PADDLE_HEIGHT / 2.0;

//        playerOnePaddle = new Paddle(paddleStartX, Constants.HEIGHT - 60);
//        playerTwoPaddle = new Paddle(paddleStartX, 60);
//        playerOnePaddle = new Paddle(paddleStartX, fieldBottom - 60);
//        playerTwoPaddle = new Paddle(paddleStartX, fieldTop + 40);
        playerOnePaddle = new Paddle(fieldLeft + PADDLE_OFFSET_X, paddleStartY);
        playerOnePaddle.setWidth(PADDLE_WIDTH);
        playerOnePaddle.setHeight(PADDLE_HEIGHT);
        playerOnePaddle.setMovementBounds(playerOnePaddle.getX(), playerOnePaddle.getX());
        playerOnePaddle.setVerticalMovementBounds(fieldTop + 24, fieldBottom - 24);

//        ball = new Ball(Constants.WIDTH / 2.0,
//                Constants.HEIGHT / 2.0,
//        playerOnePaddle.setMovementBounds(fieldLeft, fieldRight);
//        playerTwoPaddle.setMovementBounds(fieldLeft, fieldRight);
        playerTwoPaddle = new Paddle(fieldRight - PADDLE_OFFSET_X - PADDLE_WIDTH, paddleStartY);
        playerTwoPaddle.setWidth(PADDLE_WIDTH);
        playerTwoPaddle.setHeight(PADDLE_HEIGHT);
        playerTwoPaddle.setMovementBounds(playerTwoPaddle.getX(), playerTwoPaddle.getX());
        playerTwoPaddle.setVerticalMovementBounds(fieldTop + 24, fieldBottom - 24);

        ball = new Ball((fieldLeft + fieldRight) / 2.0,
                (fieldTop + fieldBottom) / 2.0,
                Constants.BALL_RADIUS,
                Constants.DEFAULT_SPEED,
//                0,
//                -1);
                1,
                0);
        ball.clearTrail();

//        playerOneLives.set(Constants.START_LIVES);
//        playerTwoLives.set(Constants.START_LIVES);
        playerOneBars.clear();
        playerTwoBars.clear();
//        createDefenseBars(playerOneBars, false);
//        createDefenseBars(playerTwoBars, true);
        createDefenseBars(playerOneBars);
        createDefenseBars(playerTwoBars);

        updateLivesFromBars();
        playerOneScore.set(0);
        playerTwoScore.set(0);
        matchTimeSeconds.set(0);

        servingPlayer.set(ServingPlayer.PLAYER_ONE);
//        lastHitter = ServingPlayer.PLAYER_ONE;
        ballLaunched = false;
        matchOver = false;
        winnerMessage = "";

//        bricks.clear();
//        spawnInitialBricks();
        attachBallToServer();
    }

//    private void spawnInitialBricks() {
//        int rows = 3;
//        int columns = Math.max(4, (int) Math.floor(
//                (Constants.PLAYFIELD_WIDTH - 120)
//                        / (Constants.BRICK_WIDTH + Constants.BRICK_PADDING_X)));
//
//        double totalWidth = columns * Constants.BRICK_WIDTH
//                + (columns - 1) * Constants.BRICK_PADDING_X;
//        double startX = Constants.PLAYFIELD_LEFT
//                + (Constants.PLAYFIELD_WIDTH - totalWidth) / 2.0;
//        double startY = Constants.HEIGHT / 2.0
//                - (rows / 2.0) * (Constants.BRICK_HEIGHT + Constants.BRICK_PADDING_Y);
//
//        for (int row = 0; row < rows; row++) {
//            double y = startY + row * (Constants.BRICK_HEIGHT + Constants.BRICK_PADDING_Y);
//            for (int col = 0; col < columns; col++) {
//                double x = startX + col * (Constants.BRICK_WIDTH + Constants.BRICK_PADDING_X);
//                NormalBrick brick = new NormalBrick(x, y);
//                if (brickTexture != null) {
//                    brick.setTexture(brickTexture);
//                }
//                bricks.add(brick);
//    private void createDefenseBars(List<DefenseBar> target, boolean topPlayer) {
//        double barWidth = fieldRight - fieldLeft;
private void createDefenseBars(List<DefenseBar> target) {
    target.clear();
        for (int i = 0; i < Constants.START_LIVES; i++) {
//            double y;
//            if (topPlayer) {
//                y = fieldTop + i * (BAR_HEIGHT + BAR_GAP);
//            } else {
//                y = fieldBottom - (i + 1) * BAR_HEIGHT - i * BAR_GAP;
//            }
//            target.add(new DefenseBar(fieldLeft, y, barWidth, BAR_HEIGHT, topPlayer));
            target.add(new DefenseBar());
        }
    }

    public void update(double dt) {
        if (matchOver) {
            return;
        }

        matchTimeSeconds.set(matchTimeSeconds.get() + dt);

        if (playerOnePaddle != null) {
            playerOnePaddle.update(dt);
        }
        if (playerTwoPaddle != null) {
            playerTwoPaddle.update(dt);
        }
//        for (DefenseBar bar : playerOneBars) {
//            bar.update(dt);
//        }
//        for (DefenseBar bar : playerTwoBars) {
//            bar.update(dt);
//        }

        if (!ballLaunched) {
            attachBallToServer();
            return;
        }

        ball.update(dt);
//        keepBallInsideHorizontalBounds();
        handlePaddleCollisions();
//        handleBrickCollisions();
//        checkOutOfBounds();
//        handleDefenseBarCollisions();
        keepBallInsideVerticalBounds();
//    }

//    private void keepBallInsideHorizontalBounds() {
////        if (ball.getX() <= Constants.PLAYFIELD_LEFT) {
////            ball.setX(Constants.PLAYFIELD_LEFT);
//        if (ball.getX() <= fieldLeft) {
//            ball.setX(fieldLeft);
//            ball.setDirection(Math.abs(ball.getDirX()), ball.getDirY());
////        } else if (ball.getX() + ball.getWidth() >= Constants.PLAYFIELD_RIGHT) {
////            ball.setX(Constants.PLAYFIELD_RIGHT - ball.getWidth());
//        } else if (ball.getX() + ball.getWidth() >= fieldRight) {
//            ball.setX(fieldRight - ball.getWidth());
//            ball.setDirection(-Math.abs(ball.getDirX()), ball.getDirY());
//        }
        handleBoundaryCollisions();
    }

    private void keepBallInsideVerticalBounds() {
//        if (ball.getY() + ball.getHeight() >= Constants.HEIGHT) {
//            ball.setY(Constants.HEIGHT - ball.getHeight() - 1);
        if (ball.getY() + ball.getHeight() >= fieldBottom) {
            ball.setY(fieldBottom - ball.getHeight() - 1);
            ball.setDirection(ball.getDirX(), -Math.abs(ball.getDirY()));
//        } else if (ball.getY() <= 0) {
//            ball.setY(1);
        } else if (ball.getY() <= fieldTop) {
            ball.setY(fieldTop + 1);
            ball.setDirection(ball.getDirX(), Math.abs(ball.getDirY()));
        }
    }

    private void handlePaddleCollisions() {
//        if (playerOnePaddle != null && ball.istersected(playerOnePaddle) && ball.getDy() > 0) {
//            bounceFromPaddle(playerOnePaddle, false);
////            lastHitter = ServingPlayer.PLAYER_ONE;
        if (playerOnePaddle != null && ball.istersected(playerOnePaddle) && ball.getDx() < 0) {
            bounceFromPaddle(playerOnePaddle, true);
        }
//        if (playerTwoPaddle != null && ball.istersected(playerTwoPaddle) && ball.getDy() < 0) {
//            bounceFromPaddle(playerTwoPaddle, true);
////            lastHitter = ServingPlayer.PLAYER_TWO;
        if (playerTwoPaddle != null && ball.istersected(playerTwoPaddle) && ball.getDx() > 0) {
            bounceFromPaddle(playerTwoPaddle, false);
        }
    }

////    private void handleBrickCollisions() {
////        Iterator<Brick> iterator = bricks.iterator();
////        while (iterator.hasNext()) {
////            Brick brick = iterator.next();
////            if (brick.isDestroyed()) {
////                iterator.remove();
////                continue;
//private void handleDefenseBarCollisions() {
//    if (ball.getDy() > 0) {
//        DefenseBar hit = findCollidingBar(playerOneBars);
//        if (hit != null) {
//            destroyBar(hit, ServingPlayer.PLAYER_ONE);
//            return;
//        }
//        DefenseBar nearest = findNearestActiveBar(playerOneBars, false);
//        if (nearest != null && ball.getY() + ball.getHeight() >= nearest.getY()) {
//            destroyBar(nearest, ServingPlayer.PLAYER_ONE);
//        }
//    } else if (ball.getDy() < 0) {
//        DefenseBar hit = findCollidingBar(playerTwoBars);
//        if (hit != null) {
//            destroyBar(hit, ServingPlayer.PLAYER_TWO);
//            return;
//        }
//        DefenseBar nearest = findNearestActiveBar(playerTwoBars, true);
//        if (nearest != null && ball.getY() <= nearest.getY() + nearest.getHeight()) {
//            destroyBar(nearest, ServingPlayer.PLAYER_TWO);
private void handleBoundaryCollisions() {
    if (ball.getDx() < 0 && ball.getX() <= fieldLeft) {
        DefenseBar next = findNextActiveBar(playerOneBars);
        destroyBar(next, ServingPlayer.PLAYER_ONE);
    } else if (ball.getDx() > 0 && ball.getX() + ball.getWidth() >= fieldRight) {
        DefenseBar next = findNextActiveBar(playerTwoBars);
        destroyBar(next, ServingPlayer.PLAYER_TWO);
        }
    }
//}

//            if (ball.collidesWith(brick)) {
//                String side = ball.getCollisionSide(brick);
//                switch (side) {
//                    case "LEFT" -> {
//                        ball.setX(brick.getX() - ball.getWidth() - 1);
//                        ball.setDirection(-Math.abs(ball.getDirX()), ball.getDirY());
//                    }
//                    case "RIGHT" -> {
//                        ball.setX(brick.getX() + brick.getWidth() + 1);
//                        ball.setDirection(Math.abs(ball.getDirX()), ball.getDirY());
//                    }
//                    case "TOP" -> {
//                        ball.setY(brick.getY() - ball.getHeight() - 1);
//                        ball.setDirection(ball.getDirX(), -Math.abs(ball.getDirY()));
//                    }
//                    case "BOTTOM" -> {
//                        ball.setY(brick.getY() + brick.getHeight() + 1);
//                        ball.setDirection(ball.getDirX(), Math.abs(ball.getDirY()));
//                    }
//                }

//    private DefenseBar findCollidingBar(List<DefenseBar> bars) {
    private DefenseBar findNextActiveBar(List<DefenseBar> bars) {
        for (DefenseBar bar : bars) {
//            if (bar.intersects(ball)) {
            if (!bar.isDestroyed()) {
                return bar;
            }
        }
        return null;
    }
//                brick.takeHit();
//                if (brick.isDestroyed()) {
//                    SoundManager.getInstance().play("break");
//                    iterator.remove();
//                    if (lastHitter == ServingPlayer.PLAYER_ONE) {
//                        playerOneScore.set(playerOneScore.get() + 1);
//                    } else {
//                        playerTwoScore.set(playerTwoScore.get() + 1);
//                    }

//    private DefenseBar findNearestActiveBar(List<DefenseBar> bars, boolean topPlayer) {
//        DefenseBar candidate = null;
//        for (DefenseBar bar : bars) {
//            if (bar.isDestroyed()) {
//                continue;
//            }
//            if (candidate == null) {
//                candidate = bar;
//                continue;
//            }
//            if (topPlayer) {
//                if (bar.getY() > candidate.getY()) {
//                    candidate = bar;
//                }
//            } else {
//                if (bar.getY() < candidate.getY()) {
//                    candidate = bar;
//                }
////                break;
//            }
//        }
//        return candidate;
//    }

    private void destroyBar(DefenseBar bar, ServingPlayer player) {
        if (bar == null || bar.isDestroyed()) {
            return;
        }
        bar.destroy();
        soundManager.play("break");
        soundManager.play("lose_life");

        updateLivesFromBars();

//        if (bricks.isEmpty()) {
//            spawnInitialBricks();
        if (player == ServingPlayer.PLAYER_ONE) {
            int remaining = playerOneLives.get();
            if (remaining <= 0) {
                endMatch(ServingPlayer.PLAYER_TWO, "Player 2 wins the battle!");
                return;
            }
            stateManager.setStatusMessage(String.format("Player 1 lost a shield! %d remaining.", remaining));
//            ball.setDirection(ball.getDirX(), -Math.abs(ball.getDirY()));
//            ball.setY(bar.getY() - ball.getHeight() - 2);
            double horizontal = Math.max(0.25, Math.abs(ball.getDirX()));
            ball.setX(fieldLeft + BOUNDARY_PADDING);
            ball.setDirection(horizontal, ball.getDirY());
        } else {
            int remaining = playerTwoLives.get();
            if (remaining <= 0) {
                endMatch(ServingPlayer.PLAYER_ONE, "Player 1 wins the battle!");
                return;
            }
            stateManager.setStatusMessage(String.format("Player 2 lost a shield! %d remaining.", remaining));
//            ball.setDirection(ball.getDirX(), Math.abs(ball.getDirY()));
//            ball.setY(bar.getY() + bar.getHeight() + 2);
            double horizontal = -Math.max(0.25, Math.abs(ball.getDirX()));
            ball.setX(fieldRight - ball.getWidth() - BOUNDARY_PADDING);
            ball.setDirection(horizontal, ball.getDirY());
        }
    }

//    private void bounceFromPaddle(Paddle paddle, boolean sendDownward) {
//        double paddleCenter = paddle.getX() + paddle.getWidth() / 2.0;
//        double ballCenter = ball.getX() + ball.getWidth() / 2.0;
//        double relativeIntersect = (ballCenter - paddleCenter) / (paddle.getWidth() / 2.0);
    private void bounceFromPaddle(Paddle paddle, boolean sendRightward) {
        double paddleCenter = paddle.getY() + paddle.getHeight() / 2.0;
        double ballCenter = ball.getY() + ball.getHeight() / 2.0;
        double relativeIntersect = (ballCenter - paddleCenter) / (paddle.getHeight() / 2.0);
        relativeIntersect = Math.max(-1, Math.min(1, relativeIntersect));
        double maxBounceAngle = Math.toRadians(60);
        double bounceAngle = relativeIntersect * maxBounceAngle;

//        double directionX = Math.sin(bounceAngle);
//        double directionY = Math.cos(bounceAngle);
//        if (sendDownward) {
//            ball.setY(paddle.getY() + paddle.getHeight() + 1);
//            ball.setDirection(directionX, Math.abs(directionY));
        double directionX = Math.cos(bounceAngle);
        double directionY = Math.sin(bounceAngle);
        if (sendRightward) {
            ball.setX(paddle.getX() + paddle.getWidth() + 1);
            ball.setDirection(Math.abs(directionX), directionY);
        } else {
//            ball.setY(paddle.getY() - ball.getHeight() - 1);
//            ball.setDirection(directionX, -Math.abs(directionY));
            ball.setX(paddle.getX() - ball.getWidth() - 1);
            ball.setDirection(-Math.abs(directionX), directionY);
        }
//        SoundManager.getInstance().play("bounce");
        soundManager.play("bounce");
    }

//    private void checkOutOfBounds() {
//        if (ball.getY() > Constants.HEIGHT) {
//            playerOneLostLife();
//        } else if (ball.getY() + ball.getHeight() < 0) {
//            playerTwoLostLife();
//        }
//    }
//
//    private void playerOneLostLife() {
//        playerOneLives.set(playerOneLives.get() - 1);
//        SoundManager.getInstance().play("lose_life");
//        if (playerOneLives.get() <= 0) {
//            endMatch("Player 2 wins the battle!");
//            return;
//        }
//        servingPlayer.set(ServingPlayer.PLAYER_ONE);
    private void endMatch(ServingPlayer winner, String message) {
        matchOver = true;
        winnerMessage = message;
        ballLaunched = false;
//        stateManager.setStatusMessage("Player 1 lost a life! Press SPACE to relaunch.");
//    }
        if (ball != null) {
            ball.setVelocity(0, 0);
        }

//    private void playerTwoLostLife() {
//        playerTwoLives.set(playerTwoLives.get() - 1);
//        SoundManager.getInstance().play("lose_life");
//        if (playerTwoLives.get() <= 0) {
//            endMatch("Player 1 wins the battle!");
//            return;
        if (winner == ServingPlayer.PLAYER_ONE) {
            soundManager.play("battle_victory");
            soundManager.play("battle_defeat");
        } else {
            soundManager.play("battle_defeat");
            soundManager.play("battle_victory");
        }
//        servingPlayer.set(ServingPlayer.PLAYER_TWO);
//        ballLaunched = false;
//        stateManager.setStatusMessage("Player 2 lost a life! Press SPACE to relaunch.");
//    }

//    private void endMatch(String message) {
//        matchOver = true;
//        winnerMessage = message;
//        ballLaunched = false;
//        stateManager.setStatusMessage(message + " Press ENTER to restart or F1 for Adventure.");
        stateManager.markGameOver();
        stateManager.setStatusMessage(message + " Press ENTER to restart.");
        stopPlayers();
    }

    private void attachBallToServer() {
        if (ball == null) {
            return;
        }
        Paddle server = servingPlayer.get() == ServingPlayer.PLAYER_ONE
                ? playerOnePaddle
                : playerTwoPaddle;
        if (server == null) {
            return;
        }
//        double ballX = server.getX() + server.getWidth() / 2.0 - ball.getWidth() / 2.0;
//        double ballY = servingPlayer.get() == ServingPlayer.PLAYER_ONE
//                ? server.getY() - ball.getHeight() - 4
//                : server.getY() + server.getHeight() + 4;
        double ballY = server.getY() + server.getHeight() / 2.0 - ball.getHeight() / 2.0;
        double ballX = servingPlayer.get() == ServingPlayer.PLAYER_ONE
                ? server.getX() + server.getWidth() + 6
                : server.getX() - ball.getWidth() - 6;
        ball.setPosition(ballX, ballY);
        ball.setVelocity(0, 0);
    }

    public void render(GraphicsContext gc) {
        if (gc == null) {
            return;
        }
        gc.clearRect(0, 0, Constants.WIDTH, Constants.HEIGHT);

        double fieldWidth = fieldRight - fieldLeft;
        double fieldHeight = fieldBottom - fieldTop;

        gc.setFill(Color.color(0.08, 0.12, 0.28, 0.55));
        gc.fillRoundRect(fieldLeft, fieldTop, fieldWidth, fieldHeight, 30, 30);

        double centerX = (fieldLeft + fieldRight) / 2.0;
        gc.setStroke(Color.color(1, 1, 1, 0.25));
        gc.setLineWidth(2);
////        gc.strokeLine(Constants.PLAYFIELD_LEFT,
////                Constants.HEIGHT / 2.0,
////                Constants.PLAYFIELD_RIGHT,
////                Constants.HEIGHT / 2.0);
//        gc.strokeLine(fieldLeft, (fieldTop + fieldBottom) / 2.0,
//                fieldRight, (fieldTop + fieldBottom) / 2.0);
//
////        for (Brick brick : bricks) {
////            brick.render(gc);
//        for (DefenseBar bar : playerTwoBars) {
//            bar.render(gc);
//        }



//        for (DefenseBar bar : playerOneBars) {
//            bar.render(gc);
//        }

        gc.setLineDashes(18, 18);
        gc.strokeLine(centerX, fieldTop + 18, centerX, fieldBottom - 18);
        gc.setLineDashes(null);
        if (playerOnePaddle != null) {
            playerOnePaddle.render(gc);
        }
        if (playerTwoPaddle != null) {
            playerTwoPaddle.render(gc);
        }
        if (ball != null) {
            ball.render(gc);
        }

//        gc.setFill(Color.WHITE);
//        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
//        gc.fillText("P2 Shields: " + playerTwoLives.get(), fieldLeft + 20, fieldTop - 20);
//        gc.fillText("P1 Shields: " + playerOneLives.get(), fieldLeft + 20, fieldBottom + 40);
    }

    public void launchBall() {
        if (matchOver || ballLaunched) {
            return;
        }
//        double horizontalComponent = (random.nextDouble() * 1.4) - 0.7;
//        if (Math.abs(horizontalComponent) < 0.2) {
//            horizontalComponent = Math.copySign(0.2, horizontalComponent == 0
        double verticalComponent = (random.nextDouble() * 1.4) - 0.7;
        if (Math.abs(verticalComponent) < 0.2) {
            verticalComponent = Math.copySign(0.2, verticalComponent == 0
                    ? random.nextDouble() - 0.5
//                    : horizontalComponent);
                    : verticalComponent);
        }
//        double verticalComponent = servingPlayer.get() == ServingPlayer.PLAYER_ONE ? -1 : 1;
        double horizontalComponent = servingPlayer.get() == ServingPlayer.PLAYER_ONE ? 1 : -1;
        ball.setDirection(horizontalComponent, verticalComponent);
//        lastHitter = servingPlayer.get();

        ballLaunched = true;
        stateManager.setStatusMessage("Battle on!");
    }

    public void stopPlayers() {
        if (playerOnePaddle != null) {
            playerOnePaddle.setDx(0);
            playerOnePaddle.setDy(0);
        }
        if (playerTwoPaddle != null) {
            playerTwoPaddle.setDx(0);
            playerTwoPaddle.setDy(0);
        }
    }

//    public void setPlayerOneVelocity(double dx) {
    public void setPlayerOneVelocity(double dy) {
        if (playerOnePaddle != null) {
            playerOnePaddle.setDy(dy);
        }
    }

//    public void setPlayerTwoVelocity(double dx) {
    public void setPlayerTwoVelocity(double dy) {
        if (playerTwoPaddle != null) {
            playerTwoPaddle.setDy(dy);
        }
    }

    private void updateLivesFromBars() {
        int p1 = 0;
        for (DefenseBar bar : playerOneBars) {
            if (!bar.isDestroyed()) {
                p1++;
            }
        }
        int p2 = 0;
        for (DefenseBar bar : playerTwoBars) {
            if (!bar.isDestroyed()) {
                p2++;
            }
        }
        playerOneLives.set(p1);
        playerTwoLives.set(p2);
    }

    public IntegerProperty playerOneLivesProperty() {
        return playerOneLives;
    }

    public IntegerProperty playerTwoLivesProperty() {
        return playerTwoLives;
    }

    public IntegerProperty playerOneScoreProperty() {
        return playerOneScore;
    }

    public IntegerProperty playerTwoScoreProperty() {
        return playerTwoScore;
    }

    public DoubleProperty matchTimeProperty() {
        return matchTimeSeconds;
    }

    public ObjectProperty<ServingPlayer> servingPlayerProperty() {
        return servingPlayer;
    }

    public boolean isMatchOver() {
        return matchOver;
    }

    public String getWinnerMessage() {
        return winnerMessage;
    }

    public Paddle getPlayerOnePaddle() {
        return playerOnePaddle;
    }

    public Paddle getPlayerTwoPaddle() {
        return playerTwoPaddle;
    }

    private static final class DefenseBar {
//        private final double x;
//        private final double width;
//        private final double height;
//        private final boolean topPlayer;
//        private double y;
        private boolean destroyed = false;
////        private double glowTimer = 0;
////        private double shakeTimer = 0;


//        private DefenseBar(double x, double y, double width, double height, boolean topPlayer) {
//            this.x = x;
//            this.y = y;
//            this.width = width;
//            this.height = height;
//            this.topPlayer = topPlayer;
//        }
//
//        private void update(double dt) {
//            if (glowTimer > 0) {
//                glowTimer = Math.max(0, glowTimer - dt);
//            }
//            if (shakeTimer > 0) {
//                shakeTimer = Math.max(0, shakeTimer - dt);
//            }
//        }
//
//        private boolean intersects(Ball ball) {
//            if (destroyed) {
//                return false;
//            }
//            return ball.getX() < x + width && ball.getX() + ball.getWidth() > x
//                    && ball.getY() < y + height && ball.getY() + ball.getHeight() > y;
//        }

        private void destroy() {
            destroyed = true;
//            glowTimer = BAR_GLOW_DURATION;
//            shakeTimer = BAR_SHAKE_DURATION;
        }

        private boolean isDestroyed() {
            return destroyed;
        }

//        private double getY() {
//            return y;
//        }
//
//        private double getHeight() {
//            return height;
//        }
//
//        private void render(GraphicsContext gc) {
//            if (destroyed && glowTimer <= 0) {
//                return;
//            }
//
//            double offset = 0;
//            if (shakeTimer > 0) {
//                double progress = 1.0 - (shakeTimer / BAR_SHAKE_DURATION);
//                double oscillation = Math.sin(progress * Math.PI * 6);
//                offset = oscillation * BAR_SHAKE_INTENSITY;
//                if (!topPlayer) {
//                    offset = -offset;
//                }
//            }
//
//            Color baseColor = topPlayer ? Color.web("#3FA9F5") : Color.web("#FF6F61");
//
//            gc.save();
//            gc.translate(0, offset);
//
//            if (!destroyed) {
//                gc.setFill(baseColor.deriveColor(0, 1, 1, 0.9));
//                gc.fillRoundRect(x, y, width, height, 16, 16);
//                gc.setGlobalAlpha(0.35);
//                gc.setFill(Color.WHITE);
//                double highlightY = topPlayer ? y : y + height * 0.55;
//                gc.fillRoundRect(x, highlightY, width, height * 0.45, 14, 14);
//            } else {
//                double alpha = Math.max(0, glowTimer / BAR_GLOW_DURATION);
//                gc.setGlobalAlpha(alpha);
//                gc.setFill(baseColor.brighter());
//                gc.fillRoundRect(x, y, width, height, 20, 20);
//                gc.setGlobalAlpha(alpha * 0.6);
//                gc.setFill(Color.WHITE);
//                gc.fillRoundRect(x - 6, y - 4, width + 12, height + 8, 24, 24);
//            }
//
//            gc.restore();
//            gc.setGlobalAlpha(1.0);
//        }
    }
}