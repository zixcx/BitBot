package com.bitbot.client.model;

import java.time.LocalDateTime;

/**
 * Candle (OHLCV) Data Model
 * Represents a single candlestick with Open, High, Low, Close, Volume
 */
public record Candle(
    LocalDateTime timestamp,
    double open,
    double high,
    double low,
    double close,
    double volume
) {
    /**
     * Check if this is a bullish candle (close > open)
     */
    public boolean isBullish() {
        return close > open;
    }

    /**
     * Get the body size (absolute difference between open and close)
     */
    public double getBodySize() {
        return Math.abs(close - open);
    }

    /**
     * Get the upper wick size
     */
    public double getUpperWick() {
        return high - Math.max(open, close);
    }

    /**
     * Get the lower wick size
     */
    public double getLowerWick() {
        return Math.min(open, close) - low;
    }

    /**
     * Get the total range (high - low)
     */
    public double getRange() {
        return high - low;
    }
}


