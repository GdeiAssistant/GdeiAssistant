import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const source = readFileSync(join(dirname(fileURLToPath(import.meta.url)), 'Profile.vue'), 'utf8')

describe('Profile layout ownership', () => {
  it('keeps social stats inside the avatar header card and privacy under settings', () => {
    expect(source).toContain('profile-stat-link')
    expect(source).toContain('to="/social/search"')
    expect(source).toContain('to="/user/privacy-setting"')
    expect(source).not.toContain('to="/social/privacy"')
    expect(source).not.toContain('to="/social/blocks"')
    expect(source).not.toMatch(/social\.statsTitle/)
  })
})
