package com.bitbot.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * API Rate Limit 관리 유틸리티
 * 토큰 버킷 알고리즘을 사용하여 API 호출 제한 관리
 * 
 * Binance API Rate Limits:
 * - IP 기반: 1200 requests per minute
 * - Order: 10 orders per second, 100,000 orders per 24h
 */
public class RateLimiter {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimiter.class);
    
    // Binance API 기본 제한: 1200 requests per minute
    private static final int DEFAULT_MAX_REQUESTS = 1200;
    private static final long DEFAULT_INTERVAL_MS = 60_000; // 1분
    
    private final int maxRequests;
    private final long intervalMs;
    private final AtomicInteger currentRequests;
    private final AtomicLong windowStartTime;
    
    // 싱글톤 인스턴스 (Binance API용)
    private static final RateLimiter binanceRateLimiter = new RateLimiter(
        DEFAULT_MAX_REQUESTS, 
        DEFAULT_INTERVAL_MS
    );
    
    /**
     * RateLimiter 생성
     * @param maxRequests 시간 윈도우 내 최대 요청 수
     * @param intervalMs 시간 윈도우 (밀리초)
     */
    public RateLimiter(int maxRequests, long intervalMs) {
        this.maxRequests = maxRequests;
        this.intervalMs = intervalMs;
        this.currentRequests = new AtomicInteger(0);
        this.windowStartTime = new AtomicLong(System.currentTimeMillis());
    }
    
    /**
     * Binance API용 RateLimiter 인스턴스 반환
     */
    public static RateLimiter getBinanceRateLimiter() {
        return binanceRateLimiter;
    }
    
    /**
     * API 호출 허용 여부 확인 및 대기
     * Rate limit에 도달하면 다음 윈도우까지 대기
     * 
     * @throws InterruptedException 대기 중 인터럽트 발생 시
     */
    public void acquire() throws InterruptedException {
        long now = System.currentTimeMillis();
        long windowStart = windowStartTime.get();
        long elapsed = now - windowStart;
        
        // 시간 윈도우가 지났으면 리셋
        if (elapsed >= intervalMs) {
            synchronized (this) {
                // Double-check locking
                elapsed = now - windowStartTime.get();
                if (elapsed >= intervalMs) {
                    currentRequests.set(0);
                    windowStartTime.set(now);
                    logger.debug("Rate limit 윈도우 리셋: {}ms 경과", elapsed);
                }
            }
        }
        
        // 현재 요청 수 확인
        int current = currentRequests.get();
        
        // Rate limit에 도달했으면 대기
        if (current >= maxRequests) {
            long waitTime = intervalMs - elapsed;
            if (waitTime > 0) {
                logger.warn("Rate limit 도달 ({}/{}) - {}ms 대기 중...", 
                        current, maxRequests, waitTime);
                Thread.sleep(waitTime);
                
                // 대기 후 리셋
                synchronized (this) {
                    currentRequests.set(0);
                    windowStartTime.set(System.currentTimeMillis());
                }
            }
        }
        
        // 요청 수 증가
        int newCount = currentRequests.incrementAndGet();
        
        if (newCount % 100 == 0) {
            logger.debug("Rate limit 사용량: {}/{} ({}%)", 
                    newCount, maxRequests, (newCount * 100 / maxRequests));
        }
    }
    
    /**
     * API 호출 허용 여부 확인 (대기 없음)
     * @return 허용 가능 여부
     */
    public boolean tryAcquire() {
        long now = System.currentTimeMillis();
        long windowStart = windowStartTime.get();
        long elapsed = now - windowStart;
        
        // 시간 윈도우가 지났으면 리셋
        if (elapsed >= intervalMs) {
            synchronized (this) {
                elapsed = now - windowStartTime.get();
                if (elapsed >= intervalMs) {
                    currentRequests.set(0);
                    windowStartTime.set(now);
                }
            }
        }
        
        // Rate limit 확인
        int current = currentRequests.get();
        if (current >= maxRequests) {
            return false;
        }
        
        currentRequests.incrementAndGet();
        return true;
    }
    
    /**
     * 현재 사용량 조회
     * @return 현재 요청 수 / 최대 요청 수
     */
    public double getUsageRate() {
        long now = System.currentTimeMillis();
        long windowStart = windowStartTime.get();
        long elapsed = now - windowStart;
        
        if (elapsed >= intervalMs) {
            return 0.0; // 윈도우가 지났으면 0%
        }
        
        int current = currentRequests.get();
        return (double) current / maxRequests;
    }
    
    /**
     * 다음 윈도우까지 남은 시간 (밀리초)
     */
    public long getRemainingTime() {
        long now = System.currentTimeMillis();
        long windowStart = windowStartTime.get();
        long elapsed = now - windowStart;
        
        if (elapsed >= intervalMs) {
            return 0;
        }
        
        return intervalMs - elapsed;
    }
    
    /**
     * 현재 상태 정보 반환
     */
    public String getStatus() {
        int current = currentRequests.get();
        double usage = getUsageRate() * 100;
        long remaining = getRemainingTime();
        
        return String.format("Rate Limit: %d/%d (%.1f%%) - 다음 리셋까지 %dms", 
                current, maxRequests, usage, remaining);
    }
}

