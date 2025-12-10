package com.bitbot.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Binance Kline (Candlestick) Response
 * API: /api/v3/klines
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BinanceKline {
    
    private long openTime;
    private String open;
    private String high;
    private String low;
    private String close;
    private String volume;
    private long closeTime;
    private String quoteAssetVolume;
    private int numberOfTrades;
    private String takerBuyBaseAssetVolume;
    private String takerBuyQuoteAssetVolume;
    
    // Constructor for array deserialization
    public BinanceKline() {}
    
    public BinanceKline(Object[] data) {
        this.openTime = ((Number) data[0]).longValue();
        this.open = (String) data[1];
        this.high = (String) data[2];
        this.low = (String) data[3];
        this.close = (String) data[4];
        this.volume = (String) data[5];
        this.closeTime = ((Number) data[6]).longValue();
        this.quoteAssetVolume = (String) data[7];
        this.numberOfTrades = ((Number) data[8]).intValue();
        this.takerBuyBaseAssetVolume = (String) data[9];
        this.takerBuyQuoteAssetVolume = (String) data[10];
    }
    
    // Getters and Setters
    public long getOpenTime() { return openTime; }
    public void setOpenTime(long openTime) { this.openTime = openTime; }
    
    public String getOpen() { return open; }
    public void setOpen(String open) { this.open = open; }
    
    public String getHigh() { return high; }
    public void setHigh(String high) { this.high = high; }
    
    public String getLow() { return low; }
    public void setLow(String low) { this.low = low; }
    
    public String getClose() { return close; }
    public void setClose(String close) { this.close = close; }
    
    public String getVolume() { return volume; }
    public void setVolume(String volume) { this.volume = volume; }
    
    public long getCloseTime() { return closeTime; }
    public void setCloseTime(long closeTime) { this.closeTime = closeTime; }
    
    public String getQuoteAssetVolume() { return quoteAssetVolume; }
    public void setQuoteAssetVolume(String quoteAssetVolume) { this.quoteAssetVolume = quoteAssetVolume; }
    
    public int getNumberOfTrades() { return numberOfTrades; }
    public void setNumberOfTrades(int numberOfTrades) { this.numberOfTrades = numberOfTrades; }
    
    public String getTakerBuyBaseAssetVolume() { return takerBuyBaseAssetVolume; }
    public void setTakerBuyBaseAssetVolume(String takerBuyBaseAssetVolume) { 
        this.takerBuyBaseAssetVolume = takerBuyBaseAssetVolume; 
    }
    
    public String getTakerBuyQuoteAssetVolume() { return takerBuyQuoteAssetVolume; }
    public void setTakerBuyQuoteAssetVolume(String takerBuyQuoteAssetVolume) { 
        this.takerBuyQuoteAssetVolume = takerBuyQuoteAssetVolume; 
    }
}


