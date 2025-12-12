# 日志输出示例

## 📋 完整的日志输出示例

当您调用 API 分析图片时，会看到如下详细的日志输出：

```log
==========================================
开始使用 AI 分析图片
图片路径: /path/to/image.png
[1/3] 开始编码图片为 Base64...
[1/3] 图片编码完成，Base64 长度: 123456 字符
[2/3] 开始调用 AI API...

========== AI API 请求开始 ==========
请求 URL: https://zenmux.ai/api/v1/chat/completions
请求模型: google/gemini-2.5-flash
请求参数: max_tokens=4096, temperature=0.4, top_p=1.0
请求体预览: {"model":"google/gemini-2.5-flash","messages":[{"role":"user","content":[{"type":"text","text":"你是一个专业的 UI 设计分析师..."}]}],"max_tokens":4096,"temperature":0.4,"top_p":1.0}... (已截断，总长度: 150000)
图片 Base64 长度: 123456 字符
发送请求中...
收到响应: HTTP 200
响应体预览: {"id":"chatcmpl-abc123","object":"chat.completion","created":1234567890,"model":"google/gemini-2.5-flash","choices":[{"index":0,"message":{"role":"assistant","content":"```json\n{\n  \"components\": [\n    {\n      \"type\": \"text\",\n      \"content\": \"欢迎来到活动页面\",\n      \"position\": {\"x\": 100, \"y\": 50},\n      \"size\": {\"width\": 300, \"height\": 40}... (已截断，总长度: 5678)
开始解析 API 响应...
原始内容长度: 5000 字符
清理后内容长度: 4950 字符
清理后内容预览: {
  "components": [
    {
      "type": "text",
      "content": "欢迎来到活动页面",
      "position": {"x": 100, "y": 50},
      "size": {"width": 300, "height": 40},
      "style": {
        "fontSize": 24...
========== AI API 请求完成 ==========

[2/3] AI API 调用完成
[3/3] 开始解析 AI 响应...
开始解析组件数据...
检测到 15 个组件待解析
解析第 1 个组件，类型: text
解析第 2 个组件，类型: button
解析第 3 个组件，类型: image
解析第 4 个组件，类型: text
...
组件解析统计: 文本=8, 按钮=4, 图片=3, 其他=0
[3/3] 响应解析完成

==========================================
AI 分析完成！
识别到组件数量: 15
总耗时: 3456 ms (3.456 秒)
==========================================
```

---

## 🔍 日志说明

### 1. **分析流程日志**
```log
==========================================
开始使用 AI 分析图片
图片路径: /path/to/image.png
```
- 显示分析开始和图片路径

---

### 2. **图片编码日志**
```log
[1/3] 开始编码图片为 Base64...
[1/3] 图片编码完成，Base64 长度: 123456 字符
```
- 显示编码进度和 Base64 字符串长度

---

### 3. **API 请求日志**
```log
========== AI API 请求开始 ==========
请求 URL: https://zenmux.ai/api/v1/chat/completions
请求模型: google/gemini-2.5-flash
请求参数: max_tokens=4096, temperature=0.4, top_p=1.0
请求体预览: {...} (已截断，总长度: 150000)
图片 Base64 长度: 123456 字符
发送请求中...
```
- 显示完整的请求信息
- 请求体会自动截断（只显示前 500 字符）
- 显示图片 Base64 长度

---

### 4. **API 响应日志**
```log
收到响应: HTTP 200
响应体预览: {...} (已截断，总长度: 5678)
开始解析 API 响应...
原始内容长度: 5000 字符
清理后内容长度: 4950 字符
清理后内容预览: {...}
========== AI API 请求完成 ==========
```
- 显示 HTTP 状态码
- 响应体预览（自动截断，只显示前 1000 字符）
- 显示内容清理前后的长度

---

### 5. **组件解析日志**
```log
开始解析组件数据...
检测到 15 个组件待解析
解析第 1 个组件，类型: text
解析第 2 个组件，类型: button
...
组件解析统计: 文本=8, 按钮=4, 图片=3, 其他=0
```
- 显示检测到的组件数量
- 逐个显示组件解析过程（DEBUG 级别）
- 统计各类型组件数量

---

### 6. **完成统计日志**
```log
==========================================
AI 分析完成！
识别到组件数量: 15
总耗时: 3456 ms (3.456 秒)
==========================================
```
- 显示总共识别的组件数量
- 显示总耗时（毫秒和秒）

---

## ❌ 错误日志示例

### API 调用失败
```log
========== API 调用失败 ==========
错误状态码: 401
错误响应体: {"error":{"message":"Invalid API key","type":"invalid_request_error"}}
====================================
```

### 响应格式错误
```log
API 响应格式错误，无法找到 choices 或 message 节点
```

### 组件解析失败
```log
解析组件失败: type=unknown
```

---

## 🎛 日志级别控制

在 `application.properties` 中可以调整日志级别：

```properties
# 显示所有日志（包括 DEBUG）
logging.level.com.example.vibecoing2=DEBUG

# 只显示重要日志（INFO 及以上）
logging.level.com.example.vibecoing2=INFO

# 只显示警告和错误
logging.level.com.example.vibecoing2=WARN
```

### 各级别显示的日志：

| 级别 | 显示内容 |
|------|---------|
| **DEBUG** | 所有日志，包括每个组件的解析详情 |
| **INFO** | 主要流程日志、请求/响应信息、统计数据 |
| **WARN** | 警告信息（如组件解析失败） |
| **ERROR** | 错误信息（如 API 调用失败） |

---

## 💡 日志使用建议

### 开发调试时
```properties
logging.level.com.example.vibecoing2=DEBUG
```
- 查看所有细节
- 定位具体问题

### 生产环境
```properties
logging.level.com.example.vibecoing2=INFO
```
- 保留关键信息
- 减少日志量
- 便于追踪问题

### 性能优化
```properties
logging.level.com.example.vibecoing2=WARN
```
- 只记录异常情况
- 最小化性能影响

---

## 📊 日志分析要点

### 1. 性能分析
查看 `总耗时` 日志，了解各阶段耗时：
- 图片编码：通常很快（< 100ms）
- API 调用：主要耗时（2-10秒）
- 组件解析：通常很快（< 100ms）

### 2. 错误排查
- 检查 `错误状态码` 是否为 401（API Key 错误）或 500（服务器错误）
- 查看 `错误响应体` 了解具体错误原因
- 检查 `请求体预览` 确认请求格式正确

### 3. 质量监控
- 查看 `组件解析统计` 了解识别效果
- 对比不同图片的识别结果
- 调整 prompt 优化识别准确度

---

## 🔧 自定义日志格式

如需更详细的日志，可以在代码中调整：

### 显示完整请求体
```java
// VisionAIService.java 第 129 行
log.info("完整请求体: {}", requestBody);  // 移除截断
```

### 显示完整响应体
```java
// VisionAIService.java 第 159 行
log.info("完整响应体: {}", responseBody);  // 移除截断
```

⚠️ **注意**：显示完整内容会导致日志非常冗长，建议仅在调试时使用。
