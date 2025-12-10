package com.bitbot.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketStatsDto {
    private String symbol;
    private Double priceChange;
    private Double priceChangePercent;
    private Double highPrice;
    private Double lowPrice;
    private Double volume;

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public Double getPriceChange() { return priceChange; }
    public void setPriceChange(Double priceChange) { this.priceChange = priceChange; }

    public Double getPriceChangePercent() { return priceChangePercent; }
    public void setPriceChangePercent(Double priceChangePercent) { this.priceChangePercent = priceChangePercent; }

    public Double getHighPrice() { return highPrice; }
    public void setHighPrice(Double highPrice) { this.highPrice = highPrice; }

    public Double getLowPrice() { return lowPrice; }
    public void setLowPrice(Double lowPrice) { this.lowPrice = lowPrice; }

    public Double getVolume() { return volume; }
    public void setVolume(Double volume) { this.volume = volume; }
}
