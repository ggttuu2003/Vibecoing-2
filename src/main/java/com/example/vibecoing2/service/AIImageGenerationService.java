package com.example.vibecoing2.service;

import com.example.vibecoing2.config.AIImageGenConfig;
import com.example.vibecoing2.dto.AIImageGenRequest;
import com.example.vibecoing2.dto.AIImageGenResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * AI 图像生成服务
 * 调用 Google GenAI API (Vertex AI) 生成图片
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIImageGenerationService {

    private final AIImageGenConfig config;
    private final ObjectMapper objectMapper;
    private OkHttpClient httpClient;

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    /**
     * 获取 HTTP 客户端（懒加载）
     */
    private OkHttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = new OkHttpClient.Builder()
                    .connectTimeout(config.getTimeout(), TimeUnit.MILLISECONDS)
                    .readTimeout(config.getTimeout(), TimeUnit.MILLISECONDS)
                    .writeTimeout(config.getTimeout(), TimeUnit.MILLISECONDS)
                    .build();
        }
        return httpClient;
    }

    /**
     * 生成图片
     *
     * @param prompt 提示词
     * @param model  模型名称
     * @return Base64 编码的图片数据（不含 data:image/png;base64, 前缀）
     * @throws IOException 如果生成失败
     */
    public String generateImage(String prompt, String model) throws IOException {
        // 验证参数
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("Prompt 不能为空");
        }

        // 使用默认模型（如果未指定）
        String actualModel = (model != null && !model.trim().isEmpty()) ? model : config.getDefaultModel();

        // 验证模型是否支持
        if (!config.isSupportedModel(actualModel)) {
            throw new IllegalArgumentException("不支持的模型: " + actualModel);
        }

        log.info("开始生成图片: model={}, promptLength={}", actualModel, prompt.length());

        // 构建请求
        AIImageGenRequest request = AIImageGenRequest.createImageRequest(prompt);
        String requestJson = objectMapper.writeValueAsString(request);

        log.debug("API 请求: {}", requestJson);

        // 发送请求
        String imageData = sendRequestWithRetry(requestJson, actualModel, config.getMaxRetries());

        log.info("图片生成成功: model={}", actualModel);
        return imageData;
    }

    /**
     * 带重试的请求发送
     */
    private String sendRequestWithRetry(String requestJson, String model, int maxRetries) throws IOException {
        IOException lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return sendRequest(requestJson, model);
            } catch (IOException e) {
                lastException = e;
                log.warn("第 {} 次请求失败: {}", attempt, e.getMessage());

                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(2000); // 重试前等待 2 秒
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("请求被中断", ie);
                    }
                }
            }
        }

        throw new IOException("请求失败，已重试 " + maxRetries + " 次", lastException);
    }

    /**
     * 发送单次请求
     */
    private String sendRequest(String requestJson, String model) throws IOException {
        // 从模型名称中提取实际的模型 ID（去掉 google/ 前缀）
        // 例如: "google/gemini-2.5-flash-image" -> "gemini-2.5-flash-image"
        String modelId = model.replace("google/", "");

        // 构建 URL - 使用 Vertex AI 标准路径格式
        // 格式: /v1/publishers/google/models/{modelId}:generateContent
        String url = config.getBaseUrl() + "/v1/publishers/google/models/" + modelId + ":generateContent";

        log.info("请求 URL: {}", url);
        log.debug("使用模型: {}", model);
        log.debug("模型 ID: {}", modelId);
        log.debug("Base URL: {}", config.getBaseUrl());

        RequestBody body = RequestBody.create(requestJson, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("x-goog-api-key", config.getApiKey())
                .build();

        try (Response response = getHttpClient().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "无响应体";
                // 截断错误响应，避免占满控制台
                String truncatedError = errorBody.length() > 500 ? errorBody.substring(0, 500) + "... (truncated)" : errorBody;
                log.error("API 请求失败: code={}, body={}", response.code(), truncatedError);
                throw new IOException("API 请求失败: HTTP " + response.code() + " - " + truncatedError);
            }

            String responseBody = response.body().string();
            // 截断响应体，避免打印完整的 base64 图片数据
            String truncatedResponse = responseBody.length() > 500
                ? responseBody.substring(0, 500) + "... (truncated, total length: " + responseBody.length() + " chars)"
                : responseBody;
            log.debug("API 响应: {}", truncatedResponse);

            // 解析响应
            AIImageGenResponse apiResponse = objectMapper.readValue(responseBody, AIImageGenResponse.class);

            // 提取图片数据
            String imageData = apiResponse.extractImageData();
            if (imageData == null || imageData.isEmpty()) {
                throw new IOException("API 响应中没有图片数据");
            }

            // 记录成功日志（不打印完整的 base64 数据）
            log.info("成功提取图片数据，长度: {} 字符", imageData.length());

            return imageData;
        }
    }

    /**
     * 将 base64 图片数据转换为完整的 data URL
     *
     * @param base64Data Base64 编码的图片数据
     * @return 完整的 data URL (data:image/png;base64,xxx)
     */
    public String toDataUrl(String base64Data) {
        if (base64Data == null || base64Data.isEmpty()) {
            return null;
        }
        // 如果已经包含前缀，直接返回
        if (base64Data.startsWith("data:")) {
            return base64Data;
        }
        // 添加前缀
        return "data:image/png;base64," + base64Data;
    }
}
