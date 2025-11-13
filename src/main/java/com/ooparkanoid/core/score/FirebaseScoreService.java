package com.ooparkanoid.core.score;

import com.ooparkanoid.core.state.PlayerContext;
import javafx.application.Platform;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing score submissions and retrievals from Google Firebase Firestore.
 * Provides functionality to persist high scores and fetch leaderboard data using Firestore REST API.
 *
 * Features:
 * - Submit scores with authentication (requires Firebase ID token)
 * - Automatic upsert (update if exists, insert if new)
 * - Only keeps highest score per user (prevents score downgrade)
 * - Retrieve top scores sorted by descending order
 * - Asynchronous operations using CompletableFuture
 *
 * Technical Implementation:
 * - Uses Firestore REST API (no Firebase SDK dependency)
 * - HTTP/1.1 client for compatibility
 * - Bearer token authentication with Firebase ID tokens
 * - Structured queries for efficient data retrieval
 *
 * Security:
 * - Requires user authentication (PlayerContext.idToken)
 * - User-based score isolation (one high score per userId)
 * - Server-side validation through Firebase Security Rules
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public final class FirebaseScoreService {

    /** Firebase project ID (configured in Firebase Console) */
    private static final String PROJECT_ID = "coffehouseuet201";

    /** Base URL for Firestore REST API endpoints */
    private static final String BASE_URL = "https://firestore.googleapis.com/v1/projects/"
            + PROJECT_ID + "/databases/(default)/documents";

    /** Shared HTTP client for all Firestore operations */
    private static final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    /**
     * Submits a score to Firestore with intelligent upsert logic.
     * Only updates if new score is higher than existing score for the user.
     * Requires user to be authenticated (PlayerContext must have valid uid and idToken).
     *
     * Upsert Logic:
     * 1. Fetch existing score for current user
     * 2. Compare with new score
     * 3. Update only if new score is higher
     * 4. Create new document if user has no previous score
     *
     * @param entry the score entry to submit (must have score > 0)
     */
    public static void submitScore(ScoreEntry entry) {
        if (entry == null || entry.getScore() <= 0) {
            return;
        }

        if (!PlayerContext.isLoggedIn()) {
            System.err.println("User not logged in, cannot submit score!");
            return;
        }

        String userId = PlayerContext.uid;
        if (userId == null || userId.isBlank()) {
            System.err.println("No userId available, skipping score submission!");
            return;
        }
        fetchExistingScore(userId)
                .thenCompose(existing -> {
                    if (existing != null && existing.score >= entry.getScore()) {
                        System.out.println("Existing score is higher or equal. Skipping update.");
                        return CompletableFuture.completedFuture(null);
                    }
                    String documentName = existing != null ? existing.documentName : null;
                    return upsertScore(entry, userId, documentName);
                })
                .exceptionally(e -> {
                    System.err.println("Failed to submit score to Firebase: " + e.getMessage());
                    return null;
                });
    }

    /**
     * Retrieves top 100 scores from Firestore, sorted by score in descending order.
     * Uses Firestore structured query API for efficient server-side sorting.
     *
     * @return CompletableFuture containing list of top score entries, sorted highest to lowest
     */
    public static CompletableFuture<List<ScoreEntry>> getTopScores() {

        // 1. Build structured query payload
        JSONObject query = new JSONObject();
        query.put("from", new JSONArray().put(new JSONObject().put("collectionId", "scores")));
        query.put("orderBy", new JSONArray().put(
                new JSONObject()
                        .put("field", new JSONObject().put("fieldPath", "score"))
                        .put("direction", "DESCENDING")
        ));
        query.put("limit", 100);

        JSONObject requestBody = new JSONObject();
        requestBody.put("structuredQuery", query);

        // 2. Build authenticated HTTP request
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + ":runQuery"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()));

        if (PlayerContext.idToken != null && !PlayerContext.idToken.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + PlayerContext.idToken);
        }

        HttpRequest request = requestBuilder.build();

        // 3. Execute query and parse response
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(FirebaseScoreService::parseFirestoreResponse);
    }

    /**
     * Parses Firestore query response JSON into list of ScoreEntry objects.
     * Handles Firestore's complex nested JSON structure with type annotations.
     *
     * @param responseBody raw JSON response from Firestore
     * @return list of parsed ScoreEntry objects
     */
    private static List<ScoreEntry> parseFirestoreResponse(String responseBody) {
        List<ScoreEntry> entries = new ArrayList<>();
        try {
            JSONArray documents = new JSONArray(responseBody);

            for (int i = 0; i < documents.length(); i++) {
                JSONObject docContainer = documents.optJSONObject(i);
                if (docContainer == null || !docContainer.has("document")) {
                    continue;
                }

                JSONObject doc = docContainer.getJSONObject("document");
                JSONObject fields = doc.getJSONObject("fields");

                // Extract fields with safe defaults
                String name = fields.optJSONObject("playerName").optString("stringValue", "Player");
                int score = Integer.parseInt(fields.optJSONObject("score").optString("integerValue", "0"));
                int rounds = Integer.parseInt(fields.optJSONObject("roundsPlayed").optString("integerValue", "1"));
                double seconds = fields.optJSONObject("totalSeconds").optDouble("doubleValue", 0.0);

                entries.add(new ScoreEntry(name, score, rounds, seconds));
            }
        } catch (Exception e) {
            System.err.println("Error parsing JSON from Firebase: " + e.getMessage());
            e.printStackTrace();
        }
        return entries;
    }

    /**
     * Fetches existing score document for a user from Firestore.
     * Queries by userId field and returns the highest score if multiple documents exist.
     *
     * @param userId the user's unique identifier
     * @return CompletableFuture containing ScoreDocument, or null if no score exists
     */
    private static CompletableFuture<ScoreDocument> fetchExistingScore(String userId) {
        try {
            JSONObject query = new JSONObject();
            query.put("from", new JSONArray().put(new JSONObject().put("collectionId", "scores")));

            JSONObject where = new JSONObject();
            JSONObject fieldFilter = new JSONObject();
            fieldFilter.put("field", new JSONObject().put("fieldPath", "userId"));
            fieldFilter.put("op", "EQUAL");
            fieldFilter.put("value", new JSONObject().put("stringValue", userId));
            where.put("fieldFilter", fieldFilter);
            query.put("where", where);
            query.put("limit", 5);

            JSONObject requestBody = new JSONObject();
            requestBody.put("structuredQuery", query);

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + ":runQuery"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()));

            if (PlayerContext.idToken != null && !PlayerContext.idToken.isEmpty()) {
                requestBuilder.header("Authorization", "Bearer " + PlayerContext.idToken);
            }

            return client.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(FirebaseScoreService::extractBestScoreDocument);
        } catch (Exception e) {
            CompletableFuture<ScoreDocument> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }

    /**
     * Upserts (update or insert) a score document in Firestore.
     * Uses PATCH for updates (existing document) or POST for inserts (new document).
     *
     * @param entry the score entry to save
     * @param userId the user's unique identifier
     * @param existingDocumentName full document path if updating, null if inserting
     * @return CompletableFuture that completes when operation finishes
     */
    private static CompletableFuture<Void> upsertScore(ScoreEntry entry, String userId, String existingDocumentName) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("fields", buildFields(entry, userId));

            HttpRequest.Builder requestBuilder;
            if (existingDocumentName != null && !existingDocumentName.isBlank()) {
                String url = "https://firestore.googleapis.com/v1/" + existingDocumentName;
                requestBuilder = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody.toString()));
            } else {
                String encodedId = URLEncoder.encode(userId, StandardCharsets.UTF_8);
                String url = BASE_URL + "/scores?documentId=" + encodedId;
                requestBuilder = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()));
            }

            if (PlayerContext.idToken != null && !PlayerContext.idToken.isEmpty()) {
                requestBuilder.header("Authorization", "Bearer " + PlayerContext.idToken);
            }

            return client.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            System.out.println("Firebase score update successful!");
                        } else {
                            System.err.println("Firebase score update failed. Code: "
                                    + response.statusCode() + ", Body: " + response.body());
                        }
                    })
                    .exceptionally(e -> {
                        System.err.println("Error updating Firebase score: " + e.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }

    /**
     * Builds Firestore document fields from a ScoreEntry.
     * Converts Java objects to Firestore field format with type annotations.
     *
     * @param entry the score entry to convert
     * @param userId the user's unique identifier
     * @return JSONObject containing Firestore-formatted fields
     */
    private static JSONObject buildFields(ScoreEntry entry, String userId) {
        JSONObject fields = new JSONObject();
        fields.put("score", new JSONObject().put("integerValue", entry.getScore()));
        fields.put("playerName", new JSONObject().put("stringValue", entry.getPlayerName()));
        fields.put("roundsPlayed", new JSONObject().put("integerValue", entry.getRoundsPlayed()));
        fields.put("totalSeconds", new JSONObject().put("doubleValue", entry.getTotalSeconds()));
        fields.put("createdAt", new JSONObject().put("timestampValue", Instant.now().toString()));
        fields.put("userId", new JSONObject().put("stringValue", userId));
        return fields;
    }

    /**
     * Extracts the highest score document from a Firestore query response.
     * Iterates through all returned documents and selects the one with maximum score.
     *
     * @param responseBody raw JSON response from Firestore query
     * @return ScoreDocument with highest score, or null if no valid documents found
     */
    private static ScoreDocument extractBestScoreDocument(String responseBody) {
        try {
            JSONArray documents = new JSONArray(responseBody);
            ScoreDocument best = null;
            for (int i = 0; i < documents.length(); i++) {
                JSONObject docContainer = documents.optJSONObject(i);
                if (docContainer == null || !docContainer.has("document")) {
                    continue;
                }

                JSONObject document = docContainer.getJSONObject("document");
                ScoreDocument parsed = parseScoreDocument(document);
                if (parsed == null) {
                    continue;
                }

                if (best == null || parsed.score > best.score) {
                    best = parsed;
                }
            }
            return best;
        } catch (Exception e) {
            System.err.println("Error reading existing score: " + e.getMessage());
            return null;
        }
    }

    /**
     * Parses a single Firestore document into a ScoreDocument.
     * Extracts document name and score value from Firestore JSON structure.
     *
     * @param document Firestore document JSON
     * @return ScoreDocument with parsed data, or null if parsing fails
     */
    private static ScoreDocument parseScoreDocument(JSONObject document) {
        try {
            String name = document.optString("name", null);
            if (name == null) {
                return null;
            }

            JSONObject fields = document.getJSONObject("fields");
            int score = Integer.parseInt(fields.optJSONObject("score").optString("integerValue", "0"));
            return new ScoreDocument(name, score);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Internal data structure for representing a Firestore score document.
     * Contains document path and score value for comparison purposes.
     */
    /**
     * Internal data structure for representing a Firestore score document.
     * Contains document path and score value for comparison purposes.
     */
    private static final class ScoreDocument {
        /** Full Firestore document path (e.g., "projects/.../scores/{docId}") */
        private final String documentName;

        /** Score value stored in this document */
        private final int score;

        /**
         * Constructs a ScoreDocument with document path and score.
         *
         * @param documentName full Firestore document path
         * @param score score value
         */
        private ScoreDocument(String documentName, int score) {
            this.documentName = documentName;
            this.score = score;
        }
    }
}
