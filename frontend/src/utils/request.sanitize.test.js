import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../router', () => ({
  default: { push: vi.fn(() => Promise.resolve()) }
}))
vi.mock('./toast.js', () => ({
  showErrorTopTips: vi.fn()
}))
vi.mock('../i18n', () => ({
  default: {
    global: {
      t: (key) => ({
        'common.saveFailed': '操作失败',
        'common.systemBusy': '系统繁忙，请稍后再试'
      }[key] || key),
      locale: { value: 'zh-CN' }
    }
  }
}))
vi.mock('../services/data-source.js', () => ({
  isMockMode: () => false
}))
vi.mock('../mock/index.js', () => ({
  handleRequest: vi.fn()
}))
vi.mock('../composables/useSocialRealtime.js', () => ({
  resetSocialRealtimeOnAuthChange: vi.fn()
}))

describe('sanitizeMessage', () => {
  beforeEach(() => {
    vi.resetModules()
  })

  it('keeps friendly english backend messages for social errors', async () => {
    const { sanitizeMessage } = await import('./request.js')
    expect(sanitizeMessage('Unable to send message')).toBe('Unable to send message')
    expect(sanitizeMessage('Failed to save')).toBe('Failed to save')
    expect(sanitizeMessage('메시지를 보낼 수 없습니다')).toBe('메시지를 보낼 수 없습니다')
    expect(sanitizeMessage('You cannot message this user')).toBe('You cannot message this user')
    expect(sanitizeMessage('メッセージを送信できません')).toBe('メッセージを送信できません')
  })

  it('blocks technical leaks and credential-like payloads', async () => {
    const { sanitizeMessage } = await import('./request.js')
    expect(sanitizeMessage('Request failed with status code 500')).toBe('系统繁忙，请稍后再试')
    expect(sanitizeMessage('500 Internal Server Error')).toBe('系统繁忙，请稍后再试')
    expect(sanitizeMessage('AxiosError: Network Error')).toBe('系统繁忙，请稍后再试')
    expect(sanitizeMessage('NullPointerException at SocialController.java:88')).toBe('系统繁忙，请稍后再试')
    expect(sanitizeMessage('Bearer abc.def.ghi')).toBe('系统繁忙，请稍后再试')
    expect(sanitizeMessage('password=super-secret')).toBe('系统繁忙，请稍后再试')
  })
})
