package com.example.vibecoing2.service;

import com.example.vibecoing2.config.AIConfig;
import com.example.vibecoing2.domain.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisionAIService {

    private final AIConfig aiConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // API 端点常量
    private static final String CHAT_COMPLETIONS_ENDPOINT = "/chat/completions";

    // 角色常量
    private static final String ROLE_USER = "user";

    // 内容类型常量
    private static final String CONTENT_TYPE_TEXT = "text";
    private static final String CONTENT_TYPE_IMAGE_URL = "image_url";

    // 图片格式常量
    private static final String IMAGE_MIME_TYPE_PNG = "image/png";
    private static final String IMAGE_MIME_TYPE_JPEG = "image/jpeg";
    private static final String DATA_URL_PREFIX = "data:%s;base64,%s";

    private static final String ANALYSIS_PROMPT = """
            你是一个专业的 UI 设计分析师。请逐个组件分析这张设计稿，识别每个独立的 UI 元素并输出 JSON。

            ⚠️ 关键要求：
            1. **不要识别整张背景图** - 只识别具体的、独立的组件
            2. **每个组件必须有视觉信息**：
               - 如果是图片组件，标记为需要提取图片
               - 如果是纯色区域，返回准确的颜色值（包括透明度）
            3. **逐个识别** - 将设计稿拆解为多个独立的组件，不要遗漏

            组件识别规则：

            📝 **文字组件** (type: "text")：
            - 所有文字内容（标题、正文、标签、数字、价格等）
            - 根据重要性选择标签：h1(主标题) / h2(副标题) / h3(小标题) / p(段落) / span(短文字)
            - 必须提取：文字内容、颜色、字体大小、字重、对齐方式

            🎯 **按钮组件** (type: "button")：
            - 所有可点击的按钮
            - HTML 标签：button
            - 必须提取：按钮文字、背景色、文字颜色、圆角、阴影

            🖼️ **图片/图形组件** (type: "image")：
            - **装饰性图形**：图标、边框、装饰元素、分割线
            - **内容图片**：商品图、头像、展示图
            - **纯色区块**：卡片背景、色块、蒙层

            ⚠️ 图片组件的颜色规则：
            - 如果是**纯色区块**（如卡片背景、色块），返回准确的 backgroundColor
            - 如果是**渐变色**，返回 background: "linear-gradient(...)"
            - 如果是**半透明蒙层**，返回 backgroundColor: "rgba(r, g, b, alpha)"
            - 如果是**真实图片**（人物、商品、图标），imageType 设为 "decoration" 或 "content"

            输出 JSON 格式（严格遵守）：
            {
              "components": [
                {
                  "type": "text|button|image",
                  "htmlTag": "h1|h2|h3|p|span|button|img|div",
                  "content": "文字内容（text和button需要）",
                  "position": {"x": 100, "y": 50},
                  "size": {"width": 300, "height": 60},
                  "layer": 2,
                  "imageType": "decoration|content（仅真实图片需要，纯色区块不需要）",
                  "cssStyles": {
                    "position": "absolute",
                    "top": "50px",
                    "left": "100px",
                    "width": "300px",
                    "height": "60px",
                    "backgroundColor": "#F5F5F5",
                    "fontSize": "16px",
                    "fontWeight": "600",
                    "color": "#333333",
                    "borderRadius": "8px",
                    "padding": "10px 20px",
                    "margin": "0",
                    "textAlign": "center",
                    "display": "block",
                    "boxShadow": "0 2px 8px rgba(0,0,0,0.1)",
                    "border": "1px solid #E0E0E0"
                  }
                }
              ]
            }

            Layer 层次规则：
            - layer: 0 = 最底层（大面积背景色块、页面底色）
            - layer: 1 = 装饰层（装饰图形、图标、边框）
            - layer: 2 = 内容层（文字、按钮、内容图片）
            - layer: 3 = 顶层（浮动按钮、提示框）

            cssStyles 属性要求：

            **所有组件必须包含**：
            - position: "absolute"
            - top, left, width, height（必须带 px 单位）
            - display: "block|inline-block|flex"
            - margin: "0"

            **文字组件额外包含**：
            - fontSize: "16px"
            - fontWeight: "400|500|600|700|800"
            - color: "#333333"
            - textAlign: "left|center|right"
            - lineHeight: "1.2|1.5|1.8"

            **按钮组件额外包含**：
            - backgroundColor: "#007AFF"
            - color: "#FFFFFF"
            - borderRadius: "8px"
            - padding: "10px 20px"
            - fontWeight: "600"
            - border: "none"
            - cursor: "pointer"

            **图片/图形组件额外包含**：
            - 如果是**纯色区块**：
              * backgroundColor: "#F5F5F5"（准确的颜色）
              * borderRadius: "8px"（如果有圆角）
            - 如果是**渐变色**：
              * background: "linear-gradient(135deg, #FF6B6B 0%, #4ECDC4 100%)"
            - 如果是**半透明**：
              * backgroundColor: "rgba(0, 0, 0, 0.5)"
            - 如果是**真实图片**：
              * objectFit: "cover|contain"
              * imageType: "decoration|content"

            注意事项：
            1. ⚠️ **不要识别整张背景图** - 只识别具体的组件
            2. 所有尺寸值必须带单位（px）
            3. 颜色必须是十六进制（#RRGGBB）或 rgba 格式
            4. 纯色区块使用 backgroundColor，不要标记为 imageType
            5. 只返回 JSON，不要有其他任何文字或 markdown 标记
            6. 仔细观察设计稿，识别所有可见的组件
            """;

    public List<Component> analyzeImage(String imagePath) throws IOException {
        log.info("==========================================");
        log.info("开始使用 AI 分析图片");
        log.info("图片路径: {}", imagePath);
        log.info("超时配置: {} ms ({} 秒)", aiConfig.getTimeout(), aiConfig.getTimeout() / 1000.0);
        log.info("重试配置: 最多重试 {} 次，重试间隔 {} ms",
                aiConfig.getMaxRetries(), aiConfig.getRetryDelay());
        log.info("图片压缩配置: 最大尺寸 {} px", aiConfig.getMaxImageSize());

        long startTime = System.currentTimeMillis();

        // 1. 编码图片
        log.info("[1/3] 开始编码图片为 Base64...");
        long encodeStartTime = System.currentTimeMillis();
        String base64Image = encodeImageToBase64(imagePath);
        long encodeTime = System.currentTimeMillis() - encodeStartTime;
        log.info("[1/3] 图片编码完成，耗时: {} ms", encodeTime);

        // 2. 调用 AI API
        log.info("[2/3] 开始调用 AI API...");
        long apiStartTime = System.currentTimeMillis();
        String responseJson = callClaudeVisionAPI(base64Image);
        long apiTime = System.currentTimeMillis() - apiStartTime;
        log.info("[2/3] AI API 调用完成，耗时: {} ms", apiTime);

        // 3. 解析响应
        log.info("[3/3] 开始解析 AI 响应...");
        long parseStartTime = System.currentTimeMillis();
        List<Component> components = parseAIResponse(responseJson);
        long parseTime = System.currentTimeMillis() - parseStartTime;
        log.info("[3/3] 响应解析完成，耗时: {} ms", parseTime);

        long totalTime = System.currentTimeMillis() - startTime;

        log.info("==========================================");
        log.info("AI 分析完成！");
        log.info("识别到组件数量: {}", components.size());
        log.info("阶段耗时统计: 编码={}ms, API={}ms, 解析={}ms", encodeTime, apiTime, parseTime);
        log.info("总耗时: {} ms ({} 秒)", totalTime, String.format("%.2f", totalTime / 1000.0));
        log.info("==========================================");

        return components;
    }

    private String encodeImageToBase64(String imagePath) throws IOException {
        File imageFile = new File(imagePath);

        // 压缩图片（如果需要）
        byte[] imageBytes = compressImageIfNeeded(imageFile);

        // 编码为 Base64
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        log.info("Base64 编码完成，长度: {} 字符 ({} KB)", base64.length(), base64.length() / 1024);

        return base64;
    }

    private String callClaudeVisionAPI(String base64Image) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(aiConfig.getTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(aiConfig.getTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(aiConfig.getTimeout(), TimeUnit.MILLISECONDS)
                .build();

        // 使用重试机制调用 API
        int maxRetries = aiConfig.getMaxRetries() != null ? aiConfig.getMaxRetries() : 0;
        long retryDelay = aiConfig.getRetryDelay() != null ? aiConfig.getRetryDelay() : 2000;

        return executeWithRetry(() -> callUnifiedAPI(client, base64Image), maxRetries, retryDelay);
    }

    /**
     * 带重试机制的执行器
     */
    private String executeWithRetry(RetryableOperation operation, int maxRetries, long retryDelay) throws IOException {
        int attempt = 0;
        IOException lastException = null;

        while (attempt <= maxRetries) {
            try {
                if (attempt > 0) {
                    log.info("重试第 {} 次 (最多 {} 次)...", attempt, maxRetries);
                }

                long startTime = System.currentTimeMillis();
                String result = operation.execute();
                long duration = System.currentTimeMillis() - startTime;

                if (attempt > 0) {
                    log.info("重试成功！本次耗时: {} ms", duration);
                } else {
                    log.info("API 调用成功，耗时: {} ms", duration);
                }

                return result;

            } catch (SocketTimeoutException e) {
                lastException = e;
                attempt++;
                log.warn("API 调用超时 (尝试 {}/{}): {}", attempt, maxRetries + 1, e.getMessage());

                if (attempt <= maxRetries) {
                    log.info("等待 {} ms 后重试...", retryDelay);
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("重试被中断", ie);
                    }
                }

            } catch (IOException e) {
                // 检查是否是可重试的网络错误
                if (isRetryableError(e)) {
                    lastException = e;
                    attempt++;
                    log.warn("API 调用失败 (尝试 {}/{}): {}", attempt, maxRetries + 1, e.getMessage());

                    if (attempt <= maxRetries) {
                        log.info("等待 {} ms 后重试...", retryDelay);
                        try {
                            Thread.sleep(retryDelay);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new IOException("重试被中断", ie);
                        }
                    }
                } else {
                    // 不可重试的错误，直接抛出
                    log.error("遇到不可重试的错误，停止重试: {}", e.getMessage());
                    throw e;
                }
            }
        }

        // 所有重试都失败
        log.error("API 调用失败，已达到最大重试次数 ({})", maxRetries + 1);
        if (lastException != null) {
            throw lastException;
        }
        throw new IOException("API 调用失败且没有捕获到异常信息");
    }

    /**
     * 判断是否是可重试的错误
     */
    private boolean isRetryableError(IOException e) {
        String message = e.getMessage();
        if (message == null) {
            return false;
        }

        // 网络相关的错误可以重试
        return message.contains("timeout") ||
                message.contains("Connection reset") ||
                message.contains("Connection refused") ||
                message.contains("Broken pipe") ||
                message.contains("Network is unreachable");
    }

    /**
     * 可重试的操作接口
     */
    @FunctionalInterface
    private interface RetryableOperation {
        String execute() throws IOException;
    }

    /**
     * 调用统一的 OpenAI 兼容 API
     */
    private String callUnifiedAPI(OkHttpClient client, String base64Image) throws IOException {
        String requestBody = buildMultimodalRequestBody(base64Image);
        String url = buildApiUrl();

        // 打印请求信息
        log.info("========== AI API 请求开始 ==========");
        log.info("请求 URL: {}", url);
        log.info("请求模型: {}", aiConfig.getModel());
        log.info("请求参数: max_tokens={}, temperature={}, top_p={}",
                aiConfig.getMaxTokens(), aiConfig.getTemperature(), aiConfig.getTopP());

        // 打印请求体（隐藏图片 base64 数据，只显示前100个字符）
        String logRequestBody = requestBody.length() > 500
            ? requestBody.substring(0, 500) + "... (已截断，总长度: " + requestBody.length() + ")"
            : requestBody;
        log.info("请求体预览: {}", logRequestBody);
        log.info("图片 Base64 长度: {} 字符", base64Image.length());

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + aiConfig.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                .build();

        log.info("发送请求中...");

        try (Response response = client.newCall(request).execute()) {
            log.info("收到响应: HTTP {}", response.code());

            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                log.error("========== API 调用失败 ==========");
                log.error("错误状态码: {}", response.code());
                log.error("错误响应体: {}", errorBody);
                log.error("====================================");
                throw new IOException("API 调用失败: " + response.code() + " - " + errorBody);
            }

            if (response.body() == null) {
                throw new IOException("API 返回空响应体");
            }
            String responseBody = response.body().string();

            // 打印响应信息（截断过长的响应）
            String logResponseBody = responseBody.length() > 1000
                ? responseBody.substring(0, 1000) + "... (已截断，总长度: " + responseBody.length() + ")"
                : responseBody;
            log.info("响应体预览: {}", logResponseBody);

            String result = parseUnifiedAPIResponse(responseBody);

            log.info("========== AI API 请求完成 ==========");

            return result;
        }
    }

    /**
     * 构建 API URL
     */
    private String buildApiUrl() {
        return aiConfig.getBaseUrl() + CHAT_COMPLETIONS_ENDPOINT;
    }

    /**
     * 构建多模态请求体（文本 + 图片）
     */
    private String buildMultimodalRequestBody(String base64Image) throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // 构建消息内容数组
            List<Object> contentList = buildMultimodalContent(base64Image);

            // 构建消息对象
            var message = new java.util.HashMap<String, Object>();
            message.put("role", ROLE_USER);
            message.put("content", contentList);

            // 构建完整请求体
            var requestBody = new java.util.HashMap<String, Object>();
            requestBody.put("model", aiConfig.getModel());
            requestBody.put("messages", List.of(message));
            requestBody.put("max_tokens", aiConfig.getMaxTokens());
            requestBody.put("temperature", aiConfig.getTemperature());
            requestBody.put("top_p", aiConfig.getTopP());

            return mapper.writeValueAsString(requestBody);
        } catch (Exception e) {
            log.error("构建请求体失败", e);
            throw new IOException("构建请求体失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建多模态内容（支持扩展更多类型）
     */
    private List<Object> buildMultimodalContent(String base64Image) {
        List<Object> contentList = new ArrayList<>();

        // 添加文本内容
        var textContent = new java.util.HashMap<String, String>();
        textContent.put("type", CONTENT_TYPE_TEXT);
        textContent.put("text", ANALYSIS_PROMPT);
        contentList.add(textContent);

        // 添加图片内容
        var imageContent = new java.util.HashMap<String, Object>();
        imageContent.put("type", CONTENT_TYPE_IMAGE_URL);

        var imageUrl = new java.util.HashMap<String, String>();
        imageUrl.put("url", String.format(DATA_URL_PREFIX, IMAGE_MIME_TYPE_PNG, base64Image));

        imageContent.put("image_url", imageUrl);
        contentList.add(imageContent);

        return contentList;
    }

    /**
     * 解析统一 API 响应（OpenAI 格式）
     */
    private String parseUnifiedAPIResponse(String responseBody) throws IOException {
        log.info("开始解析 API 响应...");

        JsonNode responseJson = objectMapper.readTree(responseBody);
        JsonNode choices = responseJson.get("choices");

        if (choices != null && choices.isArray() && choices.size() > 0) {
            JsonNode choice = choices.get(0);

            // 检查 finish_reason
            JsonNode finishReasonNode = choice.get("finish_reason");
            if (finishReasonNode != null) {
                String finishReason = finishReasonNode.asText();
                log.info("完成原因: {}", finishReason);

                if ("length".equals(finishReason)) {
                    log.error("响应被截断！当前 max_tokens={} 不足，请增加配置", aiConfig.getMaxTokens());
                    throw new IOException("AI 响应超过最大 token 限制被截断，请增加 app.ai.max-tokens 配置");
                }
            }

            JsonNode message = choice.get("message");
            if (message != null) {
                String content = message.get("content").asText();
                log.info("原始内容长度: {} 字符", content.length());

                // 清理可能的 markdown 代码块标记
                content = content.replaceAll("```json\\s*", "").replaceAll("```\\s*$", "").trim();
                log.info("清理后内容长度: {} 字符", content.length());

                // 预览清理后的内容
                String contentPreview = content.length() > 200
                    ? content.substring(0, 200) + "..."
                    : content;
                log.info("清理后内容预览: {}", contentPreview);

                return content;
            }
        }

        log.error("API 响应格式错误，无法找到 choices 或 message 节点");
        throw new IOException("API 响应格式错误");
    }

    private List<Component> parseAIResponse(String responseJson) throws IOException {
        log.info("开始解析组件数据...");
        List<Component> components = new ArrayList<>();

        JsonNode root = objectMapper.readTree(responseJson);
        JsonNode componentsNode = root.get("components");

        if (componentsNode == null || !componentsNode.isArray()) {
            log.warn("AI 响应中没有 components 数组");
            return components;
        }

        log.info("检测到 {} 个组件待解析", componentsNode.size());

        int textCount = 0, buttonCount = 0, imageCount = 0, unknownCount = 0;

        for (int i = 0; i < componentsNode.size(); i++) {
            JsonNode componentNode = componentsNode.get(i);
            String type = componentNode.get("type").asText();

            log.debug("解析第 {} 个组件，类型: {}", i + 1, type);

            Component component = createComponentByType(type, componentNode);

            if (component != null) {
                components.add(component);
                switch (type) {
                    case "text" -> textCount++;
                    case "button" -> buttonCount++;
                    case "image" -> imageCount++;
                    default -> unknownCount++;
                }
            }
        }

        log.info("组件解析统计: 文本={}, 按钮={}, 图片={}, 其他={}",
                textCount, buttonCount, imageCount, unknownCount);

        return components;
    }

    private Component createComponentByType(String type, JsonNode node) {
        try {
            switch (type) {
                case "text":
                    return parseTextComponent(node);
                case "button":
                    return parseButtonComponent(node);
                case "image":
                    return parseImageComponent(node);
                default:
                    log.warn("未知的组件类型: {}", type);
                    return null;
            }
        } catch (Exception e) {
            log.error("解析组件失败: type={}", type, e);
            return null;
        }
    }

    private TextComponent parseTextComponent(JsonNode node) {
        TextComponent component = new TextComponent();
        component.setId("text-ai-" + System.nanoTime());
        component.setContent(node.get("content").asText());
        component.setPosition(parsePosition(node.get("position")));
        component.setSize(parseSize(node.get("size")));
        component.setConfidence(0.8);

        // 读取 layer
        if (node.has("layer")) {
            component.setLayer(node.get("layer").asInt());
        }

        // 读取 HTML 标签
        if (node.has("htmlTag")) {
            component.setHtmlTag(node.get("htmlTag").asText());
        } else {
            component.setHtmlTag("p"); // 默认为段落
        }

        // 读取 CSS 样式
        if (node.has("cssStyles")) {
            component.setCssStyles(parseCssStyles(node.get("cssStyles")));
        }

        // 兼容旧的 style 字段
        JsonNode styleNode = node.get("style");
        if (styleNode != null) {
            if (styleNode.has("fontSize")) {
                component.setFontSize(styleNode.get("fontSize").asInt());
            }
            if (styleNode.has("fontWeight")) {
                component.setFontWeight(styleNode.get("fontWeight").asInt());
            }
            if (styleNode.has("color")) {
                component.setColor(styleNode.get("color").asText());
            }
            if (styleNode.has("textAlign")) {
                component.setTextAlign(styleNode.get("textAlign").asText());
            }
        }

        return component;
    }

    private ButtonComponent parseButtonComponent(JsonNode node) {
        ButtonComponent component = new ButtonComponent();
        component.setId("button-ai-" + System.nanoTime());
        component.setText(node.get("content").asText());
        component.setPosition(parsePosition(node.get("position")));
        component.setSize(parseSize(node.get("size")));
        component.setConfidence(0.8);

        // 读取 layer
        if (node.has("layer")) {
            component.setLayer(node.get("layer").asInt());
        }

        // 读取 HTML 标签
        if (node.has("htmlTag")) {
            component.setHtmlTag(node.get("htmlTag").asText());
        } else {
            component.setHtmlTag("button"); // 默认为 button
        }

        // 读取 CSS 样式
        if (node.has("cssStyles")) {
            component.setCssStyles(parseCssStyles(node.get("cssStyles")));
        }

        // 兼容旧的 style 字段
        JsonNode styleNode = node.get("style");
        if (styleNode != null) {
            if (styleNode.has("backgroundColor")) {
                component.setBackgroundColor(styleNode.get("backgroundColor").asText());
            }
            if (styleNode.has("color")) {
                component.setTextColor(styleNode.get("color").asText());
            }
            if (styleNode.has("fontSize")) {
                component.setFontSize(styleNode.get("fontSize").asInt());
            }
            if (styleNode.has("borderRadius")) {
                component.setBorderRadius(styleNode.get("borderRadius").asInt());
            }
        }

        component.setOnClick("handleButtonClick");

        return component;
    }

    private ImageComponent parseImageComponent(JsonNode node) {
        ImageComponent component = new ImageComponent();
        component.setId("image-ai-" + System.nanoTime());
        component.setPosition(parsePosition(node.get("position")));
        component.setSize(parseSize(node.get("size")));
        component.setConfidence(0.8);

        // 读取 imageType（background/decoration/content）
        if (node.has("imageType")) {
            component.setImageType(node.get("imageType").asText());
        }

        // 读取 layer
        if (node.has("layer")) {
            component.setLayer(node.get("layer").asInt());
        }

        // 读取 HTML 标签
        if (node.has("htmlTag")) {
            component.setHtmlTag(node.get("htmlTag").asText());
        } else {
            component.setHtmlTag("img"); // 默认为 img
        }

        // 读取 CSS 样式
        if (node.has("cssStyles")) {
            component.setCssStyles(parseCssStyles(node.get("cssStyles")));
        }

        // 设置样式
        component.setObjectFit("cover");

        // 兼容旧的 style 字段
        JsonNode styleNode = node.get("style");
        if (styleNode != null) {
            if (styleNode.has("borderRadius")) {
                component.setBorderRadius(styleNode.get("borderRadius").asInt());
            }
        }

        // 不再使用 placehold.co，而是标记为真实图片区域
        component.setPlaceholderAlt("图片区域");

        return component;
    }

    private Position parsePosition(JsonNode positionNode) {
        if (positionNode == null) {
            return new Position(0, 0);
        }
        return new Position(
            positionNode.get("x").asInt(),
            positionNode.get("y").asInt()
        );
    }

    private Size parseSize(JsonNode sizeNode) {
        if (sizeNode == null) {
            return new Size(100, 50);
        }
        return new Size(
            sizeNode.get("width").asInt(),
            sizeNode.get("height").asInt()
        );
    }

    /**
     * 解析 CSS 样式对象
     */
    private Map<String, String> parseCssStyles(JsonNode cssStylesNode) {
        Map<String, String> styles = new HashMap<>();
        if (cssStylesNode == null || !cssStylesNode.isObject()) {
            return styles;
        }

        // 遍历所有字段
        cssStylesNode.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            JsonNode value = entry.getValue();

            // 将值转换为字符串
            if (value.isTextual()) {
                styles.put(key, value.asText());
            } else if (value.isNumber()) {
                // 数字类型，可能需要添加单位
                styles.put(key, value.asText());
            } else {
                styles.put(key, value.toString());
            }
        });

        return styles;
    }

    /**
     * 压缩图片（如果需要）
     * 当图片尺寸超过配置的最大尺寸时进行压缩
     */
    private byte[] compressImageIfNeeded(File imageFile) throws IOException {
        BufferedImage originalImage = ImageIO.read(imageFile);
        if (originalImage == null) {
            log.warn("无法读取图片，使用原始文件字节");
            return Files.readAllBytes(imageFile.toPath());
        }

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        long originalSize = imageFile.length();

        log.info("原始图片尺寸: {}x{}, 大小: {} bytes ({} KB)",
                originalWidth, originalHeight, originalSize, originalSize / 1024);

        // 检查是否需要压缩
        int maxSize = aiConfig.getMaxImageSize() != null ? aiConfig.getMaxImageSize() : 1024;
        if (originalWidth <= maxSize && originalHeight <= maxSize) {
            log.info("图片尺寸未超过限制 ({}px)，无需压缩", maxSize);
            return Files.readAllBytes(imageFile.toPath());
        }

        // 计算压缩后的尺寸（保持宽高比）
        double scale;
        if (originalWidth > originalHeight) {
            scale = (double) maxSize / originalWidth;
        } else {
            scale = (double) maxSize / originalHeight;
        }

        int targetWidth = (int) (originalWidth * scale);
        int targetHeight = (int) (originalHeight * scale);

        log.info("开始压缩图片: {}x{} -> {}x{} (缩放比例: {})",
                originalWidth, originalHeight, targetWidth, targetHeight, String.format("%.2f", scale));

        // 压缩图片
        Image scaledImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage compressedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        compressedImage.getGraphics().drawImage(scaledImage, 0, 0, null);

        // 转换为字节数组
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(compressedImage, "jpg", baos);
        byte[] compressedBytes = baos.toByteArray();

        long compressedSize = compressedBytes.length;
        double compressionRatio = (1 - (double) compressedSize / originalSize) * 100;

        log.info("图片压缩完成: {} bytes ({} KB), 压缩率: {}%",
                compressedSize, compressedSize / 1024, String.format("%.1f", compressionRatio));

        return compressedBytes;
    }
}
