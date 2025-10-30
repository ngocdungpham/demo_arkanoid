package com.ooparkanoid.sound;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

// Quản lý việc tải và phát âm thanh và nhạc nền
public class SoundManager {
    private static SoundManager instance;
    private Map<String, AudioClip> soundEffects;
    private MediaPlayer musicPlayer;

    private SoundManager() {
        soundEffects = new HashMap<>();
        // sound 1 lần
        loadSound("bounce", "/sounds/bounce.wav");
        loadSound("break", "/sounds/break.mp3");
        loadSound("powerup", "/sounds/powerup.wav");
        loadSound("lose_life", "/sounds/lose_life.wav"); // <--- Dễ dàng thêm mới
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    // Hàm  dùng để tải một file âm thanh và lưu vào Map.
    private void loadSound(String name, String path) {
        URL resource = getClass().getResource(path);
        if (resource == null) {
            System.err.println("Không tìm thấy file âm thanh: " + path);
            return;
        }
        try {
            AudioClip clip = new AudioClip(resource.toExternalForm());
            soundEffects.put(name, clip);
        } catch (Exception e) {
            System.err.println("Lỗi khi tải âm thanh: " + path + " - " + e.getMessage());
        }
    }

    // phát sound
    public void play(String name) {
        AudioClip clip = soundEffects.get(name);
        if (clip != null) {
            clip.play();
        } else {
            System.err.println("Không tìm thấy âm thanh tên: " + name);
        }
    }

    // Phát nhạc nền. Sẽ dừng nhạc cũ nếu có nhạc mới
    public void playMusic(String filename) {
        if (musicPlayer != null) {
            musicPlayer.stop();
        }

        URL resource = getClass().getResource("/sounds/" + filename);
        if (resource == null) {
            System.err.println("Không tìm thấy file nhạc: " + filename);
            return;
        }

        try {
            Media media = new Media(resource.toExternalForm());
            musicPlayer = new MediaPlayer(media);
            musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);  // Lặp vô hạn
            musicPlayer.setVolume(0.5);
            musicPlayer.play();
        } catch (Exception e) {
            System.err.println("Failed load sound: " + filename + " - " + e.getMessage());
        }
    }

    // Dừng phát nhạc nền.
    public void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
        }
    }
}