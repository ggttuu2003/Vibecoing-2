package com.example.vibecoing2.controller;

import com.example.vibecoing2.config.AIImageGenConfig;
import com.example.vibecoing2.dto.ApiResponse;
import com.example.vibecoing2.dto.GenerateImageRequest;
import com.example.vibecoing2.dto.GenerateImageResponse;
import com.example.vibecoing2.service.ImageGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;

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
     * 生成图片（支持背景图上传）
     */
    @PostMapping("/image")
    public ApiResponse<GenerateImageResponse> generateImage(
            @RequestParam("title") String title,
            @RequestParam(value = "subtitle", required = false) String subtitle,
            @RequestParam("keywords") String keywords,
            @RequestParam("style") String style,
            @RequestParam(value = "count", defaultValue = "1") Integer count,
            @RequestParam("model") String model,
            @RequestParam(value = "backgroundImage", required = false) MultipartFile backgroundImage
    ) {
        try {
            log.info("收到图片生成请求: title={}, style={}, count={}, hasBackgroundImage={}",
                    title, style, count, backgroundImage != null);

            // 构建请求对象
            GenerateImageRequest request = new GenerateImageRequest();
            request.setTitle(title);
            request.setSubtitle(subtitle);
            request.setStyle(style);
            request.setCount(count);
            request.setModel(model);

            // 处理关键词（逗号分隔）
            if (keywords != null && !keywords.trim().isEmpty()) {
                List<String> keywordList = Arrays.asList(keywords.split(","));
                request.setKeywords(keywordList.stream()
                        .map(String::trim)
                        .filter(k -> !k.isEmpty())
                        .toList());
            }

            // 处理背景图片
            if (backgroundImage != null && !backgroundImage.isEmpty()) {
                try {
                    byte[] imageBytes = backgroundImage.getBytes();
                    String base64Image = "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
                    request.setBackgroundImage(base64Image);
                    log.info("背景图片已转换为base64，大小: {} KB", imageBytes.length / 1024);
                } catch (Exception e) {
                    log.error("背景图片处理失败", e);
                    return ApiResponse.error(400, "背景图片处理失败: " + e.getMessage());
                }
            }

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
