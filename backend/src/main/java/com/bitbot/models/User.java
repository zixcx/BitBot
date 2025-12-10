package com.bitbot.models;

/**
 * 사용자 모델
 */
public class User {
    
    private Integer id;
    private String email;
    private String username;
    private String passwordHash;
    private String binanceApiKeyEncrypted;
    private String binanceSecretKeyEncrypted;
    private boolean tradingEnabled;
    private boolean riskManagementEnabled;
    private double maxInvestmentPercent;
    
    public User() {}
    
    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public String getBinanceApiKeyEncrypted() { return binanceApiKeyEncrypted; }
    public void setBinanceApiKeyEncrypted(String binanceApiKeyEncrypted) { this.binanceApiKeyEncrypted = binanceApiKeyEncrypted; }
    
    public String getBinanceSecretKeyEncrypted() { return binanceSecretKeyEncrypted; }
    public void setBinanceSecretKeyEncrypted(String binanceSecretKeyEncrypted) { this.binanceSecretKeyEncrypted = binanceSecretKeyEncrypted; }
    
    public boolean isTradingEnabled() { return tradingEnabled; }
    public void setTradingEnabled(boolean tradingEnabled) { this.tradingEnabled = tradingEnabled; }
    
    public boolean isRiskManagementEnabled() { return riskManagementEnabled; }
    public void setRiskManagementEnabled(boolean riskManagementEnabled) { this.riskManagementEnabled = riskManagementEnabled; }
    
    public double getMaxInvestmentPercent() { return maxInvestmentPercent; }
    public void setMaxInvestmentPercent(double maxInvestmentPercent) { this.maxInvestmentPercent = maxInvestmentPercent; }
}

