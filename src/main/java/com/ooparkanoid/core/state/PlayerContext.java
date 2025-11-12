package com.ooparkanoid.core.state;

/**
 * Static context holder for the currently authenticated player's session data.
 * Stores authentication credentials and user information obtained from Firebase Authentication.
 *
 * Features:
 * - Global access to player session data across the application
 * - Firebase Authentication integration (uid, email, idToken)
 * - Player display name management
 * - Session state validation
 * - Clean session lifecycle (set/clear)
 *
 * Security Considerations:
 * - idToken should be treated as sensitive credential
 * - Token expires after a period (typically 1 hour)
 * - Consider implementing token refresh mechanism
 * - Clear session on logout or application exit
 *
 * Thread Safety: Not thread-safe. All fields are static and mutable.
 * Should only be accessed from JavaFX Application Thread.
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public class PlayerContext {

    /** Player's display name shown in game UI and leaderboards (default: "Player") */
    public static String playerName = "Player";

    /** Unique user identifier from Firebase Authentication */
    public static String uid;

    /** User's email address from Firebase Authentication */
    public static String email;

    /** Firebase ID token for authenticating API requests (acts as session credential) */
    public static String idToken;

    /**
     * Checks if a user is currently logged in with valid session data.
     * Validates that both uid and idToken are present and non-empty.
     *
     * Note: This does not validate if the token is still valid/non-expired.
     * Consider adding token expiration checking for production.
     *
     * @return true if user has valid session credentials, false otherwise
     */
    public static boolean isLoggedIn() {
        return uid != null && !uid.isEmpty() && idToken != null;
    }

    /**
     * Initializes a new player session with Firebase Authentication data.
     * Stores all authentication credentials and user information for the session.
     * Should be called after successful login or signup.
     *
     * @param uid unique user identifier from Firebase Auth
     * @param email user's email address
     * @param idToken Firebase ID token for API authentication
     * @param displayName player's display name (falls back to "Player" if null/empty)
     */
    public static void setSession(String uid, String email, String idToken, String displayName) {
        PlayerContext.uid = uid;
        PlayerContext.email = email;
        PlayerContext.idToken = idToken;
        PlayerContext.playerName = displayName; // Update player name from authentication data
    }

    /**
     * Clears the current player session and resets all fields to default values.
     * Should be called on logout or when authentication session expires.
     * Sensitive data (uid, email, idToken) is set to null for security.
     * Player name is reset to default "Player".
     */
    public static void clearSession() {
        PlayerContext.uid = null;
        PlayerContext.email = null;
        PlayerContext.idToken = null;
        PlayerContext.playerName = "Player";
    }
}