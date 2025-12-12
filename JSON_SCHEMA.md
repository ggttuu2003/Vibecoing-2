# JSON Schema 规范

## 概述

本文档定义了 AI 设计稿解析系统输出的 JSON 模板格式规范。

## 完整示例

```json
{
  "version": "1.0",
  "page": {
    "width": 750,
    "height": 1334,
    "backgroundColor": "#FFFFFF",
    "metadata": {
      "designTool": "AI Generated",
      "timestamp": "2025-12-11T10:00:00Z"
    }
  },
  "components": [
    {
      "id": "text-1",
      "type": "text",
      "content": "欢迎参加活动",
      "position": {
        "x": 20,
        "y": 100,
        "unit": "px",
        "alignment": "center"
      },
      "size": {
        "width": 335,
        "height": 40
      },
      "style": {
        "fontSize": 28,
        "fontWeight": 700,
        "fontFamily": "PingFang SC",
        "color": "#333333",
        "lineHeight": 1.5,
        "textAlign": "center",
        "textShadow": "0 2px 4px rgba(0,0,0,0.1)"
      },
      "layer": 2,
      "confidence": 0.95
    }
  ],
  "layout": {
    "type": "flow",
    "direction": "vertical",
    "gap": 20,
    "sections": []
  }
}
```

## 字段说明

### 1. 根对象

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| version | String | 是 | JSON Schema 版本号 |
| page | Object | 是 | 页面信息 |
| components | Array | 是 | 组件数组 |
| layout | Object | 是 | 布局信息 |

### 2. page 对象

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| width | Number | 是 | 页面宽度（px） |
| height | Number | 是 | 页面高度（px） |
| backgroundColor | String | 否 | 背景颜色，默认 "#FFFFFF" |
| metadata | Object | 否 | 元数据 |

### 3. components 数组

每个组件对象包含以下字段：

#### 3.1 通用字段

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | String | 是 | 组件唯一标识 |
| type | String | 是 | 组件类型：text/button/image |
| position | Object | 是 | 位置信息 |
| size | Object | 是 | 尺寸信息 |
| layer | Number | 否 | 层级（z-index），默认 1 |
| confidence | Number | 否 | 识别置信度 (0-1) |

#### 3.2 position 对象

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| x | Number | 是 | X 坐标 |
| y | Number | 是 | Y 坐标 |
| unit | String | 否 | 单位，默认 "px" |
| alignment | String | 否 | 对齐方式：left/center/right |

#### 3.3 size 对象

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| width | Number | 是 | 宽度 |
| height | Number | 是 | 高度 |

### 4. 组件类型详解

#### 4.1 TextComponent (type: "text")

```json
{
  "id": "text-1",
  "type": "text",
  "content": "文字内容",
  "position": {...},
  "size": {...},
  "style": {
    "fontSize": 16,
    "fontWeight": 400,
    "fontFamily": "PingFang SC",
    "color": "#000000",
    "lineHeight": 1.5,
    "textAlign": "left",
    "textShadow": "none"
  },
  "layer": 1,
  "confidence": 0.95
}
```

**style 字段说明：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| fontSize | Number | 否 | 字体大小（px） |
| fontWeight | Number | 否 | 字体粗细：400/700 |
| fontFamily | String | 否 | 字体族 |
| color | String | 否 | 文字颜色（十六进制） |
| lineHeight | Number | 否 | 行高倍数 |
| textAlign | String | 否 | 对齐：left/center/right |
| textShadow | String | 否 | 文字阴影（CSS 格式） |

#### 4.2 ButtonComponent (type: "button")

```json
{
  "id": "button-1",
  "type": "button",
  "text": "按钮文字",
  "position": {...},
  "size": {...},
  "style": {
    "backgroundColor": "#FF6B6B",
    "textColor": "#FFFFFF",
    "fontSize": 16,
    "fontWeight": 600,
    "borderRadius": 25,
    "border": "none",
    "boxShadow": "0 4px 12px rgba(255,107,107,0.4)",
    "padding": {
      "top": 12,
      "right": 40,
      "bottom": 12,
      "left": 40
    },
    "hover": {
      "backgroundColor": "#FF5252",
      "transform": "scale(1.05)"
    }
  },
  "interaction": {
    "onClick": "handleClick",
    "haptic": true
  },
  "layer": 2,
  "confidence": 0.90
}
```

**style 字段说明：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| backgroundColor | String | 否 | 背景颜色 |
| textColor | String | 否 | 文字颜色 |
| fontSize | Number | 否 | 字体大小 |
| fontWeight | Number | 否 | 字体粗细 |
| borderRadius | Number | 否 | 圆角半径（px） |
| border | String | 否 | 边框样式（CSS 格式） |
| boxShadow | String | 否 | 盒子阴影（CSS 格式） |
| padding | Object | 否 | 内边距 |
| hover | Object | 否 | 悬停样式 |

**interaction 字段说明：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| onClick | String | 否 | 点击事件处理函数名 |
| haptic | Boolean | 否 | 是否启用触感反馈 |

