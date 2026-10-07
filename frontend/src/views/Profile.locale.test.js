import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { createApp, nextTick, ref } from 'vue'
import { createI18n } from 'vue-i18n'
import { createMemoryHistory, createRouter } from 'vue-router'
import Profile from './Profile.vue'
import LocationPicker from '../components/ui/LocationPicker.vue'
import LoginRecord from './user/LoginRecord.vue'
import DatingPublish from './dating/Publish.vue'
import About from './About.vue'
import { getLocationCatalog } from '../catalog/locationCatalog'
import { getProfileCatalog } from '../catalog/profileCatalog'
import zhCN from '../locales/zh-CN.json'
import zhHK from '../locales/zh-HK.json'
import zhTW from '../locales/zh-TW.json'
import en from '../locales/en.json'
import ja from '../locales/ja.json'
import ko from '../locales/ko.json'

const api = vi.hoisted(() => Object.fromEntries(['getCurrentUserProfile', 'getLocationList', 'getProfileOptions', 'updateIntroduction', 'updateBirthday', 'updateFaculty', 'updateLocation', 'updateHometown', 'updateMajor', 'updateEnrollment', 'updateNickname'].map(name => [name, vi.fn()])))
const social = vi.hoisted(() => ({ fetchSocialMe: vi.fn() }))
const request = vi.hoisted(() => ({ get: vi.fn() }))
vi.mock('../api/user.js', () => api)
vi.mock('../api/social.js', () => social)
vi.mock('../utils/request', () => ({ default: request }))
vi.mock('../composables/useToast', () => ({ useToast: () => ({ success: vi.fn(), error: vi.fn(), loading: vi.fn(), hideLoading: vi.fn() }) }))

const messages = { 'zh-CN': zhCN, 'zh-HK': zhHK, 'zh-TW': zhTW, en, ja, ko }
const codeTree = [{ code: 'CN', children: [{ code: '44', children: [{ code: '1' }, { code: '5' }] }] }]
const mounted = []
const flush = async () => { for (let i = 0; i < 8; i++) { await Promise.resolve(); await nextTick() } }
async function mountPage(component) {
  const router = createRouter({ history: createMemoryHistory(), routes: [{ path: '/:pathMatch(.*)*', component }] })
  await router.push('/profile')
  const i18n = createI18n({ legacy: false, locale: 'zh-CN', messages })
  const host = document.createElement('div')
  document.body.appendChild(host)
  const app = createApp(component).use(router).use(i18n)
  app.mount(host)
  mounted.push(() => { app.unmount(); host.remove() })
  await flush()
  return { host, locale: i18n.global.locale }
}
const buttonWith = text => [...document.querySelectorAll('button')].find(button => button.textContent.trim() === text)

beforeEach(() => {
  vi.resetAllMocks()
  api.getCurrentUserProfile.mockResolvedValue({ success: true, data: {
    username: '20231234', nickname: '用户写的中国广东', introduction: '简体原文不会转换',
    facultyCode: 11, majorCode: 'software_engineering',
    location: { regionCode: 'CN', stateCode: '44', cityCode: '1' },
    hometown: { regionCode: 'CN', stateCode: '44', cityCode: '5' }, ipArea: '广东 广州'
  } })
  api.getProfileOptions.mockResolvedValue({ success: true, data: { faculties: [{ code: 11, majors: ['software_engineering', 'network_engineering'] }] } })
  api.getLocationList.mockResolvedValue({ success: true, data: codeTree })
  social.fetchSocialMe.mockResolvedValue({ data: { id: 'demo' } })
  api.updateMajor.mockResolvedValue({ success: true })
  api.updateLocation.mockResolvedValue({ success: true })
})
afterEach(() => mounted.splice(0).forEach(unmount => unmount()))

