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
