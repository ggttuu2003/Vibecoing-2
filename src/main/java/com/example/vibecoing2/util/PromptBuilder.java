package com.example.vibecoing2.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 图像生成 Prompt 构建器
 * 将用户输入转换为适合 AI 图像生成的提示词
 */
public class PromptBuilder {

    private static final String XIAOHONGSHU_TEMPLATE_PATH = "/prompts/xiaohongshu-template.txt";
    private static final String ADVERTISING_TEMPLATE_PATH = "/prompts/advertising-template.txt";

    private static final String PLACEHOLDER_TITLE = "{title}";
    private static final String PLACEHOLDER_SUBTITLE = "{subtitle}";
    private static final String PLACEHOLDER_KEYWORDS = "{keywords}";

    private static final Map<String, String> TEMPLATE_CACHE = new HashMap<>();

    /**
     * 从 classpath 加载模板文件
     *
     * @param templatePath 模板文件路径
     * @return 模板内容
     */
    private static String loadTemplate(String templatePath) {
        if (TEMPLATE_CACHE.containsKey(templatePath)) {
            return TEMPLATE_CACHE.get(templatePath);
        }

        try (InputStream inputStream = PromptBuilder.class.getResourceAsStream(templatePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("模板文件不存在: " + templatePath);
            }

            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }

            String template = content.toString();
            TEMPLATE_CACHE.put(templatePath, template);
            return template;

        } catch (IOException e) {
            throw new RuntimeException("读取模板文件失败: " + templatePath, e);
        }
    }

    /**
     * 替换模板中的占位符
     *
     * @param template     模板内容
     * @param title        主标题
     * @param subtitle     副标题
     * @param keywords     关键词列表
     * @param keywordStyle 关键词格式化样式 (xiaohongshu 或 advertising)
     * @return 替换后的内容
     */
    private static String replaceVariables(String template, String title, String subtitle,
                                          List<String> keywords, String keywordStyle) {
        String result = template;

        result = result.replace(PLACEHOLDER_TITLE, title);

        if (subtitle != null && !subtitle.trim().isEmpty()) {
            String subtitleLine = "xiaohongshu".equals(keywordStyle)
                ? "副标题:" + subtitle
                : "副信息:" + subtitle;
            result = result.replace(PLACEHOLDER_SUBTITLE, subtitleLine);
        } else {
            result = removeLineWithPlaceholder(result, PLACEHOLDER_SUBTITLE);
        }

        if (keywords != null && !keywords.isEmpty()) {
            String formattedKeywords = formatKeywords(keywords, keywordStyle);
            result = result.replace(PLACEHOLDER_KEYWORDS, formattedKeywords);
        } else {
            result = removeLineWithPlaceholder(result, PLACEHOLDER_KEYWORDS);
        }

        return result;
    }

    /**
     * 格式化关键词列表
     *
     * @param keywords     关键词列表
     * @param keywordStyle 格式化样式
     * @return 格式化后的关键词字符串
     */
    private static String formatKeywords(List<String> keywords, String keywordStyle) {
        if ("xiaohongshu".equals(keywordStyle)) {
            StringBuilder formatted = new StringBuilder("标签:");
            for (int i = 0; i < keywords.size(); i++) {
                formatted.append("#").append(keywords.get(i));
                if (i < keywords.size() - 1) {
                    formatted.append(" ");
                }
            }
            return formatted.toString();
        } else {
            StringBuilder formatted = new StringBuilder("关键点:");
            for (int i = 0; i < keywords.size(); i++) {
                formatted.append(keywords.get(i));
                if (i < keywords.size() - 1) {
                    formatted.append("、");
                }
            }
            return formatted.toString();
        }
    }

    /**
     * 移除包含占位符的整行
     *
     * @param content     内容
     * @param placeholder 占位符
     * @return 移除后的内容
     */
    private static String removeLineWithPlaceholder(String content, String placeholder) {
        String[] lines = content.split("\n");
        StringBuilder result = new StringBuilder();

        for (String line : lines) {
            if (!line.contains(placeholder)) {
                result.append(line).append("\n");
            }
        }

        return result.toString();
    }

    /**
     * 构建小红书风格的 prompt
     *
     * @param title    主标题
     * @param subtitle 副标题
     * @param keywords 关键词列表
     * @return 生成的 prompt
     */
    public static String buildXiaohongshuPrompt(String title, String subtitle, List<String> keywords) {
        String template = loadTemplate(XIAOHONGSHU_TEMPLATE_PATH);
        return replaceVariables(template, title, subtitle, keywords, "xiaohongshu");
    }

    /**
     * 构建广告投放风格的 prompt
     *
     * @param title    主标题
     * @param subtitle 副标题
     * @param keywords 关键词列表
     * @return 生成的 prompt
     */
    public static String buildAdvertisingPrompt(String title, String subtitle, List<String> keywords) {
        String template = loadTemplate(ADVERTISING_TEMPLATE_PATH);
        return replaceVariables(template, title, subtitle, keywords, "advertising");
    }

    /**
     * 根据风格类型构建 prompt
     *
     * @param style    风格类型 (xiaohongshu 或 advertising_a)
     * @param title    主标题
     * @param subtitle 副标题
     * @param keywords 关键词列表
     * @return 生成的 prompt
     */
    public static String buildPrompt(String style, String title, String subtitle, List<String> keywords) {
        if (style == null || style.trim().isEmpty()) {
            throw new IllegalArgumentException("风格类型不能为空");
        }

        switch (style.toLowerCase()) {
            case "xiaohongshu":
                return buildXiaohongshuPrompt(title, subtitle, keywords);
            case "advertising_a":
                return buildAdvertisingPrompt(title, subtitle, keywords);
            default:
                throw new IllegalArgumentException("不支持的风格类型: " + style);
        }
    }
}
