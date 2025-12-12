package com.example.vibecoing2.service;

import com.example.vibecoing2.dto.AnalysisHistoryRecord;
import com.example.vibecoing2.dto.GenerateImageRequest;
import com.example.vibecoing2.dto.GenerateImageResponse;
import com.example.vibecoing2.dto.HistoryRecord;
import com.example.vibecoing2.dto.TemplateResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 历史记录服务
 */
@Slf4j
@Service
public class HistoryService {

    private static final String HISTORY_BASE_DIR = "history";
    private static final String IMAGES_DIR = "images";
    private static final String DATA_DIR = "data";
    private static final String ANALYSIS_DIR = "analysis";

    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;

    public HistoryService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.httpClient = new OkHttpClient.Builder().build();

        // 初始化目录
        initDirectories();
    }

    /**
     * 初始化历史记录目录
     */
    private void initDirectories() {
        try {
            Files.createDirectories(Paths.get(HISTORY_BASE_DIR, IMAGES_DIR));
            Files.createDirectories(Paths.get(HISTORY_BASE_DIR, DATA_DIR));
            Files.createDirectories(Paths.get(HISTORY_BASE_DIR, ANALYSIS_DIR));
            log.info("历史记录目录初始化成功");
        } catch (IOException e) {
            log.error("创建历史记录目录失败", e);
        }
    }

    /**
     * 保存历史记录
     *
     * @param request  原始请求
     * @param response 生成响应
     */
    public void saveHistory(GenerateImageRequest request, GenerateImageResponse response) {
        try {
            // 生成历史记录 ID
            String historyId = UUID.randomUUID().toString();
            LocalDateTime timestamp = LocalDateTime.now();

            log.info("开始保存历史记录: historyId={}", historyId);

            // 创建图片目录
            Path imageDir = Paths.get(HISTORY_BASE_DIR, IMAGES_DIR, historyId);
            Files.createDirectories(imageDir);

            // 保存图片并收集路径
            List<String> imagePaths = new ArrayList<>();
            for (int i = 0; i < response.getImages().size(); i++) {
                GenerateImageResponse.GeneratedImage image = response.getImages().get(i);
                String filename = "image_" + (i + 1) + ".png";
                Path imagePath = imageDir.resolve(filename);

                // 处理图片数据（base64 或 URL）
                saveImage(image.getBase64(), imagePath);

                // 记录相对路径
                String relativePath = IMAGES_DIR + "/" + historyId + "/" + filename;
                imagePaths.add(relativePath);

                log.debug("保存图片 {}: {}", i + 1, relativePath);
            }

            // 构建历史记录对象
            HistoryRecord record = new HistoryRecord(
                    historyId,
                    timestamp,
                    request,
                    response.getMetadata(),
                    imagePaths,
                    request.getStyle(),
                    request.getModel(),
                    response.getImages().size(),
                    null  // images 字段在保存时为 null，查询时会动态加载
            );

            // 保存 JSON 元数据
            Path dataPath = Paths.get(HISTORY_BASE_DIR, DATA_DIR, historyId + ".json");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(dataPath.toFile(), record);

            log.info("历史记录保存成功: historyId={}, imageCount={}", historyId, imagePaths.size());

        } catch (Exception e) {
            log.error("保存历史记录失败（不影响主流程）", e);
        }
    }

    /**
     * 保存图片（处理 base64 或 URL）
     *
     * @param imageData base64 数据或 URL
     * @param targetPath 目标文件路径
     */
    private void saveImage(String imageData, Path targetPath) throws IOException {
        if (imageData.startsWith("data:")) {
            // Base64 格式：data:image/png;base64,xxxxx
            String base64Data = imageData.substring(imageData.indexOf(",") + 1);
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            Files.write(targetPath, imageBytes);
            log.debug("保存 Base64 图片: {}", targetPath);
        } else if (imageData.startsWith("http://") || imageData.startsWith("https://")) {
            // URL 格式：下载图片
            downloadImage(imageData, targetPath);
            log.debug("下载 URL 图片: {}", targetPath);
        } else {
            throw new IllegalArgumentException("不支持的图片数据格式: " + imageData.substring(0, Math.min(50, imageData.length())));
        }
    }

    /**
     * 从 URL 下载图片
     *
     * @param url        图片 URL
     * @param targetPath 目标文件路径
     */
    private void downloadImage(String url, Path targetPath) throws IOException {
        Request request = new Request.Builder().url(url).build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("下载图片失败: HTTP " + response.code());
            }
            byte[] imageBytes = response.body().bytes();
            Files.write(targetPath, imageBytes);
        }
    }

    /**
     * 列出历史记录
     *
     * @param page  页码（从 1 开始）
     * @param size  每页数量
     * @param style 风格筛选（可选）
     * @return 历史记录列表
     */
    public List<HistoryRecord> listHistory(int page, int size, String style) {
        try {
            Path dataDir = Paths.get(HISTORY_BASE_DIR, DATA_DIR);
            if (!Files.exists(dataDir)) {
                return Collections.emptyList();
            }

            // 读取所有 JSON 文件
            List<HistoryRecord> allRecords = Files.list(dataDir)
                    .filter(path -> path.toString().endsWith(".json"))
                    .map(this::readHistoryRecord)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // 按风格筛选
            if (style != null && !style.trim().isEmpty()) {
                allRecords = allRecords.stream()
                        .filter(record -> style.equals(record.getStyle()))
                        .collect(Collectors.toList());
            }

            // 按时间倒序排序
            allRecords.sort(Comparator.comparing(HistoryRecord::getTimestamp).reversed());

            // 分页
            int start = (page - 1) * size;
            int end = Math.min(start + size, allRecords.size());
            if (start >= allRecords.size()) {
                return Collections.emptyList();
            }

            return allRecords.subList(start, end);

        } catch (Exception e) {
            log.error("查询历史记录失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取历史记录详情
     *
     * @param historyId 历史记录 ID
     * @return 历史记录详情
     */
    public HistoryRecord getHistoryDetail(String historyId) {
        try {
            Path dataPath = Paths.get(HISTORY_BASE_DIR, DATA_DIR, historyId + ".json");
            if (!Files.exists(dataPath)) {
                log.warn("历史记录不存在: {}", historyId);
                return null;
            }
            return readHistoryRecord(dataPath);
        } catch (Exception e) {
            log.error("读取历史记录详情失败: historyId={}", historyId, e);
            return null;
        }
    }

    /**
     * 删除历史记录
     *
     * @param historyId 历史记录 ID
     * @return 是否删除成功
     */
    public boolean deleteHistory(String historyId) {
        try {
            // 删除 JSON 文件
            Path dataPath = Paths.get(HISTORY_BASE_DIR, DATA_DIR, historyId + ".json");
            if (Files.exists(dataPath)) {
                Files.delete(dataPath);
                log.debug("删除元数据文件: {}", dataPath);
            }

            // 删除图片目录
            Path imageDir = Paths.get(HISTORY_BASE_DIR, IMAGES_DIR, historyId);
            if (Files.exists(imageDir)) {
                deleteDirectory(imageDir.toFile());
                log.debug("删除图片目录: {}", imageDir);
            }

            log.info("历史记录删除成功: historyId={}", historyId);
            return true;

        } catch (Exception e) {
            log.error("删除历史记录失败: historyId={}", historyId, e);
            return false;
        }
    }

    /**
     * 递归删除目录
     *
     * @param directory 目录
     */
    private void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        directory.delete();
    }

    /**
     * 读取历史记录
     *
     * @param path JSON 文件路径
     * @return 历史记录对象
     */
    private HistoryRecord readHistoryRecord(Path path) {
        try {
            HistoryRecord record = objectMapper.readValue(path.toFile(), HistoryRecord.class);

            // 加载图片数据（转换为 base64）
            if (record != null) {
                loadImagesForRecord(record);
            }

            return record;
        } catch (com.fasterxml.jackson.core.io.JsonEOFException e) {
            log.warn("历史记录文件损坏（已跳过）: {}", path);
            return null;
        } catch (Exception e) {
            log.error("读取历史记录文件失败: {}", path, e);
            return null;
        }
    }

    /**
     * 为历史记录加载图片数据
     *
     * @param record 历史记录
     */
    private void loadImagesForRecord(HistoryRecord record) {
        try {
            if (record.getImagePaths() == null || record.getImagePaths().isEmpty()) {
                return;
            }

            List<HistoryRecord.ImageData> images = new ArrayList<>();

            for (String relativePath : record.getImagePaths()) {
                try {
                    // 构建完整路径
                    Path imagePath = Paths.get(HISTORY_BASE_DIR, relativePath);

                    if (Files.exists(imagePath)) {
                        // 读取图片文件并转换为 base64
                        byte[] imageBytes = Files.readAllBytes(imagePath);
                        String base64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);

                        // 从 request 中获取尺寸信息
                        int width = 1080;
                        int height = 1080;
                        if (record.getStyle() != null && "xiaohongshu".equals(record.getStyle())) {
                            height = 1350;
                        }

                        images.add(new HistoryRecord.ImageData(
                                base64,
                                width,
                                height,
                                record.getStyle()
                        ));
                    } else {
                        log.warn("图片文件不存在: {}", imagePath);
                    }
                } catch (Exception e) {
                    log.error("加载图片失败: {}", relativePath, e);
                }
            }

            record.setImages(images);

        } catch (Exception e) {
            log.error("加载历史记录图片失败", e);
        }
    }

    /**
     * 获取历史记录总数
     *
     * @param style 风格筛选（可选）
     * @return 总数
     */
    public long getTotalCount(String style) {
        try {
            Path dataDir = Paths.get(HISTORY_BASE_DIR, DATA_DIR);
            if (!Files.exists(dataDir)) {
                return 0;
            }

            long count = Files.list(dataDir)
                    .filter(path -> path.toString().endsWith(".json"))
                    .count();

            // 如果有风格筛选，需要读取文件过滤
            if (style != null && !style.trim().isEmpty()) {
                count = Files.list(dataDir)
                        .filter(path -> path.toString().endsWith(".json"))
                        .map(this::readHistoryRecord)
                        .filter(Objects::nonNull)
                        .filter(record -> style.equals(record.getStyle()))
                        .count();
            }

            return count;
        } catch (Exception e) {
            log.error("获取历史记录总数失败", e);
            return 0;
        }
    }

    // ==================== 设计稿解析历史记录 ====================

    /**
     * 保存设计稿解析历史记录
     *
     * @param originalImageData 原始图片的 base64 数据
     * @param templateResponse  解析响应
     */
    public void saveAnalysisHistory(String originalImageData, TemplateResponse templateResponse) {
        try {
            // 生成历史记录 ID
            String historyId = UUID.randomUUID().toString();
            LocalDateTime timestamp = LocalDateTime.now();

            log.info("开始保存设计稿解析历史记录: historyId={}", historyId);

            // 创建分析目录
            Path analysisDir = Paths.get(HISTORY_BASE_DIR, ANALYSIS_DIR, historyId);
            Files.createDirectories(analysisDir);

            // 保存原始图片
            String filename = "original.png";
            Path imagePath = analysisDir.resolve(filename);
            saveImage(originalImageData, imagePath);

            // 记录相对路径
            String relativePath = ANALYSIS_DIR + "/" + historyId + "/" + filename;

            // 确定使用的分析引擎
            String analysisEngine = buildAnalysisEngineString(templateResponse.getMetadata());

            // 计算组件数量（安全处理可能的 null 值）
            int componentCount = 0;
            if (templateResponse.getTemplate() != null && templateResponse.getTemplate().getComponents() != null) {
                componentCount = templateResponse.getTemplate().getComponents().size();
            }

            // 构建历史记录对象
            AnalysisHistoryRecord record = new AnalysisHistoryRecord(
                    historyId,
                    timestamp,
                    relativePath,
                    templateResponse.getTemplate(),
                    templateResponse.getMetadata(),
                    componentCount,
                    analysisEngine
            );

            // 保存 JSON 元数据
            Path dataPath = Paths.get(HISTORY_BASE_DIR, ANALYSIS_DIR, historyId + ".json");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(dataPath.toFile(), record);

            log.info("设计稿解析历史记录保存成功: historyId={}, componentCount={}",
                    historyId, record.getComponentCount());

        } catch (Exception e) {
            log.error("保存设计稿解析历史记录失败（不影响主流程）", e);
        }
    }

    /**
     * 构建分析引擎字符串
     */
    private String buildAnalysisEngineString(TemplateResponse.AnalysisMetadata metadata) {
        List<String> engines = new ArrayList<>();
        if (metadata.getAiUsed() != null && metadata.getAiUsed()) {
            engines.add("AI");
        }
        if (metadata.getOcrUsed() != null && metadata.getOcrUsed()) {
            engines.add("OCR");
        }
        if (metadata.getCvUsed() != null && metadata.getCvUsed()) {
            engines.add("CV");
        }
        return String.join(" + ", engines);
    }

    /**
     * 列出设计稿解析历史记录
     *
     * @param page 页码（从 1 开始）
     * @param size 每页数量
     * @return 历史记录列表
     */
    public List<AnalysisHistoryRecord> listAnalysisHistory(int page, int size) {
        try {
            Path analysisDir = Paths.get(HISTORY_BASE_DIR, ANALYSIS_DIR);
            if (!Files.exists(analysisDir)) {
                return Collections.emptyList();
            }

            // 读取所有 JSON 文件
            List<AnalysisHistoryRecord> allRecords = Files.list(analysisDir)
                    .filter(path -> path.toString().endsWith(".json"))
                    .map(this::readAnalysisHistoryRecord)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // 按时间倒序排序
            allRecords.sort(Comparator.comparing(AnalysisHistoryRecord::getTimestamp).reversed());

            // 分页
            int start = (page - 1) * size;
            int end = Math.min(start + size, allRecords.size());
            if (start >= allRecords.size()) {
                return Collections.emptyList();
            }

            return allRecords.subList(start, end);

        } catch (Exception e) {
            log.error("查询设计稿解析历史记录失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取设计稿解析历史记录详情
     *
     * @param historyId 历史记录 ID
     * @return 历史记录详情
     */
    public AnalysisHistoryRecord getAnalysisHistoryDetail(String historyId) {
        try {
            Path dataPath = Paths.get(HISTORY_BASE_DIR, ANALYSIS_DIR, historyId + ".json");
            if (!Files.exists(dataPath)) {
                log.warn("设计稿解析历史记录不存在: {}", historyId);
                return null;
            }
            return readAnalysisHistoryRecord(dataPath);
        } catch (Exception e) {
            log.error("读取设计稿解析历史记录详情失败: historyId={}", historyId, e);
            return null;
        }
    }

    /**
     * 删除设计稿解析历史记录
     *
     * @param historyId 历史记录 ID
     * @return 是否删除成功
     */
    public boolean deleteAnalysisHistory(String historyId) {
        try {
            // 删除 JSON 文件
            Path dataPath = Paths.get(HISTORY_BASE_DIR, ANALYSIS_DIR, historyId + ".json");
            if (Files.exists(dataPath)) {
                Files.delete(dataPath);
                log.debug("删除元数据文件: {}", dataPath);
            }

            // 删除图片目录
            Path imageDir = Paths.get(HISTORY_BASE_DIR, ANALYSIS_DIR, historyId);
            if (Files.exists(imageDir)) {
                deleteDirectory(imageDir.toFile());
                log.debug("删除图片目录: {}", imageDir);
            }

            log.info("设计稿解析历史记录删除成功: historyId={}", historyId);
            return true;

        } catch (Exception e) {
            log.error("删除设计稿解析历史记录失败: historyId={}", historyId, e);
            return false;
        }
    }

    /**
     * 获取设计稿解析历史记录总数
     *
     * @return 总数
     */
    public long getAnalysisTotalCount() {
        try {
            Path analysisDir = Paths.get(HISTORY_BASE_DIR, ANALYSIS_DIR);
            if (!Files.exists(analysisDir)) {
                return 0;
            }

            return Files.list(analysisDir)
                    .filter(path -> path.toString().endsWith(".json"))
                    .count();

        } catch (Exception e) {
            log.error("获取设计稿解析历史记录总数失败", e);
            return 0;
        }
    }

    /**
     * 读取设计稿解析历史记录
     *
     * @param path JSON 文件路径
     * @return 历史记录对象
     */
    private AnalysisHistoryRecord readAnalysisHistoryRecord(Path path) {
        try {
            return objectMapper.readValue(path.toFile(), AnalysisHistoryRecord.class);
        } catch (com.fasterxml.jackson.core.io.JsonEOFException e) {
            log.warn("设计稿解析历史记录文件损坏（已跳过）: {}", path);
            return null;
        } catch (Exception e) {
            log.error("读取设计稿解析历史记录文件失败: {}", path, e);
            return null;
        }
    }
}
