package com.ooparkanoid.factory;

import com.ooparkanoid.object.bricks.Brick;
import com.ooparkanoid.object.bricks.IndestructibleBrick;
import javafx.scene.image.Image;

public class IndestructibleBrickFactory implements BrickFactory {
    private final Image texture;

    public IndestructibleBrickFactory(Image texture) {
        this.texture = texture;
    }

    @Override
    public Brick createBrick(double x, double y) {
        Brick brick = new IndestructibleBrick(x, y);
        brick.setTexture(this.texture);
        return brick;
    }
}
