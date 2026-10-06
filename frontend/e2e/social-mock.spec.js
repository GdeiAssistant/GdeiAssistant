import { test, expect } from '@playwright/test'
import { readFileSync } from 'node:fs'

const me = '11111111-1111-4111-8111-111111111111'
const peer = '22222222-2222-4222-8222-222222222222'
test.setTimeout(60000)

test.beforeEach(async ({ page }) => {
  await page.addInitScript(({ me, peer }) => {
    if (localStorage.getItem('social_e2e_initialized')) return
    localStorage.setItem('social_e2e_initialized', '1')
    localStorage.setItem('gdei_data_source_mode', 'mock')
    localStorage.setItem('locale', 'zh-CN')
    localStorage.setItem('token', 'mock-token')
    localStorage.setItem('mockRuntimeState', JSON.stringify({
      token: 'mock-token', profile: { username: 'gdeiassistant', nickname: 'GDEI小助手' },
      social: {
        dmPolicy: 'MUTUAL', blocks: [],
        // 模拟对方已关注我；我的关注操作由 UI 完成。
        follows: [{ followerId: peer, followeeId: me }],
        users: {
          [me]: { id: me, nickname: 'GDEI小助手', avatarUrl: null, introduction: '演示账号' },
          [peer]: { id: peer, nickname: '演示同学', avatarUrl: null, introduction: '互关可以私信' }
        },
        conversations: {}, messages: {}, nextConversationId: 1, nextMessageId: 1
      }
    }))
  }, { me, peer })
})

test('互关、发送、刷新、拉黑与解除拉黑使用持久演示状态', async ({ page }) => {
  const remoteRequests = []
  page.on('request', request => { if (new URL(request.url()).pathname.startsWith('/api/')) remoteRequests.push(request.url()) })
  await page.goto(`/social/users/${peer}`)
  await expect(page.getByText('演示同学', { exact: true })).toBeVisible()
  await page.getByRole('button', { name: '关注', exact: true }).click()
  await expect(page.getByRole('button', { name: '取消关注', exact: true })).toBeVisible()
  await expect(page.locator('a[href$="relationships?kind=friends"]')).toContainText('1')
  await page.getByRole('button', { name: '私信', exact: true }).click()
  await expect(page).toHaveURL(/\/social\/chat\/\d+/)
  const conversationUrl = page.url()
  await page.getByPlaceholder('输入文字消息').fill('你好，社交演示🙂')
  await page.getByRole('button', { name: '发送', exact: true }).click()
  await expect(page.getByText('你好，社交演示🙂', { exact: true })).toHaveCount(1)
  await expect.poll(() => page.evaluate(() => Object.values(JSON.parse(localStorage.getItem('mockRuntimeState')).social.messages).flat().length)).toBe(1)
  await page.reload()
  await expect(page.getByText('你好，社交演示🙂', { exact: true })).toBeVisible()
  await expect(page.getByPlaceholder('输入文字消息')).toBeEnabled()
  await page.goto(`/social/users/${peer}`)
  await page.getByRole('button', { name: '拉黑', exact: true }).click()
  await expect.poll(() => page.evaluate(() => JSON.parse(localStorage.getItem('mockRuntimeState')).social.blocks.length)).toBe(1)
  await page.goto(conversationUrl)
  await expect(page.getByText('你好，社交演示🙂', { exact: true })).toBeVisible()
  await expect(page.getByPlaceholder('输入文字消息')).toBeDisabled()
  await page.goto('/social/blocks')
  await page.getByRole('button', { name: '取消拉黑', exact: true }).click()
  await expect.poll(() => page.evaluate(() => JSON.parse(localStorage.getItem('mockRuntimeState')).social.blocks.length)).toBe(0)
  await page.goto(`/social/users/${peer}`)
  await expect(page.getByRole('button', { name: '关注', exact: true })).toBeVisible()
  await expect(page.locator('a[href$="relationships?kind=friends"]')).toContainText('0')
  expect(remoteRequests).toEqual([])
})

