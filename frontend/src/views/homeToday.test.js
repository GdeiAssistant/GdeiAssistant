import { describe, expect, it } from 'vitest'
import { selectTodayCourses, todayColumn } from './homeToday'

const list = [
  { column: 1, row: 4, position: 29, scheduleName: '数据结构', scheduleLocation: 'B305', scheduleLesson: '第 5-6 节', minScheduleWeek: 1, maxScheduleWeek: 16 },
  { column: 1, row: 0, position: 1, scheduleName: '高等数学', scheduleLocation: 'A201', scheduleLesson: '第 1-2 节', minScheduleWeek: 1, maxScheduleWeek: 16 },
  { column: 1, row: 2, position: 15, scheduleName: '毕业设计', scheduleLocation: 'E402', scheduleLesson: '第 3-4 节', minScheduleWeek: 17, maxScheduleWeek: 18 },
  { column: 2, row: 0, position: 2, scheduleName: '离散数学', scheduleLocation: 'C101', scheduleLesson: '第 1-2 节', minScheduleWeek: 1, maxScheduleWeek: 16 }
]

describe('todayColumn', () => {
  it('maps Monday to 0 and Sunday to 6', () => {
    expect(todayColumn(new Date(2026, 9, 12))).toBe(0)
    expect(todayColumn(new Date(2026, 9, 18))).toBe(6)
  })
})

describe('selectTodayCourses', () => {
  it('keeps only today and the current week, ordered by section', () => {
    const result = selectTodayCourses(list, 5, 1)
    expect(result.map((c) => c.name)).toEqual(['高等数学', '数据结构'])
    expect(result[0]).toMatchObject({ location: 'A201', lesson: '第 1-2 节' })
  })

  it('falls back to position when row and column are missing', () => {
    const result = selectTodayCourses([{ position: 8, scheduleName: 'X', minScheduleWeek: 1, maxScheduleWeek: 2 }], 1, 1)
    expect(result).toHaveLength(1)
    expect(result[0].row).toBe(1)
  })

  it('drops courses without a week range when a week is known', () => {
    expect(selectTodayCourses([{ column: 0, row: 0, scheduleName: 'Y' }], 3, 0)).toEqual([])
  })

  it('returns an empty list for bad input', () => {
    expect(selectTodayCourses(null, 1, 0)).toEqual([])
  })
})
