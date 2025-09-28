package com.edtech.dto;

import org.springframework.web.multipart.MultipartFile;

public class GenerateRequest {
    private MultipartFile image;
    private String subject;
    private String difficulty;
    private Integer questionCount;
    
    // Getters and setters
    public MultipartFile getImage() { return image; }
    public void setImage(MultipartFile image) { this.image = image; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    
    public Integer getQuestionCount() { return questionCount; }
    public void setQuestionCount(Integer questionCount) { this.questionCount = questionCount; }
}
