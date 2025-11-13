package com.ooparkanoid.core.engine;

import com.ooparkanoid.factory.*;
import com.ooparkanoid.object.bricks.Brick;
import com.ooparkanoid.utils.Constants;
import javafx.scene.image.Image;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages level loading and brick creation from configuration files.
 * Implements the Abstract Factory Pattern to create different types of bricks
 * based on character codes read from level definition files.
 *
 * This class has a single responsibility: transforming level files into
 * playable brick layouts with proper positioning and factory-created instances.
 *
 * Design Pattern: Abstract Factory Pattern with Factory Registry
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public class LevelManager {

    /** Registry mapping character codes to their corresponding brick factories */
    private final Map<Character, BrickFactory> brickFactories;

    /**
     * Constructs a LevelManager with required brick textures.
     * Initializes and registers all brick factories with their character codes.
     * Implements Dependency Injection by receiving textures as parameters.
     *
     * Factory Registry:
     * - 'N' = Normal Brick
     * - 'S' = Strong Brick (3 hits)
     * - '#' = Indestructible Brick
     * - 'F' = Flicker Brick (alternating visibility)
     * - 'X' = Explosive Brick (area damage)
     *
     * @param normalBrickTexture texture for normal bricks
     * @param indestructibleBrickTexture texture for indestructible bricks
     * @param explosiveBrickTexture texture for explosive bricks
     */
    public LevelManager(Image normalBrickTexture, Image indestructibleBrickTexture, Image explosiveBrickTexture) {
        brickFactories = new HashMap<>();

        // Register all brick factories with their character codes
        brickFactories.put('N', new NormalBrickFactory(normalBrickTexture));
        brickFactories.put('S', new StrongBrickFactory()); // Self-manages textures
        brickFactories.put('#', new IndestructibleBrickFactory(indestructibleBrickTexture));
        brickFactories.put('F', new FlickerBrickFactory()); // Self-manages textures
        brickFactories.put('X', new ExplosiveBrickFactory(explosiveBrickTexture));
    }

    /**
     * Loads and creates a level from a configuration file.
     * Reads the level file, parses brick definitions, and creates positioned brick instances.
     *
     * Level File Format:
     * - Each character represents a brick type (see factory registry)
     * - Space ' ' represents empty cell
     * - Rows and columns are automatically centered in playfield
     * - Maximum dimensions are enforced (MAX_ROWS_PER_LEVEL x MAX_COLS_PER_LEVEL)
     *
     * Positioning Algorithm:
     * - Calculates total brick layout width based on MAX_COLS_PER_LEVEL
     * - Centers the layout horizontally within the playfield
     * - Applies consistent padding between bricks (BRICK_PADDING_X/Y)
     * - Starts from BRICK_OFFSET_TOP for vertical positioning
     *
     * @param levelNum the level number to load (1-based)
     * @return list of positioned Brick instances, or empty list if level file not found
     */
    public List<Brick> createLevel(int levelNum) {
        List<Brick> bricks = new ArrayList<>();
        String levelFilePath = Constants.LEVELS_FOLDER + "level" + levelNum + ".txt";

        InputStream is = getClass().getResourceAsStream(levelFilePath);
        if (is == null) {
            System.err.println("Level file not found: " + levelFilePath);
            return bricks;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            // Calculate horizontal centering based on maximum column count
            double totalBricksWidth = Constants.MAX_COLS_PER_LEVEL * Constants.BRICK_WIDTH
                                    + (Constants.MAX_COLS_PER_LEVEL - 1) * Constants.BRICK_PADDING_X;
            double startX = Constants.PLAYFIELD_LEFT + (Constants.PLAYFIELD_WIDTH - totalBricksWidth) / 2;

            String line;
            int row = 0;

            while ((line = reader.readLine()) != null) {
                // Enforce maximum row limit
                if (row >= Constants.MAX_ROWS_PER_LEVEL) {
                    System.out.println("Warning: Level " + levelNum + " has more than "
                                     + Constants.MAX_ROWS_PER_LEVEL + " rows. Truncating.");
                    break;
                }

                // Enforce maximum column limit per row
                int colsToRead = Math.min(line.length(), Constants.MAX_COLS_PER_LEVEL);

                for (int col = 0; col < colsToRead; col++) {
                    char brickChar = line.charAt(col);
                    if (brickChar == ' ') continue; // Skip empty cells

                    // Calculate brick position with padding
                    double brickX = startX + col * (Constants.BRICK_WIDTH + Constants.BRICK_PADDING_X);
                    double brickY = Constants.BRICK_OFFSET_TOP + row * (Constants.BRICK_HEIGHT + Constants.BRICK_PADDING_Y);

                    // Create brick using registered factory
                    BrickFactory factory = brickFactories.get(brickChar);
                    if (factory != null) {
                        Brick newBrick = factory.createBrick(brickX, brickY);
                        bricks.add(newBrick);
                    } else {
                        System.err.println("Unknown brick character in level " + levelNum + ": '" + brickChar + "'");
                    }
                }
                row++;
            }
        } catch (Exception e) {
            System.err.println("Error loading level " + levelNum + ": " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Level " + levelNum + " created by LevelManager with " + bricks.size() + " bricks.");
        return bricks;
    }
}
