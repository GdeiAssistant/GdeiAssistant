import { describe, expect, it } from 'vitest'
import {
  createScheduleDayLabels,
  createScheduleDayOptions,
  createScheduleMessages,
  createWeekOptions
} from './scheduleContent'

describe('schedule localized content', () => {
  const messages = {
    'schedule.day.monday': 'Mon',
    'schedule.day.tuesday': 'Tue',
    'schedule.day.wednesday': 'Wed',
    'schedule.day.thursday': 'Thu',
    'schedule.day.friday': 'Fri',
    'schedule.day.saturday': 'Sat',
    'schedule.day.sunday': 'Sun',
    'schedule.weekLabel': 'Week {week}',
    'schedule.message.syncLoading': 'Syncing schedule...',
    'schedule.message.duplicateCourse': 'Course already exists in this time slot'
  }

  const t = (key, params = {}) => {
    const template = messages[key] ?? key
    return template.replace(/\{(\w+)\}/g, (_, name) => String(params[name] ?? `{${name}}`))
  }

  it('builds localized day labels and options', () => {
    expect(createScheduleDayLabels(t)).toEqual(['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'])
    expect(createScheduleDayOptions(t)[0]).toEqual({ label: 'Mon', value: 1 })
  })

  it('builds localized week picker options', () => {
    expect(createWeekOptions(t, 3)).toEqual([
      { label: 'Week 1', value: 1 },
      { label: 'Week 2', value: 2 },
      { label: 'Week 3', value: 3 }
    ])
  })

  it('exposes translated schedule messages', () => {
    expect(createScheduleMessages(t).syncLoading).toBe('Syncing schedule...')
    expect(createScheduleMessages(t).duplicateCourse).toBe('Course already exists in this time slot')
  })
})
