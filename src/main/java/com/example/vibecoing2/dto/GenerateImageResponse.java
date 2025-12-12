package com.example.vibecoing2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 图片生成响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateImageResponse {
    /**
     * 生成的图片列表
     */
    private List<GeneratedImage> images;

    /**
     * 元数据
     */
    private Metadata metadata;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneratedImage {
        /**
         * base64编码的图片数据
         */
        private String base64;

        /**
         * 图片宽度
         */
        private Integer width;

        /**
         * 图片高度
         */
        private Integer height;

        /**
         * 使用的模板
         */
        private String template;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metadata {
        /**
         * 生成耗时（毫秒）
         */
        private Long generationTimeMs;

        /**
         * 图片数量
         */
        private Integer count;
    }
}
