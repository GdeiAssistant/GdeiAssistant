import { test, expect } from './fixtures.js'

test.use({ viewport: { width: 390, height: 900 }, timezoneId: 'UTC' })

async function openWithSchedule(page, data) {
  const requests = []
  await page.clock.setFixedTime(new Date('2026-10-11T16:30:00Z'))
  await page.addInitScript(() => {
    localStorage.setItem('token', 'synthetic-home-contract')
    localStorage.setItem('gdei_data_source_mode', 'remote')
    localStorage.setItem('locale', 'zh-CN')
  })
  await page.route(url => url.pathname.startsWith('/api/'), route => {
    const request = route.request()
    requests.push({ path: new URL(request.url()).pathname, query: new URL(request.url()).search, method: request.method() })
    const path = new URL(request.url()).pathname
    let response = {}
    if (path === '/api/schedule') response = data
    if (path === '/api/user/profile') response = { nickname: 'Synthetic' }
    if (path === '/api/social/unread') response = { total: 0 }
    if (path === '/api/information/message/categories/unread') response = { interaction: 0, service: 0 }
    if (path === '/api/information/announcement/unread') response = 0
    return route.fulfill({ status: 200, json: { success: true, data: response } })
  })
  await page.goto('/home')
  await expect(page.locator('.home-hero__date')).toHaveText('10月12日星期一')
  return requests
}

const complete = {
  scheduleName: 'Synthetic', column: 0, row: 0, minScheduleWeek: 1, maxScheduleWeek: 16,
  scheduleLesson: '第 1-2 节', scheduleLocation: 'Synthetic room'
}

for (const [name, data] of [
  ['missing list', { week: 8 }],
  ['unknown current week', { scheduleList: [] }],
  ['unknown course day', { week: 8, scheduleList: [{ ...complete, column: null }] }],
  ['unknown course week bounds', { week: 8, scheduleList: [{ ...complete, minScheduleWeek: null }] }]
]) {
  test(`successful HTTP with ${name} remains unavailable`, async ({ page }) => {
    const requests = await openWithSchedule(page, data)
    await expect(page.locator('.home-today__empty')).toContainText('课表暂时无法获取')
    await expect(page.locator('.home-today__course')).toHaveCount(0)
    expect(requests.some(request => request.path === '/api/schedule')).toBe(true)
    expect(requests.every(request => request.method === 'GET')).toBe(true)
  })
}

test('valid empty HTTP timetable alone establishes an empty campus day', async ({ page }) => {
  await openWithSchedule(page, { week: 8, scheduleList: [] })
  await expect(page.locator('.home-today__empty')).toContainText('今天没有课')
  await expect(page.locator('.home-today__course')).toHaveCount(0)
})

test('HTTP timetable uses campus Monday/current API week and retains actual lesson labels', async ({ page }) => {
  const requests = await openWithSchedule(page, {
    week: 8,
    scheduleList: [
      { ...complete, scheduleName: 'Later', row: 4, scheduleLesson: '第 5-6 节' },
      { ...complete, scheduleName: 'Early' },
      { ...complete, scheduleName: 'Wrong week', maxScheduleWeek: 7 },
      { ...complete, scheduleName: 'Wrong day', column: 1 }
    ]
  })
  await expect(page.locator('.home-today__course strong')).toHaveText(['Early', 'Later'])
  await expect(page.locator('.home-today__lesson')).toHaveText(['第 1-2 节', '第 5-6 节'])
  await expect(page.locator('.home-today__heading p')).toHaveText('今天 2 节课')
  expect(requests.filter(request => request.path === '/api/schedule').every(request => request.query === '')).toBe(true)
  expect(requests.every(request => request.method === 'GET')).toBe(true)
})
