package com.cvservice.aiclient;

import com.cvservice.config.GoogleAIConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AIClient {
    private final WebClient webClient;
    private final GoogleAIConfig config;

    public String analyzeResume(String text) {

        String requestBody = buildRequest(text);

        return webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::extractTextFromGeminiResponse) // Extract text from Gemini response
                .timeout(config.getTimeout()) // ⏱ timeout handling
                .retry(2) // 🔁 retry if fails
                .onErrorResume(ex -> {
                    // ⚠️ fallback
                    return Mono.just("AI service temporarily unavailable");
                })
                .block(); // blocking for simplicity (can make async later)
    }

    private String buildRequest(String text) {
        return """
        {
          "contents": [
            {
              "parts": [
                {
                  "text": "Extract top skills and missing skills in JSON format from this resume text: %s"
                }
              ]
            }
          ]
        }
        """.formatted(text);
    }

    private String extractTextFromGeminiResponse(String response) {
        // Gemini API response format: {"candidates": [{"content": {"parts": [{"text": "..."}]}}]}
        try {
            // Simple extraction - in production, use proper JSON parsing
            if (response.contains("\"text\":")) {
                int start = response.indexOf("\"text\":\"") + 8;
                int end = response.indexOf("\"", start);
                // Handle escaped quotes
                while (end > start && response.charAt(end - 1) == '\\') {
                    end = response.indexOf("\"", end + 1);
                }
                return response.substring(start, end).replace("\\\"", "\"").replace("\\n", "\n");
            }
            return response;
        } catch (Exception e) {
            return response; // fallback to raw response
        }
    }
}
