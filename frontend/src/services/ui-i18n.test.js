import { describe, expect, it, vi } from 'vitest'
import { createApp, defineComponent, h, nextTick } from 'vue'
import { createI18n } from 'vue-i18n'
import { createRouter, createMemoryHistory } from 'vue-router'
import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { parse as parseSfc } from '@vue/compiler-sfc'
import { parse as parseTemplate, NodeTypes } from '@vue/compiler-dom'
import CommunityHeader from '../components/community/CommunityHeader.vue'
import CommunityModuleLayout from '../components/community/CommunityModuleLayout.vue'
import Evaluate from '../views/evaluate/Evaluate.vue'
import SecretDetail from '../views/secret/Detail.vue'
import { getDataSourceLabel } from './data-source'
import { resolveLoadingMessage } from '../composables/useToast'
import zhCN from '../locales/zh-CN.json'
import zhHK from '../locales/zh-HK.json'
import zhTW from '../locales/zh-TW.json'
import en from '../locales/en.json'
import ja from '../locales/ja.json'
import ko from '../locales/ko.json'

const LOCALES = {
  'zh-CN': zhCN,
  'zh-HK': zhHK,
  'zh-TW': zhTW,
  en,
  ja,
  ko
}

const LOCALE_CODES = Object.keys(LOCALES)
const request = vi.hoisted(() => ({ get: vi.fn(), post: vi.fn() }))
vi.mock('../utils/request', () => ({ default: request }))

async function mountLocalized(component, path = '/') {
  const i18n = createI18n({ legacy: false, locale: 'zh-CN', fallbackLocale: 'zh-CN', messages: LOCALES })
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [{ path: '/:pathMatch(.*)*', component: { render: () => null } }, { path: '/secret/id/:id', component: { render: () => null } }]
  })
  await router.push(path)
  await router.isReady()
  const host = document.createElement('div')
  document.body.appendChild(host)
  const app = createApp(component).use(i18n).use(router)
  app.mount(host)
  await new Promise(resolve => setTimeout(resolve, 0))
  await nextTick()
  return { host, i18n, unmount: () => { app.unmount(); host.remove() } }
}

describe('shared ui i18n helpers', () => {
  const messages = {
    'common.loading': 'Loading...',
    'dataSource.mock': 'Mock Data Source',
    'dataSource.remote': 'Remote Data Source'
  }

  const t = (key) => messages[key] ?? key

  it('uses translated loading text when no custom message is passed', () => {
    expect(resolveLoadingMessage(undefined, t)).toBe('Loading...')
    expect(resolveLoadingMessage('Please wait', t)).toBe('Please wait')
  })

  it('resolves the data source label from translation keys', () => {
    expect(getDataSourceLabel(true, t)).toBe('Mock Data Source')
    expect(getDataSourceLabel(false, t)).toBe('Remote Data Source')
  })
})

