import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import { createI18n } from 'vue-i18n'
import AppLoading from './AppLoading.vue'

const i18n = createI18n({ legacy: false, locale: 'en', messages: { en: { common: { loading: 'Loading…' } } } })

describe('AppLoading', () => {
  it('announces the label as a status and hides the bars from assistive tech', () => {
    const wrapper = mount(AppLoading, { props: { label: 'Loading news' }, global: { plugins: [i18n] } })
    expect(wrapper.attributes('role')).toBe('status')
    expect(wrapper.find('.sr-only').text()).toBe('Loading news')
    expect(wrapper.find('[aria-hidden="true"]').exists()).toBe(true)
  })

  it('falls back to the shared common.loading text and renders the requested bar count', () => {
    const wrapper = mount(AppLoading, { props: { lines: 2 }, global: { plugins: [i18n] } })
    expect(wrapper.find('.sr-only').text()).toBe('Loading…')
    expect(wrapper.findAll('.animate-pulse')).toHaveLength(2)
  })
})
