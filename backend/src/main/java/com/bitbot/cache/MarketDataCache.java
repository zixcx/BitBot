package com.bitbot.cache;

import com.bitbot.database.DatabaseConnection;
import com.bitbot.models.MarketData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 시장 데이터 캐시 관리
 * 데이터베이스의 market_data_cache 테이블을 활용하여 API 호출 횟수 감소
 */
public class MarketDataCache {
    
    private static final Logger logger = LoggerFactory.getLogger(MarketDataCache.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // 캐시 유효 시간 (분) - 5분 이내 데이터는 캐시에서 사용
    private static final int CACHE_VALID_MINUTES = 5;
    
    /**
     * 캐시에서 데이터 조회 또는 저장
     * 
     * @param symbol 심볼 (예: BTCUSDT)
     * @param interval 시간 간격 (예: 15m, 1h, 1d)
     * @param limit 데이터 개수
     * @param dataSupplier 캐시 미스 시 데이터를 제공하는 함수
     * @return 시장 데이터 리스트
     */
    public static List<MarketData> getOrFetch(
            String symbol, 
            String interval, 
            int limit,
            java.util.function.Supplier<List<MarketData>> dataSupplier) {
        
        try {
            // 1. 캐시에서 조회
            List<MarketData> cached = loadFromCache(symbol, interval, limit);
            if (cached != null && isCacheValid(cached)) {
                logger.debug("캐시 히트: {} {} ({}개)", symbol, interval, cached.size());
                return cached;
            }
            
            // 2. 캐시 미스 → API 호출
            logger.debug("캐시 미스: {} {} - API 호출", symbol, interval);
            List<MarketData> fresh = dataSupplier.get();
            
            // 3. 캐시에 저장
            if (fresh != null && !fresh.isEmpty()) {
                saveToCache(symbol, interval, fresh);
            }
            
            return fresh != null ? fresh : new ArrayList<>();
            
        } catch (Exception e) {
            logger.error("캐시 처리 중 오류 발생", e);
            // 오류 발생 시 API 호출 결과 반환
            try {
                return dataSupplier.get();
            } catch (Exception ex) {
                logger.error("API 호출 실패", ex);
                return new ArrayList<>();
            }
        }
    }
    
    /**
     * 캐시에서 데이터 로드
     */
    private static List<MarketData> loadFromCache(String symbol, String interval, int limit) {
        String sql = """
            SELECT data_json, cached_at 
            FROM market_data_cache 
            WHERE symbol = ? AND interval = ? 
            ORDER BY cached_at DESC 
            LIMIT 1
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, symbol);
            stmt.setString(2, interval);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String dataJson = rs.getString("data_json");
                
                // JSON 파싱
                List<MarketData> dataList = parseJsonToMarketDataList(dataJson);
                
                // 최신 데이터만 limit 개수만큼 반환
                if (dataList.size() > limit) {
                    return dataList.subList(dataList.size() - limit, dataList.size());
                }
                
                return dataList;
            }
            
        } catch (Exception e) {
            logger.debug("캐시 로드 실패: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 캐시 유효성 검사
     */
    private static boolean isCacheValid(List<MarketData> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return false;
        }
        
        // 가장 최신 데이터의 타임스탬프 확인
        MarketData latest = dataList.get(dataList.size() - 1);
        LocalDateTime latestTime = latest.getTimestamp();
        LocalDateTime now = LocalDateTime.now();
        
        // 캐시 유효 시간 이내인지 확인
        long minutesDiff = ChronoUnit.MINUTES.between(latestTime, now);
        return minutesDiff <= CACHE_VALID_MINUTES;
    }
    
    /**
     * 캐시에 데이터 저장
     */
    private static void saveToCache(String symbol, String interval, List<MarketData> dataList) {
        String sql = """
            INSERT OR REPLACE INTO market_data_cache (symbol, interval, data_json, cached_at)
            VALUES (?, ?, ?, ?)
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // MarketData 리스트를 JSON으로 변환
            String dataJson = objectMapper.writeValueAsString(dataList);
            
            stmt.setString(1, symbol);
            stmt.setString(2, interval);
            stmt.setString(3, dataJson);
            stmt.setString(4, LocalDateTime.now().toString());
            
            stmt.executeUpdate();
            logger.debug("캐시 저장 완료: {} {} ({}개)", symbol, interval, dataList.size());
            
        } catch (Exception e) {
            logger.warn("캐시 저장 실패: {}", e.getMessage());
        }
    }
    
    /**
     * JSON 문자열을 MarketData 리스트로 변환
     */
    private static List<MarketData> parseJsonToMarketDataList(String json) {
        try {
            return objectMapper.readValue(
                json,
                objectMapper.getTypeFactory().constructCollectionType(
                    List.class, MarketData.class
                )
            );
        } catch (Exception e) {
            logger.error("JSON 파싱 실패", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 캐시 삭제 (특정 심볼/인터벌)
     */
    public static void clearCache(String symbol, String interval) {
        String sql = "DELETE FROM market_data_cache WHERE symbol = ? AND interval = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, symbol);
            stmt.setString(2, interval);
            
            int deleted = stmt.executeUpdate();
            logger.info("캐시 삭제 완료: {} {} ({}개)", symbol, interval, deleted);
            
        } catch (Exception e) {
            logger.error("캐시 삭제 실패", e);
        }
    }
    
    /**
     * 모든 캐시 삭제
     */
    public static void clearAllCache() {
        String sql = "DELETE FROM market_data_cache";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            int deleted = stmt.executeUpdate(sql);
            logger.info("전체 캐시 삭제 완료: {}개", deleted);
            
        } catch (Exception e) {
            logger.error("전체 캐시 삭제 실패", e);
        }
    }
}

