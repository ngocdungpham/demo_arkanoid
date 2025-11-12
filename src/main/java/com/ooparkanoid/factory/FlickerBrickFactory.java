package com.ooparkanoid.factory;

import com.ooparkanoid.object.bricks.Brick;
import com.ooparkanoid.object.bricks.FlickerBrick;

/**
 * Concrete factory implementation for creating FlickerBrick instances.
 * Implements the BrickFactory interface to provide flicker brick creation functionality.
 *
 * Flicker Bricks:
 * - Alternate between visible and invisible states periodically
 * - Require one hit to destroy (standard durability)
 * - Create visual challenge by disappearing and reappearing
 * - Self-manage their textures through static initialization
 *
 * Design Pattern: Concrete Factory (Abstract Factory Pattern)
 * - Implements BrickFactory interface
 * - Encapsulates FlickerBrick creation logic
 * - Delegates texture management to FlickerBrick class itself
 *
 * Texture Management:
 * Unlike other brick factories, FlickerBrick manages its own textures
 * through static initialization blocks, eliminating the need for
 * texture injection in this factory.
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public class FlickerBrickFactory implements BrickFactory {
    /**
     * Creates a new FlickerBrick instance at the specified coordinates.
     * The brick is initialized with flicker behavior and self-managed textures.
     *
     * @param x the X coordinate where the flicker brick should be positioned
     * @param y the Y coordinate where the flicker brick should be positioned
     * @return a new FlickerBrick instance with the specified position and flicker behavior
     */
    @Override
    public Brick createBrick(double x, double y) {
        return new FlickerBrick(x, y);
    }
}
