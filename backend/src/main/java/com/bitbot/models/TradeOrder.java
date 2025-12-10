package com.bitbot.models;

import java.time.LocalDateTime;

/**
 * 거래 주문을 나타내는 모델
 */
public class TradeOrder {
    
    public enum OrderType {
        MARKET_BUY,
        MARKET_SELL,
        LIMIT_BUY,
        LIMIT_SELL
    }
    
    public enum OrderStatus {
        PENDING,
        SUBMITTED,
        FILLED,
        PARTIALLY_FILLED,
        CANCELLED,
        REJECTED,
        FAILED
    }
    
    private Long id;
    private String symbol;
    private OrderType type;
    private OrderStatus status;
    
    private double quantity;
    private double price;
    private double executedPrice;
    private double totalCost;
    
    private int leverage;  // 레버리지 배수 (1, 3, 10 등)
    private boolean isFuturesTrade;  // 선물 거래 여부
    
    private String reason;
    private String binanceOrderId;
    
    private LocalDateTime createdAt;
    private LocalDateTime executedAt;
    
    private TradingDecision decision;  // 이 주문을 발생시킨 결정
    
    public TradeOrder() {
        this.createdAt = LocalDateTime.now();
        this.status = OrderStatus.PENDING;
    }
    
    public TradeOrder(String symbol, OrderType type, double quantity, double price) {
        this();
        this.symbol = symbol;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public OrderType getType() {
        return type;
    }
    
    public void setType(OrderType type) {
        this.type = type;
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    public double getQuantity() {
        return quantity;
    }
    
    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
    
    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    public double getExecutedPrice() {
        return executedPrice;
    }
    
    public void setExecutedPrice(double executedPrice) {
        this.executedPrice = executedPrice;
    }
    
    public double getTotalCost() {
        return totalCost;
    }
    
    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getBinanceOrderId() {
        return binanceOrderId;
    }
    
    public void setBinanceOrderId(String binanceOrderId) {
        this.binanceOrderId = binanceOrderId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getExecutedAt() {
        return executedAt;
    }
    
    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }
    
    public TradingDecision getDecision() {
        return decision;
    }
    
    public void setDecision(TradingDecision decision) {
        this.decision = decision;
    }
    
    public int getLeverage() {
        return leverage;
    }
    
    public void setLeverage(int leverage) {
        this.leverage = leverage;
    }
    
    public boolean isFuturesTrade() {
        return isFuturesTrade;
    }
    
    public void setFuturesTrade(boolean isFuturesTrade) {
        this.isFuturesTrade = isFuturesTrade;
    }
    
    public boolean isBuyOrder() {
        return type == OrderType.MARKET_BUY || type == OrderType.LIMIT_BUY;
    }
    
    public boolean isSellOrder() {
        return type == OrderType.MARKET_SELL || type == OrderType.LIMIT_SELL;
    }
    
    @Override
    public String toString() {
        return String.format("Order[%s %s %.4f @ %.2f] Status: %s", 
                symbol, type, quantity, price, status);
    }
}


