<template>
  <div class="min-h-screen py-12 px-4">
    <div class="max-w-7xl mx-auto">
      <!-- 标题 -->
      <div class="text-center mb-12 animate-fade-in-up">
        <h1 class="text-6xl font-bold text-white mb-4 tracking-tight">
          <span class="gradient-text">AI 设计稿自动解析系统</span>
        </h1>
        <p class="text-xl text-white/80">
          ✨ 上传设计稿，AI 自动识别并生成 JSON 模板
        </p>
        <div class="mt-4 flex items-center justify-center gap-3 text-sm text-white/60">
          <span class="flex items-center gap-1">
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
              <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
            </svg>
            Claude 3.5 Sonnet
          </span>
          <span>•</span>
          <span class="flex items-center gap-1">
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M6.267 3.455a3.066 3.066 0 001.745-.723 3.066 3.066 0 013.976 0 3.066 3.066 0 001.745.723 3.066 3.066 0 012.812 2.812c.051.643.304 1.254.723 1.745a3.066 3.066 0 010 3.976 3.066 3.066 0 00-.723 1.745 3.066 3.066 0 01-2.812 2.812 3.066 3.066 0 00-1.745.723 3.066 3.066 0 01-3.976 0 3.066 3.066 0 00-1.745-.723 3.066 3.066 0 01-2.812-2.812 3.066 3.066 0 00-.723-1.745 3.066 3.066 0 010-3.976 3.066 3.066 0 00.723-1.745 3.066 3.066 0 012.812-2.812zm7.44 5.252a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
            </svg>
            双引擎识别
          </span>
          <span>•</span>
          <span class="flex items-center gap-1">
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
              <path d="M13 6a3 3 0 11-6 0 3 3 0 016 0zM18 8a2 2 0 11-4 0 2 2 0 014 0zM14 15a4 4 0 00-8 0v3h8v-3zM6 8a2 2 0 11-4 0 2 2 0 014 0zM16 18v-3a5.972 5.972 0 00-.75-2.906A3.005 3.005 0 0119 15v3h-3zM4.75 12.094A5.973 5.973 0 004 15v3H1v-3a3 3 0 013.75-2.906z" />
            </svg>
            多模态大模型
          </span>
        </div>
      </div>

      <!-- 主内容区域 -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- 左列：上传区域 + 配置 -->
        <div class="space-y-4 h-[calc(100vh-16rem)] overflow-y-auto">
          <ImageUploader
            @upload="handleUpload"
            :loading="loading"
          />

          <!-- 配置选项 -->
          <div class="glass-card rounded-xl p-5">
            <h3 class="text-lg font-semibold mb-4 text-white">🎛️ 识别引擎配置</h3>
            <div class="space-y-3">
              <label class="flex items-center space-x-3 cursor-pointer group">
                <input
                  type="checkbox"
                  v-model="config.enableAI"
                  class="w-5 h-5 text-purple-600 rounded focus:ring-2 focus:ring-purple-500 focus:ring-offset-0"
                >
                <span class="text-white/90 group-hover:text-white transition text-sm">🤖 启用 AI 识别（Claude 3.5 Sonnet）</span>
              </label>
              <label class="flex items-center space-x-3 cursor-pointer group">
                <input
                  type="checkbox"
                  v-model="config.enableOCR"
                  class="w-5 h-5 text-purple-600 rounded focus:ring-2 focus:ring-purple-500 focus:ring-offset-0"
                >
                <span class="text-white/90 group-hover:text-white transition text-sm">📝 启用 OCR 文字识别（Tesseract）</span>
              </label>
              <label class="flex items-center space-x-3 cursor-pointer group">
                <input
                  type="checkbox"
                  v-model="config.enableCV"
                  class="w-5 h-5 text-purple-600 rounded focus:ring-2 focus:ring-purple-500 focus:ring-offset-0"
                >
                <span class="text-white/90 group-hover:text-white transition text-sm">👁️ 启用 OpenCV 组件检测</span>
              </label>
            </div>
          </div>

          <!-- 统计信息 -->
          <div v-if="result" class="glass-card rounded-xl p-5">
            <h3 class="text-lg font-semibold mb-4 text-white">📊 分析结果</h3>
            <div class="grid grid-cols-2 gap-3">
              <div class="text-center p-3 bg-blue-500/20 rounded-lg border border-blue-500/30 hover:bg-blue-500/30 transition">
                <div class="text-2xl font-bold text-blue-300">{{ animatedTextCount }}</div>
                <div class="text-xs text-white/70">文字组件</div>
              </div>
              <div class="text-center p-3 bg-green-500/20 rounded-lg border border-green-500/30 hover:bg-green-500/30 transition">
                <div class="text-2xl font-bold text-green-300">{{ animatedButtonCount }}</div>
                <div class="text-xs text-white/70">按钮组件</div>
              </div>
              <div class="text-center p-3 bg-purple-500/20 rounded-lg border border-purple-500/30 hover:bg-purple-500/30 transition">
                <div class="text-2xl font-bold text-purple-300">{{ animatedImageCount }}</div>
                <div class="text-xs text-white/70">图片组件</div>
              </div>
              <div class="text-center p-3 bg-yellow-500/20 rounded-lg border border-yellow-500/30 hover:bg-yellow-500/30 transition">
                <div class="text-2xl font-bold text-yellow-300">{{ animatedProcessingTime }}</div>
                <div class="text-xs text-white/70">处理时间(ms)</div>
              </div>
            </div>
          </div>

          <!-- 导出功能 -->
          <div v-if="result" class="glass-card rounded-xl p-5">
            <h3 class="text-lg font-semibold mb-4 text-white">💾 导出选项</h3>
            <div class="space-y-2">
              <button
                @click="handleExportHTML"
                class="w-full px-4 py-2.5 bg-gradient-to-r from-green-500 to-emerald-600 text-white rounded-lg hover:from-green-600 hover:to-emerald-700 transition-all shadow-lg hover:shadow-2xl hover:scale-105 flex items-center justify-center gap-2 font-medium text-sm"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
                导出为 HTML 文件
              </button>

              <button
                @click="handleCopyJSON"
                class="w-full px-4 py-2.5 bg-gradient-to-r from-blue-500 to-cyan-600 text-white rounded-lg hover:from-blue-600 hover:to-cyan-700 transition-all shadow-lg hover:shadow-2xl hover:scale-105 flex items-center justify-center gap-2 font-medium text-sm"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
                </svg>
                复制 JSON
              </button>
            </div>
          </div>
        </div>

        <!-- 中列：结果展示 -->
        <div class="space-y-4 h-[calc(100vh-16rem)] overflow-y-auto">
          <!-- JSON 预览 -->
          <JsonPreview :jsonData="result?.template" />

          <!-- 页面渲染 -->
          <PageRenderer
            v-if="result?.template"
            ref="pageRendererRef"
            :template="result.template"
            @fullscreen="handleFullscreen"
          />
        </div>

        <!-- 右列：解析历史 -->
        <div class="glass-card rounded-xl p-6 h-[calc(100vh-16rem)]">
          <HistoryListAnalysis
            :historyList="analysisHistoryList"
            :loading="historyLoading"
            @export="handleExportFromHistory"
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
import { ref, computed, onMounted } from 'vue'
import ImageUploader from '../components/ImageUploader.vue'
import JsonPreview from '../components/JsonPreview.vue'
import PageRenderer from '../components/PageRenderer.vue'
import Toast from '../components/Toast.vue'
import HistoryListAnalysis from '../components/HistoryListAnalysis.vue'
import { analyzeImage, getAnalysisHistory, deleteAnalysisHistory } from '../services/api.js'
import { exportToHTML } from '../utils/htmlExporter.js'
import { useCountAnimation } from '../composables/useCountAnimation.js'

