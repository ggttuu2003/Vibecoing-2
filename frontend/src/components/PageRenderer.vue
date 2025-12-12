<template>
  <div class="glass-card rounded-xl p-6">
    <div class="flex items-center justify-between mb-4">
      <h3 class="text-xl font-bold text-white">🖥️ 页面预览</h3>
      <button
        v-if="template"
        @click="$emit('fullscreen')"
        class="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-all flex items-center gap-2 hover:scale-105 shadow-lg"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 8V4m0 0h4M4 4l5 5m11-1V4m0 0h-4m4 0l-5 5M4 16v4m0 0h4m-4 0l5-5m11 5l-5-5m5 5v-4m0 4h-4" />
        </svg>
        全屏预览
      </button>
    </div>

    <div
      v-if="template"
      class="border-2 border-white/20 rounded-lg overflow-hidden bg-white/5 backdrop-blur-sm"
      :style="{
        width: '100%',
        maxWidth: '375px',
        margin: '0 auto'
      }"
    >
      <div
        class="relative"
        :style="{
          width: template.page.width + 'px',
          height: template.page.height + 'px',
          backgroundColor: template.page.backgroundColor,
          backgroundImage: template.page.backgroundImage ? `url(${template.page.backgroundImage})` : 'none',
          backgroundSize: 'cover',
          backgroundPosition: 'center',
          backgroundRepeat: 'no-repeat',
          transform: `scale(${scale})`,
          transformOrigin: 'top left'
        }"
      >
        <!-- 渲染所有组件 -->
        <component
          v-for="comp in template.components"
          :key="comp.id"
          :is="getComponentType(comp.type)"
          :component="comp"
        />
      </div>
    </div>

    <div v-else class="text-center py-12 text-white/60">
      <svg class="mx-auto h-12 w-12 mb-4 text-purple-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
      </svg>
      <p class="text-white/80">暂无预览</p>
      <p class="text-sm mt-2 text-white/60">解析完成后将显示页面预览</p>
    </div>
  </div>

  <!-- 全屏预览模态框 -->
  <Teleport to="body">
    <div
      v-if="showFullscreen"
      class="fixed inset-0 z-50 bg-black bg-opacity-90 flex items-center justify-center"
      @click.self="closeFullscreen"
    >
      <div class="relative w-full h-full flex flex-col">
        <!-- 顶部控制栏 -->
        <div class="bg-gray-900 bg-opacity-80 px-6 py-4 flex items-center justify-between">
          <div class="text-white text-sm">
            📐 {{ template.page.width }} × {{ template.page.height }}px |
            📦 {{ template.components.length }} 个组件 |
            🎨 AI 自动生成
          </div>

          <div class="flex items-center gap-4">
            <!-- 缩放控制 -->
            <div class="flex items-center gap-2">
              <span class="text-white text-sm">缩放：</span>
              <button
                v-for="scaleOption in scaleOptions"
                :key="scaleOption"
                @click="currentScale = scaleOption"
                class="px-3 py-1 rounded transition-colors"
                :class="currentScale === scaleOption
                  ? 'bg-purple-600 text-white'
                  : 'bg-gray-700 text-gray-300 hover:bg-gray-600'"
              >
                {{ scaleOption * 100 }}%
              </button>
            </div>

            <!-- 关闭按钮 -->
            <button
              @click="closeFullscreen"
              class="text-white hover:text-red-400 transition-colors"
            >
              <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>

        <!-- 预览内容 -->
        <div class="flex-1 overflow-hidden flex items-center justify-center p-8">
          <div
            class="relative shadow-2xl"
            :style="{
              width: template.page.width + 'px',
              height: template.page.height + 'px',
              backgroundColor: template.page.backgroundColor,
              backgroundImage: template.page.backgroundImage ? `url(${template.page.backgroundImage})` : 'none',
              backgroundSize: 'cover',
              backgroundPosition: 'center',
              backgroundRepeat: 'no-repeat',
              transform: `scale(${currentScale})`,
              transformOrigin: 'center'
            }"
          >
            <!-- 渲染所有组件 -->
            <component
              v-for="comp in template.components"
              :key="comp.id"
              :is="getComponentType(comp.type)"
              :component="comp"
            />
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, computed } from 'vue'
import TextRenderer from './renderers/TextRenderer.vue'
import ButtonRenderer from './renderers/ButtonRenderer.vue'
import ImageRenderer from './renderers/ImageRenderer.vue'

const props = defineProps({
  template: {
    type: Object,
    default: null
  },
  fullscreen: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['fullscreen', 'close-fullscreen'])

const showFullscreen = ref(false)
const currentScale = ref(1)
const scaleOptions = [0.5, 0.75, 1, 1.25, 1.5]

const scale = computed(() => {
  if (!props.template) return 1
  return 375 / props.template.page.width
})

const getComponentType = (type) => {
  const mapping = {
    'text': TextRenderer,
    'button': ButtonRenderer,
    'image': ImageRenderer
  }
  return mapping[type] || 'div'
}

const closeFullscreen = () => {
  showFullscreen.value = false
  currentScale.value = 1
  emit('close-fullscreen')
}

// 监听全屏事件
const openFullscreen = () => {
  showFullscreen.value = true
}

// 暴露给父组件
defineExpose({
  openFullscreen
})
</script>
