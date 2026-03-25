import { ref } from 'vue'
import i18n from '../i18n'

const toasts = ref([])
let id = 0

export function resolveLoadingMessage(message, t = i18n.global.t.bind(i18n.global)) {
  return message || t('common.loading')
}

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
  function loading(message) { toast({ message: resolveLoadingMessage(message), type: 'loading', duration: 60000 }) }
  function hideLoading() { toasts.value = toasts.value.filter(t => t.type !== 'loading') }

  return { toasts, toast, success, error, loading, hideLoading }
}
