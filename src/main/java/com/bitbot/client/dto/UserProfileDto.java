package com.bitbot.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User Profile DTO
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserProfileDto {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("userId")
    private Long userId;
    
    @JsonProperty("investorType")
    private String investorType;
    
    @JsonProperty("totalScore")
    private Integer totalScore;
    
    @JsonProperty("tradingStrategy")
    private String tradingStrategy;
    
    @JsonProperty("riskSettings")
    private RiskSettings riskSettings;
    
    @JsonProperty("createdAt")
    private String createdAt;
    
    @JsonProperty("updatedAt")
    private String updatedAt;

    public UserProfileDto() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getInvestorType() {
        return investorType;
    }

    public void setInvestorType(String investorType) {
        this.investorType = investorType;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public String getTradingStrategy() {
        return tradingStrategy;
    }

    public void setTradingStrategy(String tradingStrategy) {
        this.tradingStrategy = tradingStrategy;
    }

    public RiskSettings getRiskSettings() {
        return riskSettings;
    }

    public void setRiskSettings(RiskSettings riskSettings) {
        this.riskSettings = riskSettings;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RiskSettings {
        @JsonProperty("leverage_allowed")
        private Boolean leverageAllowed;
        
        @JsonProperty("max_leverage")
        private Integer maxLeverage;
        
        @JsonProperty("stop_loss_percent")
        private Double stopLossPercent;
        
        @JsonProperty("take_profit_percent")
        private Double takeProfitPercent;
        
        @JsonProperty("max_loss_percent")
        private Double maxLossPercent;
        
        @JsonProperty("max_position_percent")
        private Double maxPositionPercent;

        // Getters and Setters
        public Boolean isLeverageAllowed() {
            return leverageAllowed;
        }

        public void setLeverageAllowed(Boolean leverageAllowed) {
            this.leverageAllowed = leverageAllowed;
        }

        public Integer getMaxLeverage() {
            return maxLeverage;
        }

        public void setMaxLeverage(Integer maxLeverage) {
            this.maxLeverage = maxLeverage;
        }

        public Double getStopLossPercent() {
            return stopLossPercent;
        }

        public void setStopLossPercent(Double stopLossPercent) {
            this.stopLossPercent = stopLossPercent;
        }

        public Double getTakeProfitPercent() {
            return takeProfitPercent;
        }

        public void setTakeProfitPercent(Double takeProfitPercent) {
            this.takeProfitPercent = takeProfitPercent;
        }

        public Double getMaxLossPercent() {
            return maxLossPercent;
        }

        public void setMaxLossPercent(Double maxLossPercent) {
            this.maxLossPercent = maxLossPercent;
        }

        public Double getMaxPositionPercent() {
            return maxPositionPercent;
        }

        public void setMaxPositionPercent(Double maxPositionPercent) {
            this.maxPositionPercent = maxPositionPercent;
        }
    }
}
