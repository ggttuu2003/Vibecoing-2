package com.example.vibecoing2.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Google GenAI 图像生成 API 响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)  // 忽略未知字段（如 sdkHttpResponse、usageMetadata 等）
public class AIImageGenResponse {
    /**
     * 候选结果列表
     */
    private List<Candidate> candidates;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Candidate {
        private Content content;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Content {
        private List<Part> parts;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Part {
        /**
         * 文本内容
         */
        private String text;

        /**
         * 内联数据（base64 图片）
         */
        @JsonProperty("inlineData")
        private InlineData inlineData;

        /**
         * 文件数据（图片 URL）
         */
        @JsonProperty("fileData")
        private java.util.List<FileData> fileData;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InlineData {
        /**
         * MIME 类型
         */
        @JsonProperty("mimeType")
        private String mimeType;

        /**
         * Base64 编码的图片数据
         */
        private String data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FileData {
        /**
         * 图片 URL
         */
        @JsonProperty("fileUri")
        private String fileUri;

        /**
         * MIME 类型
         */
        @JsonProperty("mimeType")
        private String mimeType;
    }

    /**
     * 提取第一张图片的数据（base64 或 URL）
     *
     * @return 图片数据（可直接用于 img src）：
     *         - base64: data:image/png;base64,xxx
     *         - URL: https://...
     *         如果没有图片返回 null
     */
    public String extractImageData() {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        Candidate candidate = candidates.get(0);
        if (candidate.getContent() == null || candidate.getContent().getParts() == null) {
            return null;
        }

        for (Part part : candidate.getContent().getParts()) {
            // 优先查找 inlineData（base64）
            if (part.getInlineData() != null && part.getInlineData().getData() != null) {
                String base64 = part.getInlineData().getData();
                String mimeType = part.getInlineData().getMimeType() != null
                    ? part.getInlineData().getMimeType()
                    : "image/png";
                // 返回完整的 data URL
                return "data:" + mimeType + ";base64," + base64;
            }

            // 如果没有 inlineData，查找 fileData（URL）
            if (part.getFileData() != null && !part.getFileData().isEmpty()) {
                FileData fileData = part.getFileData().get(0);
                if (fileData.getFileUri() != null) {
                    // 直接返回 URL
                    return fileData.getFileUri();
                }
            }
        }

        return null;
    }

    /**
     * 提取文本描述
     *
     * @return 文本描述，如果没有返回空字符串
     */
    public String extractText() {
        if (candidates == null || candidates.isEmpty()) {
            return "";
        }

        Candidate candidate = candidates.get(0);
        if (candidate.getContent() == null || candidate.getContent().getParts() == null) {
            return "";
        }

        for (Part part : candidate.getContent().getParts()) {
            if (part.getText() != null) {
                return part.getText();
            }
        }

        return "";
    }
}
