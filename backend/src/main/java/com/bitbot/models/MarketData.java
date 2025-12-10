package com.bitbot.models;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 시장 데이터를 담는 모델 (OHLCV + 기술 지표)
 */
public class MarketData {
    
    private LocalDateTime timestamp;
    private double open;
    private double high;
    private double low;
    private double close;
    private double volume;
    
    // 추가 데이터
    private double quoteVolume;     // 거래 금액 (USD)
    private int tradeCount;         // 거래 횟수
    private double takerBuyVolume;  // 테이커 매수량
    private double takerBuyQuote;   // 테이커 매수 금액
    
    // 기술 지표
    private Double rsi;
    private Double macd;
    private Double macdSignal;
    private Double maShort;
    private Double maLong;
    private Double bollingerUpper;
    private Double bollingerMiddle;
    private Double bollingerLower;
    
    public MarketData() {
    }
    
    public MarketData(LocalDateTime timestamp, double open, double high, 
                     double low, double close, double volume) {
        this.timestamp = timestamp;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }
    
    // Getters and Setters
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public double getOpen() {
        return open;
    }
    
    public void setOpen(double open) {
        this.open = open;
    }
    
    public double getHigh() {
        return high;
    }
    
    public void setHigh(double high) {
        this.high = high;
    }
    
    public double getLow() {
        return low;
    }
    
    public void setLow(double low) {
        this.low = low;
    }
    
    public double getClose() {
        return close;
    }
    
    public void setClose(double close) {
        this.close = close;
    }
    
    public double getVolume() {
        return volume;
    }
    
    public void setVolume(double volume) {
        this.volume = volume;
    }
    
    public double getQuoteVolume() {
        return quoteVolume;
    }
    
    public void setQuoteVolume(double quoteVolume) {
        this.quoteVolume = quoteVolume;
    }
    
    public int getTradeCount() {
        return tradeCount;
    }
    
    public void setTradeCount(int tradeCount) {
        this.tradeCount = tradeCount;
    }
    
    public double getTakerBuyVolume() {
        return takerBuyVolume;
    }
    
    public void setTakerBuyVolume(double takerBuyVolume) {
        this.takerBuyVolume = takerBuyVolume;
    }
    
    public double getTakerBuyQuote() {
        return takerBuyQuote;
    }
    
    public void setTakerBuyQuote(double takerBuyQuote) {
        this.takerBuyQuote = takerBuyQuote;
    }
    
    public Double getRsi() {
        return rsi;
    }
    
    public void setRsi(Double rsi) {
        this.rsi = rsi;
    }
    
    public Double getMacd() {
        return macd;
    }
    
    public void setMacd(Double macd) {
        this.macd = macd;
    }
    
    public Double getMacdSignal() {
        return macdSignal;
    }
    
    public void setMacdSignal(Double macdSignal) {
        this.macdSignal = macdSignal;
    }
    
    public Double getMaShort() {
        return maShort;
    }
    
    public void setMaShort(Double maShort) {
        this.maShort = maShort;
    }
    
    public Double getMaLong() {
        return maLong;
    }
    
    public void setMaLong(Double maLong) {
        this.maLong = maLong;
    }
    
    public Double getBollingerUpper() {
        return bollingerUpper;
    }
    
    public void setBollingerUpper(Double bollingerUpper) {
        this.bollingerUpper = bollingerUpper;
    }
    
    public Double getBollingerMiddle() {
        return bollingerMiddle;
    }
    
    public void setBollingerMiddle(Double bollingerMiddle) {
        this.bollingerMiddle = bollingerMiddle;
    }
    
    public Double getBollingerLower() {
        return bollingerLower;
    }
    
    public void setBollingerLower(Double bollingerLower) {
        this.bollingerLower = bollingerLower;
    }
    
    @Override
    public String toString() {
        return String.format("MarketData[%s] O:%.2f H:%.2f L:%.2f C:%.2f V:%.2f", 
                timestamp, open, high, low, close, volume);
    }
}


