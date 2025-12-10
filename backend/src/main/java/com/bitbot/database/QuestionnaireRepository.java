package com.bitbot.database;

import com.bitbot.models.InvestorType;
import com.bitbot.models.Questionnaire;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 설문조사 응답 데이터베이스 저장소
 */
public class QuestionnaireRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(QuestionnaireRepository.class);
    private final ObjectMapper objectMapper;
    
    public QuestionnaireRepository() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 설문조사 응답 저장
     */
    public void save(Questionnaire questionnaire) {
        // 먼저 사용자가 존재하는지 확인하고 없으면 생성
        UserRepository userRepo = new UserRepository();
        userRepo.getOrCreateUser(questionnaire.getUserId());
        
        String sql = """
            INSERT INTO questionnaires (
                user_id, answers, total_score, result_type, completed_at
            ) VALUES (?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, questionnaire.getUserId());
            
            // 답변을 JSON으로 변환
            String answersJson = objectMapper.writeValueAsString(questionnaire.getAnswers());
            stmt.setString(2, answersJson);
            
            stmt.setInt(3, questionnaire.getTotalScore());
            stmt.setString(4, questionnaire.getResultType() != null ? 
                    questionnaire.getResultType().name() : "UNKNOWN");
            stmt.setString(5, questionnaire.getCompletedAt() != null ? 
                    questionnaire.getCompletedAt().toString() : LocalDateTime.now().toString());
            
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                logger.info("설문조사 응답 저장 완료: {}", questionnaire);
            }
            
        } catch (Exception e) {
            logger.error("설문조사 응답 저장 실패", e);
            throw new RuntimeException("설문조사 저장 실패", e);
        }
    }
    
    /**
     * 사용자의 최신 설문조사 조회
     */
    public Questionnaire findLatestByUserId(Integer userId) {
        String sql = """
            SELECT * FROM questionnaires 
            WHERE user_id = ? 
            ORDER BY completed_at DESC 
            LIMIT 1
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToQuestionnaire(rs);
            }
            
        } catch (Exception e) {
            logger.error("설문조사 조회 실패: userId={}", userId, e);
        }
        
        return null;
    }
    
    private Questionnaire mapResultSetToQuestionnaire(ResultSet rs) throws Exception {
        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setId(rs.getLong("id"));
        questionnaire.setUserId(rs.getInt("user_id"));
        questionnaire.setTotalScore(rs.getInt("total_score"));
        
        String resultTypeStr = rs.getString("result_type");
        if (resultTypeStr != null && !resultTypeStr.equals("UNKNOWN")) {
            questionnaire.setResultType(InvestorType.valueOf(resultTypeStr));
        }
        
        // 답변 JSON 파싱
        String answersJson = rs.getString("answers");
        if (answersJson != null) {
            @SuppressWarnings("unchecked")
            Map<String, Integer> answers = objectMapper.readValue(answersJson, Map.class);
            questionnaire.setAnswers(answers);
        }
        
        // 날짜 파싱
        String completedAtStr = rs.getString("completed_at");
        if (completedAtStr != null) {
            try {
                questionnaire.setCompletedAt(LocalDateTime.parse(completedAtStr.replace(" ", "T")));
            } catch (Exception e) {
                logger.warn("날짜 파싱 실패: {}", completedAtStr);
            }
        }
        
        return questionnaire;
    }
}

