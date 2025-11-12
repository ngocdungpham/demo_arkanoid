package com.ooparkanoid.factory;

import com.ooparkanoid.object.bricks.Brick;
import com.ooparkanoid.object.bricks.IndestructibleBrick;
import javafx.scene.image.Image;

/**
 * Concrete factory implementation for creating IndestructibleBrick instances.
 * Implements the BrickFactory interface to provide indestructible brick creation functionality.
 *
 * Indestructible Bricks:
 * - Cannot be destroyed by any means (ball collisions, explosions, etc.)
 * - Serve as permanent obstacles in level design
 * - Provide strategic blocking elements for gameplay
 * - Use specialized indestructible texture for visual distinction
 *
 * Design Pattern: Concrete Factory (Abstract Factory Pattern)
 * - Implements BrickFactory interface
 * - Encapsulates IndestructibleBrick creation logic
 * - Manages texture assignment for indestructible bricks
 *
 * Usage:
 * Indestructible bricks are typically used in level design to create
 * permanent barriers, guide player movement, or create strategic challenges
 * that cannot be overcome through normal gameplay.
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public class IndestructibleBrickFactory implements BrickFactory {
    /** Texture image used for all indestructible bricks created by this factory */
    private final Image texture;

    /**
     * Constructs an IndestructibleBrickFactory with the specified texture.
     * The texture will be applied to all indestructible bricks created by this factory.
     *
     * @param texture the Image texture to use for indestructible bricks
     */
    public IndestructibleBrickFactory(Image texture) {
        this.texture = texture;
    }

    /**
     * Creates a new IndestructibleBrick instance at the specified coordinates.
     * The brick is initialized with indestructible behavior and the factory's texture.
     *
     * @param x the X coordinate where the indestructible brick should be positioned
     * @param y the Y coordinate where the indestructible brick should be positioned
     * @return a new IndestructibleBrick instance with the specified position and texture
     */
    @Override
    public Brick createBrick(double x, double y) {
        Brick brick = new IndestructibleBrick(x, y);
        brick.setTexture(this.texture);
        return brick;
    }
}
