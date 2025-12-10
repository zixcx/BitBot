package com.bitbot.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM 암호화 유틸리티
 * API 키 등 민감한 정보 암호화에 사용
 */
public class EncryptionUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(EncryptionUtil.class);
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256; // AES-256
    private static final int GCM_IV_LENGTH = 12; // GCM IV는 12바이트 권장
    private static final int GCM_TAG_LENGTH = 128; // GCM 태그는 128비트
    
    // 암호화 키 (실제 운영 환경에서는 환경 변수나 키 관리 시스템에서 가져와야 함)
    private static final String ENCRYPTION_KEY = getEncryptionKey();
    
    /**
     * 암호화 키 가져오기 (환경 변수에서 가져오거나 기본값 사용)
     */
    private static String getEncryptionKey() {
        String key = ConfigLoader.get("ENCRYPTION_KEY", "");
        if (key.isEmpty()) {
            // 기본 키 생성 (실제 운영 환경에서는 반드시 환경 변수로 설정)
            logger.warn("ENCRYPTION_KEY가 설정되지 않았습니다. 기본 키를 사용합니다. (운영 환경에서는 반드시 설정하세요!)");
            return "BitBotDefaultEncryptionKey256Bit"; // 정확히 32바이트 (256비트)
        }
        
        // 키를 바이트 배열로 변환하여 정확히 32바이트로 조정
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            logger.warn("ENCRYPTION_KEY가 너무 짧습니다. 최소 32바이트 이상이어야 합니다.");
            // 32바이트로 패딩
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
            for (int i = keyBytes.length; i < 32; i++) {
                padded[i] = 0;
            }
            return new String(padded, StandardCharsets.UTF_8);
        } else if (keyBytes.length > 32) {
            // 32바이트로 자르기
            byte[] trimmed = new byte[32];
            System.arraycopy(keyBytes, 0, trimmed, 0, 32);
            return new String(trimmed, StandardCharsets.UTF_8);
        }
        
        return key; // 정확히 32바이트
    }
    
    /**
     * 평문을 암호화
     * @param plaintext 평문
     * @return Base64로 인코딩된 암호문 (IV + 암호문)
     */
    public static String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }
        
        try {
            // SecretKey 생성 (정확히 32바이트)
            byte[] keyBytes = ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8);
            if (keyBytes.length != 32) {
                // 32바이트로 조정
                byte[] adjustedKey = new byte[32];
                System.arraycopy(keyBytes, 0, adjustedKey, 0, Math.min(keyBytes.length, 32));
                keyBytes = adjustedKey;
            }
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            
            // IV 생성 (랜덤)
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            
            // Cipher 초기화
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            
            // 암호화
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            
            // IV + 암호문을 결합하여 Base64 인코딩
            byte[] encrypted = new byte[GCM_IV_LENGTH + ciphertext.length];
            System.arraycopy(iv, 0, encrypted, 0, GCM_IV_LENGTH);
            System.arraycopy(ciphertext, 0, encrypted, GCM_IV_LENGTH, ciphertext.length);
            
            return Base64.getEncoder().encodeToString(encrypted);
            
        } catch (Exception e) {
            logger.error("암호화 실패", e);
            throw new RuntimeException("암호화 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 암호문을 복호화
     * @param ciphertext Base64로 인코딩된 암호문 (IV + 암호문)
     * @return 평문
     */
    public static String decrypt(String ciphertext) {
        if (ciphertext == null || ciphertext.isEmpty()) {
            return ciphertext;
        }
        
        try {
            // Base64 디코딩
            byte[] encrypted = Base64.getDecoder().decode(ciphertext);
            
            // IV와 암호문 분리
            if (encrypted.length < GCM_IV_LENGTH) {
                throw new IllegalArgumentException("암호문이 너무 짧습니다.");
            }
            
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(encrypted, 0, iv, 0, GCM_IV_LENGTH);
            
            byte[] ciphertextBytes = new byte[encrypted.length - GCM_IV_LENGTH];
            System.arraycopy(encrypted, GCM_IV_LENGTH, ciphertextBytes, 0, ciphertextBytes.length);
            
            // SecretKey 생성 (정확히 32바이트)
            byte[] keyBytes = ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8);
            if (keyBytes.length != 32) {
                // 32바이트로 조정
                byte[] adjustedKey = new byte[32];
                System.arraycopy(keyBytes, 0, adjustedKey, 0, Math.min(keyBytes.length, 32));
                keyBytes = adjustedKey;
            }
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            
            // Cipher 초기화
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            
            // 복호화
            byte[] plaintext = cipher.doFinal(ciphertextBytes);
            
            return new String(plaintext, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            logger.error("복호화 실패", e);
            throw new RuntimeException("복호화 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 암호화 키 생성 (테스트용)
     * @return Base64로 인코딩된 256비트 키
     */
    public static String generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(KEY_SIZE);
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            logger.error("키 생성 실패", e);
            throw new RuntimeException("키 생성 실패: " + e.getMessage(), e);
        }
    }
}