#### 4.3 ImageComponent (type: "image")

```json
{
  "id": "image-1",
  "type": "image",
  "position": {...},
  "size": {...},
  "style": {
    "borderRadius": 12,
    "objectFit": "cover",
    "filter": "none"
  },
  "placeholder": {
    "url": "https://placehold.co/300x200",
    "alt": "图片描述",
    "dominantColor": "#E8F5E9"
  },
  "layer": 1,
  "confidence": 0.85
}
```

**style 字段说明：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| borderRadius | Number | 否 | 圆角半径 |
| objectFit | String | 否 | 填充方式：cover/contain/fill |
| filter | String | 否 | 滤镜效果（CSS 格式） |

**placeholder 字段说明：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| url | String | 是 | 占位符图片 URL |
| alt | String | 否 | 图片描述 |
| dominantColor | String | 否 | 主色调（用于预加载） |

### 5. layout 对象

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | String | 是 | 布局类型：flow/grid/absolute |
| direction | String | 否 | 流向：vertical/horizontal |
| gap | Number | 否 | 间距（px） |
| sections | Array | 否 | 分区数组 |

**sections 数组元素：**

```json
{
  "name": "header",
  "components": ["text-1", "image-1"]
}
```

## 颜色格式规范

所有颜色值必须使用以下格式之一：

1. **十六进制**：`#RRGGBB` 或 `#RGB`
   - 示例：`#FF6B6B`、`#F00`

2. **RGBA**：`rgba(r, g, b, a)`
   - 示例：`rgba(255, 107, 107, 0.4)`

## 坐标系统

- **原点**：左上角 (0, 0)
- **X 轴**：从左到右递增
- **Y 轴**：从上到下递增
- **单位**：默认 px（像素）

## 层级规则

- **layer** 字段对应 CSS 的 `z-index`
- 值越大，层级越高
- 默认值为 1
- 重叠组件的 layer 值应不同

## 置信度说明

- **confidence** 字段表示识别置信度
- 取值范围：0.0 ~ 1.0
- 0.0 表示完全不确定，1.0 表示完全确定
- 建议阈值：0.7

## 扩展性

### 自定义组件类型

可以扩展新的组件类型，只需遵循以下规范：

```json
{
  "id": "custom-1",
  "type": "custom-type",
  "position": {...},
  "size": {...},
  "customField1": "value1",
  "customField2": "value2"
}
```

### 自定义样式属性

可以在 `style` 对象中添加任意 CSS 属性：

```json
{
  "style": {
    "customProperty": "customValue",
    "animation": "fadeIn 0.3s ease-in"
  }
}
```

## 验证规则

### 必填字段验证

- `version`、`page`、`components` 必须存在
- 每个组件必须有 `id`、`type`、`position`、`size`

### 数值范围验证

- `width` > 0
- `height` > 0
- `layer` >= 1
- `confidence` 在 [0, 1] 之间

### 颜色格式验证

- 十六进制颜色必须匹配正则：`^#[0-9A-Fa-f]{3}$|^#[0-9A-Fa-f]{6}$`
- RGBA 颜色必须匹配正则：`^rgba\(\d+,\s*\d+,\s*\d+,\s*[\d.]+\)$`

## 示例集合

### 示例1：简单文字页面

```json
{
  "version": "1.0",
  "page": {
    "width": 750,
    "height": 1334,
    "backgroundColor": "#FFFFFF"
  },
  "components": [
    {
      "id": "title",
      "type": "text",
      "content": "欢迎使用",
      "position": {"x": 0, "y": 100, "alignment": "center"},
      "size": {"width": 750, "height": 60},
      "style": {
        "fontSize": 32,
        "fontWeight": 700,
        "color": "#333333",
        "textAlign": "center"
      }
    }
  ],
  "layout": {
    "type": "flow",
    "direction": "vertical"
  }
}
```

### 示例2：带按钮的页面

```json
{
  "version": "1.0",
  "page": {
    "width": 750,
    "height": 1334
  },
  "components": [
    {
      "id": "cta-button",
      "type": "button",
      "text": "立即体验",
      "position": {"x": 275, "y": 800},
      "size": {"width": 200, "height": 50},
      "style": {
        "backgroundColor": "#007AFF",
        "textColor": "#FFFFFF",
        "borderRadius": 25
      },
      "interaction": {
        "onClick": "handleCTA"
      }
    }
  ],
  "layout": {
    "type": "flow",
    "direction": "vertical"
  }
}
```

## 常见问题

### Q1: 如何处理重叠组件？

A: 使用 `layer` 字段区分层级，值越大越靠前。

### Q2: 如何表示响应式布局？

A: 使用相对单位（%）或提供多个尺寸配置。

### Q3: 如何处理复杂样式（渐变、动画）？

A: 在 `style` 对象中使用 CSS 标准语法。

## 更新日志

- **v1.0** (2025-12-11)
  - 初始版本
  - 支持 text、button、image 三种组件类型
  - 支持基础样式和交互配置
