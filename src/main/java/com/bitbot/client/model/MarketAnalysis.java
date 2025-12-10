package com.bitbot.client.model;

import com.bitbot.client.service.analysis.TechnicalIndicators;

import java.util.List;

/**
 * Market Analysis Result
 * Contains all technical indicators and market context
 * This is passed to LLM for decision making
 */
public class MarketAnalysis {
    
    private double currentPrice;
    private double priceChangePercent;
    private String trendDirection;
    
    // Technical Indicators
    private double rsi14;
    private TechnicalIndicators.MACDResult macd;
    private TechnicalIndicators.BollingerBandsResult bollingerBands;
    private double ma20;
    private double ma60;
    private double ma120;
    
    // Market Context
    private int fearGreedIndex;
    private String orderBookStrength; // "STRONG_BID", "STRONG_ASK", "BALANCED"
    
    // Candle Data
    private List<Candle> recentCandles;
    
    public MarketAnalysis() {}

    // Getters and Setters
    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public double getPriceChangePercent() {
        return priceChangePercent;
    }

    public void setPriceChangePercent(double priceChangePercent) {
        this.priceChangePercent = priceChangePercent;
    }

    public String getTrendDirection() {
        return trendDirection;
    }

    public void setTrendDirection(String trendDirection) {
        this.trendDirection = trendDirection;
    }

    public double getRsi14() {
        return rsi14;
    }

    public void setRsi14(double rsi14) {
        this.rsi14 = rsi14;
    }

    public TechnicalIndicators.MACDResult getMacd() {
        return macd;
    }

    public void setMacd(TechnicalIndicators.MACDResult macd) {
        this.macd = macd;
    }

    public TechnicalIndicators.BollingerBandsResult getBollingerBands() {
        return bollingerBands;
    }

    public void setBollingerBands(TechnicalIndicators.BollingerBandsResult bollingerBands) {
        this.bollingerBands = bollingerBands;
    }

    public double getMa20() {
        return ma20;
    }

    public void setMa20(double ma20) {
        this.ma20 = ma20;
    }

    public double getMa60() {
        return ma60;
    }

    public void setMa60(double ma60) {
        this.ma60 = ma60;
    }

    public double getMa120() {
        return ma120;
    }

    public void setMa120(double ma120) {
        this.ma120 = ma120;
    }

    public int getFearGreedIndex() {
        return fearGreedIndex;
    }

    public void setFearGreedIndex(int fearGreedIndex) {
        this.fearGreedIndex = fearGreedIndex;
    }

    public String getOrderBookStrength() {
        return orderBookStrength;
    }

    public void setOrderBookStrength(String orderBookStrength) {
        this.orderBookStrength = orderBookStrength;
    }

    public List<Candle> getRecentCandles() {
        return recentCandles;
    }

    public void setRecentCandles(List<Candle> recentCandles) {
        this.recentCandles = recentCandles;
    }

    /**
     * Quick assessment of market conditions
     */
    public String getMarketConditionSummary() {
        StringBuilder summary = new StringBuilder();
        
        // RSI assessment
        if (rsi14 < 30) {
            summary.append("RSI Oversold");
        } else if (rsi14 > 70) {
            summary.append("RSI Overbought");
        } else {
            summary.append("RSI Neutral");
        }
        
        summary.append(" | ");
        
        // Trend
        summary.append(trendDirection);
        
        summary.append(" | ");
        
        // MACD
        if (macd != null) {
            summary.append(macd.status());
        }
        
        return summary.toString();
    }

    @Override
    public String toString() {
        return String.format(
            "MarketAnalysis{price=$%.2f, change=%.2f%%, trend=%s, rsi=%.2f, macd=%s}",
            currentPrice, priceChangePercent, trendDirection, rsi14,
            macd != null ? macd.status() : "N/A"
        );
    }
}


