import { reactive, computed } from 'vue'
import request from '@/utils/request'
import { getDataSourceMode } from '@/services/data-source'

export const messageUnread = reactive({ interaction: 0, service: 0, announcement: 0, direct: 0, errors: {} })
export const totalMessageUnread = computed(() => messageUnread.interaction + messageUnread.service + messageUnread.announcement + messageUnread.direct)
export const messageBadge = computed(() => totalMessageUnread.value > 99 ? '99+' : totalMessageUnread.value || '')
let identity = ''
let generation = 0
export function resetMessageUnread() {
  generation++
  identity = ''
  Object.assign(messageUnread, { interaction: 0, service: 0, announcement: 0, direct: 0, errors: {} })
}
export async function refreshMessageUnread() {
  const current = `${localStorage.getItem('token') || ''}:${getDataSourceMode()}`
  if (current !== identity) { resetMessageUnread(); identity = current }
  const epoch = ++generation
  const results = await Promise.allSettled([
    request.get('/information/message/categories/unread'),
    request.get('/information/announcement/unread'),
    request.get('/social/unread')
  ])
  if (epoch !== generation || current !== `${localStorage.getItem('token') || ''}:${getDataSourceMode()}`) return
  const keys = [['interaction', 'service'], ['announcement'], ['direct']]
  results.forEach((result, i) => {
    const response = result.status === 'fulfilled' ? result.value : null
    const values = i === 0 ? response?.data : { [keys[i][0]]: i === 1 ? response?.data : response?.data?.total }
    keys[i].forEach(key => {
      const value = values?.[key]
      const valid = response?.success && Number.isInteger(value) && value >= 0
      messageUnread.errors[key] = !valid
      if (valid) messageUnread[key] = value
    })
  })
}
