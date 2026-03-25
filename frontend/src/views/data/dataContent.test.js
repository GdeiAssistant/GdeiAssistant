import { describe, expect, it } from 'vitest'
import { createElectricityResultFields } from './dataContent'

describe('data localized content', () => {
  const messages = {
    'electricityFees.result.year': 'Year',
    'electricityFees.result.totalElectricBill': 'Total Bill'
  }

  const t = (key) => messages[key] ?? key

  it('builds localized electricity result fields', () => {
    const fields = createElectricityResultFields(t)
    expect(fields[0]).toEqual({ key: 'year', label: 'Year' })
    expect(fields.at(-2)).toEqual({ key: 'totalElectricBill', label: 'Total Bill' })
  })
})
