<template>
  <div class="h-full flex flex-col">
    <h2 class="text-xl font-bold text-white mb-4">📜 解析历史</h2>

    <!-- 加载中 -->
    <div v-if="loading" class="flex-1 flex items-center justify-center">
      <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-white"></div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!historyList || historyList.length === 0" class="flex-1 flex flex-col items-center justify-center text-white/60">
      <svg class="w-16 h-16 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
      </svg>
      <p>暂无解析历史</p>
    </div>

    <!-- 历史列表 -->
    <div v-else class="flex-1 overflow-y-auto space-y-3">
      <div
        v-for="record in historyList"
        :key="record.historyId"
        class="bg-white/10 rounded-lg p-3 border border-white/20 hover:bg-white/15 transition cursor-pointer"
        @click="$emit('view-detail', record)"
      >
        <!-- 预览图 (原始设计稿缩略图) -->
        <div class="relative aspect-[4/3] rounded-lg overflow-hidden bg-gradient-to-br from-gray-700 to-gray-800 mb-3">
          <img
            v-if="record.originalImagePath"
            :src="getImageUrl(record.originalImagePath)"
            :alt="'设计稿解析'"
            class="w-full h-full object-cover"
          />
          <div v-else class="flex items-center justify-center h-full text-white/40">
            <svg class="w-12 h-12" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 5a1 1 0 011-1h14a1 1 0 011 1v2a1 1 0 01-1 1H5a1 1 0 01-1-1V5zM4 13a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H5a1 1 0 01-1-1v-6zM16 13a1 1 0 011-1h2a1 1 0 011 1v6a1 1 0 01-1 1h-2a1 1 0 01-1-1v-6z" />
            </svg>
          </div>
          <!-- 组件数量标签 -->
          <div class="absolute top-2 right-2 bg-black/70 px-2 py-1 rounded text-xs text-white">
            {{ record.componentCount }} 组件
          </div>
        </div>

        <!-- 分析引擎 -->
        <div class="mb-2">
          <span class="px-2 py-1 bg-blue-500/30 text-blue-200 rounded text-xs">
            {{ record.analysisEngine }}
          </span>
        </div>

        <!-- 统计信息 -->
        <div class="grid grid-cols-3 gap-2 mb-2 text-xs">
          <div class="bg-white/5 rounded px-2 py-1.5 text-center">
            <div class="text-blue-300 font-semibold">{{ record.metadata.textCount || 0 }}</div>
            <div class="text-white/60">文字</div>
          </div>
          <div class="bg-white/5 rounded px-2 py-1.5 text-center">
            <div class="text-green-300 font-semibold">{{ record.metadata.buttonCount || 0 }}</div>
            <div class="text-white/60">按钮</div>
          </div>
          <div class="bg-white/5 rounded px-2 py-1.5 text-center">
            <div class="text-purple-300 font-semibold">{{ record.metadata.imageCount || 0 }}</div>
            <div class="text-white/60">图片</div>
          </div>
        </div>

        <!-- 元信息 -->
        <div class="flex items-center justify-between text-xs text-white/60 mb-3">
          <span>{{ formatTime(record.timestamp) }}</span>
          <span>{{ record.metadata.processingTimeMs }}ms</span>
        </div>

        <!-- 操作按钮 -->
        <div class="flex gap-2">
          <button
            @click.stop="$emit('export', record)"
            class="flex-1 px-3 py-1.5 bg-green-500/80 text-white rounded hover:bg-green-600 transition text-xs"
          >
            导出
          </button>
          <button
            @click.stop="$emit('delete', record.historyId)"
            class="px-3 py-1.5 bg-red-500/80 text-white rounded hover:bg-red-600 transition text-xs"
          >
            删除
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  historyList: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['view-detail', 'export', 'delete'])

const getImageUrl = (imagePath) => {
  const parts = imagePath.split('/')
  if (parts.length >= 3) {
    const historyId = parts[1]
    const filename = parts[2]
    return `/api/history/analysis/image/${historyId}/${filename}`
  }
  return ''
}

const formatTime = (timestamp) => {
  if (!timestamp) return ''
  return timestamp.replace('T', ' ').substring(0, 16)
}
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
