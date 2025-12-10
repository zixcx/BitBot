package com.bitbot.models;

/**
 * 거래 전략 유형
 */
public enum TradingStrategy {
    SPOT_DCA("현물 분할 매수", "RSI 과매도 구간에서만 진입, 하락 시 피라미딩 매수"),
    TREND_FOLLOWING("추세 추종", "이동평균선 골든크로스, MACD 0선 돌파 시 매수"),
    SWING_TRADING("스윙 트레이딩", "볼린저밴드 상/하단 매매, 역추세 전략 활용"),
    VOLATILITY_BREAKOUT("변동성 돌파", "당일 변동폭 돌파 시 추격 매수, 양방향 매매");
    
    private final String koreanName;
    private final String description;
    
    TradingStrategy(String koreanName, String description) {
        this.koreanName = koreanName;
        this.description = description;
    }
    
    public String getKoreanName() {
        return koreanName;
    }
    
    public String getDescription() {
        return description;
    }
}

