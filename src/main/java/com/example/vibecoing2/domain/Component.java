package com.example.vibecoing2.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TextComponent.class, name = "text"),
    @JsonSubTypes.Type(value = ButtonComponent.class, name = "button"),
    @JsonSubTypes.Type(value = ImageComponent.class, name = "image")
})
public abstract class Component {
    private String id;
    private String type;
    private Position position;
    private Size size;
    private Integer layer;
    private Double confidence;

    // HTML 标签类型 (如: h1, h2, p, div, button, img)
    private String htmlTag;

    // 完整的 CSS 样式映射
    private Map<String, String> cssStyles;

    protected Component(String type) {
        this.type = type;
        this.layer = 1;
        this.confidence = 1.0;
        this.cssStyles = new HashMap<>();
    }
}
