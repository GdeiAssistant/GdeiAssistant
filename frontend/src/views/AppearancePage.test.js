import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import { setThemeAccent, setThemeMode } from '@/theme'
import AppearancePage from './AppearancePage.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key) => key,
    locale: { value: 'zh-CN' }
  })
}))

vi.mock('@/i18n', () => ({
  setLocale: vi.fn()
}))

describe('AppearancePage external theme and accent synchronization', () => {
  beforeEach(() => {
    localStorage.clear()
    delete document.documentElement.dataset.theme
    delete document.documentElement.dataset.accent
    window.matchMedia = vi.fn().mockReturnValue({ matches: false, addEventListener: vi.fn() })
  })

  it('syncs active accent selection when setThemeAccent runs externally', async () => {
    setThemeAccent('banyan')
    const wrapper = mount(AppearancePage, {
      global: {
        mocks: {
          $router: { back: vi.fn() }
        }
      }
    })

    const accentButtons = () => wrapper.findAll('.accent-option')
    expect(accentButtons()[0].attributes('aria-checked')).toBe('true')
    expect(accentButtons()[1].attributes('aria-checked')).toBe('false')

    setThemeAccent('kapok')
    await nextTick()
    await Promise.resolve()

    expect(accentButtons()[0].attributes('aria-checked')).toBe('false')
    expect(accentButtons()[1].attributes('aria-checked')).toBe('true')
    expect(accentButtons()[1].classes()).toContain('option-item--active')

    setThemeMode('dark')
    await nextTick()
    await Promise.resolve()

    const themeButtons = wrapper.findAll('.theme-option')
    expect(themeButtons[2].attributes('aria-checked')).toBe('true')

    wrapper.unmount()
  })
})
