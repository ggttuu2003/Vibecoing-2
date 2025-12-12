<template>
  <div
    class="absolute overflow-hidden group"
    :style="getStyle()"
  >
    <!-- 图片内容 -->
    <img
      v-if="component.placeholder?.url"
      :src="component.placeholder.url"
      :alt="component.placeholder?.alt || '图片'"
      class="w-full h-full"
      :style="{
        objectFit: component.style?.objectFit || 'cover'
      }"
    >
    <div
      v-else
      class="w-full h-full flex items-center justify-center"
      :style="{
        backgroundColor: component.placeholder?.dominantColor || '#E5E7EB'
      }"
    >
      <div class="text-center">
        <svg class="w-12 h-12 mx-auto text-gray-400 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
        </svg>
        <p class="text-xs text-gray-500">{{ imageTypeText }}</p>
      </div>
    </div>

    <!-- imageType 标签 -->
    <div
      v-if="component.imageType"
      class="absolute top-2 left-2 px-2 py-1 rounded text-xs font-bold text-white shadow-lg transition-all duration-300 opacity-90 group-hover:opacity-100"
      :class="imageTypeClass"
    >
      {{ imageTypeIcon }} {{ imageTypeText }}
    </div>

    <!-- Layer 标签 -->
    <div
      v-if="component.layer !== undefined"
      class="absolute top-2 right-2 px-2 py-1 rounded-full text-xs font-bold bg-black/60 text-white shadow-lg opacity-0 group-hover:opacity-100 transition-opacity"
    >
      Layer {{ component.layer }}
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  component: {
    type: Object,
    required: true
  }
})

const getStyle = () => {
  const { position, size, style } = props.component

  return {
    left: position.x + 'px',
    top: position.y + 'px',
    width: size.width + 'px',
    height: size.height + 'px',
    borderRadius: (style?.borderRadius || 0) + 'px',
    filter: style?.filter || 'none',
    zIndex: props.component.layer || 1
  }
}

// imageType 映射配置
const imageTypeConfig = {
  background: {
    text: '背景图',
    icon: '🌄',
    class: 'bg-red-500'
  },
  decoration: {
    text: '装饰图',
    icon: '🎨',
    class: 'bg-purple-500'
  },
  content: {
    text: '内容图',
    icon: '📦',
    class: 'bg-blue-500'
  }
}

const imageTypeText = computed(() => {
  const type = props.component.imageType || 'content'
  return imageTypeConfig[type]?.text || '图片'
})

const imageTypeIcon = computed(() => {
  const type = props.component.imageType || 'content'
  return imageTypeConfig[type]?.icon || '📷'
})

const imageTypeClass = computed(() => {
  const type = props.component.imageType || 'content'
  return imageTypeConfig[type]?.class || 'bg-blue-500'
})
</script>
