import { ref } from 'vue'

export function useScrollLoad(fetchDataCallback) {
  const items = ref([])
  const page = ref(1)
  const loading = ref(false)
  const finished = ref(false)
  const refreshing = ref(false)
  const pullY = ref(0)
  let startY = 0

  const loadData = async (isRefresh = false) => {
    if (isRefresh) {
      page.value = 1
      refreshing.value = true
      finished.value = false
    } else {
      if (finished.value || loading.value) return
      loading.value = true
    }

    try {
      const currentPage = page.value
      const res = await fetchDataCallback(currentPage)
      const data = res?.data || res
      const list = data?.list || data?.data || []
      const hasMore = data?.hasMore !== undefined ? data.hasMore : (list.length > 0 && list.length >= 10)

      if (isRefresh) {
        items.value = list
        refreshing.value = false
        pullY.value = 0
        if (!hasMore || list.length === 0) {
          finished.value = true
        } else {
          finished.value = false
          page.value = 2
        }
      } else {
        items.value = [...items.value, ...list]
        loading.value = false
        if (!hasMore || list.length === 0) {
          finished.value = true
        } else {
          page.value = currentPage + 1
        }
      }
    } catch (e) {
      if (isRefresh) {
        refreshing.value = false
        pullY.value = 0
      } else {
        loading.value = false
      }
    }
  }

  const handleScroll = (e) => {
    const { scrollTop, clientHeight, scrollHeight } = e.target
    if (scrollTop + clientHeight >= scrollHeight - 50 && !loading.value && !finished.value && !refreshing.value) {
      loadData()
    }
  }

  const handleTouchStart = (e) => {
    startY = e.touches[0].clientY
  }

  const handleTouchMove = (e, container) => {
    const el = container?.value || container
    if (el && el.scrollTop > 0) return
    const currentY = e.touches[0].clientY
    const diff = currentY - startY
    if (diff > 0) {
      e.preventDefault()
      pullY.value = Math.min(diff * 0.4, 80)
    }
  }

  const handleTouchEnd = () => {
    if (pullY.value > 50 && !refreshing.value) {
      loadData(true)
    } else {
      pullY.value = 0
    }
  }

  return {
    items,
    loading,
    finished,
    refreshing,
    pullY,
    loadData,
    handleScroll,
    handleTouchStart,
    handleTouchMove,
    handleTouchEnd
  }
}
