package com.bitbot.agents;

import com.bitbot.models.TradingDecision;
import com.bitbot.models.UserProfile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 총괄 코디네이터 에이전트
 * 모든 전문 에이전트의 분석 결과를 종합하여 최종 예비 결정을 내림
 */
public class MasterCoordinatorAgent {
    
    private static final Logger logger = LoggerFactory.getLogger(MasterCoordinatorAgent.class);
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;
    
    public MasterCoordinatorAgent() {
        this.geminiClient = new GeminiClient();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 전문 에이전트들의 보고서를 종합하여 최종 결정
     * @param agentReports 각 에이전트의 분석 결과 리스트
     * @param userProfile 사용자 프로필 (투자 성향 정보, null 가능)
     * @return 최종 예비 투자 결정
     */
    public TradingDecision coordinateDecision(List<TradingDecision> agentReports, UserProfile userProfile) {
        try {
            if (agentReports.isEmpty()) {
                throw new IllegalArgumentException("분석 보고서가 없습니다");
            }
            
            String prompt = buildPrompt(agentReports, userProfile);
            
            if (userProfile != null) {
                logger.info("[총괄 코디네이터] 보고서 종합 및 최종 결정 중... (투자 성향: {})", 
                        userProfile.getInvestorType().getKoreanName());
            } else {
                logger.info("[총괄 코디네이터] 보고서 종합 및 최종 결정 중...");
            }
            String response = geminiClient.callGemini(prompt);
            
            // JSON 파싱
            JsonNode jsonResponse = geminiClient.parseJSONResponse(response);
            
            String decisionStr = jsonResponse.get("preliminary_decision").asText();
            String reason = jsonResponse.get("summary_reason").asText();
            
            // 신뢰도는 각 에이전트의 평균으로 계산
            double avgConfidence = agentReports.stream()
                    .mapToDouble(TradingDecision::getConfidence)
                    .average()
                    .orElse(0.5);
            
            TradingDecision.Decision decision = TradingDecision.Decision.valueOf(decisionStr);
            
            TradingDecision result = new TradingDecision(
                    "Master Coordinator",
                    decision,
                    avgConfidence,
                    reason
            );
            
            logger.info("[총괄 코디네이터] 최종 예비 결정: {}", result);
            return result;
            
        } catch (Exception e) {
            logger.error("총괄 코디네이터 분석 실패", e);
            return new TradingDecision(
                    "Master Coordinator",
                    TradingDecision.Decision.HOLD,
                    0.0,
                    "결정 실패: " + e.getMessage()
            );
        }
    }
    
    private String buildPrompt(List<TradingDecision> agentReports, UserProfile profile) {
        // 각 에이전트의 보고서를 JSON 형식으로 변환
        StringBuilder reportsJson = new StringBuilder("[\n");
        
        for (int i = 0; i < agentReports.size(); i++) {
            TradingDecision report = agentReports.get(i);
            reportsJson.append(String.format("""
                  {
                    "agent": "%s",
                    "decision": "%s",
                    "confidence": %.2f,
                    "reason": "%s"
                  }""",
                    report.getAgentName(),
                    report.getDecision(),
                    report.getConfidence(),
                    report.getReason().replace("\"", "'")
            ));
            
            if (i < agentReports.size() - 1) {
                reportsJson.append(",\n");
            }
        }
        reportsJson.append("\n]");
        
        // 투자 성향별 의사결정 원칙
        String decisionPrinciple = """
                의사결정 원칙:
                1. 3개 에이전트의 의견이 2개 이상 일치하면 그 방향을 우선 고려
                2. 의견이 첨예하게 대립하면 'HOLD'로 보수적 접근
                3. 높은 신뢰도의 의견에 더 큰 가중치 부여
                4. 시장 심리가 극단적(극단적 공포/탐욕)이면 변동성 우려하여 신중히 판단
                5. 기술적 분석과 심리 분석이 상충하면, 단기 변동성이 크다고 판단
                """;
        
        if (profile != null) {
            var riskSettings = profile.getRiskSettings();
            decisionPrinciple += String.format("""
                    
                    [투자자 프로필]
                    투자 성향: %s
                    거래 전략: %s
                    레버리지: %s
                    손절 기준: %.1f%%
                    익절 기준: +%.1f%%
                    진입 비중: %.1f%%
                    
                    """, 
                    profile.getInvestorType().getKoreanName(),
                    profile.getTradingStrategy().getKoreanName(),
                    riskSettings.isLeverageAllowed() ? riskSettings.getMaxLeverage() + "배" : "1배 (현물)",
                    riskSettings.getMaxLossPercent(),
                    riskSettings.getTakeProfitPercent(),
                    riskSettings.getMaxPositionPercent());
            
            switch (profile.getInvestorType()) {
                case CONSERVATIVE:
                    decisionPrinciple += """
                        [보수적 의사결정]
                        - 모든 에이전트가 일치할 때만 행동
                        - 불확실하면 HOLD 우선
                        - 신뢰도 0.8 이상일 때만 추천
                        - 원금 보호 최우선
                        - 손절 -15%를 염두에 두고, 리스크가 큰 결정은 피하세요
                        - DCA 전략: 작은 비중(5%)으로 분할 매수만 고려
                        - 레버리지 없음: 현물 거래만 사용
                        """;
                    break;
                    
                case MODERATE:
                    decisionPrinciple += """
                        [균형잡힌 의사결정]
                        - 2개 이상 에이전트 일치 시 행동
                        - 신뢰도 0.7 이상 고려
                        - 안정적 수익 추구
                        - 손절 -7%를 염두에 두고, 추세 전환 시 주의
                        - 추세 추종 전략: 명확한 추세 확인 후 진입
                        - 레버리지 없음: 현물 거래만 사용
                        """;
                    break;
                    
                case AGGRESSIVE:
                    decisionPrinciple += """
                        [공격적 의사결정]
                        - 1개 에이전트라도 강한 신호면 행동 고려
                        - 신뢰도 0.6 이상도 고려
                        - 변동성 기회 적극 활용
                        - 레버리지 3배 사용: 손절 -5%를 염두에 두고 매우 신중하게
                        - 스윙 트레이딩: 중기 변동성 활용, 빠른 진입/청산
                        - 레버리지로 손실이 3배 확대될 수 있으므로, 확실한 기회만 선택
                        - 손실 확대 위험을 항상 염두에 두고 결정하세요
                        """;
                    break;
                    
                case SPECULATIVE:
                    decisionPrinciple += """
                        [매우 공격적 의사결정]
                        - 약한 신호도 기회로 활용
                        - 신뢰도 0.5 이상도 고려
                        - 빠른 진입/청산 우선
                        - 높은 수익 추구
                        - 레버리지 10배 사용: 손절 -3%를 염두에 두고 극도로 신중하게
                        - 변동성 돌파 전략: 변동성 확대 시점 포착, 매우 빠른 반응
                        - 레버리지로 손실이 10배 확대될 수 있으므로, 리스크 관리 최우선
                        - 높은 수익 추구하되, 손실 확대 위험을 항상 최우선으로 고려
                        - 빠른 진입/청산이 중요하지만, 무모한 결정은 피하세요
                        """;
                    break;
            }
        }
        
        String systemRole = "당신은 수조 원을 운용하는 헤지펀드의 최고 투자 책임자(CIO)입니다.";
        
        String task = String.format("""
                각 분야 전문가들의 상이한 보고서를 종합적으로 검토하여,
                가장 합리적인 단일 '예비 투자 결정(Preliminary Decision)'을 내리세요.
                
                %s
                
                결정의 배경이 된 핵심 요약 근거를 2~3문장으로 명확히 정리하세요.
                """, decisionPrinciple);
        
        String outputFormat = """
                {
                  "agent": "Master Coordinator",
                  "preliminary_decision": "HOLD",
                  "summary_reason": "기술적으로는 강력한 매수 신호가 있으나, 시장 심리를 악화시키는 외부 뉴스가 있어 잠재적 변동성이 매우 크다. 따라서 신규 진입보다는 현재 상황을 관망하는 것이 유리하다."
                }
                """;
        
        return GeminiClient.wrapPromptForJSON(systemRole, task, reportsJson.toString(), outputFormat);
    }
}


