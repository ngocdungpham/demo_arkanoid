package com.ooparkanoid.factory;

import com.ooparkanoid.object.bricks.Brick;
import com.ooparkanoid.object.bricks.FlickerBrick;

public class FlickerBrickFactory implements BrickFactory {
    // FlickerBrick tự quản lý texture của nó thông qua static block
    @Override
    public Brick createBrick(double x, double y) {
        return new FlickerBrick(x, y);
    }
}
