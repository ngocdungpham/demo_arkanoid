package com.ooparkanoid.graphics;

// load và cache tất cả hình ảnh, tránh load lại nhiều lần

import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.Map;

public class ResourceManager {
    private static ResourceManager instance;
    private Map<String, Image> imageCache = new HashMap<>();        // save image
    private Map<String, SpriteSheet> spriteSheetCache = new HashMap<>();        // save sprite Sheet

    private static final String IMAGES_PATH = "/images/";
    private static final String SPRITES_PATH = "/sprites/";

    // Không tạo đối tượng mới bằng new
    private ResourceManager() {
    }

    // tạo thông qua getInstance
    public static ResourceManager getInstance() {
        if (instance == null) {
            instance = new ResourceManager();
        }
        return instance;
    }

    public Image loadImage(String filename) {
        if (imageCache.containsKey(filename)) {
            return imageCache.get(filename);
        }
        try {
            String path = IMAGES_PATH + filename;
            Image image = new Image(getClass().getResourceAsStream(path));
            imageCache.put(filename, image);
            System.out.println("Loaded image:" + filename);
            return image;
        } catch (Exception e) {
            System.err.println("Failed to load image: " + filename);
            return null;
        }
    }

    public SpriteSheet loadSpriteSheet(String filename, int frameWidth, int frameHeight) {
        if (spriteSheetCache.containsKey(filename)) {
            return spriteSheetCache.get(filename);
        }
        try {
            String path = SPRITES_PATH + filename;
            Image image = new Image(getClass().getResourceAsStream(path));
            SpriteSheet sheet = new SpriteSheet(image, frameWidth, frameHeight);
            spriteSheetCache.put(filename, sheet);
            System.out.println("Loaded spritesheet: " + filename);
            return sheet;
        } catch (Exception e) {
            System.err.println("Failed to load spritesheet: " + filename);
            return null;
        }
    }

    // resetgame
    public void clearCache() {
        imageCache.clear();
        spriteSheetCache.clear();
    }
}
