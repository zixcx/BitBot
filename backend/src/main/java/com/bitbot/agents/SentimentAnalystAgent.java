package com.bitbot.agents;

import com.bitbot.data.FearGreedIndexCollector;
import com.bitbot.data.NewsCollector;
import com.bitbot.exceptions.AnalysisException;
import com.bitbot.models.TradingDecision;
import com.bitbot.models.UserProfile;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 시장 심리 분석 에이전트
 * 뉴스, 소셜 미디어, 공포-탐욕 지수 등을 분석
 */
public class SentimentAnalystAgent {
    
    private static final Logger logger = LoggerFactory.getLogger(SentimentAnalystAgent.class);
    private final GeminiClient geminiClient;
    private final NewsCollector newsCollector;
    private final FearGreedIndexCollector fearGreedCollector;
    
    public SentimentAnalystAgent() {
        this.geminiClient = new GeminiClient();
        this.newsCollector = new NewsCollector();
        this.fearGreedCollector = new FearGreedIndexCollector();
    }
    
    /**
     * 시장 심리 분석 수행
     * @param currentPrice 현재 BTC 가격
     * @param userProfile 사용자 프로필 (투자 성향 정보, null 가능)
     * @return 분석 결과
     * @throws AnalysisException 분석 실패 시
     */
    public TradingDecision analyze(double currentPrice, UserProfile userProfile) throws AnalysisException {
        try {
            // 뉴스 데이터 수집
            logger.info("[시장 심리 분석] 뉴스 데이터 수집 중...");
            List<NewsCollector.NewsItem> newsList = newsCollector.getRecentNews(10);
            
            // 공포/탐욕 지수 수집
            logger.info("[시장 심리 분석] 공포/탐욕 지수 수집 중...");
            FearGreedIndexCollector.FearGreedIndex fearGreedIndex = fearGreedCollector.getCurrentIndex();
            
            String prompt = buildPrompt(currentPrice, userProfile, newsList, fearGreedIndex);
            
            if (userProfile != null) {
                logger.info("[시장 심리 분석 에이전트] LLM 호출 시작... (투자 성향: {}, 뉴스: {}개, 공포/탐욕: {})", 
                        userProfile.getInvestorType().getKoreanName(), 
                        newsList.size(),
                        fearGreedIndex.getKoreanClassification());
            } else {
                logger.info("[시장 심리 분석 에이전트] LLM 호출 시작... (뉴스: {}개, 공포/탐욕: {})", 
                        newsList.size(),
                        fearGreedIndex.getKoreanClassification());
            }
            String response = geminiClient.callGemini(prompt);
            
            // JSON 파싱
            JsonNode jsonResponse = geminiClient.parseJSONResponse(response);
            
            String decisionStr = jsonResponse.get("decision").asText();
            double confidence = jsonResponse.get("confidence").asDouble();
            String reason = jsonResponse.get("reason").asText();
            
            TradingDecision.Decision decision = TradingDecision.Decision.valueOf(decisionStr);
            
            TradingDecision result = new TradingDecision(
                    "Sentiment Analyst",
                    decision,
                    confidence,
                    reason
            );
            
            logger.info("[시장 심리 분석] 결과: {}", result);
            return result;
            
        } catch (Exception e) {
            logger.error("시장 심리 분석 실패", e);
            throw new AnalysisException("시장 심리 분석 실패: " + e.getMessage(), e);
        }
    }
    
