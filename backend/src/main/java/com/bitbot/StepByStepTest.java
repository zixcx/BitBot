package com.bitbot;

import com.bitbot.database.DatabaseConnection;
import com.bitbot.database.TradeRepository;
import com.bitbot.database.TradeLogRepository;
import com.bitbot.database.UserProfileRepository;
import com.bitbot.models.UserProfile;
import com.bitbot.models.TradeOrder;
import com.bitbot.utils.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.List;

/**
 * 단계별 시스템 테스트
 */
public class StepByStepTest {
    
    private static final Logger logger = LoggerFactory.getLogger(StepByStepTest.class);
    
    public static void main(String[] args) {
        logger.info("=".repeat(80));
        logger.info("단계별 시스템 테스트 시작");
        logger.info("=".repeat(80));
        
        try {
            // 환경 설정 로드
            ConfigLoader.loadConfig();
            logger.info("✅ 1단계: 환경 설정 로드 완료");
            
            // 2단계: 데이터베이스 연결 테스트
            testDatabaseConnection();
            
            // 3단계: 데이터베이스 스키마 확인
            testDatabaseSchema();
            
            // 4단계: 프로필 조회 테스트
            testProfileRetrieval();
            
            // 5단계: 거래 내역 조회 테스트
            testTradeRetrieval();
            
            // 6단계: 거래 로그 조회 테스트
            testTradeLogRetrieval();
            
            logger.info("\n" + "=".repeat(80));
            logger.info("✅ 모든 단계 테스트 완료!");
            logger.info("=".repeat(80));
            
        } catch (Exception e) {
            logger.error("❌ 테스트 실패", e);
            System.exit(1);
        }
    }
    
    private static void testDatabaseConnection() {
        logger.info("\n[2단계] 데이터베이스 연결 테스트...");
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                logger.info("✅ 데이터베이스 연결 성공");
                conn.close();
            } else {
                throw new Exception("데이터베이스 연결 실패");
            }
        } catch (Exception e) {
            logger.error("❌ 데이터베이스 연결 실패: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    private static void testDatabaseSchema() {
        logger.info("\n[3단계] 데이터베이스 스키마 확인...");
        try {
            Connection conn = DatabaseConnection.getConnection();
            java.sql.DatabaseMetaData metaData = conn.getMetaData();
            
            // 주요 테이블 확인
            String[] tables = {"users", "user_profiles", "questionnaires", "trades", "trade_logs"};
            for (String table : tables) {
                java.sql.ResultSet rs = metaData.getTables(null, null, table, null);
                if (rs.next()) {
                    logger.info("  ✅ 테이블 '{}' 존재", table);
                } else {
                    logger.warn("  ⚠️ 테이블 '{}' 없음", table);
                }
            }
            conn.close();
            logger.info("✅ 데이터베이스 스키마 확인 완료");
        } catch (Exception e) {
            logger.error("❌ 스키마 확인 실패: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    private static void testProfileRetrieval() {
        logger.info("\n[4단계] 프로필 조회 테스트...");
        try {
            UserProfileRepository repo = new UserProfileRepository();
            UserProfile profile = repo.findByUserId(1);
            
            if (profile != null) {
                logger.info("✅ 프로필 조회 성공:");
                logger.info("  - 투자 성향: {}", profile.getInvestorType().getKoreanName());
                logger.info("  - 전략: {}", profile.getTradingStrategy().getKoreanName());
                logger.info("  - 점수: {}", profile.getTotalScore());
            } else {
                logger.warn("⚠️ 프로필 없음 (설문조사 필요)");
            }
        } catch (Exception e) {
            logger.error("❌ 프로필 조회 실패: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    private static void testTradeRetrieval() {
        logger.info("\n[5단계] 거래 내역 조회 테스트...");
        try {
            TradeRepository repo = new TradeRepository();
            List<TradeOrder> trades = repo.findRecentTrades("1", 10);
            
            logger.info("✅ 거래 내역 조회 성공: {}개", trades.size());
            if (!trades.isEmpty()) {
                TradeOrder latest = trades.get(0);
                logger.info("  - 최근 거래: {} {} @ ${}", 
                    latest.getType(), 
                    latest.getQuantity(), 
                    latest.getExecutedPrice());
            }
        } catch (Exception e) {
            logger.error("❌ 거래 내역 조회 실패: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    private static void testTradeLogRetrieval() {
        logger.info("\n[6단계] 거래 로그 조회 테스트...");
        try {
            TradeLogRepository repo = new TradeLogRepository();
            List<TradeLogRepository.TradeLog> logs = repo.findByUserId(1, 10);
            
            logger.info("✅ 거래 로그 조회 성공: {}개", logs.size());
            if (!logs.isEmpty()) {
                TradeLogRepository.TradeLog latest = logs.get(0);
                logger.info("  - 최근 로그: {} (신뢰도: {})", 
                    latest.getActionType(), 
                    latest.getConfidenceScore());
            }
        } catch (Exception e) {
            logger.error("❌ 거래 로그 조회 실패: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}

