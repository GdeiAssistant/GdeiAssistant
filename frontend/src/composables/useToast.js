import { ref } from 'vue'

const toasts = ref([])
let id = 0

export function useToast() {
  function toast({ message, type = 'default', duration = 3000 }) {
    const toastId = ++id
    toasts.value.push({ id: toastId, message, type })
    setTimeout(() => {
      toasts.value = toasts.value.filter(t => t.id !== toastId)
    }, duration)
  }

  function success(message) { toast({ message, type: 'success' }) }
  function error(message) { toast({ message, type: 'error' }) }
  function loading(message = '加载中...') { toast({ message, type: 'loading', duration: 60000 }) }
  function hideLoading() { toasts.value = toasts.value.filter(t => t.type !== 'loading') }

  return { toasts, toast, success, error, loading, hideLoading }
}
