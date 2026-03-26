import { describe, expect, it } from 'vitest'
import {
  createGradeActionSheetItems,
  createGradeTableHeaders,
  createGradeYearTabs
} from './gradeContent'

describe('grade localized content', () => {
  const messages = {
    'gradePage.year.freshman': 'Year 1',
    'gradePage.year.sophomore': 'Year 2',
    'gradePage.year.junior': 'Year 3',
    'gradePage.year.senior': 'Year 4',
    'gradePage.table.course': 'Course',
    'gradePage.table.credit': 'Credits',
    'gradePage.table.score': 'Score',
    'gradePage.actionSheet.manageCache': 'Manage Cache Settings',
    'gradePage.actionSheet.updateCache': 'Refresh Cached Data',
    'gradePage.actionSheet.cancel': 'Cancel'
  }

  const t = (key) => messages[key] ?? key

  it('builds localized year tabs', () => {
    expect(createGradeYearTabs(t)).toEqual([
      { value: 0, label: 'Year 1' },
      { value: 1, label: 'Year 2' },
      { value: 2, label: 'Year 3' },
      { value: 3, label: 'Year 4' }
    ])
  })

  it('builds localized table headers', () => {
    expect(createGradeTableHeaders(t)).toEqual({
      course: 'Course',
      credit: 'Credits',
      score: 'Score'
    })
  })

  it('builds localized action sheet labels', () => {
    expect(createGradeActionSheetItems(t)).toEqual({
      manageCache: 'Manage Cache Settings',
      updateCache: 'Refresh Cached Data',
      cancel: 'Cancel'
    })
  })
})
