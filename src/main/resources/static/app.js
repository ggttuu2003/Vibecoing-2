// 按钮组交互
function initButtonGroups() {
    const buttonGroups = document.querySelectorAll('.button-group');

    buttonGroups.forEach(group => {
        const buttons = group.querySelectorAll('.button-option');

        buttons.forEach(button => {
            button.addEventListener('click', () => {
                // 移除同组其他按钮的 active 类
                buttons.forEach(btn => btn.classList.remove('active'));
                // 添加当前按钮的 active 类
                button.classList.add('active');
            });
        });
    });
}

// 获取按钮组选中值
function getButtonGroupValue(groupId) {
    const group = document.getElementById(groupId);
    const activeButton = group.querySelector('.button-option.active');
    return activeButton ? activeButton.dataset.value : null;
}

// 初始化按钮组
initButtonGroups();

// ==================== 模板图片上传功能 ====================
let templateImageDataUrl = null; // 存储模板图片的 data URL

const uploadArea = document.getElementById('uploadArea');
const templateImageInput = document.getElementById('templateImageInput');
const uploadContent = document.getElementById('uploadContent');
const previewSection = document.getElementById('previewSection');
const previewImage = document.getElementById('previewImage');
const clearTemplateBtn = document.getElementById('clearTemplateBtn');

// 点击上传区域触发文件选择
uploadArea.addEventListener('click', () => {
    templateImageInput.click();
});

// 阻止上传区域的默认拖放行为
uploadArea.addEventListener('dragover', (e) => {
    e.preventDefault();
    uploadArea.style.borderColor = '#5DADE2';
});

uploadArea.addEventListener('dragleave', (e) => {
    e.preventDefault();
    uploadArea.style.borderColor = '#DFE4E8';
});

// 处理拖放上传
uploadArea.addEventListener('drop', (e) => {
    e.preventDefault();
    uploadArea.style.borderColor = '#DFE4E8';

    const files = e.dataTransfer.files;
    if (files.length > 0) {
        handleImageUpload(files[0]);
    }
});

// 处理文件选择
templateImageInput.addEventListener('change', (e) => {
    const files = e.target.files;
    if (files.length > 0) {
        handleImageUpload(files[0]);
    }
});

// 处理图片上传
function handleImageUpload(file) {
    // 验证文件类型
    if (!file.type.startsWith('image/')) {
        showToast('请上传图片文件', 'error');
        return;
    }

    // 验证文件大小（限制为 5MB）
    const maxSize = 5 * 1024 * 1024;
    if (file.size > maxSize) {
        showToast('图片大小不能超过 5MB', 'error');
        return;
    }

    // 读取文件为 data URL
    const reader = new FileReader();
    reader.onload = (e) => {
        templateImageDataUrl = e.target.result;

        // 显示预览
        previewImage.src = templateImageDataUrl;
        uploadContent.style.display = 'none';
        previewSection.style.display = 'block';

        showToast('模板图片上传成功', 'success');
    };
    reader.onerror = () => {
        showToast('图片读取失败', 'error');
    };
    reader.readAsDataURL(file);
}

// 清除模板图片
clearTemplateBtn.addEventListener('click', () => {
    templateImageDataUrl = null;
    templateImageInput.value = '';
    previewImage.src = '';
    uploadContent.style.display = 'block';
    previewSection.style.display = 'none';
    showToast('已清除模板图片', 'success');
});

// 关键词计数
const keywordsInput = document.getElementById('keywords');
const keywordCount = document.getElementById('keywordCount');

keywordsInput.addEventListener('input', () => {
    const keywords = keywordsInput.value.split(',').map(k => k.trim()).filter(k => k);
    keywordCount.textContent = `已输入 ${keywords.length} 个关键词`;

    if (keywords.length > 5) {
        keywordCount.style.color = '#f45c43';
    } else {
        keywordCount.style.color = '#999';
    }
});

// 表单提交
const form = document.getElementById('generateForm');
const generateBtn = document.getElementById('generateBtn');
const emptyState = document.getElementById('emptyState');
const loadingState = document.getElementById('loadingState');
const resultContent = document.getElementById('resultContent');
const imageGrid = document.getElementById('imageGrid');
const resultCount = document.getElementById('resultCount');
const resultTime = document.getElementById('resultTime');

form.addEventListener('submit', async (e) => {
    e.preventDefault();

    // 验证关键词数量
    const keywords = keywordsInput.value.split(',').map(k => k.trim()).filter(k => k);
    if (keywords.length === 0) {
        showToast('请输入至少一个关键词', 'error');
        return;
    }
    if (keywords.length > 5) {
        showToast('关键词数量不能超过5个', 'error');
        return;
    }

    // 收集表单数据
    const formData = {
        title: document.getElementById('title').value,
        subtitle: document.getElementById('subtitle').value,
        keywords: keywords,
        style: getButtonGroupValue('styleGroup'),
        model: getButtonGroupValue('modelGroup'),
        count: parseInt(getButtonGroupValue('countGroup'))
    };

    // 如果有模板图片，添加到请求中
    if (templateImageDataUrl) {
        formData.backgroundImage = templateImageDataUrl;
    }

    // 显示加载状态
    emptyState.style.display = 'none';
    resultContent.style.display = 'none';
    loadingState.style.display = 'block';
    generateBtn.disabled = true;
    generateBtn.innerHTML = '<span>生成中...</span>';

    try {
        const response = await fetch('/api/generate/image', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });

        const result = await response.json();

        if (result.code === 200) {
            showToast('✅ 图片生成成功！', 'success');
            displayResults(result.data);
        } else {
            showToast('❌ ' + result.message, 'error');
            loadingState.style.display = 'none';
            emptyState.style.display = 'block';
        }
    } catch (error) {
        console.error('生成失败:', error);
        showToast('❌ 生成失败: ' + error.message, 'error');
        loadingState.style.display = 'none';
        emptyState.style.display = 'block';
    } finally {
        generateBtn.disabled = false;
        generateBtn.innerHTML = '<span>🎨 生成图片</span>';
    }
});

