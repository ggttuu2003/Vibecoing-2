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

            // 2. 判断是否有模板图
            boolean hasTemplate = request.getBackgroundImage() != null
                    && !request.getBackgroundImage().trim().isEmpty();

            // 3. 构建 AI Prompt（根据是否有模板选择不同的 prompt 策略）
            String prompt = PromptBuilder.buildPrompt(
                    request.getStyle(),
                    request.getTitle(),
                    request.getSubtitle(),
                    request.getKeywords(),
                    hasTemplate
            );

            log.info("生成的 Prompt: {}", prompt);
            if (hasTemplate) {
                log.info("检测到模板图片，使用模板叠加模式");
            }

            // 4. 确定生成数量
            int count = Math.min(Math.max(request.getCount() != null ? request.getCount() : 1, 1), 3);

            // 5. 使用 AI 生成图片
            List<GenerateImageResponse.GeneratedImage> images = new ArrayList<>();
            for (int i = 1; i <= count; i++) {
                try {
                    // 调用 AI 服务生成图片
                    // 如果有模板图，传入模板图；否则传入 null
                    String base64Data = aiImageGenerationService.generateImage(
                            prompt,
                            request.getModel(),
                            hasTemplate ? request.getBackgroundImage() : null
                    );

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

            // 6. 构建响应
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
