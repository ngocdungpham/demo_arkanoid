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

public class LoginController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button signInButton;
    @FXML private Button signUpButton;
    @FXML private Text errorText;

    private Runnable onLoginSuccess;

    public void setOnLoginSuccess(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
    }

    @FXML
    private void handleSignIn() {
        String email = emailField.getText();
        String password = passwordField.getText();
        String name = nameField.getText(); // Lấy tên hiển thị

        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            showError("Vui lòng nhập Tên, Email và Mật khẩu.");
            return;
        }

        setLoading(true);
        AuthService.signIn(email, password).thenAccept(response -> {
            Platform.runLater(() -> {
                processAuthResponse(response, name);
                setLoading(false);
            });
        });
    }

    @FXML
    private void handleSignUp() {
        String email = emailField.getText();
        String password = passwordField.getText();
        String name = nameField.getText(); // Lấy tên hiển thị

        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            showError("Vui lòng nhập Tên, Email và Mật khẩu.");
            return;
        }

        setLoading(true);
        AuthService.signUp(email, password).thenAccept(response -> {
            Platform.runLater(() -> {
                processAuthResponse(response, name);
                setLoading(false);
            });
        });
    }

    private void processAuthResponse(String responseBody, String displayName) {
        JSONObject json = new JSONObject(responseBody);
        if (json.has("idToken")) {
            // Đăng nhập/Đăng ký thành công!
            String uid = json.getString("localId");
            String email = json.getString("email");
            String idToken = json.getString("idToken");

            // Lưu thông tin phiên đăng nhập
            PlayerContext.setSession(uid, email, idToken, displayName);

            // Gọi callback để chuyển cảnh
            if (onLoginSuccess != null) {
                onLoginSuccess.run();
            }
        } else if (json.has("error")) {
            String message = json.getJSONObject("error").getString("message");
            showError(message);
        } else {
            showError("Lỗi không xác định.");
        }
    }

    private void showError(String message) {
        errorText.setText(message);
    }

    private void setLoading(boolean isLoading) {
        signInButton.setDisable(isLoading);
        signUpButton.setDisable(isLoading);
    }
}