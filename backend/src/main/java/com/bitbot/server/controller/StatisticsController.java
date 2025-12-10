package com.bitbot.server.controller;

import com.bitbot.database.TradeRepository;
import com.bitbot.models.TradeOrder;
import com.bitbot.server.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 통계 데이터 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = "*")
public class StatisticsController {
    
    private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);
    private final TradeRepository tradeRepository;
    
    // 기본 사용자 ID
    private static final Integer DEFAULT_USER_ID = 1;
    
    public StatisticsController() {
        this.tradeRepository = new TradeRepository();
    }
    
    /**
     * 거래 통계 조회
     * GET /api/statistics/trades
     */
    @GetMapping("/trades")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTradeStatistics() {
        try {
            // 최근 거래 내역 조회
            List<TradeOrder> trades = tradeRepository.findRecentTrades(DEFAULT_USER_ID.toString(), 1000);
            
            // 통계 계산
            int totalTrades = trades.size();
            int buyTrades = 0;
            int sellTrades = 0;
            int winningTrades = 0;
            int losingTrades = 0;
            double totalProfit = 0.0;
            double totalLoss = 0.0;
            double maxProfit = 0.0;
            double maxLoss = 0.0;
            
            for (TradeOrder trade : trades) {
                if (trade.isBuyOrder()) {
                    buyTrades++;
                } else {
                    sellTrades++;
                }
                
                if (trade.getProfitLoss() != null) {
                    double pnl = trade.getProfitLoss();
                    if (pnl > 0) {
                        winningTrades++;
                        totalProfit += pnl;
                        maxProfit = Math.max(maxProfit, pnl);
                    } else if (pnl < 0) {
                        losingTrades++;
                        totalLoss += Math.abs(pnl);
                        maxLoss = Math.min(maxLoss, pnl);
                    }
                }
            }
            
            double winRate = totalTrades > 0 ? (double) winningTrades / totalTrades * 100 : 0.0;
            double avgProfit = winningTrades > 0 ? totalProfit / winningTrades : 0.0;
            double avgLoss = losingTrades > 0 ? totalLoss / losingTrades : 0.0;
            double netProfit = totalProfit - totalLoss;
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalTrades", totalTrades);
            stats.put("buyTrades", buyTrades);
            stats.put("sellTrades", sellTrades);
            stats.put("winningTrades", winningTrades);
            stats.put("losingTrades", losingTrades);
            stats.put("winRate", Math.round(winRate * 100.0) / 100.0);
            stats.put("totalProfit", Math.round(totalProfit * 100.0) / 100.0);
            stats.put("totalLoss", Math.round(totalLoss * 100.0) / 100.0);
            stats.put("netProfit", Math.round(netProfit * 100.0) / 100.0);
            stats.put("avgProfit", Math.round(avgProfit * 100.0) / 100.0);
            stats.put("avgLoss", Math.round(avgLoss * 100.0) / 100.0);
            stats.put("maxProfit", Math.round(maxProfit * 100.0) / 100.0);
            stats.put("maxLoss", Math.round(maxLoss * 100.0) / 100.0);
            
            return ResponseEntity.ok(ApiResponse.success(stats));
            
        } catch (Exception e) {
            logger.error("거래 통계 조회 실패", e);
            return ResponseEntity.ok(ApiResponse.error("거래 통계 조회 실패: " + e.getMessage()));
        }
    }
}

