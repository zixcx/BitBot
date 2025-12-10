package com.bitbot.database;

import com.bitbot.models.MarketData;
import com.bitbot.models.TradingDecision;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 거래 로그 저장소 (PRD 요구사항: HOLD 포함 모든 AI 판단 기록)
 */
public class TradeLogRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(TradeLogRepository.class);
    private final ObjectMapper objectMapper;
    
    public TradeLogRepository() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 거래 로그 저장 (HOLD 포함 모든 판단 기록)
     * 
     * @param decision AI 판단 결과
     * @param userId 사용자 ID
     * @param symbol 거래 심볼 (기본: BTCUSDT)
     * @param executedPrice 실행 가격 (HOLD시 null)
     * @param executedQty 실행 수량 (HOLD시 null)
     * @param realizedPnl 실현 손익 (매도시만)
     * @param marketSnapshot 시장 스냅샷 (JSON)
     * @return 저장된 로그 ID
     */
    public Long save(
            TradingDecision decision,
            Integer userId,
            String symbol,
            Double executedPrice,
            Double executedQty,
            Double realizedPnl,
            String marketSnapshot) {
        
        String sql = """
            INSERT INTO trade_logs (
                user_id, symbol, action_type, confidence_score,
                brief_reason, full_reason,
                executed_price, executed_qty, realized_pnl,
                market_snapshot, agent_name, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Decision을 action_type으로 변환
            String actionType = mapDecisionToActionType(decision.getDecision());
            
            // brief_reason과 full_reason 구분
            String reason = decision.getReason();
            String briefReason = reason != null && reason.length() > 255 
                    ? reason.substring(0, 252) + "..." 
                    : reason;
            String fullReason = reason;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, symbol != null ? symbol : "BTCUSDT");
                stmt.setString(3, actionType);
                stmt.setDouble(4, decision.getConfidence());
                stmt.setString(5, briefReason);
                stmt.setString(6, fullReason);
                stmt.setObject(7, executedPrice);
                stmt.setObject(8, executedQty);
                stmt.setObject(9, realizedPnl);
                stmt.setString(10, marketSnapshot);
                stmt.setString(11, decision.getAgentName());
                stmt.setString(12, decision.getTimestamp() != null 
                        ? decision.getTimestamp().toString() 
                        : Timestamp.valueOf(java.time.LocalDateTime.now()).toString());
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    try (Statement idStmt = conn.createStatement();
                         ResultSet rs = idStmt.executeQuery("SELECT last_insert_rowid()")) {
                        if (rs.next()) {
                            Long id = rs.getLong(1);
                            conn.commit();
                            logger.debug("거래 로그 저장 완료 (ID: {}, Action: {})", id, actionType);
                            return id;
                        }
                    }
                }
            }
            
            conn.rollback();
            logger.error("거래 로그 저장 실패: 영향받은 행 없음");
            return null;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("롤백 실패", ex);
                }
            }
            logger.error("거래 로그 저장 실패", e);
            return null;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.error("연결 종료 실패", e);
                }
            }
        }
    }
    
    /**
     * 시장 스냅샷 JSON 생성
     */
    public String createMarketSnapshot(MarketData latest) {
        try {
            ObjectNode snapshot = objectMapper.createObjectNode();
            snapshot.put("price", latest.getClose());
            snapshot.put("rsi", latest.getRsi() != null ? latest.getRsi() : 0);
            snapshot.put("macd", latest.getMacd() != null ? latest.getMacd() : 0);
            snapshot.put("macd_signal", latest.getMacdSignal() != null ? latest.getMacdSignal() : 0);
            snapshot.put("ma20", latest.getMaShort() != null ? latest.getMaShort() : latest.getClose());
            snapshot.put("ma60", latest.getMaLong() != null ? latest.getMaLong() : latest.getClose());
            snapshot.put("bb_upper", latest.getBollingerUpper() != null ? latest.getBollingerUpper() : latest.getClose());
            snapshot.put("bb_middle", latest.getBollingerMiddle() != null ? latest.getBollingerMiddle() : latest.getClose());
            snapshot.put("bb_lower", latest.getBollingerLower() != null ? latest.getBollingerLower() : latest.getClose());
            snapshot.put("volume", latest.getVolume());
            
            return objectMapper.writeValueAsString(snapshot);
        } catch (Exception e) {
            logger.error("시장 스냅샷 생성 실패", e);
            return "{}";
        }
    }
    
    /**
     * Decision을 action_type으로 변환
     */
    private String mapDecisionToActionType(TradingDecision.Decision decision) {
        return switch (decision) {
            case STRONG_BUY, BUY -> "BUY";
            case STRONG_SELL, SELL -> "SELL";
            case HOLD -> "HOLD";
        };
    }
    
    /**
     * 사용자의 최근 거래 로그 조회
     */
    public List<TradeLog> findByUserId(Integer userId, int limit) {
        String sql = """
            SELECT id, user_id, symbol, action_type, confidence_score,
                   brief_reason, full_reason, executed_price, executed_qty, realized_pnl,
                   market_snapshot, agent_name, created_at
            FROM trade_logs
            WHERE user_id = ?
            ORDER BY created_at DESC
            LIMIT ?
            """;
        
        List<TradeLog> logs = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToTradeLog(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("거래 로그 조회 실패", e);
        }
        
        return logs;
    }
    
    private TradeLog mapResultSetToTradeLog(ResultSet rs) throws SQLException {
        TradeLog log = new TradeLog();
        log.setId(rs.getLong("id"));
        log.setUserId(rs.getInt("user_id"));
        log.setSymbol(rs.getString("symbol"));
        log.setActionType(rs.getString("action_type"));
        log.setConfidenceScore(rs.getDouble("confidence_score"));
        log.setBriefReason(rs.getString("brief_reason"));
        log.setFullReason(rs.getString("full_reason"));
        log.setExecutedPrice(rs.getObject("executed_price", Double.class));
        log.setExecutedQty(rs.getObject("executed_qty", Double.class));
        log.setRealizedPnl(rs.getObject("realized_pnl", Double.class));
        log.setMarketSnapshot(rs.getString("market_snapshot"));
        log.setAgentName(rs.getString("agent_name"));
        log.setCreatedAt(rs.getString("created_at"));
        return log;
    }
    
    /**
     * 최근 거래 로그 조회 (Map 형태로 반환, REST API용)
     */
    public List<Map<String, Object>> findRecentLogs(Integer userId, int limit) {
        List<TradeLog> logs = findByUserId(userId, limit);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (TradeLog log : logs) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", log.getId());
            map.put("userId", log.getUserId());
            map.put("symbol", log.getSymbol());
            map.put("actionType", log.getActionType());
            map.put("confidenceScore", log.getConfidenceScore());
            map.put("briefReason", log.getBriefReason());
            map.put("fullReason", log.getFullReason());
            map.put("executedPrice", log.getExecutedPrice());
            map.put("executedQty", log.getExecutedQty());
            map.put("realizedPnl", log.getRealizedPnl());
            map.put("marketSnapshot", log.getMarketSnapshot());
            map.put("agentName", log.getAgentName());
            map.put("createdAt", log.getCreatedAt());
            result.add(map);
        }
        
        return result;
    }
    
    /**
     * 거래 로그 모델 (내부 클래스)
     */
    public static class TradeLog {
        private Long id;
        private Integer userId;
        private String symbol;
        private String actionType;
        private Double confidenceScore;
        private String briefReason;
        private String fullReason;
        private Double executedPrice;
        private Double executedQty;
        private Double realizedPnl;
        private String marketSnapshot;
        private String agentName;
        private String createdAt;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public String getActionType() { return actionType; }
        public void setActionType(String actionType) { this.actionType = actionType; }
        public Double getConfidenceScore() { return confidenceScore; }
        public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }
        public String getBriefReason() { return briefReason; }
        public void setBriefReason(String briefReason) { this.briefReason = briefReason; }
        public String getFullReason() { return fullReason; }
        public void setFullReason(String fullReason) { this.fullReason = fullReason; }
        public Double getExecutedPrice() { return executedPrice; }
        public void setExecutedPrice(Double executedPrice) { this.executedPrice = executedPrice; }
        public Double getExecutedQty() { return executedQty; }
        public void setExecutedQty(Double executedQty) { this.executedQty = executedQty; }
        public Double getRealizedPnl() { return realizedPnl; }
        public void setRealizedPnl(Double realizedPnl) { this.realizedPnl = realizedPnl; }
        public String getMarketSnapshot() { return marketSnapshot; }
        public void setMarketSnapshot(String marketSnapshot) { this.marketSnapshot = marketSnapshot; }
        public String getAgentName() { return agentName; }
        public void setAgentName(String agentName) { this.agentName = agentName; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }
}

