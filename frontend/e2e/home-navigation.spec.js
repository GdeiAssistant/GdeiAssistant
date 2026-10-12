import { test, expect } from './fixtures.js'

for (const width of [320, 390, 1280]) {
  test(`home keeps one bottom inbox entry at ${width}px, including an open sidebar`, async ({ page }) => {
    await page.setViewportSize({ width, height: 900 })
    await page.addInitScript(() => {
      localStorage.setItem('gdei_data_source_mode', 'mock')
      localStorage.setItem('locale', 'zh-CN')
    })
    await page.goto('/login')
    await page.locator('input[type="checkbox"]').check()
    await page.getByRole('button', { name: '登录', exact: true }).click()
    await expect(page).toHaveURL(/\/home$/)

    const inbox = page.locator('.campus-mobile-tabbar__item').filter({ hasText: '消息' })
    await expect(inbox).toHaveCount(1)
    await expect(inbox).toBeVisible()
    await expect(page.locator('.campus-sidebar__nav').getByRole('button', { name: /消息/ })).toHaveCount(0)
    await expect(page.locator('.home-page').getByRole('button', { name: /消息/ })).toHaveCount(0)
    await expect(page.locator('.feature-section h2')).toHaveText(['校园服务', '校园生活'])
    await expect(inbox.locator('.message-unread-badge')).toBeVisible()

    if (width < 1024) {
      await page.locator('.campus-topbar__menu').click()
      await expect(page.locator('.campus-sidebar--open')).toBeVisible()
      await expect(inbox).toBeVisible()
      await expect(page.getByRole('button', { name: /消息/ })).toHaveCount(1)
    }

    await inbox.click()
    await expect(page).toHaveURL(/\/info$/)
    await expect(page.locator('.campus-sidebar--open')).toHaveCount(0)
    await page.locator('.campus-mobile-tabbar__item').filter({ hasText: '首页' }).click()
    await expect(page).toHaveURL(/\/home$/)
    await page.evaluate(() => window.scrollTo(0, document.documentElement.scrollHeight))
    const lastCard = await page.locator('.feature-card').last().boundingBox()
    const bottomNav = await page.locator('.campus-mobile-tabbar').boundingBox()
    expect(lastCard.y + lastCard.height).toBeLessThanOrEqual(bottomNav.y)
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true)
  })
}
