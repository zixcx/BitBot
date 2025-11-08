package com.bitbot.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class SupabaseDBService {

    private static final String PROJECT_URL = "https://epsgxftswbcwodbneaqd.supabase.co";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVwc2d4ZnRzd2Jjd29kYm5lYXFkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA2MDczODUsImV4cCI6MjA3NjE4MzM4NX0.Oyd9QSsLEWA6RC39Yp3ZAEOV-FGsWsrJI4yxl6N7ZiE";

    // ✅ 로그인된 사용자 정보 조회 (JWT 기반)
    public static String getUserProfile() {
        try {
            String token = AuthStorage.getAccessToken();
            if (token == null) return "❌ 토큰 없음 — 로그인 필요";

            URL url = new URL(PROJECT_URL + "/rest/v1/users?select=*");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("apikey", API_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Accept", "application/json");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            return sb.toString();
        } catch (Exception e) {
            return "❌ getUserProfile Error: " + e.getMessage();
        }
    }

    // ✅ 로그 추가 (예시)
    public static String insertLog(String model, String decision, double pnl) {
        try {
            String token = AuthStorage.getAccessToken();
            if (token == null) return "❌ 인증 필요";

            URL url = new URL(PROJECT_URL + "/rest/v1/logs");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("apikey", API_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String body = String.format("{\"model\":\"%s\", \"decision\":\"%s\", \"pnl\":%f}", model, decision, pnl);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            return "✅ 로그 등록 성공";
        } catch (Exception e) {
            return "❌ insertLog Error: " + e.getMessage();
        }
    }
}
