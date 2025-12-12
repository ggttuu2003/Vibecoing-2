<template>
  <Teleport to="body">
    <Transition name="toast">
      <div
        v-if="show"
        class="fixed top-8 left-1/2 transform -translate-x-1/2 z-50 px-6 py-4 rounded-xl shadow-2xl backdrop-blur-lg flex items-center gap-3 min-w-[300px]"
        :class="typeClass"
      >
        <!-- 图标 -->
        <div class="flex-shrink-0">
          <svg v-if="type === 'success'" class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
          </svg>
          <svg v-else-if="type === 'error'" class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
          <svg v-else class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>

        <!-- 消息 -->
        <div class="flex-1 font-medium">
          {{ message }}
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  message: {
    type: String,
    required: true
  },
  type: {
    type: String,
    default: 'success',
    validator: (value) => ['success', 'error', 'info'].includes(value)
  },
  duration: {
    type: Number,
    default: 3000
  },
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const show = ref(props.modelValue)

watch(() => props.modelValue, (newValue) => {
  show.value = newValue
  if (newValue && props.duration > 0) {
    setTimeout(() => {
      show.value = false
      emit('update:modelValue', false)
    }, props.duration)
  }
})

const typeClass = {
  success: 'bg-green-500/90 text-white',
  error: 'bg-red-500/90 text-white',
  info: 'bg-blue-500/90 text-white'
}[props.type]
</script>

<style scoped>
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

.toast-enter-from {
  opacity: 0;
  transform: translate(-50%, -20px) scale(0.9);
}

.toast-leave-to {
  opacity: 0;
  transform: translate(-50%, -20px) scale(0.9);
}
</style>
