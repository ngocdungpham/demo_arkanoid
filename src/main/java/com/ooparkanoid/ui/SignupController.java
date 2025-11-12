// src/main/java/com/ooparkanoid/ui/SignupController.java
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

import java.util.concurrent.CompletableFuture;

/**
 * Controller for the user registration screen managing account creation.
 * Handles traditional email/password signup and provides navigation to login.
 * Implements FXML controller pattern for JavaFX UI integration.
 *
 * Features:
 * - Email/password account registration with Firebase
 * - Password confirmation validation
 * - Automatic session management and player context updates
 * - Reactive UI with loading states and error handling
 * - Navigation callbacks for success and login transitions
 *
 * Registration Flow:
 * 1. User enters registration details (name, email, password, confirm password)
 * 2. UI validates input fields and shows loading state
 * 3. Registration request sent to Firebase
 * 4. Response parsed and session established
 * 5. Success callback triggered or error displayed
 *
 * Validation Rules:
 * - All fields must be non-empty
 * - Password must match confirmation
 * - Password must be at least 6 characters
 * - Email format validation handled by Firebase
 *
 * Error Handling:
 * - Network connectivity issues
 * - Invalid input validation
 * - Firebase registration errors (email exists, weak password, etc.)
 * - JSON parsing errors
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public class SignupController {

    /** Display name input field */
    @FXML private TextField nameField;

    /** Email address input field */
    @FXML private TextField emailField;

    /** Password input field */
    @FXML private PasswordField passwordField;

    /** Password confirmation input field */
    @FXML private PasswordField confirmPasswordField;

    /** Registration button */
    @FXML private Button signUpButton;

    /** Google OAuth registration button (currently disabled) */
    @FXML private Button googleSignInButton;

    /** Error message display text */
    @FXML private Text errorText;

    /** Link to navigate to login screen */
    @FXML private Text loginLink;

    /** Callback for successful registration navigation */
    private Runnable onSignUpSuccess;

    /** Callback for login screen navigation */
    private Runnable onGoToLogin;

    /** Default text for registration button */
    private String defaultSignUpText;

    /**
     * Sets the callback to execute on successful registration.
     * Typically used to navigate to the main menu or game screen.
     *
     * @param onSignUpSuccess the Runnable to execute on registration success
     */
    public void setOnSignUpSuccess(Runnable onSignUpSuccess) {
        this.onSignUpSuccess = onSignUpSuccess;
    }

    /**
     * Sets the callback to execute when navigating to login screen.
     *
     * @param onGoToLogin the Runnable to execute for login navigation
     */
    public void setOnGoToLogin(Runnable onGoToLogin) {
        this.onGoToLogin = onGoToLogin;
    }

    /**
     * Initializes the controller after FXML loading.
     * Sets up default button texts and disables Google sign-in temporarily.
     * Called automatically by JavaFX when the FXML is loaded.
     */
    @FXML
    private void initialize() {
        defaultSignUpText = signUpButton.getText();
        googleSignInButton.setDisable(true); // Temporarily disable Google sign-in
    }

    /**
     * Handles registration button click.
     * Validates input fields, initiates registration, and processes response.
     */
    @FXML
    private void handleSignUp() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (!isInputValid(email, password, name, confirmPassword)) {
            return;
        }

        setLoading(true);
        // Use AuthService.signUp for registration
        performAuthentication(AuthService.signUp(email, password), name);
    }

    /**
     * Handles Google OAuth registration button click.
     * Currently displays an error message as Google sign-in is not yet implemented.
     */
    @FXML
    private void handleGoogleSignIn() {
        // (Google sign-in logic would be more complex and require SDK)
        showError("Google sign-in feature is not yet supported.");
    }

    /**
     * Handles click on the login link to navigate to login screen.
     */
    @FXML
    private void handleLoginLinkClick() {
        if (onGoToLogin != null) {
            onGoToLogin.run();
        }
    }

    /**
     * Validates user input fields for registration.
     * Checks that all required fields are filled and passwords match.
     *
     * @param email the email input to validate
     * @param password the password input to validate
     * @param name the display name input to validate
     * @param confirmPassword the password confirmation input to validate
     * @return true if all inputs are valid, false otherwise
     */
    private boolean isInputValid(String email, String password, String name, String confirmPassword) {
        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please enter all required information.");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            return false;
        }
        if (password.length() < 6) {
            showError("Password must be at least 6 characters long.");
            return false;
        }
        clearError();
        return true;
    }

    /**
     * Performs authentication with the provided request and handles the response.
     * Manages loading states and error handling for registration.
     *
     * @param request the registration request future
     * @param displayName the display name for the user
     */
    private void performAuthentication(CompletableFuture<String> request, String displayName) {
        setLoading(true);
        request
                .thenApply(responseBody -> parseAuthResponse(responseBody, displayName))
                .exceptionally(ex -> AuthResult.failure("Cannot connect to server. Please try again."))
                .thenAccept(result -> Platform.runLater(() -> {
                    setLoading(false);
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
     *
     * @param responseBody the JSON response body from Firebase
     * @param displayName the display name for the user
     * @return AuthResult containing success/failure status and user data
     */
    private AuthResult parseAuthResponse(String responseBody, String displayName) {
        try {
            JSONObject json = new JSONObject(responseBody);
            if (json.has("idToken")) {
                String uid = json.getString("localId");
                String email = json.getString("email");
                String idToken = json.getString("idToken");
                return AuthResult.success(uid, email, idToken, displayName);
            }
            if (json.has("error")) {
                String message = json.getJSONObject("error").optString("message", "Unknown error.");
                return AuthResult.failure(translateErrorMessage(message));
            }
            return AuthResult.failure("Unknown error.");
        } catch (Exception ex) {
            return AuthResult.failure("Invalid response from server.");
        }
    }

    /**
     * Finalizes the registration process by updating player context and triggering success callback.
     *
     * @param result the successful authentication result
     */
    private void finalizeLogin(AuthResult result) {
        PlayerContext.setSession(result.uid(), result.email(), result.idToken(), result.displayName());
        clearError();
        if (onSignUpSuccess != null) {
            onSignUpSuccess.run();
        }
    }

    /**
     * Translates Firebase error codes into user-friendly messages.
     *
     * @param message the raw Firebase error message
     * @return translated user-friendly error message
     */
    private String translateErrorMessage(String message) {
        return switch (message.split(":")[0]) {
            case "EMAIL_EXISTS" -> "Email is already registered. Please sign in.";
            case "INVALID_PASSWORD" -> "Incorrect password.";
            case "EMAIL_NOT_FOUND" -> "No account found with this email.";
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
     * Sets the loading state for UI controls during registration.
     * Disables input fields and updates button text based on loading state.
     *
     * @param isLoading true to show loading state, false to restore normal state
     */
    private void setLoading(boolean isLoading) {
        signUpButton.setDisable(isLoading);
        googleSignInButton.setDisable(isLoading);
        loginLink.setDisable(isLoading);
        nameField.setDisable(isLoading);
        emailField.setDisable(isLoading);
        passwordField.setDisable(isLoading);
        confirmPasswordField.setDisable(isLoading);
        signUpButton.setText(isLoading ? "REGISTERING..." : defaultSignUpText);
    }

    /**
     * Record representing the result of a registration attempt.
     * Contains success status and either user data or error message.
     */
    private record AuthResult(boolean isSuccess,
                              String uid,
                              String email,
                              String idToken,
                              String displayName,
                              String errorMessage) {

        /**
         * Creates a successful registration result.
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
         * Creates a failed registration result.
         *
         * @param errorMessage the error message describing the failure
         * @return failed AuthResult
         */
        static AuthResult failure(String errorMessage) {
            return new AuthResult(false, null, null, null, null, errorMessage);
        }
    }
}

