package com.bitbot.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Trade Statistics DTO
 */
public class TradeStatisticsDto {
    
    @JsonProperty("totalTrades")
    private Integer totalTrades;
    
    @JsonProperty("buyTrades")
    private Integer buyTrades;
    
    @JsonProperty("sellTrades")
    private Integer sellTrades;
    
    @JsonProperty("winningTrades")
    private Integer winningTrades;
    
    @JsonProperty("losingTrades")
    private Integer losingTrades;
    
    @JsonProperty("winRate")
    private Double winRate;
    
    @JsonProperty("totalProfit")
    private Double totalProfit;
    
    @JsonProperty("totalLoss")
    private Double totalLoss;
    
    @JsonProperty("netProfit")
    private Double netProfit;
    
    @JsonProperty("avgProfit")
    private Double avgProfit;
    
    @JsonProperty("avgLoss")
    private Double avgLoss;
    
    @JsonProperty("maxProfit")
    private Double maxProfit;
    
    @JsonProperty("maxLoss")
    private Double maxLoss;

    // Getters and Setters
    public Integer getTotalTrades() { return totalTrades; }
    public void setTotalTrades(Integer totalTrades) { this.totalTrades = totalTrades; }

    public Integer getBuyTrades() { return buyTrades; }
    public void setBuyTrades(Integer buyTrades) { this.buyTrades = buyTrades; }

    public Integer getSellTrades() { return sellTrades; }
    public void setSellTrades(Integer sellTrades) { this.sellTrades = sellTrades; }

    public Integer getWinningTrades() { return winningTrades; }
    public void setWinningTrades(Integer winningTrades) { this.winningTrades = winningTrades; }

    public Integer getLosingTrades() { return losingTrades; }
    public void setLosingTrades(Integer losingTrades) { this.losingTrades = losingTrades; }

    public Double getWinRate() { return winRate; }
    public void setWinRate(Double winRate) { this.winRate = winRate; }

    public Double getTotalProfit() { return totalProfit; }
    public void setTotalProfit(Double totalProfit) { this.totalProfit = totalProfit; }

    public Double getTotalLoss() { return totalLoss; }
    public void setTotalLoss(Double totalLoss) { this.totalLoss = totalLoss; }

    public Double getNetProfit() { return netProfit; }
    public void setNetProfit(Double netProfit) { this.netProfit = netProfit; }

    public Double getAvgProfit() { return avgProfit; }
    public void setAvgProfit(Double avgProfit) { this.avgProfit = avgProfit; }

    public Double getAvgLoss() { return avgLoss; }
    public void setAvgLoss(Double avgLoss) { this.avgLoss = avgLoss; }

    public Double getMaxProfit() { return maxProfit; }
    public void setMaxProfit(Double maxProfit) { this.maxProfit = maxProfit; }

    public Double getMaxLoss() { return maxLoss; }
    public void setMaxLoss(Double maxLoss) { this.maxLoss = maxLoss; }
}

