import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { createApp, nextTick } from 'vue'
import Grade from './grade/Grade.vue'
import Schedule from './schedule/Schedule.vue'

const mocks = vi.hoisted(() => ({
  getGrade: vi.fn(), updateGradeCache: vi.fn(),
  getSchedule: vi.fn(), updateScheduleCache: vi.fn(), addCustomSchedule: vi.fn(), deleteCustomSchedule: vi.fn(),
  success: vi.fn(), error: vi.fn(), loading: vi.fn(), hideLoading: vi.fn(), back: vi.fn(), push: vi.fn()
}))
vi.mock('@/api/grade', () => ({ getGrade: mocks.getGrade, updateGradeCache: mocks.updateGradeCache }))
vi.mock('@/api/schedule', () => ({ getSchedule: mocks.getSchedule, updateScheduleCache: mocks.updateScheduleCache,
  addCustomSchedule: mocks.addCustomSchedule, deleteCustomSchedule: mocks.deleteCustomSchedule }))
vi.mock('@/composables/useToast', () => ({ useToast: () => mocks }))
vi.mock('vue-router', () => ({ useRouter: () => mocks }))
vi.mock('vue-i18n', () => ({ useI18n: () => ({ t: (key, params) => params?.week ? `${key}:${params.week}` : key }) }))

const deferred = () => {
  let resolve, reject
  const promise = new Promise((yes, no) => { resolve = yes; reject = no })
  return { promise, resolve, reject }
}
const grade = name => ({ success: true, data: { firstTermGPA: 3.5, secondTermGPA: 3,
  firstTermGradeList: [{ gradeName: name, gradeCredit: 2, gradeScore: '80' }], secondTermGradeList: [] } })
const schedule = (name, week = 1) => ({ success: true, data: { week, scheduleList: [{ scheduleName: name,
  scheduleLocation: '合成教室', position: 0, row: 0, column: 0, scheduleLength: 1, minScheduleWeek: 1, maxScheduleWeek: 20 }] } })
let app, root
const flush = async () => { await Promise.resolve(); await Promise.resolve(); await nextTick() }
const mount = async component => {
  root = document.createElement('div'); document.body.append(root)
  app = createApp(component); app.mount(root); await flush()
}
const click = async label => {
  const button = [...document.querySelectorAll('button')].find(item => item.textContent.trim() === label)
  expect(button, `button ${label}`).toBeTruthy(); button.click(); await flush()
}
const input = async (element, value) => {
  element.value = value; element.dispatchEvent(new Event(element.tagName === 'SELECT' ? 'change' : 'input', { bubbles: true })); await flush()
}
beforeEach(() => { Object.values(mocks).forEach(mock => mock.mockReset()) })
afterEach(() => { app?.unmount(); app = null; document.body.replaceChildren() })

