import { test as base, expect } from '@playwright/test'

// Exercise the app's built-in controls with the optional CDN SDK unavailable.
// Match only the parser-blocking SDK URL; application/API requests stay real or
// use each spec's existing mocks. This is not a WeUI SDK integration fixture.
const WEUI_SCRIPT_URL = 'https://res.wx.qq.com/open/libs/weuijs/1.2.1/weui.min.js'

export const test = base.extend({
  context: async ({ context }, use) => {
    await context.route(WEUI_SCRIPT_URL, route => route.fulfill({
      status: 200,
      contentType: 'application/javascript',
      body: '// Offline smoke: optional WeUI SDK intentionally unavailable.'
    }))
    await use(context)
  }
})

export { expect }