describe('community and page i18n resources', () => {
  const REQUIRED_KEYS = [
    'community.navAriaLabel',
    'community.defaultPublish',
    'community.heroSubtitle',
    'community.safetyTip',
    'loginPage.visualIntro',
    'cetPage.resultTitle',
    'cetPage.resultDisclaimer',
    'cetPage.writingScore',
    'graduateExam.totalScore',
    'graduateExam.emptyDescription',
    'evaluatePage.riskNotice',
    'evaluatePage.confirmMessage',
    'secret.detail.floor',
    'secret.detail.commentTooLong',
    'secret.detail.audioPause',
    'libraryPage.detail.emptyDescription',
    'info.announcementMissing',
    'pePage.wechatRequiredTitle',
    'pePage.wechatRequiredDescription',
    'delivery.publish.privacyHint',
    'lostandfound.publish.privacyHint',
    'marketplace.publish.safetyHint',
    'avatarEdit.privacyHint'
  ]

  function readPath(messages, path) {
    return path.split('.').reduce((node, part) => (node == null ? undefined : node[part]), messages)
  }

  it('keeps required page keys present in all six locales', () => {
    for (const code of LOCALE_CODES) {
      for (const key of REQUIRED_KEYS) {
        const value = readPath(LOCALES[code], key)
        expect(String(value || '').trim(), `${code}:${key}`).not.toBe('')
      }
    }
  })

  it('formats secret detail params without exposing raw keys', () => {
    const i18n = createI18n({
      legacy: false,
      locale: 'zh-CN',
      fallbackLocale: 'zh-CN',
      messages: LOCALES
    })

    for (const code of LOCALE_CODES) {
      i18n.global.locale.value = code
      const floor = i18n.global.t('secret.detail.floor', { n: 3 })
      const tooLong = i18n.global.t('secret.detail.commentTooLong', { max: 50 })
      expect(floor).toContain('3')
      expect(floor).not.toContain('secret.detail.floor')
      expect(tooLong).toContain('50')
      expect(tooLong).not.toContain('secret.detail.commentTooLong')
      expect(tooLong).not.toContain('{max}')
      expect(floor).not.toContain('{n}')
    }
  })

  it('switches actual community components and evaluation warnings without losing form state or translating API content', async () => {
    request.get.mockResolvedValue({ data: [{ id: 'fixture', title: '用户公告原文' }] })
    const Comp = defineComponent({
      setup: () => () => h('div', [
        h(CommunityHeader, { title: '用户标题' }),
        h(CommunityModuleLayout, { title: '用户模块', basePath: '/community', tabs: [] }),
        h(Evaluate)
      ])
    })
    const { host, i18n, unmount } = await mountLocalized(Comp)
    try {
      const checkbox = host.querySelector('input[type="checkbox"]')
      checkbox.checked = true
      checkbox.dispatchEvent(new Event('change', { bubbles: true }))
      const submit = [...host.querySelectorAll('button')].find(button => button.textContent.trim() === zhCN.evaluatePage.submitAction)
      submit.click()
      await nextTick()
      for (const code of LOCALE_CODES) {
        i18n.global.locale.value = code
        await nextTick()
        expect(host.querySelector('.community-header__back').getAttribute('aria-label')).toBe(LOCALES[code].common.back)
        expect(host.querySelector('.community-tabbar').getAttribute('aria-label')).toBe(LOCALES[code].community.navAriaLabel)
        expect(host.querySelector('.community-module-layout__hero-copy p').textContent).toBe(LOCALES[code].community.heroSubtitle)
        expect(host.querySelector('.community-module-layout__activity-list small').textContent).toBe(LOCALES[code].community.noticeFallback)
        expect(host.textContent).toContain(LOCALES[code].evaluatePage.riskNotice)
        expect(document.body.textContent).toContain(LOCALES[code].evaluatePage.confirmMessage)
        expect(host.textContent).toContain('用户公告原文')
        expect(host.textContent).toContain('用户标题')
        expect(checkbox.checked).toBe(true)
      }
    } finally {
      unmount()
    }
  })

  it('updates displayed audio errors and comment validation after switching locales, preserving typed text', async () => {
    let audio
    vi.stubGlobal('Audio', class {
      constructor() { audio = this }
      load() {}
      pause() {}
    })
    request.get.mockImplementation(async url => url.endsWith('/comments')
      ? { data: [{ id: 'comment', comment: '用户评论原文' }] }
      : { success: true, data: { id: 'fixture', type: 1, theme: 1, voiceURL: '/fixture-audio', liked: 0 } })
    const { host, i18n, unmount } = await mountLocalized(SecretDetail, '/secret/id/fixture')
    try {
      audio.onerror()
      const input = host.querySelector('input[name="comment"]')
      input.value = '保留用户内容'.repeat(12)
      input.dispatchEvent(new Event('input', { bubbles: true }))
      input.dispatchEvent(new KeyboardEvent('keyup', { key: 'Enter', bubbles: true }))
      await nextTick()
      for (const code of LOCALE_CODES) {
        i18n.global.locale.value = code
        await nextTick()
        expect(host.textContent).toContain(LOCALES[code].secret.detail.audioLoadFailed)
        expect(host.textContent).toContain(i18n.global.t('secret.detail.commentTooLong', { max: 50 }))
        expect(input.placeholder).toBe(LOCALES[code].secret.detail.commentPlaceholder)
        expect(input.value).toBe('保留用户内容'.repeat(12))
        expect(host.textContent).toContain('用户评论原文')
      }
      expect(request.post).not.toHaveBeenCalled()
    } finally {
      unmount()
      vi.unstubAllGlobals()
    }
  })

  it('keeps the corrected production templates free of static Chinese UI except the project name', () => {
    const files = [
      'components/community/CommunityHeader.vue', 'components/community/CommunityTabbar.vue',
      'components/community/CommunityModuleLayout.vue', 'views/Login.vue', 'views/Info.vue',
      'views/graduateExam/GraduateExamResult.vue', 'views/cet/Cet.vue', 'views/evaluate/Evaluate.vue',
      'views/secret/Detail.vue', 'views/collection/CollectionDetail.vue', 'views/info/AnnouncementDetail.vue',
      'views/pe/PeIndex.vue', 'views/delivery/Publish.vue', 'views/lostandfound/Publish.vue',
      'views/marketplace/Publish.vue', 'views/user/AvatarEdit.vue'
    ]
    const hits = []
    for (const file of files) {
      const source = readFileSync(join(dirname(fileURLToPath(import.meta.url)), '..', file), 'utf8')
      const template = parseSfc(source).descriptor.template
      const visit = (node) => {
        if (node.type === NodeTypes.TEXT && /\p{Script=Han}/u.test(node.content)) {
          if (!(file === 'views/Login.vue' && node.content.trim() === '广东二师助手')) hits.push(`${file}:${node.content.trim()}`)
        }
        for (const prop of node.props || []) {
          if (prop.type === NodeTypes.ATTRIBUTE && prop.value && /\p{Script=Han}/u.test(prop.value.content)) hits.push(`${file}:${prop.name}=${prop.value.content}`)
        }
        for (const child of node.children || []) visit(child)
      }
      visit(parseTemplate(template.content))
    }
    expect(hits).toEqual([])
  })
})
