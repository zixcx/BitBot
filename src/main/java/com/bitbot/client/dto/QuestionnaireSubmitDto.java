package com.bitbot.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Questionnaire Submit Request DTO
 */
public class QuestionnaireSubmitDto {
    
    @JsonProperty("answers")
    private Map<String, Integer> answers;

    public QuestionnaireSubmitDto() {}

    public QuestionnaireSubmitDto(Map<String, Integer> answers) {
        this.answers = answers;
    }

    public Map<String, Integer> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<String, Integer> answers) {
        this.answers = answers;
    }
}

