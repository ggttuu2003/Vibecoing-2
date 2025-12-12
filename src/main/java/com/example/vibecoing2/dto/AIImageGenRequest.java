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

        @JsonProperty("inlineData")
        private InlineData inlineData;

        // 便捷构造函数：仅文本
        public Part(String text) {
            this.text = text;
        }

        // 便捷构造函数：仅图片
        public Part(InlineData inlineData) {
            this.inlineData = inlineData;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InlineData {
        @JsonProperty("mimeType")
        private String mimeType;

        private String data;
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

    /**
     * 创建带模板图片的图像生成请求
     *
     * @param prompt 提示词
     * @param templateImageBase64 模板图片的 base64 数据（纯 base64，不含前缀）
     * @param mimeType 图片 MIME 类型（如 image/png、image/jpeg）
     * @return 请求对象
     */
    public static AIImageGenRequest createImageRequestWithTemplate(String prompt,
                                                                     String templateImageBase64,
                                                                     String mimeType) {
        InlineData imageData = new InlineData(mimeType, templateImageBase64);
        Part imagePart = new Part(imageData);
        Part textPart = new Part(prompt);

        // 先传图片，后传文本
        Content content = new Content("user", List.of(imagePart, textPart));
        GenerationConfig config = new GenerationConfig(List.of("TEXT", "IMAGE"));
        return new AIImageGenRequest(List.of(content), config);
    }
}
