<template>
  <div class="min-h-screen py-8 px-4">
    <div class="max-w-7xl mx-auto">
      <!-- 页面标题 -->
      <div class="mb-8">
        <h1 class="text-4xl font-bold bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-600 bg-clip-text text-transparent mb-2">
          历史记录
        </h1>
        <p class="text-gray-600 text-lg">
          查看和管理您的所有图片生成历史
        </p>
      </div>

      <!-- 筛选栏 -->
      <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 mb-6">
        <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div class="flex items-center gap-4">
            <label class="text-sm font-medium text-gray-700">筛选条件：</label>
            <select
              v-model="filterStyle"
              @change="loadHistory"
              class="px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-gray-700 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition"
            >
              <option value="">全部风格</option>
              <option value="xiaohongshu">小红书封面</option>
              <option value="advertising_a">投放素材风格A</option>
            </select>
          </div>
          <div class="flex items-center gap-2 text-gray-600">
            <svg class="w-5 h-5 text-indigo-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            <span class="text-sm">共 <span class="font-semibold text-indigo-600">{{ totalCount }}</span> 条记录</span>
          </div>
        </div>
      </div>

      <!-- 加载中 -->
      <div v-if="loading" class="flex flex-col items-center justify-center py-24">
        <div class="relative">
          <div class="animate-spin rounded-full h-16 w-16 border-4 border-indigo-200"></div>
          <div class="animate-spin rounded-full h-16 w-16 border-t-4 border-indigo-600 absolute top-0"></div>
        </div>
        <p class="text-gray-600 mt-4">加载中...</p>
      </div>

      <!-- 历史记录网格 -->
      <div v-else-if="historyList.length > 0" class="space-y-6">
        <!-- 历史记录卡片 -->
        <div
          v-for="record in historyList"
          :key="record.historyId"
          class="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-lg hover:border-indigo-200 transition-all duration-300"
        >
          <div class="grid grid-cols-1 lg:grid-cols-4 gap-6 p-6">
            <!-- 左侧：图片预览 -->
            <div class="lg:col-span-1">
              <div class="relative aspect-square rounded-xl overflow-hidden bg-gradient-to-br from-gray-50 to-gray-100 group">
                <img
                  v-if="record.imagePaths && record.imagePaths.length > 0"
                  :src="getImageUrl(record.imagePaths[0])"
                  :alt="record.request.title"
                  class="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
                />
                <div v-else class="flex items-center justify-center h-full text-gray-400">
                  <svg class="w-16 h-16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                </div>
                <!-- 图片数量标签 -->
                <div class="absolute top-3 right-3 bg-white/95 backdrop-blur-sm px-3 py-1 rounded-full text-xs font-medium text-gray-700 shadow-sm">
                  {{ record.imageCount }} 张
                </div>
              </div>
            </div>

            <!-- 中间：详细信息 -->
            <div class="lg:col-span-2 flex flex-col justify-between">
              <div class="space-y-3">
                <!-- 标题 -->
                <h3 class="text-xl font-bold text-gray-900 line-clamp-2">
                  {{ record.request.title }}
                </h3>

                <!-- 副标题 -->
                <p v-if="record.request.subtitle" class="text-gray-600 line-clamp-1">
                  {{ record.request.subtitle }}
                </p>

                <!-- 关键词标签 -->
                <div v-if="record.request.keywords && record.request.keywords.length > 0" class="flex flex-wrap gap-2">
                  <span
                    v-for="(keyword, index) in record.request.keywords.slice(0, 3)"
                    :key="index"
                    class="px-3 py-1 bg-gradient-to-r from-indigo-50 to-purple-50 text-indigo-700 rounded-lg text-sm font-medium"
                  >
                    #{{ keyword }}
                  </span>
                  <span
                    v-if="record.request.keywords.length > 3"
                    class="px-3 py-1 bg-gray-100 text-gray-600 rounded-lg text-sm"
                  >
                    +{{ record.request.keywords.length - 3 }}
                  </span>
                </div>

                <!-- 元信息 -->
                <div class="flex flex-wrap items-center gap-4 text-sm text-gray-500">
                  <div class="flex items-center gap-1.5">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    {{ formatTime(record.timestamp) }}
                  </div>
                  <div class="flex items-center gap-1.5">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 21a4 4 0 01-4-4V5a2 2 0 012-2h4a2 2 0 012 2v12a4 4 0 01-4 4zm0 0h12a2 2 0 002-2v-4a2 2 0 00-2-2h-2.343M11 7.343l1.657-1.657a2 2 0 012.828 0l2.829 2.829a2 2 0 010 2.828l-8.486 8.485M7 17h.01" />
                    </svg>
                    {{ getStyleName(record.style) }}
                  </div>
                  <div class="flex items-center gap-1.5">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
                    </svg>
                    {{ record.metadata.generationTimeMs }}ms
                  </div>
                </div>
              </div>
            </div>

            <!-- 右侧：操作按钮 -->
            <div class="lg:col-span-1 flex lg:flex-col gap-2">
              <button
                @click="viewDetail(record)"
                class="flex-1 px-4 py-2.5 bg-gradient-to-r from-indigo-500 to-purple-500 text-white rounded-xl hover:from-indigo-600 hover:to-purple-600 transition-all shadow-sm hover:shadow-md font-medium text-sm flex items-center justify-center gap-2"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                </svg>
                查看详情
              </button>
              <button
                @click="downloadAll(record)"
                class="flex-1 px-4 py-2.5 bg-gradient-to-r from-green-500 to-emerald-500 text-white rounded-xl hover:from-green-600 hover:to-emerald-600 transition-all shadow-sm hover:shadow-md font-medium text-sm flex items-center justify-center gap-2"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                </svg>
                下载全部
              </button>
              <button
                @click="confirmDelete(record.historyId)"
                class="flex-1 px-4 py-2.5 bg-gradient-to-r from-red-500 to-pink-500 text-white rounded-xl hover:from-red-600 hover:to-pink-600 transition-all shadow-sm hover:shadow-md font-medium text-sm flex items-center justify-center gap-2"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                </svg>
                删除
              </button>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div class="flex items-center justify-center gap-3 pt-6">
          <button
            @click="prevPage"
            :disabled="currentPage <= 1"
            class="px-5 py-2.5 bg-white border border-gray-200 text-gray-700 rounded-xl hover:bg-gray-50 hover:border-indigo-300 transition-all disabled:opacity-40 disabled:cursor-not-allowed disabled:hover:bg-white disabled:hover:border-gray-200 font-medium shadow-sm"
          >
            上一页
          </button>
          <div class="px-4 py-2 bg-gradient-to-r from-indigo-50 to-purple-50 text-indigo-700 rounded-xl font-medium text-sm">
            第 {{ currentPage }} / {{ totalPages }} 页
          </div>
          <button
            @click="nextPage"
            :disabled="currentPage >= totalPages"
            class="px-5 py-2.5 bg-white border border-gray-200 text-gray-700 rounded-xl hover:bg-gray-50 hover:border-indigo-300 transition-all disabled:opacity-40 disabled:cursor-not-allowed disabled:hover:bg-white disabled:hover:border-gray-200 font-medium shadow-sm"
          >
            下一页
          </button>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="bg-white rounded-2xl shadow-sm border border-gray-100 p-16">
        <div class="flex flex-col items-center justify-center text-center">
          <div class="w-24 h-24 bg-gradient-to-br from-indigo-100 to-purple-100 rounded-full flex items-center justify-center mb-6">
            <svg class="w-12 h-12 text-indigo-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
          </div>
          <h3 class="text-xl font-semibold text-gray-900 mb-2">暂无历史记录</h3>
          <p class="text-gray-500 mb-6">您还没有生成过任何图片</p>
          <router-link
            to="/generate"
            class="px-6 py-3 bg-gradient-to-r from-indigo-500 to-purple-500 text-white rounded-xl hover:from-indigo-600 hover:to-purple-600 transition-all shadow-md hover:shadow-lg font-medium"
          >
            立即生成图片
          </router-link>
        </div>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <div
      v-if="showDetailModal"
      class="fixed inset-0 bg-black/30 backdrop-blur-sm flex items-center justify-center z-50 p-4 animate-fade-in"
      @click.self="closeDetail"
    >
      <div class="bg-white rounded-3xl shadow-2xl max-w-6xl w-full max-h-[90vh] overflow-y-auto">
        <!-- 弹窗头部 -->
        <div class="sticky top-0 bg-white border-b border-gray-100 px-8 py-6 flex items-center justify-between rounded-t-3xl z-10">
          <h2 class="text-2xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent">
            历史记录详情
          </h2>
          <button
            @click="closeDetail"
            class="w-10 h-10 flex items-center justify-center rounded-full hover:bg-gray-100 transition-colors text-gray-500 hover:text-gray-700"
          >
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <!-- 弹窗内容 -->
        <div v-if="selectedRecord" class="p-8 space-y-8">
          <!-- 基本信息卡片 -->
          <div class="bg-gradient-to-br from-indigo-50 to-purple-50 rounded-2xl p-6">
            <h3 class="text-2xl font-bold text-gray-900 mb-2">{{ selectedRecord.request.title }}</h3>
            <p v-if="selectedRecord.request.subtitle" class="text-gray-700 text-lg mb-4">
              {{ selectedRecord.request.subtitle }}
            </p>
            <div class="flex flex-wrap items-center gap-4 text-sm text-gray-600">
              <div class="flex items-center gap-2">
                <svg class="w-5 h-5 text-indigo-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                {{ formatTime(selectedRecord.timestamp) }}
              </div>
              <div class="flex items-center gap-2">
                <svg class="w-5 h-5 text-purple-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 21a4 4 0 01-4-4V5a2 2 0 012-2h4a2 2 0 012 2v12a4 4 0 01-4 4zm0 0h12a2 2 0 002-2v-4a2 2 0 00-2-2h-2.343M11 7.343l1.657-1.657a2 2 0 012.828 0l2.829 2.829a2 2 0 010 2.828l-8.486 8.485M7 17h.01" />
                </svg>
                {{ getStyleName(selectedRecord.style) }}
              </div>
              <div class="flex items-center gap-2">
                <svg class="w-5 h-5 text-pink-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                </svg>
                {{ getModelShortName(selectedRecord.model) }}
              </div>
            </div>
          </div>

          <!-- 图片网格 -->
          <div>
            <h4 class="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
              <svg class="w-6 h-6 text-indigo-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              生成的图片（{{ selectedRecord.imageCount }} 张）
            </h4>
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              <div
                v-for="(imagePath, index) in selectedRecord.imagePaths"
                :key="index"
                class="group relative"
              >
                <div class="aspect-square rounded-2xl overflow-hidden bg-gradient-to-br from-gray-50 to-gray-100 shadow-sm hover:shadow-lg transition-all duration-300">
                  <img
                    :src="getImageUrl(imagePath)"
                    :alt="`图片 ${index + 1}`"
                    class="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
                  />
                </div>
                <button
                  @click="downloadSingleImage(imagePath, index)"
                  class="mt-3 w-full px-4 py-2.5 bg-gradient-to-r from-green-500 to-emerald-500 text-white rounded-xl hover:from-green-600 hover:to-emerald-600 transition-all shadow-sm hover:shadow-md font-medium text-sm flex items-center justify-center gap-2"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                  </svg>
                  下载图片 {{ index + 1 }}
                </button>
              </div>
            </div>
          </div>
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
import { ref, onMounted } from 'vue'
import axios from 'axios'
import Toast from '../components/Toast.vue'

