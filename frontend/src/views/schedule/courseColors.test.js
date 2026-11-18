import { describe, expect, it } from 'vitest'
import { assignCoursePairs, blockGridRow, hashCourseName, periodGridRow } from './courseColors'

const block = (scheduleName, column, row, scheduleLength = 2, extra = {}) => ({
  scheduleName, column, row, scheduleLength, position: row * 7 + column, minScheduleWeek: 1, maxScheduleWeek: 16, ...extra
})

describe('assignCoursePairs', () => {
  it('gives each course one pair regardless of day, in first-appearance order', () => {
    const list = [block('B', 1, 0), block('A', 0, 0), block('A', 3, 4), block('C', 2, 2)]
    const pairs = assignCoursePairs(list)
    expect([pairs.get('A'), pairs.get('B'), pairs.get('C')]).toEqual([1, 2, 3])
  })

  it('ignores server colorCode and list order', () => {
    const a = assignCoursePairs([block('X', 0, 0, 2, { colorCode: '#fdca00' }), block('Y', 1, 0)])
    const b = assignCoursePairs([block('Y', 1, 0), block('X', 0, 0, 2, { colorCode: '#1ae961' })])
    expect([...a].sort()).toEqual([...b].sort())
  })

  it('never puts p1 and p5 on back-to-back blocks of one day', () => {
    // Courses 1..5 on the same day, back to back: course 5 would normally take p5 next to p1's block.
    const list = [
      block('c1', 0, 0, 1), block('c2', 1, 0, 1), block('c3', 2, 0, 1), block('c4', 3, 0, 1),
      block('c5', 0, 1, 1)
    ]
    const pairs = assignCoursePairs(list)
    expect(pairs.get('c1')).toBe(1)
    expect(pairs.get('c5')).not.toBe(5)
  })

  it('reuses the least adjacent pair for the 9th course', () => {
    const list = []
    for (let index = 0; index < 8; index += 1) list.push(block(`c${index + 1}`, index % 7, 0, 1, { minScheduleWeek: index + 1 }))
    list.push(block('ninth', 0, 1, 1, { minScheduleWeek: 9 }))
    const pairs = assignCoursePairs(list)
    expect(pairs.size).toBe(9)
    // c1 (p1) sits directly above 'ninth' on day 0 at week 9: p1 is not chosen.
    expect(pairs.get('ninth')).not.toBe(pairs.get('c1'))
  })

  it('is deterministic for ties', () => {
    const list = [block('beta', 0, 0), block('alpha', 0, 0, 2, { position: 0 })]
    expect(assignCoursePairs(list)).toEqual(assignCoursePairs([...list].reverse()))
    expect(hashCourseName('alpha')).toBe(hashCourseName('alpha'))
  })
})

describe('grid geometry', () => {
  it('skips the lunch and dinner bands', () => {
    expect([0, 3, 4, 7, 8, 11].map(periodGridRow)).toEqual([2, 5, 7, 10, 12, 15])
    expect(blockGridRow(2, 2)).toBe('4 / 6')
    expect(blockGridRow(3, 2)).toBe('5 / 8')
  })
})
