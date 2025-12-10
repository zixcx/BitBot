package com.bitbot.client.service.analysis;

import com.bitbot.client.model.Candle;

import java.util.ArrayList;
import java.util.List;

/**
 * Technical Indicators Calculator
 * Implements common technical analysis indicators
 * 
 * Indicators:
 * - RSI (Relative Strength Index)
 * - MACD (Moving Average Convergence Divergence)
 * - Bollinger Bands
 * - Moving Averages (SMA, EMA)
 */
public class TechnicalIndicators {

    /**
     * Calculate RSI (Relative Strength Index)
     * 
     * @param candles List of candles (sorted chronologically)
     * @param period RSI period (typically 14)
     * @return RSI value (0-100)
     */
    public static double calculateRSI(List<Candle> candles, int period) {
        if (candles.size() < period + 1) {
            return 50.0; // Neutral if not enough data
        }
        
        double gainSum = 0;
        double lossSum = 0;
        
        // Calculate initial average gain and loss
        for (int i = candles.size() - period; i < candles.size(); i++) {
            double change = candles.get(i).close() - candles.get(i - 1).close();
            
            if (change > 0) {
                gainSum += change;
            } else {
                lossSum += Math.abs(change);
            }
        }
        
        if (lossSum == 0) {
            return 100.0; // All gains, maximum RSI
        }
        
        double avgGain = gainSum / period;
        double avgLoss = lossSum / period;
        
        double rs = avgGain / avgLoss;
        double rsi = 100 - (100 / (1 + rs));
        
        return Math.round(rsi * 100.0) / 100.0;
    }

    /**
     * Calculate MACD (Moving Average Convergence Divergence)
     * 
     * @param candles List of candles
     * @param fastPeriod Fast EMA period (typically 12)
     * @param slowPeriod Slow EMA period (typically 26)
     * @param signalPeriod Signal line period (typically 9)
     * @return MACD result {macdLine, signalLine, histogram}
     */
    public static MACDResult calculateMACD(List<Candle> candles, int fastPeriod, int slowPeriod, int signalPeriod) {
        if (candles.size() < slowPeriod + signalPeriod) {
            return new MACDResult(0, 0, 0, "NEUTRAL");
        }
        
        // Extract close prices
        double[] prices = candles.stream()
            .mapToDouble(Candle::close)
            .toArray();
        
        // Calculate EMAs
        double fastEMA = calculateEMA(prices, fastPeriod);
        double slowEMA = calculateEMA(prices, slowPeriod);
        
        // MACD line
        double macdLine = fastEMA - slowEMA;
        
        // Signal line (EMA of MACD)
        // Simplified: using SMA for signal
        double signalLine = macdLine; // TODO: Calculate proper EMA of MACD
        
        // Histogram
        double histogram = macdLine - signalLine;
        
        // Status
        String status = macdLine > signalLine ? "GOLDEN_CROSS" : 
                       macdLine < signalLine ? "DEATH_CROSS" : "NEUTRAL";
        
        return new MACDResult(
            Math.round(macdLine * 100.0) / 100.0,
            Math.round(signalLine * 100.0) / 100.0,
            Math.round(histogram * 100.0) / 100.0,
            status
        );
    }

    /**
     * Calculate Bollinger Bands
     * 
     * @param candles List of candles
     * @param period Period (typically 20)
     * @param stdDevMultiplier Standard deviation multiplier (typically 2)
     * @return Bollinger Bands result
     */
    public static BollingerBandsResult calculateBollingerBands(List<Candle> candles, int period, double stdDevMultiplier) {
        if (candles.size() < period) {
            return new BollingerBandsResult(0, 0, 0, 0.5);
        }
        
        // Get recent prices
        List<Double> recentPrices = new ArrayList<>();
        for (int i = candles.size() - period; i < candles.size(); i++) {
            recentPrices.add(candles.get(i).close());
        }
        
        // Calculate SMA (middle band)
        double sma = recentPrices.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0);
        
        // Calculate standard deviation
        double variance = recentPrices.stream()
            .mapToDouble(price -> Math.pow(price - sma, 2))
            .average()
            .orElse(0);
        double stdDev = Math.sqrt(variance);
        
        // Calculate bands
        double upperBand = sma + (stdDevMultiplier * stdDev);
        double lowerBand = sma - (stdDevMultiplier * stdDev);
        
        // Calculate %B (position within bands)
        double currentPrice = candles.get(candles.size() - 1).close();
        double percentB = (currentPrice - lowerBand) / (upperBand - lowerBand);
        
        return new BollingerBandsResult(
            Math.round(upperBand * 100.0) / 100.0,
            Math.round(sma * 100.0) / 100.0,
            Math.round(lowerBand * 100.0) / 100.0,
            Math.round(percentB * 1000.0) / 1000.0
        );
    }

    /**
     * Calculate Simple Moving Average (SMA)
     * 
     * @param candles List of candles
     * @param period Period
     * @return SMA value
     */
    public static double calculateSMA(List<Candle> candles, int period) {
        if (candles.size() < period) {
            return 0;
        }
        
        double sum = 0;
        for (int i = candles.size() - period; i < candles.size(); i++) {
            sum += candles.get(i).close();
        }
        
        return Math.round((sum / period) * 100.0) / 100.0;
    }

    /**
     * Calculate Exponential Moving Average (EMA)
     * 
     * @param prices Array of prices
     * @param period Period
     * @return EMA value
     */
    public static double calculateEMA(double[] prices, int period) {
        if (prices.length < period) {
            return 0;
        }
        
        double multiplier = 2.0 / (period + 1);
        
        // Calculate initial SMA as starting point
        double sum = 0;
        for (int i = 0; i < period; i++) {
            sum += prices[i];
        }
        double ema = sum / period;
        
        // Calculate EMA for remaining prices
        for (int i = period; i < prices.length; i++) {
            ema = (prices[i] - ema) * multiplier + ema;
        }
        
        return Math.round(ema * 100.0) / 100.0;
    }

    /**
     * Determine trend direction based on moving averages
     * 
     * @param candles List of candles
     * @return Trend direction (UP_TREND, DOWN_TREND, SIDEWAYS)
     */
    public static String determineTrend(List<Candle> candles) {
        if (candles.size() < 60) {
            return "UNKNOWN";
        }
        
        double ma20 = calculateSMA(candles, 20);
        double ma60 = calculateSMA(candles, 60);
        
        if (ma20 > ma60 * 1.01) { // 1% threshold
            return "UP_TREND";
        } else if (ma20 < ma60 * 0.99) {
            return "DOWN_TREND";
        } else {
            return "SIDEWAYS";
        }
    }

    /**
     * MACD Result Container
     */
    public record MACDResult(
        double value,
        double signal,
        double histogram,
        String status
    ) {}

    /**
     * Bollinger Bands Result Container
     */
    public record BollingerBandsResult(
        double upper,
        double middle,
        double lower,
        double percentB
    ) {}
}


