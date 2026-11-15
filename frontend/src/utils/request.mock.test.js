import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../router', () => ({
  default: { push: vi.fn(() => Promise.resolve()) }
}))
vi.mock('./toast.js', () => ({
  showErrorTopTips: vi.fn()
}))
vi.mock('../composables/useSocialRealtime.js', () => ({
  resetSocialRealtimeOnAuthChange: vi.fn()
}))

import service from './request.js'
import { MOCK_ACCOUNT_DATA } from '../mock/mock-data.js'

describe('createMockAdapter form encoding support', () => {
  beforeEach(async () => {
    localStorage.clear()
    localStorage.setItem('gdei_data_source_mode', 'mock')
    const loginRes = await service.post('/auth/login', {
      username: MOCK_ACCOUNT_DATA.username,
      password: MOCK_ACCOUNT_DATA.password,
      campusCredentialConsent: true,
      policyDate: '2026-04-25',
      effectiveDate: '2026-05-11'
    })
    localStorage.setItem('token', loginRes.data.token)
  })

  it('parses FormData payload for /secret/info in mock mode', async () => {
    const payload = new FormData()
    payload.append('content', 'Mock form-data secret')
    payload.append('theme', '3')
    payload.append('type', '0')
    payload.append('timer', '0')

    const res = await service.post('/secret/info', payload)
    expect(res.success).toBe(true)

    const listRes = await service.get('/secret/info/start/0/size/5')
    expect(listRes.data[0].content).toBe('Mock form-data secret')
  })

  it('parses URLSearchParams payload for /data/electricfees and /express in mock mode', async () => {
    const elecParams = new URLSearchParams()
    elecParams.append('name', '林知远')
    elecParams.append('number', '20231234567')
    elecParams.append('year', '2026')

    const elecRes = await service.post('/data/electricfees', elecParams)
    expect(elecRes.success).toBe(true)
    expect(elecRes.data.name).toBe('林知远')
    expect(elecRes.data.number).toBe(20231234567)

    const expressParams = new URLSearchParams()
    expressParams.append('nickname', '测试昵称')
    expressParams.append('realname', '')
    expressParams.append('selfGender', '0')
    expressParams.append('name', '同学A')
    expressParams.append('personGender', '1')
    expressParams.append('content', 'Mock urlencoded express')

    const expressRes = await service.post('/express', expressParams)
    expect(expressRes.success).toBe(true)

    const expressList = await service.get('/express/start/0/size/5')
    expect(expressList.data[0].content).toBe('Mock urlencoded express')
  })

  it('keeps FormData intact for social chat image upload in mock mode', async () => {
    const mockModule = await import('../mock/index.js')
    const spy = vi.spyOn(mockModule, 'handleRequest').mockResolvedValueOnce({
      success: true,
      code: 200,
      data: { messageId: 'm-1' }
    })

    const formData = new FormData()
    formData.append('image', new Blob(['fake-image'], { type: 'image/png' }), 'chat.png')

    const res = await service.post('/social/conversations/conv-1/messages/image', formData)
    expect(res.success).toBe(true)
    expect(spy).toHaveBeenCalledTimes(1)
    expect(spy.mock.calls[0][0].data).toBeInstanceOf(FormData)
    expect(spy.mock.calls[0][0].data).toBe(formData)
  })
})
