<template>
  <div
    class="absolute"
    :style="getStyle()"
  >
    {{ component.content }}
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
    fontSize: (style?.fontSize || 16) + 'px',
    fontWeight: style?.fontWeight || 400,
    color: style?.color || '#000000',
    textAlign: style?.textAlign || 'left',
    lineHeight: style?.lineHeight || 1.5,
    fontFamily: style?.fontFamily || 'inherit',
    textShadow: style?.textShadow || 'none'
  }
}
</script>
