// SaveService.java (mới)
package com.ooparkanoid.core.save;

import java.io.*;
import java.nio.file.*;
import java.util.Optional;
import java.util.Properties;

public final class SaveService {
    private static final Path SAVE_FILE = Paths.get(System.getProperty("user.home"), ".arkanoid_save.properties");

    private SaveService(){}

    public static boolean exists() { return Files.exists(SAVE_FILE); }
    public static void deleteIfExists() {
        try { Files.deleteIfExists(SAVE_FILE); } catch (IOException ignored) {}
    }

    public static void save(GameSnapshot s) {
        try {
            Properties p = new Properties();
            p.setProperty("level", String.valueOf(s.level));
            p.setProperty("score", String.valueOf(s.score));
            p.setProperty("lives", String.valueOf(s.lives));
            p.setProperty("ballX", String.valueOf(s.ballX));
            p.setProperty("ballY", String.valueOf(s.ballY));
            p.setProperty("ballDX", String.valueOf(s.ballDX));
            p.setProperty("ballDY", String.valueOf(s.ballDY));
            p.setProperty("paddleX", String.valueOf(s.paddleX));
            Files.createDirectories(SAVE_FILE.getParent());
            try (OutputStream os = Files.newOutputStream(SAVE_FILE)) {
                p.store(os, "Arkanoid save");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Optional<GameSnapshot> load() {
        if (!exists()) return Optional.empty();
        try (InputStream is = Files.newInputStream(SAVE_FILE)) {
            Properties p = new Properties();
            p.load(is);
            GameSnapshot s = new GameSnapshot();
            s.level = Integer.parseInt(p.getProperty("level", "1"));
            s.score = Integer.parseInt(p.getProperty("score", "0"));
            s.lives = Integer.parseInt(p.getProperty("lives", "3"));
            s.ballX = Double.parseDouble(p.getProperty("ballX", "0"));
            s.ballY = Double.parseDouble(p.getProperty("ballY", "0"));
            s.ballDX = Double.parseDouble(p.getProperty("ballDX", "0"));
            s.ballDY = Double.parseDouble(p.getProperty("ballDY", "0"));
            s.paddleX = Double.parseDouble(p.getProperty("paddleX", "0"));
            return Optional.of(s);
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    // Bạn có thể mở rộng thêm trường tuỳ engine của bạn
    public static class GameSnapshot {
        public int level, score, lives;
        public double ballX, ballY, ballDX, ballDY;
        public double paddleX;
    }
}
