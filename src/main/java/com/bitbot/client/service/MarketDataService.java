package com.bitbot.client.service;

import com.bitbot.client.model.Candle;
import com.bitbot.client.service.api.BinanceApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Market Data Service
 * Manages real-time market data updates
 * 
 * Features:
 * - Periodic price updates
 * - Candle data synchronization
 * - Event-based notifications
 */
public class MarketDataService {
    
    private static final Logger logger = LoggerFactory.getLogger(MarketDataService.class);
    private static final String DEFAULT_SYMBOL = "BTCUSDT";
    private static final String DEFAULT_INTERVAL = "1h";
    private static final int DEFAULT_CANDLE_LIMIT = 50;
    
    private final BinanceApiClient binanceClient;
    private final ScheduledExecutorService scheduler;
    private MarketDataListener listener;
    private boolean running = false;

    public MarketDataService() {
        this.binanceClient = new BinanceApiClient();
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    /**
     * Start periodic market data updates
     * 
     * @param intervalSeconds Update interval in seconds
     */
    public void start(int intervalSeconds) {
        if (running) {
            logger.warn("Market data service is already running");
            return;
        }
        
        running = true;
        logger.info("Starting market data service (interval: {}s)", intervalSeconds);
        
        // Initial fetch
        updateMarketData();
        
        // Schedule periodic updates
        scheduler.scheduleAtFixedRate(
            this::updateMarketData,
            intervalSeconds,
            intervalSeconds,
            TimeUnit.SECONDS
        );
    }

    /**
     * Stop market data updates
     */
    public void stop() {
        if (!running) {
            return;
        }
        
        running = false;
        scheduler.shutdown();
        logger.info("Market data service stopped");
    }

    /**
     * Fetch latest market data
     */
    private void updateMarketData() {
        try {
            // Fetch ticker (price + 24h change)
            binanceClient.getTicker24hr(DEFAULT_SYMBOL)
                .thenAccept(ticker -> {
                    if (listener != null) {
                        javafx.application.Platform.runLater(() -> 
                            listener.onPriceUpdate(
                                ticker.getLastPriceAsDouble(),
                                ticker.getPriceChangePercentAsDouble()
                            )
                        );
                    }
                })
                .exceptionally(ex -> {
                    logger.error("Failed to fetch ticker", ex);
                    return null;
                });
            
            // Fetch candles
            binanceClient.getKlines(DEFAULT_SYMBOL, DEFAULT_INTERVAL, DEFAULT_CANDLE_LIMIT)
                .thenAccept(candles -> {
                    if (listener != null) {
                        javafx.application.Platform.runLater(() -> 
                            listener.onCandlesUpdate(candles)
                        );
                    }
                })
                .exceptionally(ex -> {
                    logger.error("Failed to fetch candles", ex);
                    return null;
                });
                
        } catch (Exception e) {
            logger.error("Error updating market data", e);
        }
    }

    /**
     * Manually trigger a market data update
     */
    public void refresh() {
        updateMarketData();
    }

    /**
     * Set listener for market data events
     */
    public void setListener(MarketDataListener listener) {
        this.listener = listener;
    }

    /**
     * Check Binance API connectivity
     */
    public CompletableFuture<Boolean> checkConnectivity() {
        return binanceClient.ping();
    }

    /**
     * Interface for market data event callbacks
     */
    public interface MarketDataListener {
        void onPriceUpdate(double price, double changePercent);
        void onCandlesUpdate(List<Candle> candles);
    }
}


