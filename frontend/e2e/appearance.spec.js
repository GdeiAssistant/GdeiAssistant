import { test, expect } from './fixtures.js'

test.describe('Appearance page', () => {
  test.beforeEach(async ({ page }) => {
    // Set a mock session token to bypass auth redirect
    await page.addInitScript(() => {
      localStorage.setItem('token', 'mock-token')
      localStorage.setItem('locale', 'zh-CN')
    })
  })

  test('appearance page loads with theme options', async ({ page }) => {
    await page.goto('/appearance')
    await expect(page.getByRole('heading', { name: '界面和外观', exact: true })).toBeVisible()
  })

  test('dark mode toggle sets data-theme attribute', async ({ page }) => {
    await page.goto('/appearance')

    // Click dark mode option
    const darkOption = page.locator('text=深色模式').first()
    if (await darkOption.isVisible()) {
      await darkOption.click()
      const theme = await page.evaluate(() => document.documentElement.getAttribute('data-theme'))
      expect(theme).toBe('dark')
    }
  })

  test('light mode toggle sets data-theme attribute', async ({ page }) => {
    await page.goto('/appearance')

    const lightOption = page.locator('text=浅色模式').first()
    if (await lightOption.isVisible()) {
      await lightOption.click()
      const theme = await page.evaluate(() => document.documentElement.getAttribute('data-theme'))
      expect(theme).toBe('light')
    }
  })

  test('theme preference persists in localStorage', async ({ page }) => {
    await page.goto('/appearance')

    const darkOption = page.locator('text=深色模式').first()
    if (await darkOption.isVisible()) {
      await darkOption.click()
      const stored = await page.evaluate(() => localStorage.getItem('theme'))
      expect(stored).toBe('dark')
    }
  })

  test('font scale slider is present', async ({ page }) => {
    await page.goto('/appearance')
    await expect(page.locator('input[type="range"]')).toBeVisible()
  })

  test('language option keeps zh-HK wording aligned with About page', async ({ page }) => {
    await page.goto('/appearance')

    const languageSection = page.locator('.appearance-section').filter({ hasText: '语言' }).first()
    await expect(languageSection.getByText('繁體中文（香港）')).toBeVisible()
    await expect(languageSection.getByText('繁體中文（港澳）')).toHaveCount(0)
  })

  test('no FOUC — data-theme is set before page renders', async ({ page }) => {
    // Set dark theme preference before navigation
    await page.addInitScript(() => localStorage.setItem('theme', 'dark'))
    await page.goto('/appearance')

    // Check that data-theme was set synchronously (inline script)
    const theme = await page.evaluate(() => document.documentElement.getAttribute('data-theme'))
    expect(theme).toBe('dark')
  })

  test('accent selection stays synchronized between external updates, sidebar, and appearance page', async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 800 })
    await page.goto('/appearance')

    // Simulate an external root attribute mutation while AppearancePage is open
    await page.evaluate(() => {
      document.documentElement.dataset.accent = 'bauhinia'
    })
    const appearanceBauhinia = page.locator('.accent-option').nth(3)
    await expect(appearanceBauhinia).toHaveAttribute('aria-checked', 'true')

    // Switching back via sidebar on home keeps appearance page synchronized
    await page.goto('/')
    await page.locator('.campus-sidebar__accent-btn').nth(1).click()
    await page.goto('/appearance')
    await expect(page.locator('.accent-option').nth(1)).toHaveAttribute('aria-checked', 'true')
  })
})
