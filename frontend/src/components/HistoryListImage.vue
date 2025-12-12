<template>
  <div class="h-full flex flex-col">
    <h2 class="text-xl font-bold text-white mb-4">📜 历史记录</h2>

    <!-- 加载中 -->
    <div v-if="loading" class="flex-1 flex items-center justify-center">
      <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-white"></div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!historyList || historyList.length === 0" class="flex-1 flex flex-col items-center justify-center text-white/60">
      <svg class="w-16 h-16 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
      </svg>
      <p>暂无历史记录</p>
    </div>

    <!-- 历史列表 -->
    <div v-else class="flex-1 overflow-y-auto space-y-3">
      <div
        v-for="record in historyList"
        :key="record.historyId"
        class="bg-white/10 rounded-lg p-3 border border-white/20 hover:bg-white/15 transition cursor-pointer"
        @click="$emit('view-detail', record)"
      >
        <!-- 缩略图 -->
        <div class="relative aspect-square rounded-lg overflow-hidden bg-gradient-to-br from-gray-700 to-gray-800 mb-3">
          <img
            v-if="record.imagePaths && record.imagePaths.length > 0"
            :src="getImageUrl(record.imagePaths[0])"
            :alt="record.request.title"
            class="w-full h-full object-cover"
          />
          <div v-else class="flex items-center justify-center h-full text-white/40">
            <svg class="w-12 h-12" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
          </div>
          <!-- 图片数量标签 -->
          <div class="absolute top-2 right-2 bg-black/70 px-2 py-1 rounded text-xs text-white">
            {{ record.imageCount }} 张
          </div>
        </div>

        <!-- 标题 -->
        <h3 class="text-white font-semibold text-sm line-clamp-2 mb-2">
          {{ record.request.title }}
        </h3>

        <!-- 关键词 -->
        <div v-if="record.request.keywords && record.request.keywords.length > 0" class="flex flex-wrap gap-1 mb-2">
          <span
            v-for="(keyword, index) in record.request.keywords.slice(0, 2)"
            :key="index"
            class="px-2 py-0.5 bg-purple-500/30 text-purple-200 rounded text-xs"
          >
            #{{ keyword }}
          </span>
          <span
            v-if="record.request.keywords.length > 2"
            class="px-2 py-0.5 bg-white/20 text-white/60 rounded text-xs"
          >
            +{{ record.request.keywords.length - 2 }}
          </span>
        </div>

        <!-- 元信息 -->
        <div class="flex items-center justify-between text-xs text-white/60">
          <span>{{ formatTime(record.timestamp) }}</span>
          <span>{{ record.metadata.generationTimeMs }}ms</span>
        </div>

        <!-- 操作按钮 -->
        <div class="mt-3 flex gap-2">
          <button
            @click.stop="$emit('download', record)"
            class="flex-1 px-3 py-1.5 bg-green-500/80 text-white rounded hover:bg-green-600 transition text-xs"
          >
            下载
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

const emit = defineEmits(['view-detail', 'download', 'delete'])

const getImageUrl = (imagePath) => {
  const parts = imagePath.split('/')
  if (parts.length >= 3) {
    const historyId = parts[1]
    const filename = parts[2]
    return `/api/history/image/${historyId}/${filename}`
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
