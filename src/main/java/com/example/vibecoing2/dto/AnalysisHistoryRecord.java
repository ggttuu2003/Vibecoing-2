package com.example.vibecoing2.dto;

import com.example.vibecoing2.domain.PageTemplate;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 设计稿解析历史记录 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisHistoryRecord {
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
     * 原始上传图片的相对路径
     * 例如: "analysis/uuid/original.png"
     */
    private String originalImagePath;

    /**
     * 解析后的模板结构
     */
    private PageTemplate template;

    /**
     * 分析元数据
     */
    private TemplateResponse.AnalysisMetadata metadata;

    /**
     * 组件总数
     */
    private Integer componentCount;

    /**
     * 使用的分析引擎（AI、OCR、CV 的组合）
     */
    private String analysisEngine;
}
