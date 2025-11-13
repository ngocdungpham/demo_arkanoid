package com.ooparkanoid.factory;

import com.ooparkanoid.object.bricks.Brick;

/**
 * Abstract Factory interface defining the contract for all brick creation factories.
 * Provides a standardized way to create different types of bricks in the game.
 *
 * Design Pattern: Abstract Factory Pattern
 * - Defines interface for creating families of related objects (bricks)
 * - Allows concrete factories to implement specific brick creation logic
 * - Decouples brick creation from specific brick types
 * - Enables easy extension with new brick types
 *
 * Usage:
 * Implement this interface to create factories for different brick types
 * (NormalBrick, StrongBrick, ExplosiveBrick, etc.). Each factory handles
 * the specific creation logic for its brick type.
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public interface BrickFactory {
    /**
     * Creates a new brick instance at the specified coordinates.
     * Each concrete factory implementation will create a specific type of brick
     * (normal, strong, explosive, etc.) with appropriate properties and behavior.
     *
     * @param x the X coordinate where the brick should be positioned
     * @param y the Y coordinate where the brick should be positioned
     * @return a new instance of a Brick subclass with the specified position
     */
    Brick createBrick(double x, double y);
}
