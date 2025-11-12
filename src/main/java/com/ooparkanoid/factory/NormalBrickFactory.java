package com.ooparkanoid.factory;

import com.ooparkanoid.object.bricks.Brick;
import com.ooparkanoid.object.bricks.NormalBrick;
import javafx.scene.image.Image;

/**
 * Concrete factory implementation for creating NormalBrick instances.
 * Implements the BrickFactory interface to provide normal brick creation functionality.
 *
 * Normal Bricks:
 * - Require one hit to destroy (standard durability)
 * - Award standard points when destroyed
 * - May drop power-ups randomly
 * - Most common brick type in levels
 *
 * Design Pattern: Concrete Factory (Abstract Factory Pattern)
 * - Implements BrickFactory interface
 * - Encapsulates NormalBrick creation logic
 * - Manages texture assignment for normal bricks
 *
 * Texture Management:
 * Requires texture injection through constructor, unlike some other brick factories
 * that self-manage their textures. This allows for flexible texture assignment
 * and easier testing.
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public class NormalBrickFactory implements BrickFactory {
    /** Texture image used for all normal bricks created by this factory */
    private final Image texture;

    /**
     * Constructs a NormalBrickFactory with the specified texture.
     * The texture will be applied to all normal bricks created by this factory.
     *
     * @param texture the Image texture to use for normal bricks
     */
    public NormalBrickFactory(Image texture) {
        this.texture = texture;
    }

    /**
     * Creates a new NormalBrick instance at the specified coordinates.
     * The brick is initialized with standard behavior and the factory's texture.
     *
     * @param x the X coordinate where the normal brick should be positioned
     * @param y the Y coordinate where the normal brick should be positioned
     * @return a new NormalBrick instance with the specified position and texture
     */
    @Override
    public Brick createBrick(double x, double y) {
        Brick brick = new NormalBrick(x, y);
        brick.setTexture(this.texture);
        return brick;
    }
}
