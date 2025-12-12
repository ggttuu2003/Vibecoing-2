package com.example.vibecoing2.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.ai")
public class AIConfig {
    private String provider;
    private String apiKey;
    private String baseUrl;
    private String model;
    private Long timeout;
    private Integer maxTokens;
    private Double temperature;
    private Double topP;
    private Integer maxRetries;
    private Long retryDelay;
    private Integer maxImageSize;
}
