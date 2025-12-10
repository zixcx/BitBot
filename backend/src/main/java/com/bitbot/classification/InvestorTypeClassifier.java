package com.bitbot.classification;

import com.bitbot.models.InvestorType;
import com.bitbot.models.PostAction;
import com.bitbot.models.RiskSettings;
import com.bitbot.models.TradingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 투자자 성향 분류기
 * 설문 점수에 따라 투자자 유형을 분류하고 적절한 리스크 설정 및 전략을 매핑
 */
public class InvestorTypeClassifier {
    
    private static final Logger logger = LoggerFactory.getLogger(InvestorTypeClassifier.class);
    
    /**
     * 점수에 따라 투자자 성향 분류
     */
    public InvestorType classify(int totalScore) {
        InvestorType type = InvestorType.fromScore(totalScore);
        logger.info("설문 점수 {}점 → {} 분류", totalScore, type.getKoreanName());
        return type;
    }
    
    /**
     * 투자자 성향에 따른 리스크 설정 반환
     */
    public RiskSettings getRiskSettings(InvestorType type) {
        RiskSettings settings;
        
        switch (type) {
            case CONSERVATIVE:
                // 안정 추구형: 레버리지 불가, 최대 손실 -15%, 진입 비중 5%, 익절 +10%
                settings = new RiskSettings(
                    false,  // 레버리지 불가
                    1,      // 레버리지 1배 (현물만)
                    -15.0,  // 최대 손실 -15%
                    5.0,    // 1회 진입 비중 5%
                    -15.0,  // 손절매 -15%
                    10.0    // 익절 +10% (작은 수익이라도 확실히 실현)
                );
                // DCA 전략: 손절 후 재진입 대기 (더 낮은 가격), 익절 후 관망
                settings.setPostStopLossAction(PostAction.WAIT_REENTRY);
                settings.setPostTakeProfitAction(PostAction.HOLD);
                break;
                
            case MODERATE:
                // 위험 중립형: 레버리지 불가, 손절매 -7%, 진입 비중 20%, 익절 +15%
                settings = new RiskSettings(
                    false,  // 레버리지 불가
                    1,      // 레버리지 1배
                    -7.0,   // 최대 손실 -7%
                    20.0,   // 1회 진입 비중 20%
                    -7.0,   // 손절매 -7%
                    15.0    // 익절 +15% (적당한 수익 실현)
                );
                // 추세 추종 전략: 손절 후 관망 (추세 재확인), 익절 후 관망
                settings.setPostStopLossAction(PostAction.HOLD);
                settings.setPostTakeProfitAction(PostAction.HOLD);
                break;
                
            case AGGRESSIVE:
                // 적극 투자형: 레버리지 최대 3배, 손절매 -5%, 진입 비중 30%, 익절 +20%
                settings = new RiskSettings(
                    true,   // 레버리지 허용
                    3,      // 최대 레버리지 3배
                    -5.0,   // 최대 손실 -5%
                    30.0,   // 1회 진입 비중 30%
                    -5.0,   // 손절매 -5%
                    20.0    // 익절 +20% (높은 수익 추구)
                );
                // 스윙 트레이딩: 손절 후 관망 (역추세 기회 대기), 익절 후 관망
                settings.setPostStopLossAction(PostAction.HOLD);
                settings.setPostTakeProfitAction(PostAction.HOLD);
                break;
                
            case SPECULATIVE:
                // 전문 투기형: 레버리지 최대 10배, 손절매 -3%, 진입 비중 50%, 익절 +30%
                settings = new RiskSettings(
                    true,   // 레버리지 허용
                    10,     // 최대 레버리지 10배
                    -3.0,   // 최대 손실 -3%
                    50.0,   // 1회 진입 비중 50%
                    -3.0,   // 손절매 -3%
                    30.0    // 익절 +30% (매우 높은 수익 추구)
                );
                // 변동성 돌파 전략: 손절 후 빠른 재진입 또는 반대 포지션, 익절 후 빠른 재진입
                settings.setPostStopLossAction(PostAction.QUICK_REENTRY);
                settings.setPostTakeProfitAction(PostAction.QUICK_REENTRY);
                break;
                
            default:
                // 기본값 (안정 추구형)
                settings = new RiskSettings(false, 1, -15.0, 5.0, -15.0, 10.0);
                settings.setPostStopLossAction(PostAction.WAIT_REENTRY);
                settings.setPostTakeProfitAction(PostAction.HOLD);
        }
        
        logger.debug("{} 리스크 설정: {}", type.getKoreanName(), settings);
        return settings;
    }
    
    /**
     * 투자자 성향에 따른 거래 전략 반환
     */
    public TradingStrategy getStrategy(InvestorType type) {
        TradingStrategy strategy;
        
        switch (type) {
            case CONSERVATIVE:
                strategy = TradingStrategy.SPOT_DCA;
                break;
            case MODERATE:
                strategy = TradingStrategy.TREND_FOLLOWING;
                break;
            case AGGRESSIVE:
                strategy = TradingStrategy.SWING_TRADING;
                break;
            case SPECULATIVE:
                strategy = TradingStrategy.VOLATILITY_BREAKOUT;
                break;
            default:
                strategy = TradingStrategy.SPOT_DCA;
        }
        
        logger.debug("{} 전략: {}", type.getKoreanName(), strategy.getKoreanName());
        return strategy;
    }
    
    /**
     * 전체 프로필 생성 (분류 + 설정 + 전략)
     */
    public ProfileResult createProfile(int totalScore) {
        InvestorType type = classify(totalScore);
        RiskSettings riskSettings = getRiskSettings(type);
        TradingStrategy strategy = getStrategy(type);
        
        return new ProfileResult(type, riskSettings, strategy);
    }
    
    /**
     * 프로필 결과 클래스
     */
    public static class ProfileResult {
        private final InvestorType investorType;
        private final RiskSettings riskSettings;
        private final TradingStrategy tradingStrategy;
        
        public ProfileResult(InvestorType investorType, RiskSettings riskSettings, 
                           TradingStrategy tradingStrategy) {
            this.investorType = investorType;
            this.riskSettings = riskSettings;
            this.tradingStrategy = tradingStrategy;
        }
        
        public InvestorType getInvestorType() {
            return investorType;
        }
        
        public RiskSettings getRiskSettings() {
            return riskSettings;
        }
        
        public TradingStrategy getTradingStrategy() {
            return tradingStrategy;
        }
    }
}

