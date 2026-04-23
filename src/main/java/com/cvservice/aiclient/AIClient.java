package com.cvservice.aiclient;

import com.cvservice.constants.AIPrompts;
import com.cvservice.exception.AIServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AIClient {
    private final ChatClient chatClient;

    public String analyzeResume(String resumeText, String jobDescription) {
        try {
            String prompt = AIPrompts.RESUME_ANALYSIS_PROMPT.formatted(resumeText, jobDescription);

            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("AI analysis failed: {}", e.getMessage(), e);
            throw new AIServiceException("AI service temporarily unavailable: " + e.getMessage(), e);
        }
    }
}
