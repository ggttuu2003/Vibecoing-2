package com.example.vibecoing2.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "app.opencv")
public class OpenCVConfig {
    private Integer minButtonWidth;
    private Integer minButtonHeight;
    private Double buttonAspectRatioMin;
    private Double buttonAspectRatioMax;

    @PostConstruct
    public void init() {
        try {
            OpenCV.loadLocally();
            log.info("OpenCV 加载成功");
        } catch (Exception e) {
            log.error("OpenCV 加载失败", e);
        }
    }
}
