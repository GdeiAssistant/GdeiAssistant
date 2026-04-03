import { describe, expect, it, vi } from 'vitest'
import { createI18n } from 'vue-i18n'
import zhCN from '../../locales/zh-CN.json'
import en from '../../locales/en.json'
import { createFaqList, createFeedbackTypes } from './feedbackContent'

describe('feedback localized content', () => {
  const messages = {
    'feedback.faq.login.title': 'What are the login credentials?',
    'feedback.faq.login.content': 'Use the campus account credentials.',
    'feedback.type.appError': 'App errors',
    'feedback.type.other': 'Other'
  }

  const t = (key) => messages[key] ?? key

  it('builds localized faq entries', () => {
    const faqList = createFaqList(t)
    expect(faqList[0]).toEqual({
      title: 'What are the login credentials?',
      content: 'Use the campus account credentials.'
    })
  })

  it('builds localized feedback types', () => {
    const types = createFeedbackTypes(t)
    expect(types).toContain('App errors')
    expect(types).toContain('Other')
  })

  it('does not trigger vue-i18n linked-message parse errors for support email', () => {
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {})
    const i18n = createI18n({
      legacy: false,
      locale: 'zh-CN',
      fallbackLocale: 'zh-CN',
      messages: {
        'zh-CN': zhCN,
        en
      }
    })

    try {
      const zhContent = createFaqList(i18n.global.t.bind(i18n.global))[4].content
      expect(zhContent).toContain('support@gdeiassistant.cn')
      expect(consoleError).not.toHaveBeenCalled()

      i18n.global.locale.value = 'en'
      const enContent = createFaqList(i18n.global.t.bind(i18n.global))[4].content
      expect(enContent).toContain('support@gdeiassistant.cn')
      expect(consoleError).not.toHaveBeenCalled()
    } finally {
      consoleError.mockRestore()
    }
  })
})
