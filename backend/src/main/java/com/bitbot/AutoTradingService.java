package com.bitbot;

import com.bitbot.models.TradingStrategy;
import com.bitbot.monitoring.NotificationService;
import com.bitbot.monitoring.SystemMonitor;
import com.bitbot.utils.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 자동 거래 서비스
 * 주기적으로 거래 사이클을 실행하는 스케줄러
 * 전략별로 적절한 실행 주기를 자동 설정
 */
public class AutoTradingService {
    
    private static final Logger logger = LoggerFactory.getLogger(AutoTradingService.class);
    
    private final TradingEngine tradingEngine;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean isRunning;
    private ScheduledFuture<?> scheduledTask;
    
    // 실행 간격 (분)
    private final int intervalMinutes;
    private final TradingStrategy strategy;
    
    // 손실 모니터링 서비스 (실시간 손실 감지)
    private LossMonitor lossMonitor;
    
    public AutoTradingService(TradingEngine tradingEngine) {
        this(tradingEngine, null);
    }
    
    /**
     * 전략 기반 자동 거래 서비스 생성
     * @param tradingEngine 거래 엔진
     * @param strategy 거래 전략 (null이면 환경 변수 또는 기본값 사용)
     */
    public AutoTradingService(TradingEngine tradingEngine, TradingStrategy strategy) {
        this.tradingEngine = tradingEngine;
        this.strategy = strategy;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.isRunning = new AtomicBoolean(false);
        
        // 전략별 실행 주기 결정
        if (strategy != null) {
            this.intervalMinutes = getIntervalForStrategy(strategy);
            logger.info("전략 기반 실행 주기 설정: {} → {}분마다 실행", 
                    strategy.getKoreanName(), intervalMinutes);
        } else {
            // 환경 변수에서 간격 읽기 (기본값: 15분)
            this.intervalMinutes = ConfigLoader.getInt("ANALYSIS_INTERVAL_MINUTES", 15);
            logger.info("환경 변수 기반 실행 주기: {}분마다 실행", intervalMinutes);
        }
    }
    
    /**
     * 전략별 실행 주기 반환 (분 단위)
     * 전략의 시간봉과 일치하도록 설정
     */
    private int getIntervalForStrategy(TradingStrategy strategy) {
        switch (strategy) {
            case SPOT_DCA:
                // DCA: 1일봉 사용 → 4시간마다 실행 (장기 투자, 빈번한 실행 불필요)
                return 240; // 4시간
            case TREND_FOLLOWING:
                // 추세 추종: 4시간봉 사용 → 4시간마다 실행
                return 240; // 4시간
            case SWING_TRADING:
                // 스윙 트레이딩: 1시간봉 사용 → 1시간마다 실행
                return 60;  // 1시간
            case VOLATILITY_BREAKOUT:
                // 변동성 돌파: 15분봉 사용 → 15분마다 실행
                return 15;  // 15분
            default:
                // 기본값: 15분
                return ConfigLoader.getInt("ANALYSIS_INTERVAL_MINUTES", 15);
        }
    }
    
