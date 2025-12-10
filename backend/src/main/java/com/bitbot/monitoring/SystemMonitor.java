package com.bitbot.monitoring;

import com.bitbot.data.BinanceDataCollector;
import com.bitbot.database.DatabaseConnection;
import com.bitbot.utils.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 시스템 상태 모니터링 서비스
 * 데이터베이스 연결, API 연결, Rate Limit 상태 등을 주기적으로 체크
 */
public class SystemMonitor {
    
    private static final Logger logger = LoggerFactory.getLogger(SystemMonitor.class);
    private static final SystemMonitor instance = new SystemMonitor();
    
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean isRunning;
    private ScheduledFuture<?> monitoringTask;
    
    // 모니터링 간격 (분)
    private static final int MONITORING_INTERVAL_MINUTES = 5;
    
    private SystemMonitor() {
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.isRunning = new AtomicBoolean(false);
    }
    
    /**
     * 싱글톤 인스턴스 반환
     */
    public static SystemMonitor getInstance() {
        return instance;
    }
    
    /**
     * 시스템 모니터링 시작
     */
    public void start() {
        if (isRunning.compareAndSet(false, true)) {
            logger.info("=".repeat(80));
            logger.info("📊 시스템 모니터링 서비스 시작");
            logger.info("=".repeat(80));
            logger.info("모니터링 간격: {}분마다", MONITORING_INTERVAL_MINUTES);
            logger.info("=".repeat(80) + "\n");
            
            // 주기적 모니터링 시작
            monitoringTask = scheduler.scheduleAtFixedRate(
                    this::checkSystemHealth,
                    MONITORING_INTERVAL_MINUTES,  // 초기 지연
                    MONITORING_INTERVAL_MINUTES,  // 실행 간격
                    TimeUnit.MINUTES
            );
            
            logger.info("✅ 시스템 모니터링 시작 완료 ({}분마다 체크)", MONITORING_INTERVAL_MINUTES);
        } else {
            logger.warn("시스템 모니터링이 이미 실행 중입니다.");
        }
    }
    
    /**
     * 시스템 모니터링 중지
     */
    public void stop() {
        if (isRunning.compareAndSet(true, false)) {
            logger.info("\n" + "=".repeat(80));
            logger.info("⏹️ 시스템 모니터링 중지 중...");
            logger.info("=".repeat(80));
            
            if (monitoringTask != null && !monitoringTask.isCancelled()) {
                monitoringTask.cancel(false);
            }
            
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            
            logger.info("✅ 시스템 모니터링 중지 완료");
        }
    }
    
    /**
     * 시스템 상태 체크
     */
    private void checkSystemHealth() {
        try {
            if (!isRunning.get()) {
                return;
            }
            
            logger.debug("[시스템 모니터링] 상태 체크 시작...");
            
            // 1. 데이터베이스 연결 상태 확인
            boolean dbHealthy = DatabaseConnection.isHealthy();
            if (!dbHealthy) {
                NotificationService.getInstance().notifyError(
                    "데이터베이스 연결 실패",
                    "데이터베이스 연결 상태가 비정상입니다.",
                    null
                );
            }
            
            // 2. Binance API 연결 상태 확인
            try {
                BinanceDataCollector collector = new BinanceDataCollector();
                boolean apiHealthy = collector.testConnection();
                if (!apiHealthy) {
                    NotificationService.getInstance().notifyWarning(
                        "Binance API 연결 실패",
                        "Binance API 연결 테스트에 실패했습니다."
                    );
                }
            } catch (Exception e) {
                NotificationService.getInstance().notifyError(
                    "Binance API 연결 오류",
                    "Binance API 연결 확인 중 오류가 발생했습니다: " + e.getMessage(),
                    e
                );
            }
            
            // 3. Rate Limit 사용량 확인
            RateLimiter rateLimiter = RateLimiter.getBinanceRateLimiter();
            double usageRate = rateLimiter.getUsageRate();
            if (usageRate > 0.8) {  // 80% 이상 사용 시 경고
                NotificationService.getInstance().notifyWarning(
                    "API Rate Limit 경고",
                    String.format("API Rate Limit 사용률이 %.1f%%입니다. 제한에 근접했습니다.", usageRate * 100)
                );
            }
            
            // 4. 시스템 상태 요약
            StringBuilder statusSummary = new StringBuilder();
            statusSummary.append(String.format("데이터베이스: %s, ", dbHealthy ? "정상" : "오류"));
            statusSummary.append(String.format("API Rate Limit: %.1f%%", usageRate * 100));
            
            logger.debug("[시스템 모니터링] 상태 체크 완료: {}", statusSummary.toString());
            
        } catch (Exception e) {
            logger.error("시스템 모니터링 중 오류 발생", e);
            NotificationService.getInstance().notifyError(
                "시스템 모니터링 오류",
                "시스템 상태 모니터링 중 오류가 발생했습니다: " + e.getMessage(),
                e
            );
        }
    }
    
    /**
     * 즉시 시스템 상태 체크 (수동 호출)
     */
    public void checkNow() {
        checkSystemHealth();
    }
    
    /**
     * 실행 상태 확인
     */
    public boolean isRunning() {
        return isRunning.get();
    }
}

