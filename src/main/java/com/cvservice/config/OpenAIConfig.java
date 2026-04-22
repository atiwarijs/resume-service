package com.cvservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "ai.openai")
@Getter
@Setter
public class OpenAIConfig {
    private String url;
    private String model;
    private String apiKey;
    private Duration timeout;
}
