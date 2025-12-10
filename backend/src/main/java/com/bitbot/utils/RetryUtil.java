package com.bitbot.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * 재시도 유틸리티
 * 지수 백오프(Exponential Backoff)를 사용한 재시도 로직
 */
public class RetryUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(RetryUtil.class);
    
    /**
     * 지수 백오프를 사용한 재시도 실행
     * 
     * @param operation 실행할 작업
     * @param maxRetries 최대 재시도 횟수
     * @param initialDelayMs 초기 지연 시간 (밀리초)
     * @param <T> 반환 타입
     * @return 작업 결과
     * @throws Exception 모든 재시도 실패 시 마지막 예외
     */
    public static <T> T retryWithBackoff(
            Supplier<T> operation,
            int maxRetries,
            long initialDelayMs
    ) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return operation.get();
                
            } catch (Exception e) {
                lastException = e;
                
                // 마지막 시도가 아니면 재시도
                if (attempt < maxRetries) {
                    // 지수 백오프: 1초, 2초, 4초, 8초...
                    long delayMs = initialDelayMs * (1L << attempt);
                    
                    logger.warn("작업 실패 (시도 {}/{}): {}. {}ms 후 재시도...",
                            attempt + 1, maxRetries + 1, e.getMessage(), delayMs);
                    
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("재시도 중 인터럽트됨", ie);
                    }
                } else {
                    logger.error("모든 재시도 실패 (총 {}회 시도)", maxRetries + 1, e);
                }
            }
        }
        
        // 모든 재시도 실패
        throw lastException != null ? lastException : new RuntimeException("재시도 실패");
    }
    
    /**
     * 재시도 가능한 예외인지 확인
     * 일시적인 네트워크 오류나 서버 오류는 재시도 가능
     * 
     * @param e 예외
     * @return 재시도 가능 여부
     */
    public static boolean isRetryableException(Exception e) {
        String message = e.getMessage();
        if (message == null) {
            return false;
        }
        
        // 재시도 가능한 오류 패턴
        String[] retryablePatterns = {
            "timeout",
            "connection",
            "network",
            "503",  // Service Unavailable
            "502",  // Bad Gateway
            "504",  // Gateway Timeout
            "429",  // Too Many Requests
            "500",  // Internal Server Error
            "502",  // Bad Gateway
            "503",  // Service Unavailable
            "504"   // Gateway Timeout
        };
        
        String lowerMessage = message.toLowerCase();
        for (String pattern : retryablePatterns) {
            if (lowerMessage.contains(pattern)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 재시도 가능한 예외만 재시도하는 실행
     * 
     * @param operation 실행할 작업
     * @param maxRetries 최대 재시도 횟수
     * @param initialDelayMs 초기 지연 시간 (밀리초)
     * @param <T> 반환 타입
     * @return 작업 결과
     * @throws Exception 재시도 불가능한 예외 또는 모든 재시도 실패 시
     */
    public static <T> T retryIfRetryable(
            Supplier<T> operation,
            int maxRetries,
            long initialDelayMs
    ) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return operation.get();
                
            } catch (Exception e) {
                lastException = e;
                
                // 재시도 불가능한 예외면 즉시 throw
                if (!isRetryableException(e)) {
                    logger.warn("재시도 불가능한 예외: {}", e.getMessage());
                    throw e;
                }
                
                // 마지막 시도가 아니면 재시도
                if (attempt < maxRetries) {
                    long delayMs = initialDelayMs * (1L << attempt);
                    
                    logger.warn("재시도 가능한 오류 발생 (시도 {}/{}): {}. {}ms 후 재시도...",
                            attempt + 1, maxRetries + 1, e.getMessage(), delayMs);
                    
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("재시도 중 인터럽트됨", ie);
                    }
                } else {
                    logger.error("모든 재시도 실패 (총 {}회 시도)", maxRetries + 1, e);
                }
            }
        }
        
        throw lastException != null ? lastException : new RuntimeException("재시도 실패");
    }
}

