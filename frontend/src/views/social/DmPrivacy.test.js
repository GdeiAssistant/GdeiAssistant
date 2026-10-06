import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { createApp, nextTick } from 'vue'
import { createI18n } from 'vue-i18n'
import { createMemoryHistory, createRouter } from 'vue-router'
import DmPrivacy from './DmPrivacy.vue'
import zhCN from '../../locales/zh-CN.json'

const api = vi.hoisted(() => ({ fetchDmPrivacy: vi.fn(), updateDmPrivacy: vi.fn() }))
const toast = vi.hoisted(() => ({ success: vi.fn(), error: vi.fn() }))
vi.mock('../../api/social.js', () => api)
vi.mock('../../composables/useToast.js', () => ({ useToast: () => toast }))

const mounted = []
const flush = async () => { await Promise.resolve(); await nextTick() }
function pending() {
  let resolve, reject
  const promise = new Promise((yes, no) => { resolve = yes; reject = no })
  return { promise, resolve, reject }
}
async function mountPage() {
  const router = createRouter({ history: createMemoryHistory(), routes: [{ path: '/:pathMatch(.*)*', component: DmPrivacy }] })
  await router.push('/social/privacy')
  await router.isReady()
  const host = document.createElement('div')
  document.body.appendChild(host)
  const app = createApp(DmPrivacy).use(router).use(createI18n({ legacy: false, locale: 'zh-CN', messages: { 'zh-CN': zhCN } }))
  app.mount(host)
  mounted.push(() => { app.unmount(); host.remove() })
  await flush()
  return host
}
const saveButton = host => [...host.querySelectorAll('button')].find(button => button.textContent.trim() === zhCN.common.save)
async function choose(host, policy) {
  const input = host.querySelector(`input[value="${policy}"]`)
  input.checked = true
  input.dispatchEvent(new Event('change', { bubbles: true }))
  await nextTick()
}

beforeEach(() => vi.resetAllMocks())
afterEach(() => { mounted.splice(0).forEach(unmount => unmount()) })

describe('DM privacy request ordering', () => {
  it('waits for the actual policy before allowing changes or writes', async () => {
    const load = pending()
    api.fetchDmPrivacy.mockReturnValue(load.promise)
    const host = await mountPage()
    expect([...host.querySelectorAll('input')].every(input => input.disabled)).toBe(true)
    expect(saveButton(host).disabled).toBe(true)
    saveButton(host).click()
    expect(api.updateDmPrivacy).not.toHaveBeenCalled()
    load.resolve({ data: { dmPolicy: 'FOLLOWING' } })
    await flush()
    expect(host.querySelector('input[value="FOLLOWING"]').checked).toBe(true)
    expect(saveButton(host).disabled).toBe(false)
    api.updateDmPrivacy.mockResolvedValue({ data: { dmPolicy: 'NONE' } })
    await choose(host, 'NONE')
    saveButton(host).click()
    await flush()
    expect(api.updateDmPrivacy).toHaveBeenCalledExactlyOnceWith('NONE')
    expect(host.querySelector('input[value="NONE"]').checked).toBe(true)
  })

  it('keeps failed loads unwritable and permits an explicit retry', async () => {
    api.fetchDmPrivacy.mockRejectedValueOnce(new Error('offline')).mockResolvedValueOnce({ data: { dmPolicy: 'NONE' } })
    const host = await mountPage()
    expect(saveButton(host).disabled).toBe(true)
    expect(toast.error).toHaveBeenCalledWith(zhCN.communityCommon.loadFailed)
    saveButton(host).click()
    expect(api.updateDmPrivacy).not.toHaveBeenCalled()
    const retry = [...host.querySelectorAll('button')].find(button => button.textContent.trim() === zhCN.common.retry)
    expect(retry).toBeDefined()
    retry.click()
    await flush()
    expect(host.querySelector('input[value="NONE"]').checked).toBe(true)
    expect(saveButton(host).disabled).toBe(false)
  })

  it('prevents concurrent saves and editing until the accepted policy is known', async () => {
    api.fetchDmPrivacy.mockResolvedValue({ data: { dmPolicy: 'MUTUAL' } })
    const saving = pending()
    api.updateDmPrivacy.mockReturnValue(saving.promise)
    const host = await mountPage()
    await choose(host, 'NONE')
    saveButton(host).click()
    await nextTick()
    saveButton(host).click()
    expect([...host.querySelectorAll('input')].every(input => input.disabled)).toBe(true)
    expect(api.updateDmPrivacy).toHaveBeenCalledExactlyOnceWith('NONE')
    saving.resolve({ data: { dmPolicy: 'NONE' } })
    await flush()
    expect(saveButton(host).disabled).toBe(false)
    expect(toast.success).toHaveBeenCalledWith(zhCN.common.saveSuccess)
  })

  it('retains a rejected draft and allows the same choice to be retried', async () => {
    api.fetchDmPrivacy.mockResolvedValue({ data: { dmPolicy: 'MUTUAL' } })
    api.updateDmPrivacy.mockRejectedValueOnce(new Error('offline')).mockResolvedValueOnce({ data: { dmPolicy: 'NONE' } })
    const host = await mountPage()
    await choose(host, 'NONE')
    saveButton(host).click()
    await flush()
    expect(host.querySelector('input[value="NONE"]').checked).toBe(true)
    expect(saveButton(host).disabled).toBe(false)
    expect(toast.error).toHaveBeenCalledWith(zhCN.common.saveFailed)
    saveButton(host).click()
    await flush()
    expect(api.updateDmPrivacy).toHaveBeenNthCalledWith(2, 'NONE')
  })
})
