import { test, expect } from '@playwright/test'

test('login page renders with username and password inputs', async ({ page }) => {
  await page.goto('/login')
  await expect(page.locator('input[type="text"]')).toBeVisible()
  await expect(page.locator('input[type="password"]')).toBeVisible()
})

test('redirects to login when not authenticated', async ({ page }) => {
  await page.goto('/home')
  await expect(page).toHaveURL(/login/)
})
