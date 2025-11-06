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

public class LoginController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button signInButton;
    private String defaultGoogleSignInText; // THÊM DÒNG NÀY
    @FXML private Button googleSignInButton; // THÊM DÒNG NÀY
    //    @FXML private Button signUpButton;
    @FXML private Text errorText;
    @FXML private Text signUpLink; // <- THÊM LINK NÀY

    private static final String CLIENT_SECRETS_FILE = "client_secrets.json"; // File bạn tải về
    private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), ".store/arkanoid_auth_cache");
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static NetHttpTransport HTTP_TRANSPORT;
    private static FileDataStoreFactory dataStoreFactory;

    private Runnable onLoginSuccess;
    private String defaultSignInText;
    private Runnable onGoToSignUp; // <- THÊM CALLBACK NÀY
//    private String defaultSignUpText;

    public void setOnLoginSuccess(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
    }

    public void setOnGoToSignUp(Runnable onGoToSignUp) {
        this.onGoToSignUp = onGoToSignUp;
    }

    @FXML
    private void initialize() {
        defaultSignInText = signInButton.getText();
//        defaultSignUpText = signUpButton.getText();
        defaultGoogleSignInText = googleSignInButton.getText(); // THÊM DÒNG NÀY

        // Khởi tạo transport
        try {
            HTTP_TRANSPORT = new NetHttpTransport();
            dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    /**
     * THÊM HÀM MỚI NÀY
     */
    @FXML
    private void handleGoogleSignIn() {
//        String name = nameField.getText();
//        if (name.isEmpty()) {
//            showError("Vui lòng nhập Tên hiển thị trước khi đăng nhập bằng Google.");
//            return;
//        }
        clearError();
        setLoading(true, AuthAction.GOOGLE_SIGN_IN);

        // Chạy việc lấy token trong một luồng riêng để không block UI
        CompletableFuture.supplyAsync(() -> {
            try {
                // Bước A: Lấy Google ID Token
                return getGoogleIdToken();
            } catch (Exception e) {
                // Bắt lỗi nếu người dùng đóng trình duyệt hoặc có lỗi mạng
                throw new RuntimeException("Lỗi khi lấy Google ID Token: " + e.getMessage(), e);
            }
        }).thenCompose(idToken -> {
            if (idToken == null) {
                return CompletableFuture.completedFuture(AuthResult.failure("Không thể lấy Google ID Token."));
            }
            // Bước B: Có ID Token, giờ gọi Firebase
            return AuthService.signInWithGoogleIdToken(idToken)
//                    .thenApply(responseBody -> parseAuthResponse(responseBody, name));
                    .thenApply(responseBody -> parseAuthResponse(responseBody, null));
        }).exceptionally(ex -> {
            // Bắt lỗi chung
            System.err.println("Lỗi đăng nhập Google: " + ex.getMessage());
            return AuthResult.failure("Quá trình đăng nhập Google thất bại.");
        }).thenAccept(result -> {
            // Bước C: Cập nhật UI
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
     * THÊM HÀM MỚI NÀY
     * Thực hiện luồng OAuth 2.0 cho desktop app để lấy ID Token.
     */
    private String getGoogleIdToken() throws Exception {
//        InputStream in = LoginController.class.getResourceAsStream(CLIENT_SECRETS_FILE);
//        InputStream in = LoginController.class.getResourceAsStream("/" + CLIENT_SECRETS_FILE);
        // Dòng 143 (Code mới - đáng tin cậy hơn)
//        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(CLIENT_SECRETS_FILE);
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(CLIENT_SECRETS_FILE);
        if (in == null) {
            throw new FileNotFoundException("Không tìm thấy file resource: " + CLIENT_SECRETS_FILE + ". Bạn đã tải về và đặt nó vào src/main/resources chưa?");
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets,
                Collections.singleton("openid email profile")) // Yêu cầu scope
                .setDataStoreFactory(dataStoreFactory)
                .setAccessType("offline")
                .build();

        // 1. Khởi tạo receiver
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        String redirectUri = receiver.getRedirectUri();

        // 2. Tự tạo URL và mở trình duyệt
        String url = flow.newAuthorizationUrl().setRedirectUri(redirectUri).build();
        browse(url); // Mở trình duyệt (bạn cần thêm hàm browse bên dưới)

        // 3. Đợi người dùng đồng ý và lấy 'code'
        String code = receiver.waitForCode();

        // 4. Dùng 'code' để trao đổi lấy TokenResponse
        // Đây là bước quan trọng, chúng ta cần GoogleTokenResponse, không phải Credential
        com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse tokenResponse =
                (com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse) flow.newTokenRequest(code)
                        .setRedirectUri(redirectUri)
                        .execute();

        // 5. ✅ LẤY ID TOKEN THÀNH CÔNG!
        String idTokenString = tokenResponse.getIdToken();

        // 6. [Bonus] Tạo và lưu credential để lần sau không cần đăng nhập lại
        // Dòng này sẽ lưu token (bao gồm cả refresh token) vào DataStoreFactory
        flow.createAndStoreCredential(tokenResponse, "user");

        // 7. Nhớ dừng server lại
        receiver.stop();

        return idTokenString;
    }

    /**
     * THÊM HÀM NÀY: Dùng để mở trình duyệt
     */
    private void browse(String url) {
        try {
            if (java.awt.Desktop.isDesktopSupported() &&
                    java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            } else {
                // Xử lý nếu không hỗ trợ mở trình duyệt
                System.err.println("Không thể mở trình duyệt tự động. Vui lòng mở URL sau: " + url);
                showError("Không thể mở trình duyệt. Hãy kiểm tra console.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi khi mở trình duyệt.");
        }
    }

    @FXML
    private void handleSignUpLinkClick() {
        if (onGoToSignUp != null) {
            onGoToSignUp.run();
        }
    }

    private boolean isInputValid(String email, String password, String name) {
        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            showError("Vui lòng nhập Tên, Email và Mật khẩu.");
            return false;
        }

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
//            if (json.has("localId") && json.has("idToken")) {
//                String uid = json.getString("localId");
////                String email = json.getString("email");
////                String idToken = json.getString("idToken");
////                return AuthResult.success(uid, email, idToken, displayName);
//                String email = json.optString("email", "");
//                String idToken = json.getString("idToken");
//                return AuthResult.success(uid, email, idToken, displayName);
//            }
//
//            // Xử lý response từ email/password (code cũ của bạn)
//            if (json.has("idToken")) {
//                String uid = json.getString("localId");
//                String email = json.getString("email");
//                String idToken = json.getString("idToken");
//                return AuthResult.success(uid, email, idToken, displayName);
//            }
//
//            // Gọi callback để chuyển cảnh
            if (json.has("error")) {
                JSONObject errorObject = json.getJSONObject("error");
                String message = errorObject.optString("message", "Lỗi không xác định.");
                return AuthResult.failure(translateErrorMessage(message));
            }
//            return AuthResult.failure("Lỗi không xác định.");
            if (!json.has("idToken")) {
                return AuthResult.failure("Phản hồi không hợp lệ từ máy chủ.");
            }

            String uid = json.optString("localId", "");
            if (uid.isBlank()) {
                return AuthResult.failure("Không thể xác định tài khoản người dùng.");
            }

            String email = json.optString("email", "");
            String idToken = json.getString("idToken");
            String resolvedDisplayName = resolveDisplayName(json, displayName);
            return AuthResult.success(uid, email, idToken, resolvedDisplayName);

        } catch (JSONException ex) {


            return AuthResult.failure("Phản hồi không hợp lệ từ máy chủ.");
        }
    }

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
                // Bỏ qua nếu rawUserInfo không phải JSON hợp lệ
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

    private record AuthData(AuthResult authResult, String googleDisplayName) {}

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
        // signUpButton.setDisable(isLoading); // <- Xóa
        signUpLink.setDisable(isLoading); // <- Thêm
        googleSignInButton.setDisable(isLoading); // CẬP NHẬT
        nameField.setDisable(isLoading);
        emailField.setDisable(isLoading);
        passwordField.setDisable(isLoading);
        googleSignInButton.setText(defaultGoogleSignInText); // CẬP NHẬT

        if (isLoading) {
            // Chỉ đặt text "loading" cho nút được nhấn
            switch (action) {
                case SIGN_IN:
                    signInButton.setText("Đang đăng nhập...");
                    break;
                case GOOGLE_SIGN_IN:
                    googleSignInButton.setText("Đang chờ Google..."); // CẬP NHẬT
                    break;
            }
        }
    }

    private enum AuthAction {
        SIGN_IN,
        SIGN_UP,
        GOOGLE_SIGN_IN
    }

    //    private record AuthResult(boolean isSuccess,
//                              String uid,
//                              String email,
//                              String idToken,
//                              String displayName,
//                              String errorMessage) {
//
//        static AuthResult success(String uid, String email, String idToken, String displayName) {
//            return new AuthResult(true, uid, email, idToken, displayName, null);
//        }
//
//        static AuthResult failure(String errorMessage) {
//            return new AuthResult(false, null, null, null, null, errorMessage);
//        }
//
//        public boolean isSuccess() {
//            return isSuccess;
//        }
//
//        public String errorMessage() {
//            return errorMessage;
//        }
//
//        public String uid() {
//            return uid;
//        }
//
//        public String email() {
//            return email;
//        }
//
//        public String idToken() {
//            return idToken;
//        }
//
//        public String displayName() {
//            return displayName;
//        }
//    }
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
    }
}