const loading = ref(false)
const historyList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const totalCount = ref(0)
const totalPages = ref(0)
const filterStyle = ref('')

const showDetailModal = ref(false)
const selectedRecord = ref(null)

const showToast = ref(false)
const toastMessage = ref('')
const toastType = ref('success')

// 加载历史记录
const loadHistory = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (filterStyle.value) {
      params.style = filterStyle.value
    }

    const response = await axios.get('/api/history/list', { params })

    if (response.data.code === 200) {
      historyList.value = response.data.data.records
      totalCount.value = response.data.data.total
      totalPages.value = response.data.data.totalPages
    } else {
      showToastMessage('加载失败: ' + response.data.message, 'error')
    }
  } catch (error) {
    console.error('加载历史记录失败:', error)
    showToastMessage('加载失败: ' + (error.response?.data?.message || error.message), 'error')
  } finally {
    loading.value = false
  }
}

// 上一页
const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
    loadHistory()
  }
}

// 下一页
const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
    loadHistory()
  }
}

// 查看详情
const viewDetail = (record) => {
  selectedRecord.value = record
  showDetailModal.value = true
}

// 关闭详情
const closeDetail = () => {
  showDetailModal.value = false
  selectedRecord.value = null
}

// 删除确认
const confirmDelete = async (historyId) => {
  if (!confirm('确定要删除这条历史记录吗？此操作不可恢复！')) {
    return
  }

  try {
    const response = await axios.delete(`/api/history/${historyId}`)

    if (response.data.code === 200) {
      showToastMessage('删除成功', 'success')
      loadHistory()
    } else {
      showToastMessage('删除失败: ' + response.data.message, 'error')
    }
  } catch (error) {
    console.error('删除失败:', error)
    showToastMessage('删除失败: ' + (error.response?.data?.message || error.message), 'error')
  }
}

