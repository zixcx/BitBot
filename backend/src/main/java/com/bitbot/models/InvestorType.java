package com.bitbot.models;

/**
 * 투자자 성향 유형
 */
public enum InvestorType {
    CONSERVATIVE("안정 추구형", 12, 20),
    MODERATE("위험 중립형", 21, 29),
    AGGRESSIVE("적극 투자형", 30, 39),
    SPECULATIVE("전문 투기형", 40, 48);
    
    private final String koreanName;
    private final int minScore;
    private final int maxScore;
    
    InvestorType(String koreanName, int minScore, int maxScore) {
        this.koreanName = koreanName;
        this.minScore = minScore;
        this.maxScore = maxScore;
    }
    
    public String getKoreanName() {
        return koreanName;
    }
    
    public int getMinScore() {
        return minScore;
    }
    
    public int getMaxScore() {
        return maxScore;
    }
    
    public static InvestorType fromScore(int score) {
        for (InvestorType type : values()) {
            if (score >= type.minScore && score <= type.maxScore) {
                return type;
            }
        }
        // 기본값
        return score < 21 ? CONSERVATIVE : SPECULATIVE;
    }
}

