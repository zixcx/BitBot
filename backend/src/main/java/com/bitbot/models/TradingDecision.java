package com.bitbot.models;

import java.time.LocalDateTime;

/**
 * 거래 결정을 나타내는 모델
 */
public class TradingDecision {
    
    public enum Decision {
        STRONG_BUY,
        BUY,
        HOLD,
        SELL,
        STRONG_SELL
    }
    
    private String agentName;
    private Decision decision;
    private double confidence;  // 0.0 ~ 1.0
    private String reason;
    private LocalDateTime timestamp;
    
    public TradingDecision(String agentName, Decision decision, double confidence, String reason) {
        this.agentName = agentName;
        this.decision = decision;
        this.confidence = confidence;
        this.reason = reason;
        this.timestamp = LocalDateTime.now();
    }
    
    public String getAgentName() {
        return agentName;
    }
    
    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }
    
    public Decision getDecision() {
        return decision;
    }
    
    public void setDecision(Decision decision) {
        this.decision = decision;
    }
    
    public double getConfidence() {
        return confidence;
    }
    
    public void setConfidence(double confidence) {
        this.confidence = Math.max(0.0, Math.min(1.0, confidence));
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s (신뢰도: %.2f) - %s", 
                agentName, decision, confidence, reason);
    }
}


