import { describe, expect, it } from 'vitest'
import { createCardActions, formatCardAmount } from './cardContent'

describe('card localized content', () => {
  const messages = {
    'card.action.info.title': 'Basic Info',
    'card.action.info.description': 'View balance, status, and loss status',
    'card.action.info.badge': 'Info',
    'card.amountUnit': ' CNY'
  }

  const t = (key) => messages[key] ?? key

  it('builds localized card actions', () => {
    expect(createCardActions(t)[0]).toEqual({
      id: 'info',
      title: 'Basic Info',
      description: 'View balance, status, and loss status',
      path: '/card/info',
      badge: 'Info'
    })
  })

  it('formats transaction amounts with translated unit', () => {
    expect(formatCardAmount('100', t)).toBe('+100 CNY')
    expect(formatCardAmount('-13.5', t)).toBe('-13.5 CNY')
  })
})
