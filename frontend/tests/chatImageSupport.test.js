import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { readFileSync } from 'node:fs'
import { webcrypto } from 'node:crypto'
import { resolveChatImage, inspectChatImage, validateChatImage, bytesFromImageDataUrl, chatMessagePreview } from '../src/views/social/chatImageSupport.js'
import { handleSocialRequest } from '../src/mock/social-handlers.js'

const me = '11111111-1111-4111-8111-111111111111'
const peer = '22222222-2222-4222-8222-222222222222'
const jpeg = new Uint8Array(readFileSync('public/img/landing/campus-hero.jpg'))
const png = Uint8Array.from(atob('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+a5k0AAAAASUVORK5CYII='), c => c.charCodeAt(0))
const route = '/api/social/conversations/12/messages/34/image'

describe('private chat image boundary', () => {
  it('accepts only the exact configured API origin and numeric media route', () => {
    expect(resolveChatImage(route, 'https://api.example.test/api', 'https://web.example.test')).toEqual({
      url: `https://api.example.test${route}`, requestPath: route.slice(4)
    })
    for (const url of [`https://evil.test${route}`, `//api.example.test${route}`, `${route}?token=secret`, `${route}#x`, route.replace('/34/', '/not-an-id/'), `${route}/`, 'data:image/png;base64,AA==', `https://user:pass@api.example.test${route}`]) {
      expect(resolveChatImage(url, 'https://api.example.test/api', 'https://web.example.test'), url).toBeNull()
    }
  })

  it('checks actual PNG/JPEG headers, dimensions and advertised MIME', () => {
    expect(inspectChatImage(jpeg)).toMatchObject({ contentType: 'image/jpeg' })
    expect(inspectChatImage(png)).toEqual({ width: 1, height: 1, contentType: 'image/png' })
    const huge = png.slice()
    new DataView(huge.buffer).setUint32(16, 4097)
    expect(() => inspectChatImage(huge)).toThrow('imageDimensionsError')
    expect(() => inspectChatImage(new TextEncoder().encode('<svg/>'))).toThrow('imageFormatError')
    expect(() => bytesFromImageDataUrl(`data:image/jpeg;base64,${btoa(String.fromCharCode(...png))}`)).toThrow('imageFormatError')
    expect(validateChatImage({ type: 'image/png', size: 5 * 1024 * 1024 + 1 })).toBe('imageSizeError')
    expect(validateChatImage({ type: 'image/svg+xml', size: 10 })).toBe('imageFormatError')
  })

  it('uses translated image previews and supports old text messages', () => {
    expect(chatMessagePreview({ type: 'IMAGE', content: '' }, '[图片]', '空')).toBe('[图片]')
    expect(chatMessagePreview({ content: '你好' }, '[图片]', '空')).toBe('你好')
  })
})

describe('demo multipart image contract', () => {
  let utils, conversationId
  const imageId = 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa'
  afterEach(() => vi.unstubAllGlobals())
  const send = (id = imageId, bytes = png, type = 'image/png') => {
    const form = new FormData()
    form.append('clientMessageId', id)
    form.append('image', new File([bytes], 'image', { type }))
    return handleSocialRequest('POST', `/api/social/conversations/${conversationId}/messages/image`, {}, form, 'tok', utils)
  }
  beforeEach(async () => {
    vi.stubGlobal('crypto', webcrypto)
    let state = { token: 'tok', profile: { nickname: 'demo' }, social: null }
    utils = {
      readState: () => state, writeState: next => { state = next },
      buildSuccess: data => ({ success: true, data }), resolveWithDelay: payload => Promise.resolve(payload),
      rejectWithMessage: (message, options = {}) => Promise.reject(Object.assign(new Error(message), options)),
      ensureAuthorized: token => token === 'tok' ? null : Promise.reject(new Error('auth'))
    }
    await handleSocialRequest('PUT', `/api/social/users/${peer}/follow`, {}, {}, 'tok', utils)
    state.social.follows.push({ followerId: peer, followeeId: me })
    const created = await handleSocialRequest('POST', '/api/social/conversations', {}, { peerId: peer }, 'tok', utils)
    conversationId = created.data.id
  })

  it('stores private bytes, exposes only proxy metadata, retries even after privacy tightens', async () => {
    const first = await send()
    expect(first.data).toMatchObject({ type: 'IMAGE', content: '', image: { width: 1, height: 1, contentType: 'image/png' } })
    expect(first.data).not.toHaveProperty('_imageKey')
    expect(first.data).not.toHaveProperty('_imageDigest')
    utils.readState().social.users[peer].dmPolicy = 'NONE'
    const retry = await send()
    expect(retry.data.id).toBe(first.data.id)
    expect(utils.readState().social.messages[conversationId]).toHaveLength(1)
    const blob = await handleSocialRequest('GET', first.data.image.url, {}, {}, 'tok', utils)
    expect(blob).toBeInstanceOf(Blob)
    expect(blob.size).toBe(png.length)
    await expect(send('bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb')).rejects.toMatchObject({ errorCode: 'PRIVACY_RESTRICTED' })
  })

  it('rejects another payload or text reuse of the same client ID', async () => {
    await send()
    await expect(send(imageId, jpeg, 'image/jpeg')).rejects.toMatchObject({ errorCode: 'CLIENT_MESSAGE_CONFLICT' })
    await expect(handleSocialRequest('POST', `/api/social/conversations/${conversationId}/messages`, {}, { clientMessageId: imageId, content: 'other' }, 'tok', utils)).rejects.toMatchObject({ errorCode: 'CLIENT_MESSAGE_CONFLICT' })
    expect(utils.readState().social.messages[conversationId]).toHaveLength(1)
  })

  it('rejects missing membership and invalid authorization on download', async () => {
    const first = await send()
    delete utils.readState().social.conversations[conversationId].members[me]
    await expect(handleSocialRequest('GET', first.data.image.url, {}, {}, 'tok', utils)).rejects.toMatchObject({ statusCode: 404 })
    await expect(handleSocialRequest('GET', first.data.image.url, {}, {}, 'wrong', utils)).rejects.toThrow('auth')
  })

  it('keeps concurrent duplicate submissions to a single committed sequence', async () => {
    const [first, second] = await Promise.all([send(), send()])
    expect(first.data.id).toBe(second.data.id)
    expect(utils.readState().social.messages[conversationId]).toHaveLength(1)
    expect(utils.readState().social.conversations[conversationId].lastSeq).toBe(1)
  })
})