const loading = ref(false)
const result = ref(null)
const config = ref({
  enableAI: true,
  enableOCR: true,
  enableCV: true
})
const pageRendererRef = ref(null)
const showToast = ref(false)
const toastMessage = ref('')
const toastType = ref('success')

// 历史记录
const analysisHistoryList = ref([])
const historyLoading = ref(false)

// 数字动画
const textCount = computed(() => result.value?.metadata.textCount || 0)
const buttonCount = computed(() => result.value?.metadata.buttonCount || 0)
const imageCount = computed(() => result.value?.metadata.imageCount || 0)
const processingTime = computed(() => result.value?.metadata.processingTimeMs || 0)

const { displayValue: animatedTextCount } = useCountAnimation(textCount, 1000)
const { displayValue: animatedButtonCount } = useCountAnimation(buttonCount, 1000)
const { displayValue: animatedImageCount } = useCountAnimation(imageCount, 1000)
const { displayValue: animatedProcessingTime } = useCountAnimation(processingTime, 1500)

// 加载解析历史
const loadHistory = async () => {
  historyLoading.value = true
  try {
    const data = await getAnalysisHistory(1, 10)
    analysisHistoryList.value = data.records
  } catch (error) {
    console.error('加载解析历史失败:', error)
  } finally {
    historyLoading.value = false
  }
}

