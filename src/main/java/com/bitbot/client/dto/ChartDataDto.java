package com.bitbot.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChartDataDto {
    private LocalDateTime timestamp;
    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private Double volume;
    
    // Technical indicators included in response
    private Double rsi;
    private Double macd;
    private Double maShort;
    private Double maLong;
    private Double bollingerUpper;
    private Double bollingerMiddle;
    private Double bollingerLower;

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Double getOpen() { return open; }
    public void setOpen(Double open) { this.open = open; }

    public Double getHigh() { return high; }
    public void setHigh(Double high) { this.high = high; }

    public Double getLow() { return low; }
    public void setLow(Double low) { this.low = low; }

    public Double getClose() { return close; }
    public void setClose(Double close) { this.close = close; }

    public Double getVolume() { return volume; }
    public void setVolume(Double volume) { this.volume = volume; }

    public Double getRsi() { return rsi; }
    public void setRsi(Double rsi) { this.rsi = rsi; }

    public Double getMacd() { return macd; }
    public void setMacd(Double macd) { this.macd = macd; }

    public Double getMaShort() { return maShort; }
    public void setMaShort(Double maShort) { this.maShort = maShort; }

    public Double getMaLong() { return maLong; }
    public void setMaLong(Double maLong) { this.maLong = maLong; }

    public Double getBollingerUpper() { return bollingerUpper; }
    public void setBollingerUpper(Double bollingerUpper) { this.bollingerUpper = bollingerUpper; }

    public Double getBollingerMiddle() { return bollingerMiddle; }
    public void setBollingerMiddle(Double bollingerMiddle) { this.bollingerMiddle = bollingerMiddle; }

    public Double getBollingerLower() { return bollingerLower; }
    public void setBollingerLower(Double bollingerLower) { this.bollingerLower = bollingerLower; }
}
