package com.cvservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "ai.google")
@Getter
@Setter
public class GoogleAIConfig {
    private String apiKey;
    private String model;
    private Duration timeout;
    private String url;
}
