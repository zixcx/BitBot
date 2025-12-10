package com.bitbot.exceptions;

/**
 * 입력값 검증 관련 예외
 */
public class ValidationException extends TradingException {
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

