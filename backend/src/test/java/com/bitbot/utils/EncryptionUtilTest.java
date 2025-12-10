package com.bitbot.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EncryptionUtil 단위 테스트
 */
class EncryptionUtilTest {
    
    @BeforeEach
    void setUp() {
        // ConfigLoader 초기화 (테스트 환경)
        try {
            com.bitbot.utils.ConfigLoader.loadConfig();
        } catch (Exception e) {
            // 환경 변수가 없어도 테스트 진행
        }
    }
    
    @Test
    @DisplayName("암호화/복호화 - 정상 케이스")
    void testEncryptDecrypt_Valid() {
        String plaintext = "TestAPIKey123456789";
        String encrypted = EncryptionUtil.encrypt(plaintext);
        
        assertNotNull(encrypted);
        assertNotEquals(plaintext, encrypted, "암호화된 텍스트는 원본과 달라야 함");
        
        String decrypted = EncryptionUtil.decrypt(encrypted);
        assertEquals(plaintext, decrypted, "복호화된 텍스트는 원본과 같아야 함");
    }
    
    @Test
    @DisplayName("암호화/복호화 - 동일 텍스트는 다른 암호문 생성")
    void testEncrypt_DifferentCiphertexts() {
        String plaintext = "TestAPIKey123456789";
        String encrypted1 = EncryptionUtil.encrypt(plaintext);
        String encrypted2 = EncryptionUtil.encrypt(plaintext);
        
        // GCM 모드는 매번 다른 IV를 사용하므로 암호문이 달라야 함
        assertNotEquals(encrypted1, encrypted2, "동일 텍스트도 다른 암호문을 생성해야 함");
        
        // 하지만 복호화하면 원본과 같아야 함
        assertEquals(plaintext, EncryptionUtil.decrypt(encrypted1));
        assertEquals(plaintext, EncryptionUtil.decrypt(encrypted2));
    }
    
    @Test
    @DisplayName("암호화 - null 또는 빈 문자열")
    void testEncrypt_NullOrEmpty() {
        assertEquals(null, EncryptionUtil.encrypt(null));
        assertEquals("", EncryptionUtil.encrypt(""));
    }
    
    @Test
    @DisplayName("복호화 - null 또는 빈 문자열")
    void testDecrypt_NullOrEmpty() {
        assertEquals(null, EncryptionUtil.decrypt(null));
        assertEquals("", EncryptionUtil.decrypt(""));
    }
    
    @Test
    @DisplayName("복호화 - 잘못된 암호문")
    void testDecrypt_InvalidCiphertext() {
        assertThrows(RuntimeException.class, () -> {
            EncryptionUtil.decrypt("InvalidCiphertext");
        });
    }
    
    @Test
    @DisplayName("복호화 - 너무 짧은 암호문")
    void testDecrypt_TooShort() {
        // Base64로 디코딩하면 너무 짧은 경우
        String shortCipher = "dGVzdA=="; // "test" in Base64
        assertThrows(RuntimeException.class, () -> {
            EncryptionUtil.decrypt(shortCipher);
        });
    }
    
    @Test
    @DisplayName("암호화/복호화 - 긴 텍스트")
    void testEncryptDecrypt_LongText() {
        String longText = "A".repeat(1000) + "B".repeat(1000);
        String encrypted = EncryptionUtil.encrypt(longText);
        String decrypted = EncryptionUtil.decrypt(encrypted);
        
        assertEquals(longText, decrypted, "긴 텍스트도 정상적으로 암호화/복호화되어야 함");
    }
    
    @Test
    @DisplayName("암호화/복호화 - 특수 문자 포함")
    void testEncryptDecrypt_SpecialCharacters() {
        String specialText = "!@#$%^&*()_+-=[]{}|;':\",./<>?`~";
        String encrypted = EncryptionUtil.encrypt(specialText);
        String decrypted = EncryptionUtil.decrypt(encrypted);
        
        assertEquals(specialText, decrypted, "특수 문자도 정상적으로 암호화/복호화되어야 함");
    }
    
    @Test
    @DisplayName("암호화/복호화 - 한글 포함")
    void testEncryptDecrypt_Korean() {
        String koreanText = "한글테스트비밀번호123!@#";
        String encrypted = EncryptionUtil.encrypt(koreanText);
        String decrypted = EncryptionUtil.decrypt(encrypted);
        
        assertEquals(koreanText, decrypted, "한글도 정상적으로 암호화/복호화되어야 함");
    }
    
    @Test
    @DisplayName("키 생성 테스트")
    void testGenerateKey() {
        String key = EncryptionUtil.generateKey();
        
        assertNotNull(key);
        assertTrue(key.length() > 0, "생성된 키는 비어있지 않아야 함");
        
        // Base64로 인코딩된 256비트 키는 44자 (32바이트 * 4/3)
        assertTrue(key.length() >= 40, "생성된 키는 충분히 길어야 함");
    }
}

