package com.bitbot.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Trade Order DTO
 */
public class TradeOrderDto {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("symbol")
    private String symbol;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("quantity")
    private Double quantity;
    
    @JsonProperty("price")
    private Double price;
    
    @JsonProperty("executedPrice")
    private Double executedPrice;
    
    @JsonProperty("totalCost")
    private Double totalCost;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("decision")
    private String decision;
    
    @JsonProperty("reason")
    private String reason;
    
    @JsonProperty("leverage")
    private Integer leverage;
    
    @JsonProperty("isFuturesTrade")
    private Boolean isFuturesTrade;
    
    @JsonProperty("profitLoss")
    private Double profitLoss;
    
    @JsonProperty("profitLossPercent")
    private Double profitLossPercent;
    
    @JsonProperty("executedAt")
    private LocalDateTime executedAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Double getExecutedPrice() { return executedPrice; }
    public void setExecutedPrice(Double executedPrice) { this.executedPrice = executedPrice; }

    public Double getTotalCost() { return totalCost; }
    public void setTotalCost(Double totalCost) { this.totalCost = totalCost; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Integer getLeverage() { return leverage; }
    public void setLeverage(Integer leverage) { this.leverage = leverage; }

    public Boolean getIsFuturesTrade() { return isFuturesTrade; }
    public void setIsFuturesTrade(Boolean isFuturesTrade) { this.isFuturesTrade = isFuturesTrade; }

    public Double getProfitLoss() { return profitLoss; }
    public void setProfitLoss(Double profitLoss) { this.profitLoss = profitLoss; }

    public Double getProfitLossPercent() { return profitLossPercent; }
    public void setProfitLossPercent(Double profitLossPercent) { this.profitLossPercent = profitLossPercent; }

    public LocalDateTime getExecutedAt() { return executedAt; }
    public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }
}

