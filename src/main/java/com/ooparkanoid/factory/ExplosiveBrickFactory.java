package com.ooparkanoid.factory;

import com.ooparkanoid.object.bricks.Brick;
import com.ooparkanoid.object.bricks.ExplosiveBrick;
import javafx.scene.image.Image;

/**
 * Concrete factory implementation for creating ExplosiveBrick instances.
 * Implements the BrickFactory interface to provide explosive brick creation functionality.
 *
 * Explosive Bricks:
 * - Destroy adjacent bricks in a 3x3 area when destroyed
 * - Require one hit to destroy (unlike strong bricks)
 * - Trigger chain reactions when multiple explosive bricks are nearby
 * - Use specialized explosive texture for visual distinction
 *
 * Design Pattern: Concrete Factory (Abstract Factory Pattern)
 * - Implements BrickFactory interface
 * - Encapsulates ExplosiveBrick creation logic
 * - Manages texture assignment for explosive bricks
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public class ExplosiveBrickFactory implements BrickFactory {
    /** Texture image used for all explosive bricks created by this factory */
    private final Image texture;

    /**
     * Constructs an ExplosiveBrickFactory with the specified texture.
     * The texture will be applied to all explosive bricks created by this factory.
     *
     * @param texture the Image texture to use for explosive bricks
     */
    public ExplosiveBrickFactory(Image texture) {
        this.texture = texture;
    }

    /**
     * Creates a new ExplosiveBrick instance at the specified coordinates.
     * The brick is initialized with explosive behavior and the factory's texture.
     *
     * @param x the X coordinate where the explosive brick should be positioned
     * @param y the Y coordinate where the explosive brick should be positioned
     * @return a new ExplosiveBrick instance with the specified position and texture
     */
    @Override
    public Brick createBrick(double x, double y) {
        Brick brick = new ExplosiveBrick(x, y);
        brick.setTexture(this.texture);
        return brick;
    }
}
