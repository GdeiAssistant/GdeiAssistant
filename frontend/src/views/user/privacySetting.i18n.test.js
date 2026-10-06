import { describe, expect, it } from 'vitest'
import { createPrivacyItems } from './settingsContent.js'

describe('privacy setting label reactivity contract', () => {
  it('keeps status while names are resolved from live t() calls', () => {
    const zh = {
      'privacy.field.faculty': '显示我的院系',
      'privacy.field.major': '显示我的专业'
    }
    const en = {
      'privacy.field.faculty': 'Show my faculty',
      'privacy.field.major': 'Show my major'
    }

    const items = createPrivacyItems((key) => zh[key] || key).map(({ key, status }) => ({ key, status }))
    items[0].status = true

    const labelFor = (localeMap) => items.map((item) => ({
      key: item.key,
      status: item.status,
      name: localeMap[`privacy.field.${item.key}`]
    }))

    expect(labelFor(zh)[0]).toEqual({ key: 'faculty', status: true, name: '显示我的院系' })
    expect(labelFor(en)[0]).toEqual({ key: 'faculty', status: true, name: 'Show my faculty' })
  })
})