describe('academic page request ownership', () => {
  it('keeps only the latest grade year even when the adapter ignores cancellation', async () => {
    const old = deferred(), current = deferred()
    mocks.getGrade.mockReturnValueOnce(old.promise).mockReturnValueOnce(current.promise)
    await mount(Grade); await click('gradePage.year.sophomore')
    expect(mocks.getGrade.mock.calls[0][1].signal.aborted).toBe(true)
    expect(mocks.getGrade.mock.calls[1][0]).toBe(1)
    current.resolve(grade('最新成绩')); await flush(); old.resolve(grade('过期成绩')); await flush()
    expect(root.textContent).toContain('最新成绩'); expect(root.textContent).not.toContain('过期成绩')
    await click('gradePage.year.sophomore'); expect(mocks.getGrade).toHaveBeenCalledTimes(2)
  })

  it('stale grade failure cannot clear the newer request loading state', async () => {
    const old = deferred(), current = deferred()
    mocks.getGrade.mockReturnValueOnce(old.promise).mockReturnValueOnce(current.promise)
    await mount(Grade); await click('gradePage.year.junior')
    old.reject(new Error('synthetic stale failure')); await flush()
    expect(root.querySelector('.animate-pulse')).toBeTruthy()
    current.resolve(grade('当前成绩')); await flush()
    expect(root.querySelector('.animate-pulse')).toBeNull(); expect(root.textContent).toContain('当前成绩')
  })

  it('unmount aborts the active grade request and ignores its late response', async () => {
    const pending = deferred(); mocks.getGrade.mockReturnValue(pending.promise)
    await mount(Grade); const signal = mocks.getGrade.mock.calls[0][1].signal
    app.unmount(); app = null; expect(signal.aborted).toBe(true)
    pending.resolve(grade('离开页面后到达')); await flush(); expect(root.textContent).toBe('')
  })

  it('failed grade cache refresh releases the toast and preserves the current data', async () => {
    mocks.getGrade.mockResolvedValue(grade('保留成绩')); mocks.updateGradeCache.mockRejectedValue(new Error('synthetic outage'))
    await mount(Grade); await click('gradePage.moreAction'); await click('gradePage.actionSheet.updateCache')
    expect(mocks.hideLoading).toHaveBeenCalled(); expect(mocks.success).not.toHaveBeenCalled()
    expect(mocks.getGrade).toHaveBeenCalledTimes(1); expect(root.textContent).toContain('保留成绩')
  })

  it('successful grade cache refresh reads the currently selected year again', async () => {
    mocks.getGrade.mockResolvedValueOnce(grade('旧成绩')).mockResolvedValueOnce(grade('更新成绩'))
    mocks.updateGradeCache.mockResolvedValue({ success: true })
    await mount(Grade); await click('gradePage.moreAction'); await click('gradePage.actionSheet.updateCache')
    expect(mocks.getGrade).toHaveBeenCalledTimes(2); expect(root.textContent).toContain('更新成绩')
    expect(mocks.success).toHaveBeenCalledWith('gradePage.updateSuccess')
  })

  it('late schedule responses cannot overwrite either courses or the chosen week', async () => {
    const old = deferred(), current = deferred()
    mocks.getSchedule.mockReturnValueOnce(old.promise).mockReturnValueOnce(current.promise)
    await mount(Schedule); await click('schedule.selectWeek'); await click('schedule.weekLabel:2')
    expect(mocks.getSchedule.mock.calls[0][1].signal.aborted).toBe(true)
    expect(mocks.getSchedule.mock.calls[1][0]).toBe(2)
    current.resolve(schedule('当前课表', 2)); await flush(); old.resolve(schedule('过期课表', 1)); await flush()
    expect(root.textContent).toContain('当前课表'); expect(root.textContent).toContain('schedule.weekLabel:2')
    expect(root.textContent).not.toContain('过期课表')
  })

  it('schedule cache failure leaves existing courses intact and clears the loading toast', async () => {
    mocks.getSchedule.mockResolvedValue(schedule('保留课程'))
    mocks.updateScheduleCache.mockRejectedValue(new Error('synthetic outage'))
    await mount(Schedule); await click('schedule.more'); await click('schedule.action.refresh')
    expect(mocks.hideLoading).toHaveBeenCalled(); expect(mocks.success).not.toHaveBeenCalled()
    expect(mocks.getSchedule).toHaveBeenCalledTimes(1); expect(root.textContent).toContain('保留课程')
  })

  it('custom courses validate required fields and overlap before posting', async () => {
    mocks.getSchedule.mockResolvedValue(schedule('已有课程'))
    await mount(Schedule); await click('schedule.more'); await click('schedule.action.addCustomCourse')
    await click('schedule.addDialog.submit'); expect(mocks.error).toHaveBeenLastCalledWith('schedule.message.nameRequired')
    const fields = document.querySelectorAll('input')
    await input(fields[0], ' 合成课程 '); await click('schedule.addDialog.submit')
    expect(mocks.error).toHaveBeenLastCalledWith('schedule.message.locationRequired')
    await input(fields[1], ' 合成教室 '); await click('schedule.addDialog.submit')
    expect(mocks.error).toHaveBeenLastCalledWith('schedule.message.duplicateCourse')
    expect(mocks.addCustomSchedule).not.toHaveBeenCalled()
    await input(document.querySelectorAll('select')[0], '2')
    mocks.addCustomSchedule.mockResolvedValue({ success: true })
    await click('schedule.addDialog.submit')
    expect(mocks.addCustomSchedule).toHaveBeenCalledWith({ scheduleName: '合成课程', scheduleLocation: '合成教室',
      scheduleLength: 1, position: 1, minScheduleWeek: 1, maxScheduleWeek: 1 })
    expect(mocks.getSchedule).toHaveBeenCalledTimes(2)
  })
})
