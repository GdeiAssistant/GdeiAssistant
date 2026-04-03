import { describe, expect, it, vi } from 'vitest'
import {
  AUTH_WHITELIST,
  hasToken,
  isWhitelisted,
  resolveNavigationTarget,
  resolveScrollPosition,
} from '../../../src/router/index.js'

describe('router auth helpers', () => {

  it('reads token state from the provided storage object', () => {
    const storage = {
      getItem: vi.fn().mockReturnValue('jwt-token'),
    }

    expect(hasToken(storage)).toBe(true)
    expect(storage.getItem).toHaveBeenCalledWith('token')
    expect(hasToken({ getItem: () => '' })).toBe(false)
  })

  it('matches whitelisted paths by exact match and nested prefixes', () => {
    expect(AUTH_WHITELIST).toContain('/about')
    expect(isWhitelisted('/about')).toBe(true)
    expect(isWhitelisted('/policy/privacy')).toBe(true)
    expect(isWhitelisted('/secret/home')).toBe(false)
  })

  it('resolves navigation redirects for logged-in and anonymous users', () => {
    expect(resolveNavigationTarget('/', false)).toBe('/login')
    expect(resolveNavigationTarget('/', true)).toBe('/home')
    expect(resolveNavigationTarget('/login', true)).toBe('/home')
    expect(resolveNavigationTarget('/about/account', false)).toBeNull()
    expect(resolveNavigationTarget('/secret/home', false)).toBe('/login')
    expect(resolveNavigationTarget('/secret/home', true)).toBeNull()
  })

  it('resets scroll to the top for normal route changes and preserves browser back positions', () => {
    expect(resolveScrollPosition({}, {}, null)).toEqual({ left: 0, top: 0 })
    expect(resolveScrollPosition({}, {}, { left: 0, top: 420 })).toEqual({ left: 0, top: 420 })
  })
})
