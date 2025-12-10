package com.bitbot.database;

import com.bitbot.utils.ConfigLoader;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 데이터베이스 연결 관리자 (SQLite 및 MySQL 지원)
 * HikariCP를 사용한 커넥션 풀링
 */
public class DatabaseConnection {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    private static HikariDataSource dataSource;
    private static final String DB_PATH = "data/bitbot.db";
    
    static {
        initialize();
    }
    
    private static void initialize() {
        String dbType = ConfigLoader.getDbType();
        
        if ("mysql".equalsIgnoreCase(dbType)) {
            logger.info("MySQL 데이터베이스 모드로 초기화");
            initializeMySQL();
        } else {
            logger.info("SQLite 데이터베이스 모드로 초기화");
            initializeSQLite();
        }
    }
    
    private static void initializeSQLite() {
        try {
            // 데이터 디렉토리 생성
            java.io.File dataDir = new java.io.File("data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            
            HikariConfig config = new HikariConfig();
            
            // SQLite 연결 설정
            String dbUrl = "jdbc:sqlite:" + DB_PATH;
            config.setJdbcUrl(dbUrl);
            
            // SQLite 최적화 설정
            // WAL 모드에서는 여러 reader 연결 가능 (최대 3개 권장)
            config.setMaximumPoolSize(3);  // WAL 모드에서 여러 reader 허용
            config.setMinimumIdle(1);
            config.setConnectionTimeout(10000);  // 10초로 단축 (더 빠른 실패 감지)
            config.setIdleTimeout(300000);  // 5분
            config.setMaxLifetime(1800000);  // 30분
            config.setLeakDetectionThreshold(60000);  // 연결 누수 감지 (60초)
            
            // SQLite 특화 설정
            config.addDataSourceProperty("journal_mode", "WAL");  // Write-Ahead Logging (여러 reader 허용)
            config.addDataSourceProperty("synchronous", "NORMAL");
            config.addDataSourceProperty("foreign_keys", "true");
            config.addDataSourceProperty("busy_timeout", "10000");  // 10초로 증가
            config.addDataSourceProperty("cache_size", "-64000");  // 64MB 캐시
            config.addDataSourceProperty("temp_store", "MEMORY");  // 임시 테이블을 메모리에 저장
            
            dataSource = new HikariDataSource(config);
            
            logger.info("SQLite 데이터베이스 커넥션 풀 초기화 완료: {}", DB_PATH);
            
            // 연결 테스트 및 스키마 초기화
            try (Connection conn = dataSource.getConnection()) {
                logger.info("SQLite 데이터베이스 연결 테스트 성공");
                SchemaInitializer.initialize(conn);
            }
            
        } catch (Exception e) {
            logger.error("SQLite 데이터베이스 초기화 실패", e);
            throw new RuntimeException("데이터베이스 연결 실패", e);
        }
    }
    
    private static void initializeMySQL() {
        try {
            HikariConfig config = new HikariConfig();
            
            String host = ConfigLoader.getMysqlHost();
            int port = ConfigLoader.getMysqlPort();
            String database = ConfigLoader.getMysqlDatabase();
            String username = ConfigLoader.getMysqlUsername();
            String password = ConfigLoader.getMysqlPassword();
            
            if (password == null || password.isEmpty()) {
                throw new RuntimeException("MYSQL_PASSWORD 환경 변수가 설정되지 않았습니다.");
            }
            
            // MySQL JDBC URL
            String dbUrl = String.format(
                "jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8",
                host, port, database
            );
            
            config.setJdbcUrl(dbUrl);
            config.setUsername(username);
            config.setPassword(password);
            
            // MySQL 최적화 설정
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);  // 30초
            config.setIdleTimeout(600000);  // 10분
            config.setMaxLifetime(1800000);  // 30분
            config.setLeakDetectionThreshold(60000);
            
            // MySQL 특화 설정
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            
            dataSource = new HikariDataSource(config);
            
            logger.info("MySQL 데이터베이스 커넥션 풀 초기화 완료: {}:{}/{}", host, port, database);
            
            // 연결 테스트 및 스키마 초기화
            try (Connection conn = dataSource.getConnection()) {
                logger.info("MySQL 데이터베이스 연결 테스트 성공");
                SchemaInitializer.initialize(conn);
            }
            
        } catch (Exception e) {
            logger.error("MySQL 데이터베이스 초기화 실패", e);
            throw new RuntimeException("MySQL 연결 실패: " + e.getMessage(), e);
        }
    }
    
    
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            initialize();
        }
        return dataSource.getConnection();
    }
    
    public static DataSource getDataSource() {
        if (dataSource == null) {
            initialize();
        }
        return dataSource;
    }
    
    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("데이터베이스 커넥션 풀 종료");
        }
    }
    
    /**
     * 데이터베이스 연결 상태 확인
     */
    public static boolean isHealthy() {
        try (Connection conn = getConnection()) {
            return conn.isValid(5);
        } catch (SQLException e) {
            logger.error("데이터베이스 연결 상태 확인 실패", e);
            return false;
        }
    }
}


