package com.bitbot.server.service;

import com.bitbot.database.UserRepository;
import com.bitbot.models.User;
import com.bitbot.utils.EncryptionUtil;
import com.bitbot.utils.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 인증 서비스
 * 로그인, 회원가입, API 키 암호화/복호화 처리
 */
@Service
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    
    // 세션 관리 (실제 운영 환경에서는 Redis 등 사용 권장)
    private final Map<String, Integer> sessions = new HashMap<>();
    
    public AuthService() {
        this.userRepository = new UserRepository();
    }
    
    /**
     * 회원가입
     * @param email 이메일
     * @param username 사용자명
     * @param password 평문 비밀번호
     * @param binanceApiKey Binance API 키 (선택사항)
     * @param binanceSecretKey Binance Secret 키 (선택사항)
     * @return 생성된 사용자 ID
     */
    public Integer register(String email, String username, String password, 
                           String binanceApiKey, String binanceSecretKey) {
        // 비밀번호 강도 검증
        String validationError = PasswordUtil.validatePasswordStrength(password);
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }
        
        // 비밀번호 해싱
        String hashedPassword = PasswordUtil.hash(password);
        
        // API 키 암호화
        String encryptedApiKey = binanceApiKey != null && !binanceApiKey.isEmpty() 
                ? EncryptionUtil.encrypt(binanceApiKey) 
                : null;
        String encryptedSecretKey = binanceSecretKey != null && !binanceSecretKey.isEmpty() 
                ? EncryptionUtil.encrypt(binanceSecretKey) 
                : null;
        
        // 사용자 생성
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPasswordHash(hashedPassword);
        user.setBinanceApiKeyEncrypted(encryptedApiKey);
        user.setBinanceSecretKeyEncrypted(encryptedSecretKey);
        user.setTradingEnabled(false);
        user.setRiskManagementEnabled(true);
        user.setMaxInvestmentPercent(10.0);
        
        Integer userId = userRepository.save(user);
        logger.info("회원가입 완료: userId={}, email={}", userId, email);
        
        return userId;
    }
    
    /**
     * 로그인
     * @param email 이메일
     * @param password 평문 비밀번호
     * @return 세션 토큰
     */
    public String login(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        
        // 비밀번호 검증
        if (!PasswordUtil.verify(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        
        // 세션 토큰 생성
        String sessionToken = UUID.randomUUID().toString();
        sessions.put(sessionToken, user.getId());
        
        logger.info("로그인 성공: userId={}, email={}", user.getId(), email);
        return sessionToken;
    }
    
    /**
     * 로그아웃
     * @param sessionToken 세션 토큰
     */
    public void logout(String sessionToken) {
        sessions.remove(sessionToken);
        logger.info("로그아웃: sessionToken={}", sessionToken);
    }
    
    /**
     * 세션 토큰으로 사용자 ID 조회
     * @param sessionToken 세션 토큰
     * @return 사용자 ID (없으면 null)
     */
    public Integer getUserIdFromSession(String sessionToken) {
        return sessions.get(sessionToken);
    }
    
    /**
     * 세션 유효성 검증
     * @param sessionToken 세션 토큰
     * @return 유효 여부
     */
    public boolean isValidSession(String sessionToken) {
        return sessionToken != null && sessions.containsKey(sessionToken);
    }
    
    /**
     * API 키 복호화
     * @param userId 사용자 ID
     * @return 복호화된 API 키 맵 (apiKey, secretKey)
     */
    public Map<String, String> getDecryptedApiKeys(Integer userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        
        Map<String, String> keys = new HashMap<>();
        
        if (user.getBinanceApiKeyEncrypted() != null) {
            try {
                keys.put("apiKey", EncryptionUtil.decrypt(user.getBinanceApiKeyEncrypted()));
            } catch (Exception e) {
                logger.error("API 키 복호화 실패", e);
            }
        }
        
        if (user.getBinanceSecretKeyEncrypted() != null) {
            try {
                keys.put("secretKey", EncryptionUtil.decrypt(user.getBinanceSecretKeyEncrypted()));
            } catch (Exception e) {
                logger.error("Secret 키 복호화 실패", e);
            }
        }
        
        return keys;
    }
}

