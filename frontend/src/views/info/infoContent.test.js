import { describe, expect, it } from 'vitest'
import {
  getInfoModuleLabel,
  getInteractionLoadMoreLabel,
  normalizeInfoModule
} from './infoContent'

describe('info localized content', () => {
  const messages = {
    'info.moduleMarketplace': 'Marketplace',
    'info.moduleLostAndFound': 'Lost & Found',
    'info.moduleDating': 'Roommate Match',
    'info.moduleDelivery': 'Delivery',
    'info.moduleSecret': 'Secret',
    'info.moduleExpress': 'Confession Wall',
    'info.moduleTopic': 'Topics',
    'info.modulePhotograph': 'Campus Photography',
    'info.moduleDefault': 'Message',
    'info.loadMore': 'Load More',
    'info.loadingMore': 'Loading...'
  }

  const t = (key) => messages[key] ?? key

  it('normalizes legacy module aliases', () => {
    expect(normalizeInfoModule('ershou')).toBe('marketplace')
    expect(normalizeInfoModule('lost_found')).toBe('lostandfound')
    expect(normalizeInfoModule('roommate')).toBe('dating')
  })

  it('maps modules to localized labels', () => {
    expect(getInfoModuleLabel('secondhand', t)).toBe('Marketplace')
    expect(getInfoModuleLabel('topic', t)).toBe('Topics')
    expect(getInfoModuleLabel('unknown', t)).toBe('Message')
  })

  it('builds localized load more labels', () => {
    expect(getInteractionLoadMoreLabel(false, t)).toBe('Load More')
    expect(getInteractionLoadMoreLabel(true, t)).toBe('Loading...')
  })
})
