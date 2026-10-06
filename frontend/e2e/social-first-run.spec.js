import { test, expect } from '@playwright/test'

const peer = '22222222-2222-4222-8222-222222222222'
test.setTimeout(60000)

test('fresh mock login can follow peer then send text and image DM', async ({ page }) => {
  // Only seed once; addInitScript re-runs on every navigation.
  await page.addInitScript(() => {
    if (sessionStorage.getItem('social_first_run_seeded') === '1') return
    localStorage.clear()
    sessionStorage.clear()
    sessionStorage.setItem('social_first_run_seeded', '1')
    localStorage.setItem('gdei_data_source_mode', 'mock')
    localStorage.setItem('locale', 'zh-CN')
  })

  await page.goto('/login')
  await page.locator('input[autocomplete="username"]').fill('gdeiassistant')
  await page.locator('input[autocomplete="current-password"]').fill('gdeiassistant')
  await page.getByRole('checkbox').check()
  await page.locator('button.login-submit').click()
  await expect.poll(async () => page.evaluate(() => localStorage.getItem('token'))).toBeTruthy()

  // Visiting a social page initializes mock social state (including default PEER → ME).
  await page.goto(`/social/users/${peer}`)
  await expect(page.getByText('演示同学', { exact: true })).toBeVisible()
  await expect.poll(async () => page.evaluate(() => {
    const state = JSON.parse(localStorage.getItem('mockRuntimeState') || '{}')
    return (state.social?.follows || []).some((f) => (
      f.followerId === '22222222-2222-4222-8222-222222222222'
      && f.followeeId === '11111111-1111-4111-8111-111111111111'
    ))
  })).toBe(true)

  await page.getByRole('button', { name: '关注', exact: true }).click()
  await expect(page.getByRole('button', { name: '取消关注', exact: true })).toBeVisible()
  await page.getByRole('button', { name: '私信', exact: true }).click()
  await expect(page).toHaveURL(/\/social\/chat\/\d+/)

  await page.getByPlaceholder('输入文字消息').fill('first-run hello')
  await page.getByRole('button', { name: '发送', exact: true }).click()
  await expect(page.getByText('first-run hello', { exact: true })).toHaveCount(1)

  await page.locator('input[type="file"]').setInputFiles('public/img/landing/campus-hero.jpg')
  await expect(page.locator('.social-attachment-preview img')).toBeVisible()
  await page.getByRole('button', { name: '发送', exact: true }).click()
  const image = page.locator('.social-message .social-image-open img')
  await expect(image).toBeVisible()
  await expect.poll(() => image.evaluate((el) => el.complete && el.naturalWidth > 0)).toBe(true)
})
