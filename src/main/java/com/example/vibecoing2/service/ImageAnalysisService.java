package com.example.vibecoing2.service;

import com.example.vibecoing2.config.AppConfig;
import com.example.vibecoing2.domain.Component;
import com.example.vibecoing2.domain.PageTemplate;
import com.example.vibecoing2.domain.TextComponent;
import com.example.vibecoing2.domain.ImageComponent;
import com.example.vibecoing2.domain.ButtonComponent;
import com.example.vibecoing2.dto.TemplateResponse;
import com.example.vibecoing2.util.FileUtil;
import com.example.vibecoing2.util.ImageProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageAnalysisService {

    private final AppConfig appConfig;
    private final FileUtil fileUtil;
    private final ImageProcessor imageProcessor;
    private final VisionAIService visionAIService;
    private final OCRService ocrService;
    private final ComponentDetectionService componentDetectionService;
    private final TemplateGeneratorService templateGeneratorService;
    private final LayoutAnalysisService layoutAnalysisService;
    private final HistoryService historyService;

    public TemplateResponse analyzeImage(MultipartFile imageFile, Boolean enableAI, Boolean enableOCR, Boolean enableCV) {
        long startTime = System.currentTimeMillis();

        try {
            fileUtil.ensureDirectoryExists(appConfig.getUpload().getDir());
            fileUtil.ensureDirectoryExists(appConfig.getProcessed().getDir());

            String uploadedPath = fileUtil.saveUploadedFile(imageFile, appConfig.getUpload().getDir());
            log.info("图片已上传: {}", uploadedPath);

            // 读取原始图片并转换为 base64（用于保存历史记录）
            byte[] originalImageBytes = Files.readAllBytes(Paths.get(uploadedPath));
            String originalImageBase64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(originalImageBytes);

            Mat originalImage = imageProcessor.loadImage(uploadedPath);
            int originalWidth = originalImage.cols();
            int originalHeight = originalImage.rows();

            Mat processedImage = imageProcessor.preprocessImage(originalImage, appConfig.getImage().getStandardWidth());
            String processedPath = appConfig.getProcessed().getDir() + "/processed_" + System.currentTimeMillis() + ".png";
            imageProcessor.saveImage(processedImage, processedPath);

            List<Component> aiComponents = new ArrayList<>();
            if (enableAI) {
                try {
                    aiComponents = visionAIService.analyzeImage(processedPath);
                    log.info("AI 分析完成，识别到 {} 个组件", aiComponents.size());
                } catch (Exception e) {
                    log.error("AI 分析失败", e);
                }
            }

            List<TextComponent> ocrComponents = new ArrayList<>();
            if (enableOCR) {
                try {
                    ocrComponents = ocrService.extractText(processedPath);
                    log.info("OCR 识别完成，识别到 {} 个文字", ocrComponents.size());
                } catch (Exception e) {
                    log.error("OCR 识别失败", e);
                }
            }

            List<Component> cvComponents = new ArrayList<>();
            if (enableCV) {
                try {
                    cvComponents = componentDetectionService.detectComponents(processedPath);
                    log.info("OpenCV 检测完成，识别到 {} 个组件", cvComponents.size());
                } catch (Exception e) {
                    log.error("OpenCV 检测失败", e);
                }
            }

            int processedWidth = processedImage.cols();
            int processedHeight = processedImage.rows();

            PageTemplate template = templateGeneratorService.generateTemplate(
                    aiComponents, cvComponents, ocrComponents, processedWidth, processedHeight
            );

            layoutAnalysisService.analyzeLayout(template);

            // 读取处理后的图像并转换为base64作为背景图
            try {
                byte[] imageBytes = Files.readAllBytes(Paths.get(processedPath));
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                template.getPage().setBackgroundImage("data:image/png;base64," + base64Image);
                log.info("背景图片已转换为 base64，大小: {} KB", imageBytes.length / 1024);
            } catch (IOException e) {
                log.warn("背景图片转换为 base64 失败", e);
            }

            // 提取所有组件的图片数据
            extractComponentImages(processedImage, template);

            long processingTime = System.currentTimeMillis() - startTime;

            TemplateResponse.AnalysisMetadata metadata = createMetadata(
                    processingTime, template, enableAI, enableOCR, enableCV
            );

            fileUtil.deleteFile(uploadedPath);
            fileUtil.deleteFile(processedPath);

            // 构建响应
            TemplateResponse response = TemplateResponse.success(template, metadata);

            // 保存历史记录
            historyService.saveAnalysisHistory(originalImageBase64, response);

            return response;

        } catch (Exception e) {
            log.error("图片分析失败", e);
            return TemplateResponse.error("图片分析失败: " + e.getMessage());
        }
    }

    private TemplateResponse.AnalysisMetadata createMetadata(
            long processingTime, PageTemplate template, Boolean aiUsed, Boolean ocrUsed, Boolean cvUsed) {

        int textCount = 0;
        int buttonCount = 0;
        int imageCount = 0;

        for (Component component : template.getComponents()) {
            switch (component.getType()) {
                case "text":
                    textCount++;
                    break;
                case "button":
                    buttonCount++;
                    break;
                case "image":
                    imageCount++;
                    break;
            }
        }

        return new TemplateResponse.AnalysisMetadata(
                processingTime,
                textCount,
                buttonCount,
                imageCount,
                "claude-3-5-sonnet-20241022",
                aiUsed,
                ocrUsed,
                cvUsed
        );
    }

    /**
     * 提取所有组件的图片数据
     */
    private void extractComponentImages(Mat processedImage, PageTemplate template) {
        log.info("开始提取组件图片数据...");
        int imageExtracted = 0;
        int buttonImageExtracted = 0;

        for (Component component : template.getComponents()) {
            try {
                if (component instanceof ImageComponent) {
                    ImageComponent imageComp = (ImageComponent) component;
                    extractImageComponentData(processedImage, imageComp);
                    imageExtracted++;
                } else if (component instanceof ButtonComponent) {
                    ButtonComponent buttonComp = (ButtonComponent) component;
                    extractButtonBackgroundImage(processedImage, buttonComp);
                    if (buttonComp.getBackgroundImage() != null) {
                        buttonImageExtracted++;
                    }
                }
            } catch (Exception e) {
                log.warn("提取组件图片失败: {}", e.getMessage());
            }
        }

        log.info("图片提取完成: ImageComponent={}, ButtonComponent={}", imageExtracted, buttonImageExtracted);
    }

    /**
     * 提取图片组件的图片数据
     */
    private void extractImageComponentData(Mat image, ImageComponent component) {
        int x = component.getPosition().getX();
        int y = component.getPosition().getY();
        int width = component.getSize().getWidth();
        int height = component.getSize().getHeight();

        // 裁剪图片区域
        Mat region = imageProcessor.cropImageRegion(image, x, y, width, height);

        // 检查裁剪是否成功
        if (region == null || region.empty()) {
            log.warn("图片组件裁剪失败: 位置=({},{}), 尺寸={}x{}", x, y, width, height);
            return;
        }

        // 根据图片类型选择压缩尺寸（提高清晰度）
        int maxSize;
        String imageType = component.getImageType();
        if ("background".equals(imageType)) {
            maxSize = 1920; // 背景图（高清）
        } else if ("decoration".equals(imageType)) {
            maxSize = 800; // 装饰图
        } else {
            maxSize = 600; // 内容图/小图标（提高清晰度）
        }

        // 转换为base64
        String base64Data = imageProcessor.matToBase64(region, maxSize);
        if (base64Data != null) {
            component.setPlaceholderUrl(base64Data);
            log.debug("提取图片组件成功: 位置=({},{}), 尺寸={}x{}, 类型={}",
                    x, y, width, height, imageType);
        } else {
            log.warn("图片组件base64转换失败: 位置=({},{}), 尺寸={}x{}", x, y, width, height);
        }
    }

    /**
     * 提取按钮的背景图片（如果不是纯色）
     */
    private void extractButtonBackgroundImage(Mat image, ButtonComponent component) {
        int x = component.getPosition().getX();
        int y = component.getPosition().getY();
        int width = component.getSize().getWidth();
        int height = component.getSize().getHeight();

        // 裁剪按钮区域
        Mat region = imageProcessor.cropImageRegion(image, x, y, width, height);

        // 检查裁剪是否成功
        if (region == null || region.empty()) {
            log.warn("按钮区域裁剪失败: 位置=({},{}), 尺寸={}x{}", x, y, width, height);
            return;
        }

        // 检测是否为纯色
        if (!imageProcessor.isRegionSolidColor(region)) {
            // 不是纯色，提取背景图（提高清晰度）
            String base64Data = imageProcessor.matToBase64(region, 600);
            if (base64Data != null) {
                component.setBackgroundImage(base64Data);
                log.debug("提取按钮背景图成功: 位置=({},{}), 尺寸={}x{}", x, y, width, height);
            } else {
                log.warn("按钮背景图base64转换失败: 位置=({},{}), 尺寸={}x{}", x, y, width, height);
            }
        } else {
            log.debug("按钮为纯色背景，跳过图片提取: 位置=({},{})", x, y);
        }
    }
}
