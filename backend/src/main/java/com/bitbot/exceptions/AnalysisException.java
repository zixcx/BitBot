package com.bitbot.exceptions;

/**
 * 분석 관련 예외
 */
public class AnalysisException extends TradingException {
    
    public AnalysisException(String message) {
        super(message);
    }
    
    public AnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}

