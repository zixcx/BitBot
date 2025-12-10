package com.bitbot.database;

import com.bitbot.classification.InvestorTypeClassifier;
import com.bitbot.models.InvestorType;
import com.bitbot.models.RiskSettings;
import com.bitbot.models.TradingStrategy;
import com.bitbot.models.UserProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * 사용자 프로필 데이터베이스 저장소
 */
public class UserProfileRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(UserProfileRepository.class);
    private final ObjectMapper objectMapper;
    
    public UserProfileRepository() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 프로필 저장
     */
    public void save(UserProfile profile) {
        // 먼저 사용자가 존재하는지 확인하고 없으면 생성
        UserRepository userRepo = new UserRepository();
        userRepo.getOrCreateUser(profile.getUserId());
        
        String sql = """
            INSERT OR REPLACE INTO user_profiles (
                user_id, investor_type, total_score, risk_settings,
                trading_strategy, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, profile.getUserId());
            stmt.setString(2, profile.getInvestorType().name());
            stmt.setInt(3, profile.getTotalScore());
            
            // RiskSettings를 JSON으로 변환
            String riskSettingsJson = objectMapper.writeValueAsString(profile.getRiskSettings());
            stmt.setString(4, riskSettingsJson);
            
            stmt.setString(5, profile.getTradingStrategy().name());
            stmt.setString(6, profile.getCreatedAt() != null ? 
                    profile.getCreatedAt().toString() : LocalDateTime.now().toString());
            stmt.setString(7, LocalDateTime.now().toString());
            
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                logger.info("사용자 프로필 저장 완료: {}", profile);
            }
            
        } catch (Exception e) {
            logger.error("사용자 프로필 저장 실패", e);
            throw new RuntimeException("프로필 저장 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 사용자 ID로 프로필 조회
     */
    public UserProfile findByUserId(Integer userId) {
        String sql = "SELECT * FROM user_profiles WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUserProfile(rs);
            }
            
        } catch (Exception e) {
            logger.error("사용자 프로필 조회 실패: userId={}", userId, e);
        }
        
        return null;
    }
    
    /**
     * 프로필 업데이트
     */
    public void update(UserProfile profile) {
        String sql = """
            UPDATE user_profiles SET
                investor_type = ?,
                total_score = ?,
                risk_settings = ?,
                trading_strategy = ?,
                updated_at = ?
            WHERE user_id = ?
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, profile.getInvestorType().name());
            stmt.setInt(2, profile.getTotalScore());
            
            String riskSettingsJson = objectMapper.writeValueAsString(profile.getRiskSettings());
            stmt.setString(3, riskSettingsJson);
            
            stmt.setString(4, profile.getTradingStrategy().name());
            stmt.setString(5, LocalDateTime.now().toString());
            stmt.setInt(6, profile.getUserId());
            
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                logger.info("사용자 프로필 업데이트 완료: {}", profile);
            }
            
        } catch (Exception e) {
            logger.error("사용자 프로필 업데이트 실패", e);
            throw new RuntimeException("프로필 업데이트 실패", e);
        }
    }
    
    private UserProfile mapResultSetToUserProfile(ResultSet rs) throws Exception {
        UserProfile profile = new UserProfile();
        profile.setId(rs.getLong("id"));
        profile.setUserId(rs.getInt("user_id"));
        profile.setInvestorType(InvestorType.valueOf(rs.getString("investor_type")));
        profile.setTotalScore(rs.getInt("total_score"));
        profile.setTradingStrategy(TradingStrategy.valueOf(rs.getString("trading_strategy")));
        
        // RiskSettings JSON 파싱
        String riskSettingsJson = rs.getString("risk_settings");
        if (riskSettingsJson != null) {
            RiskSettings riskSettings = objectMapper.readValue(riskSettingsJson, RiskSettings.class);
            
            // PostAction과 takeProfitPercent는 JSON에 저장되지 않았을 수 있으므로 복원
            InvestorTypeClassifier classifier = new InvestorTypeClassifier();
            RiskSettings fullRiskSettings = classifier.getRiskSettings(profile.getInvestorType());
            
            // takeProfitPercent가 0이면 (JSON에 없었던 경우) 복원
            if (riskSettings.getTakeProfitPercent() == 0.0) {
                riskSettings.setTakeProfitPercent(fullRiskSettings.getTakeProfitPercent());
            }
            
            // PostAction 복원
            riskSettings.setPostStopLossAction(fullRiskSettings.getPostStopLossAction());
            riskSettings.setPostTakeProfitAction(fullRiskSettings.getPostTakeProfitAction());
            
            profile.setRiskSettings(riskSettings);
        }
        
        // 날짜 파싱
        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null) {
            try {
                profile.setCreatedAt(LocalDateTime.parse(createdAtStr.replace(" ", "T")));
            } catch (Exception e) {
                logger.warn("날짜 파싱 실패: {}", createdAtStr);
            }
        }
        
        String updatedAtStr = rs.getString("updated_at");
        if (updatedAtStr != null) {
            try {
                profile.setUpdatedAt(LocalDateTime.parse(updatedAtStr.replace(" ", "T")));
            } catch (Exception e) {
                logger.warn("날짜 파싱 실패: {}", updatedAtStr);
            }
        }
        
        return profile;
    }
}

