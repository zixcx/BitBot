package com.bitbot.exceptions;

/**
 * 데이터 수집 관련 예외
 */
public class DataCollectionException extends TradingException {
    
    public DataCollectionException(String message) {
        super(message);
    }
    
    public DataCollectionException(String message, Throwable cause) {
        super(message, cause);
    }
}

