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
 * Lớp này chịu trách nhiệm duy nhất cho việc tạo ra danh sách các viên gạch
 * cho một màn chơi cụ thể từ file cấu hình.
 * Nó sử dụng Abstract Factory Pattern để tạo các loại gạch khác nhau.
 */
public class LevelManager {

    private final Map<Character, BrickFactory> brickFactories;

    /**
     * Constructor của LevelManager.
     * Nó nhận các texture cần thiết để khởi tạo các factory tương ứng.
     * Đây là một ví dụ về Dependency Injection.
     *
     * @param normalBrickTexture Texture cho gạch thường.
     * @param indestructibleBrickTexture Texture cho gạch bất tử.
     * @param explosiveBrickTexture Texture cho gạch nổ.
     */
    public LevelManager(Image normalBrickTexture, Image indestructibleBrickTexture, Image explosiveBrickTexture) {
        brickFactories = new HashMap<>();
        // Đăng ký tất cả các nhà máy sản xuất gạch
        brickFactories.put('N', new NormalBrickFactory(normalBrickTexture));
        brickFactories.put('S', new StrongBrickFactory()); // StrongBrick tự quản lý texture
        brickFactories.put('#', new IndestructibleBrickFactory(indestructibleBrickTexture));
        brickFactories.put('F', new FlickerBrickFactory()); // FlickerBrick tự quản lý texture
        brickFactories.put('X', new ExplosiveBrickFactory(explosiveBrickTexture));
    }

    /**
     * Đọc một file level và tạo ra một danh sách các đối tượng Brick.
     *
     * @param levelNum Số thứ tự của màn chơi cần tải.
     * @return Một List<Brick> đại diện cho màn chơi. Trả về list rỗng nếu có lỗi.
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
            // ===== THAY ĐỔI 1: TÍNH TOÁN CĂN GIỮA DỰA TRÊN HẰNG SỐ =====
            // Không cần đọc dòng đầu tiên nữa. Luôn tính toán dựa trên khung cố định.
            double totalBricksWidth = Constants.MAX_COLS_PER_LEVEL * Constants.BRICK_WIDTH + (Constants.MAX_COLS_PER_LEVEL - 1) * Constants.BRICK_PADDING_X;
            double startX = Constants.PLAYFIELD_LEFT + (Constants.PLAYFIELD_WIDTH - totalBricksWidth) / 2;

            String line;
            int row = 0;
            while ((line = reader.readLine()) != null) {
                // ===== THAY ĐỔI 2: ÁP DỤNG GIỚI HẠN HÀNG =====
                if (row >= Constants.MAX_ROWS_PER_LEVEL) {
                    System.out.println("Warning: Level " + levelNum + " has more than " + Constants.MAX_ROWS_PER_LEVEL + " rows. Truncating.");
                    break; // Dừng đọc file nếu quá giới hạn hàng
                }

                // ===== THAY ĐỔI 3: ÁP DỤNG GIỚI HẠN CỘT =====
                int colsToRead = Math.min(line.length(), Constants.MAX_COLS_PER_LEVEL);
                for (int col = 0; col < colsToRead; col++) {
                    char brickChar = line.charAt(col);
                    if (brickChar == ' ') continue;

                    double brickX = startX + col * (Constants.BRICK_WIDTH + Constants.BRICK_PADDING_X);
                    double brickY = Constants.BRICK_OFFSET_TOP + row * (Constants.BRICK_HEIGHT + Constants.BRICK_PADDING_Y);

                    BrickFactory factory = brickFactories.get(brickChar);
                    if (factory != null) {
                        Brick newBrick = factory.createBrick(brickX, brickY);
                        bricks.add(newBrick);
                    } else {
                        System.err.println("Unknown brick char in level " + levelNum + ": '" + brickChar + "'");
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
