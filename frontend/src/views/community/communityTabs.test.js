import { describe, expect, it } from 'vitest'
import { createCommunityTabs } from './communityTabs'

describe('community tabs', () => {
  const messages = {
    'communityTab.home': 'Home',
    'communityTab.publish': 'Publish',
    'communityTab.profile': 'My',
    'communityTab.search': 'Search',
    'communityTab.hall': 'Hall',
    'communityTab.confess': 'Confess'
  }

  const t = (key) => messages[key] ?? key

  it('builds marketplace tabs from i18n labels', () => {
    expect(createCommunityTabs('marketplace', t).map((tab) => tab.label)).toEqual([
      'Home',
      'Publish',
      'My'
    ])
  })

  it('builds delivery tabs from i18n labels', () => {
    expect(createCommunityTabs('delivery', t).map((tab) => tab.label)).toEqual([
      'Hall',
      'Publish',
      'My'
    ])
  })

  it('builds express tabs from i18n labels', () => {
    expect(createCommunityTabs('express', t).map((tab) => tab.label)).toEqual([
      'Home',
      'Confess',
      'Search'
    ])
  })
})
