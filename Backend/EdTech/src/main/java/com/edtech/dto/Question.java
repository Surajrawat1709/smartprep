package com.edtech.dto;

import java.util.List;

public class Question {
    private String id;
    private String type; // "MCQ", "ONE_WORD", "PARAGRAPH"
    private String question;
    private List<String> options;
    private String answer;
    private String explanation;
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
    
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
