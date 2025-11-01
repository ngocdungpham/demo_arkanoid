package com.ooparkanoid.factory;

import com.ooparkanoid.object.bricks.Brick;

/**
 * Interface (Abstract Factory) định nghĩa một hợp đồng chung
 * cho tất cả các nhà máy tạo gạch.
 * Mỗi nhà máy phải có khả năng tạo ra một đối tượng Brick.
 */
public interface BrickFactory {
    /**
     * Tạo một đối tượng Brick tại tọa độ (x, y) cụ thể.
     * @param x Tọa độ X
     * @param y Tọa độ Y
     * @return Một instance của một lớp con của Brick.
     */
    Brick createBrick(double x, double y);
}
