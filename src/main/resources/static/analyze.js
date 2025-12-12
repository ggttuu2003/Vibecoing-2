// 元素引用
const uploadArea = document.getElementById('uploadArea');
const fileInput = document.getElementById('fileInput');
const previewSection = document.getElementById('previewSection');
const preview = document.getElementById('preview');
const analyzeBtn = document.getElementById('analyzeBtn');
const emptyState = document.getElementById('emptyState');
const loadingState = document.getElementById('loadingState');
const resultContent = document.getElementById('resultContent');

let currentFile = null;
let currentResult = null;

// 上传区域点击
uploadArea.addEventListener('click', () => {
    fileInput.click();
});

// 拖拽上传
uploadArea.addEventListener('dragover', (e) => {
    e.preventDefault();
    uploadArea.style.borderColor = '#667eea';
    uploadArea.style.background = 'rgba(102, 126, 234, 0.05)';
});

uploadArea.addEventListener('dragleave', () => {
    uploadArea.style.borderColor = '#e0e0e0';
    uploadArea.style.background = 'white';
});

uploadArea.addEventListener('drop', (e) => {
    e.preventDefault();
    uploadArea.style.borderColor = '#e0e0e0';
    uploadArea.style.background = 'white';

    const files = e.dataTransfer.files;
    if (files.length > 0) {
        handleFile(files[0]);
    }
});

// 文件选择
fileInput.addEventListener('change', (e) => {
    if (e.target.files.length > 0) {
        handleFile(e.target.files[0]);
    }
});

// 处理文件
function handleFile(file) {
    // 验证文件类型
    if (!file.type.startsWith('image/')) {
        showToast('请上传图片文件', 'error');
        return;
    }

    // 验证文件大小（10MB）
    if (file.size > 10 * 1024 * 1024) {
        showToast('文件大小不能超过 10MB', 'error');
        return;
    }

    currentFile = file;

    // 显示预览
    const reader = new FileReader();
    reader.onload = (e) => {
        preview.src = e.target.result;
        previewSection.style.display = 'block';
        uploadArea.style.display = 'none';
        analyzeBtn.disabled = false;
    };
    reader.readAsDataURL(file);
}

// 清除上传
function clearUpload() {
    currentFile = null;
    fileInput.value = '';
    previewSection.style.display = 'none';
    uploadArea.style.display = 'block';
    analyzeBtn.disabled = true;
}

// 开始解析
analyzeBtn.addEventListener('click', async () => {
    if (!currentFile) {
        showToast('请先上传图片', 'error');
        return;
    }

    // 收集配置
    const config = {
        enableAI: document.getElementById('enableAI').checked,
        enableOCR: document.getElementById('enableOCR').checked,
        enableCV: document.getElementById('enableCV').checked
    };

    // 显示加载状态
    emptyState.style.display = 'none';
    resultContent.style.display = 'none';
    loadingState.style.display = 'block';
    analyzeBtn.disabled = true;
    analyzeBtn.innerHTML = '<span>分析中...</span>';

    try {
        // 构建 FormData
        const formData = new FormData();
        formData.append('image', currentFile);  // 注意：参数名必须是 'image'
        formData.append('enableAI', config.enableAI);
        formData.append('enableOCR', config.enableOCR);
        formData.append('enableCV', config.enableCV);

        const response = await fetch('/api/analyze', {
            method: 'POST',
            body: formData
        });

        const result = await response.json();

        if (result.code === 200) {
            showToast('✅ 解析成功！', 'success');
            displayResult(result.data);
        } else {
            showToast('❌ ' + result.message, 'error');
            loadingState.style.display = 'none';
            emptyState.style.display = 'block';
        }
    } catch (error) {
        console.error('解析失败:', error);
        showToast('❌ 解析失败: ' + error.message, 'error');
        loadingState.style.display = 'none';
        emptyState.style.display = 'block';
    } finally {
        analyzeBtn.disabled = false;
        analyzeBtn.innerHTML = '<span>🔍 开始解析</span>';
    }
});

// 显示结果
function displayResult(data) {
    currentResult = data;

    loadingState.style.display = 'none';
    resultContent.style.display = 'block';

    // 显示统计信息（防御性检查）
    const metadata = data.metadata || {};
    document.getElementById('textCount').textContent = metadata.textCount || 0;
    document.getElementById('buttonCount').textContent = metadata.buttonCount || 0;
    document.getElementById('imageCount').textContent = metadata.imageCount || 0;
    document.getElementById('processTime').textContent = metadata.processingTimeMs || 0;

    // 显示 JSON
    const jsonPreview = document.getElementById('jsonPreview');
    jsonPreview.textContent = JSON.stringify(data.template, null, 2);

    // 自动预览 HTML
    previewHTML();
}

// 复制 JSON
function copyJSON() {
    if (!currentResult) {
        showToast('没有可复制的内容', 'error');
        return;
    }

    const jsonString = JSON.stringify(currentResult.template, null, 2);
    navigator.clipboard.writeText(jsonString).then(() => {
        showToast('✅ JSON 已复制到剪贴板', 'success');
    }).catch(err => {
        showToast('❌ 复制失败', 'error');
    });
}

// 预览 HTML
function previewHTML() {
    if (!currentResult) {
        showToast('没有可预览的内容', 'error');
        return;
    }

    const htmlContent = generateHTMLContent();
    const iframe = document.getElementById('htmlPreview');
    const iframeDoc = iframe.contentDocument || iframe.contentWindow.document;

    iframeDoc.open();
    iframeDoc.write(htmlContent);
    iframeDoc.close();
}

