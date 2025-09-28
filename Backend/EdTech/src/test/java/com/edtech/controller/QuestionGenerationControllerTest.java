package com.edtech.controller;

import com.edtech.service.QuestionGenerationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuestionGenerationController.class)
class QuestionGenerationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuestionGenerationService questionGenerationService;

    @Test
    @WithMockUser
    void healthEndpoint_ShouldReturnSuccess() throws Exception {
        mockMvc.perform(get("/api/generate/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Question Generation Service is running"));
    }
}