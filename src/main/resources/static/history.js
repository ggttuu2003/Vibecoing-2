// 全局变量
let currentPage = 1;
let totalPages = 1;
let currentFilter = '';

// 页面加载时获取历史记录
window.addEventListener('DOMContentLoaded', () => {
    loadHistory();

    // 监听筛选变化
    document.getElementById('styleFilter').addEventListener('change', (e) => {
        currentFilter = e.target.value;
        currentPage = 1;
        loadHistory();
    });

    // 分页按钮
    document.getElementById('prevBtn').addEventListener('click', () => {
        if (currentPage > 1) {
            currentPage--;
            loadHistory();
        }
    });

    document.getElementById('nextBtn').addEventListener('click', () => {
        if (currentPage < totalPages) {
            currentPage++;
            loadHistory();
        }
    });
});

// 加载历史记录
async function loadHistory() {
    const loadingState = document.getElementById('loadingState');
    const emptyState = document.getElementById('emptyState');
    const historyList = document.getElementById('historyList');
    const pagination = document.getElementById('pagination');

    loadingState.style.display = 'block';
    historyList.innerHTML = '';
    emptyState.style.display = 'none';
    pagination.style.display = 'none';

    try {
        const params = new URLSearchParams({
            page: currentPage,
            size: 10
        });

        if (currentFilter) {
            params.append('style', currentFilter);
        }

        const response = await fetch(`/api/history/list?${params}`);
        const result = await response.json();

        if (result.code === 200) {
            const data = result.data;
            document.getElementById('totalCount').textContent = data.total;

            if (data.records.length === 0) {
                emptyState.style.display = 'block';
            } else {
                displayHistory(data.records);
                displayPagination(data);
            }
        } else {
            showToast('加载失败: ' + result.message, 'error');
            emptyState.style.display = 'block';
        }
    } catch (error) {
        console.error('加载失败:', error);
        showToast('加载失败: ' + error.message, 'error');
        emptyState.style.display = 'block';
    } finally {
        loadingState.style.display = 'none';
    }
}

// 显示历史记录
function displayHistory(records) {
    const historyList = document.getElementById('historyList');

    records.forEach(record => {
        const card = document.createElement('div');
        card.className = 'history-card';

        // 获取第一张图片URL
        const firstImage = record.imagePaths && record.imagePaths.length > 0
            ? getImageUrl(record.imagePaths[0])
            : '';

        // 关键词标签
        const tagsHtml = record.request.keywords && record.request.keywords.length > 0
            ? record.request.keywords.slice(0, 3).map(k => `<span class="tag">#${k}</span>`).join('')
            : '';

        const moreTag = record.request.keywords && record.request.keywords.length > 3
            ? `<span class="tag">+${record.request.keywords.length - 3}</span>`
            : '';

        card.innerHTML = `
            <div class="history-card-image">
                ${firstImage ? `<img src="${firstImage}" alt="${record.request.title}">` : ''}
                <div class="history-card-badge">${record.imageCount} 张</div>
            </div>
            <div class="history-card-info">
                <h3>${record.request.title}</h3>
                ${record.request.subtitle ? `<p>${record.request.subtitle}</p>` : ''}
                <div class="history-card-tags">
                    ${tagsHtml}
                    ${moreTag}
                </div>
                <div class="history-card-meta">
                    <span>⏰ ${formatTime(record.timestamp)}</span>
                    <span>🎨 ${getStyleName(record.style)}</span>
                    <span>⚡ ${record.metadata.generationTimeMs}ms</span>
                </div>
            </div>
            <div class="history-card-actions">
                <button class="btn btn-primary btn-small" onclick="viewDetail('${record.historyId}')">查看详情</button>
                <button class="btn btn-success btn-small" onclick="downloadAll('${record.historyId}')">下载全部</button>
                <button class="btn btn-danger btn-small" onclick="deleteRecord('${record.historyId}')">删除</button>
            </div>
        `;

        historyList.appendChild(card);
    });
}

// 显示分页
function displayPagination(data) {
    const pagination = document.getElementById('pagination');
    const pageInfo = document.getElementById('pageInfo');
    const prevBtn = document.getElementById('prevBtn');
    const nextBtn = document.getElementById('nextBtn');

    totalPages = data.totalPages;

    if (totalPages > 1) {
        pagination.style.display = 'flex';
        pageInfo.textContent = `第 ${currentPage} / ${totalPages} 页`;
        prevBtn.disabled = currentPage <= 1;
        nextBtn.disabled = currentPage >= totalPages;
    }
}

