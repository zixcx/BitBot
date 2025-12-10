package com.bitbot.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Account Information DTO
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountInfoDto {
    
    @JsonProperty("totalBalance")
    private Double totalBalance;
    
    @JsonProperty("availableBalance")
    private Double availableBalance;
    
    @JsonProperty("investedAmount")
    private Double investedAmount;
    
    @JsonProperty("btcHolding")
    private Double btcHolding;
    
    @JsonProperty("btcValue")
    private Double btcValue;
    
    @JsonProperty("totalProfitLoss")
    private Double totalProfitLoss;
    
    @JsonProperty("profitLossPercent")
    private Double profitLossPercent;

    @JsonProperty("totalTrades")
    private Integer totalTrades;

    @JsonProperty("winningTrades")
    private Integer winningTrades;

    @JsonProperty("losingTrades")
    private Integer losingTrades;

    @JsonProperty("winRate")
    private Double winRate;

    @JsonProperty("investmentRatio")
    private Double investmentRatio;

    // Getters and Setters
    public Double getTotalBalance() { return totalBalance; }
    public void setTotalBalance(Double totalBalance) { this.totalBalance = totalBalance; }

    public Double getAvailableBalance() { return availableBalance; }
    public void setAvailableBalance(Double availableBalance) { this.availableBalance = availableBalance; }

    public Double getInvestedAmount() { return investedAmount; }
    public void setInvestedAmount(Double investedAmount) { this.investedAmount = investedAmount; }

    public Double getBtcHolding() { return btcHolding; }
    public void setBtcHolding(Double btcHolding) { this.btcHolding = btcHolding; }

    public Double getBtcValue() { return btcValue; }
    public void setBtcValue(Double btcValue) { this.btcValue = btcValue; }

    public Double getTotalProfitLoss() { return totalProfitLoss; }
    public void setTotalProfitLoss(Double totalProfitLoss) { this.totalProfitLoss = totalProfitLoss; }

    public Double getProfitLossPercent() { return profitLossPercent; }
    public void setProfitLossPercent(Double profitLossPercent) { this.profitLossPercent = profitLossPercent; }

    public Integer getTotalTrades() { return totalTrades; }
    public void setTotalTrades(Integer totalTrades) { this.totalTrades = totalTrades; }

    public Integer getWinningTrades() { return winningTrades; }
    public void setWinningTrades(Integer winningTrades) { this.winningTrades = winningTrades; }

    public Integer getLosingTrades() { return losingTrades; }
    public void setLosingTrades(Integer losingTrades) { this.losingTrades = losingTrades; }

    public Double getWinRate() { return winRate; }
    public void setWinRate(Double winRate) { this.winRate = winRate; }

    public Double getInvestmentRatio() { return investmentRatio; }
    public void setInvestmentRatio(Double investmentRatio) { this.investmentRatio = investmentRatio; }
}
