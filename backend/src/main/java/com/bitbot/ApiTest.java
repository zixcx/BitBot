package com.bitbot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * REST API 테스트 프로그램
 */
public class ApiTest {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiTest.class);
    private static final String BASE_URL = "http://localhost:8080";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static String sessionToken = null;
    
    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("REST API 테스트 시작");
        System.out.println("=".repeat(80));
        System.out.flush();
        
        try {
            // 서버가 실행 중인지 확인
            System.out.print("\n[확인] 서버 연결 확인 중... ");
            System.out.flush();
            if (!checkServerHealth()) {
                System.out.println("❌ 실패");
                System.err.println("\n❌ 서버가 실행 중이 아닙니다!");
                System.err.println("   먼저 별도 터미널에서 'run-server.bat'을 실행하세요.");
                System.exit(1);
            }
            System.out.println("✅ 성공");
            System.out.flush();
            
            // 1. Health Check
            testHealthCheck();
            
            // 2. 회원가입
            testRegister();
            
            // 3. 로그인
            testLogin();
            
            // 4. 계좌 정보 조회
            testGetAccount();
            
            // 5. 프로필 조회
            testGetProfile();
            
            // 6. 거래 내역 조회
            testGetTrades();
            
            // 7. 거래 로그 조회
            testGetTradeLogs();
            
            // 8. 시장 데이터 조회
            testGetMarketData();
            
            // 9. 통계 조회
            testGetStatistics();
            
            // 10. 알림 조회
            testGetNotifications();
            
            System.out.println("\n" + "=".repeat(80));
            System.out.println("✅ 모든 API 테스트 완료!");
            System.out.println("=".repeat(80));
            
        } catch (Exception e) {
            System.err.println("\n❌ 테스트 실패: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static boolean checkServerHealth() {
        try {
            URL url = new URL(BASE_URL + "/api/health");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            int responseCode = conn.getResponseCode();
            conn.disconnect();
            return responseCode == 200;
        } catch (Exception e) {
            return false;
        }
    }
    
    private static void testHealthCheck() {
        System.out.print("\n[1/10] Health Check 테스트... ");
        System.out.flush();
        try {
            String response = sendGetRequest("/api/health");
            System.out.println("✅ 성공");
            System.out.println("   응답: " + response.substring(0, Math.min(100, response.length())));
            System.out.flush();
        } catch (Exception e) {
            System.out.println("❌ 실패");
            System.err.println("   오류: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    private static void testRegister() {
        System.out.print("\n[2/10] 회원가입 테스트... ");
        System.out.flush();
        try {
            String requestBody = """
                {
                    "email": "test@example.com",
                    "username": "testuser",
                    "password": "test1234"
                }
                """;
            String response = sendPostRequest("/api/auth/register", requestBody);
            System.out.println("✅ 성공");
            System.out.println("   응답: " + response.substring(0, Math.min(100, response.length())));
            System.out.flush();
        } catch (Exception e) {
            System.out.println("⚠️ 실패 (이미 존재할 수 있음)");
            System.out.println("   메시지: " + e.getMessage());
            System.out.flush();
        }
    }
    
    private static void testLogin() {
        System.out.print("\n[3/10] 로그인 테스트... ");
        System.out.flush();
        try {
            String requestBody = """
                {
                    "email": "test@example.com",
                    "password": "test1234"
                }
                """;
            String response = sendPostRequest("/api/auth/login", requestBody);
            JsonNode json = objectMapper.readTree(response);
            if (json.has("data") && json.get("data").has("token")) {
                sessionToken = json.get("data").get("token").asText();
                System.out.println("✅ 성공 (토큰 발급됨)");
                System.out.flush();
            } else {
                System.out.println("⚠️ 토큰 없음");
                System.out.println("   응답: " + response.substring(0, Math.min(100, response.length())));
                System.out.flush();
            }
        } catch (Exception e) {
            System.out.println("❌ 실패");
            System.err.println("   오류: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    private static void testGetAccount() {
        System.out.print("\n[4/10] 계좌 정보 조회 테스트... ");
        System.out.flush();
        try {
            String response = sendGetRequest("/api/account");
            int count = countArrayItems(response);
            System.out.println("✅ 성공");
            System.out.println("   응답 길이: " + response.length() + " bytes");
            System.out.flush();
        } catch (Exception e) {
            System.out.println("❌ 실패");
            System.out.println("   오류: " + e.getMessage());
            System.out.flush();
        }
    }
    
    private static void testGetProfile() {
        System.out.print("\n[5/10] 프로필 조회 테스트... ");
        System.out.flush();
        try {
            String response = sendGetRequest("/api/profile");
            System.out.println("✅ 성공");
            System.out.println("   응답: " + response.substring(0, Math.min(150, response.length())));
            System.out.flush();
        } catch (Exception e) {
            System.out.println("⚠️ 실패 (프로필이 없을 수 있음)");
            System.out.println("   메시지: " + e.getMessage());
            System.out.flush();
        }
    }
    
    private static void testGetTrades() {
        System.out.print("\n[6/10] 거래 내역 조회 테스트... ");
        System.out.flush();
        try {
            String response = sendGetRequest("/api/trades?limit=10");
            int count = countArrayItems(response);
            System.out.println("✅ 성공 (" + count + "개)");
            System.out.flush();
        } catch (Exception e) {
            System.out.println("⚠️ 실패");
            System.out.println("   메시지: " + e.getMessage());
            System.out.flush();
        }
    }
    
    private static void testGetTradeLogs() {
        System.out.print("\n[7/10] 거래 로그 조회 테스트... ");
        System.out.flush();
        try {
            String response = sendGetRequest("/api/trade-logs?limit=10");
            int count = countArrayItems(response);
            System.out.println("✅ 성공 (" + count + "개)");
            System.out.flush();
        } catch (Exception e) {
            System.out.println("⚠️ 실패");
            System.out.println("   메시지: " + e.getMessage());
            System.out.flush();
        }
    }
    
    private static void testGetMarketData() {
        System.out.print("\n[8/10] 시장 데이터 조회 테스트... ");
        System.out.flush();
        try {
            // 현재 가격
            System.out.print("\n   - 현재 가격... ");
            System.out.flush();
            String priceResponse = sendGetRequest("/api/market/price?symbol=BTCUSDT");
            System.out.println("✅");
            
            // 24시간 통계
            System.out.print("   - 24시간 통계... ");
            System.out.flush();
            String statsResponse = sendGetRequest("/api/market/24hr?symbol=BTCUSDT");
            System.out.println("✅");
            
            // 차트 데이터
            System.out.print("   - 차트 데이터... ");
            System.out.flush();
            String chartResponse = sendGetRequest("/api/market/chart?symbol=BTCUSDT&interval=1h&limit=10");
            int count = countArrayItems(chartResponse);
            System.out.println("✅ (" + count + "개)");
            System.out.flush();
            
        } catch (Exception e) {
            System.out.println("❌ 실패");
            System.out.println("   오류: " + e.getMessage());
            System.out.flush();
        }
    }
    
    private static void testGetStatistics() {
        System.out.print("\n[9/10] 통계 조회 테스트... ");
        System.out.flush();
        try {
            String response = sendGetRequest("/api/statistics/trades");
            System.out.println("✅ 성공");
            System.out.println("   응답: " + response.substring(0, Math.min(150, response.length())));
            System.out.flush();
        } catch (Exception e) {
            System.out.println("⚠️ 실패");
            System.out.println("   메시지: " + e.getMessage());
            System.out.flush();
        }
    }
    
    private static void testGetNotifications() {
        System.out.print("\n[10/10] 알림 조회 테스트... ");
        System.out.flush();
        try {
            String response = sendGetRequest("/api/notifications?limit=10");
            int count = countArrayItems(response);
            System.out.println("✅ 성공 (" + count + "개)");
            System.out.flush();
        } catch (Exception e) {
            System.out.println("⚠️ 실패");
            System.out.println("   메시지: " + e.getMessage());
            System.out.flush();
        }
    }
    
    private static String sendGetRequest(String endpoint) throws Exception {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        
        if (sessionToken != null) {
            conn.setRequestProperty("Authorization", "Bearer " + sessionToken);
        }
        
        int responseCode = conn.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(
            responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream(),
            StandardCharsets.UTF_8
        ));
        
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();
        conn.disconnect();
        
        if (responseCode >= 200 && responseCode < 300) {
            return response.toString();
        } else {
            throw new Exception("HTTP " + responseCode + ": " + response.toString());
        }
    }
    
    private static String sendPostRequest(String endpoint, String requestBody) throws Exception {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        
        if (sessionToken != null) {
            conn.setRequestProperty("Authorization", "Bearer " + sessionToken);
        }
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        int responseCode = conn.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(
            responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream(),
            StandardCharsets.UTF_8
        ));
        
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();
        conn.disconnect();
        
        if (responseCode >= 200 && responseCode < 300) {
            return response.toString();
        } else {
            throw new Exception("HTTP " + responseCode + ": " + response.toString());
        }
    }
    
    private static int countArrayItems(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            if (node.has("data") && node.get("data").isArray()) {
                return node.get("data").size();
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }
}

