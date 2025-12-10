package com.bitbot.utils;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 환경 변수 및 설정 로더
 */
public class ConfigLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
    private static Dotenv dotenv;
    
    public static void loadConfig() {
        try {
            dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
            
            logger.info("환경 변수 로드 완료");
            validateRequiredKeys();
            
        } catch (Exception e) {
            logger.error(".env 파일 로드 실패. .env.example을 참고하여 .env 파일을 생성하세요.", e);
            throw new RuntimeException("환경 설정 로드 실패", e);
        }
    }
    
    private static void validateRequiredKeys() {
        // SQLite 사용으로 변경되어 SUPABASE는 더 이상 필수가 아님
        String[] requiredKeys = {
            "GEMINI_API_KEY",
            "BINANCE_API_KEY",
            "BINANCE_SECRET_KEY"
        };
        
        for (String key : requiredKeys) {
            if (get(key) == null || get(key).isEmpty()) {
                logger.warn("필수 환경 변수가 설정되지 않음: {}", key);
            }
        }
        
        // 선택적 환경 변수 (없어도 경고만)
        String[] optionalKeys = {
            "SUPABASE_URL",
            "SUPABASE_USER",
            "SUPABASE_PASSWORD"
        };
        
        for (String key : optionalKeys) {
            if (get(key) == null || get(key).isEmpty()) {
                logger.debug("선택적 환경 변수 미설정 (SQLite 사용 시 불필요): {}", key);
            }
        }
    }
    
    public static String get(String key) {
        return dotenv != null ? dotenv.get(key) : System.getenv(key);
    }
    
    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }
    
    public static int getInt(String key, int defaultValue) {
        try {
            String value = get(key);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            logger.warn("정수 변환 실패: {} (기본값 사용: {})", key, defaultValue);
            return defaultValue;
        }
    }
    
    public static double getDouble(String key, double defaultValue) {
        try {
            String value = get(key);
            return value != null ? Double.parseDouble(value) : defaultValue;
        } catch (NumberFormatException e) {
            logger.warn("실수 변환 실패: {} (기본값 사용: {})", key, defaultValue);
            return defaultValue;
        }
    }
    
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }
    
    // Convenience methods for common configs
    public static String getGeminiApiKey() {
        return get("GEMINI_API_KEY");
    }
    
    public static String getBinanceApiKey() {
        return get("BINANCE_API_KEY");
    }
    
    public static String getBinanceSecretKey() {
        return get("BINANCE_SECRET_KEY");
    }
    
    public static boolean isTestnet() {
        return getBoolean("BINANCE_USE_TESTNET", true);
    }
    
    public static String getTradingMode() {
        return get("TRADING_MODE", "SIMULATION");
    }
    
    public static double getMaxInvestmentPercent() {
        return getDouble("MAX_INVESTMENT_PERCENT", 10.0);
    }
    
    public static double getMaxTotalInvestmentPercent() {
        return getDouble("MAX_TOTAL_INVESTMENT_PERCENT", 50.0);
    }
    
    public static double getStopLossPercent() {
        return getDouble("STOP_LOSS_PERCENT", -10.0);
    }
    
    public static int getAnalysisIntervalMinutes() {
        return getInt("ANALYSIS_INTERVAL_MINUTES", 15);
    }
    
    public static boolean isRiskManagementEnabled() {
        return getBoolean("ENABLE_RISK_MANAGEMENT", true);
    }
    
    public static double getMinConfidenceThreshold() {
        return getDouble("MIN_CONFIDENCE_THRESHOLD", 0.70);
    }
    
    // MySQL 설정 메서드
    public static String getDbType() {
        return get("DB_TYPE", "sqlite");  // 기본값: sqlite
    }
    
    public static String getMysqlHost() {
        return get("MYSQL_HOST", "127.0.0.1");
    }
    
    public static int getMysqlPort() {
        return getInt("MYSQL_PORT", 3306);
    }
    
    public static String getMysqlDatabase() {
        return get("MYSQL_DATABASE", "bitbot");
    }
    
    public static String getMysqlUsername() {
        return get("MYSQL_USERNAME", "root");
    }
    
    public static String getMysqlPassword() {
        return get("MYSQL_PASSWORD");
    }
}