const handleUpload = async (file) => {
  loading.value = true
  result.value = null

  try {
    const response = await analyzeImage(file, config.value)
    result.value = response
    showToastMessage('✅ 分析完成！', 'success')
    // 刷新历史记录
    loadHistory()
  } catch (error) {
    showToastMessage('❌ 分析失败: ' + error.message, 'error')
  } finally {
    loading.value = false
  }
}

const handleFullscreen = () => {
  if (pageRendererRef.value) {
    pageRendererRef.value.openFullscreen()
  }
}

const handleExportHTML = () => {
  if (!result.value?.template) {
    showToastMessage('⚠️ 没有可导出的内容', 'error')
    return
  }

  try {
    const timestamp = new Date().toISOString().replace(/[:.]/g, '-').slice(0, 19)
    const filename = `ai-design-preview-${timestamp}.html`
    exportToHTML(result.value.template, filename)
    showToastMessage(`✅ HTML 文件已导出: ${filename}`, 'success')
  } catch (error) {
    showToastMessage('❌ 导出失败: ' + error.message, 'error')
  }
}

const handleCopyJSON = async () => {
  if (!result.value?.template) {
    showToastMessage('⚠️ 没有可复制的内容', 'error')
    return
  }

  try {
    const jsonString = JSON.stringify(result.value.template, null, 2)
    await navigator.clipboard.writeText(jsonString)
    showToastMessage('✅ JSON 已复制到剪贴板', 'success')
  } catch (error) {
    showToastMessage('❌ 复制失败: ' + error.message, 'error')
  }
}

const handleExportFromHistory = (record) => {
  if (!record?.template) {
    showToastMessage('⚠️ 没有可导出的内容', 'error')
    return
  }

  try {
    const timestamp = record.timestamp.replace(/[:.T]/g, '-').slice(0, 19)
    const filename = `ai-design-preview-${timestamp}.html`
    exportToHTML(record.template, filename)
    showToastMessage(`✅ HTML 文件已导出: ${filename}`, 'success')
  } catch (error) {
    showToastMessage('❌ 导出失败: ' + error.message, 'error')
  }
}

const handleDeleteHistory = async (historyId) => {
  if (!confirm('确定要删除这条历史记录吗？')) {
    return
  }

  try {
    await deleteAnalysisHistory(historyId)
    showToastMessage('删除成功', 'success')
    loadHistory()
  } catch (error) {
    console.error('删除失败:', error)
    showToastMessage('删除失败: ' + error.message, 'error')
  }
}

const showToastMessage = (message, type = 'success') => {
  toastMessage.value = message
  toastType.value = type
  showToast.value = true
}

// 页面加载时获取历史记录
onMounted(() => {
  loadHistory()
})
</script>
