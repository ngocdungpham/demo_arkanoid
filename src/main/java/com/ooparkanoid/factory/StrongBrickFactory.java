package com.ooparkanoid.factory;

import com.ooparkanoid.object.bricks.Brick;
import com.ooparkanoid.object.bricks.StrongBrick;

/**
 * Concrete factory implementation for creating StrongBrick instances.
 * Implements the BrickFactory interface to provide strong brick creation functionality.
 *
 * Strong Bricks:
 * - Require multiple hits to destroy (typically 3 hits)
 * - Change appearance as they take damage (visual feedback)
 * - Award higher points when destroyed
 * - Self-manage their textures through static initialization
 *
 * Design Pattern: Concrete Factory (Abstract Factory Pattern)
 * - Implements BrickFactory interface
 * - Encapsulates StrongBrick creation logic
 * - Delegates texture management to StrongBrick class itself
 *
 * Texture Management:
 * Unlike other brick factories, StrongBrick manages its own textures
 * through static initialization blocks, eliminating the need for
 * texture injection in this factory. This approach simplifies
 * factory construction while maintaining encapsulation.
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public class StrongBrickFactory implements BrickFactory {
    /**
     * Creates a new StrongBrick instance at the specified coordinates.
     * The brick is initialized with multi-hit behavior and self-managed textures.
     *
     * @param x the X coordinate where the strong brick should be positioned
     * @param y the Y coordinate where the strong brick should be positioned
     * @return a new StrongBrick instance with the specified position and multi-hit behavior
     */
    @Override
    public Brick createBrick(double x, double y) {
        // StrongBrick constructor handles initial texture setup
        return new StrongBrick(x, y);
    }
}
