import { describe, expect, it } from 'vitest'
import { campusDateLabel, selectTodayCourses, todayColumn } from './homeToday'

const list = [
  { column: 1, row: 4, position: 29, scheduleName: '数据结构', scheduleLocation: 'B305', scheduleLesson: '第 5-6 节', minScheduleWeek: 1, maxScheduleWeek: 16 },
  { column: 1, row: 0, position: 1, scheduleName: '高等数学', scheduleLocation: 'A201', scheduleLesson: '第 1-2 节', minScheduleWeek: 1, maxScheduleWeek: 16 },
  { column: 1, row: 2, position: 15, scheduleName: '毕业设计', scheduleLocation: 'E402', scheduleLesson: '第 3-4 节', minScheduleWeek: 17, maxScheduleWeek: 18 },
  { column: 2, row: 0, position: 2, scheduleName: '离散数学', scheduleLocation: 'C101', scheduleLesson: '第 1-2 节', minScheduleWeek: 1, maxScheduleWeek: 16 }
]

describe('todayColumn', () => {
  it('maps Monday to 0 and Sunday to 6', () => {
    expect(todayColumn(new Date('2026-10-11T16:00:00Z'))).toBe(0)
    expect(todayColumn(new Date('2026-10-18T15:59:59Z'))).toBe(6)
  })

  it('uses the campus day across UTC midnight and the Sunday/Monday boundary', () => {
    expect(todayColumn(new Date('2026-10-11T15:59:59Z'))).toBe(6)
    expect(todayColumn(new Date('2026-10-11T16:00:00Z'))).toBe(0)
    expect(todayColumn(new Date('2026-10-11T23:59:59Z'))).toBe(0)
    expect(todayColumn(new Date('2026-10-12T00:00:00Z'))).toBe(0)
    expect(campusDateLabel(new Date('2026-10-11T16:30:00Z'), 'en')).toContain('Monday')
    expect(campusDateLabel(new Date('2026-10-11T16:30:00Z'), 'en')).toContain('12')
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

  it('keeps unknown course weeks unavailable rather than claiming an empty day', () => {
    expect(selectTodayCourses([{ column: 0, row: 0, scheduleName: 'Y' }], 3, 0)).toBeNull()
  })

  it('keeps missing lists and unknown current weeks unavailable', () => {
    expect(selectTodayCourses(null, 1, 0)).toBeNull()
    for (const week of [null, undefined, 0, -1, 21, '5', NaN]) {
      expect(selectTodayCourses(list, week, 1)).toBeNull()
      expect(selectTodayCourses([], week, 1)).toBeNull()
    }
  })

  it('accepts a real empty timetable and a known day with no matching courses', () => {
    expect(selectTodayCourses([], 5, 0)).toEqual([])
    expect(selectTodayCourses(list, 5, 6)).toEqual([])
  })

  it('rejects missing day/section metadata without inventing position zero', () => {
    const course = { scheduleName: 'Synthetic', minScheduleWeek: 1, maxScheduleWeek: 16 }
    expect(selectTodayCourses([course], 5, 0)).toBeNull()
    expect(selectTodayCourses([{ ...course, row: 0 }], 5, 0)).toBeNull()
    expect(selectTodayCourses([{ ...course, column: 0 }], 5, 0)).toBeNull()
  })

  it('rejects out-of-range and non-integer metadata, including other unknown days', () => {
    for (const patch of [
      { position: -1 }, { position: 70 }, { column: 7 }, { row: 10 }, { row: null, position: null },
      { column: '' }, { minScheduleWeek: null }, { maxScheduleWeek: 0 }, { minScheduleWeek: 17, maxScheduleWeek: 16 }
    ]) {
      expect(selectTodayCourses([{ ...list[0], ...patch }], 5, 0)).toBeNull()
    }
  })

  it('preserves missing lesson/location labels without guessing time or period', () => {
    const [course] = selectTodayCourses([{ position: 8, scheduleName: 'Synthetic', minScheduleWeek: 1, maxScheduleWeek: 16 }], 5, 1)
    expect(course.lesson).toBe('')
    expect(course.location).toBe('')
    expect(course.row).toBe(1)
  })

  it('rejects unknown course names instead of counting a fabricated unnamed course', () => {
    expect(selectTodayCourses([{ ...list[0], scheduleName: null }], 5, 1)).toBeNull()
  })
})
