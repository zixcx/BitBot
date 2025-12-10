package com.bitbot.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PasswordUtil 단위 테스트
 */
class PasswordUtilTest {
    
    @Test
    @DisplayName("비밀번호 해싱 - 정상 케이스")
    void testHash_Valid() {
        String password = "TestPassword123!";
        String hashed = PasswordUtil.hash(password);
        
        assertNotNull(hashed);
        assertNotEquals(password, hashed, "해시된 비밀번호는 원본과 달라야 함");
        assertTrue(hashed.length() > 20, "BCrypt 해시는 충분히 길어야 함");
    }
    
    @Test
    @DisplayName("비밀번호 해싱 - 동일 비밀번호는 다른 해시 생성")
    void testHash_DifferentHashes() {
        String password = "TestPassword123!";
        String hashed1 = PasswordUtil.hash(password);
        String hashed2 = PasswordUtil.hash(password);
        
        // BCrypt는 매번 다른 salt를 사용하므로 해시가 달라야 함
        assertNotEquals(hashed1, hashed2, "동일 비밀번호도 다른 해시를 생성해야 함");
    }
    
    @Test
    @DisplayName("비밀번호 해싱 - null 또는 빈 문자열")
    void testHash_Invalid() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hash(null));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hash(""));
    }
    
    @Test
    @DisplayName("비밀번호 검증 - 정상 케이스")
    void testVerify_Valid() {
        String password = "TestPassword123!";
        String hashed = PasswordUtil.hash(password);
        
        assertTrue(PasswordUtil.verify(password, hashed), "올바른 비밀번호는 검증 성공해야 함");
    }
    
    @Test
    @DisplayName("비밀번호 검증 - 잘못된 비밀번호")
    void testVerify_InvalidPassword() {
        String password = "TestPassword123!";
        String hashed = PasswordUtil.hash(password);
        
        assertFalse(PasswordUtil.verify("WrongPassword", hashed), "잘못된 비밀번호는 검증 실패해야 함");
    }
    
    @Test
    @DisplayName("비밀번호 검증 - null 입력")
    void testVerify_Null() {
        String hashed = PasswordUtil.hash("TestPassword123!");
        
        assertFalse(PasswordUtil.verify(null, hashed), "null 비밀번호는 검증 실패해야 함");
        assertFalse(PasswordUtil.verify("TestPassword123!", null), "null 해시는 검증 실패해야 함");
        assertFalse(PasswordUtil.verify(null, null), "둘 다 null이면 검증 실패해야 함");
    }
    
    @Test
    @DisplayName("비밀번호 강도 검증 - 정상 케이스")
    void testValidatePasswordStrength_Valid() {
        // 영문 + 숫자
        assertNull(PasswordUtil.validatePasswordStrength("Password123"));
        
        // 영문 + 특수문자
        assertNull(PasswordUtil.validatePasswordStrength("Password!@#"));
        
        // 숫자 + 특수문자
        assertNull(PasswordUtil.validatePasswordStrength("12345678!@#"));
        
        // 영문 + 숫자 + 특수문자
        assertNull(PasswordUtil.validatePasswordStrength("Password123!@#"));
    }
    
    @Test
    @DisplayName("비밀번호 강도 검증 - 너무 짧음")
    void testValidatePasswordStrength_TooShort() {
        String result = PasswordUtil.validatePasswordStrength("Pass1");
        assertNotNull(result);
        assertTrue(result.contains("8자 이상"));
    }
    
    @Test
    @DisplayName("비밀번호 강도 검증 - 너무 김")
    void testValidatePasswordStrength_TooLong() {
        String longPassword = "A".repeat(129) + "1";
        String result = PasswordUtil.validatePasswordStrength(longPassword);
        assertNotNull(result);
        assertTrue(result.contains("128자"));
    }
    
    @Test
    @DisplayName("비밀번호 강도 검증 - 영문만")
    void testValidatePasswordStrength_LettersOnly() {
        String result = PasswordUtil.validatePasswordStrength("PasswordOnly");
        assertNotNull(result);
        assertTrue(result.contains("2가지 이상"));
    }
    
    @Test
    @DisplayName("비밀번호 강도 검증 - 숫자만")
    void testValidatePasswordStrength_NumbersOnly() {
        String result = PasswordUtil.validatePasswordStrength("12345678");
        assertNotNull(result);
        assertTrue(result.contains("2가지 이상"));
    }
    
    @Test
    @DisplayName("비밀번호 강도 검증 - null 또는 빈 문자열")
    void testValidatePasswordStrength_NullOrEmpty() {
        String result1 = PasswordUtil.validatePasswordStrength(null);
        assertNotNull(result1);
        assertTrue(result1.contains("필수"));
        
        String result2 = PasswordUtil.validatePasswordStrength("");
        assertNotNull(result2);
        assertTrue(result2.contains("필수"));
    }
}

