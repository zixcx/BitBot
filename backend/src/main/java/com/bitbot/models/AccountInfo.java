package com.bitbot.models;

/**
 * 계좌 정보 모델
 */
public class AccountInfo {
    
    private double totalBalance;        // 총 잔고 (USD)
    private double availableBalance;    // 사용 가능 잔고
    private double investedAmount;      // 투자 중인 금액
    private double btcHolding;          // 보유 BTC 수량
    private double btcValue;            // 보유 BTC 가치 (USD)
    
    private double totalProfitLoss;     // 총 손익
    private double profitLossPercent;   // 손익률 (%)
    
    private int totalTrades;            // 총 거래 횟수
    private int winningTrades;          // 수익 거래 수
    private int losingTrades;           // 손실 거래 수
    
    public AccountInfo() {
    }
    
    // Getters and Setters
    public double getTotalBalance() {
        return totalBalance;
    }
    
    public void setTotalBalance(double totalBalance) {
        this.totalBalance = totalBalance;
    }
    
    public double getAvailableBalance() {
        return availableBalance;
    }
    
    public void setAvailableBalance(double availableBalance) {
        this.availableBalance = availableBalance;
    }
    
    public double getInvestedAmount() {
        return investedAmount;
    }
    
    public void setInvestedAmount(double investedAmount) {
        this.investedAmount = investedAmount;
    }
    
    public double getBtcHolding() {
        return btcHolding;
    }
    
    public void setBtcHolding(double btcHolding) {
        this.btcHolding = btcHolding;
    }
    
    public double getBtcValue() {
        return btcValue;
    }
    
    public void setBtcValue(double btcValue) {
        this.btcValue = btcValue;
    }
    
    public double getTotalProfitLoss() {
        return totalProfitLoss;
    }
    
    public void setTotalProfitLoss(double totalProfitLoss) {
        this.totalProfitLoss = totalProfitLoss;
    }
    
    public double getProfitLossPercent() {
        return profitLossPercent;
    }
    
    public void setProfitLossPercent(double profitLossPercent) {
        this.profitLossPercent = profitLossPercent;
    }
    
    public int getTotalTrades() {
        return totalTrades;
    }
    
    public void setTotalTrades(int totalTrades) {
        this.totalTrades = totalTrades;
    }
    
    public int getWinningTrades() {
        return winningTrades;
    }
    
    public void setWinningTrades(int winningTrades) {
        this.winningTrades = winningTrades;
    }
    
    public int getLosingTrades() {
        return losingTrades;
    }
    
    public void setLosingTrades(int losingTrades) {
        this.losingTrades = losingTrades;
    }
    
    public double getWinRate() {
        return totalTrades > 0 ? (double) winningTrades / totalTrades * 100 : 0;
    }
    
    public double getInvestmentRatio() {
        return totalBalance > 0 ? investedAmount / totalBalance * 100 : 0;
    }
    
    @Override
    public String toString() {
        return String.format("Account[Total: $%.2f, Available: $%.2f, BTC: %.6f, P/L: %.2f%%]",
                totalBalance, availableBalance, btcHolding, profitLossPercent);
    }
}


