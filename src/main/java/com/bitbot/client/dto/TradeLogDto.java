package com.bitbot.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Trade Log DTO
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TradeLogDto {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("userId")
    private Long userId;
    
    @JsonProperty("symbol")
    private String symbol;
    
    @JsonProperty("actionType")
    private String actionType; // BUY, SELL, HOLD
    
    @JsonProperty("executedPrice")
    private Double executedPrice;
    
    @JsonProperty("executedQty")
    private Double executedQty;
    
    @JsonProperty("confidenceScore")
    private Double confidenceScore;
    
    @JsonProperty("reason")
    private String reason;
    
    @JsonProperty("fullReason")
    private String fullReason;
    
    @JsonProperty("createdAt")
    private String createdAt;

    public TradeLogDto() {}

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

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public Double getExecutedPrice() {
        return executedPrice;
    }

    public void setExecutedPrice(Double executedPrice) {
        this.executedPrice = executedPrice;
    }

    public Double getExecutedQty() {
        return executedQty;
    }

    public void setExecutedQty(Double executedQty) {
        this.executedQty = executedQty;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getFullReason() {
        return fullReason;
    }

    public void setFullReason(String fullReason) {
        this.fullReason = fullReason;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}

