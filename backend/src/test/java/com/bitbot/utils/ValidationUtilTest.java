package com.bitbot.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ValidationUtil 단위 테스트
 */
class ValidationUtilTest {
    
    @Test
    @DisplayName("설문조사 답변 검증 - 정상 케이스")
    void testValidateQuestionnaireAnswer_Valid() {
        // 정상 범위 내 값
        assertDoesNotThrow(() -> ValidationUtil.validateQuestionnaireAnswer(1, 1, 4));
        assertDoesNotThrow(() -> ValidationUtil.validateQuestionnaireAnswer(2, 1, 4));
        assertDoesNotThrow(() -> ValidationUtil.validateQuestionnaireAnswer(4, 1, 4));
    }
    
    @Test
    @DisplayName("설문조사 답변 검증 - 최소값 미만")
    void testValidateQuestionnaireAnswer_BelowMin() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ValidationUtil.validateQuestionnaireAnswer(0, 1, 4)
        );
        assertTrue(exception.getMessage().contains("답변은 1-4 사이여야 합니다"));
    }
    
    @Test
    @DisplayName("설문조사 답변 검증 - 최대값 초과")
    void testValidateQuestionnaireAnswer_AboveMax() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ValidationUtil.validateQuestionnaireAnswer(5, 1, 4)
        );
        assertTrue(exception.getMessage().contains("답변은 1-4 사이여야 합니다"));
    }
    
    @Test
    @DisplayName("주문 수량 검증 - 정상 케이스")
    void testValidateOrderQuantity_Valid() {
        assertDoesNotThrow(() -> ValidationUtil.validateOrderQuantity(0.001));
        assertDoesNotThrow(() -> ValidationUtil.validateOrderQuantity(1.0));
        assertDoesNotThrow(() -> ValidationUtil.validateOrderQuantity(1000.0));
    }
    
    @Test
    @DisplayName("주문 수량 검증 - 0 이하")
    void testValidateOrderQuantity_ZeroOrNegative() {
        IllegalArgumentException exception1 = assertThrows(
            IllegalArgumentException.class,
            () -> ValidationUtil.validateOrderQuantity(0)
        );
        assertTrue(exception1.getMessage().contains("주문 수량은 0보다 커야 합니다"));
        
        IllegalArgumentException exception2 = assertThrows(
            IllegalArgumentException.class,
            () -> ValidationUtil.validateOrderQuantity(-1)
        );
        assertTrue(exception2.getMessage().contains("주문 수량은 0보다 커야 합니다"));
    }
    
    @Test
    @DisplayName("주문 수량 검증 - 최대값 초과")
    void testValidateOrderQuantity_AboveMax() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ValidationUtil.validateOrderQuantity(1001)
        );
        assertTrue(exception.getMessage().contains("주문 수량이 너무 큽니다"));
    }
    
    @Test
    @DisplayName("가격 검증 - 정상 케이스")
    void testValidatePrice_Valid() {
        assertDoesNotThrow(() -> ValidationUtil.validatePrice(0.01));
        assertDoesNotThrow(() -> ValidationUtil.validatePrice(50000.0));
        assertDoesNotThrow(() -> ValidationUtil.validatePrice(1000000.0));
    }
    
    @Test
    @DisplayName("가격 검증 - 0 이하")
    void testValidatePrice_ZeroOrNegative() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ValidationUtil.validatePrice(0)
        );
        assertTrue(exception.getMessage().contains("가격은 0보다 커야 합니다"));
    }
    
    @Test
    @DisplayName("가격 검증 - 비정상적으로 높은 가격")
    void testValidatePrice_TooHigh() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ValidationUtil.validatePrice(1000001)
        );
        assertTrue(exception.getMessage().contains("가격이 비정상적으로 높습니다"));
    }
    
    @Test
    @DisplayName("레버리지 검증 - 정상 케이스")
    void testValidateLeverage_Valid() {
        assertDoesNotThrow(() -> ValidationUtil.validateLeverage(1, 10));
        assertDoesNotThrow(() -> ValidationUtil.validateLeverage(5, 10));
        assertDoesNotThrow(() -> ValidationUtil.validateLeverage(10, 10));
    }
    
    @Test
    @DisplayName("레버리지 검증 - 1 미만")
    void testValidateLeverage_BelowOne() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ValidationUtil.validateLeverage(0, 10)
        );
        assertTrue(exception.getMessage().contains("레버리지는 1 이상이어야 합니다"));
    }
    
    @Test
    @DisplayName("레버리지 검증 - 최대값 초과")
    void testValidateLeverage_AboveMax() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ValidationUtil.validateLeverage(11, 10)
        );
        assertTrue(exception.getMessage().contains("레버리지가 최대 허용값을 초과합니다"));
    }
    
    @Test
    @DisplayName("레버리지 검증 - 125 초과")
    void testValidateLeverage_Above125() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ValidationUtil.validateLeverage(126, 200)
        );
        assertTrue(exception.getMessage().contains("레버리지는 125를 초과할 수 없습니다"));
    }
    
    @Test
    @DisplayName("비율 검증 - 정상 케이스")
    void testValidatePercent_Valid() {
        assertDoesNotThrow(() -> ValidationUtil.validatePercent(5.0, 0.0, 100.0, "투자 비율"));
        assertDoesNotThrow(() -> ValidationUtil.validatePercent(50.0, 0.0, 100.0, "투자 비율"));
        assertDoesNotThrow(() -> ValidationUtil.validatePercent(100.0, 0.0, 100.0, "투자 비율"));
    }
    
    @Test
    @DisplayName("비율 검증 - 범위 초과")
    void testValidatePercent_OutOfRange() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ValidationUtil.validatePercent(101.0, 0.0, 100.0, "투자 비율")
        );
        assertTrue(exception.getMessage().contains("투자 비율"));
    }
    
    @Test
    @DisplayName("사용자 ID 검증 - 정상 케이스")
    void testValidateUserId_Valid() {
        assertDoesNotThrow(() -> ValidationUtil.validateUserId(1));
        assertDoesNotThrow(() -> ValidationUtil.validateUserId(100));
    }
    
    @Test
    @DisplayName("사용자 ID 검증 - null")
    void testValidateUserId_Null() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ValidationUtil.validateUserId(null)
        );
        assertTrue(exception.getMessage().contains("사용자 ID는 null일 수 없습니다"));
    }
    
    @Test
    @DisplayName("사용자 ID 검증 - 0 이하")
    void testValidateUserId_ZeroOrNegative() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ValidationUtil.validateUserId(0)
        );
        assertTrue(exception.getMessage().contains("사용자 ID는 0보다 커야 합니다"));
    }
    
    @Test
    @DisplayName("문자열 검증 - 정상 케이스")
    void testValidateString_Valid() {
        assertDoesNotThrow(() -> ValidationUtil.validateString("test", "필드명"));
        assertDoesNotThrow(() -> ValidationUtil.validateString("  test  ", "필드명"));
    }
    
    @Test
    @DisplayName("문자열 검증 - null")
    void testValidateString_Null() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ValidationUtil.validateString(null, "필드명")
        );
        assertTrue(exception.getMessage().contains("필드명"));
    }
    
    @Test
    @DisplayName("문자열 검증 - 빈 문자열")
    void testValidateString_Empty() {
        IllegalArgumentException exception1 = assertThrows(
            IllegalArgumentException.class,
            () -> ValidationUtil.validateString("", "필드명")
        );
        assertTrue(exception1.getMessage().contains("필드명"));
        
        IllegalArgumentException exception2 = assertThrows(
            IllegalArgumentException.class,
            () -> ValidationUtil.validateString("   ", "필드명")
        );
        assertTrue(exception2.getMessage().contains("필드명"));
    }
}

