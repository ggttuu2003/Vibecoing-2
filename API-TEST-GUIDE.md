# Vibecoing API 测试指南

## 📝 重构说明

已完成 AI 调用代码的重构，统一使用 OpenAI 兼容 API 格式。

### 主要变更：
1. ✅ 统一 API 格式 - 使用 zenmux.ai 的 `/chat/completions` 端点
2. ✅ 支持多模态 - 灵活组合文本和图片内容
3. ✅ 提高可扩展性 - 便于添加新模型和功能
4. ✅ 优化代码结构 - 常量定义、方法抽象、职责分离

---

## 🚀 快速开始

### 1. 导入 Postman Collection

1. 打开 Postman
2. 点击 **Import** 按钮
3. 选择 `Vibecoing-API.postman_collection.json` 文件
4. 导入后会看到 4 个测试接口

### 2. 准备测试图片

准备一张 UI 设计稿图片（支持格式：jpg, jpeg, png, bmp），大小不超过 10MB。

### 3. 测试接口

#### 接口地址
```
POST http://localhost:8080/api/analyze
```

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| image | File | 是 | - | 图片文件 |
| enableAI | Boolean | 否 | true | 是否启用 AI 分析 |
| enableOCR | Boolean | 否 | true | 是否启用 OCR 识别 |
| enableCV | Boolean | 否 | true | 是否启用计算机视觉 |

#### 响应示例

```json
{
  "code": 200,
  "message": "分析成功",
  "data": {
    "success": true,
    "message": "分析成功",
    "components": [
      {
        "id": "text-ai-1234567890",
        "type": "text",
        "content": "欢迎来到活动页面",
        "position": {"x": 100, "y": 50},
        "size": {"width": 300, "height": 40},
        "style": {
          "fontSize": 24,
          "fontWeight": 700,
          "color": "#333333",
          "textAlign": "center"
        }
      },
      {
        "id": "button-ai-1234567891",
        "type": "button",
        "text": "立即参与",
        "position": {"x": 150, "y": 400},
        "size": {"width": 200, "height": 50},
        "style": {
          "backgroundColor": "#FF6B6B",
          "textColor": "#FFFFFF",
          "fontSize": 16,
          "borderRadius": 25
        }
      }
    ]
  }
}
```

---

## 🔧 配置说明

### AI 配置（application.properties）

```properties
# AI Configuration
app.ai.provider=gemini
app.ai.api-key=sk-ai-v1-04a5921ea316f19aa4d44d7c6ef2bf34ef02a3cb85fa117c6a88f13254149b51
app.ai.base-url=https://zenmux.ai/api/v1
app.ai.model=google/gemini-2.5-flash
app.ai.timeout=60000
app.ai.max-tokens=4096
app.ai.temperature=0.4
app.ai.top-p=1.0
```

### 支持的模型

通过修改 `app.ai.model` 可切换不同模型：

- `google/gemini-2.5-flash` - Gemini 2.5 Flash（推荐）
- `google/gemini-2.5-pro` - Gemini 2.5 Pro（更强大）
- `anthropic/claude-3-5-sonnet` - Claude 3.5 Sonnet
- 其他 zenmux.ai 支持的模型

---

## 📊 代码架构

### VisionAIService.java 核心方法

```java
// 统一 API 调用入口
callUnifiedAPI(client, base64Image)

// 构建多模态请求体
buildMultimodalRequestBody(base64Image)

// 构建多模态内容（可扩展）
buildMultimodalContent(base64Image)

// 解析统一响应格式
parseUnifiedAPIResponse(responseBody)
```

### 可扩展性示例

**添加更多内容类型**（如 URL、文件等）：

```java
private List<Object> buildMultimodalContent(String base64Image, String imageUrl) {
    List<Object> contentList = new ArrayList<>();

    // 文本
    contentList.add(Map.of("type", "text", "text", ANALYSIS_PROMPT));

    // Base64 图片
    if (base64Image != null) {
        contentList.add(Map.of(
            "type", "image_url",
            "image_url", Map.of("url", "data:image/png;base64," + base64Image)
        ));
    }

    // URL 图片
    if (imageUrl != null) {
        contentList.add(Map.of(
            "type", "image_url",
            "image_url", Map.of("url", imageUrl)
        ));
    }

    return contentList;
}
```

---

## 🐛 常见问题

### 1. API 调用失败

**问题**：`API 调用失败: 401`

**解决**：检查 `app.ai.api-key` 是否正确

---

### 2. 响应格式错误

**问题**：`API 响应格式错误`

**解决**：
1. 检查模型名称是否正确
2. 查看日志中的完整响应内容
3. 确认 zenmux.ai 服务正常

---

### 3. 图片上传失败

**问题**：`文件大小不能超过 10MB`

**解决**：压缩图片或调整配置：
```properties
spring.servlet.multipart.max-file-size=20MB
spring.servlet.multipart.max-request-size=20MB
```

---

## 📞 技术支持

如有问题，请检查：
1. 服务是否启动：`http://localhost:8080/api/health`
2. 日志输出：查看控制台日志
3. API Key 是否有效
4. 网络连接是否正常

---

## 🎉 下一步

1. ✅ 启动应用：`./mvnw spring-boot:run`
2. ✅ 使用 Postman 测试接口
3. ✅ 根据实际需求调整参数
4. ✅ 尝试不同的模型和配置
