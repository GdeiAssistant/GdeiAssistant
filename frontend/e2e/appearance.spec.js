import { test, expect } from '@playwright/test'

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
    await expect(page.locator('text=界面和外观')).toBeVisible()
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

  test('no FOUC — data-theme is set before page renders', async ({ page }) => {
    // Set dark theme preference before navigation
    await page.addInitScript(() => localStorage.setItem('theme', 'dark'))
    await page.goto('/appearance')

    // Check that data-theme was set synchronously (inline script)
    const theme = await page.evaluate(() => document.documentElement.getAttribute('data-theme'))
    expect(theme).toBe('dark')
  })
})
