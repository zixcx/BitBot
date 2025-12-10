package com.bitbot.data;

import com.bitbot.exceptions.DataCollectionException;
import com.bitbot.utils.ConfigLoader;
import com.bitbot.utils.RetryUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * 뉴스 데이터 수집기
 * CryptoPanic API 또는 Google News RSS를 통해 암호화폐 관련 뉴스 수집
 */
public class NewsCollector {
    
    private static final Logger logger = LoggerFactory.getLogger(NewsCollector.class);
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String cryptoPanicApiKey;
    
    public NewsCollector() {
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.cryptoPanicApiKey = ConfigLoader.get("CRYPTOPANIC_API_KEY", "");
    }
    
    /**
     * 최근 암호화폐 뉴스 수집
     * @param limit 최대 뉴스 개수 (기본: 10)
     * @return 뉴스 목록
     */
    public List<NewsItem> getRecentNews(int limit) {
        List<NewsItem> newsList = new ArrayList<>();
        
        // CryptoPanic API 우선 시도
        if (!cryptoPanicApiKey.isEmpty()) {
            try {
                newsList = fetchFromCryptoPanic(limit);
                if (!newsList.isEmpty()) {
                    logger.info("✅ CryptoPanic에서 {}개 뉴스 수집 완료", newsList.size());
                    return newsList;
                }
            } catch (Exception e) {
                logger.warn("CryptoPanic API 호출 실패: {}", e.getMessage());
            }
        }
        
        // CryptoPanic 실패 시 Google News RSS 시도
        try {
            newsList = fetchFromGoogleNews(limit);
            if (!newsList.isEmpty()) {
                logger.info("✅ Google News에서 {}개 뉴스 수집 완료", newsList.size());
                return newsList;
            }
        } catch (Exception e) {
            logger.warn("Google News RSS 호출 실패: {}", e.getMessage());
        }
        
        logger.warn("뉴스 수집 실패: 모든 소스에서 데이터를 가져올 수 없습니다.");
        return newsList;
    }
    
