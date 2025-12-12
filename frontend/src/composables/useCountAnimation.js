import { ref, watch } from 'vue'

/**
 * 数字滚动动画 Hook
 * @param {Number} target - 目标数值
 * @param {Number} duration - 动画时长（毫秒）
 * @returns {Object} - { displayValue: ref }
 */
export function useCountAnimation(target, duration = 1000) {
  const displayValue = ref(0)
  const frameRate = 1000 / 60 // 60fps
  const totalFrames = Math.round(duration / frameRate)

  const animate = (endValue) => {
    let currentFrame = 0
    const increment = endValue / totalFrames

    const counter = setInterval(() => {
      currentFrame++
      displayValue.value = Math.round(increment * currentFrame)

      if (currentFrame === totalFrames) {
        displayValue.value = endValue
        clearInterval(counter)
      }
    }, frameRate)
  }

  watch(() => target.value, (newValue) => {
    if (newValue !== undefined && newValue !== null) {
      animate(newValue)
    }
  }, { immediate: true })

  return {
    displayValue
  }
}