    private String buildPrompt(double currentPrice, UserProfile profile, 
                               List<NewsCollector.NewsItem> newsList, 
                               FearGreedIndexCollector.FearGreedIndex fearGreedIndex) {
        
        // 뉴스 데이터 포맷팅
        StringBuilder newsSection = new StringBuilder();
        if (!newsList.isEmpty()) {
            newsSection.append("\n[최근 주요 뉴스]\n");
            int count = 0;
            for (NewsCollector.NewsItem news : newsList) {
                if (count >= 5) break; // 최대 5개만
                newsSection.append(String.format(
                    "- [%s] %s (감정 점수: %.2f, 출처: %s)\n",
                    news.getPublishedAt() != null ? news.getPublishedAt().toString() : "최근",
                    news.getTitle(),
                    news.getSentimentScore(),
                    news.getSource()
                ));
                count++;
            }
        } else {
            newsSection.append("\n[최근 주요 뉴스]\n- 뉴스 데이터를 수집할 수 없습니다.\n");
        }
        
        // 공포/탐욕 지수 포맷팅
        String fearGreedSection = String.format("""
                
                [공포/탐욕 지수]
                현재 지수: %d/100 (%s)
                정규화 값: %.2f (-1.0=극도의 공포, 0=중립, 1.0=극도의 탐욕)
                """,
                fearGreedIndex.getValue(),
                fearGreedIndex.getKoreanClassification(),
                fearGreedIndex.getNormalizedValue()
        );
        
        String marketContext = String.format("""
                현재 비트코인 가격: $%.2f
                %s
                %s
                
                최근 암호화폐 시장 상황:
                - 전반적인 시장 분위기 및 투자 심리를 분석하세요
                - 위 뉴스 이벤트들이 시장에 미치는 영향 (규제, 기관 투자, 기술 발전 등)
                - 공포/탐욕 지수를 고려한 투자자들의 리스크 선호도
                - 거시경제 환경 (금리, 인플레이션 등)
                """, 
                currentPrice,
                newsSection.toString(),
                fearGreedSection
        );
        
        // 투자 성향별 심리 분석 스타일
        String sentimentStyle = "";
        if (profile != null) {
            var riskSettings = profile.getRiskSettings();
            sentimentStyle = String.format("""
                    
                    [투자자 프로필]
                    투자 성향: %s
                    거래 전략: %s
                    레버리지: %s
                    손절 기준: %.1f%%
                    익절 기준: +%.1f%%
                    
                    분석 스타일:
                    """, 
                    profile.getInvestorType().getKoreanName(),
                    profile.getTradingStrategy().getKoreanName(),
                    riskSettings.isLeverageAllowed() ? riskSettings.getMaxLeverage() + "배" : "1배 (현물)",
                    riskSettings.getMaxLossPercent(),
                    riskSettings.getTakeProfitPercent());
            
            switch (profile.getInvestorType()) {
                case CONSERVATIVE:
                    sentimentStyle += """
                        - 보수적 관점: 불안한 시장 심리는 매수 기회로 보지 않음
                        - 공포 지수 높을 때는 HOLD 권장
                        - 탐욕 지수 극단적일 때는 매도 고려
                        - 신뢰도 0.8 이상일 때만 추천
                        - 손절 -15%를 염두에 두고, 불안한 심리는 피하세요
                        - DCA 전략: 시장이 안정적일 때만 분할 매수
                        """;
                    break;
                    
                case MODERATE:
                    sentimentStyle += """
                        - 균형잡힌 관점: 심리와 기술적 분석 조화
                        - 공포/탐욕 극단적일 때만 반대 매매 고려
                        - 신뢰도 0.7 이상 고려
                        - 손절 -7%를 염두에 두고, 극단적 심리 시 주의
                        - 추세 추종: 시장 심리가 추세와 일치할 때 진입
                        """;
                    break;
                    
                case AGGRESSIVE:
                    sentimentStyle += """
                        - 공격적 관점: 시장 심리 극단화를 기회로 활용
                        - 공포 지수 높을 때 역매매 기회 포착
                        - 신뢰도 0.6 이상도 고려
                        - 레버리지 3배 사용: 손절 -5%를 염두에 두고 신중하게
                        - 스윙 트레이딩: 심리 변동성을 중기 수익 기회로 활용
                        - 레버리지로 손실 확대 위험이 있으므로, 극단적 심리 시 주의
                        """;
                    break;
                    
                case SPECULATIVE:
                    sentimentStyle += """
                        - 매우 공격적 관점: 심리 변동성을 수익 기회로 활용
                        - 공포/탐욕 극단화 시 빠른 진입/청산
                        - 신뢰도 0.5 이상도 고려
                        - 레버리지 10배 사용: 손절 -3%를 염두에 두고 매우 신중하게
                        - 변동성 돌파: 심리 극단화 시 빠른 반응, 단기 변동성 활용
                        - 레버리지로 손실이 10배 확대될 수 있으므로, 심리 변동성에 매우 주의
                        - 공포/탐욕 극단화는 기회이지만, 빠른 청산 준비 필수
                        """;
                    break;
            }
        }
        
        String systemRole = "당신은 글로벌 투자은행의 거시 경제 및 시장 심리 분석가입니다.";
        
        String task = String.format("""
                주어진 시장 상황을 바탕으로 현재 암호화폐 시장의 투자 심리를 진단하고,
                이것이 단기 비트코인 가격에 미칠 영향을 예측하세요.
                
                %s
                
                분석 시 고려사항:
                1. 공포(Fear) vs 탐욕(Greed) - 현재 시장 심리 상태
                2. 주요 뉴스의 긍정/부정 영향
                3. 기관 투자자 vs 개인 투자자 동향
                4. 거시경제 환경이 리스크 자산에 미치는 영향
                5. 과거 유사한 상황에서의 시장 반응
                
                'BUY', 'SELL', 'HOLD' 중 추천 포지션과 신뢰도(0.0~1.0), 근거를 제시하세요.
                """, sentimentStyle);
        
        String outputFormat = """
                {
                  "agent": "Sentiment Analyst",
                  "decision": "SELL",
                  "confidence": 0.70,
                  "reason": "최근 주요 거래소 보안 이슈로 시장 불안감이 커지며 공포 지수 상승. 단기적으로 투매 가능성 높음."
                }
                """;
        
        return GeminiClient.wrapPromptForJSON(systemRole, task, marketContext, outputFormat);
    }
}


