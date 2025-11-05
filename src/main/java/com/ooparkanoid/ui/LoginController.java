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

public class LoginController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button signInButton;
    @FXML private Button signUpButton;
    @FXML private Text errorText;

    private Runnable onLoginSuccess;
    private String defaultSignInText;
    private String defaultSignUpText;

    public void setOnLoginSuccess(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
    }

    @FXML
    private void initialize() {
        defaultSignInText = signInButton.getText();
        defaultSignUpText = signUpButton.getText();
    }

    @FXML
    private void handleSignIn() {
        String email = emailField.getText();
        String password = passwordField.getText();
        String name = nameField.getText(); // Lấy tên hiển thị

//        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
//            showError("Vui lòng nhập Tên, Email và Mật khẩu.");
        if (!isInputValid(email, password, name)) {
            return;
        }

//        setLoading(true);
//        AuthService.signIn(email, password).thenAccept(response -> {
//            Platform.runLater(() -> {
//                processAuthResponse(response, name);
//                setLoading(false);
//            });
//        });
//        AuthService.signIn(email, password)
//                .thenAccept(response -> {
//                    Platform.runLater(() -> {
//                        processAuthResponse(response, name);
//                        setLoading(false);
//                    });
//                })
//                .exceptionally(ex -> {
//                    Platform.runLater(() -> {
//                        showError("Không thể kết nối đến máy chủ. Vui lòng thử lại.");
//                        setLoading(false);
//                    });
//                    return null;
//                });

        performAuthentication(AuthService.signIn(email, password), name, AuthAction.SIGN_IN);
    }

    @FXML
    private void handleSignUp() {
        String email = emailField.getText();
        String password = passwordField.getText();
        String name = nameField.getText(); // Lấy tên hiển thị

        if (!isInputValid(email, password, name)) {
            return;
        }

        performAuthentication(AuthService.signUp(email, password), name, AuthAction.SIGN_UP);
    }
    private boolean isInputValid(String email, String password, String name) {
        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            showError("Vui lòng nhập Tên, Email và Mật khẩu.");
            return false;
        }

//        setLoading(true);
//        AuthService.signUp(email, password).thenAccept(response -> {
//            Platform.runLater(() -> {
//                processAuthResponse(response, name);
//                setLoading(false);
//            });
//        });
//        AuthService.signUp(email, password)
//                .thenAccept(response -> {
//                    Platform.runLater(() -> {
//                        processAuthResponse(response, name);
//                        setLoading(false);
//                    });
//                })
//                .exceptionally(ex -> {
//                    Platform.runLater(() -> {
//                        showError("Không thể kết nối đến máy chủ. Vui lòng thử lại.");
//                        setLoading(false);
//                    });
//                    return null;
//                });
        clearError();
        return true;
    }

    //    private void processAuthResponse(String responseBody, String displayName) {
//        JSONObject json = new JSONObject(responseBody);
//        if (json.has("idToken")) {
//            // Đăng nhập/Đăng ký thành công!
//            String uid = json.getString("localId");
//            String email = json.getString("email");
//            String idToken = json.getString("idToken");
    private void performAuthentication(CompletableFuture<String> request,
                                       String displayName,
                                       AuthAction action) {
        setLoading(true, action);

        request
                .thenApply(responseBody -> parseAuthResponse(responseBody, displayName))
                .exceptionally(ex -> AuthResult.failure("Không thể kết nối đến máy chủ. Vui lòng thử lại."))
                .thenAccept(result -> Platform.runLater(() -> {
                    setLoading(false, action);
                    if (result.isSuccess()) {
                        finalizeLogin(result);
                    } else {
                        showError(result.errorMessage());
                    }
                }));
    }

    // Lưu thông tin phiên đăng nhập
//            PlayerContext.setSession(uid, email, idToken, displayName);
    private AuthResult parseAuthResponse(String responseBody, String displayName) {
        try {
            JSONObject json = new JSONObject(responseBody);
            if (json.has("idToken")) {
                String uid = json.getString("localId");
                String email = json.getString("email");
                String idToken = json.getString("idToken");
                return AuthResult.success(uid, email, idToken, displayName);
            }
            // Gọi callback để chuyển cảnh
//            if (onLoginSuccess != null) {
//                onLoginSuccess.run();
            if (json.has("error")) {
                JSONObject errorObject = json.getJSONObject("error");
                String message = errorObject.optString("message", "Lỗi không xác định.");
                return AuthResult.failure(translateErrorMessage(message));
            }
//        } else if (json.has("error")) {
//            String message = json.getJSONObject("error").getString("message");
//            showError(message);
//        } else {
//            showError("Lỗi không xác định.");
            return AuthResult.failure("Lỗi không xác định.");
        } catch (JSONException ex) {
            return AuthResult.failure("Phản hồi không hợp lệ từ máy chủ.");
        }
    }

    private void finalizeLogin(AuthResult result) {
        PlayerContext.setSession(result.uid(), result.email(), result.idToken(), result.displayName());
        clearError();
        if (onLoginSuccess != null) {
            onLoginSuccess.run();
        }
    }

    private String translateErrorMessage(String message) {
        String normalized = message.split(":")[0];
        return switch (normalized) {
            case "EMAIL_EXISTS" -> "Email đã được đăng ký. Vui lòng đăng nhập.";
            case "INVALID_PASSWORD" -> "Mật khẩu không chính xác.";
            case "EMAIL_NOT_FOUND" -> "Không tìm thấy tài khoản với email này.";
            case "USER_DISABLED" -> "Tài khoản của bạn đã bị vô hiệu hóa.";
            case "TOO_MANY_ATTEMPTS_TRY_LATER" -> "Bạn đã thử quá nhiều lần. Vui lòng thử lại sau.";
            default -> "Lỗi: " + message.replace('_', ' ');
        };
    }

    private void showError(String message) {
        errorText.setText(message);
    }

    //    private void setLoading(boolean isLoading) {
    private void clearError() {
        errorText.setText("");
    }

    private void setLoading(boolean isLoading, AuthAction action) {
        signInButton.setDisable(isLoading);
        signUpButton.setDisable(isLoading);
        nameField.setDisable(isLoading);
        emailField.setDisable(isLoading);
        passwordField.setDisable(isLoading);

        if (isLoading) {
            if (action == AuthAction.SIGN_IN) {
                signInButton.setText("Đang đăng nhập...");
                signUpButton.setText(defaultSignUpText);
            } else {
                signUpButton.setText("Đang đăng ký...");
                signInButton.setText(defaultSignInText);
            }
        } else {
            signInButton.setText(defaultSignInText);
            signUpButton.setText(defaultSignUpText);
        }
    }

    private enum AuthAction {
        SIGN_IN,
        SIGN_UP
    }

    private record AuthResult(boolean isSuccess,
                              String uid,
                              String email,
                              String idToken,
                              String displayName,
                              String errorMessage) {

        static AuthResult success(String uid, String email, String idToken, String displayName) {
            return new AuthResult(true, uid, email, idToken, displayName, null);
        }

        static AuthResult failure(String errorMessage) {
            return new AuthResult(false, null, null, null, null, errorMessage);
        }

        public boolean isSuccess() {
            return isSuccess;
        }

        public String errorMessage() {
            return errorMessage;
        }

        public String uid() {
            return uid;
        }

        public String email() {
            return email;
        }

        public String idToken() {
            return idToken;
        }

        public String displayName() {
            return displayName;
        }
    }
}