package com.bitbot;

import com.bitbot.data.FearGreedIndexCollector;
import com.bitbot.data.NewsCollector;
import com.bitbot.agents.SentimentAnalystAgent;
import com.bitbot.models.TradingDecision;
import com.bitbot.models.UserProfile;
import com.bitbot.models.InvestorType;
import com.bitbot.models.TradingStrategy;
import com.bitbot.models.RiskSettings;
import com.bitbot.classification.InvestorTypeClassifier;
import com.bitbot.utils.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 뉴스 데이터 및 공포/탐욕 지수 통합 테스트
 * 실제 API 호출 및 LLM 분석 반영 확인
 */
public class NewsAndSentimentTest {
    
    private static final Logger logger = LoggerFactory.getLogger(NewsAndSentimentTest.class);
    
    public static void main(String[] args) {
        // 환경 변수 로드
        try {
            ConfigLoader.loadConfig();
        } catch (Exception e) {
            logger.error("환경 변수 로드 실패", e);
            System.exit(1);
        }
        
        logger.info("=".repeat(80));
        logger.info("뉴스 데이터 및 공포/탐욕 지수 통합 테스트 시작");
        logger.info("=".repeat(80));
        
        try {
            // 1. 뉴스 데이터 수집 테스트
            testNewsCollection();
            
            // 2. 공포/탐욕 지수 수집 테스트
            testFearGreedIndex();
            
            // 3. SentimentAnalystAgent 통합 테스트
            testSentimentAnalysis();
            
            logger.info("=".repeat(80));
            logger.info("✅ 모든 테스트 완료!");
            logger.info("=".repeat(80));
            
        } catch (Exception e) {
            logger.error("테스트 실패", e);
            System.exit(1);
        }
    }
    
    /**
     * 뉴스 데이터 수집 테스트
     */
    private static void testNewsCollection() {
        logger.info("\n" + "=".repeat(80));
        logger.info("[테스트 1] 뉴스 데이터 수집 테스트");
        logger.info("=".repeat(80));
        
        NewsCollector newsCollector = new NewsCollector();
        
        try {
            logger.info("뉴스 수집 중... (최대 10개)");
            List<NewsCollector.NewsItem> newsList = newsCollector.getRecentNews(10);
            
            if (newsList.isEmpty()) {
                logger.warn("⚠️ 뉴스 데이터를 수집할 수 없습니다.");
                logger.warn("   - CryptoPanic API 키가 설정되지 않았거나");
                logger.warn("   - Google News RSS 접근이 제한되었을 수 있습니다.");
                return;
            }
            
            logger.info("✅ 뉴스 {}개 수집 완료\n", newsList.size());
            
            // 상위 5개 뉴스 출력
            int count = Math.min(5, newsList.size());
            for (int i = 0; i < count; i++) {
                NewsCollector.NewsItem news = newsList.get(i);
                logger.info("[뉴스 {}]", i + 1);
                logger.info("  제목: {}", news.getTitle());
                logger.info("  출처: {}", news.getSource());
                logger.info("  URL: {}", news.getUrl());
                logger.info("  감정 점수: {:.2f} (-1.0=부정, 0=중립, 1.0=긍정)", news.getSentimentScore());
                logger.info("  발행일: {}", news.getPublishedAt());
                logger.info("");
            }
            
            // 통계
            double avgSentiment = newsList.stream()
                    .mapToDouble(NewsCollector.NewsItem::getSentimentScore)
                    .average()
                    .orElse(0.0);
            
            logger.info("📊 뉴스 통계:");
            logger.info("  총 뉴스 수: {}", newsList.size());
            logger.info("  평균 감정 점수: {:.2f}", avgSentiment);
            logger.info("  시장 심리: {}", 
                    avgSentiment > 0.3 ? "긍정적" : 
                    avgSentiment < -0.3 ? "부정적" : "중립");
            
        } catch (Exception e) {
            logger.error("❌ 뉴스 수집 테스트 실패", e);
            throw new RuntimeException("뉴스 수집 테스트 실패", e);
        }
    }
    
