package com.ooparkanoid.graphics;

import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages image and sprite sheet resources with caching.
 * Implements singleton pattern to provide centralized resource management.
 * Prevents redundant loading by caching all loaded resources.
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public class ResourceManager {
    private static ResourceManager instance;
    private Map<String, Image> imageCache = new HashMap<>();
    private Map<String, SpriteSheet> spriteSheetCache = new HashMap<>();

    private static final String IMAGES_PATH = "/images/";
    private static final String SPRITES_PATH = "/sprites/";

    /**
     * Private constructor to enforce singleton pattern.
     */
    private ResourceManager() {
    }

    /**
     * Gets the singleton instance of ResourceManager.
     *
     * @return the singleton ResourceManager instance
     */
    public static ResourceManager getInstance() {
        if (instance == null) {
            instance = new ResourceManager();
        }
        return instance;
    }

    /**
     * Loads an image from resources and caches it.
     * If already cached, returns the cached version.
     * Used by AssetLoadingTask for preloading.
     *
     * @param filename the image filename (e.g., "paddle1.png")
     * @return the loaded Image, or null if loading fails
     */
    public Image loadImage(String filename) {
        if (imageCache.containsKey(filename)) {
            return imageCache.get(filename);
        }
        try {
            String path = IMAGES_PATH + filename;
            Image image = new Image(getClass().getResourceAsStream(path));
            imageCache.put(filename, image);
            System.out.println("Loaded image: " + filename);
            return image;
        } catch (Exception e) {
            System.err.println("Failed to load image: " + filename);
            return null;
        }
    }

    /**
     * Loads a sprite sheet from resources and caches it.
     * If already cached, returns the cached version.
     * Used by AssetLoadingTask for preloading.
     *
     * @param filename the sprite sheet filename
     * @param frameWidth width of each frame in pixels
     * @param frameHeight height of each frame in pixels
     * @return the loaded SpriteSheet, or null if loading fails
     */
    public SpriteSheet loadSpriteSheet(String filename, int frameWidth, int frameHeight) {
        if (spriteSheetCache.containsKey(filename)) {
            return spriteSheetCache.get(filename);
        }
        try {
            String path = SPRITES_PATH + filename;
            Image image = new Image(getClass().getResourceAsStream(path));
            SpriteSheet sheet = new SpriteSheet(image, frameWidth, frameHeight, 0, 0);
            spriteSheetCache.put(filename, sheet);
            System.out.println("Loaded spritesheet: " + filename);
            return sheet;
        } catch (Exception e) {
            // Silent fail - fallback mechanism handles missing sprites
            return null;
        }
    }

    /**
     * Retrieves a cached image.
     * Does not load the image if not already cached.
     *
     * @param filename the image filename
     * @return the cached Image, or null if not found
     */
    public Image getImage(String filename) {
        // Returns null if not cached - fallback mechanism handles this
        return imageCache.get(filename);
    }

    /**
     * Retrieves a cached sprite sheet.
     * Does not load the sprite sheet if not already cached.
     *
     * @param filename the sprite sheet filename
     * @return the cached SpriteSheet, or null if not found
     */
    public SpriteSheet getSpriteSheet(String filename) {
        // Returns null if not cached - fallback mechanism handles this
        return spriteSheetCache.get(filename);
    }

    /**
     * Clears all cached resources.
     * Useful for resetting game state or freeing memory.
     */
    public void clearCache() {
        imageCache.clear();
        spriteSheetCache.clear();
    }
}
