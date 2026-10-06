import { describe, it, expect, beforeEach } from 'vitest'
import { handleSocialRequest } from '../src/mock/social-handlers.js'
import { handleRequest } from '../src/mock/index.js'

function makeUtils() {
  let state = {
    token: 'tok',
    profile: { nickname: 'GDEI小助手', introduction: 'demo' },
    social: null
  }
  return {
    readState: () => state,
    writeState: (next) => { state = next },
    buildSuccess: (data) => ({ success: true, code: 200, message: 'success', data }),
    rejectWithMessage: (message, options = {}) => Promise.reject(Object.assign(new Error(message), options)),
    resolveWithDelay: (payload) => Promise.resolve(payload),
    ensureAuthorized: (token) => (token === 'tok' ? null : Promise.reject(new Error('auth')))
  }
}

describe('social mock contract', () => {
  let utils
  beforeEach(() => {
    utils = makeUtils()
  })

  it('preserves follow and privacy through the actual storage adapter', async () => {
    localStorage.clear()
    localStorage.setItem('mockRuntimeState', JSON.stringify({ token: 'tok', profile: { nickname: 'demo' } }))
    const peer = '22222222-2222-4222-8222-222222222222'
    await handleRequest({ method: 'PUT', path: `/api/social/users/${peer}/follow`, token: 'tok' })
    const me = await handleRequest({ method: 'GET', path: '/api/social/me', token: 'tok' })
    expect(me.data.followingCount).toBe(1)
    await handleRequest({ method: 'PUT', path: '/api/social/privacy', token: 'tok', data: { dmPolicy: 'NONE' } })
    const privacy = await handleRequest({ method: 'GET', path: '/api/social/privacy', token: 'tok' })
    expect(privacy.data.dmPolicy).toBe('NONE')
  })

  it('seeds peer one-way follow so first-run mutual DM is reachable', async () => {
    const me = '11111111-1111-4111-8111-111111111111'
    const peer = '22222222-2222-4222-8222-222222222222'
    await handleSocialRequest('GET', '/api/social/me', {}, {}, 'tok', utils)
    expect(utils.readState().social.follows).toEqual([{ followerId: peer, followeeId: me }])
  })

  it('follow mutual then allow dm under MUTUAL policy', async () => {
    const me = '11111111-1111-4111-8111-111111111111'
    const peer = '22222222-2222-4222-8222-222222222222'
    await handleSocialRequest('GET', '/api/social/me', {}, {}, 'tok', utils)
    // Isolate the privacy gate: start without the default peer→me seed.
    const cleared = utils.readState()
    cleared.social.follows = []
    utils.writeState(cleared)

    await handleSocialRequest('PUT', `/api/social/users/${peer}/follow`, {}, {}, 'tok', utils)
    // peer does not follow back yet
    const create = handleSocialRequest('POST', '/api/social/conversations', {}, { peerId: peer }, 'tok', utils)
    await expect(create).rejects.toMatchObject({ errorCode: 'PRIVACY_RESTRICTED' })

    // simulate mutual by writing reverse follow
    const social = utils.readState().social
    social.follows.push({ followerId: peer, followeeId: me })
    utils.writeState({ ...utils.readState(), social })

    const ok = await handleSocialRequest('POST', '/api/social/conversations', {}, { peerId: peer }, 'tok', utils)
    expect(ok.data.id).toBeTruthy()
  })

  it('message retry same body is idempotent and conflict on different body', async () => {
    const peer = '22222222-2222-4222-8222-222222222222'
    const social = (await (async () => {
      await handleSocialRequest('PUT', `/api/social/users/${peer}/follow`, {}, {}, 'tok', utils)
      const s = utils.readState().social
      s.follows.push({ followerId: peer, followeeId: '11111111-1111-4111-8111-111111111111' })
      utils.writeState({ ...utils.readState(), social: s })
      return s
    })())
    const created = await handleSocialRequest('POST', '/api/social/conversations', {}, { peerId: peer }, 'tok', utils)
    const cid = created.data.id
    const clientMessageId = 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa'
    const first = await handleSocialRequest('POST', `/api/social/conversations/${cid}/messages`, {}, {
      clientMessageId, content: '你好'
    }, 'tok', utils)
    const second = await handleSocialRequest('POST', `/api/social/conversations/${cid}/messages`, {}, {
      clientMessageId, content: '你好'
    }, 'tok', utils)
    expect(second.data.id).toBe(first.data.id)
    await expect(handleSocialRequest('POST', `/api/social/conversations/${cid}/messages`, {}, {
      clientMessageId, content: '不同'
    }, 'tok', utils)).rejects.toMatchObject({ errorCode: 'CLIENT_MESSAGE_CONFLICT' })
    expect(social.messages[cid].length).toBe(1)
  })
})
