package com.bitbot;

import com.bitbot.data.BinanceDataCollector;
import com.bitbot.database.UserProfileRepository;
import com.bitbot.monitoring.NotificationService;
import com.bitbot.models.AccountInfo;
import com.bitbot.models.PostAction;
import com.bitbot.models.RiskSettings;
import com.bitbot.models.TradeOrder;
import com.bitbot.models.TradingStrategy;
import com.bitbot.models.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 손익 모니터링 서비스
 * 실시간으로 손익률을 모니터링하고, 손절/익절 기준 도달 시 즉시 대응
 * 
 * 거래 주기와 독립적으로 작동하여 긴급 상황에 빠르게 대응
 */
public class LossMonitor {
    
    private static final Logger logger = LoggerFactory.getLogger(LossMonitor.class);
    
    private final BinanceDataCollector dataCollector;
    private final UserProfileRepository profileRepository;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean isRunning;
    private ScheduledFuture<?> monitoringTask;
    
    // 모니터링 간격 (분) - 손실 모니터링은 더 자주 체크
    private final int monitoringIntervalMinutes;
    
    // 현재 사용자 ID
    private final Integer userId;
    
    // TradingEngine 참조 (긴급 손절/익절 실행용)
    private final TradingEngine tradingEngine;
    
    public LossMonitor(TradingEngine tradingEngine, Integer userId) {
        this.tradingEngine = tradingEngine;
        this.userId = userId;
        this.dataCollector = new BinanceDataCollector();
        this.profileRepository = new UserProfileRepository();
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.isRunning = new AtomicBoolean(false);
        
        // 손실 모니터링은 1분마다 체크 (긴급 상황 대응)
        this.monitoringIntervalMinutes = 1;
    }
    
    /**
     * 손실 모니터링 시작
     */
    public void start() {
        if (isRunning.compareAndSet(false, true)) {
            logger.info("=".repeat(80));
            logger.info("🛡️ 손익 모니터링 서비스 시작");
            logger.info("=".repeat(80));
            logger.info("모니터링 간격: {}분마다", monitoringIntervalMinutes);
            logger.info("=".repeat(80) + "\n");
            
            // 주기적 모니터링 시작
            monitoringTask = scheduler.scheduleAtFixedRate(
                    this::checkProfitLoss,
                    monitoringIntervalMinutes,  // 초기 지연
                    monitoringIntervalMinutes,  // 실행 간격
                    TimeUnit.MINUTES
            );
            
            logger.info("✅ 손익 모니터링 시작 완료 ({}분마다 체크)", monitoringIntervalMinutes);
        } else {
            logger.warn("손실 모니터링이 이미 실행 중입니다.");
        }
    }
    
    /**
     * 손실 모니터링 중지
     */
    public void stop() {
        if (isRunning.compareAndSet(true, false)) {
            logger.info("\n" + "=".repeat(80));
            logger.info("⏹️ 손익 모니터링 중지 중...");
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
            
            logger.info("✅ 손익 모니터링 중지 완료");
        }
    }
    
    /**
     * 손익 체크 및 대응 (손절 + 익절)
     */
    private void checkProfitLoss() {
        try {
            if (!isRunning.get()) {
                return;
            }
            
            // 계좌 정보 조회
            AccountInfo accountInfo = dataCollector.getAccountInfo();
            
            // 사용자 프로필 조회
            UserProfile profile = profileRepository.findByUserId(userId);
            RiskSettings riskSettings = profile != null && profile.getRiskSettings() != null
                    ? profile.getRiskSettings()
                    : getDefaultRiskSettings();
            
            double currentProfitLossPercent = accountInfo.getProfitLossPercent();
            double stopLossPercent = riskSettings.getStopLossPercent();
            double maxLossPercent = riskSettings.getMaxLossPercent();
            double takeProfitPercent = riskSettings.getTakeProfitPercent();
            
            logger.debug("[손익 모니터링] 현재 손익률: {}%, 손절: {}%, 익절: {}%, 최대 손실: {}%",
                    String.format("%.2f", currentProfitLossPercent), 
                    String.format("%.1f", stopLossPercent),
                    String.format("%.1f", takeProfitPercent),
                    String.format("%.1f", maxLossPercent));
            
            // 익절 기준 도달 확인 (우선순위 1)
            if (currentProfitLossPercent >= takeProfitPercent) {
                logger.info("\n" + "=".repeat(80));
                logger.info("🎉 익절 신호 감지!");
                logger.info("=".repeat(80));
                logger.info("현재 수익률: {}%", String.format("%.2f", currentProfitLossPercent));
                logger.info("익절 기준: {}%", String.format("%.1f", takeProfitPercent));
                logger.info("즉시 익절 실행...");
                logger.info("=".repeat(80) + "\n");
                
                // 긴급 익절 실행
                executeEmergencyTakeProfit(accountInfo, riskSettings, profile);
                
            } 
            // 손절 기준 도달 확인 (우선순위 2)
            else if (currentProfitLossPercent <= stopLossPercent) {
                logger.warn("\n" + "!".repeat(80));
                logger.warn("🚨 긴급 손절 신호 감지!");
                logger.warn("!".repeat(80));
                logger.warn("현재 손실률: {}%", String.format("%.2f", currentProfitLossPercent));
                logger.warn("손절 기준: {}%", String.format("%.1f", stopLossPercent));
                logger.warn("즉시 손절 실행...");
                logger.warn("!".repeat(80) + "\n");
                
                // 긴급 손절 실행
                executeEmergencyStopLoss(accountInfo, riskSettings, profile);
                
            } else if (currentProfitLossPercent <= maxLossPercent) {
                // 최대 손실 기준 도달 (손절 전 경고)
                logger.warn("[손익 모니터링] ⚠️ 경고: 손실률 {}%가 최대 손실 기준 {}%에 근접",
                        String.format("%.2f", currentProfitLossPercent), 
                        String.format("%.1f", maxLossPercent));
            } else if (currentProfitLossPercent > 0 && currentProfitLossPercent >= takeProfitPercent * 0.8) {
                // 익절 근접 경고 (익절 기준의 80% 도달)
                logger.info("[손익 모니터링] 💰 수익률 {}%가 익절 기준 {}%에 근접",
                        String.format("%.2f", currentProfitLossPercent),
                        String.format("%.1f", takeProfitPercent));
            }
            
        } catch (Exception e) {
            logger.error("손익 모니터링 중 오류 발생", e);
            
            // 에러 알림 전송
            NotificationService.getInstance().notifyError(
                "손익 모니터링 오류",
                "손익 모니터링 중 예상치 못한 오류가 발생했습니다.",
                e
            );
        }
    }
    
