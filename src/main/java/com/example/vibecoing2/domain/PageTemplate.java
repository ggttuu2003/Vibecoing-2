package com.example.vibecoing2.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class PageTemplate {
    private String version = "1.0";
    private PageInfo page;
    private List<Component> components;
    private LayoutInfo layout;

    public PageTemplate() {
        this.components = new ArrayList<>();
        this.layout = new LayoutInfo();
    }

    public PageTemplate(Integer width, Integer height) {
        this.page = new PageInfo(width, height);
        this.components = new ArrayList<>();
        this.layout = new LayoutInfo();
    }

    @Data
    public static class PageInfo {
        private Integer width;
        private Integer height;
        private String backgroundColor = "#FFFFFF";
        private String backgroundImage; // Base64编码的背景图片
        private Map<String, Object> metadata;

        public PageInfo() {
        }

        public PageInfo(Integer width, Integer height) {
            this.width = width;
            this.height = height;
            this.metadata = new HashMap<>();
            this.metadata.put("designTool", "AI Generated");
            this.metadata.put("timestamp", Instant.now().toString());
        }
    }

    @Data
    @NoArgsConstructor
    public static class LayoutInfo {
        private String type = "flow";
        private String direction = "vertical";
        private Integer gap = 20;
        private List<Section> sections = new ArrayList<>();
    }

    @Data
    public static class Section {
        private String name;
        private List<String> components = new ArrayList<>();

        public Section() {
        }

        public Section(String name) {
            this.name = name;
        }
    }

    public void addComponent(Component component) {
        this.components.add(component);
    }

    public void addSection(String name, List<String> componentIds) {
        PageTemplate.Section section = new PageTemplate.Section(name);
        section.setComponents(componentIds);
        this.layout.getSections().add(section);
    }
}