test('私信隐私保存后刷新仍为原选择', async ({ page }) => {
  await page.goto('/social/privacy')
  await expect(page.locator('input[value="MUTUAL"]')).toBeChecked()
  await page.locator('input[value="NONE"]').check()
  await page.getByRole('button', { name: '保存', exact: true }).click()
  await expect.poll(() => page.evaluate(() => JSON.parse(localStorage.getItem('mockRuntimeState')).social.dmPolicy)).toBe('NONE')
  await page.reload()
  await expect(page.locator('input[value="NONE"]')).toBeChecked()
})

test('关系列表能够继续加载下一页', async ({ page }) => {
  await page.goto('/social/privacy')
  await page.evaluate(({ me }) => {
    const state = JSON.parse(localStorage.getItem('mockRuntimeState'))
    for (let index = 1; index <= 52; index += 1) {
      const id = `${String(index).padStart(8, '0')}-aaaa-4aaa-8aaa-aaaaaaaaaaaa`
      state.social.users[id] = { id, nickname: `分页同学${index}`, avatarUrl: null }
      state.social.follows.push({ followerId: id, followeeId: me })
    }
    localStorage.setItem('mockRuntimeState', JSON.stringify(state))
  }, { me })
  await page.goto(`/social/users/${me}/relationships?kind=followers`)
  await expect(page.getByRole('button', { name: /分页同学/ })).toHaveCount(19)
  await page.getByRole('button', { name: '加载更多', exact: true }).click()
  await expect(page.getByRole('button', { name: /分页同学/ })).toHaveCount(39)
  await page.getByRole('button', { name: '加载更多', exact: true }).click()
  await expect(page.getByRole('button', { name: /分页同学/ })).toHaveCount(52)
  await expect(page.getByRole('button', { name: '加载更多', exact: true })).toHaveCount(0)
})

for (const viewport of [{ width: 320, height: 640 }, { width: 375, height: 812 }, { width: 1366, height: 900 }]) {
  test(`聊天输入和长文本在 ${viewport.width}px 页面内可用`, async ({ page }) => {
    await page.setViewportSize(viewport)
    await page.goto(`/social/users/${peer}`)
    await page.getByRole('button', { name: '关注', exact: true }).click()
    await page.getByRole('button', { name: '私信', exact: true }).click()
    const input = page.getByRole('textbox', { name: '输入文字消息', exact: true })
    const send = page.getByRole('button', { name: '发送', exact: true })
    await expect(input).toBeEnabled()
    const content = `资料链接：https://example.test/${'a'.repeat(400)}`
    await input.fill(content)
    await send.click()
    await expect(page.getByText(content, { exact: true })).toHaveCount(1)
    const layout = await page.evaluate(() => {
      const input = document.querySelector('input[placeholder="输入文字消息"]')
      const composer = input.getBoundingClientRect()
      const nav = document.querySelector('.campus-mobile-tabbar')
      const navTop = nav && getComputedStyle(nav).display !== 'none' ? nav.getBoundingClientRect().top : innerHeight
      const bubble = document.querySelector('.social-message')
      const button = [...document.querySelectorAll('button')].find(e => e.textContent.trim() === '发送')
      return { bottom: composer.bottom, top: composer.top, navTop, height: composer.height,
        hit: document.elementFromPoint(composer.x + composer.width / 2, composer.y + composer.height / 2) === input,
        pageOverflow: document.documentElement.scrollWidth > innerWidth,
        bubbleOverflow: bubble.scrollWidth > bubble.clientWidth + 1,
        buttonHeight: button.getBoundingClientRect().height }
    })
    expect(layout.bottom).toBeLessThanOrEqual(layout.navTop)
    expect(layout.top).toBeGreaterThanOrEqual(0)
    expect(layout.hit).toBe(true)
    expect(layout.height).toBeGreaterThanOrEqual(44)
    expect(layout.buttonHeight).toBeGreaterThanOrEqual(44)
    expect(layout.pageOverflow).toBe(false)
    expect(layout.bubbleOverflow).toBe(false)
    await page.reload()
    await expect(page.getByText(content, { exact: true })).toHaveCount(1)
  })
}

