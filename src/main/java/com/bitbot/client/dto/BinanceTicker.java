package com.bitbot.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Binance Ticker Price Response
 * API: /api/v3/ticker/24hr
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BinanceTicker {
    
    @JsonProperty("symbol")
    private String symbol;
    
    @JsonProperty("priceChange")
    private String priceChange;
    
    @JsonProperty("priceChangePercent")
    private String priceChangePercent;
    
    @JsonProperty("lastPrice")
    private String lastPrice;
    
    @JsonProperty("highPrice")
    private String highPrice;
    
    @JsonProperty("lowPrice")
    private String lowPrice;
    
    @JsonProperty("volume")
    private String volume;
    
    @JsonProperty("quoteVolume")
    private String quoteVolume;
    
    // Getters and Setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public String getPriceChange() { return priceChange; }
    public void setPriceChange(String priceChange) { this.priceChange = priceChange; }
    
    public String getPriceChangePercent() { return priceChangePercent; }
    public void setPriceChangePercent(String priceChangePercent) { 
        this.priceChangePercent = priceChangePercent; 
    }
    
    public String getLastPrice() { return lastPrice; }
    public void setLastPrice(String lastPrice) { this.lastPrice = lastPrice; }
    
    public String getHighPrice() { return highPrice; }
    public void setHighPrice(String highPrice) { this.highPrice = highPrice; }
    
    public String getLowPrice() { return lowPrice; }
    public void setLowPrice(String lowPrice) { this.lowPrice = lowPrice; }
    
    public String getVolume() { return volume; }
    public void setVolume(String volume) { this.volume = volume; }
    
    public String getQuoteVolume() { return quoteVolume; }
    public void setQuoteVolume(String quoteVolume) { this.quoteVolume = quoteVolume; }
    
    public double getLastPriceAsDouble() {
        return Double.parseDouble(lastPrice);
    }
    
    public double getPriceChangePercentAsDouble() {
        return Double.parseDouble(priceChangePercent);
    }
}


