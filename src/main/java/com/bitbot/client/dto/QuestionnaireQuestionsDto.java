package com.bitbot.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * DTO for questionnaire questions response
 */
public class QuestionnaireQuestionsDto {
    
    private List<QuestionSection> sections;
    
    @JsonProperty("totalQuestions")
    private int totalQuestions;
    
    @JsonProperty("scoringQuestions")
    private List<String> scoringQuestions;
    
    @JsonProperty("nonScoringQuestions")
    private List<String> nonScoringQuestions;
    
    @JsonProperty("scoreRange")
    private ScoreRange scoreRange;

    public QuestionnaireQuestionsDto() {}

    // Getters and Setters
    public List<QuestionSection> getSections() {
        return sections;
    }

    public void setSections(List<QuestionSection> sections) {
        this.sections = sections;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public List<String> getScoringQuestions() {
        return scoringQuestions;
    }

    public void setScoringQuestions(List<String> scoringQuestions) {
        this.scoringQuestions = scoringQuestions;
    }

    public List<String> getNonScoringQuestions() {
        return nonScoringQuestions;
    }

    public void setNonScoringQuestions(List<String> nonScoringQuestions) {
        this.nonScoringQuestions = nonScoringQuestions;
    }

    public ScoreRange getScoreRange() {
        return scoreRange;
    }

    public void setScoreRange(ScoreRange scoreRange) {
        this.scoreRange = scoreRange;
    }

    // Inner Classes
    public static class QuestionSection {
        private String id;
        private String title;
        private String description;
        private List<Question> questions;

        public QuestionSection() {}

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<Question> getQuestions() {
            return questions;
        }

        public void setQuestions(List<Question> questions) {
            this.questions = questions;
        }
    }

    public static class Question {
        private String id;
        private String text;
        
        @JsonProperty("hasScore")
        private boolean hasScore;
        
        private List<QuestionOption> options;

        public Question() {}

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public boolean isHasScore() {
            return hasScore;
        }

        public void setHasScore(boolean hasScore) {
            this.hasScore = hasScore;
        }

        public List<QuestionOption> getOptions() {
            return options;
        }

        public void setOptions(List<QuestionOption> options) {
            this.options = options;
        }
    }

    public static class QuestionOption {
        private int value;
        private String text;

        public QuestionOption() {}

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    public static class ScoreRange {
        private int min;
        private int max;

        public ScoreRange() {}

        public int getMin() {
            return min;
        }

        public void setMin(int min) {
            this.min = min;
        }

        public int getMax() {
            return max;
        }

        public void setMax(int max) {
            this.max = max;
        }
    }
}