test('图片可预览取消、发送、查看，刷新和拉黑后历史图片仍可读取', async ({ page }) => {
  await page.setViewportSize({ width: 375, height: 812 })
  await page.goto(`/social/users/${peer}`)
  await page.getByRole('button', { name: '关注', exact: true }).click()
  await page.getByRole('button', { name: '私信', exact: true }).click()
  await expect(page).toHaveURL(/\/social\/chat\/\d+/)
  const conversationUrl = page.url()
  await expect(page.getByRole('button', { name: '选择图片', exact: true })).toBeEnabled()
  await page.locator('input[type="file"]').setInputFiles('public/img/landing/campus-hero.jpg')
  await expect(page.locator('.social-attachment-preview img')).toBeVisible()
  await page.getByRole('button', { name: '取消图片', exact: true }).click()
  await expect(page.locator('.social-attachment-preview')).toHaveCount(0)
  await page.locator('input[type="file"]').setInputFiles('public/img/landing/campus-hero.jpg')
  await expect(page.locator('.social-attachment-preview img')).toBeVisible()
  await page.getByRole('button', { name: '发送', exact: true }).click()
  const image = page.locator('.social-message .social-image-open img')
  await expect(image).toBeVisible()
  await expect.poll(() => image.evaluate(el => el.complete && el.naturalWidth > 0)).toBe(true)
  await expect.poll(() => page.evaluate(() => Object.values(JSON.parse(localStorage.getItem('mockRuntimeState')).social.messages).flat().filter(m => m.type === 'IMAGE').length)).toBe(1)
  await page.locator('.social-image-open').click()
  await expect(page.locator('dialog[open] img')).toBeVisible()
  await page.keyboard.press('Escape')
  await expect(page.locator('dialog[open]')).toHaveCount(0)
  await page.reload()
  await expect(image).toBeVisible()
  await expect.poll(() => image.evaluate(el => el.complete && el.naturalWidth > 0)).toBe(true)
  expect(await image.getAttribute('src')).toMatch(/^blob:/)
  await expect(page.getByPlaceholder('输入文字消息')).toBeEnabled()
  await page.screenshot({ path: '/tmp/gdei-chat-images-20261005/mobile-image-chat.png' })
  await page.goto('/social/chats')
  await expect(page.getByText('[图片]', { exact: true })).toBeVisible()
  await page.screenshot({ path: '/tmp/gdei-chat-images-20261005/mobile-image-inbox.png' })
  await page.goto(`/social/users/${peer}`)
  await page.getByRole('button', { name: '拉黑', exact: true }).click()
  await expect.poll(() => page.evaluate(() => JSON.parse(localStorage.getItem('mockRuntimeState')).social.blocks.length)).toBe(1)
  await page.goto(conversationUrl)
  await expect(page.getByRole('button', { name: '选择图片', exact: true })).toBeDisabled()
  await expect(image).toBeVisible()
  await expect.poll(() => image.evaluate(el => el.complete && el.naturalWidth > 0)).toBe(true)
})

test('相机 JPEG 的 EXIF 方向在上传前旋正并移除', async ({ page }) => {
  await page.goto(`/social/users/${peer}`)
  await page.getByRole('button', { name: '关注', exact: true }).click()
  await page.getByRole('button', { name: '私信', exact: true }).click()
  await expect(page.getByRole('button', { name: '选择图片', exact: true })).toBeEnabled()
  const jpeg = readFileSync('public/img/landing/campus-hero.jpg')
  // TIFF orientation=6 in an APP1 segment: test the real browser decoder/canvas.
  const exif = Buffer.from('45786966000049492a0008000000010012010300010000000600000000000000', 'hex')
  const marker = Buffer.from([0xff, 0xe1, 0, exif.length + 2])
  const rotated = Buffer.concat([jpeg.subarray(0, 2), marker, exif, jpeg.subarray(2)])
  await page.locator('input[type="file"]').setInputFiles({ name: 'camera.jpg', mimeType: 'image/jpeg', buffer: rotated })
  await expect(page.locator('.social-attachment-preview img')).toBeVisible()
  await page.getByRole('button', { name: '发送', exact: true }).click()
  await expect.poll(() => page.evaluate(() => Object.values(JSON.parse(localStorage.getItem('mockRuntimeState')).social.messages).flat().find(m => m.type === 'IMAGE')?.image)).toMatchObject({ width: 941, height: 1672 })
  const image = page.locator('.social-image-open img')
  await expect.poll(() => image.evaluate(el => ({ width: el.naturalWidth, height: el.naturalHeight }))).toEqual({ width: 941, height: 1672 })
})
