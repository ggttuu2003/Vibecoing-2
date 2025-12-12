package com.example.vibecoing2.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Component
public class FileUtil {

    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "bmp"};

    public String saveUploadedFile(MultipartFile file, String uploadDir) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (!isAllowedExtension(extension)) {
            throw new IllegalArgumentException("不支持的文件格式: " + extension);
        }

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = UUID.randomUUID().toString() + "." + extension;
        Path filePath = uploadPath.resolve(fileName);

        Files.copy(file.getInputStream(), filePath);

        log.info("文件已保存: {}", filePath);

        return filePath.toString();
    }

    private boolean isAllowedExtension(String extension) {
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (allowed.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    public void ensureDirectoryExists(String dirPath) throws IOException {
        Path path = Paths.get(dirPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
            log.info("目录已创建: {}", dirPath);
        }
    }

    public void deleteFile(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
            log.info("文件已删除: {}", filePath);
        } catch (IOException e) {
            log.error("删除文件失败: {}", filePath, e);
        }
    }
}
