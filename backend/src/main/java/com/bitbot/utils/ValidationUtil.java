package com.bitbot.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 입력값 검증 유틸리티
 */
public class ValidationUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidationUtil.class);
    
    /**
     * 설문조사 답변 검증
     * @param answer 답변 값
     * @param min 최소값
     * @param max 최대값
     * @throws IllegalArgumentException 검증 실패 시
     */
    public static void validateQuestionnaireAnswer(int answer, int min, int max) {
        if (answer < min || answer > max) {
            String message = String.format("답변은 %d-%d 사이여야 합니다 (입력값: %d)", min, max, answer);
            logger.error("설문조사 답변 검증 실패: {}", message);
            throw new IllegalArgumentException(message);
        }
    }
    
    /**
     * 주문 수량 검증
     * @param quantity 주문 수량 (BTC)
     * @throws IllegalArgumentException 검증 실패 시
     */
    public static void validateOrderQuantity(double quantity) {
        if (quantity <= 0) {
            String message = "주문 수량은 0보다 커야 합니다 (입력값: " + quantity + ")";
            logger.error("주문 수량 검증 실패: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (quantity > 1000) {
            String message = "주문 수량이 너무 큽니다 (최대 1000 BTC, 입력값: " + quantity + ")";
            logger.error("주문 수량 검증 실패: {}", message);
            throw new IllegalArgumentException(message);
        }
    }
    
    /**
     * 가격 검증
     * @param price 가격
     * @throws IllegalArgumentException 검증 실패 시
     */
    public static void validatePrice(double price) {
        if (price <= 0) {
            String message = "가격은 0보다 커야 합니다 (입력값: " + price + ")";
            logger.error("가격 검증 실패: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (price > 1000000) {
            String message = "가격이 비정상적으로 높습니다 (입력값: " + price + ")";
            logger.error("가격 검증 실패: {}", message);
            throw new IllegalArgumentException(message);
        }
    }
    
    /**
     * 레버리지 검증
     * @param leverage 레버리지 배수
     * @param maxLeverage 최대 허용 레버리지
     * @throws IllegalArgumentException 검증 실패 시
     */
    public static void validateLeverage(int leverage, int maxLeverage) {
        if (leverage < 1) {
            String message = "레버리지는 1 이상이어야 합니다 (입력값: " + leverage + ")";
            logger.error("레버리지 검증 실패: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (leverage > maxLeverage) {
            String message = String.format("레버리지가 최대 허용값을 초과합니다 (최대: %d, 입력값: %d)", 
                    maxLeverage, leverage);
            logger.error("레버리지 검증 실패: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (leverage > 125) {
            String message = "레버리지는 125를 초과할 수 없습니다 (입력값: " + leverage + ")";
            logger.error("레버리지 검증 실패: {}", message);
            throw new IllegalArgumentException(message);
        }
    }
    
    /**
     * 비율 검증 (퍼센트)
     * @param percent 비율 (%)
     * @param min 최소값
     * @param max 최대값
     * @param fieldName 필드명 (에러 메시지용)
     * @throws IllegalArgumentException 검증 실패 시
     */
    public static void validatePercent(double percent, double min, double max, String fieldName) {
        if (percent < min || percent > max) {
            String message = String.format("%s는 %.1f%%-%.1f%% 사이여야 합니다 (입력값: %.2f%%)", 
                    fieldName, min, max, percent);
            logger.error("비율 검증 실패: {}", message);
            throw new IllegalArgumentException(message);
        }
    }
    
    /**
     * 사용자 ID 검증
     * @param userId 사용자 ID
     * @throws IllegalArgumentException 검증 실패 시
     */
    public static void validateUserId(Integer userId) {
        if (userId == null) {
            String message = "사용자 ID는 null일 수 없습니다";
            logger.error("사용자 ID 검증 실패: {}", message);
            throw new IllegalArgumentException(message);
        }
        if (userId <= 0) {
            String message = "사용자 ID는 0보다 커야 합니다 (입력값: " + userId + ")";
            logger.error("사용자 ID 검증 실패: {}", message);
            throw new IllegalArgumentException(message);
        }
    }
    
    /**
     * 문자열 검증 (null 및 빈 문자열 체크)
     * @param value 검증할 문자열
     * @param fieldName 필드명 (에러 메시지용)
     * @throws IllegalArgumentException 검증 실패 시
     */
    public static void validateString(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            String message = String.format("%s는 null이거나 빈 문자열일 수 없습니다", fieldName);
            logger.error("문자열 검증 실패: {}", message);
            throw new IllegalArgumentException(message);
        }
    }
}

