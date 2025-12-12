package com.example.vibecoing2.service;

import com.example.vibecoing2.domain.Component;
import com.example.vibecoing2.domain.ImageComponent;
import com.example.vibecoing2.domain.PageTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LayoutAnalysisService {

    // 背景图面积阈值：占页面面积的60%以上视为背景图
    private static final double BACKGROUND_AREA_THRESHOLD = 0.6;

    // 默认layer值
    private static final int LAYER_BACKGROUND = 0;
    private static final int LAYER_DEFAULT = 1;

    public void analyzeLayout(PageTemplate template) {
        List<Component> components = template.getComponents();

        if (components.isEmpty()) {
            return;
        }

        // 检测并标记背景图
        detectAndMarkBackgroundImages(template, components);

        // 分配层次
        assignLayers(components);

        // 识别区域
        identifySections(template);

        log.info("布局分析完成");
    }

    /**
     * 检测并标记背景图
     * 大面积图片（面积 > 页面面积的60%）自动标记为背景层
     */
    private void detectAndMarkBackgroundImages(PageTemplate template, List<Component> components) {
        int pageArea = template.getPage().getWidth() * template.getPage().getHeight();
        double backgroundThreshold = pageArea * BACKGROUND_AREA_THRESHOLD;

        int backgroundCount = 0;
        for (Component component : components) {
            if (component instanceof ImageComponent) {
                ImageComponent imageComp = (ImageComponent) component;
                int imageArea = component.getSize().getWidth() * component.getSize().getHeight();

                // 如果是大面积图片且imageType为background，设置为背景层
                if (imageArea > backgroundThreshold || "background".equals(imageComp.getImageType())) {
                    component.setLayer(LAYER_BACKGROUND);
                    log.debug("检测到背景图: id={}, 面积={} (页面面积: {})",
                            component.getId(), imageArea, pageArea);
                    backgroundCount++;
                }
            }
        }

        if (backgroundCount > 0) {
            log.info("检测到 {} 个背景图，已标记为 layer 0", backgroundCount);
        }
    }

    private void assignLayers(List<Component> components) {
        // 按面积从小到大排序
        components.sort(Comparator.comparingInt(c -> c.getSize().getWidth() * c.getSize().getHeight()));

        for (int i = 0; i < components.size(); i++) {
            Component component = components.get(i);

            // 如果AI已经分配了layer（且不为默认值1），则优先使用AI的layer
            if (component.getLayer() != null && component.getLayer() != LAYER_DEFAULT) {
                log.debug("组件 {} 使用AI分配的layer: {}", component.getId(), component.getLayer());
                continue;
            }

            // 否则，自动计算layer
            int layer = calculateLayer(component, components.subList(0, i));
            component.setLayer(layer);
        }
    }

    private int calculateLayer(Component component, List<Component> previousComponents) {
        int maxLayer = 1;

        for (Component prev : previousComponents) {
            if (isOverlapping(component, prev)) {
                maxLayer = Math.max(maxLayer, prev.getLayer() + 1);
            }
        }

        return maxLayer;
    }

    private boolean isOverlapping(Component c1, Component c2) {
        int x1 = c1.getPosition().getX();
        int y1 = c1.getPosition().getY();
        int x2 = x1 + c1.getSize().getWidth();
        int y2 = y1 + c1.getSize().getHeight();

        int x3 = c2.getPosition().getX();
        int y3 = c2.getPosition().getY();
        int x4 = x3 + c2.getSize().getWidth();
        int y4 = y3 + c2.getSize().getHeight();

        return !(x2 < x3 || x4 < x1 || y2 < y3 || y4 < y1);
    }

    private void identifySections(PageTemplate template) {
        List<Component> components = template.getComponents();
        int pageHeight = template.getPage().getHeight();

        List<Component> headerComponents = components.stream()
                .filter(c -> c.getPosition().getY() < pageHeight / 3)
                .collect(Collectors.toList());

        List<Component> contentComponents = components.stream()
                .filter(c -> c.getPosition().getY() >= pageHeight / 3 && c.getPosition().getY() < pageHeight * 2 / 3)
                .collect(Collectors.toList());

        List<Component> footerComponents = components.stream()
                .filter(c -> c.getPosition().getY() >= pageHeight * 2 / 3)
                .collect(Collectors.toList());

        if (!headerComponents.isEmpty()) {
            template.addSection("header", headerComponents.stream().map(Component::getId).collect(Collectors.toList()));
        }

        if (!contentComponents.isEmpty()) {
            template.addSection("content", contentComponents.stream().map(Component::getId).collect(Collectors.toList()));
        }

        if (!footerComponents.isEmpty()) {
            template.addSection("footer", footerComponents.stream().map(Component::getId).collect(Collectors.toList()));
        }
    }
}
