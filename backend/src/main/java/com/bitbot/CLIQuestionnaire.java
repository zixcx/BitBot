package com.bitbot;

import com.bitbot.models.Questionnaire;
import com.bitbot.service.QuestionnaireService;
import com.bitbot.utils.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * 설문조사 CLI 인터페이스
 * 사용자에게 설문을 진행하고 프로필을 생성
 */
public class CLIQuestionnaire {
    
    private static final Logger logger = LoggerFactory.getLogger(CLIQuestionnaire.class);
    private final QuestionnaireService questionnaireService;
    private final Scanner scanner;
    
    public CLIQuestionnaire() {
        this.questionnaireService = new QuestionnaireService();
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * 설문조사 실행
     */
    public void runQuestionnaire(Integer userId) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("투자 성향 설문조사");
        System.out.println("=".repeat(80));
        System.out.println("각 질문에 대해 1~4 중 하나를 선택하세요.\n");
        
        Questionnaire questionnaire = new Questionnaire(userId);
        
        // Section A: 재무 상황
        System.out.println("[Section A] 재무 상황 및 자금 성격\n");
        
        askQuestion(questionnaire, "Q1", 
                "본 프로그램에 투입할 투자 자산의 비중은 귀하의 전체 금융 자산 중 어느 정도입니까?",
                "1. 10% 미만 (없어도 생활에 지장 없는 자금) [4점]",
                "2. 10% ~ 30% (여유 자금의 일부) [3점]",
                "3. 30% ~ 50% (상당한 비중의 목돈) [2점]",
                "4. 50% 이상 (전 재산에 가까움) [1점]");
        
        askQuestion(questionnaire, "Q2",
                "투입된 자금의 향후 사용 계획은 어떻습니까?",
                "1. 3년 이상 사용할 계획이 없는 순수 장기 투자금 [4점]",
                "2. 1년 정도는 묵혀둘 수 있는 여유 자금 [3점]",
                "3. 6개월 내에 주택 자금 등 다른 용도로 사용될 가능성 있음 [2점]",
                "4. 언제든지 현금화가 필요한 비상금 성격 [1점]");
        
        askQuestion(questionnaire, "Q3",
                "귀하의 현재 소득 안정성은 어떻습니까?",
                "1. 매우 안정적이며 정기적인 추가 입금이 가능함 [4점]",
                "2. 안정적이나 추가 입금은 어려움 [3점]",
                "3. 불규칙하여 투자 수익이 생활비로 충당될 수 있음 [2점]",
                "4. 현재 소득이 없거나 매우 불안정함 [1점]");
        
        // Section B: 투자 경험
        System.out.println("\n[Section B] 투자 경험 및 지식\n");
        
        askQuestion(questionnaire, "Q4",
                "암호화폐(가상자산) 또는 주식 투자 경력은 어느 정도입니까?",
                "1. 3년 이상 [4점]",
                "2. 1년 ~ 3년 미만 [3점]",
                "3. 1년 미만 (기초 지식 있음) [2점]",
                "4. 경험 없음 (이번이 처음) [1점]");
        
        askQuestion(questionnaire, "Q5",
                "파생상품(선물/옵션) 및 레버리지(Leverage) 구조에 대해 이해하고 계십니까?",
                "1. 매우 잘 이해하며, 청산(Liquidation) 위험도 알고 있음 [4점]",
                "2. 개념은 알고 있으나 직접 투자해 본 적은 없음 [3점]",
                "3. 들어본 적은 있으나 정확한 원리는 모름 [2점]",
                "4. 전혀 모름 [1점]");
        
        askQuestion(questionnaire, "Q6",
                "차트 분석 시 보조지표(RSI, MACD, 볼린저밴드 등)를 활용하십니까?",
                "1. 나만의 매매 지표 조합과 기준이 명확함 [4점]",
                "2. 기본적인 지표(이동평균선 등)는 참고함 [3점]",
                "3. 용어 정도만 알고 있음 [2점]",
                "4. 차트를 볼 줄 모름 [1점]");
        
        // Section C: 위험 감수 성향
        System.out.println("\n[Section C] 위험 감수 성향\n");
        
        askQuestion(questionnaire, "Q7",
                "투자 원금 1,000만 원이 일주일 만에 800만 원(-20%)이 되었습니다. 귀하의 심정은?",
                "1. 어차피 오를 것이므로 신경 쓰지 않거나, 물타기 기회로 삼는다. [4점]",
                "2. 불안하지만 회복을 기다리며 관망한다. [3점]",
                "3. 밤잠을 설치며, 원금 회복이 될지 걱정한다. [2점]",
                "4. 즉시 전량 매도하여 남은 돈이라도 건진다. [1점]");
        
        askQuestion(questionnaire, "Q8",
                "귀하가 기대하는 '수익'과 '위험'의 교환 비율(Trade-off)은?",
                "1. 원금 손실 가능성이 높더라도 연 100% 이상의 수익을 원함 [4점]",
                "2. 어느 정도의 손실(-20%)을 감수하고 연 30~50% 수익 추구 [3점]",
                "3. 제한된 손실(-10%) 내에서 연 10~20% 수익 추구 [2점]",
                "4. 손실은 절대 싫으며, 은행 이자보다 조금 더 높은 수준이면 만족 [1점]");
        
        askQuestion(questionnaire, "Q9",
                "비트코인의 높은 변동성(하루 ±10% 이상 등락)에 대한 귀하의 생각은?",
                "1. 변동성이야말로 수익의 원천이다. 클수록 좋다. [4점]",
                "2. 변동성을 이용하되, 리스크 관리가 필요하다. [3점]",
                "3. 너무 큰 변동성은 스트레스이므로 피하고 싶다. [2점]",
                "4. 변동성이 큰 자산은 투기라고 생각한다. [1점]");
        
        // Section D: 매매 스타일
        System.out.println("\n[Section D] 매매 스타일 선호도\n");
        
        askQuestion(questionnaire, "Q10",
                "선호하는 거래 빈도(Frequency)는?",
                "1. 초단타 (1시간에도 수차례 거래) [4점]",
                "2. 데이 트레이딩 (하루 1~3회 내외) [3점]",
                "3. 스윙 트레이딩 (주 1~2회) [2점]",
                "4. 포지션 트레이딩 (월 1회 미만, 진득하게 보유) [1점]");
        
        askQuestion(questionnaire, "Q11",
                "어떤 시장 상황에서 진입하는 것을 선호하십니까?",
                "1. 가격이 급변할 때 빠르게 올라타서 짧게 먹고 나오기 [4점]",
                "2. 박스권에서 저점에 사서 고점에 팔기 [3점]",
                "3. 확실한 상승 추세가 확인된 후에 안전하게 진입하기 [2점]",
                "4. 가격이 많이 떨어져서 저평가되었다고 판단될 때만 매수 [1점]");
        
        // Section E: 비트코인 시장관
        System.out.println("\n[Section E] 비트코인 시장관\n");
        
        askQuestion(questionnaire, "Q14",
                "선물(Futures) 거래 기능(공매도 포함) 사용을 허용하시겠습니까?",
                "1. 적극 허용 (고배율 레버리지 포함) [4점]",
                "2. 허용하되 레버리지는 3배 이하로 제한 [3점]",
                "3. 헷징(1배 숏) 목적으로만 허용 [2점]",
                "4. 절대 허용 안 함 (현물만 거래) [1점]");
        
        // 설문 완료 및 처리
        System.out.println("\n" + "=".repeat(80));
        System.out.println("설문조사 완료!");
        System.out.println("=".repeat(80));
        System.out.printf("총점: %d점\n", questionnaire.getTotalScore());
        System.out.printf("분류된 투자 성향: %s\n", 
                questionnaire.getResultType() != null ? 
                        questionnaire.getResultType().getKoreanName() : "미분류");
        System.out.println("=".repeat(80) + "\n");
        
        // 프로필 생성
        try {
            var profile = questionnaireService.processQuestionnaire(questionnaire);
            System.out.println("✅ 사용자 프로필이 생성되었습니다!");
            System.out.println(profile);
            System.out.println();
        } catch (Exception e) {
            logger.error("프로필 생성 실패", e);
            System.out.println("❌ 프로필 생성 실패: " + e.getMessage());
        }
    }
    
    private void askQuestion(Questionnaire questionnaire, String questionId, 
                            String question, String... options) {
        System.out.println(question);
        for (String option : options) {
            System.out.println("  " + option);
        }
        System.out.print("답변 (1-4): ");
        
        try {
            int answer = Integer.parseInt(scanner.nextLine().trim());
            
            // 입력값 검증
            try {
                ValidationUtil.validateQuestionnaireAnswer(answer, 1, 4);
                
                // 점수 매핑: 1번 선택 = 4점, 2번 = 3점, 3번 = 2점, 4번 = 1점
                int score = 5 - answer;
                questionnaire.addAnswer(questionId, score);
                System.out.println("✅ 선택: " + answer + "번 (" + score + "점)\n");
            } catch (IllegalArgumentException e) {
                System.out.println("⚠️ " + e.getMessage() + " 기본값 2번(3점) 적용.\n");
                questionnaire.addAnswer(questionId, 3);
            }
        } catch (NumberFormatException e) {
            System.out.println("⚠️ 숫자를 입력하세요. 기본값 2번(3점) 적용.\n");
            questionnaire.addAnswer(questionId, 3);
        }
    }
}

