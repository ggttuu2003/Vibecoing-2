<template>
  <div class="glass-card rounded-xl p-8">
    <h2 class="text-2xl font-bold mb-6 text-white">📤 上传设计稿</h2>

    <!-- 拖拽上传区域 -->
    <div
      @dragover.prevent="isDragging = true"
      @dragleave.prevent="isDragging = false"
      @drop.prevent="handleDrop"
      @click="triggerFileInput"
      :class="[
        'border-3 border-dashed rounded-xl p-12 text-center cursor-pointer transition-all duration-300',
        isDragging
          ? 'border-purple-400 bg-purple-500/20 pulse-glow scale-105'
          : 'border-white/30 hover:border-purple-400/60 hover:bg-white/5'
      ]"
    >
      <input
        ref="fileInput"
        type="file"
        accept="image/png,image/jpeg,image/jpg"
        @change="handleFileSelect"
        class="hidden"
      >

      <div v-if="!preview" class="space-y-4">
        <svg class="mx-auto h-16 w-16 text-purple-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
        </svg>
        <div>
          <p class="text-lg font-medium text-white">
            拖拽图片到这里，或点击上传
          </p>
          <p class="text-sm text-white/60 mt-2">
            支持 PNG、JPG 格式，最大 10MB
          </p>
        </div>
      </div>

      <!-- 预览图 -->
      <div v-else class="relative">
        <img :src="preview" alt="Preview" class="max-h-96 mx-auto rounded-lg shadow-2xl border-2 border-white/20">
        <button
          @click.stop="clearImage"
          class="absolute top-2 right-2 bg-red-500 text-white p-2 rounded-full hover:bg-red-600 transition hover:scale-110 shadow-lg"
        >
          <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </div>

    <!-- 上传按钮 -->
    <button
      v-if="selectedFile"
      @click="handleUpload"
      :disabled="loading"
      class="mt-6 w-full py-4 px-6 rounded-xl font-semibold text-white text-lg transition-all duration-300 transform hover:scale-105 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none shadow-2xl"
      :class="loading ? 'bg-gray-500' : 'bg-gradient-to-r from-purple-600 via-blue-600 to-cyan-600 hover:from-purple-700 hover:via-blue-700 hover:to-cyan-700'"
    >
      <span v-if="loading" class="flex items-center justify-center">
        <svg class="animate-spin h-6 w-6 mr-3" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
        🔍 AI 分析中...
      </span>
      <span v-else>✨ 开始分析</span>
    </button>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['upload'])

const fileInput = ref(null)
const selectedFile = ref(null)
const preview = ref(null)
const isDragging = ref(false)

const triggerFileInput = () => {
  fileInput.value.click()
}

const handleFileSelect = (event) => {
  const file = event.target.files[0]
  if (file) {
    processFile(file)
  }
}

const handleDrop = (event) => {
  isDragging.value = false
  const file = event.dataTransfer.files[0]
  if (file && file.type.startsWith('image/')) {
    processFile(file)
  } else {
    alert('请上传图片文件（PNG/JPG）')
  }
}

const processFile = (file) => {
  if (file.size > 10 * 1024 * 1024) {
    alert('文件大小不能超过 10MB')
    return
  }

  selectedFile.value = file

  const reader = new FileReader()
  reader.onload = (e) => {
    preview.value = e.target.result
  }
  reader.readAsDataURL(file)
}

const clearImage = () => {
  selectedFile.value = null
  preview.value = null
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

const handleUpload = () => {
  if (selectedFile.value) {
    emit('upload', selectedFile.value)
  }
}
</script>
