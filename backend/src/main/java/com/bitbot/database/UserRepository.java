package com.bitbot.database;

import com.bitbot.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * 사용자 데이터베이스 저장소
 */
public class UserRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);
    
    /**
     * 사용자 ID로 사용자 존재 여부 확인
     */
    public boolean exists(Integer userId) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (Exception e) {
            logger.error("사용자 존재 확인 실패: userId={}", userId, e);
        }
        
        return false;
    }
    
    /**
     * 기본 사용자 생성 (ID가 없을 경우)
     */
    public Integer createDefaultUser(Integer userId) {
        // 이미 존재하면 그대로 반환
        if (exists(userId)) {
            return userId;
        }
        
        String sql = """
            INSERT INTO users (id, email, username, password_hash, trading_enabled, risk_management_enabled)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String email = "user" + userId + "@bitbot.local";
            String username = "user" + userId;
            String passwordHash = "default";  // 실제로는 해시된 비밀번호
            
            stmt.setInt(1, userId);
            stmt.setString(2, email);
            stmt.setString(3, username);
            stmt.setString(4, passwordHash);
            stmt.setInt(5, 1);  // trading_enabled
            stmt.setInt(6, 1);  // risk_management_enabled
            
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                logger.info("기본 사용자 생성 완료: userId={}", userId);
                return userId;
            }
            
        } catch (Exception e) {
            logger.error("기본 사용자 생성 실패: userId={}", userId, e);
        }
        
        return null;
    }
    
    /**
     * 사용자 생성 또는 조회 (없으면 생성)
     */
    public Integer getOrCreateUser(Integer userId) {
        if (exists(userId)) {
            return userId;
        }
        return createDefaultUser(userId);
    }
    
    /**
     * 사용자 저장
     * @param user 사용자 객체
     * @return 생성된 사용자 ID
     */
    public Integer save(User user) {
        String sql = """
            INSERT INTO users (email, username, password_hash, 
                             binance_api_key_encrypted, binance_secret_key_encrypted,
                             trading_enabled, risk_management_enabled, max_investment_percent)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // 데이터베이스 타입 확인
            boolean isMySQL = isMySQL(conn);
            
            try (PreparedStatement stmt = isMySQL 
                    ? conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
                    : conn.prepareStatement(sql)) {
                
                stmt.setString(1, user.getEmail());
                stmt.setString(2, user.getUsername());
                stmt.setString(3, user.getPasswordHash());
                stmt.setString(4, user.getBinanceApiKeyEncrypted());
                stmt.setString(5, user.getBinanceSecretKeyEncrypted());
                stmt.setInt(6, user.isTradingEnabled() ? 1 : 0);
                stmt.setInt(7, user.isRiskManagementEnabled() ? 1 : 0);
                stmt.setDouble(8, user.getMaxInvestmentPercent());
                
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    Integer userId = null;
                    
                    if (isMySQL) {
                        // MySQL: getGeneratedKeys() 사용
                        try (ResultSet rs = stmt.getGeneratedKeys()) {
                            if (rs.next()) {
                                userId = rs.getInt(1);
                            }
                        }
                    } else {
                        // SQLite: last_insert_rowid() 사용
                        try (Statement idStmt = conn.createStatement();
                             ResultSet rs = idStmt.executeQuery("SELECT last_insert_rowid()")) {
                            if (rs.next()) {
                                userId = rs.getInt(1);
                            }
                        }
                    }
                    
                    if (userId != null) {
                        user.setId(userId);
                        conn.commit();
                        logger.info("사용자 저장 완료: userId={}, email={}", userId, user.getEmail());
                        return userId;
                    }
                }
            }
            
            conn.rollback();
            logger.error("사용자 저장 실패: 생성된 ID를 가져올 수 없습니다.");
            return null;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("롤백 실패", ex);
                }
            }
            logger.error("사용자 저장 실패", e);
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
     * 데이터베이스 타입 확인
     */
    private boolean isMySQL(Connection conn) throws SQLException {
        String dbProductName = conn.getMetaData().getDatabaseProductName().toLowerCase();
        return dbProductName.contains("mysql");
    }
    
    /**
     * 이메일로 사용자 조회
     * @param email 이메일
     * @return 사용자 객체 (없으면 null)
     */
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.error("사용자 조회 실패: email={}", email, e);
        }
        
        return null;
    }
    
    /**
     * ID로 사용자 조회
     * @param userId 사용자 ID
     * @return 사용자 객체 (없으면 null)
     */
    public User findById(Integer userId) {
        String sql = "SELECT * FROM users WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.error("사용자 조회 실패: userId={}", userId, e);
        }
        
        return null;
    }
    
    /**
     * ResultSet을 User 객체로 변환
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setBinanceApiKeyEncrypted(rs.getString("binance_api_key_encrypted"));
        user.setBinanceSecretKeyEncrypted(rs.getString("binance_secret_key_encrypted"));
        user.setTradingEnabled(rs.getInt("trading_enabled") == 1);
        user.setRiskManagementEnabled(rs.getInt("risk_management_enabled") == 1);
        user.setMaxInvestmentPercent(rs.getDouble("max_investment_percent"));
        return user;
    }
}

