package com.ooparkanoid.core.score;

import com.ooparkanoid.AlertBox;
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
 * Quản lý việc GỬI và LẤY điểm số từ Google Firebase Firestore REST API.
 */
public final class FirebaseScoreService {

    // !!! THAY THẾ BẰNG PROJECT ID CỦA BẠN TỪ BƯỚC 1 !!!
    private static final String PROJECT_ID = "coffehouseuet201";

    // URL cơ sở của Firestore REST API
    private static final String BASE_URL = "https://firestore.googleapis.com/v1/projects/"
            + PROJECT_ID + "/databases/(default)/documents";

    private static final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    /**
     * Gửi điểm số mới lên Firestore.
     */
    public static void submitScore(ScoreEntry entry) {
        if (entry == null || entry.getScore() <= 0) {
            return;
        }

        if (!PlayerContext.isLoggedIn()) {
            System.err.println("Chưa đăng nhập, không thể gửi điểm!");
            return;
        }

//        try {
//            // 1. Tạo đối tượng JSON cho ScoreEntry
//            // Định dạng này là bắt buộc của Firestore
//            JSONObject fields = new JSONObject();
////            fields.put("playerName", new JSONObject().put("stringValue", entry.getPlayerName()));
//            fields.put("score", new JSONObject().put("integerValue", entry.getScore()));
//            fields.put("playerName", new JSONObject().put("stringValue", PlayerContext.playerName)); // Lấy tên từ Context
////            fields.put("score", ...);
//            fields.put("roundsPlayed", new JSONObject().put("integerValue", entry.getRoundsPlayed()));
//            fields.put("totalSeconds", new JSONObject().put("doubleValue", entry.getTotalSeconds()));
//            // Thêm một timestamp để biết điểm nào là mới nhất
//            fields.put("createdAt", new JSONObject().put("timestampValue", Instant.now().toString()));
//            fields.put("userId", new JSONObject().put("stringValue", PlayerContext.uid));
//
//            JSONObject requestBody = new JSONObject();
//            requestBody.put("fields", fields);
//
//            // Tạo yêu cầu Post
////            String url = BASE_URL + "/scores?auth=" + PlayerContext.idToken;
//
//            // 2. Tạo yêu cầu POST để *tạo* tài liệu mới
//            // Gửi đến collection 'scores'
////            HttpRequest request = HttpRequest.newBuilder()
//            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
//                    .uri(URI.create(BASE_URL + "/scores"))
//                    .header("Content-Type", "application/json")
////                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
////                    .build();
//                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()));
//
//            if (PlayerContext.idToken != null && !PlayerContext.idToken.isEmpty()) {
//                requestBuilder.header("Authorization", "Bearer " + PlayerContext.idToken);
//            }
//
//            HttpRequest request = requestBuilder.build();
//
//            // 3. Gửi bất đồng bộ
//            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
////                    .thenApply(HttpResponse::body)
////                    .thenAccept(body -> System.out.println("Gửi điểm lên Firebase: Thành công!"))
//                    .thenAccept(response -> {
//                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
//                            System.out.println("Gửi điểm lên Firebase: Thành công!");
//                        } else {
//                            System.err.println("Gửi điểm lên Firebase thất bại. Mã: "
//                                    + response.statusCode() + ", Nội dung: " + response.body());
//                        }
//                    })
//                    .exceptionally(e -> {
//                        System.err.println("Lỗi khi gửi điểm lên Firebase: " + e.getMessage());
//                        return null;
//                    });
//
//        } catch (Exception e) {
//            System.err.println("Không thể tạo yêu cầu gửi điểm Firebase: " + e.getMessage());
        String userId = PlayerContext.uid;
        if (userId == null || userId.isBlank()) {
            System.err.println("Không có userId, bỏ qua gửi điểm!");
            return;
        }
        fetchExistingScore(userId)
                .thenCompose(existing -> {
                    if (existing != null && existing.score >= entry.getScore()) {
                        System.out.println("Điểm hiện có cao hơn hoặc bằng điểm mới. Bỏ qua cập nhật.");
                        return CompletableFuture.completedFuture(null);
                    }
                    String documentName = existing != null ? existing.documentName : null;
                    return upsertScore(entry, userId, documentName);
                })
                .exceptionally(e -> {
                    System.err.println("Không thể gửi điểm lên Firebase: " + e.getMessage());
                    return null;
                });
    }

    /**
     * Lấy Top 10 điểm cao nhất từ Firestore.
     */
    public static CompletableFuture<List<ScoreEntry>> getTopScores() {

        // 1. Tạo một JSON payload để TRUY VẤN
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

        // 2. Tạo yêu cầu POST để *chạy truy vấn*
//        HttpRequest request = HttpRequest.newBuilder()
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + ":runQuery"))
                .header("Content-Type", "application/json")
//                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
//                .build();
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()));

        if (PlayerContext.idToken != null && !PlayerContext.idToken.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + PlayerContext.idToken);
        }

        HttpRequest request = requestBuilder.build();

        // 3. Gửi và xử lý kết quả
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(FirebaseScoreService::parseFirestoreResponse);
    }

    /**
     * Xử lý chuỗi JSON phức tạp trả về từ Firestore.
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

                // Lấy từng trường một cách an toàn
                String name = fields.optJSONObject("playerName").optString("stringValue", "Player");
                int score = Integer.parseInt(fields.optJSONObject("score").optString("integerValue", "0"));
                int rounds = Integer.parseInt(fields.optJSONObject("roundsPlayed").optString("integerValue", "1"));
                double seconds = fields.optJSONObject("totalSeconds").optDouble("doubleValue", 0.0);

                entries.add(new ScoreEntry(name, score, rounds, seconds));
            }
        } catch (Exception e) {
            System.err.println("Lỗi parse JSON từ Firebase: " + e.getMessage());
            e.printStackTrace();
        }
        return entries;
    }
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
                            System.out.println("Cập nhật điểm Firebase thành công!");
                        } else {
                            System.err.println("Cập nhật điểm Firebase thất bại. Mã: "
                                    + response.statusCode() + ", Nội dung: " + response.body());
                        }
                    })
                    .exceptionally(e -> {
                        System.err.println("Lỗi khi cập nhật điểm Firebase: " + e.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }

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
            System.err.println("Lỗi khi đọc điểm hiện có: " + e.getMessage());
            return null;
        }
    }

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

    private static final class ScoreDocument {
        private final String documentName;
        private final int score;

        private ScoreDocument(String documentName, int score) {
            this.documentName = documentName;
            this.score = score;
        }
    }
}
