import { describe, expect, it } from 'vitest'

import { maskCampusAccount, maskIdentifier } from './mask'

describe('mask utilities', () => {
  it('does not leak the original suffix when visibleEnd is zero', () => {
    const raw = 'abc'

    const masked = maskIdentifier(raw, 2, 2)

    expect(masked).toBe('ab****')
    expect(masked).not.toContain(raw)
  })

  it('keeps short campus account masking redacted', () => {
    const raw = 'stu'

    const masked = maskCampusAccount(raw)

    expect(masked).toBe('st****')
    expect(masked).not.toContain(raw)
  })
})
