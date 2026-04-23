package com.cvservice.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VertexAIConfig {

    @Bean
    public ChatClient chatClient(VertexAiGeminiChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultOptions(chatModel.getDefaultOptions())
                .build();
    }
}
