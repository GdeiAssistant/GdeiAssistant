import { test, expect } from '@playwright/test'
import { readFileSync } from 'node:fs'

const codes = ['zh-CN', 'zh-HK', 'zh-TW', 'en', 'ja', 'ko']
const labels = ['简体中文', '繁體中文（香港）', '繁體中文（台灣）', 'English', '日本語', '한국어']
const table = (code) => JSON.parse(readFileSync(new URL(`../src/locales/${code}.json`, import.meta.url), 'utf8'))

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
      await expect(page.locator('.profile-page a[href="/social/privacy"]')).toHaveCount(0)
      await expect(page.locator('.profile-page a[href="/social/blocks"]')).toHaveCount(0)
      await page.locator('.profile-page a[href="/user/privacy-setting"]').click()
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
