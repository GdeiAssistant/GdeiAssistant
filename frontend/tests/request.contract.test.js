import { beforeEach, describe, expect, it, vi } from 'vitest'
const spies = vi.hoisted(() => ({ toast: vi.fn(), push: vi.fn(() => Promise.resolve()), reset: vi.fn() }))
vi.mock('../src/router', () => ({ default: { push: spies.push } }))
vi.mock('../src/utils/toast.js', () => ({ showErrorTopTips: spies.toast }))
vi.mock('../src/i18n', () => ({ default: { global: { t: key => key, locale: { value: 'en' } } } }))
vi.mock('../src/services/data-source.js', () => ({ isMockMode: () => false }))
vi.mock('../src/composables/useSocialRealtime.js', () => ({ resetSocialRealtimeOnAuthChange: spies.reset }))
let service
beforeEach(async () => {
  vi.resetModules(); vi.clearAllMocks(); localStorage.clear(); sessionStorage.clear()
  service = (await import('../src/utils/request.js')).default
})
const response = data => async config => ({ data, status: 200, config })
const failure = status => async config => {
  const error = new Error('synthetic transport failure'); error.config = config
  if (status) error.response = { status, config, data: { errorCode: 'SYNTHETIC_ERROR', code: 123 } }
  throw error
}
describe('request adapter contract', () => {
  it('sends only bearer authentication and explicit client and language headers', async () => {
    localStorage.setItem('token', 'synthetic-session')
    await service.get('/synthetic', { adapter: async config => {
      expect(config.headers.get('Authorization')).toBe('Bearer synthetic-session')
      expect(config.headers.get('token')).toBeUndefined(); expect(config.headers.get('X-Client-Type')).toBe('WEB')
      expect(config.headers.get('Accept-Language')).toBe('en'); expect(config.withCredentials).toBe(false)
      return { data: { success: true, data: [1] }, status: 200, config }
    } }).then(data => expect(data.data).toEqual([1]))
  })
  it.each([undefined, 403, 404, 429, 500, 502, 503, 504, 418])('keeps HTTP %s retry classification and uses friendly hints', async status => {
    const error = await service.get('/synthetic', { adapter: failure(status) }).catch(e => e)
    expect(error.status).toBe(status); expect(error.retryable).toBe(!status || status === 429 || status >= 500)
    expect(error.cancelled).toBe(false); expect(spies.toast).toHaveBeenCalledOnce()
    expect(spies.toast.mock.calls[0][0]).not.toContain('synthetic transport failure')
  })
  it('clears a failed current session once, retaining no cached private data', async () => {
    localStorage.setItem('token', 'synthetic-session'); sessionStorage.setItem('profile', 'synthetic-private-cache')
    await Promise.allSettled([service.get('/protected', { adapter: failure(401) }), service.get('/another', { adapter: failure(401) })])
    expect(localStorage.getItem('token')).toBe(null); expect(sessionStorage.length).toBe(0)
    expect(spies.reset).toHaveBeenCalledOnce(); expect(spies.push).toHaveBeenCalledExactlyOnceWith('/login')
  })
  it('login failure preserves an existing session', async () => {
    localStorage.setItem('token', 'synthetic-session'); await service.post('/auth/login', {}, { adapter: failure(401) }).catch(() => {})
    expect(localStorage.getItem('token')).toBe('synthetic-session'); expect(spies.push).not.toHaveBeenCalled()
  })
  it.each([400300, 400399, 123])('retains business error %s and honors caller-owned hints', async code => {
    const error = await service.get('/synthetic', { skipErrorTip: true, adapter: response({ success: false, code, errorCode: 'SYNTHETIC', message: '合成业务提示' }) }).catch(e => e)
    expect(error.message).toBe('合成业务提示'); expect(error.businessCode).toBe(code); expect(error.status).toBe(200)
    expect(error.errorCode).toBe('SYNTHETIC'); expect(error.retryable).toBe(false); expect(spies.toast).not.toHaveBeenCalled()
  })
  it('handles a business auth expiration and passes binary data through unchanged', async () => {
    localStorage.setItem('token', 'synthetic-session')
    await service.get('/synthetic', { adapter: response({ code: 400302, message: '合成过期提示' }) }).catch(() => {})
    expect(localStorage.getItem('token')).toBe(null); expect(spies.push).toHaveBeenCalledOnce()
    const binary = new Blob(['synthetic-image'])
    expect(await service.get('/avatar', { responseType: 'blob', adapter: response(binary) })).toBe(binary)
  })
})