// 获取图片 URL
const getImageUrl = (imagePath) => {
  const parts = imagePath.split('/')
  if (parts.length >= 3) {
    const historyId = parts[1]
    const filename = parts[2]
    return `/api/history/image/${historyId}/${filename}`
  }
  return ''
}

// 下载单张图片
const downloadSingleImage = async (imagePath, index) => {
  try {
    const imageUrl = getImageUrl(imagePath)
    const response = await fetch(imageUrl)
    const blob = await response.blob()
    const objectUrl = URL.createObjectURL(blob)

    const link = document.createElement('a')
    link.href = objectUrl
    link.download = `history_image_${index + 1}.png`
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

// 下���全部图片
const downloadAll = async (record) => {
  for (let i = 0; i < record.imagePaths.length; i++) {
    await downloadSingleImage(record.imagePaths[i], i)
    await new Promise(resolve => setTimeout(resolve, 200))
  }
}

// 格式化时间
const formatTime = (timestamp) => {
  return timestamp.replace('T', ' ')
}

// 获取风格名称
const getStyleName = (style) => {
  const styleMap = {
    'xiaohongshu': '小红书封面',
    'advertising_a': '投放素材风格A'
  }
  return styleMap[style] || style
}

// 获取模型简称
const getModelShortName = (model) => {
  if (!model) return '未知'
  if (model.includes('2.5-flash')) return 'Gemini 2.5 Flash'
  if (model.includes('3-pro')) return 'Gemini 3 Pro'
  return model
}

// 显示 Toast
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
/* 淡入动画 */
@keyframes fade-in {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.animate-fade-in {
  animation: fade-in 0.2s ease-out;
}

/* 文字截断 */
.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
