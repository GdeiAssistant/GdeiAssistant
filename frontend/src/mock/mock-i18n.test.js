import { describe, expect, it } from 'vitest'
import { localizeMockValue, normalizeMockLocale } from './mock-i18n'

describe('mock i18n', () => {
  it('normalizes locale with english fallback for non-chinese locales', () => {
    expect(normalizeMockLocale('zh-CN')).toBe('zh-CN')
    expect(normalizeMockLocale('ja-JP')).toBe('en')
    expect(normalizeMockLocale('ko-KR')).toBe('en')
    expect(normalizeMockLocale('en-US')).toBe('en')
  })

  it('localizes nested mock payload values recursively', () => {
    const payload = {
      title: '系统维护通知',
      list: [
        { content: '今天终于把小程序的 mock 流程跑通了，开心。' },
        '刚刚'
      ]
    }

    expect(localizeMockValue(payload, 'en')).toEqual({
      title: 'System Maintenance Notice',
      list: [
        { content: 'Finally got the mini-app mock flow working today. So happy.' },
        'Just now'
      ]
    })
  })

  it('localizes option and status values used by mock profile and community data', () => {
    expect(localizeMockValue(['校园代步', '教育学院', '待接单'], 'en')).toEqual([
      'Campus Transportation',
      'School of Education',
      'Pending'
    ])
  })

  it('localizes academic mock helper text and statuses', () => {
    expect(localizeMockValue('账号：gdeiassistant  密码：gdeiassistant  图书馆密码：library123', 'en')).toBe(
      'Account: gdeiassistant  Password: gdeiassistant  Library Password: library123'
    )
    expect(localizeMockValue(['正常', '已挂失', '广东第二师范学院', '英语六级'], 'en')).toEqual([
      'Normal',
      'Reported Lost',
      'Guangdong University of Education',
      'CET-6'
    ])
  })

  it('keeps original text when translation is missing', () => {
    expect(localizeMockValue('暂未配置的文案', 'en')).toBe('暂未配置的文案')
    expect(localizeMockValue('系统维护通知', 'zh-CN')).toBe('系统维护通知')
  })
})
