package com.bitbot.models;

/**
 * 손절/익절 후 대응 전략
 */
public enum PostAction {
    HOLD("관망", "시장 상황을 지켜보며 다음 기회를 기다림"),
    WAIT_REENTRY("재진입 대기", "더 좋은 진입 기회를 기다림 (더 낮은 가격 또는 더 좋은 신호)"),
    REVERSE_POSITION("반대 포지션", "추세 전환에 따라 반대 포지션 진입 (롱→숏 또는 숏→롱)"),
    QUICK_REENTRY("빠른 재진입", "즉시 재진입 기회를 모니터링하고 조건 만족 시 진입");
    
    private final String koreanName;
    private final String description;
    
    PostAction(String koreanName, String description) {
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

