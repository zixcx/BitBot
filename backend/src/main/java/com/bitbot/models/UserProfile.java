package com.bitbot.models;

import java.time.LocalDateTime;

/**
 * 사용자 프로필 (투자 성향 정보)
 */
public class UserProfile {
    
    private Long id;
    private Integer userId;
    private InvestorType investorType;
    private int totalScore;  // 12~48
    private RiskSettings riskSettings;
    private TradingStrategy tradingStrategy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public UserProfile() {
    }
    
    public UserProfile(Integer userId, InvestorType investorType, int totalScore,
                      RiskSettings riskSettings, TradingStrategy tradingStrategy) {
        this.userId = userId;
        this.investorType = investorType;
        this.totalScore = totalScore;
        this.riskSettings = riskSettings;
        this.tradingStrategy = tradingStrategy;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public InvestorType getInvestorType() {
        return investorType;
    }
    
    public void setInvestorType(InvestorType investorType) {
        this.investorType = investorType;
    }
    
    public int getTotalScore() {
        return totalScore;
    }
    
    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }
    
    public RiskSettings getRiskSettings() {
        return riskSettings;
    }
    
    public void setRiskSettings(RiskSettings riskSettings) {
        this.riskSettings = riskSettings;
    }
    
    public TradingStrategy getTradingStrategy() {
        return tradingStrategy;
    }
    
    public void setTradingStrategy(TradingStrategy tradingStrategy) {
        this.tradingStrategy = tradingStrategy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return String.format("UserProfile[%s (점수: %d), 전략: %s, %s]",
                investorType.getKoreanName(), totalScore, 
                tradingStrategy.getKoreanName(), riskSettings);
    }
}

