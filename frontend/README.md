# 前端 Vue 3 项目

## 安装依赖

```bash
cd frontend
npm install
```

## 运行开发服务器

```bash
npm run dev
```

访问 http://localhost:5173

## 构建生产版本

```bash
npm run build
```

## 项目结构

```
frontend/
├── src/
│   ├── components/
│   │   ├── ImageUploader.vue      # 图片上传组件
│   │   ├── JsonPreview.vue        # JSON 预览组件
│   │   ├── PageRenderer.vue       # 页面渲染引擎
│   │   └── renderers/
│   │       ├── TextRenderer.vue   # 文字组件渲染器
│   │       ├── ButtonRenderer.vue # 按钮组件渲染器
│   │       └── ImageRenderer.vue  # 图片组件渲染器
│   ├── services/
│   │   └── api.js                 # API 服务
│   ├── App.vue                    # 主应用
│   ├── main.js                    # 入口文件
│   └── style.css                  # 全局样式
├── index.html                     # HTML 模板
├── package.json                   # 依赖配置
└── vite.config.js                 # Vite 配置
```
