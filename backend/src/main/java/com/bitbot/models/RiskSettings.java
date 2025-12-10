package com.bitbot.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 리스크 관리 설정
 * 사용자 성향에 따라 자동으로 설정됨
 */
public class RiskSettings {
    
    @JsonProperty("leverage_allowed")
    private boolean leverageAllowed;
    
    @JsonProperty("max_leverage")
    private int maxLeverage;  // 1, 3, 5, 10
    
    @JsonProperty("max_loss_percent")
    private double maxLossPercent;  // -15%, -7%, -5%, -3%
    
    @JsonProperty("max_position_percent")
    private double maxPositionPercent;  // 5%, 20%, 30%, 50%
    
    @JsonProperty("stop_loss_percent")
    private double stopLossPercent;
    
    @JsonProperty("take_profit_percent")
    private double takeProfitPercent;  // 익절 기준 (%)
    
    // 손절/익절 후 대응 전략 (JSON 직렬화 제외 - 전략별로 자동 결정)
    @JsonIgnore
    private PostAction postStopLossAction;  // 손절 후 대응
    @JsonIgnore
    private PostAction postTakeProfitAction;  // 익절 후 대응
    
    public RiskSettings() {
    }
    
    public RiskSettings(boolean leverageAllowed, int maxLeverage, 
                       double maxLossPercent, double maxPositionPercent, 
                       double stopLossPercent) {
        this(leverageAllowed, maxLeverage, maxLossPercent, maxPositionPercent, 
             stopLossPercent, 15.0);  // 기본 익절 15%
    }
    
    public RiskSettings(boolean leverageAllowed, int maxLeverage, 
                       double maxLossPercent, double maxPositionPercent, 
                       double stopLossPercent, double takeProfitPercent) {
        this.leverageAllowed = leverageAllowed;
        this.maxLeverage = maxLeverage;
        this.maxLossPercent = maxLossPercent;
        this.maxPositionPercent = maxPositionPercent;
        this.stopLossPercent = stopLossPercent;
        this.takeProfitPercent = takeProfitPercent;
    }
    
    // Getters and Setters
    public boolean isLeverageAllowed() {
        return leverageAllowed;
    }
    
    public void setLeverageAllowed(boolean leverageAllowed) {
        this.leverageAllowed = leverageAllowed;
    }
    
    public int getMaxLeverage() {
        return maxLeverage;
    }
    
    public void setMaxLeverage(int maxLeverage) {
        this.maxLeverage = maxLeverage;
    }
    
    public double getMaxLossPercent() {
        return maxLossPercent;
    }
    
    public void setMaxLossPercent(double maxLossPercent) {
        this.maxLossPercent = maxLossPercent;
    }
    
    public double getMaxPositionPercent() {
        return maxPositionPercent;
    }
    
    public void setMaxPositionPercent(double maxPositionPercent) {
        this.maxPositionPercent = maxPositionPercent;
    }
    
    public double getStopLossPercent() {
        return stopLossPercent;
    }
    
    public void setStopLossPercent(double stopLossPercent) {
        this.stopLossPercent = stopLossPercent;
    }
    
    public double getTakeProfitPercent() {
        return takeProfitPercent;
    }
    
    public void setTakeProfitPercent(double takeProfitPercent) {
        this.takeProfitPercent = takeProfitPercent;
    }
    
    public PostAction getPostStopLossAction() {
        return postStopLossAction;
    }
    
    public void setPostStopLossAction(PostAction postStopLossAction) {
        this.postStopLossAction = postStopLossAction;
    }
    
    public PostAction getPostTakeProfitAction() {
        return postTakeProfitAction;
    }
    
    public void setPostTakeProfitAction(PostAction postTakeProfitAction) {
        this.postTakeProfitAction = postTakeProfitAction;
    }
    
    @Override
    public String toString() {
        return String.format("RiskSettings[레버리지: %s(%dx), 최대손실: %.1f%%, 진입비중: %.1f%%, 손절: %.1f%%, 익절: %.1f%%, 손절후: %s, 익절후: %s]",
                leverageAllowed ? "허용" : "불가", maxLeverage, maxLossPercent, 
                maxPositionPercent, stopLossPercent, takeProfitPercent,
                postStopLossAction != null ? postStopLossAction.getKoreanName() : "미설정",
                postTakeProfitAction != null ? postTakeProfitAction.getKoreanName() : "미설정");
    }
}

