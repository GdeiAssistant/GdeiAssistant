import { describe, expect, it } from 'vitest'
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
})
