package com.ooparkanoid.factory;

import com.ooparkanoid.object.bricks.Brick;
import com.ooparkanoid.object.bricks.ExplosiveBrick;
import javafx.scene.image.Image;

public class ExplosiveBrickFactory implements BrickFactory {
    private final Image texture;

    public ExplosiveBrickFactory(Image texture) {
        this.texture = texture;
    }

    @Override
    public Brick createBrick(double x, double y) {
        Brick brick = new ExplosiveBrick(x, y);
        brick.setTexture(this.texture);
        return brick;
    }
}
