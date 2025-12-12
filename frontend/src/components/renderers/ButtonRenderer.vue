<template>
  <button
    class="absolute flex items-center justify-center transition-all duration-200"
    :style="getStyle()"
    @mouseenter="isHovered = true"
    @mouseleave="isHovered = false"
  >
    {{ component.text }}
  </button>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  component: {
    type: Object,
    required: true
  }
})

const isHovered = ref(false)

const getStyle = () => {
  const { position, size, style } = props.component

  const baseStyle = {
    left: position.x + 'px',
    top: position.y + 'px',
    width: size.width + 'px',
    height: size.height + 'px',
    backgroundColor: style?.backgroundColor || '#3B82F6',
    color: style?.textColor || '#FFFFFF',
    fontSize: (style?.fontSize || 16) + 'px',
    fontWeight: style?.fontWeight || 600,
    borderRadius: (style?.borderRadius || 8) + 'px',
    border: style?.border || 'none',
    boxShadow: style?.boxShadow || '0 2px 4px rgba(0,0,0,0.1)',
    cursor: 'pointer'
  }

  if (isHovered.value && style?.hover) {
    return {
      ...baseStyle,
      backgroundColor: style.hover.backgroundColor || baseStyle.backgroundColor,
      transform: style.hover.transform || 'none'
    }
  }

  return baseStyle
}
</script>
