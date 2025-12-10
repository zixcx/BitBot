package com.bitbot.server.controller;

import com.bitbot.data.BinanceDataCollector;
import com.bitbot.data.FearGreedIndexCollector;
import com.bitbot.data.NewsCollector;
import com.bitbot.models.MarketData;
import com.bitbot.server.dto.ApiResponse;
import com.bitbot.server.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 시장 데이터 관련 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/market")
@CrossOrigin(origins = "*")
public class MarketDataController {
    
    private static final Logger logger = LoggerFactory.getLogger(MarketDataController.class);
    private final BinanceDataCollector dataCollector;
    private final NewsCollector newsCollector;
    private final FearGreedIndexCollector fearGreedCollector;
    
    public MarketDataController() {
        this.dataCollector = new BinanceDataCollector();
        this.newsCollector = new NewsCollector();
        this.fearGreedCollector = new FearGreedIndexCollector();
    }
    
    /**
     * 차트 데이터 조회 (OHLCV)
     * GET /api/market/chart?symbol=BTCUSDT&timeframe=1h&limit=100
     */
    @GetMapping("/chart")
    public ResponseEntity<ApiResponse<List<MarketData>>> getChartData(
            @RequestParam(defaultValue = "BTCUSDT") String symbol,
            @RequestParam(defaultValue = "1h") String timeframe,
            @RequestParam(defaultValue = "100") int limit) {
        try {
            List<MarketData> chartData = dataCollector.getKlines(symbol, timeframe, limit);
            return ResponseEntity.ok(ApiResponse.success(chartData));
        } catch (Exception e) {
            logger.error("차트 데이터 조회 실패", e);
            return ResponseEntity.ok(ApiResponse.error("차트 데이터 조회 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 현재 가격 조회
     * GET /api/market/price?symbol=BTCUSDT
     */
    @GetMapping("/price")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentPrice(
            @RequestParam(defaultValue = "BTCUSDT") String symbol) {
        try {
            double price = dataCollector.getCurrentPrice(symbol);
            Map<String, Object> result = new HashMap<>();
            result.put("symbol", symbol);
            result.put("price", price);
            result.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            logger.error("현재 가격 조회 실패", e);
            return ResponseEntity.ok(ApiResponse.error("현재 가격 조회 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 24시간 통계 조회
     * GET /api/market/24h-stats?symbol=BTCUSDT
     */
    @GetMapping("/24h-stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> get24hStats(
            @RequestParam(defaultValue = "BTCUSDT") String symbol) {
        try {
            Map<String, Object> stats = dataCollector.get24hrStats(symbol);
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            logger.error("24시간 통계 조회 실패", e);
            return ResponseEntity.ok(ApiResponse.error("24시간 통계 조회 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 뉴스 조회
     * GET /api/market/news?limit=10
     */
    @GetMapping("/news")
    public ResponseEntity<ApiResponse<List<NewsCollector.NewsItem>>> getNews(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<NewsCollector.NewsItem> news = newsCollector.getRecentNews(limit);
            return ResponseEntity.ok(ApiResponse.success(news));
        } catch (Exception e) {
            logger.error("뉴스 조회 실패", e);
            return ResponseEntity.ok(ApiResponse.error("뉴스 조회 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 공포/탐욕 지수 조회
     * GET /api/market/fear-greed
     */
    @GetMapping("/fear-greed")
    public ResponseEntity<ApiResponse<FearGreedIndexCollector.FearGreedIndex>> getFearGreedIndex() {
        try {
            FearGreedIndexCollector.FearGreedIndex index = fearGreedCollector.getCurrentIndex();
            return ResponseEntity.ok(ApiResponse.success(index));
        } catch (Exception e) {
            logger.error("공포/탐욕 지수 조회 실패", e);
            return ResponseEntity.ok(ApiResponse.error("공포/탐욕 지수 조회 실패: " + e.getMessage()));
        }
    }
}

