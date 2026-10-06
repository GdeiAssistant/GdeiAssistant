import { describe, expect, it } from 'vitest'
import { localizeMockValue, normalizeMockLocale } from './mock-i18n'

describe('mock i18n', () => {
  it('normalizes all six supported locales without collapsing ja/ko to english', () => {
    expect(normalizeMockLocale('zh-CN')).toBe('zh-CN')
    expect(normalizeMockLocale('zh_HK')).toBe('zh-HK')
    expect(normalizeMockLocale('zh-Hant-MO')).toBe('zh-HK')
    expect(normalizeMockLocale('ja-JP')).toBe('ja')
    expect(normalizeMockLocale('ko-KR')).toBe('ko')
    expect(normalizeMockLocale('en-US')).toBe('en')
  })

  it('localizes system time labels but keeps article titles and user content intact', () => {
    const payload = {
      title: '系统维护通知',
      nickname: '林知远',
      list: [
        { content: '今天终于把小程序的 mock 流程跑通了，开心。' },
        '刚刚'
      ]
    }

    expect(localizeMockValue(payload, 'en')).toEqual({
      title: '系统维护通知',
      nickname: '林知远',
      list: [
        { content: '今天终于把小程序的 mock 流程跑通了，开心。' },
        'Just now'
      ]
    })
  })

  it('preserves names and titles that happen to equal known system translations in every locale', () => {
    const payload = {
      name: '林知远',
      title: '系统维护通知',
      articles: [{ title: '无法发送私信', content: '系统维护通知' }],
      book: { name: '系统维护通知', title: '登录凭证已过期，请重新登录', body: '林知远' }
    }
    for (const locale of ['zh-CN', 'zh-HK', 'zh-TW', 'en', 'ja', 'ko']) {
      expect(localizeMockValue(payload, locale)).toEqual(payload)
      expect(localizeMockValue(payload.name, locale, 'name')).toBe(payload.name)
      expect(localizeMockValue(payload.title, locale, 'title')).toBe(payload.title)
    }
    expect(localizeMockValue(['校园代步', '待接单'], 'en')).toEqual(['Campus Transportation', 'Pending'])
    expect(localizeMockValue('无法发送私信', 'ja')).toBe('メッセージを送信できません')
  })

  it('uses japanese system errors instead of english fallback', () => {
    expect(localizeMockValue('登录凭证已过期，请重新登录', 'ja')).toContain('ログイン')
    expect(localizeMockValue('无法发送私信', 'ja')).toBe('メッセージを送信できません')
    expect(localizeMockValue('无法发送私信', 'ja')).not.toBe('Unable to send message')
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