    /**
     * CryptoPanic API에서 뉴스 수집
     */
    private List<NewsItem> fetchFromCryptoPanic(int limit) throws DataCollectionException {
        String url = String.format(
            "https://cryptopanic.com/api/v1/posts/?auth_token=%s&public=true&filter=hot&currencies=BTC&limit=%d",
            cryptoPanicApiKey,
            limit
        );
        
        try {
            return RetryUtil.retryIfRetryable(() -> {
                try {
                    Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();
                    
                    try (Response response = httpClient.newCall(request).execute()) {
                        if (!response.isSuccessful()) {
                            throw new RuntimeException("CryptoPanic API 호출 실패: " + response.code());
                        }
                        
                        String responseBody = response.body() != null ? response.body().string() : "";
                        JsonNode json = objectMapper.readTree(responseBody);
                        JsonNode results = json.get("results");
                        
                        List<NewsItem> newsList = new ArrayList<>();
                        if (results != null && results.isArray()) {
                            for (JsonNode item : results) {
                                NewsItem news = new NewsItem();
                                news.setTitle(item.has("title") ? item.get("title").asText("") : "");
                                
                                // Source 처리 (null 안전)
                                String source = "Unknown";
                                if (item.has("source")) {
                                    JsonNode sourceNode = item.get("source");
                                    if (sourceNode != null && sourceNode.has("title")) {
                                        source = sourceNode.get("title").asText("Unknown");
                                    } else if (sourceNode != null && sourceNode.isTextual()) {
                                        source = sourceNode.asText("Unknown");
                                    }
                                }
                                news.setSource(source);
                                
                                news.setUrl(item.has("url") ? item.get("url").asText("") : "");
                                
                                // Votes (긍정/부정) 계산
                                JsonNode votes = item.get("votes");
                                if (votes != null) {
                                    int positive = votes.get("positive").asInt(0);
                                    int negative = votes.get("negative").asInt(0);
                                    news.setSentimentScore(calculateSentimentScore(positive, negative));
                                }
                                
                                // Published at
                                String publishedAt = item.get("published_at").asText("");
                                if (!publishedAt.isEmpty()) {
                                    try {
                                        Instant instant = Instant.parse(publishedAt);
                                        news.setPublishedAt(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
                                    } catch (Exception e) {
                                        news.setPublishedAt(LocalDateTime.now());
                                    }
                                } else {
                                    news.setPublishedAt(LocalDateTime.now());
                                }
                                
                                newsList.add(news);
                            }
                        }
                        
                        return newsList;
                    }
                } catch (Exception e) {
                    throw new RuntimeException("CryptoPanic 뉴스 수집 중 오류: " + e.getMessage(), e);
                }
            }, 3, 1000); // 최대 3회 재시도, 초기 지연 1초
        } catch (Exception e) {
            throw new DataCollectionException("CryptoPanic 뉴스 수집 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * Google News RSS에서 뉴스 수집 (대체 방법)
     */
    private List<NewsItem> fetchFromGoogleNews(int limit) throws DataCollectionException {
        // Google News RSS는 XML이므로 간단한 파싱 필요
        // 여기서는 기본적인 구현만 제공 (실제로는 XML 파서 필요)
        String url = "https://news.google.com/rss/search?q=bitcoin+cryptocurrency&hl=en&gl=US&ceid=US:en";
        
        try {
            return RetryUtil.retryIfRetryable(() -> {
                try {
                    Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();
                    
                    try (Response response = httpClient.newCall(request).execute()) {
                        if (!response.isSuccessful()) {
                            throw new RuntimeException("Google News RSS 호출 실패: " + response.code());
                        }
                        
                        String responseBody = response.body() != null ? response.body().string() : "";
                        
                        // 간단한 RSS 파싱 (실제로는 XML 파서 사용 권장)
                        List<NewsItem> newsList = parseRSS(responseBody, limit);
                        return newsList;
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Google News 뉴스 수집 중 오류: " + e.getMessage(), e);
                }
            }, 3, 1000); // 최대 3회 재시도, 초기 지연 1초
        } catch (Exception e) {
            throw new DataCollectionException("Google News 뉴스 수집 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 간단한 RSS 파싱 (기본 구현)
     */
    private List<NewsItem> parseRSS(String rssContent, int limit) {
        List<NewsItem> newsList = new ArrayList<>();
        
        // 간단한 파싱: <title>과 <link> 태그 추출
        String[] items = rssContent.split("<item>");
        int count = 0;
        
        for (String item : items) {
            if (count >= limit) break;
            
            try {
                NewsItem news = new NewsItem();
                
                // Title 추출
                int titleStart = item.indexOf("<title>");
                int titleEnd = item.indexOf("</title>");
                if (titleStart > 0 && titleEnd > titleStart) {
                    String title = item.substring(titleStart + 7, titleEnd).trim();
                    // CDATA 제거
                    title = title.replace("<![CDATA[", "").replace("]]>", "").trim();
                    if (!title.isEmpty()) {
                        news.setTitle(title);
                    } else {
                        news.setTitle(""); // 빈 문자열로 설정
                    }
                } else {
                    news.setTitle(""); // Title이 없으면 빈 문자열
                }
                
                // Link 추출
                int linkStart = item.indexOf("<link>");
                int linkEnd = item.indexOf("</link>");
                if (linkStart > 0 && linkEnd > linkStart) {
                    news.setUrl(item.substring(linkStart + 6, linkEnd).trim());
                }
                
                // PubDate 추출
                int pubDateStart = item.indexOf("<pubDate>");
                int pubDateEnd = item.indexOf("</pubDate>");
                if (pubDateStart > 0 && pubDateEnd > pubDateStart) {
                    try {
                        // RFC 822 형식 파싱 (간단한 구현)
                        news.setPublishedAt(LocalDateTime.now()); // 실제로는 파싱 필요
                    } catch (Exception e) {
                        news.setPublishedAt(LocalDateTime.now());
                    }
                } else {
                    news.setPublishedAt(LocalDateTime.now());
                }
                
                news.setSource("Google News");
                news.setSentimentScore(0.0); // Google News는 감정 점수 없음
                
                // Title이 null이 아니고 비어있지 않을 때만 추가
                if (news.getTitle() != null && !news.getTitle().isEmpty()) {
                    newsList.add(news);
                    count++;
                }
            } catch (Exception e) {
                logger.debug("RSS 항목 파싱 실패: {}", e.getMessage());
            }
        }
        
        return newsList;
    }
    
    /**
     * 긍정/부정 투표로부터 감정 점수 계산 (-1.0 ~ 1.0)
     */
    private double calculateSentimentScore(int positive, int negative) {
        int total = positive + negative;
        if (total == 0) return 0.0;
        
        // 긍정 비율을 -1.0 ~ 1.0 범위로 변환
        double ratio = (double) positive / total;
        return (ratio * 2.0) - 1.0; // 0.0 ~ 1.0 -> -1.0 ~ 1.0
    }
    
    /**
     * 뉴스 아이템 모델
     */
    public static class NewsItem {
        private String title;
        private String source;
        private String url;
        private LocalDateTime publishedAt;
        private double sentimentScore; // -1.0 (부정) ~ 1.0 (긍정)
        
        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public LocalDateTime getPublishedAt() { return publishedAt; }
        public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
        
        public double getSentimentScore() { return sentimentScore; }
        public void setSentimentScore(double sentimentScore) { this.sentimentScore = sentimentScore; }
        
        @Override
        public String toString() {
            return String.format("NewsItem{title='%s', source='%s', sentiment=%.2f}", 
                title, source, sentimentScore);
        }
    }
}

