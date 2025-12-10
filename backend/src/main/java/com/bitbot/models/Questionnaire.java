package com.bitbot.models;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 설문조사 응답 모델
 */
public class Questionnaire {
    
    private Long id;
    private Integer userId;
    private Map<String, Integer> answers;  // Q1~Q15 답변
    private int totalScore;  // 12~48
    private InvestorType resultType;
    private LocalDateTime completedAt;
    
    public Questionnaire() {
        this.answers = new HashMap<>();
        this.completedAt = LocalDateTime.now();
    }
    
    public Questionnaire(Integer userId) {
        this();
        this.userId = userId;
    }
    
    /**
     * 설문 답변 추가
     */
    public void addAnswer(String question, int score) {
        answers.put(question, score);
        calculateTotalScore();
    }
    
    /**
     * 총점 계산
     */
    private void calculateTotalScore() {
        totalScore = answers.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
        resultType = InvestorType.fromScore(totalScore);
    }
    
    /**
     * 총점 재계산 (외부에서 호출 가능)
     */
    public void recalculateScore() {
        calculateTotalScore();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public Map<String, Integer> getAnswers() {
        return answers;
    }
    
    public void setAnswers(Map<String, Integer> answers) {
        this.answers = answers;
        calculateTotalScore();
    }
    
    public int getTotalScore() {
        return totalScore;
    }
    
    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
        this.resultType = InvestorType.fromScore(totalScore);
    }
    
    public InvestorType getResultType() {
        return resultType;
    }
    
    public void setResultType(InvestorType resultType) {
        this.resultType = resultType;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    /**
     * 특정 질문의 답변 가져오기
     */
    public Integer getAnswer(String question) {
        return answers.get(question);
    }
    
    @Override
    public String toString() {
        return String.format("Questionnaire[사용자: %d, 총점: %d, 성향: %s]",
                userId, totalScore, resultType != null ? resultType.getKoreanName() : "미분류");
    }
}

