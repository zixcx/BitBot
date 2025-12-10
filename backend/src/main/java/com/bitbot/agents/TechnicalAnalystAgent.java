package com.bitbot.agents;

import com.bitbot.exceptions.AnalysisException;
import com.bitbot.models.InvestorType;
import com.bitbot.models.MarketData;
import com.bitbot.models.TradingDecision;
import com.bitbot.models.UserProfile;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 기술적 분석 에이전트
 * 차트, 가격, 기술 지표를 분석하여 매매 의견 제시
 */
public class TechnicalAnalystAgent {
    
    private static final Logger logger = LoggerFactory.getLogger(TechnicalAnalystAgent.class);
    private final GeminiClient geminiClient;
    
    public TechnicalAnalystAgent() {
        this.geminiClient = new GeminiClient();
    }
    
    /**
     * 기술적 분석 수행
     * @param marketDataList 최근 시장 데이터 (최소 20개 권장)
     * @param userProfile 사용자 프로필 (투자 성향 정보, null 가능)
     * @return 분석 결과 (매수/매도/관망)
     * @throws AnalysisException 분석 실패 시
     */
    public TradingDecision analyze(List<MarketData> marketDataList, UserProfile userProfile) throws AnalysisException {
        try {
            if (marketDataList.isEmpty()) {
                throw new IllegalArgumentException("분석할 데이터가 없습니다");
            }
            
            MarketData latest = marketDataList.get(marketDataList.size() - 1);
            
            // 프롬프트 생성 (프로필 정보 포함)
            String prompt = buildPrompt(marketDataList, latest, userProfile);
            
            if (userProfile != null) {
                logger.info("[기술적 분석 에이전트] LLM 호출 시작... (투자 성향: {})", 
                        userProfile.getInvestorType().getKoreanName());
            } else {
                logger.info("[기술적 분석 에이전트] LLM 호출 시작...");
            }
            String response = geminiClient.callGemini(prompt);
            
            // JSON 파싱
            JsonNode jsonResponse = geminiClient.parseJSONResponse(response);
            
            String decisionStr = jsonResponse.get("decision").asText();
            double confidence = jsonResponse.get("confidence").asDouble();
            String reason = jsonResponse.get("reason").asText();
            
            TradingDecision.Decision decision = TradingDecision.Decision.valueOf(decisionStr);
            
            TradingDecision result = new TradingDecision(
                    "Technical Analyst",
                    decision,
                    confidence,
                    reason
            );
            
            logger.info("[기술적 분석] 결과: {}", result);
            return result;
            
        } catch (Exception e) {
            logger.error("기술적 분석 실패", e);
            throw new AnalysisException("기술적 분석 실패: " + e.getMessage(), e);
        }
    }
    
