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

public class SignupController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField; // Thêm trường này
    @FXML private Button signUpButton;
    @FXML private Button googleSignInButton;
    @FXML private Text errorText;
    @FXML private Text loginLink; // Thêm link này

    private Runnable onSignUpSuccess;
    private Runnable onGoToLogin; // Callback để quay lại Login

    private String defaultSignUpText;

    public void setOnSignUpSuccess(Runnable onSignUpSuccess) {
        this.onSignUpSuccess = onSignUpSuccess;
    }

    public void setOnGoToLogin(Runnable onGoToLogin) {
        this.onGoToLogin = onGoToLogin;
    }

    @FXML
    private void initialize() {
        defaultSignUpText = signUpButton.getText();
        googleSignInButton.setDisable(true); // Tạm vô hiệu hóa nút Google
    }

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
        // Dùng logic AuthService.signUp của bạn
        performAuthentication(AuthService.signUp(email, password), name);
    }

    @FXML
    private void handleGoogleSignIn() {
        // (Logic đăng nhập Google sẽ phức tạp hơn và cần SDK)
        showError("Tính năng đăng nhập Google chưa được hỗ trợ.");
    }

    @FXML
    private void handleLoginLinkClick() {
        if (onGoToLogin != null) {
            onGoToLogin.run();
        }
    }

    private boolean isInputValid(String email, String password, String name, String confirmPassword) {
        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || confirmPassword.isEmpty()) {
            showError("Vui lòng nhập đầy đủ thông tin.");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            showError("Mật khẩu nhập lại không khớp.");
            return false;
        }
        if (password.length() < 6) {
            showError("Mật khẩu phải có ít nhất 6 ký tự.");
            return false;
        }
        clearError();
        return true;
    }

    // Tái sử dụng logic xác thực từ LoginController
    private void performAuthentication(CompletableFuture<String> request, String displayName) {
        setLoading(true);
        request
                .thenApply(responseBody -> parseAuthResponse(responseBody, displayName))
                .exceptionally(ex -> AuthResult.failure("Không thể kết nối đến máy chủ. Vui lòng thử lại."))
                .thenAccept(result -> Platform.runLater(() -> {
                    setLoading(false);
                    if (result.isSuccess()) {
                        finalizeLogin(result);
                    } else {
                        showError(result.errorMessage());
                    }
                }));
    }

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
                String message = json.getJSONObject("error").optString("message", "Lỗi không xác định.");
                return AuthResult.failure(translateErrorMessage(message));
            }
            return AuthResult.failure("Lỗi không xác định.");
        } catch (Exception ex) {
            return AuthResult.failure("Phản hồi không hợp lệ từ máy chủ.");
        }
    }

    private void finalizeLogin(AuthResult result) {
        PlayerContext.setSession(result.uid(), result.email(), result.idToken(), result.displayName());
        clearError();
        if (onSignUpSuccess != null) {
            onSignUpSuccess.run();
        }
    }

    private String translateErrorMessage(String message) {
        return switch (message.split(":")[0]) {
            case "EMAIL_EXISTS" -> "Email đã được đăng ký. Vui lòng đăng nhập.";
            case "INVALID_PASSWORD" -> "Mật khẩu không chính xác.";
            case "EMAIL_NOT_FOUND" -> "Không tìm thấy tài khoản với email này.";
            default -> "Lỗi: " + message.replace('_', ' ');
        };
    }

    private void showError(String message) { errorText.setText(message); }
    private void clearError() { errorText.setText(""); }

    private void setLoading(boolean isLoading) {
        signUpButton.setDisable(isLoading);
        googleSignInButton.setDisable(isLoading);
        loginLink.setDisable(isLoading);
        nameField.setDisable(isLoading);
        emailField.setDisable(isLoading);
        passwordField.setDisable(isLoading);
        confirmPasswordField.setDisable(isLoading);
        signUpButton.setText(isLoading ? "ĐANG ĐĂNG KÝ..." : defaultSignUpText);
    }

    // Lớp record nội bộ để xử lý kết quả (giống như trong LoginController)
    private record AuthResult(boolean isSuccess, String uid, String email, String idToken, String displayName, String errorMessage) {
        static AuthResult success(String u, String e, String t, String d) { return new AuthResult(true, u, e, t, d, null); }
        static AuthResult failure(String e) { return new AuthResult(false, null, null, null, null, e); }
    }
}