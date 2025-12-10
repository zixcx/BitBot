package com.bitbot.client.service;

import com.bitbot.client.model.Candle;
import com.bitbot.client.model.MarketAnalysis;
import com.bitbot.client.model.TradeDecision;
import com.bitbot.client.service.analysis.MarketAnalysisService;
import com.bitbot.client.service.api.ServerApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Auto Trading Engine
 * Orchestrates the complete trading cycle:
 * 1. Collect market data
 * 2. Perform technical analysis
 * 3. Get AI decision from backend server
 * 4. Execute trade (if applicable)
 * 5. Update UI and log to server
 * 
 * Features:
 * - 3-Way Decision Logic (BUY/SELL/HOLD)
 * - Risk management
 * - Trade execution
 * - Event notifications
 * 
 * Note: AI analysis is handled by backend server, not client
 */
public class AutoTradingEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(AutoTradingEngine.class);
    private static final int DEFAULT_TRADING_INTERVAL = 60; // seconds
    
    private final MarketDataService marketDataService;
    private final MarketAnalysisService analysisService;
    private final ServerApiClient serverApiClient;
    
    private final ScheduledExecutorService scheduler;
    private TradingEngineListener listener;
    
    private boolean running = false;
    private List<Candle> latestCandles;
    private double latestPrice;
    private double latestPriceChange;

    public AutoTradingEngine(MarketDataService marketDataService, ServerApiClient serverApiClient) {
        this.marketDataService = marketDataService;
        this.serverApiClient = serverApiClient;
        this.analysisService = new MarketAnalysisService();
        this.scheduler = Executors.newScheduledThreadPool(1);
        
        // Listen to market data updates
        marketDataService.setListener(new MarketDataService.MarketDataListener() {
            @Override
            public void onPriceUpdate(double price, double changePercent) {
                latestPrice = price;
                latestPriceChange = changePercent;
            }

            @Override
            public void onCandlesUpdate(List<Candle> candles) {
                latestCandles = candles;
            }
        });
    }

    /**
     * Start auto trading
     * Backend server controls the actual trading execution
     * 
     * @param intervalSeconds Trading cycle interval
     */
    public void start(int intervalSeconds) {
        if (running) {
            logger.warn("Trading engine is already running");
            return;
        }
        
        if (!serverApiClient.isAuthenticated()) {
            logger.error("Server API not authenticated");
            if (listener != null) {
                listener.onError("서버에 로그인이 필요합니다");
            }
            return;
        }
        
        running = true;
        logger.info("Starting auto trading engine (interval: {}s) - Backend controlled", intervalSeconds);
        
        if (listener != null) {
            listener.onEngineStarted();
        }
        
        // Note: The actual trading is controlled by backend
        // This client just monitors the status
        logger.info("Auto trading is now managed by backend server");
    }

    /**
     * Stop auto trading
     */
    public void stop() {
        if (!running) {
            return;
        }
        
        running = false;
        scheduler.shutdown();
        logger.info("Auto trading engine stopped");
        
        if (listener != null) {
            listener.onEngineStopped();
        }
    }

    /**
     * Set event listener
     */
    public void setListener(TradingEngineListener listener) {
        this.listener = listener;
    }

    /**
     * Check if engine is running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Trading Engine Event Listener
     */
    public interface TradingEngineListener {
        void onEngineStarted();
        void onEngineStopped();
        void onAnalysisStarted();
        void onDecisionReceived(TradeDecision decision, MarketAnalysis analysis);
        void onTradeExecuted(TradeDecision decision, boolean success);
        void onHoldDecision(TradeDecision decision);
        void onError(String message);
    }
}