    private String buildPrompt(List<MarketData> marketDataList, MarketData latest, UserProfile profile) {
        // 최근 데이터 요약
        MarketData prev = marketDataList.size() > 1 ? 
                marketDataList.get(marketDataList.size() - 2) : latest;
        
        double priceChange = latest.getClose() - prev.getClose();
        double priceChangePercent = (priceChange / prev.getClose()) * 100;
        
        String marketDataSummary = String.format("""
                현재 가격: $%.2f (전일 대비 %.2f%%)
                24시간 고가: $%.2f
                24시간 저가: $%.2f
                거래량: %.2f BTC
                
                기술 지표:
                - RSI(14): %.2f %s
                - 단기 이동평균(20): $%.2f
                - 장기 이동평균(60): $%.2f
                - MACD: %.2f, Signal: %.2f
                - 볼린저밴드 상단: $%.2f, 하단: $%.2f
                """,
                latest.getClose(), priceChangePercent,
                latest.getHigh(), latest.getLow(), latest.getVolume(),
                latest.getRsi() != null ? latest.getRsi() : 0,
                getRSIStatus(latest.getRsi()),
                latest.getMaShort() != null ? latest.getMaShort() : latest.getClose(),
                latest.getMaLong() != null ? latest.getMaLong() : latest.getClose(),
                latest.getMacd() != null ? latest.getMacd() : 0,
                latest.getMacdSignal() != null ? latest.getMacdSignal() : 0,
                latest.getBollingerUpper() != null ? latest.getBollingerUpper() : latest.getClose(),
                latest.getBollingerLower() != null ? latest.getBollingerLower() : latest.getClose()
        );
        
        // 투자 성향별 분석 스타일
        String investorContext = "";
        if (profile != null) {
            var riskSettings = profile.getRiskSettings();
            investorContext = String.format("""
                    
                    [투자자 프로필]
                    투자 성향: %s
                    거래 전략: %s
                    레버리지: %s
                    손절 기준: %.1f%%
                    익절 기준: +%.1f%%
                    진입 비중: %.1f%%
                    
                    분석 시 주의사항:
                    """, 
                    profile.getInvestorType().getKoreanName(),
                    profile.getTradingStrategy().getKoreanName(),
                    riskSettings.isLeverageAllowed() ? riskSettings.getMaxLeverage() + "배" : "1배 (현물)",
                    riskSettings.getMaxLossPercent(),
                    riskSettings.getTakeProfitPercent(),
                    riskSettings.getMaxPositionPercent());
            
            // 성향별 분석 스타일 지시
            switch (profile.getInvestorType()) {
                case CONSERVATIVE:
                    investorContext += """
                        - 원금 보호를 최우선으로 하는 보수적 분석
                        - 확실한 신호가 있을 때만 매수 추천
                        - 불확실하면 HOLD 권장
                        - RSI 30 이하 같은 명확한 과매도 구간에서만 진입 고려
                        - 신뢰도 0.8 이상일 때만 추천
                        - 손절 -15%를 염두에 두고, 리스크가 큰 신호는 피하세요
                        - DCA 전략: 작은 비중(5%)으로 분할 매수 기회 포착
                        """;
                    break;
                    
                case MODERATE:
                    investorContext += """
                        - 안정적 수익 추구, 추세 확인 후 진입
                        - 골든크로스, MACD 양수 같은 추세 신호 중시
                        - 추세 이탈 시 빠른 매도 고려
                        - 신뢰도 0.7 이상 고려
                        - 손절 -7%를 염두에 두고, 추세 전환 신호에 주의하세요
                        - 추세 추종 전략: 명확한 상승 추세 확인 후 진입
                        """;
                    break;
                    
                case AGGRESSIVE:
                    investorContext += """
                        - 변동성을 활용한 수익 추구
                        - 볼린저밴드 상/하단 매매 기회 포착
                        - 역추세 전략 고려
                        - 단기 스윙 기회 적극 활용
                        - 신뢰도 0.6 이상도 고려
                        - 레버리지 3배 사용: 손절 -5%를 염두에 두고 신중하게
                        - 스윙 트레이딩: 중기 변동성 활용, 빠른 진입/청산
                        - 손실 확대 위험이 있으므로 명확한 신호만 추천
                        """;
                    break;
                    
                case SPECULATIVE:
                    investorContext += """
                        - 높은 수익 추구, 공격적 분석
                        - 변동성 돌파, 빠른 진입/청산 고려
                        - 양방향 매매 (롱/숏) 기회 포착
                        - 단기 변동성 활용 적극 추천
                        - 신뢰도 0.5 이상도 고려
                        - 레버리지 10배 사용: 손절 -3%를 염두에 두고 매우 신중하게
                        - 변동성 돌파 전략: 변동성 확대 시점 포착, 빠른 반응
                        - 레버리지로 손실이 10배 확대될 수 있으므로, 확실한 기회만 추천
                        - 단기 변동성에 민감하게 반응하되, 리스크 관리 필수
                        """;
                    break;
            }
        }
        
        String systemRole = "당신은 20년 경력의 월스트리트 퀀트 트레이더이자 차트 분석 전문가입니다.";
        
        String task = String.format("""
                주어진 비트코인 시장 데이터를 기술적 분석 관점에서 분석하여, 
                다음 1시간 내 비트코인 가격 방향성을 예측하고 'BUY', 'SELL', 'HOLD' 중 
                가장 유리한 포지션을 추천하세요.
                
                %s
                
                분석 시 고려사항:
                1. RSI가 30 이하면 과매도(매수 시그널), 70 이상이면 과매수(매도 시그널)
                2. 단기 이평선이 장기 이평선을 상향 돌파하면 골든크로스(매수)
                3. MACD가 시그널선을 상향 돌파하면 매수 시그널
                4. 볼린저밴드 하단 터치 후 반등은 매수, 상단 터치는 매도
                5. 거래량 증가와 함께 가격 상승은 강한 신호
                
                신뢰도는 0.0~1.0 사이로 표현하며, 근거를 명확히 제시하세요.
                """, investorContext);
        
        String outputFormat = """
                {
                  "agent": "Technical Analyst",
                  "decision": "BUY",
                  "confidence": 0.85,
                  "reason": "RSI 지수가 28로 과매도 구간에 진입했으며, 볼린저밴드 하단을 터치 후 반등 시그널이 포착됨. 거래량도 증가 추세."
                }
                """;
        
        return GeminiClient.wrapPromptForJSON(systemRole, task, marketDataSummary, outputFormat);
    }
    
    private String getRSIStatus(Double rsi) {
        if (rsi == null) return "(데이터 없음)";
        if (rsi < 30) return "(과매도)";
        if (rsi > 70) return "(과매수)";
        return "(중립)";
    }
}


