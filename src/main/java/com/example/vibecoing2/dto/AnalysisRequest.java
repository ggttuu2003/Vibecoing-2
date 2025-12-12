package com.example.vibecoing2.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class AnalysisRequest {
    @NotNull(message = "图片文件不能为空")
    private MultipartFile image;

    private Boolean enableAI = true;
    private Boolean enableOCR = true;
    private Boolean enableCV = true;
    private Integer standardWidth = 750;
}