    /**
     * 자동 거래 시작
     */
    public void start() {
        if (isRunning.compareAndSet(false, true)) {
            logger.info("=".repeat(80));
            logger.info("🚀 자동 거래 서비스 시작");
            logger.info("=".repeat(80));
            if (strategy != null) {
                logger.info("거래 전략: {}", strategy.getKoreanName());
                logger.info("전략 시간봉: {}", getTimeframeForStrategy(strategy));
            }
            logger.info("실행 간격: {}분 ({}시간)", intervalMinutes, intervalMinutes / 60.0);
            logger.info("거래 모드: {}", ConfigLoader.getTradingMode());
            logger.info("=".repeat(80) + "\n");
            
            // 즉시 1회 실행
            logger.info("초기 거래 사이클 실행...");
            executeTradingCycle();
            
            // 주기적 실행 스케줄링
            scheduledTask = scheduler.scheduleAtFixedRate(
                    this::executeTradingCycle,
                    intervalMinutes,  // 초기 지연
                    intervalMinutes,  // 실행 간격
                    TimeUnit.MINUTES
            );
            
            logger.info("✅ 자동 거래 스케줄러 시작 완료 ({}분마다 실행)", intervalMinutes);
            
            // 시스템 시작 알림
            String statusDetails = String.format(
                "거래 전략: %s, 실행 간격: %d분, 거래 모드: %s",
                strategy != null ? strategy.getKoreanName() : "기본",
                intervalMinutes,
                ConfigLoader.getTradingMode()
            );
            NotificationService.getInstance().notifySystemStatus("자동 거래 서비스 시작", statusDetails);
            
            // 손익 모니터링 서비스 시작 (실시간 손익 감지)
            // 거래 주기와 독립적으로 1분마다 손익률 체크 (손절 + 익절)
            if (lossMonitor == null) {
                Integer userId = tradingEngine.getUserId();
                if (userId != null) {
                    lossMonitor = new LossMonitor(tradingEngine, userId);
                    lossMonitor.registerShutdownHook();
                    lossMonitor.start();
                    logger.info("✅ 손익 모니터링 서비스 시작 완료 (1분마다 체크 - 손절/익절)");
                }
            }
            
            // 시스템 모니터링 서비스 시작
            SystemMonitor.getInstance().start();
            logger.info("✅ 시스템 모니터링 서비스 시작 완료 (5분마다 체크)");
            
        } else {
            logger.warn("자동 거래 서비스가 이미 실행 중입니다.");
        }
    }
    
    /**
     * 자동 거래 중지
     */
    public void stop() {
        if (isRunning.compareAndSet(true, false)) {
            logger.info("\n" + "=".repeat(80));
            logger.info("⏹️ 자동 거래 서비스 중지 중...");
            logger.info("=".repeat(80));
            
            if (scheduledTask != null && !scheduledTask.isCancelled()) {
                scheduledTask.cancel(false);
            }
            
            // 손실 모니터링 중지
            if (lossMonitor != null) {
                lossMonitor.stop();
            }
            
            // 시스템 모니터링 중지
            SystemMonitor.getInstance().stop();
            
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            
            logger.info("✅ 자동 거래 서비스 중지 완료");
            
            // 시스템 중지 알림
            NotificationService.getInstance().notifySystemStatus("자동 거래 서비스 중지", "정상적으로 종료되었습니다.");
        }
    }
    
    /**
     * 거래 사이클 실행 (예외 처리 포함)
     */
    private void executeTradingCycle() {
        try {
            if (!isRunning.get()) {
                return;
            }
            
            logger.info("\n" + "=".repeat(80));
            logger.info("📊 자동 거래 사이클 실행 - {}", 
                    java.time.LocalDateTime.now().format(
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            logger.info("=".repeat(80));
            
            tradingEngine.runOneCycle();
            
            logger.info("\n✅ 거래 사이클 완료. 다음 실행까지 {}분 대기...", intervalMinutes);
            
        } catch (Exception e) {
            logger.error("거래 사이클 실행 중 오류 발생", e);
            logger.warn("다음 실행까지 {}분 대기 후 재시도...", intervalMinutes);
            
            // 거래 사이클 오류 알림
            NotificationService.getInstance().notifyError(
                "자동 거래 사이클 오류",
                "자동 거래 사이클 실행 중 오류가 발생했습니다: " + e.getMessage(),
                e
            );
        }
    }
    
    /**
     * 실행 상태 확인
     */
    public boolean isRunning() {
        return isRunning.get();
    }
    
    /**
     * 전략별 시간봉 반환 (로깅용)
     */
    private String getTimeframeForStrategy(TradingStrategy strategy) {
        switch (strategy) {
            case SPOT_DCA:
                return "1d";
            case TREND_FOLLOWING:
                return "4h";
            case SWING_TRADING:
                return "1h";
            case VOLATILITY_BREAKOUT:
                return "15m";
            default:
                return "15m";
        }
    }
    
    /**
     * 현재 실행 주기 반환
     */
    public int getIntervalMinutes() {
        return intervalMinutes;
    }
    
    /**
     * 현재 전략 반환
     */
    public TradingStrategy getStrategy() {
        return strategy;
    }
    
    /**
     * 종료 핸들러 등록 (JVM 종료 시 자동 중지)
     */
    public void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("\n프로그램 종료 신호 감지...");
            stop();
        }));
    }
}

