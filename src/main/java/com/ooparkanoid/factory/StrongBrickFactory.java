package com.ooparkanoid.factory;

import com.ooparkanoid.object.bricks.Brick;
import com.ooparkanoid.object.bricks.StrongBrick;

public class StrongBrickFactory implements BrickFactory {
    // StrongBrick tự quản lý texture của nó, nên factory không cần
    @Override
    public Brick createBrick(double x, double y) {
        // Constructor của StrongBrick đã tự set texture ban đầu
        return new StrongBrick(x, y);
    }
}
