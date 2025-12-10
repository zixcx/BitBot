package com.bitbot.server.controller;

import com.bitbot.server.dto.ApiResponse;
import com.bitbot.server.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 인증 관련 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;
    
    public AuthController() {
        this.authService = new AuthService();
    }
    
    /**
     * 회원가입
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(
            @RequestBody RegisterRequest request) {
        try {
            Integer userId = authService.register(
                request.getEmail(),
                request.getUsername(),
                request.getPassword(),
                request.getBinanceApiKey(),
                request.getBinanceSecretKey()
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("email", request.getEmail());
            result.put("username", request.getUsername());
            
            return ResponseEntity.ok(ApiResponse.success("회원가입 성공", result));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error("회원가입 실패: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("회원가입 실패", e);
            return ResponseEntity.ok(ApiResponse.error("회원가입 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 로그인
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(
            @RequestBody LoginRequest request) {
        try {
            String sessionToken = authService.login(request.getEmail(), request.getPassword());
            
            Map<String, Object> result = new HashMap<>();
            result.put("sessionToken", sessionToken);
            result.put("email", request.getEmail());
            
            return ResponseEntity.ok(ApiResponse.success("로그인 성공", result));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error("로그인 실패: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("로그인 실패", e);
            return ResponseEntity.ok(ApiResponse.error("로그인 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 로그아웃
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            String sessionToken = extractSessionToken(authHeader);
            if (sessionToken != null) {
                authService.logout(sessionToken);
            }
            return ResponseEntity.ok(ApiResponse.success("로그아웃 성공", null));
        } catch (Exception e) {
            logger.error("로그아웃 실패", e);
            return ResponseEntity.ok(ApiResponse.error("로그아웃 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 세션 검증
     * GET /api/auth/verify
     */
    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verify(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            String sessionToken = extractSessionToken(authHeader);
            boolean isValid = authService.isValidSession(sessionToken);
            
            Map<String, Object> result = new HashMap<>();
            result.put("valid", isValid);
            if (isValid) {
                Integer userId = authService.getUserIdFromSession(sessionToken);
                result.put("userId", userId);
            }
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            logger.error("세션 검증 실패", e);
            return ResponseEntity.ok(ApiResponse.error("세션 검증 실패: " + e.getMessage()));
        }
    }
    
    /**
     * Authorization 헤더에서 세션 토큰 추출
     */
    private String extractSessionToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }
    
    /**
     * 회원가입 요청 DTO
     */
    public static class RegisterRequest {
        private String email;
        private String username;
        private String password;
        private String binanceApiKey;
        private String binanceSecretKey;
        
        // Getters and Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getBinanceApiKey() { return binanceApiKey; }
        public void setBinanceApiKey(String binanceApiKey) { this.binanceApiKey = binanceApiKey; }
        
        public String getBinanceSecretKey() { return binanceSecretKey; }
        public void setBinanceSecretKey(String binanceSecretKey) { this.binanceSecretKey = binanceSecretKey; }
    }
    
    /**
     * 로그인 요청 DTO
     */
    public static class LoginRequest {
        private String email;
        private String password;
        
        // Getters and Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}

