import { describe, expect, it } from 'vitest'
import { createCampusOptions, createPeriodOptions } from './spareContent'

describe('spare localized content', () => {
  const messages = {
    'spare.option.unlimited': 'Unlimited',
    'spare.campus.haizhu': 'Haizhu',
    'spare.period.morning': 'Morning'
  }

  const t = (key) => messages[key] ?? key

  it('builds localized campus options', () => {
    const options = createCampusOptions(t)
    expect(options[0]).toEqual({ label: 'Unlimited', value: 0 })
    expect(options[1]).toEqual({ label: 'Haizhu', value: 1 })
  })

  it('builds localized period options', () => {
    const options = createPeriodOptions(t)
    expect(options.at(-5)).toEqual({ label: 'Morning', value: 6 })
  })
})
