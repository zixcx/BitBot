package com.bitbot.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 비밀번호 해싱 유틸리티
 * BCrypt를 사용한 비밀번호 암호화
 */
public class PasswordUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordUtil.class);
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12); // 강도 12
    
    /**
     * 비밀번호 해싱
     * @param plainPassword 평문 비밀번호
     * @return 해시된 비밀번호
     */
    public static String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 비어있을 수 없습니다.");
        }
        return encoder.encode(plainPassword);
    }
    
    /**
     * 비밀번호 검증
     * @param plainPassword 평문 비밀번호
     * @param hashedPassword 해시된 비밀번호
     * @return 일치 여부
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return encoder.matches(plainPassword, hashedPassword);
        } catch (Exception e) {
            logger.error("비밀번호 검증 실패", e);
            return false;
        }
    }
    
    /**
     * 비밀번호 강도 검증
     * @param password 비밀번호
     * @return 검증 결과 메시지 (null이면 유효)
     */
    public static String validatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return "비밀번호는 필수입니다.";
        }
        
        if (password.length() < 8) {
            return "비밀번호는 최소 8자 이상이어야 합니다.";
        }
        
        if (password.length() > 128) {
            return "비밀번호는 최대 128자까지 가능합니다.";
        }
        
        // 영문, 숫자, 특수문자 중 2가지 이상 포함
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
        
        int typeCount = 0;
        if (hasLetter) typeCount++;
        if (hasDigit) typeCount++;
        if (hasSpecial) typeCount++;
        
        if (typeCount < 2) {
            return "비밀번호는 영문, 숫자, 특수문자 중 2가지 이상을 포함해야 합니다.";
        }
        
        return null; // 유효한 비밀번호
    }
}

