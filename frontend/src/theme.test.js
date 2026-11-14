import { beforeEach, describe, expect, it, vi } from 'vitest'
import {
  THEME_ACCENTS,
  applyThemePreference,
  getFontScaleStep,
  getThemeAccent,
  getThemeMode,
  initTheme,
  setFontScaleStep,
  setThemeAccent,
  setThemeMode
} from './theme'

function mockMatchMedia(matches = false) {
  const addEventListener = vi.fn()
  window.matchMedia = vi.fn().mockReturnValue({
    matches,
    addEventListener
  })
  return { addEventListener }
}

describe('theme preferences and campus accents', () => {
  beforeEach(() => {
    localStorage.clear()
    delete document.documentElement.dataset.theme
    delete document.documentElement.dataset.accent
    document.documentElement.style.fontSize = ''
    mockMatchMedia(false)
  })

  it('exposes all four campus accents with banyan as the default', () => {
    expect(THEME_ACCENTS).toEqual(['banyan', 'kapok', 'haizhu', 'bauhinia'])
    expect(getThemeAccent()).toBe('banyan')
  })

  it.each(['banyan', 'kapok', 'haizhu', 'bauhinia'])(
    'persists and applies campus accent %s to dataset.accent',
    (accent) => {
      const applied = setThemeAccent(accent)
      expect(applied).toBe(accent)
      expect(localStorage.getItem('gdei-theme-accent')).toBe(accent)
      expect(getThemeAccent()).toBe(accent)
      expect(document.documentElement.dataset.accent).toBe(accent)
    }
  )

  it('falls back to banyan for unknown accents', () => {
    localStorage.setItem('gdei-theme-accent', 'unknown-accent')
    expect(getThemeAccent()).toBe('banyan')

    const applied = setThemeAccent('nonexistent')
    expect(applied).toBe('banyan')
    expect(localStorage.getItem('gdei-theme-accent')).toBe('banyan')
    expect(document.documentElement.dataset.accent).toBe('banyan')
  })

  it('initializes theme mode, accent, font scale, and system listener cleanly', () => {
    localStorage.setItem('theme', 'dark')
    localStorage.setItem('gdei-theme-accent', 'kapok')
    localStorage.setItem('font_scale_step', '2')

    initTheme()

    expect(getThemeMode()).toBe('dark')
    expect(document.documentElement.dataset.theme).toBe('dark')
    expect(document.documentElement.dataset.accent).toBe('kapok')
    expect(document.documentElement.style.fontSize).toBe(`${16 * 1.15}px`)
  })

  it('applies explicit mode and accent via applyThemePreference', () => {
    applyThemePreference('light', 'bauhinia')
    expect(document.documentElement.dataset.theme).toBe('light')
    expect(document.documentElement.dataset.accent).toBe('bauhinia')

    setThemeMode('dark')
    expect(document.documentElement.dataset.theme).toBe('dark')

    setFontScaleStep(3)
    expect(getFontScaleStep()).toBe(3)
    expect(document.documentElement.style.fontSize).toBe(`${16 * 1.3}px`)
  })
})
