import { describe, it, expect } from 'vitest'
import { resolveSocialAvatar } from '../src/composables/socialAvatarUrl.js'

const path = '/api/social/users/11111111-1111-4111-8111-111111111111/avatar'
describe('social avatar authentication boundary', () => {
  it('authenticates only the exact same-origin API avatar', () => {
    expect(resolveSocialAvatar(path, 'https://api.example/api', 'https://web.example').authenticated).toBe(true)
    expect(resolveSocialAvatar('/img/avatar/default.png', 'https://api.example/api', 'https://web.example').url).toBe('https://web.example/img/avatar/default.png')
    for (const value of ['https://cdn.example/avatar.png', `http://api.example${path}`, `https://api.example:444${path}`, 'https://api.example/other.png']) {
      expect(resolveSocialAvatar(value, 'https://api.example/api', 'https://web.example').authenticated).toBe(false)
    }
  })
  it('rejects unsafe image URLs and missing avatars', () => {
    for (const value of [null, '', '//api.example/avatar', 'javascript:alert(1)', 'https://user@api.example/avatar']) {
      expect(resolveSocialAvatar(value, '/api', 'https://web.example')).toBeNull()
    }
  })
})
