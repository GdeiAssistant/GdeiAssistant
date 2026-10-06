import { test, expect } from '@playwright/test'
import { readFileSync } from 'node:fs'

const codes = ['zh-CN', 'zh-HK', 'zh-TW', 'en', 'ja', 'ko']
const labels = ['简体中文', '繁體中文（香港）', '繁體中文（台灣）', 'English', '日本語', '한국어']
const table = (code) => JSON.parse(readFileSync(new URL(`../src/locales/${code}.json`, import.meta.url), 'utf8'))
const regions = {
  'zh-CN': ['中国 广东 广州', '中国 广东 汕头', '中国', '广东', '广州'],
  'zh-HK': ['中國 廣東 廣州', '中國 廣東 汕頭', '中國', '廣東', '廣州'],
  'zh-TW': ['中國 廣東 廣州', '中國 廣東 汕頭', '中國', '廣東', '廣州'],
  en: ['Guangzhou, Guangdong, China', 'Shantou, Guangdong, China', 'China', 'Guangdong', 'Guangzhou'],
  ja: ['広州, 広東, 中国', '汕頭, 広東, 中国', '中国', '広東', '広州'],
  ko: ['광저우, 광둥, 중국', '산터우, 광둥, 중국', '중국', '광둥', '광저우']
}

test.setTimeout(120000)
for (const width of [375, 1366]) {
  test(`six language changes preserve privacy and update profile/navigation at ${width}px`, async ({ page }) => {
    await page.setViewportSize({ width, height: 900 })
    await page.addInitScript(() => {
      if (sessionStorage.getItem('locale_regression_seeded')) return
      sessionStorage.setItem('locale_regression_seeded', '1')
      localStorage.setItem('gdei_data_source_mode', 'mock')
      localStorage.setItem('locale', 'zh-CN')
    })
    await page.goto('/login')
    await page.locator('input[autocomplete="username"]').fill('gdeiassistant')
    await page.locator('input[autocomplete="current-password"]').fill('gdeiassistant')
    await page.getByRole('checkbox').check()
    await page.locator('button.login-submit').click()
    await expect(page).toHaveURL(/\/home$/)
    await page.goto('/social/privacy')
    await page.locator('input[value="NONE"]').check()
    await page.getByRole('button', { name: table('zh-CN').common.save, exact: true }).click()
    await expect.poll(() => page.evaluate(() => JSON.parse(localStorage.getItem('mockRuntimeState')).social.dmPolicy)).toBe('NONE')

    for (let i = 0; i < codes.length; i++) {
      const text = table(codes[i])
      await page.goto('/appearance')
      await page.getByRole('button', { name: labels[i], exact: false }).click()
      await expect(page.getByRole('heading', { name: text.appearance.title, exact: true })).toBeVisible()
      await expect.poll(() => page.evaluate(() => localStorage.getItem('locale'))).toBe(codes[i])
      await expect(page.locator('html')).toHaveAttribute('lang', codes[i])
      await expect(page).toHaveTitle(text.router.siteTitle)
      await page.goto('/profile')
      await expect(page.locator('.campus-sidebar__nav')).toHaveAttribute('aria-label', text.navigationAccessibility.primaryNavigation)
      const stats = page.locator('.profile-stat-link')
      await expect(stats).toHaveCount(3)
      await expect(stats.nth(0)).toContainText(text.social.following)
      await expect(stats.nth(1)).toContainText(text.social.followers)
      await expect(stats.nth(2)).toContainText(text.social.friends)
      const place = regions[codes[i]]
      const locationRow = page.locator('.profile-page button.campus-list-row').filter({ hasText: text.profile.location })
      const hometownRow = page.locator('.profile-page button.campus-list-row').filter({ hasText: text.profile.hometown })
      await expect(locationRow).toContainText(place[0])
      await expect(hometownRow).toContainText(place[1])
      await locationRow.click()
      await page.getByRole('button', { name: place[2], exact: true }).click()
      await page.getByRole('button', { name: place[3], exact: true }).click()
      await expect(page.getByRole('button', { name: place[4], exact: true })).toBeVisible()
      await page.getByRole('button', { name: text.common.cancel, exact: true }).click()
      await expect(locationRow).toContainText(place[0])
      await expect(page.locator('.profile-page a[href="/social/privacy"]')).toHaveCount(0)
      await expect(page.locator('.profile-page a[href="/social/blocks"]')).toHaveCount(0)
      await expect(page.locator('.profile-page a[href="/user/privacy-setting"]')).toHaveCount(0)
      await page.goto('/settings')
      await page.getByRole('link', { name: text.profile.privacySetting, exact: true }).click()
      await expect(page.locator('.privacy-setting-row').first()).toContainText(text.privacy.field.faculty)
      await page.getByRole('link', { name: text.social.dmPrivacyTitle, exact: true }).click()
      await expect(page.locator('input[value="NONE"]')).toBeChecked()
      await page.getByRole('button', { name: text.social.backToPrivacySettings, exact: true }).click()
      await expect(page).toHaveURL(/\/user\/privacy-setting$/)
      await page.reload()
      await expect(page.locator('.privacy-setting-title')).toHaveText(text.profile.privacySetting)
      await expect(page.getByRole('link', { name: text.social.blocksTitle, exact: true })).toBeVisible()
      expect(await page.evaluate(() => document.documentElement.scrollWidth > innerWidth)).toBe(false)
    }
  })
}
