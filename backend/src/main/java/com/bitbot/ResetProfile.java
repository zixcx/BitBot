package com.bitbot;

import com.bitbot.database.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Statement;

/**
 * 프로필 및 설문조사 데이터 초기화
 */
public class ResetProfile {
    
    private static final Logger logger = LoggerFactory.getLogger(ResetProfile.class);
    
    public static void main(String[] args) {
        logger.info("=".repeat(80));
        logger.info("프로필 및 설문조사 데이터 초기화");
        logger.info("=".repeat(80));
        
        try {
            // 환경 설정 로드
            com.bitbot.utils.ConfigLoader.loadConfig();
            
            // 데이터베이스 연결
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement()) {
                
                logger.info("사용자 프로필 및 설문조사 데이터 삭제 중...");
                
                // 외래 키 제약 조건 일시 비활성화
                stmt.execute("PRAGMA foreign_keys = OFF");
                
                // 설문조사 데이터 삭제
                int questionnaireCount = stmt.executeUpdate("DELETE FROM questionnaires WHERE user_id = 1");
                logger.info("설문조사 데이터 삭제: {}개", questionnaireCount);
                
                // 사용자 프로필 삭제
                int profileCount = stmt.executeUpdate("DELETE FROM user_profiles WHERE user_id = 1");
                logger.info("사용자 프로필 삭제: {}개", profileCount);
                
                // 외래 키 제약 조건 재활성화
                stmt.execute("PRAGMA foreign_keys = ON");
                
                logger.info("✅ 초기화 완료!");
                logger.info("거래 내역은 유지되었습니다.");
                
            } catch (Exception e) {
                logger.error("초기화 실패", e);
                System.exit(1);
            }
            
        } catch (Exception e) {
            logger.error("오류 발생", e);
            System.exit(1);
        }
    }
}

