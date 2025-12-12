package com.example.vibecoing2.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class ImageComponent extends Component {
    private static final String DEFAULT_IMAGE_TYPE = "content";
    private static final String IMAGE_TYPE_BACKGROUND = "background";
    private static final String IMAGE_TYPE_DECORATION = "decoration";
    private static final String IMAGE_TYPE_CONTENT = "content";

    private Map<String, Object> style;
    private Map<String, String> placeholder;
    private String imageType;

    public ImageComponent() {
        super("image");
        this.style = new HashMap<>();
        this.placeholder = new HashMap<>();
        this.imageType = DEFAULT_IMAGE_TYPE;
    }

    public void setBorderRadius(Integer borderRadius) {
        this.style.put("borderRadius", borderRadius);
    }

    public void setObjectFit(String objectFit) {
        this.style.put("objectFit", objectFit);
    }

    public void setFilter(String filter) {
        this.style.put("filter", filter);
    }

    public void setPlaceholderUrl(String url) {
        this.placeholder.put("url", url);
    }

    public void setPlaceholderAlt(String alt) {
        this.placeholder.put("alt", alt);
    }

    public void setDominantColor(String color) {
        this.placeholder.put("dominantColor", color);
    }
}
