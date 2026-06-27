import { ref, onMounted, onUnmounted } from 'vue'

/**
 * 屏幕尺寸检测 composable
 * 断点：>=1200px 桌面、768-1199px 平板、<768px 移动端
 */
export function useScreenSize() {
  const width = ref(window.innerWidth)

  const isMobile = ref(width.value < 768)
  const isTablet = ref(width.value >= 768 && width.value < 1200)
  const isDesktop = ref(width.value >= 1200)

  function update() {
    width.value = window.innerWidth
    isMobile.value = width.value < 768
    isTablet.value = width.value >= 768 && width.value < 1200
    isDesktop.value = width.value >= 1200
  }

  onMounted(() => window.addEventListener('resize', update))
  onUnmounted(() => window.removeEventListener('resize', update))

  return { width, isMobile, isTablet, isDesktop }
}
