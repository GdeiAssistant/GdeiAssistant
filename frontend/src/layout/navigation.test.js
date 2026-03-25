import { describe, expect, it } from 'vitest'
import { createFooterItems, createNavItems, resolveRouteTitle } from './navigation'

describe('layout navigation i18n', () => {
  const messages = {
    'tab.home': 'Home',
    'tab.info': 'Info',
    'sidebar.settings': 'Settings',
    'sidebar.about': 'About',
    'profile.title': 'Profile',
    'profile.avatar': 'Avatar'
  }

  const t = (key) => messages[key] ?? key

  it('creates sidebar labels from translation keys', () => {
    expect(createNavItems(t).map((item) => item.label)).toEqual(['Home', 'Info'])
    expect(createFooterItems(t).map((item) => item.label)).toEqual(['Settings', 'About'])
  })

  it('prefers translated titleKey for route titles', () => {
    expect(resolveRouteTitle({ meta: { titleKey: 'profile.title' }, name: 'Profile' }, t)).toBe('Profile')
    expect(resolveRouteTitle({ meta: { titleKey: 'profile.avatar', title: '头像管理' } }, t)).toBe('Avatar')
  })
})
