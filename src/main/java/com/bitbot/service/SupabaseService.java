package com.bitbot.service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.json.JSONObject;

public class SupabaseService {

    // ✅ Supabase 프로젝트 정보
    private static final String PROJECT_URL = "https://epsgxftswbcwodbneaqd.supabase.co";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVwc2d4ZnRzd2Jjd29kYm5lYXFkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA2MDczODUsImV4cCI6MjA3NjE4MzM4NX0.Oyd9QSsLEWA6RC39Yp3ZAEOV-FGsWsrJI4yxl6N7ZiE";

    /**
     * ✅ 회원가입 (Auth + users 테이블 자동 insert)
     */
    public static String signUp(String username, String email, String password) {
        try {
            // 1️⃣ Supabase Auth 회원 등록
            URL url = new URL(PROJECT_URL + "/auth/v1/signup");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("apikey", API_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // username을 metadata에 함께 저장
            String body = String.format(
                "{ \"email\": \"%s\", \"password\": \"%s\", \"data\": { \"username\": \"%s\" } }",
                email, password, username
            );

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            if (conn.getResponseCode() != 200 && conn.getResponseCode() != 201) {
                return "❌ 회원가입 실패: HTTP " + conn.getResponseCode();
            }

            // 결과 파싱
            Scanner sc = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8);
            String result = sc.useDelimiter("\\A").next();
            sc.close();

            JSONObject json = new JSONObject(result);
            if (!json.has("user")) return "⚠️ 회원가입 실패: " + result;

            // 2️⃣ 토큰 발급 (로그인 토큰)
            String token = getAccessToken(email, password);

            // 3️⃣ users 테이블에 username, email 저장
            if (token != null) {
                insertUserRow(username, email);
            }

            return "✅ 회원가입 성공! 로그인 해주세요.";

        } catch (Exception e) {
            return "❌ SignUp Error: " + e.getMessage();
        }
    }

    /**
     * ✅ 로그인 처리 (토큰 저장)
     */
    public static String signIn(String email, String password) {
        try {
            URL url = new URL(PROJECT_URL + "/auth/v1/token?grant_type=password");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("apikey", API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String body = String.format("{\"email\":\"%s\", \"password\":\"%s\"}", email, password);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            if (conn.getResponseCode() != 200) {
                return "❌ 로그인 실패: HTTP " + conn.getResponseCode();
            }

            Scanner sc = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8);
            String result = sc.useDelimiter("\\A").next();
            sc.close();

            JSONObject json = new JSONObject(result);
            if (json.has("access_token")) {
                String accessToken = json.getString("access_token");
                String refreshToken = json.getString("refresh_token");
                String userEmail = json.getJSONObject("user").getString("email");

                AuthStorage.saveToken(accessToken, refreshToken, userEmail);
                return "✅ 로그인 성공: " + userEmail;
            } else {
                return "⚠️ 로그인 실패: " + result;
            }

        } catch (Exception e) {
            return "❌ SignIn Error: " + e.getMessage();
        }
    }

    /**
     * ✅ users 테이블에 INSERT (username, email)
     */
    private static void insertUserRow(String username, String email) {
        try {
            URL url = new URL(PROJECT_URL + "/rest/v1/users");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("apikey", API_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY); // ✅ anon key로 인증
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String body = String.format(
                "{\"username\":\"%s\", \"email\":\"%s\"}",
                username, email
            );

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            if (code == 201 || code == 200) {
                System.out.println("✅ users 테이블에 username, email 저장 완료: " + email);
            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                System.err.println("⚠️ users insert 실패 (" + code + "): " + br.readLine());
            }

        } catch (Exception e) {
            System.err.println("⚠️ users 테이블 insert 실패: " + e.getMessage());
        }
    }

    /**
     * ✅ Access Token 발급용
     */
    private static String getAccessToken(String email, String password) {
        try {
            URL url = new URL(PROJECT_URL + "/auth/v1/token?grant_type=password");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("apikey", API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String body = String.format("{\"email\":\"%s\", \"password\":\"%s\"}", email, password);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            Scanner sc = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8);
            String result = sc.useDelimiter("\\A").next();
            sc.close();

            JSONObject json = new JSONObject(result);
            return json.optString("access_token", null);

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * ✅ Refresh Token으로 세션 자동 갱신
     */
    public static boolean refreshSession() {
        try {
            String refreshToken = AuthStorage.getRefreshToken();
            if (refreshToken == null) return false;

            URL url = new URL(PROJECT_URL + "/auth/v1/token?grant_type=refresh_token");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("apikey", API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String body = String.format("{\"refresh_token\":\"%s\"}", refreshToken);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            if (conn.getResponseCode() != 200) return false;

            Scanner sc = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8);
            String result = sc.useDelimiter("\\A").next();
            sc.close();

            JSONObject json = new JSONObject(result);
            if (json.has("access_token")) {
                AuthStorage.saveToken(
                    json.getString("access_token"),
                    json.optString("refresh_token", refreshToken),
                    AuthStorage.getEmail()
                );
                System.out.println("🔄 세션 자동 갱신 완료");
                return true;
            }

        } catch (Exception e) {
            System.err.println("❌ refreshSession Error: " + e.getMessage());
        }
        return false;
    }
}
