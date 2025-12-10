package com.bitbot.database;

import com.bitbot.models.TradeOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 거래 내역 데이터베이스 저장소
 */
public class TradeRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(TradeRepository.class);
    
    /**
     * 거래 내역 저장 (트랜잭션 관리 포함)
     */
    public Long save(TradeOrder order, String userId) {
        String sql = """
            INSERT INTO trades (
                user_id, symbol, order_type, order_status, quantity, price,
                executed_price, total_cost, leverage, is_futures_trade,
                decision_reason, agent_name, confidence, binance_order_id, executed_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            
            // 트랜잭션 시작
            conn.setAutoCommit(false);
            
            Integer userIdInt = userId != null ? Integer.parseInt(userId) : null;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, userIdInt);
                stmt.setString(2, order.getSymbol());
                stmt.setString(3, order.getType().name());
                stmt.setString(4, order.getStatus().name());
                stmt.setDouble(5, order.getQuantity());
                stmt.setDouble(6, order.getPrice());
                stmt.setDouble(7, order.getExecutedPrice());
                stmt.setDouble(8, order.getTotalCost());
                stmt.setInt(9, order.getLeverage() > 0 ? order.getLeverage() : 1);
                stmt.setInt(10, order.isFuturesTrade() ? 1 : 0);
                stmt.setString(11, order.getReason());
                stmt.setString(12, order.getDecision() != null ? order.getDecision().getAgentName() : null);
                stmt.setDouble(13, order.getDecision() != null ? order.getDecision().getConfidence() : 0);
                stmt.setString(14, order.getBinanceOrderId());
                stmt.setString(15, order.getExecutedAt() != null ? 
                        order.getExecutedAt().toString() : null);
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    // SQLite는 getGeneratedKeys()를 지원하지 않으므로 last_insert_rowid() 사용
                    try (Statement idStmt = conn.createStatement();
                         ResultSet rs = idStmt.executeQuery("SELECT last_insert_rowid()")) {
                        if (rs.next()) {
                            Long id = rs.getLong(1);
                            order.setId(id);
                            
                            // 트랜잭션 커밋
                            conn.commit();
                            logger.info("거래 내역 저장 완료: ID={}, {}", id, order);
                            return id;
                        }
                    }
                }
            }
            
            // 커밋 (정상 완료 시)
            conn.commit();
            return null;
            
        } catch (SQLException e) {
            // 롤백
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.warn("거래 내역 저장 실패 - 롤백 완료");
                } catch (SQLException rollbackEx) {
                    logger.error("롤백 실패", rollbackEx);
                }
            }
            logger.error("거래 내역 저장 실패", e);
            return null;
        } catch (Exception e) {
            // 롤백
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.warn("거래 내역 저장 실패 - 롤백 완료");
                } catch (SQLException rollbackEx) {
                    logger.error("롤백 실패", rollbackEx);
                }
            }
            logger.error("거래 내역 저장 중 예상치 못한 오류", e);
            return null;
        } finally {
            // AutoCommit 복원
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    logger.error("AutoCommit 복원 실패", e);
                }
            }
        }
    }
    
    /**
     * 최근 거래 내역 조회
     */
    public List<TradeOrder> findRecentTrades(String userId, int limit) {
        String sql = """
            SELECT * FROM trades 
            WHERE user_id = ?
            ORDER BY created_at DESC 
            LIMIT ?
            """;
        
        List<TradeOrder> trades = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            Integer userIdInt = userId != null ? Integer.parseInt(userId) : null;
            stmt.setObject(1, userIdInt);
            stmt.setInt(2, limit);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                trades.add(mapResultSetToTradeOrder(rs));
            }
            
        } catch (SQLException e) {
            logger.error("거래 내역 조회 실패", e);
        }
        
        return trades;
    }
    
    /**
     * 거래 상태 업데이트
     */
    public boolean updateStatus(Long orderId, TradeOrder.OrderStatus newStatus) {
        String sql = "UPDATE trades SET order_status = ?, executed_at = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, newStatus.name());
            stmt.setString(2, java.time.LocalDateTime.now().toString());
            stmt.setLong(3, orderId);
            
            int rows = stmt.executeUpdate();
            return rows > 0;
            
        } catch (SQLException e) {
            logger.error("거래 상태 업데이트 실패", e);
            return false;
        }
    }
    
    private TradeOrder mapResultSetToTradeOrder(ResultSet rs) throws SQLException {
        TradeOrder order = new TradeOrder();
        order.setId(rs.getLong("id"));
        order.setSymbol(rs.getString("symbol"));
        order.setType(TradeOrder.OrderType.valueOf(rs.getString("order_type")));
        order.setStatus(TradeOrder.OrderStatus.valueOf(rs.getString("order_status")));
        order.setQuantity(rs.getDouble("quantity"));
        order.setPrice(rs.getDouble("price"));
        order.setExecutedPrice(rs.getDouble("executed_price"));
        order.setTotalCost(rs.getDouble("total_cost"));
        
        // 레버리지 정보 (기본값: 1)
        try {
            order.setLeverage(rs.getInt("leverage"));
        } catch (SQLException e) {
            order.setLeverage(1);  // 컬럼이 없으면 기본값 1
        }
        
        try {
            order.setFuturesTrade(rs.getInt("is_futures_trade") == 1);
        } catch (SQLException e) {
            order.setFuturesTrade(false);  // 컬럼이 없으면 기본값 false
        }
        
        order.setReason(rs.getString("decision_reason"));
        order.setBinanceOrderId(rs.getString("binance_order_id"));
        
        // SQLite는 TEXT로 저장되므로 문자열 파싱
        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null) {
            try {
                order.setCreatedAt(java.time.LocalDateTime.parse(createdAtStr.replace(" ", "T")));
            } catch (Exception e) {
                logger.warn("날짜 파싱 실패: {}", createdAtStr);
            }
        }
        
        String executedAtStr = rs.getString("executed_at");
        if (executedAtStr != null) {
            try {
                order.setExecutedAt(java.time.LocalDateTime.parse(executedAtStr.replace(" ", "T")));
            } catch (Exception e) {
                logger.warn("날짜 파싱 실패: {}", executedAtStr);
            }
        }
        
        return order;
    }
}

