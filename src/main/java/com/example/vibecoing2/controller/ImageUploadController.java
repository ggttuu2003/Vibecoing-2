package com.example.vibecoing2.controller;

import com.example.vibecoing2.dto.ApiResponse;
import com.example.vibecoing2.dto.TemplateResponse;
import com.example.vibecoing2.service.ImageAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/analyze")
@RequiredArgsConstructor
public class ImageUploadController {

    private final ImageAnalysisService imageAnalysisService;

    @PostMapping
    public ApiResponse<TemplateResponse> analyzeImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "enableAI", defaultValue = "true") Boolean enableAI,
            @RequestParam(value = "enableOCR", defaultValue = "false") Boolean enableOCR,
            @RequestParam(value = "enableCV", defaultValue = "true") Boolean enableCV
    ) {
        log.info("收到图片分析请求: filename={}, size={}", image.getOriginalFilename(), image.getSize());

        if (image.isEmpty()) {
            return ApiResponse.error(400, "图片文件不能为空");
        }

        String extension = getFileExtension(image.getOriginalFilename());
        if (!isValidImageExtension(extension)) {
            return ApiResponse.error(400, "不支持的文件格式，仅支持 jpg, jpeg, png, bmp");
        }

        if (image.getSize() > 10 * 1024 * 1024) {
            return ApiResponse.error(400, "文件大小不能超过 10MB");
        }

        try {
            TemplateResponse result = imageAnalysisService.analyzeImage(image, enableAI, enableOCR, enableCV);

            if (result.getSuccess()) {
                return ApiResponse.success("分析成功", result);
            } else {
                return ApiResponse.error(500, result.getMessage());
            }
        } catch (Exception e) {
            log.error("图片分析失败", e);
            return ApiResponse.error(500, "图片分析失败: " + e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private boolean isValidImageExtension(String extension) {
        return extension.equals("jpg") || extension.equals("jpeg") ||
               extension.equals("png") || extension.equals("bmp");
    }
}