describe('system dictionaries remain live when locale changes', () => {
  it('redraws a mounted profile in all six languages while retaining user content', async () => {
    const { host, locale } = await mountPage(Profile)
    const introRow = [...host.querySelectorAll('button')].find(button => button.textContent.includes(zhCN.profile.introduction))
    introRow.click()
    await flush()
    for (const code of Object.keys(messages)) {
      locale.value = code
      await flush()
      const locations = getLocationCatalog(code)
      const dictionary = getProfileCatalog(code)
      expect(host.textContent).toContain(locations.locationLabel('CN', '44', '1'))
      expect(host.textContent).toContain(locations.locationLabel('CN', '44', '5'))
      expect(host.textContent).toContain(dictionary.facultyLabel(11))
      expect(host.textContent).toContain(dictionary.majorLabel(11, 'software_engineering'))
      expect(host.textContent).toContain('用户写的中国广东')
      expect(document.querySelector('textarea').value).toBe('简体原文不会转换')
      expect(host.textContent).toContain(locations.systemAreaLabel('广东 广州'))
    }
    expect(api.getCurrentUserProfile).toHaveBeenCalledTimes(1)
    expect(api.updateLocation).not.toHaveBeenCalled()
    expect(api.updateMajor).not.toHaveBeenCalled()
  }, 15000)

  it('updates an open major picker and saves its stable code after a language change', async () => {
    const { host, locale } = await mountPage(Profile)
    const majorRow = [...host.querySelectorAll('button')].find(button => button.textContent.includes(zhCN.profile.major))
    majorRow.click()
    await flush()
    locale.value = 'ko'
    await flush()
    const label = getProfileCatalog('ko').majorLabel(11, 'network_engineering')
    const choice = buttonWith(label)
    expect(choice).toBeDefined()
    choice.click()
    await flush()
    expect(api.updateMajor).toHaveBeenCalledExactlyOnceWith({ major: 'network_engineering' })
    expect(host.textContent).toContain(label)
  })

  it('keeps the three selected region codes and refreshes every open picker column', async () => {
    const tree = ref(getLocationCatalog('zh-CN').toPickerTree(codeTree))
    const onConfirm = vi.fn()
    const wrapper = { components: { LocationPicker }, setup: () => ({ tree, onConfirm }), template: '<LocationPicker :open="true" :tree="tree" @confirm="onConfirm" />' }
    const { locale } = await mountPage(wrapper)
    for (const label of ['中国', '广东', '广州']) { buttonWith(label).click(); await flush() }
    locale.value = 'zh-HK'
    tree.value = getLocationCatalog('zh-HK').toPickerTree(codeTree)
    await flush()
    for (const label of ['中國', '廣東', '廣州']) expect(buttonWith(label)).toBeDefined()
    expect(buttonWith('广东')).toBeUndefined()
    buttonWith(zhHK.common.confirm).click()
    expect(onConfirm).toHaveBeenCalledWith(expect.objectContaining({
      region: expect.objectContaining({ code: 'CN', name: '中國' }),
      state: expect.objectContaining({ code: '44', name: '廣東' }),
      city: expect.objectContaining({ code: '1', name: '廣州' })
    }))
  })

  it('refreshes login area and device labels while preserving IP, network and unknown areas', async () => {
    request.get.mockResolvedValue({ data: [
      { id: 1, country: '中国', province: '广东', city: '广州', ip: '192.0.2.1', network: '测试网络' },
      { id: 2, area: '广东 广州 / 自定义', ip: '192.0.2.2' }
    ] })
    const { host, locale } = await mountPage(LoginRecord)
    locale.value = 'zh-HK'
    await flush()
    expect(host.textContent).toContain('中國 廣東 廣州')
    expect(host.textContent).toContain('广东 广州 / 自定义')
    expect(host.textContent).toContain('192.0.2.1')
    expect(host.textContent).toContain('测试网络')
    expect(host.textContent).toContain(zhHK.loginRecord.unknownDevice)
    expect(request.get).toHaveBeenCalledTimes(1)
  })

  it('refreshes selected year and gender in the dating form without changing typed content', async () => {
    const { host, locale } = await mountPage(DatingPublish)
    const nickname = host.querySelector('input[type="text"]:not([readonly])')
    nickname.value = '简体姓名'
    nickname.dispatchEvent(new Event('input', { bubbles: true }))
    const selects = host.querySelectorAll('input[readonly]')
    selects[0].click()
    selects[1].click()
    await flush()
    for (const code of Object.keys(messages)) {
      locale.value = code
      await flush()
      expect(selects[0].value).toBe(messages[code].grade.year.freshman)
      expect(selects[1].value).toBe(messages[code].dating.publish.genderFemale)
      expect(nickname.value).toBe('简体姓名')
      expect(host.textContent).toContain(messages[code].dating.publish.title)
    }
    selects[0].click()
    await flush()
    expect(selects[0].value).toBe(ko.grade.year.sophomore)
  })

  it('localizes About link and media labels in Japanese and Korean', async () => {
    const { host, locale } = await mountPage(About)
    host.querySelector('.about-menu-button').click()
    const linksMenu = [...host.querySelectorAll('.about-menu-group__title')].find(button => button.textContent.includes(zhCN.about.menuFriendlyLinks))
    linksMenu.click()
    await flush()
    for (const code of ['ja', 'ko']) {
      locale.value = code
      await flush()
      expect(host.textContent).toContain(messages[code].about.links.gdEducation)
      expect(host.querySelector('.about-media-link').getAttribute('title')).toBe(messages[code].about.media.wechat)
    }
  })
})
