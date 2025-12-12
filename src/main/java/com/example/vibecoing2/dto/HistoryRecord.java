package com.example.vibecoing2.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 历史记录 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HistoryRecord {
    /**
     * 历史记录唯一标识（UUID）
     */
    private String historyId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * 原始请求信息
     */
    private GenerateImageRequest request;

    /**
     * 生成元数据
     */
    private GenerateImageResponse.Metadata metadata;

    /**
     * 图片相对路径列表
     * 例如: ["images/uuid/image_1.png", "images/uuid/image_2.png"]
     */
    private List<String> imagePaths;

    /**
     * 使用的风格模板
     */
    private String style;

    /**
     * 使用的 AI 模型
     */
    private String model;

    /**
     * 生成的图片数量
     */
    private Integer imageCount;

    /**
     * 图片数据列表（用于前端展示，包含 base64 数据）
     */
    private List<ImageData> images;

    /**
     * 便捷属性：标题（从 request 中提取）
     */
    public String getTitle() {
        return request != null ? request.getTitle() : null;
    }

    /**
     * 便捷属性：副标题（从 request 中提取）
     */
    public String getSubtitle() {
        return request != null ? request.getSubtitle() : null;
    }

    /**
     * 便捷属性：图片数量（兼容）
     */
    public Integer getCount() {
        return imageCount;
    }

    /**
     * 图片数据
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageData {
        private String base64;
        private Integer width;
        private Integer height;
        private String template;
    }
}