    /**
     * 공포/탐욕 지수 수집 테스트
     */
    private static void testFearGreedIndex() {
        logger.info("\n" + "=".repeat(80));
        logger.info("[테스트 2] 공포/탐욕 지수 수집 테스트");
        logger.info("=".repeat(80));
        
        FearGreedIndexCollector fearGreedCollector = new FearGreedIndexCollector();
        
        try {
            // 현재 지수 조회
            logger.info("현재 공포/탐욕 지수 조회 중...");
            FearGreedIndexCollector.FearGreedIndex currentIndex = fearGreedCollector.getCurrentIndex();
            
            logger.info("✅ 공포/탐욕 지수 수집 완료\n");
            logger.info("📊 현재 공포/탐욕 지수:");
            logger.info("  지수 값: {}/100", currentIndex.getValue());
            logger.info("  분류: {} ({})", 
                    currentIndex.getClassification(), 
                    currentIndex.getKoreanClassification());
            logger.info("  정규화 값: {:.2f} (-1.0=극도의 공포, 0=중립, 1.0=극도의 탐욕)", 
                    currentIndex.getNormalizedValue());
            logger.info("  타임스탬프: {}", currentIndex.getTimestamp());
            
            // 시장 심리 해석
            int value = currentIndex.getValue();
            String interpretation = "";
            if (value >= 75) {
                interpretation = "🚨 극도의 탐욕 - 과매수 구간, 매도 고려";
            } else if (value >= 55) {
                interpretation = "📈 탐욕 - 상승 추세, 신중한 매수";
            } else if (value >= 45) {
                interpretation = "⚖️ 중립 - 관망 권장";
            } else if (value >= 25) {
                interpretation = "📉 공포 - 하락 추세, 역매수 기회";
            } else {
                interpretation = "🚨 극도의 공포 - 과매도 구간, 매수 기회";
            }
            
            logger.info("  해석: {}", interpretation);
            
            // 최근 7일 히스토리 조회
            logger.info("\n최근 7일 공포/탐욕 지수 히스토리 조회 중...");
            List<FearGreedIndexCollector.FearGreedIndex> history = 
                    fearGreedCollector.getHistoricalIndex(7);
            
            if (!history.isEmpty()) {
                logger.info("✅ 최근 7일 지수 {}개 수집 완료\n", history.size());
                
                logger.info("📊 최근 7일 추이:");
                for (int i = 0; i < Math.min(7, history.size()); i++) {
                    FearGreedIndexCollector.FearGreedIndex index = history.get(i);
                    logger.info("  일차 {}: {} ({})", 
                            i + 1, 
                            index.getValue(), 
                            index.getKoreanClassification());
                }
                
                // 평균 계산
                double avgValue = history.stream()
                        .mapToInt(FearGreedIndexCollector.FearGreedIndex::getValue)
                        .average()
                        .orElse(50.0);
                
                logger.info("\n  최근 7일 평균: {:.1f}/100", avgValue);
            } else {
                logger.warn("⚠️ 히스토리 데이터를 수집할 수 없습니다.");
            }
            
        } catch (Exception e) {
            logger.error("❌ 공포/탐욕 지수 수집 테스트 실패", e);
            throw new RuntimeException("공포/탐욕 지수 수집 테스트 실패", e);
        }
    }
    
