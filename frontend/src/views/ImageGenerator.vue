<template>
  <div class="min-h-screen py-12 px-4">
    <div class="max-w-7xl mx-auto">
      <!-- 标题 -->
      <div class="text-center mb-12">
        <h1 class="text-6xl font-bold text-white mb-4">
          AI 图片生成工具
        </h1>
        <p class="text-xl text-white/80">
          配置参数，一键生成1-3张高质量图片
        </p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- 左列：配置表单 -->
        <div class="glass-card rounded-xl p-6 h-[calc(100vh-16rem)] overflow-y-auto">
          <h2 class="text-2xl font-bold text-white mb-6">📝 配置参数</h2>

          <form @submit.prevent="handleGenerate" class="space-y-5">
            <!-- 主标题 -->
            <div>
              <label class="block text-white/90 mb-2 font-medium text-sm">主标题 *</label>
              <input
                v-model="form.title"
                type="text"
                required
                placeholder="例如：如何提升聊天魅力？"
                class="w-full px-4 py-2.5 bg-white/10 border border-white/20 rounded-lg text-white placeholder-white/50 focus:outline-none focus:border-white/50 transition text-sm"
              />
            </div>

            <!-- 副标题 -->
            <div>
              <label class="block text-white/90 mb-2 font-medium text-sm">副标题</label>
              <input
                v-model="form.subtitle"
                type="text"
                placeholder="例如：AI教你三招立刻变会聊"
                class="w-full px-4 py-2.5 bg-white/10 border border-white/20 rounded-lg text-white placeholder-white/50 focus:outline-none focus:border-white/50 transition text-sm"
              />
            </div>

            <!-- 关键词 -->
            <div>
              <label class="block text-white/90 mb-2 font-medium text-sm">关键词 * (最多5个，逗号分隔)</label>
              <input
                v-model="keywordsInput"
                type="text"
                required
                placeholder="例如：AI技巧,聊天提升,社交指南"
                class="w-full px-4 py-2.5 bg-white/10 border border-white/20 rounded-lg text-white placeholder-white/50 focus:outline-none focus:border-white/50 transition text-sm"
              />
              <p class="text-white/60 text-xs mt-1.5">已输入 {{ form.keywords.length }} 个关键词</p>
            </div>

            <!-- 风格模板 -->
            <div>
              <label class="block text-white/90 mb-2 font-medium text-sm">风格模板 *</label>
              <select
                v-model="form.style"
                required
                class="w-full px-4 py-2.5 bg-white/10 border border-white/20 rounded-lg text-white focus:outline-none focus:border-white/50 transition text-sm"
              >
                <option value="xiaohongshu">小红书封面 (1080x1350)</option>
                <option value="advertising_a">投放素材风格A (1080x1080)</option>
              </select>
            </div>

            <!-- AI 模型选择 -->
            <div>
              <label class="block text-white/90 mb-2 font-medium text-sm">AI 模型 *</label>
              <select
                v-model="form.model"
                required
                class="w-full px-4 py-2.5 bg-white/10 border border-white/20 rounded-lg text-white focus:outline-none focus:border-white/50 transition text-sm"
              >
                <option value="google/gemini-2.5-flash-image">Gemini 2.5 Flash Image (推荐)</option>
                <option value="google/gemini-3-pro-image-preview">Gemini 3 Pro Image</option>
              </select>
            </div>

            <!-- 生成数量 -->
            <div>
              <label class="block text-white/90 mb-2 font-medium text-sm">生成数量</label>
              <select
                v-model.number="form.count"
                class="w-full px-4 py-2.5 bg-white/10 border border-white/20 rounded-lg text-white focus:outline-none focus:border-white/50 transition text-sm"
              >
                <option :value="1">1张</option>
                <option :value="2">2张</option>
                <option :value="3">3张</option>
              </select>
            </div>

            <!-- 背景图上传 -->
            <div>
              <label class="block text-white/90 mb-2 font-medium text-sm">背景图（可选）</label>
              <div
                @click="triggerFileInput"
                class="border-2 border-dashed border-white/30 rounded-lg p-4 cursor-pointer hover:border-purple-400/60 hover:bg-white/5 transition"
              >
                <input
                  ref="backgroundFileInput"
                  type="file"
                  accept="image/png,image/jpeg,image/jpg"
                  @change="handleBackgroundFileSelect"
                  class="hidden"
                />
                <div v-if="!backgroundPreview" class="text-center text-white/60">
                  <svg class="mx-auto h-10 w-10 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
                  </svg>
                  <p class="text-xs">点击上传背景图</p>
                </div>
                <div v-else class="relative">
                  <img :src="backgroundPreview" alt="背景图" class="max-h-32 mx-auto rounded">
                  <button
                    @click.stop="clearBackgroundImage"
                    class="absolute top-1 right-1 bg-red-500 text-white p-1 rounded-full hover:bg-red-600 transition"
                  >
                    <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </button>
                </div>
              </div>
            </div>

            <!-- 提交按钮 -->
            <button
              type="submit"
              :disabled="loading"
              class="w-full px-6 py-3.5 bg-gradient-to-r from-purple-500 to-pink-600 text-white rounded-lg hover:from-purple-600 hover:to-pink-700 transition-all shadow-lg hover:shadow-2xl disabled:opacity-50 disabled:cursor-not-allowed font-bold text-base"
            >
              {{ loading ? '生成中...' : '🎨 生成图片' }}
            </button>
          </form>
        </div>

        <!-- 中列：生成结果 -->
        <div class="glass-card rounded-xl p-6 h-[calc(100vh-16rem)] overflow-y-auto">
          <h2 class="text-2xl font-bold text-white mb-6">🖼️ 生成结果</h2>

          <!-- 加载中 -->
          <div v-if="loading" class="flex flex-col items-center justify-center h-96">
            <div class="animate-spin rounded-full h-16 w-16 border-t-4 border-b-4 border-white mb-4"></div>
            <p class="text-white/80">正在生成图片，请稍候...</p>
          </div>

          <!-- 结果展示 -->
          <div v-else-if="result" class="space-y-4">
            <!-- 元数据 -->
            <div class="flex items-center justify-between text-white/80 text-sm bg-white/10 rounded-lg p-3">
              <span>✅ 生成成功: {{ result.metadata.count }} 张</span>
              <span>⏱️ 耗时: {{ result.metadata.generationTimeMs }}ms</span>
            </div>

            <!-- 图片列表 -->
            <div class="space-y-3">
              <div
                v-for="(image, index) in result.images"
                :key="index"
                class="bg-white/5 rounded-lg p-3 border border-white/10"
              >
                <div class="flex items-center justify-between mb-2">
                  <span class="text-white/90 font-medium text-sm">图片 {{ index + 1 }}</span>
                  <span class="text-white/60 text-xs">{{ image.width }}x{{ image.height }}</span>
                </div>
                <img
                  :src="image.base64"
                  :alt="`生成的图片 ${index + 1}`"
                  class="w-full rounded-lg shadow-lg"
                />
                <button
                  @click="downloadImage(image.base64, index)"
                  class="mt-2 w-full px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition text-sm"
                >
                  📥 下载图片
                </button>
              </div>
            </div>
          </div>

          <!-- 初始状态 -->
          <div v-else class="flex flex-col items-center justify-center h-96 text-white/60">
            <svg class="w-24 h-24 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
            <p class="text-lg">填写左侧表单开始生成</p>
          </div>
        </div>

        <!-- 右列：历史记录 -->
        <div class="glass-card rounded-xl p-6 h-[calc(100vh-16rem)]">
          <HistoryListImage
            :historyList="historyList"
            :loading="historyLoading"
            @download="handleDownloadHistory"
            @delete="handleDeleteHistory"
          />
        </div>
      </div>
    </div>

    <!-- Toast 提示 -->
    <Toast
      v-model="showToast"
      :message="toastMessage"
      :type="toastType"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import axios from 'axios'
