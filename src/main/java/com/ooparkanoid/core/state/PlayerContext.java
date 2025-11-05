package com.ooparkanoid.core.state;

/**
 * Lưu trữ thông tin phiên đăng nhập của người chơi.
 */
public class PlayerContext {

    public static String playerName = "Player"; // Tên hiển thị (từ lần trước)
    public static String uid;                   // User ID duy nhất từ Firebase Auth
    public static String email;                 // Email
    public static String idToken;               // "Vé" (token) để xác thực API

    public static boolean isLoggedIn() {
        return uid != null && !uid.isEmpty() && idToken != null;
    }

    public static void setSession(String uid, String email, String idToken, String displayName) {
        PlayerContext.uid = uid;
        PlayerContext.email = email;
        PlayerContext.idToken = idToken;
        PlayerContext.playerName = displayName; // Cập nhật tên người chơi từ tên nhập
    }

    public static void clearSession() {
        PlayerContext.uid = null;
        PlayerContext.email = null;
        PlayerContext.idToken = null;
        PlayerContext.playerName = "Player";
    }
}