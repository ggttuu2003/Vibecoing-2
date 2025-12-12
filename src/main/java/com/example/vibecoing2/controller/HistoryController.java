package com.example.vibecoing2.controller;

import com.example.vibecoing2.dto.AnalysisHistoryRecord;
import com.example.vibecoing2.dto.ApiResponse;
import com.example.vibecoing2.dto.HistoryRecord;
import com.example.vibecoing2.service.HistoryService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 历史记录控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    /**
     * 获取历史记录列表
     *
     * @param page  页码（默认 1）
     * @param size  每页数量（默认 10）
     * @param style 风格筛选（可选）
     * @return 历史记录列表
     */
    @GetMapping("/list")
    public ApiResponse<HistoryListResponse> listHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String style
    ) {
        try {
            log.info("查询历史记录列表: page={}, size={}, style={}", page, size, style);

            List<HistoryRecord> records = historyService.listHistory(page, size, style);
            long total = historyService.getTotalCount(style);

            HistoryListResponse response = new HistoryListResponse(
                    records,
                    total,
                    page,
                    size,
                    (int) Math.ceil((double) total / size)
            );

            return ApiResponse.success(response);

        } catch (Exception e) {
            log.error("查询历史记录列表失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取历史记录详情
     *
     * @param id 历史记录 ID
     * @return 历史记录详情
     */
    @GetMapping("/{id}")
    public ApiResponse<HistoryRecord> getHistoryDetail(@PathVariable String id) {
        try {
            log.info("查询历史记录详情: id={}", id);

            HistoryRecord record = historyService.getHistoryDetail(id);
            if (record == null) {
                return ApiResponse.error(404, "历史记录不存在");
            }

            return ApiResponse.success(record);

        } catch (Exception e) {
            log.error("查询历史记录详情失败: id={}", id, e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 删除历史记录
     *
     * @param id 历史记录 ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteHistory(@PathVariable String id) {
        try {
            log.info("删除历史记录: id={}", id);

            boolean success = historyService.deleteHistory(id);
            if (!success) {
                return ApiResponse.error(500, "删除失败");
            }

            return ApiResponse.success("删除成功", null);

        } catch (Exception e) {
            log.error("删除历史记录失败: id={}", id, e);
            return ApiResponse.error(500, "删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取历史记录图片文件
     *
     * @param historyId 历史记录 ID
     * @param filename  图片文件名
     * @return 图片文件
     */
    @GetMapping("/image/{historyId}/{filename}")
    public ResponseEntity<Resource> getImage(
            @PathVariable String historyId,
            @PathVariable String filename
    ) {
        try {
            // 构建图片路径
            Path imagePath = Paths.get("history", "images", historyId, filename);
            File imageFile = imagePath.toFile();

            if (!imageFile.exists()) {
                log.warn("图片文件不存在: {}", imagePath);
                return ResponseEntity.notFound().build();
            }

            // 读取文件
            Resource resource = new FileSystemResource(imageFile);

            // 确定 MIME 类型
            String contentType = Files.probeContentType(imagePath);
            if (contentType == null) {
                contentType = "image/png";
            }

            log.debug("返回图片文件: {}", imagePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("获取图片文件失败: historyId={}, filename={}", historyId, filename, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 历史记录列表响应
     */
    @Data
    @AllArgsConstructor
    public static class HistoryListResponse {
        /**
         * 历史记录列表
         */
        private List<HistoryRecord> records;

        /**
         * 总记录数
         */
        private Long total;

        /**
         * 当前页码
         */
        private Integer page;

        /**
         * 每页数量
         */
        private Integer size;

        /**
         * 总页数
         */
        private Integer totalPages;
    }

    // ==================== 设计稿解析历史记录 API ====================

    /**
     * 获取设计稿解析历史记录列表
     *
     * @param page 页码（默认 1）
     * @param size 每页数量（默认 10）
     * @return 历史记录列表
     */
    @GetMapping("/analysis/list")
    public ApiResponse<AnalysisHistoryListResponse> listAnalysisHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            log.info("查询设计稿解析历史记录列表: page={}, size={}", page, size);

            List<AnalysisHistoryRecord> records = historyService.listAnalysisHistory(page, size);
            long total = historyService.getAnalysisTotalCount();

            AnalysisHistoryListResponse response = new AnalysisHistoryListResponse(
                    records,
                    total,
                    page,
                    size,
                    (int) Math.ceil((double) total / size)
            );

            return ApiResponse.success(response);

        } catch (Exception e) {
            log.error("查询设计稿解析历史记录列表失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取设计稿解析历史记录详情
     *
     * @param id 历史记录 ID
     * @return 历史记录详情
     */
    @GetMapping("/analysis/{id}")
    public ApiResponse<AnalysisHistoryRecord> getAnalysisHistoryDetail(@PathVariable String id) {
        try {
            log.info("查询设计稿解析历史记录详情: id={}", id);

            AnalysisHistoryRecord record = historyService.getAnalysisHistoryDetail(id);
            if (record == null) {
                return ApiResponse.error(404, "历史记录不存在");
            }

            return ApiResponse.success(record);

        } catch (Exception e) {
            log.error("查询设计稿解析历史记录详情失败: id={}", id, e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 删除设计稿解析历史记录
     *
     * @param id 历史记录 ID
     * @return 删除结果
     */
    @DeleteMapping("/analysis/{id}")
    public ApiResponse<Void> deleteAnalysisHistory(@PathVariable String id) {
        try {
            log.info("删除设计稿解析历史记录: id={}", id);

            boolean success = historyService.deleteAnalysisHistory(id);
            if (!success) {
                return ApiResponse.error(500, "删除失败");
            }

            return ApiResponse.success("删除成功", null);

        } catch (Exception e) {
            log.error("删除设计稿解析历史记录失败: id={}", id, e);
            return ApiResponse.error(500, "删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取设计稿解析历史记录原始图片
     *
     * @param historyId 历史记录 ID
     * @param filename  图片文件名
     * @return 图片文件
     */
    @GetMapping("/analysis/image/{historyId}/{filename}")
    public ResponseEntity<Resource> getAnalysisImage(
            @PathVariable String historyId,
            @PathVariable String filename
    ) {
        try {
            // 构建图片路径
            Path imagePath = Paths.get("history", "analysis", historyId, filename);
            File imageFile = imagePath.toFile();

            if (!imageFile.exists()) {
                log.warn("设计稿解析图片文件不存在: {}", imagePath);
                return ResponseEntity.notFound().build();
            }

            // 读取文件
            Resource resource = new FileSystemResource(imageFile);

            // 确定 MIME 类型
            String contentType = Files.probeContentType(imagePath);
            if (contentType == null) {
                contentType = "image/png";
            }

            log.debug("返回设计稿解析图片文件: {}", imagePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("获取设计稿解析图片文件失败: historyId={}, filename={}", historyId, filename, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 设计稿解析历史记录列表响应
     */
    @Data
    @AllArgsConstructor
    public static class AnalysisHistoryListResponse {
        /**
         * 历史记录列表
         */
        private List<AnalysisHistoryRecord> records;

        /**
         * 总记录数
         */
        private Long total;

        /**
         * 当前页码
         */
        private Integer page;

        /**
         * 每页数量
         */
        private Integer size;

        /**
         * 总页数
         */
        private Integer totalPages;
    }
}