// 显示结果
function displayResults(data) {
    loadingState.style.display = 'none';
    resultContent.style.display = 'block';

    // 显示元数据
    resultCount.textContent = `✅ 生成 ${data.metadata.count} 张`;
    resultTime.textContent = `⏱️ 耗时 ${data.metadata.generationTimeMs}ms`;

    // 显示图片
    imageGrid.innerHTML = '';
    data.images.forEach((image, index) => {
        const imageItem = document.createElement('div');
        imageItem.className = 'image-item';
        imageItem.innerHTML = `
            <img src="${image.base64}" alt="生成的图片 ${index + 1}">
            <div class="image-item-info">
                <span>图片 ${index + 1}</span>
                <span>${image.width}x${image.height}</span>
            </div>
            <button class="btn btn-success btn-small" onclick="downloadImage('${image.base64}', ${index})">
                📥 下载图片
            </button>
        `;
        imageGrid.appendChild(imageItem);
    });

    // 刷新历史记录（如果是新生成的结果）
    if (data.id || data.metadata) {
        loadHistory();
    }
}

// 下载图片
function downloadImage(imageUrl, index) {
    if (imageUrl.startsWith('data:')) {
        // Base64 图片直接下载
        const link = document.createElement('a');
        link.href = imageUrl;
        link.download = `generated_image_${index + 1}.png`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        showToast('图片已开始下载', 'success');
    } else {
        // URL 图片先获取再下载
        fetch(imageUrl)
            .then(response => response.blob())
            .then(blob => {
                const url = URL.createObjectURL(blob);
                const link = document.createElement('a');
                link.href = url;
                link.download = `generated_image_${index + 1}.png`;
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
                URL.revokeObjectURL(url);
                showToast('图片已开始下载', 'success');
            })
            .catch(error => {
                console.error('下载失败:', error);
                showToast('图片下载失败', 'error');
            });
    }
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
        const response = await fetch('/api/history/list?page=1&size=10');
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

// 渲染历史记录列表（纯图片网格）
function renderHistory(records) {
    const historyList = document.getElementById('historyList');

    // 只显示图片，每条记录取第一张图片
    historyList.innerHTML = `
        <div style="display: grid; grid-template-columns: 1fr; gap: 12px;">
            ${records.map(record => {
                // 获取第一张图片，如果没有图片则跳过
                const firstImage = record.images && record.images.length > 0 ? record.images[0] : null;
                if (!firstImage) return '';

                return `
                    <div style="position: relative; cursor: pointer; border-radius: 8px; overflow: hidden; border: 1px solid #E8ECEF; transition: all 0.2s;"
                         onclick="viewHistory('${record.id}')"
                         onmouseover="this.style.borderColor='#5DADE2'; this.style.boxShadow='0 4px 12px rgba(93, 173, 226, 0.15)';"
                         onmouseout="this.style.borderColor='#E8ECEF'; this.style.boxShadow='none';">
                        <img src="${firstImage.base64}"
                             alt="${record.title}"
                             style="width: 100%; display: block; object-fit: cover;">
                    </div>
                `;
            }).filter(html => html).join('')}
        </div>
    `;
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
        const response = await fetch(`/api/history/${historyId}`);
        const result = await response.json();

        if (result.code === 200) {
            displayResults(result.data);
            showToast('✅ 已加载历史记录', 'success');
        } else {
            showToast('❌ 加载失败', 'error');
        }
    } catch (error) {
        console.error('加载历史记录失败:', error);
        showToast('❌ 加载失败', 'error');
    }
}

// 下载历史记录中的所有图片
async function downloadHistoryImages(historyId) {
    try {
        const response = await fetch(`/api/history/${historyId}`);
        const result = await response.json();

        if (result.code === 200 && result.data.images) {
            for (let i = 0; i < result.data.images.length; i++) {
                const image = result.data.images[i];
                await new Promise(resolve => {
                    downloadImage(image.base64, i);
                    setTimeout(resolve, 300); // 延迟避免同时下载太多
                });
            }
            showToast('✅ 图片已开始下载', 'success');
        } else {
            showToast('❌ 下载失败', 'error');
        }
    } catch (error) {
        console.error('下载失败:', error);
        showToast('❌ 下载失败', 'error');
    }
}

// 删除历史记录
async function deleteHistory(historyId) {
    if (!confirm('确定要删除这条历史记录吗？')) {
        return;
    }

    try {
        const response = await fetch(`/api/history/${historyId}`, {
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
