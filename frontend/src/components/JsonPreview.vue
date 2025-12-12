<template>
  <div class="glass-card rounded-xl p-6">
    <div class="flex items-center justify-between mb-4">
      <h3 class="text-xl font-bold text-white">📄 JSON 模板</h3>
      <div class="flex space-x-2">
        <button
          @click="copyToClipboard"
          class="px-4 py-2 bg-blue-500/80 text-white rounded-lg hover:bg-blue-600 transition text-sm font-medium hover:scale-105 shadow-lg"
        >
          📋 复制
        </button>
        <button
          @click="downloadJson"
          class="px-4 py-2 bg-green-500/80 text-white rounded-lg hover:bg-green-600 transition text-sm font-medium hover:scale-105 shadow-lg"
        >
          💾 下载
        </button>
      </div>
    </div>

    <div
      v-if="jsonData"
      class="bg-gray-900/80 backdrop-blur-sm rounded-lg p-4 overflow-x-auto max-h-96 overflow-y-auto border border-white/10"
    >
      <pre class="text-sm text-emerald-400 font-mono">{{ formattedJson }}</pre>
    </div>

    <div v-else class="text-center py-12 text-white/60">
      <svg class="mx-auto h-12 w-12 mb-4 text-purple-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
      </svg>
      <p class="text-white/80">暂无 JSON 数据</p>
      <p class="text-sm mt-2 text-white/60">上传设计稿后将显示解析结果</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  jsonData: {
    type: Object,
    default: null
  }
})

const formattedJson = computed(() => {
  return props.jsonData ? JSON.stringify(props.jsonData, null, 2) : ''
})

const copyToClipboard = async () => {
  if (!props.jsonData) return

  try {
    await navigator.clipboard.writeText(formattedJson.value)
    alert('JSON 已复制到剪贴板！')
  } catch (err) {
    alert('复制失败: ' + err.message)
  }
}

const downloadJson = () => {
  if (!props.jsonData) return

  const blob = new Blob([formattedJson.value], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `template-${Date.now()}.json`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}
</script>
