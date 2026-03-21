import { test, expect } from '@playwright/test'

test('login page renders', async ({ page }) => {
  await page.goto('/login')
  await expect(page.locator('input')).toBeVisible()
})

test('redirects to login when not authenticated', async ({ page }) => {
  await page.goto('/home')
  await expect(page).toHaveURL(/login/)
})
