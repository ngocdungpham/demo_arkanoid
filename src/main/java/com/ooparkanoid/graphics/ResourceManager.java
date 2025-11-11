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

    // Dùng trong AssetLoadingTask
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

    // Dùng trong AssetLoadingTask
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
            System.err.println("Failed to load spritesheet: " + filename);
            return null;
        }
    }

    public Image getImage(String filename) {
        Image image = imageCache.get(filename);
        if (image == null) {
            System.err.println("Image not loaded :" + filename);
        }
        return image;
    }

    public SpriteSheet getSpriteSheet(String filename) {
        SpriteSheet sheet = spriteSheetCache.get(filename);
        if (sheet == null) {
            System.err.println("SpriteSheet not loaded : " + filename);
        }
        return sheet;
    }

    // resetgame
    public void clearCache() {
        imageCache.clear();
        spriteSheetCache.clear();
    }
}
