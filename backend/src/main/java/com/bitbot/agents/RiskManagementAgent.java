package com.bitbot.agents;

import com.bitbot.models.AccountInfo;
import com.bitbot.models.RiskSettings;
import com.bitbot.models.TradingDecision;
import com.bitbot.models.UserProfile;
import com.bitbot.utils.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 리스크 관리 에이전트 (규칙 기반)
 * 사용자 프로필 기반으로 LLM의 예비 결정을 최종 검증하여 거래 승인/거부
 */
public class RiskManagementAgent {
    
    private static final Logger logger = LoggerFactory.getLogger(RiskManagementAgent.class);
    
    // 기본값 (프로필이 없을 때 사용)
    private final double defaultMaxInvestmentPercent;
    private final double defaultMaxTotalInvestmentPercent;
    private final double defaultStopLossPercent;
    
    public RiskManagementAgent() {
        this.defaultMaxInvestmentPercent = ConfigLoader.getMaxInvestmentPercent();
        this.defaultMaxTotalInvestmentPercent = ConfigLoader.getMaxTotalInvestmentPercent();
        this.defaultStopLossPercent = ConfigLoader.getStopLossPercent();
    }
    
    /**
     * 리스크 검증 수행 (프로필 기반)
     * @param decision 예비 투자 결정
     * @param accountInfo 현재 계좌 정보
     * @param orderAmount 주문 금액 (USD)
     * @param userProfile 사용자 프로필 (null이면 기본값 사용)
     * @return 승인 여부 및 사유
     */
    public RiskCheckResult validateDecision(TradingDecision decision, 
                                            AccountInfo accountInfo, 
                                            double orderAmount,
                                            UserProfile userProfile) {
        
        logger.info("[리스크 관리] 안전장치 검증 시작...");
        logger.info("예비 결정: {}, 주문 금액: ${:.2f}", decision.getDecision(), orderAmount);
        
        // 프로필에서 리스크 설정 가져오기 (없으면 기본값 사용)
        RiskSettings riskSettings = userProfile != null && userProfile.getRiskSettings() != null
                ? userProfile.getRiskSettings()
                : getDefaultRiskSettings();
        
        if (userProfile != null) {
            logger.info("사용자 프로필 적용: {} - {}", 
                    userProfile.getInvestorType().getKoreanName(), riskSettings);
        }
        
        // 레버리지 적용 여부 확인
        int leverage = riskSettings.isLeverageAllowed() ? riskSettings.getMaxLeverage() : 1;
        double leveragedPositionSize = orderAmount * leverage;
        
        // 규칙 1: 1회 최대 투자 금액 제한 (레버리지 적용 전 실제 투자 금액 기준)
        double maxSingleOrder = accountInfo.getTotalBalance() * 
                               (riskSettings.getMaxPositionPercent() / 100.0);
        if (orderAmount > maxSingleOrder) {
            String reason = String.format(
                    "규칙 위반 [1회 최대 투자 제한]: 주문 금액 $%.2f이 최대 허용액 $%.2f (총 자산의 %.1f%%)를 초과",
                    orderAmount, maxSingleOrder, riskSettings.getMaxPositionPercent());
            logger.warn("[리스크 관리] {}", reason);
            return new RiskCheckResult(false, reason);
        }
        
        // 규칙 1-1: 레버리지 사용 시 포지션 크기 제한 (레버리지 적용 후)
        if (leverage > 1) {
            double maxLeveragedPosition = accountInfo.getTotalBalance() * leverage;
            if (leveragedPositionSize > maxLeveragedPosition) {
                String reason = String.format(
                        "규칙 위반 [레버리지 포지션 제한]: 레버리지 적용 포지션 $%.2f이 최대 허용 포지션 $%.2f를 초과",
                        leveragedPositionSize, maxLeveragedPosition);
                logger.warn("[리스크 관리] {}", reason);
                return new RiskCheckResult(false, reason);
            }
            
            logger.info("[리스크 관리] 레버리지 {}배 적용: 실제 투자 ${:.2f} → 포지션 크기 ${:.2f}",
                    leverage, orderAmount, leveragedPositionSize);
        }
        
        // 규칙 2: 총 투자 비중 제한 (매수 주문만 해당)
        if (decision.getDecision() == TradingDecision.Decision.BUY || 
            decision.getDecision() == TradingDecision.Decision.STRONG_BUY) {
            
            double newInvestedAmount = accountInfo.getInvestedAmount() + orderAmount;
            double maxTotalInvestment = accountInfo.getTotalBalance() * 
                                       (riskSettings.getMaxPositionPercent() / 100.0);
            
            if (newInvestedAmount > maxTotalInvestment) {
                String reason = String.format(
                        "규칙 위반 [총 투자 비중 제한]: 신규 투자 후 총 투자액 $%.2f이 최대 허용액 $%.2f (총 자산의 %.1f%%)를 초과",
                        newInvestedAmount, maxTotalInvestment, riskSettings.getMaxPositionPercent());
                logger.warn("[리스크 관리] {}", reason);
                return new RiskCheckResult(false, reason);
            }
        }
        
        // 규칙 3: 손실 방지 서킷 브레이커
        if (accountInfo.getProfitLossPercent() < riskSettings.getMaxLossPercent()) {
            if (decision.getDecision() == TradingDecision.Decision.BUY || 
                decision.getDecision() == TradingDecision.Decision.STRONG_BUY) {
                
                String reason = String.format(
                        "규칙 위반 [손실 방지 서킷 브레이커]: 현재 손실률 %.2f%%가 손절 기준 %.1f%%를 초과. 신규 매수 중단",
                        accountInfo.getProfitLossPercent(), riskSettings.getMaxLossPercent());
                logger.warn("[리스크 관리] {}", reason);
                return new RiskCheckResult(false, reason);
            }
        }
        
        // 규칙 4: 최소 신뢰도 기준
        double minConfidence = ConfigLoader.getMinConfidenceThreshold();
        if (decision.getConfidence() < minConfidence) {
            String reason = String.format(
                    "규칙 위반 [최소 신뢰도]: 결정 신뢰도 %.2f가 최소 기준 %.2f보다 낮음",
                    decision.getConfidence(), minConfidence);
            logger.warn("[리스크 관리] {}", reason);
            return new RiskCheckResult(false, reason);
        }
        
        // 규칙 5: HOLD 결정 처리
        if (decision.getDecision() == TradingDecision.Decision.HOLD) {
            String reason = "예비 결정이 HOLD(관망)이므로 거래를 실행하지 않음";
            logger.info("[리스크 관리] {}", reason);
            return new RiskCheckResult(false, reason);
        }
        
        // 모든 규칙 통과
        String reason = "모든 안전장치 규칙 통과. 거래 승인";
        logger.info("[리스크 관리] ✅ {}", reason);
        return new RiskCheckResult(true, reason);
    }
    
    /**
     * 기본 리스크 설정 (프로필이 없을 때)
     */
    private RiskSettings getDefaultRiskSettings() {
        return new RiskSettings(
            false,  // 레버리지 불가
            1,
            defaultStopLossPercent,
            defaultMaxInvestmentPercent,
            defaultStopLossPercent
        );
    }
    
    /**
     * 리스크 검증 결과
     */
    public static class RiskCheckResult {
        private final boolean approved;
        private final String reason;
        
        public RiskCheckResult(boolean approved, String reason) {
            this.approved = approved;
            this.reason = reason;
        }
        
        public boolean isApproved() {
            return approved;
        }
        
        public String getReason() {
            return reason;
        }
        
        @Override
        public String toString() {
            return String.format("RiskCheck[%s]: %s", 
                    approved ? "APPROVED" : "REJECTED", reason);
        }
    }
}


