import { describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import AppButton from './AppButton.vue'
import AppInput from './AppInput.vue'

describe('AppInput', () => {
  it('sends layout class to the wrapper and native attributes and listeners to the input', async () => {
    const onKeyup = vi.fn()
    const wrapper = mount(AppInput, {
      props: { modelValue: 'a' },
      attrs: { class: 'mt-4 flex-1', maxlength: '20', inputmode: 'numeric', onKeyup },
    })
    expect(wrapper.classes()).toContain('mt-4')
    const input = wrapper.find('input')
    expect(input.classes()).not.toContain('mt-4')
    expect(input.attributes('maxlength')).toBe('20')
    expect(input.attributes('inputmode')).toBe('numeric')
    await input.trigger('keyup', { key: 'Enter' })
    expect(onKeyup).toHaveBeenCalledTimes(1)
  })

  it('emits update:modelValue and also calls a parent @input listener', async () => {
    const onInput = vi.fn()
    const wrapper = mount(AppInput, { props: { modelValue: '' }, attrs: { onInput } })
    await wrapper.find('input').setValue('x')
    expect(wrapper.emitted('update:modelValue')[0]).toEqual(['x'])
    expect(onInput).toHaveBeenCalledTimes(1)
  })
})

describe('AppButton', () => {
  it('keeps native type, click and disabled semantics', async () => {
    const onClick = vi.fn()
    const wrapper = mount(AppButton, { attrs: { type: 'button', onClick }, slots: { default: 'Go' } })
    expect(wrapper.attributes('type')).toBe('button')
    await wrapper.trigger('click')
    expect(onClick).toHaveBeenCalledTimes(1)
    await wrapper.setProps({ disabled: true })
    expect(wrapper.attributes('disabled')).toBeDefined()
    expect(wrapper.text()).toBe('Go')
  })

  it('sm keeps a 36px visual with a 44px hit area', () => {
    const wrapper = mount(AppButton, { props: { size: 'sm' } })
    expect(wrapper.classes()).toContain('min-h-9')
    expect(wrapper.classes()).toContain('before:-inset-y-1')
  })
})