// 生成 HTML 内容（供预览和导出复用）
function generateHTMLContent() {
    const template = currentResult.template;
    const components = template.components || [];
    const pageWidth = template.page?.width || 750;
    const pageHeight = template.page?.height || 1334;

    // 按 layer 排序组件（从低到高，确保层次正确）
    const sortedComponents = [...components].sort((a, b) => {
        const layerA = a.layer || 1;
        const layerB = b.layer || 1;
        return layerA - layerB;
    });

    // 生成组件 HTML
    let componentsHTML = '';
    sortedComponents.forEach(comp => {
        componentsHTML += generateComponentHTML(comp);
    });

    // 生成完整的 HTML
    return `<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>设计稿还原 - ${new Date().toLocaleDateString()}</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Helvetica Neue', Arial, sans-serif;
            background: #F8F9FA;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            padding: 40px 20px;
        }

        .page-container {
            position: relative;
            width: ${pageWidth}px;
            height: ${pageHeight}px;
            background: #F8F9FA;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
            overflow: hidden;
        }

        /* 组件样式 */
        .component {
            position: absolute;
            box-sizing: border-box;
        }

        /* 图片组件 */
        .component img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }

        /* 按钮组件 */
        .component button {
            width: 100%;
            height: 100%;
            border: none;
            cursor: pointer;
            font-family: inherit;
        }

        /* 响应式适配 */
        @media (max-width: ${pageWidth}px) {
            .page-container {
                transform: scale(0.9);
                transform-origin: center;
            }
        }
    </style>
</head>
<body>
    <div class="page-container">
${componentsHTML}
    </div>
</body>
</html>`;
}

// 导出 HTML
function exportHTML() {
    if (!currentResult) {
        showToast('没有可导出的内容', 'error');
        return;
    }

    const html = generateHTMLContent();

    // 下载文件
    const blob = new Blob([html], { type: 'text/html' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `design-restore-${Date.now()}.html`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);

    showToast('✅ HTML 文件已导出', 'success');
}

/**
 * 生成单个组件的 HTML
 */
function generateComponentHTML(comp) {
    const htmlTag = comp.htmlTag || getDefaultTag(comp.type);

    // 合并 cssStyles 和 position/size 信息
    const mergedStyles = { ...(comp.cssStyles || {}) };

    // 从 position 字段提取位置信息
    if (comp.position) {
        if (!mergedStyles.position) {
            mergedStyles.position = 'absolute';
        }
        if (comp.position.x !== undefined && !mergedStyles.left) {
            mergedStyles.left = comp.position.x + 'px';
        }
        if (comp.position.y !== undefined && !mergedStyles.top) {
            mergedStyles.top = comp.position.y + 'px';
        }
    }

    // 从 size 字段提取尺寸信息
    if (comp.size) {
        if (comp.size.width !== undefined && !mergedStyles.width) {
            mergedStyles.width = comp.size.width + 'px';
        }
        if (comp.size.height !== undefined && !mergedStyles.height) {
            mergedStyles.height = comp.size.height + 'px';
        }
    }

    const styleString = generateStyleString(mergedStyles);

    let content = '';
    let attributes = `class="component" style="${styleString}"`;

    switch (comp.type) {
        case 'text':
            content = escapeHTML(comp.content || '');
            return `        <${htmlTag} ${attributes}>${content}</${htmlTag}>\n`;

        case 'button':
            content = escapeHTML(comp.text || comp.content || '按钮');
            return `        <${htmlTag} ${attributes}>${content}</${htmlTag}>\n`;

        case 'image':
            const imgSrc = comp.placeholderUrl || '';
            const imgAlt = comp.placeholderAlt || '图片';
            const hasImageType = comp.imageType && (comp.imageType === 'decoration' || comp.imageType === 'content');

            // 如果有 imageType，说明是真实图片组件
            if (hasImageType && htmlTag === 'img' && imgSrc) {
                return `        <${htmlTag} ${attributes} src="${imgSrc}" alt="${imgAlt}" />\n`;
            } else if (hasImageType && imgSrc) {
                // 使用 div + 背景图
                const bgStyle = `background-image: url('${imgSrc}'); background-size: cover; background-position: center;`;
                const fullStyle = styleString + '; ' + bgStyle;
                return `        <div ${attributes.replace(styleString, fullStyle)}></div>\n`;
            } else {
                // 纯色区块，直接使用 cssStyles 中的 backgroundColor
                return `        <div ${attributes}></div>\n`;
            }

        default:
            return `        <div ${attributes}></div>\n`;
    }
}

/**
 * 根据组件类型获取默认 HTML 标签
 */
function getDefaultTag(type) {
    switch (type) {
        case 'text': return 'p';
        case 'button': return 'button';
        case 'image': return 'img';
        default: return 'div';
    }
}

/**
 * 将 CSS 样式对象转换为样式字符串
 */
function generateStyleString(cssStyles) {
    if (!cssStyles || typeof cssStyles !== 'object') {
        return '';
    }

    return Object.entries(cssStyles)
        .map(([key, value]) => {
            // 将驼峰命名转换为 CSS 属性名（如 fontSize -> font-size）
            const cssKey = key.replace(/([A-Z])/g, '-$1').toLowerCase();
            return `${cssKey}: ${value}`;
        })
        .join('; ');
}

/**
 * HTML 转义
 */
function escapeHTML(str) {
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}

// Toast 提示
function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast ${type} show`;

    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}
