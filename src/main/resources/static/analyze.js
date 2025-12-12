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
let uploadedImageBase64 = null; // 保存上传图片的 base64，用于预览背景

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
        uploadedImageBase64 = e.target.result; // 保存 base64 用于预览背景
        preview.src = uploadedImageBase64;
        previewSection.style.display = 'block';
        uploadArea.style.display = 'none';
        analyzeBtn.disabled = false;
    };
    reader.readAsDataURL(file);
}

// 清除上传
function clearUpload() {
    currentFile = null;
    uploadedImageBase64 = null;
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

    // 刷新历史记录（如果是新解析的结果）
    if (data.id) {
        loadHistory();
    }
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

    // 计算缩放比例，使iframe内容完整显示在容器中（无滚动条）
    const template = currentResult.template;
    const pageWidth = template.page?.width || 750;
    const pageHeight = template.page?.height || 1334;

    // 容器尺寸（600px 高度）
    const containerHeight = 600;
    const containerWidth = iframe.parentElement.clientWidth;

    // 计算缩放比例（按高度和宽度取最小值，确保完整显示）
    const scaleByHeight = containerHeight / pageHeight;
    const scaleByWidth = containerWidth / pageWidth;
    const scale = Math.min(scaleByHeight, scaleByWidth, 1); // 最大不超过1

    // 设置iframe尺寸和缩放
    iframe.style.width = pageWidth + 'px';
    iframe.style.height = pageHeight + 'px';
    iframe.style.transform = `scale(${scale})`;

    // 居中显示
    const scaledWidth = pageWidth * scale;
    const scaledHeight = pageHeight * scale;
    iframe.style.left = ((containerWidth - scaledWidth) / 2) + 'px';
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

    // 背景图片样式（使用上传的原图）
    const backgroundStyle = uploadedImageBase64
        ? `background-image: url('${uploadedImageBase64}');
           background-size: cover;
           background-position: center;
           background-repeat: no-repeat;`
        : 'background: #F8F9FA;';

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
            ${backgroundStyle}
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

// ==================== 历史记录功能 ====================

// 页面加载时获取历史记录
window.addEventListener('DOMContentLoaded', () => {
    loadHistory();
});

// 加载历史记录
async function loadHistory() {
    const historyLoading = document.getElementById('historyLoading');
    const historyList = document.getElementById('historyList');
    const historyEmpty = document.getElementById('historyEmpty');

    historyLoading.style.display = 'block';
    historyList.style.display = 'none';
    historyEmpty.style.display = 'none';

    try {
        const response = await fetch('/api/history/analysis/list?page=1&size=10');
        const result = await response.json();

        if (result.code === 200 && result.data.records && result.data.records.length > 0) {
            renderHistory(result.data.records);
            historyList.style.display = 'block';
        } else {
            historyEmpty.style.display = 'block';
        }
    } catch (error) {
        console.error('加载历史记录失败:', error);
        historyEmpty.style.display = 'block';
    } finally {
        historyLoading.style.display = 'none';
    }
}

// 渲染历史记录列表
function renderHistory(records) {
    const historyList = document.getElementById('historyList');

    historyList.innerHTML = records.map((record, index) => `
        <div style="background: #F8F9FA; padding: 16px; border-radius: 8px; margin-bottom: 16px; border: 1px solid #E8ECEF;">
            <div style="display: flex; justify-content: space-between; align-items: start; margin-bottom: 12px;">
                <div>
                    <div style="font-weight: 600; color: #2C3E50; margin-bottom: 4px;">记录 #${records.length - index}</div>
                    <div style="font-size: 12px; color: #7F8C8D;">${formatDate(record.timestamp)}</div>
                </div>
                <button
                    class="btn btn-danger btn-small"
                    onclick="deleteHistory('${record.id}')"
                    style="padding: 6px 12px; font-size: 12px;"
                >
                    删除
                </button>
            </div>

            <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; margin-bottom: 12px; font-size: 12px;">
                <div style="text-align: center; padding: 8px; background: #E3F2FD; border-radius: 6px; color: #1976D2;">
                    <div style="font-weight: 600;">${record.textCount || 0}</div>
                    <div style="font-size: 11px; opacity: 0.8;">文字</div>
                </div>
                <div style="text-align: center; padding: 8px; background: #E8F5E9; border-radius: 6px; color: #388E3C;">
                    <div style="font-weight: 600;">${record.buttonCount || 0}</div>
                    <div style="font-size: 11px; opacity: 0.8;">按钮</div>
                </div>
                <div style="text-align: center; padding: 8px; background: #F3E5F5; border-radius: 6px; color: #7B1FA2;">
                    <div style="font-weight: 600;">${record.imageCount || 0}</div>
                    <div style="font-size: 11px; opacity: 0.8;">图片</div>
                </div>
            </div>

            <div style="display: flex; gap: 8px;">
                <button
                    class="btn btn-secondary btn-small"
                    onclick="viewHistory('${record.id}')"
                    style="flex: 1; padding: 8px; font-size: 12px;"
                >
                    查看
                </button>
                <button
                    class="btn btn-success btn-small"
                    onclick="exportHistoryHTML('${record.id}')"
                    style="flex: 1; padding: 8px; font-size: 12px;"
                >
                    导出
                </button>
            </div>
        </div>
    `).join('');
}

// 格式化日期
function formatDate(timestamp) {
    const date = new Date(timestamp);
    const now = new Date();
    const diff = now - date;

    if (diff < 60000) return '刚刚';
    if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
    if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';

    return date.toLocaleDateString('zh-CN', {
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// 查看历史记录
async function viewHistory(historyId) {
    try {
        const response = await fetch(`/api/history/analysis/${historyId}`);
        const result = await response.json();

        if (result.code === 200) {
            currentResult = result.data;
            displayResult(result.data);
            showToast('✅ 已加载历史记录', 'success');
        } else {
            showToast('❌ 加载失败', 'error');
        }
    } catch (error) {
        console.error('加载历史记录失败:', error);
        showToast('❌ 加载失败', 'error');
    }
}

// 导出历史记录的 HTML
async function exportHistoryHTML(historyId) {
    try {
        const response = await fetch(`/api/history/analysis/${historyId}`);
        const result = await response.json();

        if (result.code === 200) {
            const tempResult = currentResult;
            currentResult = result.data;
            exportHTML();
            currentResult = tempResult;
        } else {
            showToast('❌ 导出失败', 'error');
        }
    } catch (error) {
        console.error('导出失败:', error);
        showToast('❌ 导出失败', 'error');
    }
}

// 删除历史记录
async function deleteHistory(historyId) {
    if (!confirm('确定要删除这条历史记录吗？')) {
        return;
    }

    try {
        const response = await fetch(`/api/history/analysis/${historyId}`, {
            method: 'DELETE'
        });
        const result = await response.json();

        if (result.code === 200) {
            showToast('✅ 删除成功', 'success');
            loadHistory(); // 重新加载历史记录
        } else {
            showToast('❌ 删除失败: ' + result.message, 'error');
        }
    } catch (error) {
        console.error('删除失败:', error);
        showToast('❌ 删除失败', 'error');
    }
}
