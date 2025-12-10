package com.bitbot.strategy;

import com.bitbot.models.MarketData;
import com.bitbot.models.TradingDecision;
import com.bitbot.models.TradingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 전략별 거래 실행 로직
 * 전략이 능동적으로 신호를 생성하고, LLM 결정과 결합하여 최종 결정
 */
public class StrategyExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(StrategyExecutor.class);
    
    /**
     * 전략에 따라 능동적으로 신호를 생성하고 LLM 결정과 결합
     * 
     * 동작 방식:
     * 1. 전략이 먼저 신호 생성 (능동적)
     * 2. LLM 결정과 결합
     * 3. 전략 신호가 우선순위를 가짐 (전략 조건 만족 시)
     */
    public TradingDecision applyStrategy(
            TradingDecision llmDecision,
            TradingStrategy strategy,
            List<MarketData> marketData,
            MarketData latest) {
        
        logger.info("[전략 실행기] 전략: {}, LLM 결정: {}", 
                strategy.getKoreanName(), llmDecision.getDecision());
        
        // 1단계: 전략이 먼저 신호 생성 (능동적)
        TradingDecision strategySignal = generateStrategySignal(strategy, marketData, latest);
        
        // 2단계: 전략 신호와 LLM 결정 결합
        TradingDecision result = combineDecisions(strategySignal, llmDecision, strategy);
        
        if (!result.getDecision().equals(llmDecision.getDecision())) {
            logger.info("[전략 실행기] 전략 신호 적용: LLM {} → 최종 {}", 
                    llmDecision.getDecision(), result.getDecision());
        }
        
        return result;
    }
    
    /**
     * 전략별 신호 생성 (능동적)
     * 전략 조건이 만족되면 신호를 생성 (LLM 결정과 무관)
     */
    private TradingDecision generateStrategySignal(
            TradingStrategy strategy,
            List<MarketData> marketData,
            MarketData latest) {
        
        switch (strategy) {
            case SPOT_DCA:
                return generateDCASignal(marketData, latest);
            case TREND_FOLLOWING:
                return generateTrendFollowingSignal(marketData, latest);
            case SWING_TRADING:
                return generateSwingTradingSignal(marketData, latest);
            case VOLATILITY_BREAKOUT:
                return generateVolatilityBreakoutSignal(marketData, latest);
            default:
                return null; // 신호 없음
        }
    }
    
    /**
     * 전략 신호와 LLM 결정 결합
     * 전략 신호가 있으면 우선순위를 가짐
     */
    private TradingDecision combineDecisions(
            TradingDecision strategySignal,
            TradingDecision llmDecision,
            TradingStrategy strategy) {
        
        // 전략 신호가 없으면 LLM 결정 따름
        if (strategySignal == null) {
            return llmDecision;
        }
        
        // 전략 신호가 있으면 우선순위 적용
        TradingDecision.Decision strategyDecision = strategySignal.getDecision();
        TradingDecision.Decision llmDecisionType = llmDecision.getDecision();
        
        // 전략이 강한 신호를 생성한 경우 (STRONG_BUY, STRONG_SELL)
        if (strategyDecision == TradingDecision.Decision.STRONG_BUY ||
            strategyDecision == TradingDecision.Decision.STRONG_SELL) {
            logger.info("[전략 실행기] 전략 강한 신호 우선: {} (LLM: {})", 
                    strategyDecision, llmDecisionType);
            return strategySignal;
        }
        
        // 전략이 매수 신호를 생성한 경우
        if (strategyDecision == TradingDecision.Decision.BUY) {
            // LLM이 매도 권하면 전략 신호 우선 (전략 조건 만족)
            if (llmDecisionType == TradingDecision.Decision.SELL ||
                llmDecisionType == TradingDecision.Decision.STRONG_SELL) {
                logger.info("[전략 실행기] 전략 매수 신호 우선 (LLM 매도 무시)");
                return strategySignal;
            }
            // LLM이 HOLD나 매수 권하면 전략 신호 승인
            return strategySignal;
        }
        
        // 전략이 매도 신호를 생성한 경우
        if (strategyDecision == TradingDecision.Decision.SELL) {
            // LLM이 매수 권하면 전략 신호 우선 (전략 조건 만족)
            if (llmDecisionType == TradingDecision.Decision.BUY ||
                llmDecisionType == TradingDecision.Decision.STRONG_BUY) {
                logger.info("[전략 실행기] 전략 매도 신호 우선 (LLM 매수 무시)");
                return strategySignal;
            }
            // LLM이 HOLD나 매도 권하면 전략 신호 승인
            return strategySignal;
        }
        
        // 전략이 HOLD 신호를 생성한 경우 (조건 미만족)
        // LLM 결정 따름
        return llmDecision;
    }
    
    /**
     * DCA 전략 신호 생성 (능동적)
     * RSI 30 이하에서 강한 매수 신호 생성 (LLM 결정과 무관)
     */
    private TradingDecision generateDCASignal(
            List<MarketData> marketData, 
            MarketData latest) {
        
        if (latest.getRsi() == null) {
            return null; // 신호 없음
        }
        
        double rsi = latest.getRsi();
        
        // RSI 30 이하: 강한 매수 신호 (능동적)
        if (rsi < 30) {
            logger.info("[DCA 전략] RSI {} - 강한 매수 신호 생성", rsi);
            return new TradingDecision(
                "Strategy Executor (DCA)",
                TradingDecision.Decision.STRONG_BUY,
                0.9,
                "DCA 전략: RSI " + String.format("%.2f", rsi) + " (과매도 구간) - 분할 매수 기회"
            );
        }
        
        // RSI 30-40: 약한 매수 신호
        if (rsi < 40) {
            logger.debug("[DCA 전략] RSI {} - 약한 매수 신호", rsi);
            return new TradingDecision(
                "Strategy Executor (DCA)",
                TradingDecision.Decision.BUY,
                0.7,
                "DCA 전략: RSI " + String.format("%.2f", rsi) + " (매수 고려 구간)"
            );
        }
        
        // RSI 40 이상: 매수 신호 없음 (HOLD)
        return new TradingDecision(
            "Strategy Executor (DCA)",
            TradingDecision.Decision.HOLD,
            0.8,
            "DCA 전략: RSI " + String.format("%.2f", rsi) + " (매수 대기 - RSI 30 이하 대기)"
        );
    }
    
    /**
     * 추세 추종 전략 신호 생성 (능동적)
     * 골든크로스 + MACD 양수 시 매수 신호 생성
     */
    private TradingDecision generateTrendFollowingSignal(
            List<MarketData> marketData,
            MarketData latest) {
        
        if (latest.getMaShort() == null || latest.getMaLong() == null || 
            latest.getMacd() == null) {
            return null; // 신호 없음
        }
        
        // 골든크로스 확인 (단기 MA > 장기 MA)
        boolean goldenCross = latest.getMaShort() > latest.getMaLong();
        
        // MACD 0선 돌파 확인
        boolean macdPositive = latest.getMacd() > 0;
        
        // 데드크로스 확인
        boolean deadCross = latest.getMaShort() < latest.getMaLong();
        
        // MACD 음수 확인
        boolean macdNegative = latest.getMacd() < 0;
        
        // 강한 추세 확인: 골든크로스 + MACD 양수
        if (goldenCross && macdPositive) {
            logger.info("[추세 추종 전략] 골든크로스 + MACD 양수 - 강한 매수 신호 생성");
            return new TradingDecision(
                "Strategy Executor (Trend Following)",
                TradingDecision.Decision.STRONG_BUY,
                0.9,
                "추세 추종 전략: 골든크로스 확인 (단기 MA: " + 
                String.format("%.2f", latest.getMaShort()) + 
                " > 장기 MA: " + String.format("%.2f", latest.getMaLong()) + 
                "), MACD 양수 (" + String.format("%.4f", latest.getMacd()) + ")"
            );
        }
        
        // 추세 이탈: 데드크로스 또는 MACD 음수
        if (deadCross || macdNegative) {
            logger.info("[추세 추종 전략] 추세 이탈 확인 - 매도 신호 생성");
            return new TradingDecision(
                "Strategy Executor (Trend Following)",
                TradingDecision.Decision.SELL,
                0.8,
                "추세 추종 전략: 추세 이탈 (데드크로스: " + deadCross + 
                ", MACD 음수: " + macdNegative + ")"
            );
        }
        
        // 추세 미확인: HOLD
        return new TradingDecision(
            "Strategy Executor (Trend Following)",
            TradingDecision.Decision.HOLD,
            0.7,
            "추세 추종 전략: 추세 미확인 (골든크로스: " + goldenCross + 
            ", MACD 양수: " + macdPositive + ")"
        );
    }
    
    /**
     * 스윙 트레이딩 전략 신호 생성 (능동적)
     * 볼린저밴드 하단/상단 근처에서 매수/매도 신호 생성
     */
    private TradingDecision generateSwingTradingSignal(
            List<MarketData> marketData,
            MarketData latest) {
        
        if (latest.getBollingerLower() == null || 
            latest.getBollingerUpper() == null ||
            latest.getBollingerMiddle() == null) {
            return null; // 신호 없음
        }
        
        double currentPrice = latest.getClose();
        double lowerBand = latest.getBollingerLower();
        double upperBand = latest.getBollingerUpper();
        
        // 볼린저밴드 하단 터치 또는 근처 (3% 이내) → 강한 매수 신호
        if (currentPrice <= lowerBand * 1.03) {
            logger.info("[스윙 전략] 볼린저밴드 하단 터치 - 강한 매수 신호 생성");
            return new TradingDecision(
                "Strategy Executor (Swing Trading)",
                TradingDecision.Decision.STRONG_BUY,
                0.9,
                "스윙 전략: 볼린저밴드 하단 터치 (현재: " + 
                String.format("%.2f", currentPrice) + 
                ", 하단: " + String.format("%.2f", lowerBand) + ") - 역추세 매수 기회"
            );
        }
        
        // 볼린저밴드 하단 근처 (5% 이내) → 매수 신호
        if (currentPrice <= lowerBand * 1.05) {
            logger.info("[스윙 전략] 볼린저밴드 하단 근처 - 매수 신호 생성");
            return new TradingDecision(
                "Strategy Executor (Swing Trading)",
                TradingDecision.Decision.BUY,
                0.8,
                "스윙 전략: 볼린저밴드 하단 근처 (현재: " + 
                String.format("%.2f", currentPrice) + 
                ", 하단: " + String.format("%.2f", lowerBand) + ")"
            );
        }
        
        // 볼린저밴드 상단 터치 또는 근처 (3% 이내) → 강한 매도 신호
        if (currentPrice >= upperBand * 0.97) {
            logger.info("[스윙 전략] 볼린저밴드 상단 터치 - 강한 매도 신호 생성");
            return new TradingDecision(
                "Strategy Executor (Swing Trading)",
                TradingDecision.Decision.STRONG_SELL,
                0.9,
                "스윙 전략: 볼린저밴드 상단 터치 (현재: " + 
                String.format("%.2f", currentPrice) + 
                ", 상단: " + String.format("%.2f", upperBand) + ") - 역추세 매도 기회"
            );
        }
        
        // 볼린저밴드 상단 근처 (5% 이내) → 매도 신호
        if (currentPrice >= upperBand * 0.95) {
            logger.info("[스윙 전략] 볼린저밴드 상단 근처 - 매도 신호 생성");
            return new TradingDecision(
                "Strategy Executor (Swing Trading)",
                TradingDecision.Decision.SELL,
                0.8,
                "스윙 전략: 볼린저밴드 상단 근처 (현재: " + 
                String.format("%.2f", currentPrice) + 
                ", 상단: " + String.format("%.2f", upperBand) + ")"
            );
        }
        
        // 중간 구간: HOLD
        return new TradingDecision(
            "Strategy Executor (Swing Trading)",
            TradingDecision.Decision.HOLD,
            0.7,
            "스윙 전략: 볼린저밴드 중간 구간 (현재: " + 
            String.format("%.2f", currentPrice) + 
            ", 하단: " + String.format("%.2f", lowerBand) + 
            ", 상단: " + String.format("%.2f", upperBand) + ")"
        );
    }
    
    /**
     * 변동성 돌파 전략 신호 생성 (능동적)
     * 변동성 돌파 발생 시 강한 신호 생성
     */
    private TradingDecision generateVolatilityBreakoutSignal(
            List<MarketData> marketData,
            MarketData latest) {
        
        if (marketData.size() < 2) {
            return null; // 신호 없음
        }
        
        // 전일 고가/저가 계산
        MarketData previous = marketData.get(marketData.size() - 2);
        double previousHigh = previous.getHigh();
        double previousLow = previous.getLow();
        double volatility = previousHigh - previousLow;
        
        if (volatility <= 0) {
            return null; // 변동성 없음
        }
        
        // 변동성 돌파 기준 (전일 고가 + 변동폭의 50%)
        double breakoutUpper = previousHigh + (volatility * 0.5);
        // 변동성 하향 돌파 기준 (전일 저가 - 변동폭의 50%)
        double breakoutLower = previousLow - (volatility * 0.5);
        
        double currentPrice = latest.getClose();
        
        // 상단 돌파 → 강한 매수 신호 (능동적)
        if (currentPrice > breakoutUpper) {
            logger.info("[변동성 돌파 전략] 상단 돌파 확인 - 강한 매수 신호 생성");
            return new TradingDecision(
                "Strategy Executor (Volatility Breakout)",
                TradingDecision.Decision.STRONG_BUY,
                0.95,
                "변동성 돌파 전략: 상단 돌파 확인 (현재: " + 
                String.format("%.2f", currentPrice) + 
                ", 돌파가: " + String.format("%.2f", breakoutUpper) + 
                ", 변동폭: " + String.format("%.2f", volatility) + ") - 추격 매수"
            );
        }
        
        // 하단 돌파 → 강한 매도 신호 (능동적)
        if (currentPrice < breakoutLower) {
            logger.info("[변동성 돌파 전략] 하단 돌파 확인 - 강한 매도 신호 생성");
            return new TradingDecision(
                "Strategy Executor (Volatility Breakout)",
                TradingDecision.Decision.STRONG_SELL,
                0.95,
                "변동성 돌파 전략: 하단 돌파 확인 (현재: " + 
                String.format("%.2f", currentPrice) + 
                ", 돌파가: " + String.format("%.2f", breakoutLower) + 
                ", 변동폭: " + String.format("%.2f", volatility) + ") - 추격 매도"
            );
        }
        
        // 돌파 없음: HOLD (LLM 결정 따름)
        return new TradingDecision(
            "Strategy Executor (Volatility Breakout)",
            TradingDecision.Decision.HOLD,
            0.6,
            "변동성 돌파 전략: 돌파 없음 (현재: " + 
            String.format("%.2f", currentPrice) + 
            ", 상단: " + String.format("%.2f", breakoutUpper) + 
            ", 하단: " + String.format("%.2f", breakoutLower) + ")"
        );
    }
}

