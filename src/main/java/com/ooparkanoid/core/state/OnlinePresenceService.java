package com.ooparkanoid.core.state;

import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import com.ooparkanoid.core.state.PlayerContext;

/**
 * Quản lý trạng thái online của người chơi
 * sử dụng Firebase Realtime Database (RTDB).
 */
public class OnlinePresenceService {

    // !!! THAY THẾ BẰNG URL CỦA RTDB TỪ BƯỚC 1 !!!
    private static final String RTDB_URL = "https://coffehouseuet201-default-rtdb.asia-southeast1.firebasedatabase.app/";

    private static final HttpClient client = HttpClient.newBuilder().build();

    /**
     * Báo danh "Online" lên server.
     * @param playerName Tên người chơi (đã được làm sạch)
     */
    public static void goOnline(String uid) {
        if (uid == null || uid.isEmpty() || !PlayerContext.isLoggedIn()) {
            return;
        }

        try {
            // Tạo một đối tượng JSON để lưu
            JSONObject presenceData = new JSONObject();
            presenceData.put("isOnline", true);
            presenceData.put("lastSeen", Instant.now().toString());
            presenceData.put("name", PlayerContext.playerName); // them ten de hien thi


            String url = RTDB_URL + "presence/" + uid + ".json?auth=" + PlayerContext.idToken;
            // Tên người chơi sẽ là "key" trong CSDL
            // Chúng ta dùng "PUT" để ghi đè (hoặc tạo mới) dữ liệu
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(presenceData.toString()))
                    .build();

            // Gửi bất đồng bộ
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenRun(() -> System.out.println(PlayerContext.playerName + " đã online!"));

        } catch (Exception e) {
            System.err.println("Lỗi khi báo danh online: " + e.getMessage());
        }
    }

    /**
     * Báo danh "Offline" khi người dùng thoát game.
     * @param playerName Tên người chơi
     */
    public static void goOffline(String uid) {
        if (uid == null || uid.isEmpty() || !PlayerContext.isLoggedIn()) {
            return;
        }

        try {
            // Chúng ta có thể dùng DELETE để xóa hoàn toàn người chơi khỏi danh sách
            // Hoặc dùng PUT để cập nhật { "isOnline": false }

            String url = RTDB_URL + "presence/" + uid + ".json?auth=" + PlayerContext.idToken;

            // Cách 1: Xóa luôn
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .DELETE()
                    .build();

            // Gửi bất đồng bộ
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenRun(() -> System.out.println(PlayerContext.playerName + " đã offline."));

        } catch (Exception e) {
            System.err.println("Lỗi khi báo danh offline: " + e.getMessage());
        }
    }

    /**
     * Firebase keys không thể chứa các ký tự như '.', '#', '$', '[', ']'
     * Chúng ta cần "làm sạch" tên người chơi.
     */
//    private static String sanitizePlayerName(String name) {
//        return name.replaceAll("[.#$\\[\\]]", "_");
//    }

    // (Bạn có thể thêm hàm getOnlinePlayers() ở đây
    // bằng cách gọi GET đến RTDB_URL + "presence.json")
}