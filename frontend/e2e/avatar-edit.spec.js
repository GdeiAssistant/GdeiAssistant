import { test, expect } from '@playwright/test'

const PNG_2X2 = Buffer.from(
  'iVBORw0KGgoAAAANSUhEUgAAAAIAAAACCAYAAABytg0kAAAAFUlEQVR42mP8z8BQz0AEYBxVSF+FAAQ2AQL7r5f5AAAAAElFTkSuQmCC',
  'base64'
)

test.beforeEach(async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.setItem('token', 'mock-token')
    localStorage.setItem('locale', 'zh-CN')
  })

  let uploadIndex = 0
  await page.route('**/api/profile/avatar', async route => {
    if (route.request().method() === 'POST') {
      await route.fulfill({ json: { success: true, data: true } })
      return
    }
    await route.fulfill({ json: { success: true, data: '/img/login/qq.png' } })
  })
  await page.route('**/api/upload/presignedUrl**', async route => {
    uploadIndex += 1
    await route.fulfill({
      json: {
        success: true,
        data: {
          url: `https://upload.example/avatar-${uploadIndex}.jpg`,
          objectKey: `avatar-${uploadIndex}.jpg`
        }
      }
    })
  })
  await page.route('https://upload.example/**', async route => {
    await route.fulfill({ status: 200, body: '' })
  })
})

test('avatar crop flow uploads cropped image', async ({ page }) => {
  await page.goto('/user/avatar-edit')
  await expect(page.getByText('更换头像')).toBeVisible()

  await page.locator('input[type="file"]').setInputFiles({
    name: 'avatar.png',
    mimeType: 'image/png',
    buffer: PNG_2X2
  })

  await expect(page.getByRole('img', { name: '裁剪头像' })).toBeVisible()
  await expect(page.locator('cropper-selection')).toBeVisible()

  await Promise.all([
    page.waitForRequest(request => request.method() === 'POST' && request.url().includes('/api/profile/avatar')),
    page.getByRole('button', { name: '确定' }).click()
  ])
})
