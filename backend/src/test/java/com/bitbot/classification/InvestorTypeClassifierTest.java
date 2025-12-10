package com.bitbot.classification;

import com.bitbot.models.InvestorType;
import com.bitbot.models.PostAction;
import com.bitbot.models.RiskSettings;
import com.bitbot.models.TradingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * InvestorTypeClassifier 단위 테스트
 */
class InvestorTypeClassifierTest {
    
    private InvestorTypeClassifier classifier;
    
    @BeforeEach
    void setUp() {
        classifier = new InvestorTypeClassifier();
    }
    
    @Test
    @DisplayName("점수 12-20: 안정 추구형 분류")
    void testClassifyConservative() {
        // 경계값 테스트
        assertEquals(InvestorType.CONSERVATIVE, classifier.classify(12));
        assertEquals(InvestorType.CONSERVATIVE, classifier.classify(20));
        assertEquals(InvestorType.CONSERVATIVE, classifier.classify(15));
    }
    
    @Test
    @DisplayName("점수 21-28: 위험 중립형 분류")
    void testClassifyModerate() {
        assertEquals(InvestorType.MODERATE, classifier.classify(21));
        assertEquals(InvestorType.MODERATE, classifier.classify(28));
        assertEquals(InvestorType.MODERATE, classifier.classify(25));
    }
    
    @Test
    @DisplayName("점수 30-39: 적극 투자형 분류")
    void testClassifyAggressive() {
        assertEquals(InvestorType.AGGRESSIVE, classifier.classify(30));
        assertEquals(InvestorType.AGGRESSIVE, classifier.classify(39));
        assertEquals(InvestorType.AGGRESSIVE, classifier.classify(35));
    }
    
    @Test
    @DisplayName("점수 40-48: 전문 투기형 분류")
    void testClassifySpeculative() {
        assertEquals(InvestorType.SPECULATIVE, classifier.classify(40));
        assertEquals(InvestorType.SPECULATIVE, classifier.classify(48));
        assertEquals(InvestorType.SPECULATIVE, classifier.classify(44));
    }
    
    @Test
    @DisplayName("안정 추구형 리스크 설정 검증")
    void testConservativeRiskSettings() {
        RiskSettings settings = classifier.getRiskSettings(InvestorType.CONSERVATIVE);
        
        assertNotNull(settings);
        assertFalse(settings.isLeverageAllowed());
        assertEquals(1, settings.getMaxLeverage());
        assertEquals(-15.0, settings.getMaxLossPercent());
        assertEquals(5.0, settings.getMaxPositionPercent());
        assertEquals(-15.0, settings.getStopLossPercent());
        assertEquals(10.0, settings.getTakeProfitPercent());
        assertEquals(PostAction.WAIT_REENTRY, settings.getPostStopLossAction());
        assertEquals(PostAction.HOLD, settings.getPostTakeProfitAction());
    }
    
    @Test
    @DisplayName("위험 중립형 리스크 설정 검증")
    void testModerateRiskSettings() {
        RiskSettings settings = classifier.getRiskSettings(InvestorType.MODERATE);
        
        assertNotNull(settings);
        assertFalse(settings.isLeverageAllowed());
        assertEquals(1, settings.getMaxLeverage());
        assertEquals(-7.0, settings.getMaxLossPercent());
        assertEquals(20.0, settings.getMaxPositionPercent());
        assertEquals(-7.0, settings.getStopLossPercent());
        assertEquals(15.0, settings.getTakeProfitPercent());
        assertEquals(PostAction.HOLD, settings.getPostStopLossAction());
        assertEquals(PostAction.HOLD, settings.getPostTakeProfitAction());
    }
    
    @Test
    @DisplayName("적극 투자형 리스크 설정 검증")
    void testAggressiveRiskSettings() {
        RiskSettings settings = classifier.getRiskSettings(InvestorType.AGGRESSIVE);
        
        assertNotNull(settings);
        assertTrue(settings.isLeverageAllowed());
        assertEquals(3, settings.getMaxLeverage());
        assertEquals(-5.0, settings.getMaxLossPercent());
        assertEquals(30.0, settings.getMaxPositionPercent());
        assertEquals(-5.0, settings.getStopLossPercent());
        assertEquals(20.0, settings.getTakeProfitPercent());
        assertEquals(PostAction.HOLD, settings.getPostStopLossAction());
        assertEquals(PostAction.HOLD, settings.getPostTakeProfitAction());
    }
    
    @Test
    @DisplayName("전문 투기형 리스크 설정 검증")
    void testSpeculativeRiskSettings() {
        RiskSettings settings = classifier.getRiskSettings(InvestorType.SPECULATIVE);
        
        assertNotNull(settings);
        assertTrue(settings.isLeverageAllowed());
        assertEquals(10, settings.getMaxLeverage());
        assertEquals(-3.0, settings.getMaxLossPercent());
        assertEquals(50.0, settings.getMaxPositionPercent());
        assertEquals(-3.0, settings.getStopLossPercent());
        assertEquals(30.0, settings.getTakeProfitPercent());
        assertEquals(PostAction.QUICK_REENTRY, settings.getPostStopLossAction());
        assertEquals(PostAction.QUICK_REENTRY, settings.getPostTakeProfitAction());
    }
    
    @Test
    @DisplayName("안정 추구형 전략 검증")
    void testConservativeStrategy() {
        TradingStrategy strategy = classifier.getStrategy(InvestorType.CONSERVATIVE);
        assertEquals(TradingStrategy.SPOT_DCA, strategy);
    }
    
    @Test
    @DisplayName("위험 중립형 전략 검증")
    void testModerateStrategy() {
        TradingStrategy strategy = classifier.getStrategy(InvestorType.MODERATE);
        assertEquals(TradingStrategy.TREND_FOLLOWING, strategy);
    }
    
    @Test
    @DisplayName("적극 투자형 전략 검증")
    void testAggressiveStrategy() {
        TradingStrategy strategy = classifier.getStrategy(InvestorType.AGGRESSIVE);
        assertEquals(TradingStrategy.SWING_TRADING, strategy);
    }
    
    @Test
    @DisplayName("전문 투기형 전략 검증")
    void testSpeculativeStrategy() {
        TradingStrategy strategy = classifier.getStrategy(InvestorType.SPECULATIVE);
        assertEquals(TradingStrategy.VOLATILITY_BREAKOUT, strategy);
    }
    
    @Test
    @DisplayName("전체 프로필 생성 테스트")
    void testCreateProfile() {
        // 안정 추구형 프로필
        InvestorTypeClassifier.ProfileResult profile = classifier.createProfile(15);
        assertNotNull(profile);
        assertEquals(InvestorType.CONSERVATIVE, profile.getInvestorType());
        assertNotNull(profile.getRiskSettings());
        assertEquals(TradingStrategy.SPOT_DCA, profile.getTradingStrategy());
        
        // 전문 투기형 프로필
        profile = classifier.createProfile(45);
        assertNotNull(profile);
        assertEquals(InvestorType.SPECULATIVE, profile.getInvestorType());
        assertNotNull(profile.getRiskSettings());
        assertEquals(TradingStrategy.VOLATILITY_BREAKOUT, profile.getTradingStrategy());
    }
}

