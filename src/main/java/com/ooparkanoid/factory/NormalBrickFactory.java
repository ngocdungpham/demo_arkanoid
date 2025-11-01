package com.ooparkanoid.factory;

import com.ooparkanoid.object.bricks.Brick;
import com.ooparkanoid.object.bricks.NormalBrick;
import javafx.scene.image.Image;

public class NormalBrickFactory implements BrickFactory {
    private final Image texture;

    // Factory này cần texture được cung cấp từ bên ngoài
    public NormalBrickFactory(Image texture) {
        this.texture = texture;
    }

    @Override
    public Brick createBrick(double x, double y) {
        Brick brick = new NormalBrick(x, y);
        brick.setTexture(this.texture);
        return brick;
    }
}
