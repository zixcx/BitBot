package com.bitbot.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RateLimiter 단위 테스트
 */
class RateLimiterTest {
    
    private RateLimiter rateLimiter;
    
    @BeforeEach
    void setUp() {
        // 테스트용 RateLimiter: 10 requests per 1 second
        rateLimiter = new RateLimiter(10, 1000);
    }
    
    @Test
    @DisplayName("RateLimiter 생성 테스트")
    void testRateLimiterCreation() {
        assertNotNull(rateLimiter);
    }
    
    @Test
    @DisplayName("Binance RateLimiter 싱글톤 테스트")
    void testBinanceRateLimiterSingleton() {
        RateLimiter limiter1 = RateLimiter.getBinanceRateLimiter();
        RateLimiter limiter2 = RateLimiter.getBinanceRateLimiter();
        
        assertSame(limiter1, limiter2);
    }
    
    @Test
    @DisplayName("tryAcquire - 정상 케이스")
    void testTryAcquire_Valid() throws InterruptedException {
        // 처음 10번은 성공해야 함
        for (int i = 0; i < 10; i++) {
            assertTrue(rateLimiter.tryAcquire(), "요청 " + (i + 1) + "번째는 성공해야 함");
        }
        
        // 11번째는 실패해야 함
        assertFalse(rateLimiter.tryAcquire(), "11번째 요청은 실패해야 함");
    }
    
    @Test
    @DisplayName("acquire - 정상 케이스")
    void testAcquire_Valid() throws InterruptedException {
        // 처음 10번은 성공해야 함
        for (int i = 0; i < 10; i++) {
            rateLimiter.acquire();
        }
        
        // 11번째는 대기 후 성공해야 함 (시간 윈도우가 지나면)
        long startTime = System.currentTimeMillis();
        rateLimiter.acquire();
        long elapsed = System.currentTimeMillis() - startTime;
        
        // 대기 시간이 있어야 함 (최소 100ms 이상)
        assertTrue(elapsed >= 100, "Rate limit 도달 시 대기해야 함");
    }
    
    @Test
    @DisplayName("getUsageRate - 초기 상태")
    void testGetUsageRate_Initial() {
        double usage = rateLimiter.getUsageRate();
        assertEquals(0.0, usage, 0.01, "초기 사용률은 0%여야 함");
    }
    
    @Test
    @DisplayName("getUsageRate - 사용 중")
    void testGetUsageRate_InUse() throws InterruptedException {
        // 5번 요청
        for (int i = 0; i < 5; i++) {
            rateLimiter.tryAcquire();
        }
        
        double usage = rateLimiter.getUsageRate();
        assertEquals(0.5, usage, 0.01, "5/10 = 50% 사용률이어야 함");
    }
    
    @Test
    @DisplayName("getUsageRate - 최대 사용")
    void testGetUsageRate_MaxUsage() throws InterruptedException {
        // 10번 요청 (최대)
        for (int i = 0; i < 10; i++) {
            rateLimiter.tryAcquire();
        }
        
        double usage = rateLimiter.getUsageRate();
        assertEquals(1.0, usage, 0.01, "10/10 = 100% 사용률이어야 함");
    }
    
    @Test
    @DisplayName("getRemainingTime - 초기 상태")
    void testGetRemainingTime_Initial() {
        long remaining = rateLimiter.getRemainingTime();
        assertTrue(remaining > 0 && remaining <= 1000, "남은 시간은 0-1000ms 사이여야 함");
    }
    
    @Test
    @DisplayName("윈도우 리셋 테스트")
    void testWindowReset() throws InterruptedException {
        // 10번 요청으로 윈도우 채우기
        for (int i = 0; i < 10; i++) {
            rateLimiter.tryAcquire();
        }
        
        // 윈도우가 가득 참
        assertFalse(rateLimiter.tryAcquire());
        
        // 1초 대기 (윈도우 리셋)
        Thread.sleep(1100);
        
        // 다시 요청 가능해야 함
        assertTrue(rateLimiter.tryAcquire(), "윈도우 리셋 후 요청 가능해야 함");
    }
    
    @Test
    @DisplayName("getStatus 테스트")
    void testGetStatus() throws InterruptedException {
        rateLimiter.tryAcquire();
        String status = rateLimiter.getStatus();
        
        assertNotNull(status);
        assertTrue(status.contains("Rate Limit"));
        assertTrue(status.contains("1/10") || status.contains("10"));
    }
}

