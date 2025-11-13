package com.ooparkanoid.ui;

import com.ooparkanoid.core.auth.AuthService;
import com.ooparkanoid.core.state.PlayerContext;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.concurrent.CompletableFuture;
// THÊM CÁC IMPORT NÀY
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;
import javafx.application.Platform;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.ooparkanoid.core.auth.AuthService;
import com.ooparkanoid.core.state.PlayerContext;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for the login screen managing user authentication.
 * Handles traditional email/password login and Google OAuth integration.
 * Implements FXML controller pattern for JavaFX UI integration.
 *
 * Features:
 * - Email/password authentication with Firebase
 * - Google OAuth 2.0 desktop flow integration
 * - Automatic session management and player context updates
 * - Reactive UI with loading states and error handling
 * - Navigation callbacks for success and signup transitions
 *
 * Authentication Flow:
 * 1. User enters credentials or clicks Google sign-in
 * 2. UI shows loading state and disables inputs
 * 3. Authentication request sent to Firebase
 * 4. Response parsed and session established
 * 5. Success callback triggered or error displayed
 *
 * Google OAuth Implementation:
 * - Desktop OAuth 2.0 flow with local server receiver
 * - Automatic browser launch for user consent
 * - Token caching for subsequent logins
 * - ID token extraction for Firebase authentication
 *
 * Error Handling:
 * - Network connectivity issues
 * - Invalid credentials
 * - OAuth flow interruptions
 * - JSON parsing errors
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public class LoginController {

    /** Display name input field */
    @FXML private TextField nameField;

    /** Email address input field */
    @FXML private TextField emailField;

    /** Password input field */
    @FXML private PasswordField passwordField;

    /** Email/password sign-in button */
    @FXML private Button signInButton;

    /** Default text for sign-in button */
    private String defaultGoogleSignInText;

    /** Google OAuth sign-in button */
    @FXML private Button googleSignInButton;

    /** Error message display text */
    @FXML private Text errorText;

    /** Link to navigate to signup screen */
    @FXML private Text signUpLink;

    /** Google OAuth client secrets file path */
    private static final String CLIENT_SECRETS_FILE = "client_secrets.json";

    /** Directory for storing OAuth token cache */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), ".store/arkanoid_auth_cache");

    /** JSON factory for Google API client */
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    /** HTTP transport for Google API calls */
    private static NetHttpTransport HTTP_TRANSPORT;

    /** Data store factory for token persistence */
    private static FileDataStoreFactory dataStoreFactory;

    /** Callback for successful login navigation */
    private Runnable onLoginSuccess;

    /** Default text for sign-in button */
    private String defaultSignInText;

    /** Callback for signup screen navigation */
    private Runnable onGoToSignUp;

    /**
     * Sets the callback to execute on successful login.
     * Typically used to navigate to the main menu or game screen.
     *
     * @param onLoginSuccess the Runnable to execute on login success
     */
    public void setOnLoginSuccess(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
    }

    /**
     * Sets the callback to execute when navigating to signup screen.
     *
     * @param onGoToSignUp the Runnable to execute for signup navigation
     */
    public void setOnGoToSignUp(Runnable onGoToSignUp) {
        this.onGoToSignUp = onGoToSignUp;
    }

    /**
     * Initializes the controller after FXML loading.
     * Sets up default button texts and initializes Google API transport.
     * Called automatically by JavaFX when the FXML is loaded.
     */
    @FXML
    private void initialize() {
        defaultSignInText = signInButton.getText();
        defaultGoogleSignInText = googleSignInButton.getText();

        // Initialize Google API transport
        try {
            HTTP_TRANSPORT = new NetHttpTransport();
            dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles email/password sign-in button click.
     * Validates input fields, initiates authentication, and processes response.
     */
    @FXML
    private void handleSignIn() {
        String email = emailField.getText();
        String password = passwordField.getText();
        String name = nameField.getText();

        if (!isInputValid(email, password, name)) {
            return;
        }

        performAuthentication(AuthService.signIn(email, password), name, AuthAction.SIGN_IN);
    }

    /**
     * Handles Google OAuth sign-in button click.
     * Initiates the desktop OAuth flow, obtains ID token, and authenticates with Firebase.
     * Runs the OAuth process asynchronously to avoid blocking the UI thread.
     */
    @FXML
    private void handleGoogleSignIn() {
        clearError();
        setLoading(true, AuthAction.GOOGLE_SIGN_IN);

        // Execute OAuth flow asynchronously to avoid blocking UI
        CompletableFuture.supplyAsync(() -> {
            try {
                // Step A: Obtain Google ID Token
                return getGoogleIdToken();
            } catch (Exception e) {
                // Handle errors if user closes browser or network issues occur
                throw new RuntimeException("Error obtaining Google ID Token: " + e.getMessage(), e);
            }
        }).thenCompose(idToken -> {
            if (idToken == null) {
                return CompletableFuture.completedFuture(AuthResult.failure("Unable to obtain Google ID Token."));
            }
            // Step B: With ID Token, authenticate with Firebase
            return AuthService.signInWithGoogleIdToken(idToken)
                    .thenApply(responseBody -> parseAuthResponse(responseBody, null));
        }).exceptionally(ex -> {
            // Handle general errors
            System.err.println("Google sign-in error: " + ex.getMessage());
            return AuthResult.failure("Google sign-in process failed.");
        }).thenAccept(result -> {
            // Step C: Update UI
            Platform.runLater(() -> {
                setLoading(false, AuthAction.GOOGLE_SIGN_IN);
                if (result.isSuccess()) {
                    finalizeLogin(result);
                } else {
                    showError(result.errorMessage());
                }
            });
        });
    }

    /**
     * Executes the OAuth 2.0 desktop flow to obtain a Google ID Token.
     * Launches browser for user consent, handles authorization code exchange,
     * and caches credentials for future use.
     *
     * @return the Google ID Token string, or null if flow fails
     * @throws Exception if OAuth flow encounters errors
     */
    private String getGoogleIdToken() throws Exception {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(CLIENT_SECRETS_FILE);
        if (in == null) {
            throw new FileNotFoundException("Resource file not found: " + CLIENT_SECRETS_FILE + ". Have you downloaded and placed it in src/main/resources?");
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets,
                Collections.singleton("openid email profile")) // Required scopes
                .setDataStoreFactory(dataStoreFactory)
                .setAccessType("offline")
                .build();

        // 1. Initialize receiver
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        String redirectUri = receiver.getRedirectUri();

        // 2. Generate URL and open browser
        String url = flow.newAuthorizationUrl().setRedirectUri(redirectUri).build();
        browse(url); // Open browser (implementation below)

        // 3. Wait for user consent and obtain authorization code
        String code = receiver.waitForCode();

        // 4. Exchange code for token response
        // This is the critical step - we need GoogleTokenResponse, not Credential
        com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse tokenResponse =
                (com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse) flow.newTokenRequest(code)
                        .setRedirectUri(redirectUri)
                        .execute();

        // 5. ✅ SUCCESSFULLY OBTAIN ID TOKEN!
        String idTokenString = tokenResponse.getIdToken();

        // 6. [Bonus] Create and store credential for future logins without re-auth
        // This saves the token (including refresh token) to DataStoreFactory
        flow.createAndStoreCredential(tokenResponse, "user");

        // 7. Remember to stop the server
        receiver.stop();

        return idTokenString;
    }

    /**
     * Opens the specified URL in the system's default browser.
     * Falls back to console message if desktop browsing is not supported.
     *
     * @param url the URL to open in the browser
     */
    private void browse(String url) {
        try {
            if (java.awt.Desktop.isDesktopSupported() &&
                    java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            } else {
                // Handle case where automatic browser opening is not supported
                System.err.println("Cannot open browser automatically. Please open this URL: " + url);
                showError("Cannot open browser. Please check console.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error opening browser.");
        }
    }

    /**
     * Handles click on the signup link to navigate to signup screen.
     */
    @FXML
    private void handleSignUpLinkClick() {
        if (onGoToSignUp != null) {
            onGoToSignUp.run();
        }
    }

    /**
     * Validates user input fields for authentication.
     * Checks that all required fields are non-empty.
     *
     * @param email the email input to validate
     * @param password the password input to validate
     * @param name the display name input to validate
     * @return true if all inputs are valid, false otherwise
     */
    private boolean isInputValid(String email, String password, String name) {
        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            showError("Please enter Name, Email, and Password.");
            return false;
        }

        clearError();
        return true;
    }

    /**
     * Performs authentication with the provided request and handles the response.
     * Manages loading states and error handling for both sign-in methods.
     *
     * @param request the authentication request future
     * @param displayName the display name for the user
     * @param action the type of authentication action being performed
     */
    private void performAuthentication(CompletableFuture<String> request,
                                       String displayName,
                                       AuthAction action) {
        setLoading(true, action);

        request
                .thenApply(responseBody -> parseAuthResponse(responseBody, displayName))
                .exceptionally(ex -> AuthResult.failure("Cannot connect to server. Please try again."))
                .thenAccept(result -> Platform.runLater(() -> {
                    setLoading(false, action);
                    if (result.isSuccess()) {
                        finalizeLogin(result);
                    } else {
                        showError(result.errorMessage());
                    }
                }));
    }

    /**
     * Parses the authentication response from Firebase.
     * Extracts user information and handles error cases.
     * Attempts to resolve display name from various response fields.
     *
     * @param responseBody the JSON response body from Firebase
     * @param displayName fallback display name if not provided in response
     * @return AuthResult containing success/failure status and user data
     */
    private AuthResult parseAuthResponse(String responseBody, String displayName) {
        try {
            JSONObject json = new JSONObject(responseBody);

            if (json.has("error")) {
                JSONObject errorObject = json.getJSONObject("error");
                String message = errorObject.optString("message", "Unknown error.");
                return AuthResult.failure(translateErrorMessage(message));
            }

            if (!json.has("idToken")) {
                return AuthResult.failure("Invalid response from server.");
            }

            String uid = json.optString("localId", "");
            if (uid.isBlank()) {
                return AuthResult.failure("Cannot identify user account.");
            }

            String email = json.optString("email", "");
            String idToken = json.getString("idToken");
            String resolvedDisplayName = resolveDisplayName(json, displayName);
            return AuthResult.success(uid, email, idToken, resolvedDisplayName);

        } catch (JSONException ex) {
            return AuthResult.failure("Invalid response from server.");
        }
    }

    /**
     * Resolves the display name from the authentication response.
     * Tries multiple fields in order of preference, with fallbacks.
     *
     * @param json the JSON response object
     * @param fallbackDisplayName fallback name if none found in response
     * @return the resolved display name, or default if none available
     */
    private String resolveDisplayName(JSONObject json, String fallbackDisplayName) {
        String displayName = json.optString("displayName", "");
        if (!displayName.isBlank()) {
            return displayName.trim();
        }

        displayName = json.optString("fullName", "");
        if (!displayName.isBlank()) {
            return displayName.trim();
        }

        String firstName = json.optString("firstName", "");
        String lastName = json.optString("lastName", "");
        String combined = (firstName + " " + lastName).trim();
        if (!combined.isBlank()) {
            return combined;
        }

        String rawUserInfo = json.optString("rawUserInfo", "");
        if (!rawUserInfo.isBlank()) {
            try {
                JSONObject raw = new JSONObject(rawUserInfo);
                String rawDisplayName = raw.optString("displayName", "");
                if (!rawDisplayName.isBlank()) {
                    return rawDisplayName.trim();
                }
                JSONObject nameObject = raw.optJSONObject("name");
                if (nameObject != null) {
                    String given = nameObject.optString("givenName", "");
                    String family = nameObject.optString("familyName", "");
                    String rawCombined = (given + " " + family).trim();
                    if (!rawCombined.isBlank()) {
                        return rawCombined;
                    }
                }
            } catch (JSONException ignored) {
                // Ignore if rawUserInfo is not valid JSON
            }
        }

        if (fallbackDisplayName != null && !fallbackDisplayName.isBlank()) {
            return fallbackDisplayName.trim();
        }

        String email = json.optString("email", "");
        if (!email.isBlank()) {
            int atIndex = email.indexOf('@');
            if (atIndex > 0) {
                return email.substring(0, atIndex);
            }
            return email;
        }

        return PlayerContext.playerName != null ? PlayerContext.playerName : "Player";
    }

    /**
     * Finalizes the login process by updating player context and triggering success callback.
     *
     * @param result the successful authentication result
     */
    private void finalizeLogin(AuthResult result) {
        PlayerContext.setSession(result.uid(), result.email(), result.idToken(), result.displayName());
        clearError();
        if (onLoginSuccess != null) {
            onLoginSuccess.run();
        }
    }

    /**
     * Translates Firebase error codes into user-friendly Vietnamese messages.
     *
     * @param message the raw Firebase error message
     * @return translated user-friendly error message
     */
    private String translateErrorMessage(String message) {
        String normalized = message.split(":")[0];
        return switch (normalized) {
            case "EMAIL_EXISTS" -> "Email is already registered. Please sign in.";
            case "INVALID_PASSWORD" -> "Incorrect password.";
            case "EMAIL_NOT_FOUND" -> "No account found with this email.";
            case "USER_DISABLED" -> "This account has been disabled.";
            case "TOO_MANY_ATTEMPTS_TRY_LATER" -> "Too many attempts. Please try again later.";
            default -> "Error: " + message.replace('_', ' ');
        };
    }

    /**
     * Displays an error message to the user.
     *
     * @param message the error message to display
     */
    private void showError(String message) {
        errorText.setText(message);
    }

    /**
     * Clears any displayed error message.
     */
    private void clearError() {
        errorText.setText("");
    }

    /**
     * Sets the loading state for UI controls during authentication.
     * Disables input fields and updates button text based on action type.
     *
     * @param isLoading true to show loading state, false to restore normal state
     * @param action the type of authentication action being performed
     */
    private void setLoading(boolean isLoading, AuthAction action) {
        signInButton.setDisable(isLoading);
        signUpLink.setDisable(isLoading);
        googleSignInButton.setDisable(isLoading);
        nameField.setDisable(isLoading);
        emailField.setDisable(isLoading);
        passwordField.setDisable(isLoading);
        googleSignInButton.setText(defaultGoogleSignInText);

        if (isLoading) {
            // Set loading text only for the pressed button
            switch (action) {
                case SIGN_IN:
                    signInButton.setText("Signing in...");
                    break;
                case GOOGLE_SIGN_IN:
                    googleSignInButton.setText("Waiting for Google...");
                    break;
            }
        }
    }

    /**
     * Enumeration of authentication action types for UI state management.
     */
    private enum AuthAction {
        /** Email/password sign-in action */
        SIGN_IN,

        /** User registration action */
        SIGN_UP,

        /** Google OAuth sign-in action */
        GOOGLE_SIGN_IN
    }

    /**
     * Record representing the result of an authentication attempt.
     * Contains success status and either user data or error message.
     */
    private record AuthResult(boolean isSuccess,
                              String uid,
                              String email,
                              String idToken,
                              String displayName,
                              String errorMessage) {

        /**
         * Creates a successful authentication result.
         *
         * @param uid user unique identifier
         * @param email user email address
         * @param idToken Firebase ID token
         * @param displayName user display name
         * @return successful AuthResult
         */
        static AuthResult success(String uid, String email, String idToken, String displayName) {
            return new AuthResult(true, uid, email, idToken, displayName, null);
        }

        /**
         * Creates a failed authentication result.
         *
         * @param errorMessage the error message describing the failure
         * @return failed AuthResult
         */
        static AuthResult failure(String errorMessage) {
            return new AuthResult(false, null, null, null, null, errorMessage);
        }
    }
}
