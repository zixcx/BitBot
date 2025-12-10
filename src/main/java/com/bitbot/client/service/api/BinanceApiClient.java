package com.bitbot.client.service.api;

import com.bitbot.client.dto.BinanceKline;
import com.bitbot.client.dto.BinanceTicker;
import com.bitbot.client.model.Candle;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Binance API Client
 * Handles market data retrieval from Binance API
 * 
 * Public endpoints (no authentication required):
 * - Ticker 24hr statistics
 * - Kline/Candlestick data
 * - Order book
 */
public class BinanceApiClient {
    
    private static final Logger logger = LoggerFactory.getLogger(BinanceApiClient.class);
    private static final String BASE_URL = "https://api.binance.com";
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public BinanceApiClient() {
        this.httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Get 24hr ticker price change statistics
     * 
     * @param symbol Trading pair (e.g., "BTCUSDT")
     * @return CompletableFuture<BinanceTicker>
     */
    public CompletableFuture<BinanceTicker> getTicker24hr(String symbol) {
        String url = String.format("%s/api/v3/ticker/24hr?symbol=%s", BASE_URL, symbol);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    BinanceTicker ticker = objectMapper.readValue(response.body(), BinanceTicker.class);
                    logger.debug("Fetched ticker for {}: ${}", symbol, ticker.getLastPrice());
                    return ticker;
                } else {
                    logger.error("Failed to fetch ticker: {} - {}", response.statusCode(), response.body());
                    throw new RuntimeException("API request failed: " + response.statusCode());
                }
                
            } catch (Exception e) {
                logger.error("Error fetching ticker", e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Get Kline/Candlestick data
     * 
     * @param symbol Trading pair (e.g., "BTCUSDT")
     * @param interval Interval (1m, 5m, 15m, 1h, 4h, 1d, etc.)
     * @param limit Number of candles (max 1000, default 500)
     * @return CompletableFuture<List<Candle>>
     */
    public CompletableFuture<List<Candle>> getKlines(String symbol, String interval, int limit) {
        String url = String.format("%s/api/v3/klines?symbol=%s&interval=%s&limit=%d", 
            BASE_URL, symbol, interval, limit);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    // Parse JSON array response
                    JsonNode jsonArray = objectMapper.readTree(response.body());
                    List<Candle> candles = new ArrayList<>();
                    
                    for (JsonNode node : jsonArray) {
                        long openTime = node.get(0).asLong();
                        double open = Double.parseDouble(node.get(1).asText());
                        double high = Double.parseDouble(node.get(2).asText());
                        double low = Double.parseDouble(node.get(3).asText());
                        double close = Double.parseDouble(node.get(4).asText());
                        double volume = Double.parseDouble(node.get(5).asText());
                        
                        LocalDateTime timestamp = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(openTime), 
                            ZoneId.systemDefault()
                        );
                        
                        candles.add(new Candle(timestamp, open, high, low, close, volume));
                    }
                    
                    logger.debug("Fetched {} candles for {} ({})", candles.size(), symbol, interval);
                    return candles;
                } else {
                    logger.error("Failed to fetch klines: {} - {}", response.statusCode(), response.body());
                    throw new RuntimeException("API request failed: " + response.statusCode());
                }
                
            } catch (Exception e) {
                logger.error("Error fetching klines", e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Get current price (simple)
     * 
     * @param symbol Trading pair (e.g., "BTCUSDT")
     * @return CompletableFuture<Double>
     */
    public CompletableFuture<Double> getCurrentPrice(String symbol) {
        String url = String.format("%s/api/v3/ticker/price?symbol=%s", BASE_URL, symbol);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    JsonNode json = objectMapper.readTree(response.body());
                    double price = json.get("price").asDouble();
                    logger.debug("Current price for {}: ${}", symbol, price);
                    return price;
                } else {
                    logger.error("Failed to fetch price: {} - {}", response.statusCode(), response.body());
                    throw new RuntimeException("API request failed: " + response.statusCode());
                }
                
            } catch (Exception e) {
                logger.error("Error fetching current price", e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Ping Binance API to check connectivity
     * 
     * @return CompletableFuture<Boolean>
     */
    public CompletableFuture<Boolean> ping() {
        String url = BASE_URL + "/api/v3/ping";
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                boolean success = response.statusCode() == 200;
                logger.debug("Binance API ping: {}", success ? "SUCCESS" : "FAILED");
                return success;
                
            } catch (Exception e) {
                logger.error("Error pinging Binance API", e);
                return false;
            }
        });
    }
}


