import { beforeEach, expect, it, vi } from 'vitest'
const api = vi.hoisted(() => ({ get: vi.fn() }))
vi.mock('@/utils/request', () => ({ default: api }))
vi.mock('@/services/data-source', () => ({ getDataSourceMode: () => 'mock' }))
import { messageUnread, messageBadge, refreshMessageUnread, resetMessageUnread } from './useMessageUnread'
beforeEach(() => { vi.resetAllMocks(); localStorage.setItem('token', 'test'); resetMessageUnread() })
const respond = (direct = 2) => api.get.mockImplementation(path => Promise.resolve({ success: true, data: path.includes('categories') ? { interaction: 3, service: 4 } : path.includes('announcement') ? 5 : { total: direct } }))
it('sums distinct sources and caps only display at 99+', async () => {
 respond(100); await refreshMessageUnread()
 expect(messageUnread.direct).toBe(100); expect(messageBadge.value).toBe('99+')
})
it('preserves each failed source while updating successful ones', async () => {
 respond(); await refreshMessageUnread()
 api.get.mockImplementation(path => path.includes('categories') ? Promise.reject(new Error('offline')) : Promise.resolve({ success: true, data: path.includes('announcement') ? 0 : { total: 0 } }))
 await refreshMessageUnread()
 expect(messageUnread.interaction).toBe(3); expect(messageUnread.service).toBe(4)
 expect(messageUnread.announcement).toBe(0); expect(messageBadge.value).toBe(7)
 expect(messageUnread.errors.service).toBe(true)
})
it('ignores late responses after logout', async () => {
 const complete = []
 api.get.mockImplementation(() => new Promise(resolve => complete.push(resolve)))
 const pending = refreshMessageUnread()
 localStorage.removeItem('token'); resetMessageUnread()
 complete[0]({ success: true, data: { interaction: 99, service: 99 } })
 complete[1]({ success: true, data: 99 })
 complete[2]({ success: true, data: { total: 99 } })
 await pending
 expect(messageBadge.value).toBe(''); expect(messageUnread.direct).toBe(0)
})
