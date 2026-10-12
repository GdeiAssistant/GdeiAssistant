import { test, expect } from '@playwright/test'

const OPTIONAL_SDK_URL = 'https://res.wx.qq.com/open/libs/weuijs/1.2.1/weui.min.js'

for (const width of [390, 1280]) {
  test(`login stays interactive when the optional CDN never responds at ${width}px`, async ({ page }) => {
    await page.setViewportSize({ width, height: 900 })
    await page.addInitScript(() => {
      localStorage.setItem('gdei_data_source_mode', 'mock')
      localStorage.setItem('locale', 'zh-CN')
    })

    let pendingSdk
    await page.route(OPTIONAL_SDK_URL, route => { pendingSdk = route })
    try {
      await page.goto('/login', { waitUntil: 'commit' })
      await expect(page.locator('input[autocomplete="username"]')).toBeVisible()
      await expect(page.locator('select option')).toHaveCount(6)
      await page.locator('select').selectOption('en')
      await expect(page.locator('button.login-submit')).toHaveText('Login')
      await expect(page.locator('html')).toHaveAttribute('lang', 'en')
    } finally {
      if (pendingSdk) {
        await pendingSdk.fulfill({ status: 200, contentType: 'application/javascript', body: '// Optional SDK unavailable.' })
      }
    }
  })
}
