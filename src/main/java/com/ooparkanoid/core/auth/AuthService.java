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

/**
 * Service for handling Firebase Authentication operations.
 * Provides methods for user registration, login, and Google OAuth integration.
 * All operations are asynchronous using CompletableFuture for non-blocking execution.
 *
 * Supported authentication methods:
 * - Email/password registration
 * - Email/password login
 * - Google OAuth 2.0 sign-in
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public class AuthService {

    /** Firebase Web API Key for authentication requests */
    private static final String WEB_API_KEY = "AIzaSyAqwpyrrTJvnRZi1QCzYcXBOR-QeX_uWOg";

    /** Firebase REST API endpoint for user registration */
    private static final String SIGN_UP_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + WEB_API_KEY;

    /** Firebase REST API endpoint for email/password authentication */
    private static final String SIGN_IN_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + WEB_API_KEY;

    /** Firebase REST API endpoint for OAuth provider sign-in (Google, Facebook, etc.) */
    private static final String SIGN_IN_WITH_IDP_URL =
            "https://identitytoolkit.googleapis.com/v1/accounts:signInWithIdp?key=" + WEB_API_KEY;

    /** Shared HTTP client for all authentication requests */
    private static final HttpClient client = HttpClient.newBuilder().build();

    /**
     * Registers a new user with email and password.
     * Creates a new Firebase Authentication account.
     *
     * @param email the user's email address
     * @param password the user's password (must meet Firebase security requirements)
     * @return CompletableFuture containing JSON response with:
     *         - idToken: Firebase ID token for authenticated requests
     *         - email: registered email address
     *         - localId: unique user ID (uid)
     *         - refreshToken: token for obtaining new ID tokens
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
     * Authenticates an existing user with email and password.
     *
     * @param email the user's registered email address
     * @param password the user's password
     * @return CompletableFuture containing JSON response with:
     *         - idToken: Firebase ID token for authenticated requests
     *         - email: authenticated email address
     *         - localId: unique user ID (uid)
     *         - refreshToken: token for obtaining new ID tokens
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
     * Authenticates user via Google OAuth 2.0 integration.
     * Exchanges a Google ID token for a Firebase authentication token.
     *
     * The postBody must be form-encoded (not JSON) according to Firebase requirements.
     * Format: "id_token={encoded_token}&providerId=google.com"
     *
     * @param idToken Google ID token obtained from Google OAuth 2.0 flow
     * @return CompletableFuture containing JSON response with:
     *         - idToken: Firebase ID token for authenticated requests
     *         - email: user's Google account email
     *         - localId: unique user ID (uid)
     *         - federatedId: Google user identifier
     *         - providerId: OAuth provider identifier (google.com)
     *         - refreshToken: token for obtaining new ID tokens
     */
    public static CompletableFuture<String> signInWithGoogleIdToken(String idToken) {
        HttpClient client = HttpClient.newHttpClient();

        // Construct form-encoded postBody (required format for Firebase IDP sign-in)
        String postBody = "id_token=" + URLEncoder.encode(idToken, StandardCharsets.UTF_8)
                + "&providerId=google.com";

        // Build Firebase API request payload
        JSONObject payload = new JSONObject();
        payload.put("postBody", postBody);
        payload.put("requestUri", "http://localhost"); // Required but can be any valid URI
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