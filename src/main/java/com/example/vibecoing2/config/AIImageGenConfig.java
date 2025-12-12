package com.example.vibecoing2.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI 图像生成配置类（与 AIConfig 隔离）
 * 用于图像生成功能，读取 app.ai.image-gen.* 配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.ai.image-gen")
public class AIImageGenConfig {
    /**
     * API Base URL (Vertex AI endpoint)
     */
    private String baseUrl;

    /**
     * API Key
     */
    private String apiKey;

    /**
     * 默认模型
     */
    private String defaultModel;

    /**
     * 请求超时时间（毫秒）
     */
    private Long timeout;

    /**
     * 最大重试次数
     */
    private Integer maxRetries;

    /**
     * 支持的模型列表（仅包含 ZenMux 实际可用的模型）
     */
    private static final String[] SUPPORTED_MODELS = {
        "google/gemini-2.5-flash-image",
        "google/gemini-3-pro-image-preview"
    };

    /**
     * 检查模型是否支持
     */
    public boolean isSupportedModel(String model) {
        if (model == null) {
            return false;
        }
        for (String supportedModel : SUPPORTED_MODELS) {
            if (supportedModel.equals(model)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取支持的模型列表
     */
    public String[] getSupportedModels() {
        return SUPPORTED_MODELS;
    }
}