import Toast from '../components/Toast.vue'
import HistoryListImage from '../components/HistoryListImage.vue'
import { getImageHistory, deleteImageHistory } from '../services/api.js'

const loading = ref(false)
const result = ref(null)
const showToast = ref(false)
const toastMessage = ref('')
const toastType = ref('success')

const form = ref({
  title: '',
  subtitle: '',
  keywords: [],
  style: 'xiaohongshu',
  model: 'google/gemini-2.5-flash-image',
  count: 1
})

const keywordsInput = ref('')
const backgroundFileInput = ref(null)
const backgroundFile = ref(null)
const backgroundPreview = ref(null)

// 历史记录
const historyList = ref([])
const historyLoading = ref(false)

// 监听关键词输入，自动分割
watch(keywordsInput, (value) => {
  if (value) {
    form.value.keywords = value.split(',').map(k => k.trim()).filter(k => k)
  } else {
    form.value.keywords = []
  }
})

// 加载历史记录
const loadHistory = async () => {
  historyLoading.value = true
  try {
    const data = await getImageHistory(1, 10)
    historyList.value = data.records
  } catch (error) {
    console.error('加载历史记录失败:', error)
  } finally {
    historyLoading.value = false
  }
}

// 背景图上传相关
const triggerFileInput = () => {
  backgroundFileInput.value.click()
}