    /**
     * 긴급 손절 실행
     */
    private void executeEmergencyStopLoss(AccountInfo accountInfo, RiskSettings riskSettings, UserProfile profile) {
        try {
            logger.info("[긴급 손절] 손절 실행 시작...");
            
            // BTC 보유량 확인
            if (accountInfo.getBtcHolding() <= 0) {
                logger.info("[긴급 손절] 보유 BTC가 없어 손절할 필요가 없습니다.");
                return;
            }
            
            // 손절 주문 실행 (전체 포지션 청산)
            double btcQuantity = accountInfo.getBtcHolding();
            String reason = String.format("긴급 손절: 손실률 %.2f%%가 손절 기준 %.1f%% 도달",
                    accountInfo.getProfitLossPercent(), riskSettings.getStopLossPercent());
            
            logger.warn("[긴급 손절] 손절 주문 실행:");
            logger.warn("  - 보유 BTC: {}", String.format("%.6f", btcQuantity));
            logger.warn("  - 현재 손실률: {}%", String.format("%.2f", accountInfo.getProfitLossPercent()));
            logger.warn("  - 손절 기준: {}%", String.format("%.1f", riskSettings.getStopLossPercent()));
            logger.warn("  - 손절 주문: MARKET_SELL {} BTC", String.format("%.6f", btcQuantity));
            
            // TradingEngine을 통해 실제 손절 주문 실행
            TradeOrder order = tradingEngine.executeEmergencyStopLoss(btcQuantity, reason);
            if (order != null && order.getStatus() == TradeOrder.OrderStatus.FILLED) {
                logger.warn("✅ [긴급 손절] 주문 체결 완료: {}", order);
                
                // 손절 알림 전송
                double currentPrice = order.getExecutedPrice();
                NotificationService.getInstance().notifyStopLoss(
                    accountInfo.getProfitLossPercent(),
                    accountInfo.getTotalBalance(),
                    currentPrice
                );
            } else {
                logger.error("❌ [긴급 손절] 주문 실행 실패");
                
                // 손절 실패 알림
                NotificationService.getInstance().notifyError(
                    "손절 실행 실패",
                    "긴급 손절 주문 실행에 실패했습니다.",
                    null
                );
            }
            
            // 손절 후 대응 전략 실행
            PostAction postAction = riskSettings.getPostStopLossAction();
            if (postAction != null) {
                logger.info("\n[손절 후 대응] 전략: {}", postAction.getKoreanName());
                executePostAction(postAction, profile, "손절");
            }
            
        } catch (Exception e) {
            logger.error("[긴급 손절] 손절 실행 중 오류 발생", e);
        }
    }
    
