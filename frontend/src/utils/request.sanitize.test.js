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
  it('keeps a replacement session when an old request returns 401', async () => {
    const { default: service } = await import('./request.js')
    localStorage.setItem('token', 'new-session')
    await expect(service.get('/protected', { adapter: async (config) => {
      config.headers.Authorization = 'Bearer old-session'
      const error = new Error('expired')
      error.config = config
      error.response = { status: 401, data: {}, config }
      throw error
    } })).rejects.toThrow('expired')
    expect(localStorage.getItem('token')).toBe('new-session')
  })

  it.each([404, 500, 429])('honors skipErrorTip for HTTP %s and retains business codes', async (status) => {
    const { default: service } = await import('./request.js')
    const { showErrorTopTips } = await import('./toast.js'); vi.mocked(showErrorTopTips).mockClear()
    const error = await service.get('/synthetic', { skipErrorTip: true, adapter: async config => {
      const failure = new Error('synthetic'); failure.config = config
      failure.response = { status, data: { errorCode: 'SYNTHETIC', code: 123 }, config }; throw failure
    } }).catch(value => value)
    expect(showErrorTopTips).not.toHaveBeenCalled(); expect(error.status).toBe(status)
    expect(error.errorCode).toBe('SYNTHETIC'); expect(error.businessCode).toBe(123)
  })
  it('does not show cancellation as a network failure', async () => {
    const { default: service } = await import('./request.js')
    const { showErrorTopTips } = await import('./toast.js'); vi.mocked(showErrorTopTips).mockClear()
    await service.get('/synthetic', { adapter: async config => {
      const error = new Error('cancelled'); error.code = 'ERR_CANCELED'; error.config = config; throw error
    } }).catch(() => {})
    expect(showErrorTopTips).not.toHaveBeenCalled()
  })

})
