package com.edtech.controller;

import com.edtech.dto.GenerateRequest;
import com.edtech.dto.GenerateResponse;
import com.edtech.service.QuestionGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/generate")
@CrossOrigin(origins = "http://localhost:4200")
public class QuestionGenerationController {

    private static final Logger logger = LoggerFactory.getLogger(QuestionGenerationController.class);

    @Autowired
    private QuestionGenerationService questionGenerationService;

    @PostMapping(value = "/questions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GenerateResponse> generateQuestions(
            @ModelAttribute GenerateRequest generateRequest) {

        try {
            logger.info("Received question generation request for subject: {}, difficulty: {}, count: {}",
                    generateRequest.getSubject(),
                    generateRequest.getDifficulty(),
                    generateRequest.getQuestionCount());

            // Validate request
            if (generateRequest.getImage() == null || generateRequest.getImage().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new GenerateResponse(null, "error", "Image file is required"));
            }

            // Generate questions
            GenerateResponse response = questionGenerationService.generateQuestions(generateRequest);

            if ("success".equals(response.getStatus())) {
                logger.info("Successfully generated {} questions", response.getQuestions().size());
                return ResponseEntity.ok(response);
            } else {
                logger.error("Failed to generate questions: {}", response.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (Exception e) {
            logger.error("Error generating questions: ", e);
            GenerateResponse errorResponse = new GenerateResponse(
                    null,
                    "error",
                    "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Question Generation Service is running");
    }

    @GetMapping("/test-config")
    public ResponseEntity<Map<String, String>> testConfiguration() {
        Map<String, String> config = new HashMap<>();
        config.put("status", "Configuration test");

        // Test if API key is configured (don't expose the actual key)
        String apiKeyStatus = questionGenerationService.isApiKeyConfigured() ? "Configured" : "Not configured";
        config.put("apiKeyStatus", apiKeyStatus);

        config.put("tesseractPath", "C:/Program Files/Tesseract-OCR/tessdata");

        return ResponseEntity.ok(config);
    }

    @PostMapping("/test-text")
    public ResponseEntity<GenerateResponse> testWithText(
            @RequestParam(defaultValue = "The photosynthesis process in plants converts carbon dioxide and water into glucose using sunlight energy. This process occurs in chloroplasts and is essential for plant growth.") String text,
            @RequestParam(defaultValue = "biology") String subject,
            @RequestParam(defaultValue = "medium") String difficulty,
            @RequestParam(defaultValue = "3") Integer questionCount) {

        try {
            logger.info("Testing question generation with text: {}", text.substring(0, Math.min(50, text.length())));

            GenerateResponse response = questionGenerationService.generateQuestionsFromTextOnly(text, subject,
                    difficulty, questionCount);

            if ("success".equals(response.getStatus())) {
                logger.info("Successfully generated {} test questions", response.getQuestions().size());
                return ResponseEntity.ok(response);
            } else {
                logger.error("Failed to generate test questions: {}", response.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (Exception e) {
            logger.error("Error generating test questions: ", e);
            GenerateResponse errorResponse = new GenerateResponse(
                    null,
                    "error",
                    "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}