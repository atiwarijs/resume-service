package com.cvservice.aiclient;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AIClient {
    private final ChatClient chatClient;

    public String analyzeResume(String text) {
        try {
            return chatClient.prompt()
                    .user("Extract top skills and missing skills in JSON format from this resume text: " + text)
                    .call()
                    .content();
        } catch (Exception e) {
            return "AI service temporarily unavailable";
        }
    }
}
