package com.bitbot.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 데이터베이스 스키마 초기화 유틸리티 (SQLite 및 MySQL 지원)
 */
public class SchemaInitializer {
    
    private static final Logger logger = LoggerFactory.getLogger(SchemaInitializer.class);
    private static boolean initialized = false;
    
    /**
     * 데이터베이스 타입 확인
     */
    private static boolean isMySQL(Connection conn) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        String dbProductName = metaData.getDatabaseProductName().toLowerCase();
        return dbProductName.contains("mysql");
    }
    
    /**
     * 스키마 초기화 (한 번만 실행)
     */
    public static synchronized void initialize(Connection conn) {
        if (initialized) {
            return;
        }
        
        try {
            boolean isMySQL = isMySQL(conn);
            logger.info("데이터베이스 타입: {}", isMySQL ? "MySQL" : "SQLite");
            
            // SQLite 전용: FOREIGN KEY 제약조건 일시 비활성화
            if (!isMySQL) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = OFF");
                }
            }
            
            // 각 테이블 생성
            createUsersTable(conn);
            createUserProfilesTable(conn);
            createQuestionnairesTable(conn);
            createTradesTable(conn);
            createTradeLogsTable(conn);  // PRD 요구사항: HOLD 포함 모든 판단 기록
            createLlmLogsTable(conn);
            createPortfolioSnapshotsTable(conn);
            createMarketDataCacheTable(conn);
            createSystemEventsTable(conn);
            
            // 인덱스 생성
            createIndexes(conn);
            
            // 트리거 생성
            createTriggers(conn);
            
            // SQLite 전용: FOREIGN KEY 제약조건 다시 활성화
            if (!isMySQL) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON");
                }
            }
            
            initialized = true;
            logger.info("✅ 데이터베이스 스키마 초기화 완료");
            
        } catch (Exception e) {
            logger.error("스키마 초기화 실패", e);
            try {
                if (!isMySQL(conn)) {
                    conn.createStatement().execute("PRAGMA foreign_keys = ON");
                }
            } catch (SQLException ignored) {}
        }
    }
    
    /**
     * SQLite와 MySQL 호환 SQL 생성 헬퍼
     */
    private static String getPrimaryKeyType(boolean isMySQL) {
        return isMySQL ? "BIGINT AUTO_INCREMENT PRIMARY KEY" : "INTEGER PRIMARY KEY AUTOINCREMENT";
    }
    
    private static String getTextType(boolean isMySQL) {
        return isMySQL ? "VARCHAR(255)" : "TEXT";
    }
    
    private static String getLongTextType(boolean isMySQL) {
        return isMySQL ? "TEXT" : "TEXT";
    }
    
    private static String getRealType(boolean isMySQL) {
        return isMySQL ? "DECIMAL(20, 8)" : "REAL";
    }
    
    private static String getTimestampDefault(boolean isMySQL) {
        return isMySQL ? "CURRENT_TIMESTAMP" : "(datetime('now'))";
    }
    
    private static void createUsersTable(Connection conn) throws SQLException {
        boolean isMySQL = isMySQL(conn);
        String sql = String.format("""
            CREATE TABLE IF NOT EXISTS users (
                id %s,
                email %s UNIQUE NOT NULL,
                username %s UNIQUE NOT NULL,
                password_hash %s NOT NULL,
                binance_api_key_encrypted %s,
                binance_secret_key_encrypted %s,
                trading_enabled INTEGER DEFAULT 0,
                risk_management_enabled INTEGER DEFAULT 1,
                max_investment_percent %s DEFAULT 10.00,
                created_at %s DEFAULT %s,
                updated_at %s DEFAULT %s
            )
            """,
            getPrimaryKeyType(isMySQL),
            getTextType(isMySQL),
            getTextType(isMySQL),
            getTextType(isMySQL),
            getLongTextType(isMySQL),
            getLongTextType(isMySQL),
            getRealType(isMySQL),
            isMySQL ? "TIMESTAMP" : "TEXT",
            getTimestampDefault(isMySQL),
            isMySQL ? "TIMESTAMP" : "TEXT",
            getTimestampDefault(isMySQL)
        );
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.debug("users 테이블 생성 완료");
        }
    }
    
    private static void createUserProfilesTable(Connection conn) throws SQLException {
        boolean isMySQL = isMySQL(conn);
        String sql = String.format("""
            CREATE TABLE IF NOT EXISTS user_profiles (
                id %s,
                user_id BIGINT NOT NULL,
                investor_type %s NOT NULL,
                total_score INT NOT NULL,
                risk_settings %s NOT NULL,
                trading_strategy %s NOT NULL,
                created_at %s DEFAULT %s,
                updated_at %s DEFAULT %s,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                UNIQUE(user_id)
            )
            """,
            getPrimaryKeyType(isMySQL),
            getTextType(isMySQL),
            getLongTextType(isMySQL),
            getTextType(isMySQL),
            isMySQL ? "TIMESTAMP" : "TEXT",
            getTimestampDefault(isMySQL),
            isMySQL ? "TIMESTAMP" : "TEXT",
            getTimestampDefault(isMySQL)
        );
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.debug("user_profiles 테이블 생성 완료");
        }
    }
    
    private static void createQuestionnairesTable(Connection conn) throws SQLException {
        boolean isMySQL = isMySQL(conn);
        String sql = String.format("""
            CREATE TABLE IF NOT EXISTS questionnaires (
                id %s,
                user_id BIGINT NOT NULL,
                answers %s NOT NULL,
                total_score INT NOT NULL,
                result_type %s NOT NULL,
                completed_at %s DEFAULT %s,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
            """,
            getPrimaryKeyType(isMySQL),
            getLongTextType(isMySQL),
            getTextType(isMySQL),
            isMySQL ? "TIMESTAMP" : "TEXT",
            getTimestampDefault(isMySQL)
        );
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.debug("questionnaires 테이블 생성 완료");
        }
    }
    
    private static void createTradesTable(Connection conn) throws SQLException {
        boolean isMySQL = isMySQL(conn);
        String sql = String.format("""
            CREATE TABLE IF NOT EXISTS trades (
                id %s,
                user_id BIGINT,
                symbol %s NOT NULL DEFAULT 'BTCUSDT',
                order_type %s NOT NULL,
                order_status %s NOT NULL,
                quantity %s NOT NULL,
                price %s NOT NULL,
                executed_price %s,
                total_cost %s,
                leverage INT DEFAULT 1,
                is_futures_trade INT DEFAULT 0,
                profit_loss %s,
                profit_loss_percent %s,
                decision_reason %s,
                agent_name %s,
                confidence %s,
                binance_order_id %s,
                created_at %s DEFAULT %s,
                executed_at %s,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
            """,
            getPrimaryKeyType(isMySQL),
            getTextType(isMySQL),
            getTextType(isMySQL),
            getTextType(isMySQL),
            getRealType(isMySQL),
            getRealType(isMySQL),
            getRealType(isMySQL),
            getRealType(isMySQL),
            getRealType(isMySQL),
            getRealType(isMySQL),
            getLongTextType(isMySQL),
            getTextType(isMySQL),
            getRealType(isMySQL),
            getTextType(isMySQL),
            isMySQL ? "TIMESTAMP" : "TEXT",
            getTimestampDefault(isMySQL),
            isMySQL ? "TIMESTAMP" : "TEXT"
        );
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.debug("trades 테이블 생성 완료");
            
            // 기존 테이블에 레버리지 컬럼 추가 (마이그레이션)
            try {
                String alterSql = isMySQL 
                    ? "ALTER TABLE trades ADD COLUMN leverage INT DEFAULT 1"
                    : "ALTER TABLE trades ADD COLUMN leverage INTEGER DEFAULT 1";
                stmt.execute(alterSql);
                logger.debug("trades 테이블에 leverage 컬럼 추가 완료");
            } catch (SQLException e) {
                // 컬럼이 이미 존재하면 무시
                logger.debug("leverage 컬럼 추가 건너뛰기: {}", e.getMessage());
            }
            
            try {
                String alterSql = isMySQL
                    ? "ALTER TABLE trades ADD COLUMN is_futures_trade INT DEFAULT 0"
                    : "ALTER TABLE trades ADD COLUMN is_futures_trade INTEGER DEFAULT 0";
                stmt.execute(alterSql);
                logger.debug("trades 테이블에 is_futures_trade 컬럼 추가 완료");
            } catch (SQLException e) {
                // 컬럼이 이미 존재하면 무시
                logger.debug("is_futures_trade 컬럼 추가 건너뛰기: {}", e.getMessage());
            }
        }
    }
    
    private static void createTradeLogsTable(Connection conn) throws SQLException {
        boolean isMySQL = isMySQL(conn);
        String sql = String.format("""
            CREATE TABLE IF NOT EXISTS trade_logs (
                id %s,
                user_id BIGINT NOT NULL,
                symbol %s NOT NULL DEFAULT 'BTCUSDT',
                action_type %s NOT NULL,
                confidence_score %s,
                brief_reason %s,
                full_reason %s,
                executed_price %s NULL,
                executed_qty %s NULL,
                realized_pnl %s NULL,
                market_snapshot %s,
                agent_name %s,
                created_at %s DEFAULT %s,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
            """,
            getPrimaryKeyType(isMySQL),
            getTextType(isMySQL),
            getTextType(isMySQL),
            getRealType(isMySQL),
            getLongTextType(isMySQL),
            getLongTextType(isMySQL),
            getRealType(isMySQL),
            getRealType(isMySQL),
            getRealType(isMySQL),
            getLongTextType(isMySQL),
            getTextType(isMySQL),
            isMySQL ? "TIMESTAMP" : "TEXT",
            getTimestampDefault(isMySQL)
        );
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.debug("trade_logs 테이블 생성 완료");
        }
    }
    
    private static void createLlmLogsTable(Connection conn) throws SQLException {
        boolean isMySQL = isMySQL(conn);
        String sql = String.format("""
            CREATE TABLE IF NOT EXISTS llm_analysis_logs (
                id %s,
                user_id BIGINT,
                agent_name %s NOT NULL,
                request_prompt %s NOT NULL,
                response_raw %s NOT NULL,
                response_parsed %s,
                decision %s,
                confidence %s,
                reason %s,
                market_data_snapshot %s,
                llm_provider %s DEFAULT 'gemini',
                tokens_used INT,
                response_time_ms INT,
                action_taken %s,
                trade_id %s,
                created_at %s DEFAULT %s,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY (trade_id) REFERENCES trades(id) ON DELETE SET NULL
            )
            """,
            getPrimaryKeyType(isMySQL),
            getTextType(isMySQL),
            getLongTextType(isMySQL),
            getLongTextType(isMySQL),
            getLongTextType(isMySQL),
            getTextType(isMySQL),
            getRealType(isMySQL),
            getLongTextType(isMySQL),
            getLongTextType(isMySQL),
            getTextType(isMySQL),
            getTextType(isMySQL),
            isMySQL ? "BIGINT" : "INTEGER",
            isMySQL ? "TIMESTAMP" : "TEXT",
            getTimestampDefault(isMySQL)
        );
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.debug("llm_analysis_logs 테이블 생성 완료");
        }
    }
    
    private static void createPortfolioSnapshotsTable(Connection conn) throws SQLException {
        boolean isMySQL = isMySQL(conn);
        String sql = String.format("""
            CREATE TABLE IF NOT EXISTS portfolio_snapshots (
                id %s,
                user_id BIGINT NOT NULL,
                total_balance %s NOT NULL,
                available_balance %s NOT NULL,
                invested_amount %s NOT NULL,
                btc_holding %s NOT NULL DEFAULT 0,
                btc_value %s NOT NULL DEFAULT 0,
                total_profit_loss %s NOT NULL DEFAULT 0,
                profit_loss_percent %s NOT NULL DEFAULT 0,
                total_trades INT NOT NULL DEFAULT 0,
                winning_trades INT NOT NULL DEFAULT 0,
                losing_trades INT NOT NULL DEFAULT 0,
                created_at %s DEFAULT %s,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
            """,
            getPrimaryKeyType(isMySQL),
            getRealType(isMySQL),
            getRealType(isMySQL),
            getRealType(isMySQL),
            getRealType(isMySQL),
            getRealType(isMySQL),
            getRealType(isMySQL),
            getRealType(isMySQL),
            isMySQL ? "TIMESTAMP" : "TEXT",
            getTimestampDefault(isMySQL)
        );
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.debug("portfolio_snapshots 테이블 생성 완료");
        }
    }
    
    private static void createMarketDataCacheTable(Connection conn) throws SQLException {
        boolean isMySQL = isMySQL(conn);
        String sql = String.format("""
            CREATE TABLE IF NOT EXISTS market_data_cache (
                id %s,
                symbol %s NOT NULL DEFAULT 'BTCUSDT',
                timestamp %s NOT NULL,
                open_price %s NOT NULL,
                high_price %s NOT NULL,
                low_price %s NOT NULL,
                close_price %s NOT NULL,
                volume %s NOT NULL,
                rsi %s,
                macd %s,
                macd_signal %s,
                ma_short %s,
                ma_long %s,
                bollinger_upper %s,
                bollinger_middle %s,
                bollinger_lower %s,
                created_at %s DEFAULT %s,
                UNIQUE(symbol, timestamp)
            )
            """,
            getPrimaryKeyType(isMySQL),
            getTextType(isMySQL),
            getTextType(isMySQL),
            getRealType(isMySQL),
            getRealType(isMySQL),
            getRealType(isMySQL),
            getRealType(isMySQL),
            getRealType(isMySQL),
            getRealType(isMySQL),
            getRealType(isMySQL),
            getRealType(isMySQL),
            getRealType(isMySQL),
            getRealType(isMySQL),
            getRealType(isMySQL),
            getRealType(isMySQL),
            getRealType(isMySQL),
            isMySQL ? "TIMESTAMP" : "TEXT",
            getTimestampDefault(isMySQL)
        );
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.debug("market_data_cache 테이블 생성 완료");
        }
    }
    
    private static void createSystemEventsTable(Connection conn) throws SQLException {
        boolean isMySQL = isMySQL(conn);
        String sql = String.format("""
            CREATE TABLE IF NOT EXISTS system_events (
                id %s,
                event_type %s NOT NULL,
                event_message %s NOT NULL,
                event_details %s,
                user_id BIGINT,
                created_at %s DEFAULT %s,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
            )
            """,
            getPrimaryKeyType(isMySQL),
            getTextType(isMySQL),
            getLongTextType(isMySQL),
            getLongTextType(isMySQL),
            isMySQL ? "TIMESTAMP" : "TEXT",
            getTimestampDefault(isMySQL)
        );
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.debug("system_events 테이블 생성 완료");
        }
    }
    
    private static void createIndexes(Connection conn) throws SQLException {
        boolean isMySQL = isMySQL(conn);
        
        // 인덱스 이름과 컬럼 정의
        String[][] indexDefinitions = {
            {"idx_trades_user_id", "trades", "user_id"},
            {"idx_trades_created_at", "trades", "created_at"},
            {"idx_trades_symbol", "trades", "symbol"},
            {"idx_trade_logs_user_id", "trade_logs", "user_id"},
            {"idx_trade_logs_created_at", "trade_logs", "created_at"},
            {"idx_trade_logs_action_type", "trade_logs", "action_type"},
            {"idx_trade_logs_symbol", "trade_logs", "symbol"},
            {"idx_llm_logs_user_id", "llm_analysis_logs", "user_id"},
            {"idx_llm_logs_agent", "llm_analysis_logs", "agent_name"},
            {"idx_llm_logs_created_at", "llm_analysis_logs", "created_at"},
            {"idx_portfolio_user_id", "portfolio_snapshots", "user_id"},
            {"idx_portfolio_created_at", "portfolio_snapshots", "created_at"},
            {"idx_market_data_symbol_timestamp", "market_data_cache", "symbol, timestamp"},
            {"idx_events_type", "system_events", "event_type"},
            {"idx_events_created_at", "system_events", "created_at"}
        };
        
        try (Statement stmt = conn.createStatement()) {
            for (String[] indexDef : indexDefinitions) {
                String indexName = indexDef[0];
                String tableName = indexDef[1];
                String columns = indexDef[2];
                
                try {
                    if (isMySQL) {
                        // MySQL: 인덱스 존재 여부 확인 후 생성
                        String checkSql = String.format(
                            "SELECT COUNT(*) FROM information_schema.statistics " +
                            "WHERE table_schema = DATABASE() AND table_name = '%s' AND index_name = '%s'",
                            tableName, indexName
                        );
                        try (var rs = stmt.executeQuery(checkSql)) {
                            if (rs.next() && rs.getInt(1) == 0) {
                                String createSql = String.format("CREATE INDEX %s ON %s(%s)", indexName, tableName, columns);
                                stmt.execute(createSql);
                            }
                        }
                    } else {
                        // SQLite: IF NOT EXISTS 지원
                        String createSql = String.format("CREATE INDEX IF NOT EXISTS %s ON %s(%s)", indexName, tableName, columns);
                        stmt.execute(createSql);
                    }
                } catch (SQLException e) {
                    logger.debug("인덱스 생성 건너뛰기 ({}): {}", indexName, e.getMessage());
                }
            }
            logger.debug("인덱스 생성 완료");
        }
    }
    
    private static void createTriggers(Connection conn) throws SQLException {
        boolean isMySQL = isMySQL(conn);
        
        // MySQL과 SQLite의 트리거 문법이 다름
        if (isMySQL) {
            // MySQL 트리거 (IF NOT EXISTS 지원 안 함, DROP IF EXISTS 사용)
            String[] triggers = {
                """
                DROP TRIGGER IF EXISTS update_users_updated_at;
                CREATE TRIGGER update_users_updated_at
                    BEFORE UPDATE ON users
                    FOR EACH ROW
                    SET NEW.updated_at = CURRENT_TIMESTAMP;
                """,
                """
                DROP TRIGGER IF EXISTS update_user_profiles_updated_at;
                CREATE TRIGGER update_user_profiles_updated_at
                    BEFORE UPDATE ON user_profiles
                    FOR EACH ROW
                    SET NEW.updated_at = CURRENT_TIMESTAMP;
                """
            };
            
            try (Statement stmt = conn.createStatement()) {
                for (String triggerSql : triggers) {
                    try {
                        // MySQL은 여러 문장을 한 번에 실행할 수 있음
                        String[] statements = triggerSql.split(";");
                        for (String statement : statements) {
                            String trimmed = statement.trim();
                            if (!trimmed.isEmpty()) {
                                stmt.execute(trimmed);
                            }
                        }
                    } catch (SQLException e) {
                        logger.debug("트리거 생성 건너뛰기: {}", e.getMessage());
                    }
                }
                logger.debug("트리거 생성 완료");
            }
        } else {
            // SQLite 트리거
            String[] triggers = {
                """
                CREATE TRIGGER IF NOT EXISTS update_users_updated_at
                    AFTER UPDATE ON users
                    FOR EACH ROW
                    BEGIN
                        UPDATE users SET updated_at = datetime('now') WHERE id = NEW.id;
                    END
                """,
                """
                CREATE TRIGGER IF NOT EXISTS update_user_profiles_updated_at
                    AFTER UPDATE ON user_profiles
                    FOR EACH ROW
                    BEGIN
                        UPDATE user_profiles SET updated_at = datetime('now') WHERE id = NEW.id;
                    END
                """
            };
            
            try (Statement stmt = conn.createStatement()) {
                for (String triggerSql : triggers) {
                    try {
                        stmt.execute(triggerSql);
                    } catch (SQLException e) {
                        logger.debug("트리거 생성 건너뛰기: {}", e.getMessage());
                    }
                }
                logger.debug("트리거 생성 완료");
            }
        }
    }
}

