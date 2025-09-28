package com.edtech.dto;

import java.util.List;

public class GenerateResponse {
    private List<Question> questions;
    private String status;
    private String message;
    
    public GenerateResponse() {}
    
    public GenerateResponse(List<Question> questions, String status, String message) {
        this.questions = questions;
        this.status = status;
        this.message = message;
    }
    
    // Getters and setters
    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
