package com.bitbot.client.model;

/**
 * Trading Decision from AI
 * Represents the AI's analysis and recommended action
 */
public class TradeDecision {
    
    private ActionType action;          // BUY, SELL, HOLD
    private double confidence;          // 0.0 - 1.0
    private String briefReason;         // One-line summary
    private String fullReason;          // Detailed reasoning
    private Double targetPrice;         // Recommended entry/exit price
    private Double stopLoss;            // Recommended stop loss
    private Double takeProfit;          // Recommended take profit
    
    public enum ActionType {
        BUY,
        SELL,
        HOLD
    }

    public TradeDecision() {}

    public TradeDecision(ActionType action, double confidence, String briefReason, String fullReason) {
        this.action = action;
        this.confidence = confidence;
        this.briefReason = briefReason;
        this.fullReason = fullReason;
    }

    // Getters and Setters
    public ActionType getAction() {
        return action;
    }

    public void setAction(ActionType action) {
        this.action = action;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getBriefReason() {
        return briefReason;
    }

    public void setBriefReason(String briefReason) {
        this.briefReason = briefReason;
    }

    public String getFullReason() {
        return fullReason;
    }

    public void setFullReason(String fullReason) {
        this.fullReason = fullReason;
    }

    public Double getTargetPrice() {
        return targetPrice;
    }

    public void setTargetPrice(Double targetPrice) {
        this.targetPrice = targetPrice;
    }

    public Double getStopLoss() {
        return stopLoss;
    }

    public void setStopLoss(Double stopLoss) {
        this.stopLoss = stopLoss;
    }

    public Double getTakeProfit() {
        return takeProfit;
    }

    public void setTakeProfit(Double takeProfit) {
        this.takeProfit = takeProfit;
    }

    /**
     * Get emoji icon for action
     */
    public String getActionIcon() {
        return switch (action) {
            case BUY -> "🟢";
            case SELL -> "🔴";
            case HOLD -> "🟡";
        };
    }

    /**
     * Get confidence level description
     */
    public String getConfidenceLevel() {
        if (confidence >= 0.8) return "HIGH";
        if (confidence >= 0.5) return "MEDIUM";
        return "LOW";
    }

    @Override
    public String toString() {
        return String.format("%s %s (%.0f%% confidence): %s",
            getActionIcon(), action, confidence * 100, briefReason);
    }
}


