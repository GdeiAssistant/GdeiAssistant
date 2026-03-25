import { describe, expect, it } from 'vitest'
import { createPrivacyItems } from './settingsContent'

describe('settings localized content', () => {
  const messages = {
    'privacy.field.faculty': 'Show my faculty',
    'privacy.field.robots': 'Allow search engines to link to my profile'
  }

  const t = (key) => messages[key] ?? key

  it('builds localized privacy items', () => {
    const items = createPrivacyItems(t)
    expect(items[0]).toEqual({ key: 'faculty', name: 'Show my faculty', status: false })
    expect(items.at(-1)).toEqual({ key: 'robots', name: 'Allow search engines to link to my profile', status: false })
  })
})
