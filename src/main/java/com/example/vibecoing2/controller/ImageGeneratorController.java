package com.example.vibecoing2.controller;

import com.example.vibecoing2.config.AIImageGenConfig;
import com.example.vibecoing2.dto.ApiResponse;
import com.example.vibecoing2.dto.GenerateImageRequest;
import com.example.vibecoing2.dto.GenerateImageResponse;
import com.example.vibecoing2.service.ImageGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 图片生成控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/generate")
@RequiredArgsConstructor
public class ImageGeneratorController {

    private final ImageGeneratorService imageGeneratorService;
    private final AIImageGenConfig aiImageGenConfig;

    /**
     * 生成图片（接收 JSON 格式数据）
     */
    @PostMapping("/image")
    public ApiResponse<GenerateImageResponse> generateImage(
            @RequestBody GenerateImageRequest request
    ) {
        try {
            log.info("收到图片生成请求: title={}, style={}, count={}, model={}, hasBackgroundImage={}",
                    request.getTitle(), request.getStyle(), request.getCount(),
                    request.getModel(), request.getBackgroundImage() != null);

            // 参数验证
            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                return ApiResponse.error(400, "主标题不能为空");
            }
            if (request.getKeywords() == null || request.getKeywords().isEmpty()) {
                return ApiResponse.error(400, "关键词不能为空");
            }
            if (request.getKeywords().size() > 5) {
                return ApiResponse.error(400, "关键词数量不能超过5个");
            }
            if (request.getStyle() == null || request.getStyle().trim().isEmpty()) {
                return ApiResponse.error(400, "风格模板不能为空");
            }
            if (request.getModel() == null || request.getModel().trim().isEmpty()) {
                return ApiResponse.error(400, "AI模型不能为空");
            }

            // 生成图片
            GenerateImageResponse response = imageGeneratorService.generateImages(request);

            return ApiResponse.success(response);
        } catch (IllegalArgumentException e) {
            log.warn("请求参数错误: {}", e.getMessage());
            return ApiResponse.error(400, "参数错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("图片生成失败", e);
            return ApiResponse.error(500, "图片生成失败: " + e.getMessage());
        }
    }

    /**
     * 获取支持的模板列表（风格）
     */
    @GetMapping("/templates")
    public ApiResponse<java.util.List<TemplateInfo>> getTemplates() {
        java.util.List<TemplateInfo> templates = java.util.Arrays.asList(
                new TemplateInfo("xiaohongshu", "小红书封面", 1080, 1350, "适合小红书平台的封面图"),
                new TemplateInfo("advertising_a", "投放素材风格A", 1080, 1080, "适合广告投放的方形素材")
        );
        return ApiResponse.success(templates);
    }

    /**
     * 获取支持的 AI 模型列表（仅返回 ZenMux 实际可用的模型）
     */
    @GetMapping("/models")
    public ApiResponse<java.util.List<ModelInfo>> getModels() {
        java.util.List<ModelInfo> models = java.util.Arrays.asList(
                new ModelInfo("google/gemini-2.5-flash-image", "Gemini 2.5 Flash Image (推荐)", "快速图像生成"),
                new ModelInfo("google/gemini-3-pro-image-preview", "Gemini 3 Pro Image", "高质量图像生成")
        );
        return ApiResponse.success(models);
    }

    /**
     * 模板信息
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class TemplateInfo {
        private String id;
        private String name;
        private int width;
        private int height;
        private String description;
    }

    /**
     * 模型信息
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ModelInfo {
        private String id;
        private String name;
        private String description;
    }
}
