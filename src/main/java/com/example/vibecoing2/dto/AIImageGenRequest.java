package com.example.vibecoing2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Google GenAI 图像生成 API 请求 DTO
 * 对应 Vertex AI 的 generateContent 接口
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIImageGenRequest {
    /**
     * 内容列表
     */
    private List<Content> contents;

    /**
     * 生成配置
     */
    @JsonProperty("generationConfig")
    private GenerationConfig generationConfig;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
        /**
         * 角色：user 或 model
         */
        private String role;

        /**
         * 内容部分列表
         */
        private List<Part> parts;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Part {
        private String text;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerationConfig {
        @JsonProperty("responseModalities")
        private List<String> responseModalities;
    }

    /**
     * 创建图像生成请求
     *
     * @param prompt 提示词
     * @return 请求对象
     */
    public static AIImageGenRequest createImageRequest(String prompt) {
        Part part = new Part(prompt);
        Content content = new Content("user", List.of(part));
        GenerationConfig config = new GenerationConfig(List.of("TEXT", "IMAGE"));
        return new AIImageGenRequest(List.of(content), config);
    }
}