    /**
     * 긴급 익절 실행
     */
    private void executeEmergencyTakeProfit(AccountInfo accountInfo, RiskSettings riskSettings, UserProfile profile) {
        try {
            logger.info("[긴급 익절] 익절 실행 시작...");
            
            // BTC 보유량 확인
            if (accountInfo.getBtcHolding() <= 0) {
                logger.info("[긴급 익절] 보유 BTC가 없어 익절할 필요가 없습니다.");
                return;
            }
            
            // 익절 주문 실행 (전체 포지션 청산)
            double btcQuantity = accountInfo.getBtcHolding();
            String reason = String.format("긴급 익절: 수익률 %.2f%%가 익절 기준 %.1f%% 도달",
                    accountInfo.getProfitLossPercent(), riskSettings.getTakeProfitPercent());
            
            logger.info("[긴급 익절] 익절 주문 실행:");
            logger.info("  - 보유 BTC: {}", String.format("%.6f", btcQuantity));
            logger.info("  - 현재 수익률: {}%", String.format("%.2f", accountInfo.getProfitLossPercent()));
            logger.info("  - 익절 기준: {}%", String.format("%.1f", riskSettings.getTakeProfitPercent()));
            logger.info("  - 익절 주문: MARKET_SELL {} BTC", String.format("%.6f", btcQuantity));
            
            // TradingEngine을 통해 실제 익절 주문 실행
            TradeOrder order = tradingEngine.executeEmergencyTakeProfit(btcQuantity, reason);
            if (order != null && order.getStatus() == TradeOrder.OrderStatus.FILLED) {
                logger.info("✅ [긴급 익절] 주문 체결 완료: {}", order);
                
                // 익절 알림 전송
                double currentPrice = order.getExecutedPrice();
                NotificationService.getInstance().notifyTakeProfit(
                    accountInfo.getProfitLossPercent(),
                    accountInfo.getTotalBalance(),
                    currentPrice
                );
            } else {
                logger.error("❌ [긴급 익절] 주문 실행 실패");
                
                // 익절 실패 알림
                NotificationService.getInstance().notifyError(
                    "익절 실행 실패",
                    "긴급 익절 주문 실행에 실패했습니다.",
                    null
                );
            }
            
            // 익절 후 대응 전략 실행
            PostAction postAction = riskSettings.getPostTakeProfitAction();
            if (postAction != null) {
                logger.info("\n[익절 후 대응] 전략: {}", postAction.getKoreanName());
                executePostAction(postAction, profile, "익절");
            }
            
        } catch (Exception e) {
            logger.error("[긴급 익절] 익절 실행 중 오류 발생", e);
        }
    }
    
    /**
     * 손절/익절 후 대응 전략 실행
     */
    private void executePostAction(PostAction action, UserProfile profile, String trigger) {
        try {
            TradingStrategy strategy = profile != null ? profile.getTradingStrategy() : null;
            
            switch (action) {
                case HOLD:
                    logger.info("[{} 후 대응] 관망 모드: 시장 상황을 지켜보며 다음 기회를 기다립니다.", trigger);
                    logger.info("  - 다음 거래 사이클까지 대기");
                    break;
                    
                case WAIT_REENTRY:
                    logger.info("[{} 후 대응] 재진입 대기: 더 좋은 진입 기회를 모니터링합니다.", trigger);
                    if (strategy == TradingStrategy.SPOT_DCA) {
                        logger.info("  - DCA 전략: RSI 30 이하 또는 더 낮은 가격에서 재진입 대기");
                    } else {
                        logger.info("  - 전략 신호 재확인 후 재진입");
                    }
                    // 재진입 조건 모니터링 활성화 (향후 구현)
                    break;
                    
                case REVERSE_POSITION:
                    logger.info("[{} 후 대응] 반대 포지션 검토: 추세 전환 가능성을 모니터링합니다.", trigger);
                    logger.info("  - 추세 반전 신호 확인 중...");
                    // 반대 포지션 진입 로직 (향후 구현)
                    // 예: 롱 포지션 손절 후 → 하락 추세 확인 시 숏 포지션 진입
                    break;
                    
                case QUICK_REENTRY:
                    logger.info("[{} 후 대응] 빠른 재진입 모드: 즉시 재진입 기회를 모니터링합니다.", trigger);
                    logger.info("  - 변동성 돌파 전략: 다음 돌파 신호 대기");
                    // 빠른 재진입 로직 (향후 구현)
                    // 예: 변동성 돌파 전략에서 손절 후 → 다음 돌파 신호 즉시 진입
                    break;
                    
                default:
                    logger.info("[{} 후 대응] 기본 동작: 관망", trigger);
            }
            
        } catch (Exception e) {
            logger.error("[{} 후 대응] 전략 실행 중 오류 발생", trigger, e);
        }
    }
    
    /**
     * 기본 리스크 설정
     */
    private RiskSettings getDefaultRiskSettings() {
        RiskSettings settings = new RiskSettings(
            false,  // 레버리지 불가
            1,
            -10.0,  // 최대 손실 -10%
            10.0,   // 진입 비중 10%
            -10.0,  // 손절 -10%
            15.0    // 익절 +15% (기본값)
        );
        settings.setPostStopLossAction(PostAction.HOLD);
        settings.setPostTakeProfitAction(PostAction.HOLD);
        return settings;
    }
    
    /**
     * 실행 상태 확인
     */
    public boolean isRunning() {
        return isRunning.get();
    }
    
    /**
     * 종료 핸들러 등록
     */
    public void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("\n프로그램 종료 신호 감지 (손익 모니터링)...");
            stop();
        }));
    }
}

