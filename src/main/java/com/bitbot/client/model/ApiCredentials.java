package com.bitbot.client.model;

/**
 * API Credentials Container
 * Binance API 키만 저장 (Gemini는 백엔드에서 처리)
 */
public class ApiCredentials {
    
    private String binanceApiKey;
    private String binanceSecretKey;
    private String serverSessionToken;
    
    public ApiCredentials() {}
    
    public ApiCredentials(String binanceApiKey, String binanceSecretKey) {
        this.binanceApiKey = binanceApiKey;
        this.binanceSecretKey = binanceSecretKey;
    }

    // Getters and Setters
    public String getBinanceApiKey() {
        return binanceApiKey;
    }

    public void setBinanceApiKey(String binanceApiKey) {
        this.binanceApiKey = binanceApiKey;
    }

    public String getBinanceSecretKey() {
        return binanceSecretKey;
    }

    public void setBinanceSecretKey(String binanceSecretKey) {
        this.binanceSecretKey = binanceSecretKey;
    }

    public String getServerSessionToken() {
        return serverSessionToken;
    }

    public void setServerSessionToken(String serverSessionToken) {
        this.serverSessionToken = serverSessionToken;
    }

    /**
     * Check if Binance credentials are configured
     */
    public boolean hasBinanceCredentials() {
        return binanceApiKey != null && !binanceApiKey.isEmpty()
            && binanceSecretKey != null && !binanceSecretKey.isEmpty();
    }

    /**
     * Clear all credentials from memory
     */
    public void clear() {
        binanceApiKey = null;
        binanceSecretKey = null;
        serverSessionToken = null;
    }

    @Override
    public String toString() {
        return "ApiCredentials{" +
            "binanceApiKey=" + (binanceApiKey != null ? "***" : "null") +
            ", binanceSecretKey=" + (binanceSecretKey != null ? "***" : "null") +
            ", serverSessionToken=" + (serverSessionToken != null ? "***" : "null") +
            '}';
    }
}
