package com.example.vibecoing2.dto;

import lombok.Data;

import java.util.List;

/**
 * 图片生成请求DTO
 */
@Data
public class GenerateImageRequest {
    /**
     * 主标题
     */
    private String title;

    /**
     * 副标题
     */
    private String subtitle;

    /**
     * 关键词列表
     */
    private List<String> keywords;

    /**
     * 风格模板: xiaohongshu(小红书封面) 或 advertising_a(投放素材风格A)
     */
    private String style;

    /**
     * 生成图片数量，默认1张，范围1-3
     */
    private Integer count = 1;

    /**
     * AI 模型选择（支持 4 个模型）
     * - google/gemini-3-pro-image-preview
     * - google/gemini-3-pro-image-preview-free
     * - google/gemini-2.5-flash-image
     * - google/gemini-2.5-flash-image-free（默认）
     * 如果不指定，使用配置文件中的默认模型
     */
    private String model;

    /**
     * 背景图片的base64编码（可选）
     * 用于提供底片，AI将在此基础上生成图片
     */
    private String backgroundImage;
}
