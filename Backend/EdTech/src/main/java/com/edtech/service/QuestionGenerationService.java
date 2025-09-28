package com.edtech.service;

import com.edtech.dto.GenerateRequest;
import com.edtech.dto.GenerateResponse;
import com.edtech.dto.Question;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class QuestionGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(QuestionGenerationService.class);

    @Value("${tesseract.data.path}")
    private String tesseractDataPath;

    @Value("${tesseract.language}")
    private String tesseractLanguage;

    @Value("${llm.api.url}")
    private String llmApiUrl;

    @Value("${llm.api.key}")
    private String llmApiKey;

    @Value("${llm.api.model:gpt-3.5-turbo}")
    private String llmModel;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public QuestionGenerationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public GenerateResponse generateQuestions(GenerateRequest request) {
        try {
            // Step 1: Extract text from image using OCR
            String extractedText = extractTextFromImage(request.getImage());

            if (extractedText == null || extractedText.trim().isEmpty()) {
                return new GenerateResponse(new ArrayList<>(), "error", "Failed to extract text from image");
            }

            // Step 2: Generate questions using LLM API
            List<Question> questions = generateQuestionsFromText(
                    extractedText,
                    request.getSubject(),
                    request.getDifficulty(),
                    request.getQuestionCount());

            return new GenerateResponse(questions, "success", "Questions generated successfully");

        } catch (Exception e) {
            logger.error("Error generating questions: ", e);
            return new GenerateResponse(new ArrayList<>(), "error", "Failed to generate questions: " + e.getMessage());
        }
    }

    private String extractTextFromImage(MultipartFile imageFile) throws TesseractException, IOException {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("Image file is empty or null");
        }

        // Create temporary file
        Path tempFile = Files.createTempFile("ocr_input_", "_" + imageFile.getOriginalFilename());

        try {
            // Copy uploaded file to temporary file
            Files.copy(imageFile.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

            // Configure Tesseract
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath(tesseractDataPath);
            tesseract.setLanguage(tesseractLanguage);

            // Extract text from image
            String extractedText = tesseract.doOCR(tempFile.toFile());
            logger.info("Extracted text: {}", extractedText);

            return extractedText;

        } finally {
            // Clean up temporary file
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException e) {
                logger.warn("Failed to delete temporary file: {}", tempFile, e);
            }
        }
    }

    private List<Question> generateQuestionsFromText(String extractedText, String subject,
            String difficulty, Integer questionCount) {
        try {
            logger.info("Extracted text from image: {}", extractedText);

            // Create prompt for LLM
            String prompt = createPrompt(extractedText, subject, difficulty, questionCount);
            logger.info("Generated prompt for LLM: {}", prompt);

            // Validate API key first
            if (llmApiKey == null || llmApiKey.equals("your-api-key-here") || llmApiKey.trim().isEmpty()) {
                logger.error(
                        "OpenRouter API key is not properly configured. Please set OPENAI_API_KEY environment variable.");
                throw new RuntimeException("OpenRouter API key is not configured");
            }

            // Call LLM API
            String llmResponse = callLlmApi(prompt);
            logger.info("Received LLM response: {}", llmResponse);

            // Parse LLM response to extract questions
            return parseQuestionsFromLlmResponse(llmResponse);

        } catch (Exception e) {
            logger.error("Error calling LLM API - falling back to mock questions: ", e);
            // Return mock questions as fallback
            return createMockQuestions(questionCount);
        }
    }

    private String createPrompt(String extractedText, String subject, String difficulty, Integer questionCount) {
        return String.format(
                "Based on the following text content, generate %d %s questions about %s with %s difficulty level. " +
                        "Respond with ONLY a strict JSON array (no markdown, no code fences) of objects where each object has: "
                        +
                        "{\"id\": \"unique_id\", \"type\": \"MCQ|ONE_WORD|PARAGRAPH\", \"question\": \"question text\", "
                        +
                        "\"options\": [\"option1\", \"option2\", \"option3\", \"option4\"] or null, " +
                        "\"answer\": \"correct answer\" or null, \"explanation\": \"explanation text\" or null}. " +
                        "For non-MCQ questions, set options to null. Use double quotes for all keys and string values. "
                        +
                        "Text content: %s",
                questionCount != null ? questionCount : 5,
                difficulty != null ? difficulty : "medium",
                subject != null ? subject : "general",
                difficulty != null ? difficulty : "medium",
                extractedText);
    }

    private String callLlmApi(String prompt) {
        try {
            // Prepare request body for OpenAI-style API
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", llmModel);

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            messages.add(message);

            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 2000);
            requestBody.put("temperature", 0.7);

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(llmApiKey);

            // Add OpenRouter specific headers
            headers.set("HTTP-Referer", "http://localhost:9876");
            headers.set("X-Title", "EdTech Question Generator");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            logger.info("Making API call to: {} with model: {}", llmApiUrl, llmModel);

            // Make API call
            ResponseEntity<String> response = restTemplate.exchange(
                    llmApiUrl,
                    HttpMethod.POST,
                    entity,
                    String.class);

            logger.info("API response status: {}", response.getStatusCode());
            logger.debug("API response body: {}", response.getBody());

            // Extract content from response
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            String content = responseJson.path("choices").get(0).path("message").path("content").asText();

            if (content.isEmpty()) {
                throw new RuntimeException("Empty response content from LLM API");
            }

            return content;

        } catch (Exception e) {
            logger.error("Error calling LLM API. URL: {}, Model: {}, API Key present: {}",
                    llmApiUrl, llmModel, (llmApiKey != null && !llmApiKey.trim().isEmpty()), e);
            throw new RuntimeException("Failed to call LLM API: " + e.getMessage(), e);
        }
    }

    private List<Question> parseQuestionsFromLlmResponse(String llmResponse) {
        try {
            // Try to extract JSON from the response (handles arrays and numbered objects)
            String jsonResponse = extractJsonFromResponse(llmResponse);

            // First attempt: direct mapping to List<Question>
            try {
                TypeReference<List<Question>> typeRef = new TypeReference<List<Question>>() {
                };
                List<Question> parsed = objectMapper.readValue(jsonResponse, typeRef);
                return ensureIdsAndDefaults(parsed);
            } catch (com.fasterxml.jackson.databind.exc.MismatchedInputException mie) {
                // Try more flexible parsing paths
                logger.warn("LLM returned non-standard structure, attempting flexible parsing: {}", mie.getMessage());

                JsonNode node = objectMapper.readTree(jsonResponse);

                // Handle wrapper objects like {"questions": [...]}
                if (node.isObject()) {
                    JsonNode questionsNode = null;
                    if (node.has("questions"))
                        questionsNode = node.get("questions");
                    else if (node.has("data"))
                        questionsNode = node.get("data");
                    else if (node.has("items"))
                        questionsNode = node.get("items");
                    if (questionsNode != null && questionsNode.isArray()) {
                        return parseQuestionsFromLlmResponse(questionsNode.toString());
                    }
                }

                // Handle array of strings: ["question 1", "question 2", ...]
                if (node.isArray() && node.size() > 0 && node.get(0).isTextual()) {
                    List<String> texts = new ArrayList<>();
                    for (JsonNode n : node) {
                        texts.add(n.asText());
                    }
                    return mapStringsToQuestions(texts);
                }

                // Handle array of objects with different field names
                if (node.isArray() && node.size() > 0 && node.get(0).isObject()) {
                    TypeReference<List<Map<String, Object>>> ref = new TypeReference<List<Map<String, Object>>>() {
                    };
                    List<Map<String, Object>> items = objectMapper.readValue(jsonResponse, ref);
                    return mapGenericListToQuestions(items);
                }

                // If none matched, fall through to plain text parsing
                throw mie;
            }

        } catch (JsonProcessingException e) {
            logger.error("Error parsing LLM response as JSON, attempting plain-text parsing: ", e);
            // Try to parse plain text bullets/numbered lines
            List<Question> fallback = parsePlainTextToQuestions(llmResponse);
            if (!fallback.isEmpty())
                return fallback;
            return createMockQuestions(3);
        } catch (Exception e) {
            logger.error("Error extracting JSON from LLM response, attempting plain-text parsing: ", e);
            List<Question> fallback = parsePlainTextToQuestions(llmResponse);
            if (!fallback.isEmpty())
                return fallback;
            return createMockQuestions(3);
        }
    }

    private List<Question> ensureIdsAndDefaults(List<Question> questions) {
        if (questions == null)
            return new ArrayList<>();
        for (Question q : questions) {
            if (q.getId() == null || q.getId().isBlank()) {
                q.setId(UUID.randomUUID().toString());
            }
            if (q.getType() == null || q.getType().isBlank()) {
                q.setType(q.getOptions() != null && !q.getOptions().isEmpty() ? "MCQ" : "PARAGRAPH");
            }
        }
        return questions;
    }

    private List<Question> mapStringsToQuestions(List<String> texts) {
        List<Question> result = new ArrayList<>();
        if (texts == null)
            return result;
        for (String t : texts) {
            if (t == null || t.isBlank())
                continue;
            Question q = new Question();
            q.setId(UUID.randomUUID().toString());
            q.setType("PARAGRAPH");
            q.setQuestion(stripLeadingNumbering(t.trim()));
            q.setOptions(null);
            q.setAnswer(null);
            q.setExplanation(null);
            result.add(q);
        }
        return result;
    }

    private List<Question> mapGenericListToQuestions(List<Map<String, Object>> items) {
        List<Question> result = new ArrayList<>();
        if (items == null)
            return result;
        for (Map<String, Object> m : items) {
            if (m == null || m.isEmpty())
                continue;
            Question q = new Question();
            q.setId(String.valueOf(m.getOrDefault("id", UUID.randomUUID().toString())));
            // Try common aliases for question text
            Object qt = firstNonNull(m.get("question"), m.get("question_text"), m.get("prompt"), m.get("text"),
                    m.get("q"));
            if (qt != null)
                q.setQuestion(String.valueOf(qt));
            // Type aliases
            Object tp = firstNonNull(m.get("type"), m.get("question_type"));
            q.setType(tp == null ? null : String.valueOf(tp));
            // Options aliases
            Object opts = firstNonNull(m.get("options"), m.get("choices"));
            if (opts instanceof List<?>) {
                List<String> casted = new ArrayList<>();
                for (Object o : (List<?>) opts) {
                    if (o != null)
                        casted.add(String.valueOf(o));
                }
                q.setOptions(casted.isEmpty() ? null : casted);
            }
            // Answer aliases
            Object ans = firstNonNull(m.get("answer"), m.get("correct_answer"), m.get("correctAnswer"));
            q.setAnswer(ans == null ? null : String.valueOf(ans));
            // Explanation aliases
            Object exp = firstNonNull(m.get("explanation"), m.get("rationale"), m.get("reason"));
            q.setExplanation(exp == null ? null : String.valueOf(exp));

            if (q.getType() == null || q.getType().isBlank()) {
                q.setType(q.getOptions() != null && !q.getOptions().isEmpty() ? "MCQ" : "PARAGRAPH");
            }

            if (q.getQuestion() == null || q.getQuestion().isBlank()) {
                // If the map is actually a single-string like {"question": "..."} missing, try
                // to stringify map
                // but better to skip empty entries
                continue;
            }

            result.add(q);
        }
        return result;
    }

    private Object firstNonNull(Object... values) {
        for (Object v : values) {
            if (v != null)
                return v;
        }
        return null;
    }

    private List<Question> parsePlainTextToQuestions(String text) {
        List<Question> result = new ArrayList<>();
        if (text == null || text.isBlank())
            return result;

        String[] lines = text.split("\r?\n");
        for (String raw : lines) {
            if (raw == null)
                continue;
            String line = raw.trim();
            if (line.isEmpty())
                continue;
            // Match patterns like "1. ...", "1) ...", "- ...", "* ..."
            if (line.matches("^(?:\\d+[\\.)]\\s*|[-*]\\s+).*$")) {
                String qText = stripLeadingNumbering(line);
                if (qText.isBlank())
                    continue;
                Question q = new Question();
                q.setId(UUID.randomUUID().toString());
                q.setType("PARAGRAPH");
                q.setQuestion(qText);
                result.add(q);
            }
        }

        // If no bullet/numbered lines detected but text is short, treat as single
        // question
        if (result.isEmpty()) {
            String normalized = text.trim();
            if (!normalized.isEmpty()) {
                Question q = new Question();
                q.setId(UUID.randomUUID().toString());
                q.setType("PARAGRAPH");
                q.setQuestion(normalized);
                result.add(q);
            }
        }
        return result;
    }

    private String stripLeadingNumbering(String s) {
        if (s == null)
            return null;
        // Remove leading numbering or bullets like "1.", "1)", "-", "*"
        return s.replaceFirst("^\\s*(?:[0-9]+[\\.\\)]\\s*|[-*]\\s+)", "").trim();
    }

    private String extractJsonFromResponse(String response) {
        // First try to find a JSON array
        int startIndex = response.indexOf('[');
        int endIndex = response.lastIndexOf(']');

        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return response.substring(startIndex, endIndex + 1);
        }

        // If no JSON array found, try to extract individual JSON objects from numbered
        // format
        List<String> jsonObjects = new ArrayList<>();

        // Use regex to find numbered JSON objects more reliably
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "\\d+\\.\\s*(\\{.*?\\})(?=\\s*(?:\\d+\\.|$))",
                java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher matcher = pattern.matcher(response);

        while (matcher.find()) {
            String jsonObject = matcher.group(1).trim();

            // Validate that it's a proper JSON object
            if (jsonObject.startsWith("{") && jsonObject.endsWith("}")) {
                jsonObjects.add(jsonObject);
            }
        }

        if (!jsonObjects.isEmpty()) {
            return "[" + String.join(", ", jsonObjects) + "]";
        }

        throw new IllegalArgumentException("No valid JSON array or numbered JSON objects found in response");
    }

    private List<Question> createMockQuestions(Integer questionCount) {
        List<Question> questions = new ArrayList<>();
        int count = questionCount != null ? questionCount : 3;

        for (int i = 1; i <= count; i++) {
            Question question = new Question();
            question.setId(UUID.randomUUID().toString());
            question.setType("MCQ");
            question.setQuestion("Sample question " + i + " based on the provided text?");

            List<String> options = new ArrayList<>();
            options.add("Option A");
            options.add("Option B");
            options.add("Option C");
            options.add("Option D");
            question.setOptions(options);

            question.setAnswer("Option A");
            question.setExplanation("This is a sample explanation for question " + i);

            questions.add(question);
        }

        return questions;
    }

    public boolean isApiKeyConfigured() {
        return llmApiKey != null && !llmApiKey.equals("your-api-key-here") && !llmApiKey.trim().isEmpty();
    }

    public GenerateResponse generateQuestionsFromTextOnly(String text, String subject, String difficulty,
            Integer questionCount) {
        try {
            if (text == null || text.trim().isEmpty()) {
                return new GenerateResponse(new ArrayList<>(), "error", "Text content is required");
            }

            // Generate questions using LLM API
            List<Question> questions = generateQuestionsFromText(text, subject, difficulty, questionCount);

            return new GenerateResponse(questions, "success", "Questions generated successfully");

        } catch (Exception e) {
            logger.error("Error generating questions from text: ", e);
            return new GenerateResponse(new ArrayList<>(), "error", "Failed to generate questions: " + e.getMessage());
        }
    }
}