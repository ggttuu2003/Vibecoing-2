package com.example.vibecoing2.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class TextComponent extends Component {
    private String content;
    private Map<String, Object> style;

    public TextComponent() {
        super("text");
        this.style = new HashMap<>();
    }

    public void setFontSize(Integer fontSize) {
        this.style.put("fontSize", fontSize);
    }

    public void setFontWeight(Integer fontWeight) {
        this.style.put("fontWeight", fontWeight);
    }

    public void setFontFamily(String fontFamily) {
        this.style.put("fontFamily", fontFamily);
    }

    public void setColor(String color) {
        this.style.put("color", color);
    }

    public void setLineHeight(Double lineHeight) {
        this.style.put("lineHeight", lineHeight);
    }

    public void setTextAlign(String textAlign) {
        this.style.put("textAlign", textAlign);
    }

    public void setTextShadow(String textShadow) {
        this.style.put("textShadow", textShadow);
    }
}
