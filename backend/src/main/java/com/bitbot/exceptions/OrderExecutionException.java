package com.bitbot.exceptions;

/**
 * 주문 실행 관련 예외
 */
public class OrderExecutionException extends TradingException {
    
    public OrderExecutionException(String message) {
        super(message);
    }
    
    public OrderExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}