// 查看详情
async function viewDetail(historyId) {
    try {
        const response = await fetch(`/api/history/${historyId}`);
        const result = await response.json();

        if (result.code === 200) {
            showDetailModal(result.data);
        } else {
            showToast('加载详情失败', 'error');
        }
    } catch (error) {
        console.error('加载详情失败:', error);
        showToast('加载详情失败', 'error');
    }
}

// 显示详情弹窗
function showDetailModal(record) {
    const modal = document.getElementById('detailModal');
    const modalBody = document.getElementById('modalBody');

    const imagesHtml = record.imagePaths.map((path, index) => `
        <div class="modal-image-item">
            <img src="${getImageUrl(path)}" alt="图片 ${index + 1}">
            <button class="btn btn-success btn-small" style="margin: 12px;" onclick="downloadSingleImage('${path}', ${index})">
                📥 下载图片 ${index + 1}
            </button>
        </div>
    `).join('');

    modalBody.innerHTML = `
        <div class="modal-info">
            <h3>${record.request.title}</h3>
            ${record.request.subtitle ? `<p>${record.request.subtitle}</p>` : ''}
            <div style="display: flex; gap: 20px; font-size: 14px; color: #666;">
                <span>⏰ ${formatTime(record.timestamp)}</span>
                <span>🎨 ${getStyleName(record.style)}</span>
                <span>🤖 ${getModelShortName(record.model)}</span>
            </div>
        </div>
        <h4 style="margin-bottom: 16px; color: #333;">生成的图片（${record.imageCount} 张）</h4>
        <div class="modal-images">
            ${imagesHtml}
        </div>
    `;

    modal.classList.add('show');
}

// 关闭弹窗
function closeModal() {
    document.getElementById('detailModal').classList.remove('show');
}

// 点击弹窗外部关闭
document.getElementById('detailModal').addEventListener('click', (e) => {
    if (e.target.id === 'detailModal') {
        closeModal();
    }
});

// 下载全部
async function downloadAll(historyId) {
    try {
        const response = await fetch(`/api/history/${historyId}`);
        const result = await response.json();

        if (result.code === 200) {
            const record = result.data;
            for (let i = 0; i < record.imagePaths.length; i++) {
                await downloadSingleImage(record.imagePaths[i], i);
                await new Promise(resolve => setTimeout(resolve, 200));
            }
        }
    } catch (error) {
        console.error('下载失败:', error);
        showToast('下载失败', 'error');
    }
}

// 下载单张图片
async function downloadSingleImage(imagePath, index) {
    try {
        const imageUrl = getImageUrl(imagePath);
        const response = await fetch(imageUrl);
        const blob = await response.blob();
        const url = URL.createObjectURL(blob);

        const link = document.createElement('a');
        link.href = url;
        link.download = `history_image_${index + 1}.png`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);

        URL.revokeObjectURL(url);
        showToast('图片已开始下载', 'success');
    } catch (error) {
        console.error('下载失败:', error);
        showToast('图片下载失败', 'error');
    }
}

// 删除记录
async function deleteRecord(historyId) {
    if (!confirm('确定要删除这条历史记录吗？此操作不可恢复！')) {
        return;
    }

    try {
        const response = await fetch(`/api/history/${historyId}`, {
            method: 'DELETE'
        });

        const result = await response.json();

        if (result.code === 200) {
            showToast('删除成功', 'success');
            loadHistory();
        } else {
            showToast('删除失败: ' + result.message, 'error');
        }
    } catch (error) {
        console.error('删除失败:', error);
        showToast('删除失败', 'error');
    }
}

// 工具函数
function getImageUrl(imagePath) {
    const parts = imagePath.split('/');
    if (parts.length >= 3) {
        const historyId = parts[1];
        const filename = parts[2];
        return `/api/history/image/${historyId}/${filename}`;
    }
    return '';
}

function formatTime(timestamp) {
    return timestamp.replace('T', ' ');
}

function getStyleName(style) {
    const styleMap = {
        'xiaohongshu': '小红书封面',
        'advertising_a': '广告素材'
    };
    return styleMap[style] || style;
}

function getModelShortName(model) {
    if (!model) return '未知';
    if (model.includes('2.5-flash')) return 'Gemini 2.5 Flash';
    if (model.includes('3-pro')) return 'Gemini 3 Pro';
    return model;
}

function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast ${type} show`;

    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}
