package com.example.vibecoing2.service;

import com.example.vibecoing2.dto.GenerateImageRequest;
import com.example.vibecoing2.dto.GenerateImageResponse;
import com.example.vibecoing2.util.PromptBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片生成服务（使用 AI）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageGeneratorService {

    private final AIImageGenerationService aiImageGenerationService;
    private final HistoryService historyService;

    /**
     * 生成图片
     */
    public GenerateImageResponse generateImages(GenerateImageRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            // 1. 验证请求参数
            validateRequest(request);

            // 2. 构建 AI Prompt
            String prompt = PromptBuilder.buildPrompt(
                    request.getStyle(),
                    request.getTitle(),
                    request.getSubtitle(),
                    request.getKeywords()
            );

            // 如果有背景图，在 prompt 中说明
            if (request.getBackgroundImage() != null && !request.getBackgroundImage().isEmpty()) {
                prompt += "\n\n重要提示：用户提供了背景图作为底片，请在此基础上进行创作，保留背景的主要元素和风格。";
                log.info("检测到背景图片，已添加到 prompt 中");
            }

            log.info("生成的 Prompt: {}", prompt);

            // 3. 确定生成数量
            int count = Math.min(Math.max(request.getCount() != null ? request.getCount() : 1, 1), 3);

            // 4. 使用 AI 生成图片
            List<GenerateImageResponse.GeneratedImage> images = new ArrayList<>();
            for (int i = 1; i <= count; i++) {
                try {
                    // 调用 AI 服务生成图片（返回不含前缀的 base64 数据）
                    String base64Data = aiImageGenerationService.generateImage(prompt, request.getModel());

                    // 转换为完整的 data URL（用于保存历史记录）
                    String dataUrl = aiImageGenerationService.toDataUrl(base64Data);

                    // 根据风格确定尺寸
                    int width = getWidthForStyle(request.getStyle());
                    int height = getHeightForStyle(request.getStyle());

                    images.add(new GenerateImageResponse.GeneratedImage(
                            dataUrl,
                            width,
                            height,
                            request.getStyle()
                    ));

                    log.debug("生成第{}张图片成功: {}x{}", i, width, height);
                } catch (Exception e) {
                    log.error("生成第{}张图片失败", i, e);
                    throw new RuntimeException("生成第" + i + "张图片失败: " + e.getMessage(), e);
                }
            }

            // 5. 构建响应
            long generationTime = System.currentTimeMillis() - startTime;
            GenerateImageResponse.Metadata metadata = new GenerateImageResponse.Metadata(
                    generationTime,
                    images.size()
            );

            log.info("图片生成完成: 风格={}, 模型={}, 数量={}, 耗时={}ms",
                    request.getStyle(), request.getModel(), images.size(), generationTime);

            GenerateImageResponse response = new GenerateImageResponse(images, metadata);

            // 保存历史记录
            historyService.saveHistory(request, response);

            return response;

        } catch (Exception e) {
            log.error("图片生成失败", e);
            throw new RuntimeException("图片生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证请求参数
     */
    private void validateRequest(GenerateImageRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("标题不能为空");
        }

        if (request.getKeywords() == null || request.getKeywords().isEmpty()) {
            throw new IllegalArgumentException("关键词不能为空");
        }

        if (request.getKeywords().size() > 5) {
            throw new IllegalArgumentException("关键词数量不能超过5个");
        }

        if (request.getStyle() == null || request.getStyle().trim().isEmpty()) {
            throw new IllegalArgumentException("风格模板不能为空");
        }
    }

    /**
     * 根据风格获取宽度
     */
    private int getWidthForStyle(String style) {
        return 1080; // 两种风格都是 1080
    }

    /**
     * 根据风格获取高度
     */
    private int getHeightForStyle(String style) {
        switch (style.toLowerCase()) {
            case "xiaohongshu":
                return 1350;
            case "advertising_a":
                return 1080;
            default:
                return 1080;
        }
    }
}
