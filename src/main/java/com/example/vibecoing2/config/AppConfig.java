package com.example.vibecoing2.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    private UploadConfig upload = new UploadConfig();
    private ProcessedConfig processed = new ProcessedConfig();
    private ImageConfig image = new ImageConfig();
    private LayoutConfig layout = new LayoutConfig();

    /**
     * 配置 ObjectMapper Bean（用于 JSON 序列化/反序列化）
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Data
    public static class UploadConfig {
        private String dir;
    }

    @Data
    public static class ProcessedConfig {
        private String dir;
    }

    @Data
    public static class ImageConfig {
        private Integer standardWidth;
        private Double quality;
    }

    @Data
    public static class LayoutConfig {
        private Double iouThreshold;
        private Double confidenceThreshold;
    }
}
