package com.bitbot.client.service.analysis;

import com.bitbot.client.model.Candle;
import com.bitbot.client.model.MarketAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Market Analysis Service
 * Performs comprehensive technical analysis on market data
 */
public class MarketAnalysisService {
    
    private static final Logger logger = LoggerFactory.getLogger(MarketAnalysisService.class);
    
    // Indicator parameters
    private static final int RSI_PERIOD = 14;
    private static final int MACD_FAST = 12;
    private static final int MACD_SLOW = 26;
    private static final int MACD_SIGNAL = 9;
    private static final int BB_PERIOD = 20;
    private static final double BB_STD_DEV = 2.0;

    /**
     * Perform full market analysis
     * 
     * @param candles Recent candle data (should have at least 120 candles for reliable analysis)
     * @param currentPrice Current market price
     * @param priceChangePercent 24h price change percentage
     * @return MarketAnalysis object with all indicators
     */
    public MarketAnalysis analyze(List<Candle> candles, double currentPrice, double priceChangePercent) {
        logger.debug("Performing market analysis with {} candles", candles.size());
        
        MarketAnalysis analysis = new MarketAnalysis();
        
        // Basic market info
        analysis.setCurrentPrice(currentPrice);
        analysis.setPriceChangePercent(priceChangePercent);
        analysis.setRecentCandles(candles.subList(Math.max(0, candles.size() - 50), candles.size()));
        
        // Calculate indicators
        try {
            // RSI
            double rsi = TechnicalIndicators.calculateRSI(candles, RSI_PERIOD);
            analysis.setRsi14(rsi);
            logger.debug("RSI(14): {}", rsi);
            
            // MACD
            TechnicalIndicators.MACDResult macd = TechnicalIndicators.calculateMACD(
                candles, MACD_FAST, MACD_SLOW, MACD_SIGNAL
            );
            analysis.setMacd(macd);
            logger.debug("MACD: {} | Signal: {} | Status: {}", 
                macd.value(), macd.signal(), macd.status());
            
            // Bollinger Bands
            TechnicalIndicators.BollingerBandsResult bb = TechnicalIndicators.calculateBollingerBands(
                candles, BB_PERIOD, BB_STD_DEV
            );
            analysis.setBollingerBands(bb);
            logger.debug("BB: Upper={}, Middle={}, Lower={}, %B={}", 
                bb.upper(), bb.middle(), bb.lower(), bb.percentB());
            
            // Moving Averages
            double ma20 = TechnicalIndicators.calculateSMA(candles, 20);
            double ma60 = TechnicalIndicators.calculateSMA(candles, 60);
            double ma120 = TechnicalIndicators.calculateSMA(candles, 120);
            analysis.setMa20(ma20);
            analysis.setMa60(ma60);
            analysis.setMa120(ma120);
            logger.debug("MA20: {}, MA60: {}, MA120: {}", ma20, ma60, ma120);
            
            // Trend determination
            String trend = TechnicalIndicators.determineTrend(candles);
            analysis.setTrendDirection(trend);
            logger.debug("Trend: {}", trend);
            
            // TODO: Fear & Greed Index (requires external API)
            analysis.setFearGreedIndex(50); // Default neutral
            
            // TODO: Order book analysis
            analysis.setOrderBookStrength("BALANCED");
            
        } catch (Exception e) {
            logger.error("Error during market analysis", e);
        }
        
        logger.info("Market analysis complete: {}", analysis.getMarketConditionSummary());
        return analysis;
    }

    /**
     * Quick analysis for real-time updates (fewer indicators)
     */
    public MarketAnalysis quickAnalyze(List<Candle> candles, double currentPrice, double priceChangePercent) {
        MarketAnalysis analysis = new MarketAnalysis();
        analysis.setCurrentPrice(currentPrice);
        analysis.setPriceChangePercent(priceChangePercent);
        
        // Calculate only essential indicators
        double rsi = TechnicalIndicators.calculateRSI(candles, RSI_PERIOD);
        analysis.setRsi14(rsi);
        
        String trend = TechnicalIndicators.determineTrend(candles);
        analysis.setTrendDirection(trend);
        
        return analysis;
    }

    /**
     * Check for specific trading signals
     */
    public String detectTradingSignal(MarketAnalysis analysis) {
        StringBuilder signals = new StringBuilder();
        
        // RSI signals
        if (analysis.getRsi14() < 30) {
            signals.append("RSI_OVERSOLD ");
        } else if (analysis.getRsi14() > 70) {
            signals.append("RSI_OVERBOUGHT ");
        }
        
        // MACD signals
        if (analysis.getMacd() != null) {
            if ("GOLDEN_CROSS".equals(analysis.getMacd().status())) {
                signals.append("MACD_BULLISH ");
            } else if ("DEATH_CROSS".equals(analysis.getMacd().status())) {
                signals.append("MACD_BEARISH ");
            }
        }
        
        // Bollinger Bands signals
        if (analysis.getBollingerBands() != null) {
            double percentB = analysis.getBollingerBands().percentB();
            if (percentB < 0) {
                signals.append("BB_BELOW_LOWER ");
            } else if (percentB > 1) {
                signals.append("BB_ABOVE_UPPER ");
            }
        }
        
        return signals.length() > 0 ? signals.toString().trim() : "NO_SIGNAL";
    }
}


