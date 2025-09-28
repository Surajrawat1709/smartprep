package com.edtech.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "questions")
public class QuestionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String type;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String questionText;
    
    @Column(columnDefinition = "JSON")
    private String options;
    
    @Column(columnDefinition = "TEXT")
    private String answer;
    
    @Column(columnDefinition = "TEXT")
    private String explanation;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_bank_id")
    private QuestionBank questionBank;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    
    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }
    
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    
    public QuestionBank getQuestionBank() { return questionBank; }
    public void setQuestionBank(QuestionBank questionBank) { this.questionBank = questionBank; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
