import { test, expect } from '@playwright/test'

const MOCK_USERNAME = 'gdeiassistant'
const MOCK_PASSWORD = 'gdeiassistant'

test.use({
  viewport: { width: 390, height: 844 }
})

test.beforeEach(async ({ page }) => {
  await page.addInitScript(() => {
    if (!localStorage.getItem('gdei_e2e_initialized')) {
      localStorage.setItem('gdei_data_source_mode', 'mock')
      localStorage.setItem('locale', 'zh-CN')
      localStorage.removeItem('token')
      localStorage.removeItem('mockRuntimeState')
      localStorage.setItem('gdei_e2e_initialized', '1')
    }
  })
})

async function loginAsMockUser(page) {
  await page.goto('/login')
  await expect(page.getByText('模拟数据模式')).toBeVisible()
  await page.locator('input[type="text"]').fill(MOCK_USERNAME)
  await page.locator('input[type="password"]').fill(MOCK_PASSWORD)
  await page.getByRole('button', { name: '登录' }).click()
  await expect(page).toHaveURL(/\/home$/)
  await expect(page.getByText('校园服务')).toBeVisible()
}

test.describe('mock 模式 UI smoke', () => {
  test('mock 登录后可以进入首页并看到主入口', async ({ page }) => {
    await loginAsMockUser(page)

    await expect(page.getByText('校园生活')).toBeVisible()
    await expect(page.getByRole('button', { name: '成绩查询' })).toBeVisible()
    await expect(page.getByRole('button', { name: '二手交易' })).toBeVisible()
  })

  test('首页成绩入口可以打开成绩页并渲染 mock 成绩', async ({ page }) => {
    await loginAsMockUser(page)

    await page.getByRole('button', { name: '成绩查询' }).click()

    await expect(page).toHaveURL(/\/grade$/)
    await expect(page.getByText('成绩查询')).toBeVisible()
    await expect(page.getByText('第一学期')).toBeVisible()
    await expect(page.getByText('第二学期')).toBeVisible()
    await expect(page.getByText('高等数学')).toBeVisible()
    await expect(page.getByText('数据结构')).toBeVisible()
  })

  test('资讯页可以加载新闻、公告和互动消息', async ({ page }) => {
    await loginAsMockUser(page)

    await page.goto('/info')

    await expect(page.getByText('查看学校公开发布的校园新闻')).toBeVisible()
    await expect(page.getByText('系统公告', { exact: true })).toBeVisible()
    await expect(page.getByText('互动消息', { exact: true })).toBeVisible()
    await expect(page.getByText('系统维护通知')).toBeVisible()
    await expect(page.getByText('卖室友互动')).toBeVisible()
  })

  test('二手模块可查看详情并校验发布页空表单提示', async ({ page }) => {
    await loginAsMockUser(page)

    await page.goto('/marketplace/home')

    await expect(page.getByText('二手交易')).toBeVisible()
    await expect(page.getByText('九成新机械键盘')).toBeVisible()

    await page.getByText('九成新机械键盘').click()

    await expect(page).toHaveURL(/\/marketplace\/detail\/101$/)
    await expect(page.getByText('商品详情')).toBeVisible()
    await expect(page.getByText('复制QQ')).toBeVisible()

    await page.goto('/marketplace/publish')

    await expect(page.getByText('发布二手商品')).toBeVisible()
    await page.getByText('完成').click()
    await expect(page.getByText('请至少选择一张图片')).toBeVisible()
    await page.getByRole('button', { name: '确定' }).click()
    await expect(page.getByText('请至少选择一张图片')).not.toBeVisible()
  })
})
