package com.bitbot.exceptions;

/**
 * 거래 시스템 기본 예외 클래스
 */
public class TradingException extends Exception {
    
    public TradingException(String message) {
        super(message);
    }
    
    public TradingException(String message, Throwable cause) {
        super(message, cause);
    }
}

