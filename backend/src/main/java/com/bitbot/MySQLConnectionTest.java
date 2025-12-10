package com.bitbot;

import com.bitbot.database.DatabaseConnection;
import com.bitbot.utils.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

/**
 * MySQL 연결 테스트
 */
public class MySQLConnectionTest {
    
    private static final Logger logger = LoggerFactory.getLogger(MySQLConnectionTest.class);
    
    public static void main(String[] args) {
        logger.info("=".repeat(60));
        logger.info("MySQL 연결 테스트 시작");
        logger.info("=".repeat(60));
        
        try {
            // 환경 변수 로드
            ConfigLoader.loadConfig();
            logger.info("✅ 환경 변수 로드 완료");
            
            // MySQL 설정 확인
            String dbType = ConfigLoader.getDbType();
            String host = ConfigLoader.getMysqlHost();
            int port = ConfigLoader.getMysqlPort();
            String database = ConfigLoader.getMysqlDatabase();
            String username = ConfigLoader.getMysqlUsername();
            
            logger.info("\n[MySQL 설정]");
            logger.info("DB_TYPE: {}", dbType);
            logger.info("Host: {}", host);
            logger.info("Port: {}", port);
            logger.info("Database: {}", database);
            logger.info("Username: {}", username);
            logger.info("Password: {} (보안상 표시 안 함)", 
                    ConfigLoader.getMysqlPassword() != null ? "설정됨" : "설정 안 됨");
            
            // 데이터베이스 연결 테스트
            logger.info("\n[연결 테스트]");
            Connection conn = DatabaseConnection.getConnection();
            
            if (conn != null && !conn.isClosed()) {
                logger.info("✅ MySQL 연결 성공!");
                
                // 데이터베이스 정보 확인
                DatabaseMetaData metaData = conn.getMetaData();
                logger.info("\n[데이터베이스 정보]");
                logger.info("Product Name: {}", metaData.getDatabaseProductName());
                logger.info("Product Version: {}", metaData.getDatabaseProductVersion());
                logger.info("Driver Name: {}", metaData.getDriverName());
                logger.info("Driver Version: {}", metaData.getDriverVersion());
                logger.info("URL: {}", metaData.getURL());
                logger.info("Username: {}", metaData.getUserName());
                
                // 테이블 목록 확인
                logger.info("\n[테이블 목록]");
                try (ResultSet tables = metaData.getTables(database, null, "%", new String[]{"TABLE"})) {
                    int count = 0;
                    while (tables.next()) {
                        String tableName = tables.getString("TABLE_NAME");
                        logger.info("  - {}", tableName);
                        count++;
                    }
                    if (count == 0) {
                        logger.info("  (테이블 없음 - 스키마 초기화 필요)");
                    } else {
                        logger.info("\n총 {}개 테이블 발견", count);
                    }
                }
                
                conn.close();
                logger.info("\n✅ 연결 테스트 완료!");
                
            } else {
                logger.error("❌ MySQL 연결 실패: 연결 객체가 null이거나 닫혀있음");
            }
            
        } catch (Exception e) {
            logger.error("❌ MySQL 연결 테스트 실패", e);
            System.exit(1);
        }
        
        logger.info("\n" + "=".repeat(60));
    }
}

