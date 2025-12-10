package com.bitbot.server.controller;

import com.bitbot.database.QuestionnaireRepository;
import com.bitbot.models.Questionnaire;
import com.bitbot.models.UserProfile;
import com.bitbot.server.dto.ApiResponse;
import com.bitbot.service.QuestionnaireService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 설문조사 관련 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/questionnaire")
@CrossOrigin(origins = "*")
public class QuestionnaireController {
    
    private static final Logger logger = LoggerFactory.getLogger(QuestionnaireController.class);
    private final QuestionnaireService questionnaireService;
    private final QuestionnaireRepository questionnaireRepository;
    
    // 기본 사용자 ID (실제로는 인증 시스템에서 가져옴)
    private static final Integer DEFAULT_USER_ID = 1;
    
    public QuestionnaireController() {
        this.questionnaireService = new QuestionnaireService();
        this.questionnaireRepository = new QuestionnaireRepository();
    }
    
    /**
     * 설문조사 제출
     * POST /api/questionnaire/submit
     */
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> submitQuestionnaire(
            @RequestBody QuestionnaireRequest request) {
        try {
            // Questionnaire 객체 생성
            Questionnaire questionnaire = new Questionnaire();
            questionnaire.setUserId(DEFAULT_USER_ID);
            questionnaire.setAnswers(request.getAnswers());
            questionnaire.calculateTotalScore();
            
            // 설문조사 처리 및 프로필 생성
            UserProfile profile = questionnaireService.processQuestionnaire(questionnaire);
            
            Map<String, Object> result = new HashMap<>();
            result.put("profile", profile);
            result.put("investorType", profile.getInvestorType().name());
            result.put("tradingStrategy", profile.getTradingStrategy().name());
            result.put("totalScore", questionnaire.getTotalScore());
            
            return ResponseEntity.ok(ApiResponse.success("설문조사 제출 성공", result));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error("설문조사 제출 실패: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("설문조사 제출 실패", e);
            return ResponseEntity.ok(ApiResponse.error("설문조사 제출 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 최근 설문조사 조회
     * GET /api/questionnaire/latest
     */
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<Questionnaire>> getLatestQuestionnaire() {
        try {
            Questionnaire questionnaire = questionnaireRepository.findLatestByUserId(DEFAULT_USER_ID);
            if (questionnaire == null) {
                return ResponseEntity.ok(ApiResponse.error("설문조사가 없습니다."));
            }
            return ResponseEntity.ok(ApiResponse.success(questionnaire));
        } catch (Exception e) {
            logger.error("설문조사 조회 실패", e);
            return ResponseEntity.ok(ApiResponse.error("설문조사 조회 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 설문조사 요청 DTO
     */
    public static class QuestionnaireRequest {
        private Map<String, Integer> answers;  // {"q1": 1, "q2": 2, ...}
        
        public Map<String, Integer> getAnswers() {
            return answers;
        }
        
        public void setAnswers(Map<String, Integer> answers) {
            this.answers = answers;
        }
    }
}

