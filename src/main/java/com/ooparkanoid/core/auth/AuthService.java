package com.ooparkanoid.core.auth;

import org.json.JSONObject;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.net.http.*;

public class AuthService {

    // !!! THAY THẾ BẰNG WEB API KEY CỦA BẠN TỪ BƯỚC 2 !!!
    private static final String WEB_API_KEY = "AIzaSyAqwpyrrTJvnRZi1QCzYcXBOR-QeX_uWOg";

    private static final String SIGN_UP_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + WEB_API_KEY;
    private static final String SIGN_IN_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + WEB_API_KEY;

//    private static final String SIGN_IN_WITH_IDP_URL =
//            "https://identitytoolkit.googleapis.com/v1/accounts:signInWithIdp?key=" + WEB_API_KEY;

    private static final String SIGN_IN_WITH_IDP_URL =
            "https://identitytoolkit.googleapis.com/v1/accounts:signInWithIdp?key=" + WEB_API_KEY;

    private static final HttpClient client = HttpClient.newBuilder().build();

    /**
     * Gọi API để tạo người dùng mới.
     * Trả về JSON chứa idToken, email, localId (uid).
     */
    public static CompletableFuture<String> signUp(String email, String password) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("email", email);
        requestBody.put("password", password);
        requestBody.put("returnSecureToken", true);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SIGN_UP_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    /**
     * Gọi API để đăng nhập người dùng.
     * Trả về JSON chứa idToken, email, localId (uid).
     */
    public static CompletableFuture<String> signIn(String email, String password) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("email", email);
        requestBody.put("password", password);
        requestBody.put("returnSecureToken", true);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SIGN_IN_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    /**
     * THÊM HÀM MỚI NÀY
     * Đăng nhập vào Firebase bằng Google ID Token.
     * @param idToken Lấy từ luồng Google OAuth 2.0
     * @return CompletableFuture chứa response từ Firebase
     */

    public static CompletableFuture<String> signInWithGoogleIdToken(String idToken) {
        HttpClient client = HttpClient.newHttpClient();

        // postBody phải là CHUỖI form-encoded, KHÔNG phải JSON
        String postBody = "id_token=" + URLEncoder.encode(idToken, StandardCharsets.UTF_8)
                + "&providerId=google.com";

        JSONObject payload = new JSONObject();
        payload.put("postBody", postBody);
        payload.put("requestUri", "http://localhost");
        payload.put("returnSecureToken", true);
        payload.put("returnIdpCredential", true);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(SIGN_IN_WITH_IDP_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        return client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }
}