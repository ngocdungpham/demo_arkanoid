package com.ooparkanoid.core.engine;

import com.ooparkanoid.core.state.GameStateManager;
import com.ooparkanoid.object.Ball;
import com.ooparkanoid.object.Paddle;
import com.ooparkanoid.object.bricks.Brick;
import com.ooparkanoid.object.bricks.NormalBrick;
import com.ooparkanoid.sound.SoundManager;
import com.ooparkanoid.utils.Constants;
import com.ooparkanoid.graphics.ResourceManager;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Iterator;
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

    private final GameStateManager stateManager;
    private final Random random = new Random();

    private Paddle playerOnePaddle;
    private Paddle playerTwoPaddle;
    private Ball ball;
    private final List<Brick> bricks = new ArrayList<>();

    private final IntegerProperty playerOneLives = new SimpleIntegerProperty();
    private final IntegerProperty playerTwoLives = new SimpleIntegerProperty();
    private final IntegerProperty playerOneScore = new SimpleIntegerProperty();
    private final IntegerProperty playerTwoScore = new SimpleIntegerProperty();
    private final DoubleProperty matchTimeSeconds = new SimpleDoubleProperty();
    private final ObjectProperty<ServingPlayer> servingPlayer =
            new SimpleObjectProperty<>(ServingPlayer.PLAYER_ONE);

    private ServingPlayer lastHitter = ServingPlayer.PLAYER_ONE;
    private boolean ballLaunched = false;
    private boolean matchOver = false;
    private String winnerMessage = "";

    private final Image brickTexture;

    public LocalBattleManager(GameStateManager stateManager) {
        this.stateManager = stateManager;
        ResourceManager resourceManager = ResourceManager.getInstance();
        this.brickTexture = resourceManager.loadImage("brick_normal.png");
    }

    public void startMatch() {
        double paddleStartX = Constants.PLAYFIELD_LEFT
                + (Constants.PLAYFIELD_WIDTH - Constants.PADDLE_WIDTH) / 2.0;

        playerOnePaddle = new Paddle(paddleStartX, Constants.HEIGHT - 60);
        playerTwoPaddle = new Paddle(paddleStartX, 60);

        ball = new Ball(Constants.WIDTH / 2.0,
                Constants.HEIGHT / 2.0,
                Constants.BALL_RADIUS,
                Constants.DEFAULT_SPEED,
                0,
                -1);
        ball.clearTrail();

        playerOneLives.set(Constants.START_LIVES);
        playerTwoLives.set(Constants.START_LIVES);
        playerOneScore.set(0);
        playerTwoScore.set(0);
        matchTimeSeconds.set(0);

        servingPlayer.set(ServingPlayer.PLAYER_ONE);
        lastHitter = ServingPlayer.PLAYER_ONE;
        ballLaunched = false;
        matchOver = false;
        winnerMessage = "";

        bricks.clear();
        spawnInitialBricks();
        attachBallToServer();
    }

    private void spawnInitialBricks() {
        int rows = 3;
        int columns = Math.max(4, (int) Math.floor(
                (Constants.PLAYFIELD_WIDTH - 120)
                        / (Constants.BRICK_WIDTH + Constants.BRICK_PADDING_X)));

        double totalWidth = columns * Constants.BRICK_WIDTH
                + (columns - 1) * Constants.BRICK_PADDING_X;
        double startX = Constants.PLAYFIELD_LEFT
                + (Constants.PLAYFIELD_WIDTH - totalWidth) / 2.0;
        double startY = Constants.HEIGHT / 2.0
                - (rows / 2.0) * (Constants.BRICK_HEIGHT + Constants.BRICK_PADDING_Y);

        for (int row = 0; row < rows; row++) {
            double y = startY + row * (Constants.BRICK_HEIGHT + Constants.BRICK_PADDING_Y);
            for (int col = 0; col < columns; col++) {
                double x = startX + col * (Constants.BRICK_WIDTH + Constants.BRICK_PADDING_X);
                NormalBrick brick = new NormalBrick(x, y);
                if (brickTexture != null) {
                    brick.setTexture(brickTexture);
                }
                bricks.add(brick);
            }
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

        if (!ballLaunched) {
            attachBallToServer();
            return;
        }

        ball.update(dt);
        keepBallInsideHorizontalBounds();
        handlePaddleCollisions();
        handleBrickCollisions();
        checkOutOfBounds();
    }

    private void keepBallInsideHorizontalBounds() {
        if (ball.getX() <= Constants.PLAYFIELD_LEFT) {
            ball.setX(Constants.PLAYFIELD_LEFT);
            ball.setDirection(Math.abs(ball.getDirX()), ball.getDirY());
        } else if (ball.getX() + ball.getWidth() >= Constants.PLAYFIELD_RIGHT) {
            ball.setX(Constants.PLAYFIELD_RIGHT - ball.getWidth());
            ball.setDirection(-Math.abs(ball.getDirX()), ball.getDirY());
        }
    }

    private void handlePaddleCollisions() {
        if (playerOnePaddle != null && ball.istersected(playerOnePaddle) && ball.getDy() > 0) {
            bounceFromPaddle(playerOnePaddle, false);
            lastHitter = ServingPlayer.PLAYER_ONE;
        }
        if (playerTwoPaddle != null && ball.istersected(playerTwoPaddle) && ball.getDy() < 0) {
            bounceFromPaddle(playerTwoPaddle, true);
            lastHitter = ServingPlayer.PLAYER_TWO;
        }
    }

    private void handleBrickCollisions() {
        Iterator<Brick> iterator = bricks.iterator();
        while (iterator.hasNext()) {
            Brick brick = iterator.next();
            if (brick.isDestroyed()) {
                iterator.remove();
                continue;
            }

            if (ball.collidesWith(brick)) {
                String side = ball.getCollisionSide(brick);
                switch (side) {
                    case "LEFT" -> {
                        ball.setX(brick.getX() - ball.getWidth() - 1);
                        ball.setDirection(-Math.abs(ball.getDirX()), ball.getDirY());
                    }
                    case "RIGHT" -> {
                        ball.setX(brick.getX() + brick.getWidth() + 1);
                        ball.setDirection(Math.abs(ball.getDirX()), ball.getDirY());
                    }
                    case "TOP" -> {
                        ball.setY(brick.getY() - ball.getHeight() - 1);
                        ball.setDirection(ball.getDirX(), -Math.abs(ball.getDirY()));
                    }
                    case "BOTTOM" -> {
                        ball.setY(brick.getY() + brick.getHeight() + 1);
                        ball.setDirection(ball.getDirX(), Math.abs(ball.getDirY()));
                    }
                }

                brick.takeHit();
                if (brick.isDestroyed()) {
                    SoundManager.getInstance().play("break");
                    iterator.remove();
                    if (lastHitter == ServingPlayer.PLAYER_ONE) {
                        playerOneScore.set(playerOneScore.get() + 1);
                    } else {
                        playerTwoScore.set(playerTwoScore.get() + 1);
                    }
                }
                break;
            }
        }

        if (bricks.isEmpty()) {
            spawnInitialBricks();
        }
    }

    private void bounceFromPaddle(Paddle paddle, boolean sendDownward) {
        double paddleCenter = paddle.getX() + paddle.getWidth() / 2.0;
        double ballCenter = ball.getX() + ball.getWidth() / 2.0;
        double relativeIntersect = (ballCenter - paddleCenter) / (paddle.getWidth() / 2.0);
        double maxBounceAngle = Math.toRadians(60);
        double bounceAngle = relativeIntersect * maxBounceAngle;

        double directionX = Math.sin(bounceAngle);
        double directionY = Math.cos(bounceAngle);
        if (sendDownward) {
            ball.setY(paddle.getY() + paddle.getHeight() + 1);
            ball.setDirection(directionX, Math.abs(directionY));
        } else {
            ball.setY(paddle.getY() - ball.getHeight() - 1);
            ball.setDirection(directionX, -Math.abs(directionY));
        }
        SoundManager.getInstance().play("bounce");
    }

    private void checkOutOfBounds() {
        if (ball.getY() > Constants.HEIGHT) {
            playerOneLostLife();
        } else if (ball.getY() + ball.getHeight() < 0) {
            playerTwoLostLife();
        }
    }

    private void playerOneLostLife() {
        playerOneLives.set(playerOneLives.get() - 1);
        SoundManager.getInstance().play("lose_life");
        if (playerOneLives.get() <= 0) {
            endMatch("Player 2 wins the battle!");
            return;
        }
        servingPlayer.set(ServingPlayer.PLAYER_ONE);
        ballLaunched = false;
        stateManager.setStatusMessage("Player 1 lost a life! Press SPACE to relaunch.");
    }

    private void playerTwoLostLife() {
        playerTwoLives.set(playerTwoLives.get() - 1);
        SoundManager.getInstance().play("lose_life");
        if (playerTwoLives.get() <= 0) {
            endMatch("Player 1 wins the battle!");
            return;
        }
        servingPlayer.set(ServingPlayer.PLAYER_TWO);
        ballLaunched = false;
        stateManager.setStatusMessage("Player 2 lost a life! Press SPACE to relaunch.");
    }

    private void endMatch(String message) {
        matchOver = true;
        winnerMessage = message;
        ballLaunched = false;
        stateManager.setStatusMessage(message + " Press ENTER to restart or F1 for Adventure.");
        stateManager.markGameOver();
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
        double ballX = server.getX() + server.getWidth() / 2.0 - ball.getWidth() / 2.0;
        double ballY = servingPlayer.get() == ServingPlayer.PLAYER_ONE
                ? server.getY() - ball.getHeight() - 4
                : server.getY() + server.getHeight() + 4;
        ball.setPosition(ballX, ballY);
        ball.setVelocity(0, 0);
    }

    public void render(GraphicsContext gc) {
        if (gc == null) {
            return;
        }
        gc.clearRect(0, 0, Constants.WIDTH, Constants.HEIGHT);

        gc.setStroke(Color.color(1, 1, 1, 0.25));
        gc.setLineWidth(2);
        gc.strokeLine(Constants.PLAYFIELD_LEFT,
                Constants.HEIGHT / 2.0,
                Constants.PLAYFIELD_RIGHT,
                Constants.HEIGHT / 2.0);

        for (Brick brick : bricks) {
            brick.render(gc);
        }
        if (playerOnePaddle != null) {
            playerOnePaddle.render(gc);
        }
        if (playerTwoPaddle != null) {
            playerTwoPaddle.render(gc);
        }
        if (ball != null) {
            ball.render(gc);
        }
    }

    public void launchBall() {
        if (matchOver || ballLaunched) {
            return;
        }
        double horizontalComponent = (random.nextDouble() * 1.4) - 0.7;
        if (Math.abs(horizontalComponent) < 0.2) {
            horizontalComponent = Math.copySign(0.2, horizontalComponent == 0
                    ? random.nextDouble() - 0.5
                    : horizontalComponent);
        }
        double verticalComponent = servingPlayer.get() == ServingPlayer.PLAYER_ONE ? -1 : 1;
        ball.setDirection(horizontalComponent, verticalComponent);
        lastHitter = servingPlayer.get();
        ballLaunched = true;
        stateManager.setStatusMessage("Battle on!");
    }

    public void stopPlayers() {
        if (playerOnePaddle != null) {
            playerOnePaddle.setDx(0);
        }
        if (playerTwoPaddle != null) {
            playerTwoPaddle.setDx(0);
        }
    }

    public void setPlayerOneVelocity(double dx) {
        if (playerOnePaddle != null) {
            playerOnePaddle.setDx(dx);
        }
    }

    public void setPlayerTwoVelocity(double dx) {
        if (playerTwoPaddle != null) {
            playerTwoPaddle.setDx(dx);
        }
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
}