    /**
     * SentimentAnalystAgent 통합 테스트
     */
    private static void testSentimentAnalysis() {
        logger.info("\n" + "=".repeat(80));
        logger.info("[테스트 3] SentimentAnalystAgent 통합 테스트");
        logger.info("=".repeat(80));
        
        try {
            SentimentAnalystAgent sentimentAgent = new SentimentAnalystAgent();
            
            // 테스트용 사용자 프로필 생성 (보수적 투자자)
            UserProfile testProfile = createTestProfile(InvestorType.CONSERVATIVE);
            
            // 현재 BTC 가격 조회 (테스트용으로 고정값 사용)
            double currentPrice = 50000.0; // 테스트용 가격
            
            logger.info("현재 BTC 가격: ${:,.2f} (테스트용)", currentPrice);
            logger.info("투자 성향: {}", testProfile.getInvestorType().getKoreanName());
            logger.info("거래 전략: {}", testProfile.getTradingStrategy().getKoreanName());
            logger.info("");
            
            logger.info("시장 심리 분석 시작... (LLM 호출 포함)");
            logger.info("이 과정은 몇 초가 걸릴 수 있습니다...\n");
            
            TradingDecision decision = sentimentAgent.analyze(currentPrice, testProfile);
            
            logger.info("✅ 시장 심리 분석 완료\n");
            logger.info("📊 분석 결과:");
            logger.info("  에이전트: {}", decision.getAgentName());
            logger.info("  결정: {}", decision.getDecision().name());
            logger.info("  신뢰도: {:.2f} (0.0 ~ 1.0)", decision.getConfidence());
            logger.info("  근거:");
            logger.info("    {}", decision.getReason().replace("\n", "\n    "));
            
            // 결정 해석
            String interpretation = switch (decision.getDecision()) {
                case STRONG_BUY -> "🟢🟢 강력 매수 권장 - 매우 긍정적인 시장 심리";
                case BUY -> "🟢 매수 권장 - 긍정적인 시장 심리";
                case HOLD -> "🟡 관망 권장 - 중립적인 시장 심리";
                case SELL -> "🔴 매도 권장 - 부정적인 시장 심리";
                case STRONG_SELL -> "🔴🔴 강력 매도 권장 - 매우 부정적인 시장 심리";
            };
            
            logger.info("\n  해석: {}", interpretation);
            
            // 다른 투자 성향으로도 테스트
            logger.info("\n" + "-".repeat(80));
            logger.info("추가 테스트: 공격적 투자자 프로필로 분석");
            logger.info("-".repeat(80));
            
            UserProfile aggressiveProfile = createTestProfile(InvestorType.AGGRESSIVE);
            logger.info("투자 성향: {}", aggressiveProfile.getInvestorType().getKoreanName());
            logger.info("거래 전략: {}", aggressiveProfile.getTradingStrategy().getKoreanName());
            logger.info("레버리지: {}배", aggressiveProfile.getRiskSettings().getMaxLeverage());
            logger.info("");
            
            logger.info("시장 심리 분석 시작... (LLM 호출 포함)\n");
            TradingDecision aggressiveDecision = sentimentAgent.analyze(currentPrice, aggressiveProfile);
            
            logger.info("✅ 공격적 투자자 프로필 분석 완료\n");
            logger.info("📊 분석 결과:");
            logger.info("  결정: {}", aggressiveDecision.getDecision().name());
            logger.info("  신뢰도: {:.2f}", aggressiveDecision.getConfidence());
            logger.info("  근거:");
            logger.info("    {}", aggressiveDecision.getReason().replace("\n", "\n    "));
            
            // 두 프로필 비교
            logger.info("\n" + "-".repeat(80));
            logger.info("📊 투자 성향별 분석 비교:");
            logger.info("-".repeat(80));
            logger.info("  보수적 투자자: {} (신뢰도: {:.2f})", 
                    decision.getDecision().name(), decision.getConfidence());
            logger.info("  공격적 투자자: {} (신뢰도: {:.2f})", 
                    aggressiveDecision.getDecision().name(), aggressiveDecision.getConfidence());
            
            if (!decision.getDecision().equals(aggressiveDecision.getDecision())) {
                logger.info("\n  💡 투자 성향에 따라 다른 결정이 나왔습니다!");
                logger.info("     이는 LLM이 투자자 프로필을 제대로 반영하고 있음을 의미합니다.");
            }
            
        } catch (Exception e) {
            logger.error("❌ SentimentAnalystAgent 통합 테스트 실패", e);
            throw new RuntimeException("SentimentAnalystAgent 통합 테스트 실패", e);
        }
    }
    
    /**
     * 테스트용 사용자 프로필 생성
     */
    private static UserProfile createTestProfile(InvestorType investorType) {
        InvestorTypeClassifier classifier = new InvestorTypeClassifier();
        
        UserProfile profile = new UserProfile();
        profile.setUserId(1);
        profile.setInvestorType(investorType);
        
        // 투자 성향에 맞는 점수 범위 설정 (중간값 사용)
        int score = switch (investorType) {
            case CONSERVATIVE -> 20;  // 12-24 범위 중간
            case MODERATE -> 30;      // 25-32 범위 중간
            case AGGRESSIVE -> 38;    // 33-40 범위 중간
            case SPECULATIVE -> 44;   // 41-48 범위 중간
        };
        profile.setTotalScore(score);
        
        RiskSettings riskSettings = classifier.getRiskSettings(investorType);
        profile.setRiskSettings(riskSettings);
        
        TradingStrategy strategy = classifier.getStrategy(investorType);
        profile.setTradingStrategy(strategy);
        
        return profile;
    }
}

