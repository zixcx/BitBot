package com.bitbot.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Trading Status DTO
 */
public class TradingStatusDto {
    
    @JsonProperty("running")
    private Boolean running;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("strategy")
    private String strategy;
    
    @JsonProperty("intervalMinutes")
    private Integer intervalMinutes;

    // Getters and Setters
    public Boolean getRunning() { return running; }
    public void setRunning(Boolean running) { this.running = running; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }

    public Integer getIntervalMinutes() { return intervalMinutes; }
    public void setIntervalMinutes(Integer intervalMinutes) { this.intervalMinutes = intervalMinutes; }
}

