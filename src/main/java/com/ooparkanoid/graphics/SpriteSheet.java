package com.ooparkanoid.graphics;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

// Cắt sprite sheet thành các frames riêng lẻ
public class SpriteSheet {
    private Image spriteSheet;
    private int frameWidth;
    private int frameHeight;
    private int columns;
    private int rows;

    public SpriteSheet(Image spriteSheet, int frameWidth, int frameHeight) {
        this.spriteSheet = spriteSheet;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.columns = (int) (spriteSheet.getWidth() / frameWidth);
        this.rows = (int) (spriteSheet.getWidth() / frameHeight);
    }

    // Lấy frame từ sprite sheet
    public Image getFrame(int index) {
        int col = index % columns;
        int row = index / columns;
        return getFrame(row, col);
    }

    // Lấy frame theo tọa do row, col
    public Image getFrame(int row, int col) {
        if (row >= rows || col >= columns) {
            return null;
        }
        PixelReader reader = spriteSheet.getPixelReader();
        int x = col * frameWidth;
        int y = row * frameHeight;
        return new WritableImage(reader, x, y, frameWidth, frameHeight); // tạo ảnh mới
    }

    // Lấy tất frames trong 1 row
    public Image[] getRowFrames(int row) {
        Image[] frames = new Image[columns];
        for (int i = 0; i < columns; i++) {
            frames[i] = getFrame(row, i);
        }
        return frames;
    }

    // Lấy số Frame
    public int getFrameCount() {
        return columns * rows;
    }
}
