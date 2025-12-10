package com.bitbot;

import com.bitbot.agents.*;
import com.bitbot.data.BinanceDataCollector;
import com.bitbot.database.TradeRepository;
import com.bitbot.database.UserProfileRepository;
import com.bitbot.indicators.TechnicalIndicators;
import com.bitbot.monitoring.NotificationService;
import com.bitbot.models.AccountInfo;
import com.bitbot.models.MarketData;
import com.bitbot.models.TradeOrder;
import com.bitbot.models.TradingDecision;
import com.bitbot.models.TradingStrategy;
import com.bitbot.models.UserProfile;
import com.bitbot.strategy.StrategyExecutor;
import com.bitbot.trading.OrderExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 거래 엔진 - 전체 거래 프로세스를 조율
 * 데이터 수집 → 분석 → 의사결정 → 리스크 검증 → 주문 실행
 */
public class TradingEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(TradingEngine.class);
    
    private final BinanceDataCollector dataCollector;
    private final TechnicalAnalystAgent technicalAnalyst;
    private final SentimentAnalystAgent sentimentAnalyst;
    private final MasterCoordinatorAgent coordinator;
    private final RiskManagementAgent riskManager;
    private final OrderExecutor orderExecutor;
    private final TradeRepository tradeRepository;
    private final UserProfileRepository profileRepository;
    private final com.bitbot.database.TradeLogRepository tradeLogRepository;
    
    // 현재 사용자 ID (실제 구현에서는 인증 시스템에서 가져옴)
    private Integer currentUserId = 1;  // 기본값: 1
    
    // 긴급 주문 실행 플래그 (중복 실행 방지)
    private volatile boolean isEmergencyOrderExecuting = false;
    
    // 거래 사이클 동시 실행 방지
    private final Object cycleLock = new Object();
    private volatile boolean isCycleExecuting = false;
    
    // LLM 병렬 실행을 위한 스레드 풀
    private final ExecutorService llmExecutorService;
    
    public TradingEngine() {
        this.dataCollector = new BinanceDataCollector();
        this.technicalAnalyst = new TechnicalAnalystAgent();
        this.sentimentAnalyst = new SentimentAnalystAgent();
        this.coordinator = new MasterCoordinatorAgent();
        this.riskManager = new RiskManagementAgent();
        this.orderExecutor = new OrderExecutor();
        this.tradeRepository = new TradeRepository();
        this.profileRepository = new UserProfileRepository();
        this.tradeLogRepository = new com.bitbot.database.TradeLogRepository();
        
        // LLM 병렬 실행을 위한 스레드 풀 (2개 스레드: Technical + Sentiment)
        this.llmExecutorService = Executors.newFixedThreadPool(2);
        
        logger.info("거래 엔진 초기화 완료");
    }
    
    /**
     * 사용자 ID 설정
     */
    public void setUserId(Integer userId) {
        this.currentUserId = userId;
    }
    
    /**
     * 현재 사용자 ID 반환
     */
    public Integer getUserId() {
        return currentUserId;
    }
    
    /**
     * 1회 분석 및 거래 실행 (동시 실행 방지)
     */
    public void runOneCycle() {
        // 동시 실행 방지
        synchronized (cycleLock) {
            if (isCycleExecuting) {
                logger.warn("거래 사이클이 이미 실행 중입니다. 중복 실행 방지.");
                return;
            }
            isCycleExecuting = true;
        }
        
        try {
            logger.info("=".repeat(80));
            logger.info("거래 사이클 시작");
            logger.info("=".repeat(80));
            // 사용자 프로필 조회 (전략별 시간봉 결정을 위해 먼저 조회)
            UserProfile userProfile = profileRepository.findByUserId(currentUserId);
            
            // 전략별 시간봉 결정
            TradingStrategy strategy = userProfile != null 
                    ? userProfile.getTradingStrategy() 
                    : TradingStrategy.SPOT_DCA; // 기본값
            
            String timeframe = getTimeframeForStrategy(strategy);
            
            // 1단계: 데이터 수집 (전략별 시간봉 사용)
            logger.info("\n[1단계] 시장 데이터 수집 중... (전략: {}, 시간봉: {})", 
                    strategy.getKoreanName(), timeframe);
            
            // PRD 요구사항: 50개 캔들 (토큰 절약)
            List<MarketData> marketData = dataCollector.getKlines("BTCUSDT", timeframe, 50);
            if (marketData.isEmpty()) {
                logger.error("시장 데이터 수집 실패: 빈 데이터");
                return;
            }
            
            logger.info("✅ 캔들 데이터 {}개 수집 완료 ({} 시간봉)", marketData.size(), timeframe);
            
            // 2단계: 기술 지표 계산
            logger.info("\n[2단계] 기술 지표 계산 중...");
            TechnicalIndicators.calculateAllIndicators(marketData);
            logger.info("✅ RSI, MACD, 이동평균, 볼린저밴드 계산 완료");
            
            MarketData latest = marketData.get(marketData.size() - 1);
            logger.info("현재 가격: ${}, RSI: {}", latest.getClose(), latest.getRsi());
            
            // 프로필 정보 로깅
            if (userProfile != null) {
                logger.info("\n[프로필] 사용자 프로필 적용: {}", userProfile);
            } else {
                logger.info("\n[프로필] 사용자 프로필 없음 - 기본 설정 사용");
            }
            
            // 3단계: 병렬 분석 (LLM 에이전트들) - 프로필 전달
            logger.info("\n[3단계] LLM 에이전트 병렬 분석 시작...");
            
            // 병렬 실행: CompletableFuture 사용
            CompletableFuture<TradingDecision> techFuture = CompletableFuture.supplyAsync(
                () -> {
                    try {
                        logger.info("[병렬 실행] 기술적 분석 시작...");
                        TradingDecision decision = technicalAnalyst.analyze(marketData, userProfile);
                        logger.info("[병렬 실행] 기술적 분석 완료: {}", decision.getDecision());
                        return decision;
                    } catch (Exception e) {
                        logger.error("[병렬 실행] 기술적 분석 실패", e);
                        return new TradingDecision(
                            "Technical Analyst",
                            TradingDecision.Decision.HOLD,
                            0.0,
                            "분석 실패: " + e.getMessage()
                        );
                    }
                },
                llmExecutorService
            );
            
            CompletableFuture<TradingDecision> sentimentFuture = CompletableFuture.supplyAsync(
                () -> {
                    try {
                        logger.info("[병렬 실행] 시장 심리 분석 시작...");
                        TradingDecision decision = sentimentAnalyst.analyze(latest.getClose(), userProfile);
                        logger.info("[병렬 실행] 시장 심리 분석 완료: {}", decision.getDecision());
                        return decision;
                    } catch (Exception e) {
                        logger.error("[병렬 실행] 시장 심리 분석 실패", e);
                        return new TradingDecision(
                            "Sentiment Analyst",
                            TradingDecision.Decision.HOLD,
                            0.0,
                            "분석 실패: " + e.getMessage()
                        );
                    }
                },
                llmExecutorService
            );
            
            // 모든 분석 완료 대기
            CompletableFuture.allOf(techFuture, sentimentFuture).join();
            
            // 결과 수집
            List<TradingDecision> agentReports = new ArrayList<>();
            agentReports.add(techFuture.join());
            agentReports.add(sentimentFuture.join());
            
            logger.info("✅ 전문 에이전트 분석 완료 ({}개) - 병렬 실행으로 시간 단축", agentReports.size());
            
            // 4단계: 총괄 코디네이터 종합 결정 (프로필 기반)
            logger.info("\n[4단계] 총괄 코디네이터 종합 결정...");
            TradingDecision llmDecision = coordinator.coordinateDecision(agentReports, userProfile);
            logger.info("✅ LLM 예비 투자 결정: {}", llmDecision);
            
            // 4-1단계: 전략별 로직 적용 (이미 위에서 결정됨)
            logger.info("\n[4-1단계] 전략 적용: {} (시간봉: {})", 
                    strategy.getKoreanName(), timeframe);
            StrategyExecutor strategyExecutor = new StrategyExecutor();
            TradingDecision finalDecision = strategyExecutor.applyStrategy(
                    llmDecision, 
                    strategy, 
                    marketData, 
                    latest);
            
            logger.info("✅ 전략 적용 후 최종 결정: {}", finalDecision);
            if (!finalDecision.getDecision().equals(llmDecision.getDecision())) {
                logger.info("⚠️ 전략 필터링: {} → {}", 
                        llmDecision.getDecision(), finalDecision.getDecision());
            }
            
            // 5단계: 리스크 관리 검증
            logger.info("\n[5단계] 리스크 관리 안전장치 검증...");
            AccountInfo accountInfo = dataCollector.getAccountInfo();
            logger.info("계좌 정보: {}", accountInfo);
            
            // 주문 금액 계산 (전략별 포지션 크기 조정)
            double orderAmountPercent = userProfile != null 
                    ? userProfile.getRiskSettings().getMaxPositionPercent()
                    : 10.0;  // 기본 10%
            
            // 전략별 포지션 크기 조정
            double adjustedPercent = adjustPositionSizeForStrategy(strategy, orderAmountPercent);
            double orderAmount = accountInfo.getTotalBalance() * (adjustedPercent / 100.0);
            
            logger.info("주문 금액 계산: 기본 {}% → 전략 조정 {}% (금액: ${})", 
                    orderAmountPercent, adjustedPercent, String.format("%.2f", orderAmount));
            
            RiskManagementAgent.RiskCheckResult riskResult = 
                    riskManager.validateDecision(finalDecision, accountInfo, orderAmount, userProfile);
            
            logger.info("리스크 검증 결과: {}", riskResult);
            
            // 6단계: 주문 실행 또는 중단
            logger.info("\n[6단계] 주문 실행 단계...");
            
            // PRD 요구사항: 모든 판단(BUY/SELL/HOLD)을 trade_logs에 기록
            Double executedPrice = null;
            Double executedQty = null;
            Double realizedPnl = null;
            
            if (riskResult.isApproved()) {
                // 레버리지 설정 확인
                int leverage = 1;  // 기본값: 현물 거래
                if (userProfile != null && userProfile.getRiskSettings().isLeverageAllowed()) {
                    leverage = userProfile.getRiskSettings().getMaxLeverage();
                    logger.info("⚡ 레버리지 {}배 적용 (투자 성향: {})", 
                            leverage, userProfile.getInvestorType().getKoreanName());
                }
                
                // 주문 수량 계산
                // 레버리지 사용 시: 실제 투자 금액은 orderAmount, 포지션 크기는 orderAmount * leverage
                double positionSize = orderAmount * leverage;  // 레버리지 적용된 포지션 크기
                double quantity = positionSize / latest.getClose();
                
                logger.info("주문 수량 계산: 투자 금액 ${} × 레버리지 {}배 = 포지션 ${} (수량: {} BTC)",
                        String.format("%.2f", orderAmount),
                        leverage,
                        String.format("%.2f", positionSize),
                        String.format("%.6f", quantity));
                
                TradeOrder order;
                try {
                    order = orderExecutor.executeMarketOrder(finalDecision, quantity, leverage);
                    logger.info("주문 결과: {}", order);
                    
                    // 7단계: 데이터베이스 저장
                    if (order != null && order.getStatus() == TradeOrder.OrderStatus.FILLED) {
                        logger.info("\n[7단계] 거래 내역 저장 중...");
                        Long tradeId = tradeRepository.save(order, currentUserId != null ? 
                                currentUserId.toString() : null);
                        if (tradeId != null) {
                            logger.info("✅ 거래 내역 저장 완료 (ID: {})", tradeId);
                        }
                        
                        // 거래 실행 알림 전송
                        String orderType = order.isBuyOrder() ? "매수" : "매도";
                        NotificationService.getInstance().notifyTradeExecution(
                            orderType,
                            order.getQuantity(),
                            order.getExecutedPrice(),
                            order.getTotalCost()
                        );
                        
                        // 실행 정보 저장
                        executedPrice = order.getExecutedPrice();
                        executedQty = order.getQuantity();
                        // realizedPnl은 매도 시에만 계산되므로 일단 null로 설정
                        // (실제 구현 시 trades 테이블에서 profit_loss를 조회하여 사용)
                        realizedPnl = null;
                    }
                } catch (com.bitbot.exceptions.OrderExecutionException e) {
                    logger.error("주문 실행 중 예외 발생", e);
                    order = null;  // 주문 실패 시 null로 설정
                    
                    // 주문 실행 실패 알림
                    NotificationService.getInstance().notifyError(
                        "주문 실행 실패",
                        "거래 주문 실행 중 오류가 발생했습니다: " + e.getMessage(),
                        e
                    );
                }
                
            } else {
                logger.warn("❌ 거래 중단: {}", riskResult.getReason());
            }
            
            // 8단계: 모든 판단을 trade_logs에 기록 (PRD 요구사항: HOLD 포함)
            logger.info("\n[8단계] 거래 로그 저장 중... (모든 판단 기록)");
            String marketSnapshot = tradeLogRepository.createMarketSnapshot(latest);
            Long logId = tradeLogRepository.save(
                    finalDecision,
                    currentUserId,
                    "BTCUSDT",
                    executedPrice,
                    executedQty,
                    realizedPnl,
                    marketSnapshot
            );
            if (logId != null) {
                logger.info("✅ 거래 로그 저장 완료 (ID: {}, Action: {})", logId, finalDecision.getDecision());
            } else {
                logger.warn("⚠️ 거래 로그 저장 실패");
            }
            
        } catch (Exception e) {
            logger.error("거래 사이클 실행 중 오류 발생", e);
            
            // 거래 사이클 오류 알림
            NotificationService.getInstance().notifyError(
                "거래 사이클 오류",
                "거래 사이클 실행 중 예상치 못한 오류가 발생했습니다: " + e.getMessage(),
                e
            );
        } finally {
            // 실행 플래그 해제
            synchronized (cycleLock) {
                isCycleExecuting = false;
            }
            
            logger.info("\n" + "=".repeat(80));
            logger.info("거래 사이클 종료");
            logger.info("=".repeat(80) + "\n");
        }
    }
    
    /**
     * 긴급 손절 주문 실행
     * LossMonitor에서 호출하여 손절 기준 도달 시 즉시 실행
     * 
     * @param btcQuantity 청산할 BTC 수량
     * @param reason 손절 사유
     * @return 주문 결과
     */
    public TradeOrder executeEmergencyStopLoss(double btcQuantity, String reason) {
        if (isEmergencyOrderExecuting) {
            logger.warn("[긴급 손절] 이미 긴급 주문이 실행 중입니다. 중복 실행 방지.");
            return null;
        }
        
        synchronized (this) {
            if (isEmergencyOrderExecuting) {
                return null;
            }
            isEmergencyOrderExecuting = true;
        }
        
        try {
            logger.warn("\n" + "!".repeat(80));
            logger.warn("🚨 [긴급 손절] 즉시 주문 실행");
            logger.warn("!".repeat(80));
            logger.warn("청산 수량: {} BTC", String.format("%.6f", btcQuantity));
            logger.warn("사유: {}", reason);
            
            // 사용자 프로필 조회 (레버리지 정보)
            UserProfile userProfile = profileRepository.findByUserId(currentUserId);
            int leverage = 1;
            if (userProfile != null && userProfile.getRiskSettings().isLeverageAllowed()) {
                leverage = userProfile.getRiskSettings().getMaxLeverage();
            }
            
            // 긴급 매도 결정 생성
            TradingDecision sellDecision = new TradingDecision(
                "LossMonitor",
                TradingDecision.Decision.STRONG_SELL,
                1.0,  // 긴급 상황이므로 신뢰도 100%
                reason
            );
            
            // 주문 실행
            TradeOrder order = orderExecutor.executeMarketOrder(sellDecision, btcQuantity, leverage);
            
            // 데이터베이스 저장
            if (order.getStatus() == TradeOrder.OrderStatus.FILLED) {
                Long tradeId = tradeRepository.save(order, currentUserId != null ? 
                        currentUserId.toString() : null);
                if (tradeId != null) {
                    logger.warn("✅ [긴급 손절] 주문 저장 완료 (ID: {})", tradeId);
                }
            }
            
            logger.warn("!".repeat(80) + "\n");
            return order;
            
        } catch (Exception e) {
            logger.error("[긴급 손절] 주문 실행 중 오류 발생", e);
            return null;
        } finally {
            synchronized (this) {
                isEmergencyOrderExecuting = false;
            }
        }
    }
    
    /**
     * 긴급 익절 주문 실행
     * LossMonitor에서 호출하여 익절 기준 도달 시 즉시 실행
     * 
     * @param btcQuantity 청산할 BTC 수량
     * @param reason 익절 사유
     * @return 주문 결과
     */
    public TradeOrder executeEmergencyTakeProfit(double btcQuantity, String reason) {
        if (isEmergencyOrderExecuting) {
            logger.warn("[긴급 익절] 이미 긴급 주문이 실행 중입니다. 중복 실행 방지.");
            return null;
        }
        
        synchronized (this) {
            if (isEmergencyOrderExecuting) {
                return null;
            }
            isEmergencyOrderExecuting = true;
        }
        
        try {
            logger.info("\n" + "=".repeat(80));
            logger.info("🎉 [긴급 익절] 즉시 주문 실행");
            logger.info("=".repeat(80));
            logger.info("청산 수량: {} BTC", String.format("%.6f", btcQuantity));
            logger.info("사유: {}", reason);
            
            // 사용자 프로필 조회 (레버리지 정보)
            UserProfile userProfile = profileRepository.findByUserId(currentUserId);
            int leverage = 1;
            if (userProfile != null && userProfile.getRiskSettings().isLeverageAllowed()) {
                leverage = userProfile.getRiskSettings().getMaxLeverage();
            }
            
            // 긴급 매도 결정 생성
            TradingDecision sellDecision = new TradingDecision(
                "LossMonitor",
                TradingDecision.Decision.STRONG_SELL,
                1.0,  // 긴급 상황이므로 신뢰도 100%
                reason
            );
            
            // 주문 실행
            TradeOrder order = orderExecutor.executeMarketOrder(sellDecision, btcQuantity, leverage);
            
            // 데이터베이스 저장
            if (order.getStatus() == TradeOrder.OrderStatus.FILLED) {
                Long tradeId = tradeRepository.save(order, currentUserId != null ? 
                        currentUserId.toString() : null);
                if (tradeId != null) {
                    logger.info("✅ [긴급 익절] 주문 저장 완료 (ID: {})", tradeId);
                }
            }
            
            logger.info("=".repeat(80) + "\n");
            return order;
            
        } catch (Exception e) {
            logger.error("[긴급 익절] 주문 실행 중 오류 발생", e);
            
            // 익절 실행 오류 알림
            NotificationService.getInstance().notifyError(
                "긴급 익절 실행 오류",
                "긴급 익절 주문 실행 중 오류가 발생했습니다: " + e.getMessage(),
                e
            );
            return null;
        } finally {
            synchronized (this) {
                isEmergencyOrderExecuting = false;
            }
        }
    }
    
    /**
     * 전략별 시간봉 반환
     */
    private String getTimeframeForStrategy(TradingStrategy strategy) {
        switch (strategy) {
            case SPOT_DCA:
                return "1d";  // 일봉 (DCA는 장기 투자)
            case TREND_FOLLOWING:
                return "4h";  // 4시간봉 (추세 추종)
            case SWING_TRADING:
                return "1h";  // 1시간봉 (스윙 트레이딩)
            case VOLATILITY_BREAKOUT:
                return "15m"; // 15분봉 (단기 변동성)
            default:
                return "15m"; // 기본값
        }
    }
    
    /**
     * 전략별 포지션 크기 조정
     * DCA: 작은 단위 분할 매수
     * 변동성 돌파: 큰 단위 빠른 진입
     */
    private double adjustPositionSizeForStrategy(TradingStrategy strategy, double basePercent) {
        switch (strategy) {
            case SPOT_DCA:
                // DCA: 작은 단위 분할 매수 (기본값의 50%)
                return basePercent * 0.5;
            case TREND_FOLLOWING:
                // 추세 추종: 기본값 유지
                return basePercent;
            case SWING_TRADING:
                // 스윙: 기본값 유지
                return basePercent;
            case VOLATILITY_BREAKOUT:
                // 변동성 돌파: 큰 단위 빠른 진입 (기본값의 150%, 최대 50%)
                return Math.min(basePercent * 1.5, 50.0);
            default:
                return basePercent;
        }
    }
    
    /**
     * 연결 테스트
     */
    public boolean testConnections() {
        logger.info("시스템 연결 테스트 시작...");
        
        boolean binanceOk = dataCollector.testConnection();
        boolean orderOk = orderExecutor.canPlaceOrder();
        
        logger.info("Binance API: {}", binanceOk ? "✅ 정상" : "❌ 실패");
        logger.info("주문 실행: {}", orderOk ? "✅ 가능" : "❌ 불가");
        
        return binanceOk && orderOk;
    }
}


