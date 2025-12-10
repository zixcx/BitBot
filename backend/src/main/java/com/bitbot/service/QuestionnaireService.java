package com.bitbot.service;

import com.bitbot.classification.InvestorTypeClassifier;
import com.bitbot.database.UserProfileRepository;
import com.bitbot.models.InvestorType;
import com.bitbot.models.Questionnaire;
import com.bitbot.models.RiskSettings;
import com.bitbot.models.TradingStrategy;
import com.bitbot.models.UserProfile;
import com.bitbot.utils.ValidationUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 설문조사 처리 서비스
 */
public class QuestionnaireService {
    
    private static final Logger logger = LoggerFactory.getLogger(QuestionnaireService.class);
    private final InvestorTypeClassifier classifier;
    private final UserProfileRepository profileRepository;
    private final ObjectMapper objectMapper;
    
    public QuestionnaireService() {
        this.classifier = new InvestorTypeClassifier();
        this.profileRepository = new UserProfileRepository();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 설문조사 처리 및 프로필 생성
     * @param questionnaire 설문조사 응답
     * @return 생성된 사용자 프로필
     */
    public UserProfile processQuestionnaire(Questionnaire questionnaire) {
        try {
            logger.info("설문조사 처리 시작: {}", questionnaire);
            
            // 0. 입력값 검증
            ValidationUtil.validateUserId(questionnaire.getUserId());
            
            // 1. 총점 계산 (이미 Questionnaire에서 계산됨)
            int totalScore = questionnaire.getTotalScore();
            
            // 총점 검증 (12-48 범위)
            if (totalScore < 12 || totalScore > 48) {
                throw new IllegalArgumentException(
                    String.format("총점은 12-48 사이여야 합니다 (현재: %d)", totalScore)
                );
            }
            
            logger.info("설문 총점: {}점", totalScore);
            
            // 2. 성향 분류
            InvestorType type = classifier.classify(totalScore);
            questionnaire.setResultType(type);
            
            // 3. 리스크 설정 생성
            RiskSettings riskSettings = classifier.getRiskSettings(type);
            
            // 4. 전략 선택
            TradingStrategy strategy = classifier.getStrategy(type);
            
            // 5. 프로필 생성
            UserProfile profile = new UserProfile(
                questionnaire.getUserId(),
                type,
                totalScore,
                riskSettings,
                strategy
            );
            
            // 6. 프로필 저장
            profileRepository.save(profile);
            logger.info("사용자 프로필 저장 완료: {}", profile);
            
            return profile;
            
        } catch (Exception e) {
            logger.error("설문조사 처리 실패", e);
            throw new RuntimeException("설문조사 처리 실패", e);
        }
    }
    
    /**
     * 설문조사 점수 계산 (Q1~Q11, Q14)
     * 총 12문항의 점수를 합산
     */
    public int calculateScore(Questionnaire questionnaire) {
        int totalScore = 0;
        
        // Q1~Q11, Q14 점수 합산
        for (int i = 1; i <= 11; i++) {
            Integer score = questionnaire.getAnswer("Q" + i);
            if (score != null) {
                totalScore += score;
            }
        }
        
        // Q14 점수 추가
        Integer q14Score = questionnaire.getAnswer("Q14");
        if (q14Score != null) {
            totalScore += q14Score;
        }
        
        questionnaire.setTotalScore(totalScore);
        return totalScore;
    }
}

