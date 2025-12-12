package com.example.vibecoing2.dto;

import com.example.vibecoing2.domain.PageTemplate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateResponse {
    private Boolean success;
    private String message;
    private PageTemplate template;
    private AnalysisMetadata metadata;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalysisMetadata {
        private Long processingTimeMs;
        private Integer textCount;
        private Integer buttonCount;
        private Integer imageCount;
        private String aiModel;
        private Boolean aiUsed;
        private Boolean ocrUsed;
        private Boolean cvUsed;
    }

    public static TemplateResponse success(PageTemplate template, AnalysisMetadata metadata) {
        return new TemplateResponse(true, "分析成功", template, metadata);
    }

    public static TemplateResponse error(String message) {
        return new TemplateResponse(false, message, null, null);
    }
}
