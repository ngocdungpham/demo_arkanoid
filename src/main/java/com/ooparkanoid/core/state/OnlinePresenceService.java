package com.ooparkanoid.core.state;

import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import com.ooparkanoid.core.state.PlayerContext;

/**
 * Service for managing player online presence status using Firebase Realtime Database (RTDB).
 * Tracks when players are online/offline and their last seen timestamps.
 *
 * Features:
 * - Real-time online/offline status updates
 * - Automatic timestamp tracking (last seen)
 * - Firebase RTDB REST API integration
 * - Asynchronous operations (non-blocking)
 * - Player name display in presence data
 *
 * Database Structure:
 * presence/
 *   {userId}/
 *     isOnline: boolean
 *     lastSeen: ISO 8601 timestamp
 *     name: player display name
 *
 * Security: Requires Firebase ID token for authentication.
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public class OnlinePresenceService {

    /** Firebase Realtime Database URL (configured for Asia Southeast region) */
    private static final String RTDB_URL = "https://coffehouseuet201-default-rtdb.asia-southeast1.firebasedatabase.app/";

    /** Shared HTTP client for all RTDB operations */
    private static final HttpClient client = HttpClient.newBuilder().build();

    /**
     * Marks a player as online in the Firebase Realtime Database.
     * Creates or updates the player's presence record with online status, timestamp, and name.
     * Requires user to be logged in with valid authentication token.
     *
     * Database Update:
     * - Sets isOnline to true
     * - Updates lastSeen to current timestamp
     * - Stores player's display name for UI purposes
     *
     * @param uid the user's unique identifier from Firebase Authentication
     */
    public static void goOnline(String uid) {
        if (uid == null || uid.isEmpty() || !PlayerContext.isLoggedIn()) {
            return;
        }

        try {
            // Build presence data JSON object
            JSONObject presenceData = new JSONObject();
            presenceData.put("isOnline", true);
            presenceData.put("lastSeen", Instant.now().toString());
            presenceData.put("name", PlayerContext.playerName); // Include name for display

            String url = RTDB_URL + "presence/" + uid + ".json?auth=" + PlayerContext.idToken;

            // Use PUT to create or overwrite presence record
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(presenceData.toString()))
                    .build();

            // Send asynchronously (non-blocking)
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenRun(() -> System.out.println(PlayerContext.playerName + " is now online!"));

        } catch (Exception e) {
            System.err.println("Error updating online status: " + e.getMessage());
        }
    }

    /**
     * Marks a player as offline when they exit the game.
     * Removes the player's presence record from the database entirely.
     * Requires user to be logged in with valid authentication token.
     *
     * Alternative Implementation:
     * Could use PUT with {isOnline: false} instead of DELETE to preserve history.
     * Current implementation: DELETE removes record completely for cleaner database.
     *
     * @param uid the user's unique identifier from Firebase Authentication
     */
    public static void goOffline(String uid) {
        if (uid == null || uid.isEmpty() || !PlayerContext.isLoggedIn()) {
            return;
        }

        try {
            String url = RTDB_URL + "presence/" + uid + ".json?auth=" + PlayerContext.idToken;

            // Use DELETE to remove presence record completely
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .DELETE()
                    .build();

            // Send asynchronously (non-blocking)
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenRun(() -> System.out.println(PlayerContext.playerName + " is now offline."));

        } catch (Exception e) {
            System.err.println("Error updating offline status: " + e.getMessage());
        }
    }

    // Note: getOnlinePlayers() method could be added here
    // by performing GET request to RTDB_URL + "presence.json"
    // to retrieve all currently online players
}