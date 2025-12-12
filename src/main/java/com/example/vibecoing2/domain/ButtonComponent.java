package com.example.vibecoing2.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class ButtonComponent extends Component {
    private String text;
    private Map<String, Object> style;
    private Map<String, Object> interaction;
    private String backgroundImage; // base64编码的背景图片

    public ButtonComponent() {
        super("button");
        this.style = new HashMap<>();
        this.interaction = new HashMap<>();
    }

    public void setBackgroundColor(String backgroundColor) {
        this.style.put("backgroundColor", backgroundColor);
    }

    public void setTextColor(String textColor) {
        this.style.put("textColor", textColor);
    }

    public void setFontSize(Integer fontSize) {
        this.style.put("fontSize", fontSize);
    }

    public void setFontWeight(Integer fontWeight) {
        this.style.put("fontWeight", fontWeight);
    }

    public void setBorderRadius(Integer borderRadius) {
        this.style.put("borderRadius", borderRadius);
    }

    public void setBorder(String border) {
        this.style.put("border", border);
    }

    public void setBoxShadow(String boxShadow) {
        this.style.put("boxShadow", boxShadow);
    }

    public void setPadding(Map<String, Integer> padding) {
        this.style.put("padding", padding);
    }

    public void setHoverStyle(Map<String, Object> hover) {
        this.style.put("hover", hover);
    }

    public void setOnClick(String onClick) {
        this.interaction.put("onClick", onClick);
    }

    public void setHaptic(Boolean haptic) {
        this.interaction.put("haptic", haptic);
    }
}
