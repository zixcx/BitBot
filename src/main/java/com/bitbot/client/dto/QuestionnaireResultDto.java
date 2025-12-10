package com.bitbot.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Questionnaire Result DTO
 */
public class QuestionnaireResultDto {
    
    @JsonProperty("profile")
    private UserProfileDto profile;
    
    @JsonProperty("investorType")
    private String investorType;
    
    @JsonProperty("tradingStrategy")
    private String tradingStrategy;
    
    @JsonProperty("totalScore")
    private Integer totalScore;

    // Getters and Setters
    public UserProfileDto getProfile() {
        return profile;
    }

    public void setProfile(UserProfileDto profile) {
        this.profile = profile;
    }

    public String getInvestorType() {
        return investorType;
    }

    public void setInvestorType(String investorType) {
        this.investorType = investorType;
    }

    public String getTradingStrategy() {
        return tradingStrategy;
    }

    public void setTradingStrategy(String tradingStrategy) {
        this.tradingStrategy = tradingStrategy;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }
}

