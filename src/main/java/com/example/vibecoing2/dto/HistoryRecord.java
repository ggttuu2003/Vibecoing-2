package com.example.vibecoing2.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
}
