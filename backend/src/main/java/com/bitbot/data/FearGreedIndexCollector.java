package com.bitbot.data;

import com.bitbot.utils.RetryUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 공포/탐욕 지수 수집기
 * Alternative.me API를 통해 Bitcoin Fear & Greed Index 수집
 */
public class FearGreedIndexCollector {
    
    private static final Logger logger = LoggerFactory.getLogger(FearGreedIndexCollector.class);
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public FearGreedIndexCollector() {
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 현재 공포/탐욕 지수 조회
     * @return 공포/탐욕 지수 (0-100, 0=극도의 공포, 100=극도의 탐욕)
     */
    public FearGreedIndex getCurrentIndex() {
        try {
            return RetryUtil.retryIfRetryable(() -> {
                try {
                    String url = "https://api.alternative.me/fng/?limit=1";
                    
                    Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();
                    
                    try (Response response = httpClient.newCall(request).execute()) {
                        if (!response.isSuccessful()) {
                            throw new RuntimeException("Fear & Greed Index API 호출 실패: " + response.code());
                        }
                        
                        String responseBody = response.body() != null ? response.body().string() : "";
                        JsonNode json = objectMapper.readTree(responseBody);
                        JsonNode data = json.get("data");
                        
                        if (data != null && data.isArray() && data.size() > 0) {
                            JsonNode item = data.get(0);
                            
                            FearGreedIndex index = new FearGreedIndex();
                            index.setValue(Integer.parseInt(item.get("value").asText()));
                            index.setClassification(item.get("value_classification").asText());
                            index.setTimestamp(Long.parseLong(item.get("timestamp").asText()));
                            
                            logger.debug("공포/탐욕 지수 수집: {} ({})", index.getValue(), index.getClassification());
                            return index;
                        }
                        
                        throw new RuntimeException("공포/탐욕 지수 데이터가 비어있습니다.");
                    }
                } catch (Exception e) {
                    throw new RuntimeException("공포/탐욕 지수 수집 중 오류: " + e.getMessage(), e);
                }
            }, 3, 1000); // 최대 3회 재시도, 초기 지연 1초
            
        } catch (Exception e) {
            logger.error("공포/탐욕 지수 수집 실패", e);
            // 실패 시 기본값 반환 (중립)
            return new FearGreedIndex(50, "Neutral", System.currentTimeMillis() / 1000);
        }
    }
    
    /**
     * 최근 N일간 공포/탐욕 지수 조회
     * @param days 일수 (최대 365)
     * @return 공포/탐욕 지수 목록
     */
    public List<FearGreedIndex> getHistoricalIndex(int days) {
        try {
            int limit = Math.min(days, 365);
            return RetryUtil.retryIfRetryable(() -> {
                try {
                    String url = String.format("https://api.alternative.me/fng/?limit=%d", limit);
                    
                    Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();
                    
                    try (Response response = httpClient.newCall(request).execute()) {
                        if (!response.isSuccessful()) {
                            throw new RuntimeException("Fear & Greed Index API 호출 실패: " + response.code());
                        }
                        
                        String responseBody = response.body() != null ? response.body().string() : "";
                        JsonNode json = objectMapper.readTree(responseBody);
                        JsonNode data = json.get("data");
                        
                        List<FearGreedIndex> indexList = new ArrayList<>();
                        if (data != null && data.isArray()) {
                            for (JsonNode item : data) {
                                FearGreedIndex index = new FearGreedIndex();
                                index.setValue(Integer.parseInt(item.get("value").asText()));
                                index.setClassification(item.get("value_classification").asText());
                                index.setTimestamp(Long.parseLong(item.get("timestamp").asText()));
                                indexList.add(index);
                            }
                        }
                        
                        logger.debug("공포/탐욕 지수 {}일치 수집 완료", indexList.size());
                        return indexList;
                    }
                } catch (Exception e) {
                    throw new RuntimeException("공포/탐욕 지수 히스토리 수집 중 오류: " + e.getMessage(), e);
                }
            }, 3, 1000); // 최대 3회 재시도, 초기 지연 1초
            
        } catch (Exception e) {
            logger.error("공포/탐욕 지수 히스토리 수집 실패", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 공포/탐욕 지수 모델
     */
    public static class FearGreedIndex {
        private int value; // 0-100
        private String classification; // "Extreme Fear", "Fear", "Neutral", "Greed", "Extreme Greed"
        private long timestamp; // Unix timestamp
        
        public FearGreedIndex() {}
        
        public FearGreedIndex(int value, String classification, long timestamp) {
            this.value = value;
            this.classification = classification;
            this.timestamp = timestamp;
        }
        
        // Getters and Setters
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
        
        public String getClassification() { return classification; }
        public void setClassification(String classification) { this.classification = classification; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        /**
         * 공포/탐욕 지수를 -1.0 ~ 1.0 범위로 정규화
         * @return -1.0 (극도의 공포) ~ 1.0 (극도의 탐욕)
         */
        public double getNormalizedValue() {
            return (value / 50.0) - 1.0; // 0-100 -> -1.0 ~ 1.0
        }
        
        /**
         * 한국어 분류명 반환
         */
        public String getKoreanClassification() {
            return switch (classification.toLowerCase()) {
                case "extreme fear" -> "극도의 공포";
                case "fear" -> "공포";
                case "neutral" -> "중립";
                case "greed" -> "탐욕";
                case "extreme greed" -> "극도의 탐욕";
                default -> classification;
            };
        }
        
        @Override
        public String toString() {
            return String.format("FearGreedIndex{value=%d, classification='%s', normalized=%.2f}", 
                value, classification, getNormalizedValue());
        }
    }
}