const handleBackgroundFileSelect = (event) => {
  const file = event.target.files[0]
  if (file) {
    if (file.size > 10 * 1024 * 1024) {
      showToastMessage('文件大小不能超过 10MB', 'error')
      return
    }
    backgroundFile.value = file
    const reader = new FileReader()
    reader.onload = (e) => {
      backgroundPreview.value = e.target.result
    }
    reader.readAsDataURL(file)
  }
}

const clearBackgroundImage = () => {
  backgroundFile.value = null
  backgroundPreview.value = null
  if (backgroundFileInput.value) {
    backgroundFileInput.value.value = ''
  }
}

const handleGenerate = async () => {
  if (form.value.keywords.length === 0) {
    showToastMessage('请输入至少一个关键词', 'error')
    return
  }

  if (form.value.keywords.length > 5) {
    showToastMessage('关键词数量不能超过5个', 'error')
    return
  }

  loading.value = true
  result.value = null

  try {
    // 使用 FormData 支持文件上传
    const formData = new FormData()
    formData.append('title', form.value.title)
    if (form.value.subtitle) {
      formData.append('subtitle', form.value.subtitle)
    }
    formData.append('keywords', form.value.keywords.join(','))
    formData.append('style', form.value.style)
    formData.append('count', form.value.count)
    formData.append('model', form.value.model)

    if (backgroundFile.value) {
      formData.append('backgroundImage', backgroundFile.value)
    }

    const response = await axios.post('/api/generate/image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    if (response.data.code === 200) {
      result.value = response.data.data
      showToastMessage('✅ 图片生成成功！', 'success')
      // 刷新历史记录
      loadHistory()
    } else {
      showToastMessage('❌ ' + response.data.message, 'error')
    }
  } catch (error) {
    console.error('生成失败:', error)
    showToastMessage('❌ 生成失败: ' + (error.response?.data?.message || error.message), 'error')
  } finally {
    loading.value = false
  }
}

const downloadImage = async (imageUrl, index) => {
  try {
    if (imageUrl.startsWith('data:')) {
      const link = document.createElement('a')
      link.href = imageUrl
      link.download = `generated_image_${index + 1}.png`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      showToastMessage('图片已开始下载', 'success')
      return
    }

    const response = await fetch(imageUrl)
    const blob = await response.blob()
    const objectUrl = URL.createObjectURL(blob)

    const link = document.createElement('a')
    link.href = objectUrl
    link.download = `generated_image_${index + 1}.png`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)

    setTimeout(() => URL.revokeObjectURL(objectUrl), 100)
    showToastMessage('图片已开始下载', 'success')
  } catch (error) {
    console.error('下载失败:', error)
    showToastMessage('图片下载失败', 'error')
  }
}

const handleDownloadHistory = async (record) => {
  for (let i = 0; i < record.imagePaths.length; i++) {
    const imagePath = record.imagePaths[i]
    const parts = imagePath.split('/')
    if (parts.length >= 3) {
      const historyId = parts[1]
      const filename = parts[2]
      const imageUrl = `/api/history/image/${historyId}/${filename}`

      try {
        const response = await fetch(imageUrl)
        const blob = await response.blob()
        const objectUrl = URL.createObjectURL(blob)

        const link = document.createElement('a')
        link.href = objectUrl
        link.download = `history_image_${i + 1}.png`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)

        setTimeout(() => URL.revokeObjectURL(objectUrl), 100)
      } catch (error) {
        console.error('下载失败:', error)
      }
      await new Promise(resolve => setTimeout(resolve, 200))
    }
  }
  showToastMessage('图片已开始下载', 'success')
}

const handleDeleteHistory = async (historyId) => {
  if (!confirm('确定要删除这条历史记录吗？')) {
    return
  }

  try {
    await deleteImageHistory(historyId)
    showToastMessage('删除成功', 'success')
    loadHistory()
  } catch (error) {
    console.error('删除失败:', error)
    showToastMessage('删除失败: ' + error.message, 'error')
  }
}

const showToastMessage = (message, type) => {
  toastMessage.value = message
  toastType.value = type
  showToast.value = true
}

// 页面加载时获取历史记录
onMounted(() => {
  loadHistory()
})
</script>

<style scoped>
.glass-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}
</style>
